package assortment_of_things.abyss.misc

import assortment_of_things.RATCampaignPlugin
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssData
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import assortment_of_things.misc.instantTeleport
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.EveryFrameScript
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
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.util.*


//Loosely based on the SoTF dustkeeper start
class AbyssStart : CustomStart() {

    override fun execute(dialog: InteractionDialogAPI, memoryMap: MutableMap<String, MemoryAPI>) {

        val data = memoryMap[MemKeys.LOCAL]!!["\$characterData"] as CharacterCreationData
        var textPanel = dialog.textPanel
        var optionPanel = dialog.optionPanel

        //data.characterData.memoryWithoutUpdate.set("\$rat_started_abyss", true)

        if (!RATSettings.enableAbyss!!) {
            textPanel.addPara("This start is only available with the Abyss enabled in the \"Random Assortment of Things\" config.",
                Misc.getTextColor(), Misc.getHighlightColor(),
                "Abyss", "Random Assortment of Things");

            optionPanel.addOption(StringHelper.getString("done", true), "fakeDone")
            optionPanel.setEnabled("fakeDone", false)
            optionPanel.addOption(StringHelper.getString("back", true), "nex_NGCStartBack")
            return
        }

        textPanel.addPara("Your fleet made the risky move to explore the unknown, venturing in to the depths.")

        textPanel.addPara("It encountered automated threats that it made quick work off, however in the process much of the fleet was lost. The decision was made to make new purpose of the defeated fleets, transfering and renovating them for your own use.")

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER)
        val tempFleet = FleetFactoryV3.createEmptyFleet(PlayerFactionStore.getPlayerFactionIdNGC(), FleetTypes.PATROL_SMALL, null)


        var tooltip = textPanel.beginTooltip()

        tooltip.addSpacer(5f)

        var automatedIMG = tooltip.beginImageWithText("graphics/icons/skills/automated_ships.png", 48f)
        automatedIMG.addPara("Start with the \"Automated Ships\" skill.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Automated Ships")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var crewConversionIMG = tooltip.beginImageWithText("graphics/hullmods/rat_crew_conversion.png", 48f)
        crewConversionIMG.addPara("The flagship has been converted for a human crew and you start with more tools for the automated fleet.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "crew")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var levelIMG = tooltip.beginImageWithText(data.characterData.person.portraitSprite, 48f)
        levelIMG.addPara("Start at level 4.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "4")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(5f)

        textPanel.addTooltip()

        addMember("rat_aboleth_m_Attack", dialog, data, tempFleet)
        addMember("rat_chuul_Attack", dialog, data, tempFleet)
        addMember("rat_chuul_Strike", dialog, data, tempFleet)
        addMember("rat_makara_Attack", dialog, data, tempFleet)
        addMember("rat_makara_Strike", dialog, data, tempFleet)
        addMember("rat_merrow_Attack", dialog, data, tempFleet)



        tempFleet.fleetData.setSyncNeeded()
        tempFleet.fleetData.syncIfNeeded()
        tempFleet.forceSync()

       /* var coordinates = MathUtils.getRandomPointInCircle(Vector2f(), 10000f)
        data.startingLocationName = "Sea of Twilight"*/


        var fuel = 0f
        var supplies = 0f
        for (member in tempFleet.fleetData.membersListCopy) {
            fuel += (member.fuelCapacity.toInt() * 0.8f).toInt()
            supplies += (member.cargoCapacity.toInt() * 0.8f).toInt()
        }

        var crew = 30f
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.CREW, crew)

        data.startingCargo.addSpecial(SpecialItemData("rat_artifact", "computational_matrix"), 1f)
        data.startingCargo.addSpecial(SpecialItemData("rat_alteration_install", "rat_abyssal_conversion"), 1f)
        data.startingCargo.credits.add(250000f)
        AddRemoveCommodity.addCreditsGainText(250000, textPanel)
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, RATItems.CHRONOS_CORE, 3f)
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, RATItems.COSMOS_CORE, 3f)

        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.FUEL, fuel)
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.SUPPLIES, supplies)

        textPanel.setFontSmallInsignia()
        textPanel.addPara("Gained 1x Computational Matrix", Misc.getPositiveHighlightColor(), Misc.getHighlightColor(), "1x")
        textPanel.addPara("Gained 1x Abyssal Crew Conversions", Misc.getPositiveHighlightColor(), Misc.getHighlightColor(), "1x")
        textPanel.setFontInsignia()
        AddRemoveCommodity.addCommodityGainText(RATItems.CHRONOS_CORE, 3, textPanel)
        AddRemoveCommodity.addCommodityGainText(RATItems.COSMOS_CORE, 3,textPanel)
        AddRemoveCommodity.addCommodityGainText(Commodities.FUEL, fuel.toInt(), textPanel)
        AddRemoveCommodity.addCommodityGainText(Commodities.SUPPLIES, supplies.toInt(),textPanel)
        AddRemoveCommodity.addCommodityGainText(Commodities.CREW, crew.toInt(), textPanel)

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER)
        ExerelinSetupData.getInstance().freeStart = true


        data.addScript {

            val fleet = Global.getSector().playerFleet

            Global.getSector().getPlayerPerson().getStats().setSkillLevel(Skills.AUTOMATED_SHIPS, 1f);

            NGCAddStandardStartingScript.adjustStartingHulls(fleet)

            for (member in fleet.fleetData.membersListCopy) {

                member.fixVariant()

                if (member.baseOrModSpec().hullId == "rat_aboleth_m") {
                    member.variant!!.addMod("rat_chronos_conversion")
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
            for (i in 0 until 3) {
                var amount = plugin.getXPForLevel(Math.min(plugin.getMaxLevel(), player.getLevel() + 1)) - player.getXP();
                var added = Math.min(amount, plugin.getXPForLevel(plugin.getMaxLevel()))
                player.addXP(added)
            }

            fleet.fleetData.setSyncNeeded()

        }

        dialog.visualPanel.showFleetInfo(StringHelper.getString("exerelin_ngc", "playerFleet", true),
            tempFleet, null, null)

        //optionPanel.addOption(StringHelper.getString("done", true), "nex_NGCDone");
        optionPanel.addOption(StringHelper.getString("done", true), "rat_NGCDone_Abyss");
        optionPanel.addOption(StringHelper.getString("back", true), "nex_NGCStartBack");
    }



    fun addMember(variantID: String, dialog: InteractionDialogAPI, data: CharacterCreationData, fleet: CampaignFleetAPI, core: String? = null) {
        data.addStartingFleetMember(variantID, FleetMemberType.SHIP)
        val member: FleetMemberAPI = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantID)
        fleet.fleetData.addFleetMember(member)
        member.repairTracker.cr = 0.7f

        if (member.baseOrModSpec().hullId == "rat_aboleth_m") {
            member.variant!!.addMod("rat_chronos_conversion")
            member.isFlagship = true
            fleet.fleetData.setFlagship(member)
        }


       // AddRemoveCommodity.addFleetMemberGainText(member.variant, dialog.textPanel)
    }
}