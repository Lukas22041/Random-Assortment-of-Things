package assortment_of_things.abyss.misc

import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
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
class AbyssStart : CustomStart() {
    override fun execute(dialog: InteractionDialogAPI, memoryMap: MutableMap<String, MemoryAPI>) {

        val data = memoryMap[MemKeys.LOCAL]!!["\$characterData"] as CharacterCreationData
        var textPanel = dialog.textPanel
        var optionPanel = dialog.optionPanel

        if (!RATSettings.enableAbyss!!) {
            textPanel.addPara("This start is only available with the Abyss enabled in the \"Random Assortment of Things\" config.",
                Misc.getTextColor(), Misc.getHighlightColor(),
                "Abyss", "Random Assortment of Things");

            optionPanel.addOption(StringHelper.getString("done", true), "fakeDone")
            optionPanel.setEnabled("fakeDone", false)
            optionPanel.addOption(StringHelper.getString("back", true), "nex_NGCStartBack")
            return
        }


        textPanel.addPara("You are the captain of a fleet that discovered a domain-era device that allows access to the \"Abyss\".\n" +
                "After days of surving the unique terrain, a hostile fleet appeared with the signature of a unique hull that has not been recorded so far.\n\n" +
                "Its presence on the battlefield was devastating, putting most ships in to a barely functioning state. " +
                "However, eventualy the fleet turned out victorious, and to make up for the damage received, an effort has been made to recover the unique ship.")

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER)
        val tempFleet = FleetFactoryV3.createEmptyFleet(PlayerFactionStore.getPlayerFactionIdNGC(), FleetTypes.PATROL_SMALL, null)
        addMember("rat_sarakiel_Standard", dialog, data, tempFleet)

        var tooltip = textPanel.beginTooltip()

        var automatedIMG = tooltip.beginImageWithText("graphics/icons/skills/automated_ships.png", 48f)
        automatedIMG.addPara("Start with the \"Automated Ships\" skill.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Automated Ships")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)
        var singularityIMG = tooltip.beginImageWithText("graphics/icons/abilities/rat_singularity_jump.png", 48f)
        singularityIMG.addPara("Start with the \"Singularity Jump\" ability, allowing immediate access to the abyss.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Singularity Jump")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)
        var conversionIMG = tooltip.beginImageWithText("graphics/hullmods/rat_crew_conversion.png", 48f)
        conversionIMG.addPara("The \"Sarakiel\" has an alteration installed that alows humans to crew it instead of AI.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Sarakiel")
        tooltip.addImageWithText(0f)

        textPanel.addTooltip()


        data.startingCargo.credits.add(250000f)
        AddRemoveCommodity.addCreditsGainText(250000, textPanel)

        tempFleet.fleetData.setSyncNeeded()
        tempFleet.fleetData.syncIfNeeded()
        tempFleet.forceSync()


        var crew = 0f
        var fuel = 0f
        var supplies = 0f
        for (member in tempFleet.fleetData.membersListCopy) {
            crew += (member.minCrew + ((member.maxCrew - member.minCrew) * 0.3f).toInt()).toInt()
            fuel += (member.fuelCapacity.toInt() * 0.5f).toInt()
            supplies += member.baseDeploymentCostSupplies.toInt() * 3
        }

        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.CREW, crew)
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.FUEL, fuel)
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.SUPPLIES, supplies)

        AddRemoveCommodity.addCommodityGainText(Commodities.CREW, crew.toInt(), textPanel)
        AddRemoveCommodity.addCommodityGainText(Commodities.FUEL, fuel.toInt(), textPanel)
        AddRemoveCommodity.addCommodityGainText(Commodities.SUPPLIES, supplies.toInt(),textPanel)

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER)
        ExerelinSetupData.getInstance().freeStart = true


        data.addScript {
            val fleet = Global.getSector().playerFleet

            Global.getSector().getCharacterData().memoryWithoutUpdate.set("\$rat_abyssWithCustomStart", true)
            Global.getSector().getCharacterData().addAbility("rat_singularity_jump_ability")
            Global.getSector().getPlayerPerson().getStats().setSkillLevel(Skills.AUTOMATED_SHIPS, 1f);

            NGCAddStandardStartingScript.adjustStartingHulls(fleet)

            for (member in fleet.fleetData.membersListCopy) {

                member.fixVariant()

                if (member.baseOrModSpec().hullId == "rat_sarakiel")
                member.variant.addPermaMod("rat_abyssal_conversion", true)

                val max = member.repairTracker.maxCR
                member.repairTracker.cr = max

            }

            fleet.fleetData.ensureHasFlagship()

            for (member in fleet.fleetData.membersListCopy) {
                val max = member.repairTracker.maxCR
                member.repairTracker.cr = max
            }

            fleet.fleetData.setSyncNeeded()
        }

        dialog.visualPanel.showFleetInfo(StringHelper.getString("exerelin_ngc", "playerFleet", true),
            tempFleet, null, null)

        optionPanel.addOption(StringHelper.getString("done", true), "nex_NGCDone");
        optionPanel.addOption(StringHelper.getString("back", true), "nex_NGCStartBack");
    }



    fun addMember(variantID: String, dialog: InteractionDialogAPI, data: CharacterCreationData, fleet: CampaignFleetAPI) {
        data.addStartingFleetMember(variantID, FleetMemberType.SHIP)
        val member: FleetMemberAPI = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantID)
        fleet.fleetData.addFleetMember(member)
        member.repairTracker.cr = 0.7f

        if (variantID == "rat_sarakiel_Standard") {
            member.fixVariant()
            member.variant.addPermaMod("rat_abyssal_conversion", true)
        }

        AddRemoveCommodity.addFleetMemberGainText(member.variant, dialog.textPanel)
    }
}