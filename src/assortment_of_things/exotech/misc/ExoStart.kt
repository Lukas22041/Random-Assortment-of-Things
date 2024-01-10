package assortment_of_things.exotech.misc

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.interactions.exoship.ExoShipBuyInteraction
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.rules.MemKeys
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.CharacterCreationData
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.impl.campaign.rulecmd.NGCAddStandardStartingScript
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.ExerelinSetupData
import exerelin.campaign.PlayerFactionStore
import exerelin.campaign.customstart.CustomStart
import exerelin.utilities.StringHelper


//Loosely based on the SoTF dustkeeper start
class ExoStart : CustomStart() {

    override fun execute(dialog: InteractionDialogAPI, memoryMap: MutableMap<String, MemoryAPI>) {

        val data = memoryMap[MemKeys.LOCAL]!!["\$characterData"] as CharacterCreationData
        var textPanel = dialog.textPanel
        var optionPanel = dialog.optionPanel

        if (!RATSettings.exoEnabled!!) {
            textPanel.addPara("This start is only available with \"Exo-Tech\" enabled in the \"Random Assortment of Things\" config.",
                Misc.getTextColor(), Misc.getHighlightColor(),
                "Exo-Tech", "Random Assortment of Things");

            optionPanel.addOption(StringHelper.getString("done", true), "fakeDone")
            optionPanel.setEnabled("fakeDone", false)
            optionPanel.addOption(StringHelper.getString("back", true), "nex_NGCStartBack")
            return
        }

        textPanel.addPara("Your fleet is a trusted partner of the exo-tech corperation, A minor-faction hiding within the fringes of the sector on their mobile colonies.")

        textPanel.addPara("You made faithfull \"Donations\" and got rewarded with some of their technologies in exchange. " +
                "Despite this, the faction still doesnt recognize you as one of their own, leaving you to fend for yourself.")

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER)
        val tempFleet = FleetFactoryV3.createEmptyFleet(PlayerFactionStore.getPlayerFactionIdNGC(), FleetTypes.PATROL_SMALL, null)


        var tooltip = textPanel.beginTooltip()

        tooltip.addSpacer(5f)

        var tokens = Misc.getDGSCredits(1250f)
        var factionIMG = tooltip.beginImageWithText(Global.getSettings().getFactionSpec("rat_exotech").crest, 48f)
        factionIMG.addPara("Starts with an active \"Exo-Tech Partnership\". Shows the location of all Exoships in intel and allows purchasing their equipment. " +
                "You also start with $tokens of their tokens. The faction requires further trade-ins of rare tech for more purchases.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Exo-Tech Partnership", "$tokens")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        Global.getSettings().getAndLoadSprite("graphics/icons/campaign/event_neutral.png")
        var extraIMG = tooltip.beginImageWithText("graphics/icons/campaign/event_neutral.png", 48f)
        extraIMG.addPara("You are not commissioned to the faction and start near a random market.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "not")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var credits = Misc.getDGSCredits(100000f)
        var levelIMG = tooltip.beginImageWithText(data.characterData.person.portraitSprite, 48f)
        levelIMG.addPara("Start at level 3 & with $credits credits.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "3", "$credits")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(5f)

        textPanel.addTooltip()

        addMember("rat_arkas_Strike", dialog, data, tempFleet)
        addMember("rat_tylos_Standard", dialog, data, tempFleet)
        addMember("rat_tylos_Standard", dialog, data, tempFleet)
        addMember("rat_thestia_Support", dialog, data, tempFleet)
        addMember("rat_thestia_Support", dialog, data, tempFleet)

        tempFleet.fleetData.setSyncNeeded()
        tempFleet.fleetData.syncIfNeeded()
        tempFleet.forceSync()

       /* var coordinates = MathUtils.getRandomPointInCircle(Vector2f(), 10000f)
        data.startingLocationName = "Sea of Twilight"*/

        var fuel = 0f
        var supplies = 0f
        var crew = 0f
        for (member in tempFleet.fleetData.membersListCopy) {
            fuel += (member.fuelCapacity.toInt() * 0.8f).toInt()
            supplies += (member.cargoCapacity.toInt() * 0.8f).toInt()
            crew += member.minCrew + 5
        }

        data.startingCargo.credits.add(100000f)
        AddRemoveCommodity.addCreditsGainText(100000, textPanel)

        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.CREW, crew)

        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.FUEL, fuel)
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.SUPPLIES, supplies)

        AddRemoveCommodity.addCommodityGainText(Commodities.FUEL, fuel.toInt(), textPanel)
        AddRemoveCommodity.addCommodityGainText(Commodities.SUPPLIES, supplies.toInt(),textPanel)
        AddRemoveCommodity.addCommodityGainText(Commodities.CREW, crew.toInt(), textPanel)

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER)
        ExerelinSetupData.getInstance().freeStart = true

        data.addScript {

            val fleet = Global.getSector().playerFleet
            Global.getSector().getFaction("rat_exotech").relToPlayer.adjustRelationship(1f, RepLevel.WELCOMING)

            NGCAddStandardStartingScript.adjustStartingHulls(fleet)

            for (member in fleet.fleetData.membersListCopy) {

                member.fixVariant()

                if (member.baseOrModSpec().hullId == "rat_arkas_Strike") {
                    member.isFlagship = true
                    fleet.fleetData.setFlagship(member)
                }

                val max = member.repairTracker.maxCR
                member.repairTracker.cr = 0.7f

            }

            fleet.fleetData.ensureHasFlagship()

            for (member in fleet.fleetData.membersListCopy) {
                val max = member.repairTracker.maxCR
                member.repairTracker.cr = max
            }

            var player = Global.getSector().playerFleet.commanderStats
            var plugin = Global.getSettings().levelupPlugin
            player.addPoints(1)
            for (i in 0 until 2) {
                var amount = plugin.getXPForLevel(Math.min(plugin.getMaxLevel(), player.getLevel() + 1)) - player.getXP();
                var added = Math.min(amount, plugin.getXPForLevel(plugin.getMaxLevel()))
                player.addXP(added)
            }

            fleet.fleetData.setSyncNeeded()

            ExoUtils.getExoData().interactedWithExoship = true
            ExoUtils.getExoData().hasPartnership = true
            ExoUtils.getExoData().tokens = 1250f

            ExoShipBuyInteraction.unlockExoIntel(null, true)
        }

        dialog.visualPanel.showFleetInfo(StringHelper.getString("exerelin_ngc", "playerFleet", true),
            tempFleet, null, null)

        //optionPanel.addOption(StringHelper.getString("done", true), "nex_NGCDone");
        optionPanel.addOption(StringHelper.getString("done", true), "nex_NGCDone");
        optionPanel.addOption(StringHelper.getString("back", true), "nex_NGCStartBack");
    }



    fun addMember(variantID: String, dialog: InteractionDialogAPI, data: CharacterCreationData, fleet: CampaignFleetAPI, core: String? = null) {
        data.addStartingFleetMember(variantID, FleetMemberType.SHIP)
        val member: FleetMemberAPI = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantID)
        fleet.fleetData.addFleetMember(member)
        member.repairTracker.cr = 0.7f

        if (member.baseOrModSpec().hullId == "rat_arkas_Strike") {
            member.isFlagship = true
            fleet.fleetData.setFlagship(member)
        }

       // AddRemoveCommodity.addFleetMemberGainText(member.variant, dialog.textPanel)
    }
}