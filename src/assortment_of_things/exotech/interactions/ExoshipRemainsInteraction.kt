package assortment_of_things.exotech.interactions

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.ExotechGenerator
import assortment_of_things.exotech.entities.ExoshipEntity
import assortment_of_things.exotech.intel.ExoshipIntel
import assortment_of_things.exotech.interactions.exoship.ExoshipRecoveryContactInteraction
import assortment_of_things.exotech.interactions.questBeginning.ExoshipRemainsIntel
import assortment_of_things.exotech.items.ExoProcessor
import assortment_of_things.exotech.submarkets.ExotechSubmarketPlugin
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.fixVariant
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.getExoData
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.InteractionDialogImageVisual
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaTextfield
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.getStorage
import java.util.*

class ExoshipRemainsInteraction : RATInteractionPlugin() {

    var data = ExoUtils.getExoData()

    override fun init() {
        textPanel.addPara("Your fleet approaches the remains.")


        if (data.readyToRepairExoship) {

            var intel = Global.getSector().intelManager.getFirstIntel(ExoshipRemainsIntel::class.java) as ExoshipRemainsIntel?
            if (intel == null) {
                intel = ExoshipRemainsIntel()
            }
            intel.endImmediately()


            textPanel.addPara("As if you had no time to spare, the fleet readies up to prepare the repair protocol. A last run of diagnostics are done and the broken catalyst is replaced with the new one")

            Global.getSector().playerFleet.cargo.removeItems(CargoAPI.CargoItemType.SPECIAL, SpecialItemData("rat_warp_catalyst", ""), 1f)

            textPanel.addPara("With just the press of a button, the procedure can now be begun.")

            createOption("Initate the repair sequence") {
                clearOptions()

                textPanel.addPara("Within seconds hundreds of maintenance hatches open across the stations hull, and thousands of drones leave its confines.")

                textPanel.addPara("It just takes a few hours, and the surface of the ship exterior looks as good as new. Some of the interior will require some finer touches, but nothing throws off the diagonstics anymore.")

                createOption("Continue") {
                    clearOptions()

                    textPanel.addPara("Before innitiating a warp to test its functionality however, it may be fitting to provide the ship with a new name.")

                    var tooltip = textPanel.beginTooltip()

                    tooltip.addSpacer(5f)

                    var textfield = tooltip.addLunaTextfield("Aurora", false, 180f, 30f).apply {
                        enableTransparency = true
                    }

                    textPanel.addTooltip()

                    createOption("Initiate a warp") {
                        clearOptions()

                        closeDialog()

                        data.recoveredExoship = true

                        var data = ExoUtils.getExoData()

                        var playerExoship = Global.getSector().hyperspace.addCustomEntity("exoship_${Misc.genUID()}", "Daybreak", "rat_exoship", "rat_exotech")
                        var playerPlugin = playerExoship.customPlugin as ExoshipEntity

                        playerExoship.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "rat_exo_market")

                        var market = ExotechGenerator.addMarketplace(Factions.PLAYER,
                            playerExoship,
                            arrayListOf(),
                            "Daybreak",
                            3,
                            arrayListOf(Conditions.OUTPOST),
                            arrayListOf("rat_exoship_market", Submarkets.SUBMARKET_STORAGE),
                            arrayListOf(Industries.MEGAPORT),
                            0.3f,
                            false,
                            false)
                        market.isHidden = true

                        //Should prevent them from building stuff on it
                        market.isPlayerOwned = false

                        market.admin = data.amelie
                        data.amelie.postId = "stationCommander"

                        (market.getStorage() as StoragePlugin).setPlayerPaidToUnlock(true)

                        playerExoship.orbit = null

                        playerExoship.name = textfield.getText()


                        //Fix submarket to prevent non-restockable to be restocked
                        var oldSubmarket = data.getExoship().market.getSubmarket("rat_exoship_market")?.plugin as ExotechSubmarketPlugin?
                        if (oldSubmarket != null) {
                            var newSubmarket = playerExoship.market.getSubmarket("rat_exoship_market").plugin as ExotechSubmarketPlugin

                            newSubmarket.first = oldSubmarket.first
                            newSubmarket.cargo.addAll(oldSubmarket.cargo)

                            oldSubmarket.cargo.clear()
                        }


                        var exoshipToken = Global.getSector().hyperspace.createToken(interactionTarget.location)

                        playerExoship.setCircularOrbit(exoshipToken, interactionTarget.facing, 0.1f, 999f)

                        playerExoship.facing = interactionTarget.facing

                        Global.getSector().hyperspace.removeEntity(interactionTarget)

                        data.setPlayerExoship(playerExoship)
                        playerPlugin.npcModule.isPlayerOwned = true
                        playerPlugin.playerModule.isPlayerOwned = true

                        var deepSpace = Global.getSector().createStarSystem("Deep Space_${Misc.genUID()}")
                        deepSpace.initNonStarCenter()
                        deepSpace.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
                        deepSpace.addTag(Tags.THEME_HIDDEN)
                        deepSpace.name = "Deep Space"
                        deepSpace.location.set(MathUtils.getRandomPointOnCircumference(Vector2f(), 3000f))
                        deepSpace.backgroundTextureFilename = "graphics/backgrounds/exo/rat_exo_deepspace.jpg"
                        deepSpace.addTag("do_not_show_stranded_dialog")

                        var planet = deepSpace.addPlanet("rat_deepspace_planet", deepSpace.center, "Rogue Planet", "barren", 0f, 300f, 1f, 900f)
                        planet.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)
                        planet.descriptionIdOverride = "barren_deep_space"
                        planet.addTag(Tags.NON_CLICKABLE)

                        //Do not let the NPC exoship commit any new warps right now
                        data.preventNPCWarps = true


                        if (Global.getSector().playerFleet.hasAbility("fracture_jump")) {
                            Global.getSector().playerFleet.getAbility("fracture_jump").cooldownLeft = 99999f
                        }

                        playerPlugin.warpModule.warp(planet, true, true) {
                            playerExoship.addTag(Tags.NON_CLICKABLE)
                            Global.getSector().getExoData().getExoshipPlugin().warpModule.doQuestlineWarp(planet) {
                                Global.getSector().getExoData().getExoship().addTag(Tags.NON_CLICKABLE)
                                Global.getSector().campaignUI.showInteractionDialog(ExoshipRecoveryContactInteraction(), Global.getSector().getExoData().getExoship())
                            }
                        }
                    }
                }
            }

        }
        else if (data.foundExoshipRemains) {

            textPanel.addPara("Your fleet takes positon around the remains. Without a warp catalyst however, not much is to be done here.")

            addLeaveOption()
            return
        }

        var defender = interactionTarget.memoryWithoutUpdate.get("\$defenderFleet")
        if (defender == null && !data.defeatedRemainsDefenses) {
            defender = generateFleet()
        }

        if (!data.foundExoshipRemains) {

            if (data.QuestBeginning_StartedFromExoship) {
                foundFromExoshipQuest()
            }
            else {
                data.QuestBeginning_StartedFromRemains = true
                foundFromRemainsQuest()
            }

        }

    }

    fun foundFromExoshipQuest() {
        textPanel.addPara("You followed the directions provided by the mysterious figure and now truly find yourself in front of a sight to behold.")

        textPanel.addPara("All Exoships, structures of incredible potential within the collapsed sector, have been known to be destroyed over several wars - with the exception of one.\n\n" +
                "Yet here you stand, in front a broken, yet prospective ship, appearing almost frozen in time. ")

        createOption("Continue") {

            textPanel.addPara("However the exciting news takes a stop there - as the fleet suddenly detects a lone ship maneuvering around the derelict.")

            textPanel.addPara("Scopes indicate it to be of Exotech nature, but it appears not to be manned. Further approach requires its elimination.")

            triggerDefenders()
        }

    }


    fun foundFromRemainsQuest() {
        textPanel.addPara("What your fleet spots is hard to belive, an exoship from a previous era. ")

        textPanel.addPara("All Exoships, structures of incredible potential within the collapsed sector, have been known to be destroyed over several wars - with the exception of one.\n\n" +
                "Yet here you stand, in front a broken, yet prospective ship, appearing almost frozen in time. ")

        createOption("Continue") {

            textPanel.addPara("However the exciting news takes a stop there - as the fleet suddenly detects a lone ship maneuvering around the derelict.")

            textPanel.addPara("Scopes indicate it to be of Exotech nature, but it appears not to be manned. Further approach requires its elimination.")

            triggerDefenders()
        }
    }






    override fun defeatedDefenders() {
        data.defeatedRemainsDefenses = true

        textPanel.addPara("With the defending ship defeated, the fleet is able to close in on the target. \n\n" +
                "But before you continue further in, the fleet makes note of discovering some type of AI-Core not typicaly seen within the sector inside of the automated drone. The crew recovers this artifact for later inspection.")

        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_ai_core_special", "rat_exo_processor"), 1f)

        var tooltip = textPanel.beginTooltip()

        var img = tooltip.beginImageWithText(Global.getSettings().getCommoditySpec("rat_exo_processor").iconName, 48f)
        img.addPara("Recovered an \"Exo-Processor\".", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Exo-Processor")
        tooltip.addImageWithText(0f)

        textPanel.addTooltip()

        createOption("Continue") {
            clearOptions()

            var path = interactionTarget.customEntitySpec.interactionImage
            var sprite = Global.getSettings().getAndLoadSprite(path)
            var interactionImage = InteractionDialogImageVisual(path, sprite.width, sprite.height)
            visualPanel.showImageVisual(interactionImage)

            textPanel.addPara("You enter the confines of the station. While the station appears heavily damaged from the outside, its interiors seem to only have the occasional gap from the shell of a railgun.")

            textPanel.addPara("After passing through multiple airlocks you make it to the heart of the construct, a massive city-like landscape build around the floor, ceiling and walls of the station. When it was still in use it could have likely housed a population in the counts of hundreds of thousands.")

            textPanel.addPara("Nearby the crew finds a yet functioning terminal, due to outdated security protocols, they are able to work themself in and manage to open a list of crew logs and access to the stations control panel.")

            createOption("Read the latest log") {
                optionPanel.setEnabled("Read the latest log", false)

                var logPara = textPanel.beginTooltip()

                logPara.addPara("" +
                        "Date Unknown - Repair Effort\n\n" +
                        "" +
                        "We've worked days on getting the warp chamber running, but it appears we are out of luck. " +
                        "A shell took out one of the catalysts, and as we have no spare on board, there is no chance of getting it working again. \n\n" +
                        "" +
                        "Our attempts at connecting to the navigational network are just as futile, we have no idea where within the Persean Abyss we are. " +
                        "If this situation continues we will have to leave the exoship with the ships docked in the hangar, and hope we can make it to a nearby star system before running out of resources. \n\n" +
                        "" +
                        "End of log.", 0f,
                Misc.getTextColor(), Misc.getHighlightColor(), "catalysts")

                textPanel.addTooltip()
            }

            createOption("Access the control panel") {
                clearOptions()

                data.foundExoshipRemains = true

                textPanel.addPara("You gain access to the control panel, and it quickly throws out multiple warnings regarding the current state of the station. \n\n" +
                        "Surprisingly however, the requirements for repairing the ship appear rather humble. Most of it can be done by specialised repair drones docked within the ship. " +
                        "The panel however also states the requirement of a \"Warp Catalyst\", a device you find no information about in the fleet's database. Without it, the warp chamber will not be able to be activated, and the ship remains stranded here.",
                Misc.getTextColor(), Misc.getHighlightColor(), "Warp Catalyst")

                if (data.QuestBeginning_StartedFromExoship) {
                    textPanel.addPara("The person that send you towards this location may know more about this object.")
                }

                var intel = Global.getSector().intelManager.getFirstIntel(ExoshipRemainsIntel::class.java)
                if (intel == null) {
                    intel = ExoshipRemainsIntel()
                    Global.getSector().intelManager.addIntel(intel)
                }

                Global.getSector().intelManager.addIntelToTextPanel(intel, textPanel)

                if (data.QuestBeginning_StartedFromRemains) {

                    createOption("Continue") {
                        clearOptions()

                        textPanel.addPara("With no information on where to acquire such an object, the crew went digging for further clues. " +
                                "Deeper within the system they discovered access codes and locations of some hypernavigational beacons in the sector. \n\n" +
                                "The Exotech faction uses such devices to plan their warps; infiltrating this network with our own navigation equipment enables us to discover the location of their active Exoship, which may provide us with more information.")

                        var exoshipIntel = ExoshipIntel(data.getExoship())
                        Global.getSector().intelManager.addIntel(exoshipIntel)
                        Global.getSector().intelManager.addIntelToTextPanel(exoshipIntel, textPanel)

                        addLeaveOption()
                    }
                }
                else {
                    addLeaveOption()
                }
            }

           /* if (!data.foundExoshipRemains && data.QuestBeginning_Active) {
                defeatedFromExoship()
            }*/

        }




    }

    fun defeatedFromExoship() {


    }

    fun generateFleet() : CampaignFleetAPI {
        var fleet = Global.getFactory().createEmptyFleet("rat_exotech", "Automated Defenses", false)
        fleet.name = "Automated Defenses"
        fleet.isNoFactionInName = true
        fleet.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true)
        fleet.inflateIfNeeded()

        var arkas = fleet.fleetData.addFleetMember("rat_arkas_Strike")
        var arkasCore = ExoProcessor().createPerson("rat_exo_processor", "rat_exotech", Random())
        arkas.captain = arkasCore
        //fleet.fleetData.setFlagship(arkas)

        arkas.fixVariant()
        arkas.variant.addPermaMod(HullMods.AUTOMATED)
        arkas.variant.addTag(Tags.TAG_AUTOMATED_NO_PENALTY)
        arkas.repairTracker.cr = 1f
        arkas.variant.addTag(Tags.TAG_NO_AUTOFIT)

        arkas.variant.addPermaMod("rat_exo_experimental")

        arkas.variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE)
        arkas.variant.addTag(Tags.SHIP_RECOVERABLE)

        fleet.inflateIfNeeded()
        interactionTarget.memoryWithoutUpdate.set("\$defenderFleet", fleet)

        return fleet
    }

}