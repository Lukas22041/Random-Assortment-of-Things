package assortment_of_things.abyss.misc

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SpecialItemData
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
import org.magiclib.achievements.MagicAchievement
import org.magiclib.achievements.MagicAchievementManager
import second_in_command.SCUtils


//Loosely based on the SoTF dustkeeper start
class AbyssStart : CustomStart() {


    override fun getDisabledTooltip(): String? {

        if (!RATSettings.enableAbyss!!) {
            return "This start is only available with the Abyss enabled in the \"Random Assortment of Things\" config."
        }

        if (!MagicAchievementManager.getInstance().getAchievement("rat_beatSingularity")!!.isComplete) {
            return "Requires facing the singularity at the bottom of the abyss to be unlocked."
        }

        return null
    }

    override fun execute(dialog: InteractionDialogAPI, memoryMap: MutableMap<String, MemoryAPI>) {

        val data = memoryMap[MemKeys.LOCAL]!!["\$characterData"] as CharacterCreationData
        var textPanel = dialog.textPanel
        var optionPanel = dialog.optionPanel

        //data.characterData.memoryWithoutUpdate.set("\$rat_started_abyss", true)

       /* if (!RATSettings.enableAbyss!!) {
            textPanel.addPara("This start is only available with the Abyss enabled in the \"Random Assortment of Things\" config.",
                Misc.getTextColor(), Misc.getHighlightColor(),
                "Abyss", "Random Assortment of Things");

            optionPanel.addOption(StringHelper.getString("done", true), "fakeDone")
            optionPanel.setEnabled("fakeDone", false)
            optionPanel.addOption(StringHelper.getString("back", true), "nex_NGCStartBack")
            return
        }*/

        textPanel.addPara("You ventured in to the vast abyssal depths, having lost most of what you entered with, but emerging with equipment before unseen.")

        textPanel.addPara("The fleet encountered many threats they were not prepared for, but through excessive hardship, managed to prevail. Though most of the fleet has not made it through, you decide to repurpose your findings for a new purpose.")

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER)
        val tempFleet = FleetFactoryV3.createEmptyFleet(PlayerFactionStore.getPlayerFactionIdNGC(), FleetTypes.PATROL_SMALL, null)


        var tooltip = textPanel.beginTooltip()

        tooltip.addSpacer(5f)

        if (Global.getSettings().modManager.isModEnabled("second_in_command")) {
            var abyssalImg = tooltip.beginImageWithText("graphics/secondInCommand/abyssal/abyssal_ships.png", 48f)
            abyssalImg.addPara("Start with an executive officer recovered from cryo-pods within the depths.", 0f,
                Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "executive officer")
            tooltip.addImageWithText(0f)
        } else {
            var automatedIMG = tooltip.beginImageWithText("graphics/icons/skills/automated_ships.png", 48f)
            automatedIMG.addPara("Start with the \"Automated Ships\" skill.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "Automated Ships")
            tooltip.addImageWithText(0f)
        }


        tooltip.addSpacer(10f)

        var crewConversionIMG = tooltip.beginImageWithText("graphics/hullmods/rat_crew_conversion.png", 48f)
        crewConversionIMG.addPara("The flagship has been converted for a human crew and you start with more tools for the automated fleet.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "crew")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var levelIMG = tooltip.beginImageWithText(data.characterData.person.portraitSprite, 48f)
        levelIMG.addPara("Start at level 3.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "3")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(5f)

        textPanel.addTooltip()

        addMember("rat_aboleth_m_Attack", dialog, data, tempFleet)
        addMember("rat_chuul_Attack", dialog, data, tempFleet)
        addMember("rat_chuul_Strike", dialog, data, tempFleet)
        addMember("rat_makara_Attack", dialog, data, tempFleet)
        addMember("rat_makara_Strike", dialog, data, tempFleet)
        addMember("rat_merrow_Attack", dialog, data, tempFleet)

        addMember("buffalo_d_Standard", dialog, data, tempFleet)


        tempFleet.fleetData.setSyncNeeded()
        tempFleet.fleetData.syncIfNeeded()
        tempFleet.forceSync()

       /* var coordinates = MathUtils.getRandomPointInCircle(Vector2f(), 10000f)
        data.startingLocationName = "Sea of Twilight"*/


        var fuel = 0f
        var supplies = 0f
        for (member in tempFleet.fleetData.membersListCopy) {
            fuel += (member.fuelCapacity.toInt() * 0.9f).toInt()
            supplies += (member.cargoCapacity.toInt() * 0.9f).toInt()
        }

        var crew = 50f
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.CREW, crew)

        data.startingCargo.addSpecial(SpecialItemData("rat_artifact", "computational_matrix"), 1f)
        //data.startingCargo.addSpecial(SpecialItemData("rat_alteration_install", "rat_abyssal_conversion"), 1f)
        data.startingCargo.credits.add(100000f)
        AddRemoveCommodity.addCreditsGainText(100000, textPanel)
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, RATItems.CHRONOS_CORE, 1f)
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, RATItems.COSMOS_CORE, 1f)

        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.FUEL, fuel)
        data.startingCargo.addItems(CargoAPI.CargoItemType.RESOURCES, Commodities.SUPPLIES, supplies)

        textPanel.setFontSmallInsignia()
        textPanel.addPara("Gained 1x Computational Matrix", Misc.getPositiveHighlightColor(), Misc.getHighlightColor(), "1x")
        //textPanel.addPara("Gained 1x Abyssal Crew Conversions", Misc.getPositiveHighlightColor(), Misc.getHighlightColor(), "1x")
        textPanel.setFontInsignia()
        AddRemoveCommodity.addCommodityGainText(RATItems.CHRONOS_CORE, 1, textPanel)
        AddRemoveCommodity.addCommodityGainText(RATItems.COSMOS_CORE, 1,textPanel)
       /* AddRemoveCommodity.addCommodityGainText(Commodities.FUEL, fuel.toInt(), textPanel)
        AddRemoveCommodity.addCommodityGainText(Commodities.SUPPLIES, supplies.toInt(),textPanel)
        AddRemoveCommodity.addCommodityGainText(Commodities.CREW, crew.toInt(), textPanel)*/

        PlayerFactionStore.setPlayerFactionIdNGC(Factions.PLAYER)
        ExerelinSetupData.getInstance().freeStart = true

        data.person.stats.addPoints(1)

        data.addScript {

            val fleet = Global.getSector().playerFleet

            if (Global.getSettings().modManager.isModEnabled("second_in_command")) {
                var officer = SCUtils.createRandomSCOfficer("rat_abyssal")
                officer.increaseLevel(1)

                SCUtils.getPlayerData().addOfficerToFleet(officer)
                SCUtils.getPlayerData().setOfficerInEmptySlotIfAvailable(officer)
            } else {
                Global.getSector().getPlayerPerson().getStats().setSkillLevel(Skills.AUTOMATED_SHIPS, 1f);
            }


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
                //val max = member.repairTracker.maxCR
                member.repairTracker.cr = 0.7f
            }

            var player = Global.getSector().playerFleet.commanderStats
            var plugin = Global.getSettings().levelupPlugin
            for (i in 0 until 2) {
                var amount = plugin.getXPForLevel(Math.min(plugin.getMaxLevel(), player.getLevel() + 1)) - player.getXP();
                var added = Math.min(amount, plugin.getXPForLevel(plugin.getMaxLevel()))
                player.addXP(added)
            }

            fleet.fleetData.setSyncNeeded()

            for (member in fleet.fleetData.membersListCopy) {
                member.setStatUpdateNeeded(true)
            }

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