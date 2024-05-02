package assortment_of_things.abyss.boss

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.VignettePanelPlugin
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.InteractionDialogImageVisual
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDDelegate
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUtil.LunaCommons
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.achievements.MagicAchievement
import org.magiclib.achievements.MagicAchievementManager
import org.magiclib.kotlin.makeImportant
import java.util.*

class GenesisInteraction : RATInteractionPlugin() {
    override fun init() {

        var config = FIDConfig()

        config.leaveAlwaysAvailable = true
        config.showCommLinkOption = false
        config.showEngageText = false
        config.showFleetAttitude = false
        config.showTransponderStatus = false
        config.showWarningDialogWhenNotHostile = false
        config.alwaysAttackVsAttack = true
        config.impactsAllyReputation = true
        config.impactsEnemyReputation = false
        config.pullInAllies = false
        config.pullInEnemies = false
        config.pullInStations = false
        config.lootCredits = false

        config.firstTimeEngageOptionText = "Engage in battle"
        config.afterFirstTimeEngageOptionText = "Re-engage the threat"
        config.noSalvageLeaveOptionText = "Continue"

        config.dismissOnLeave = false
        config.printXPToDialog = true

        config.showVictoryText = false
        config.straightToEngage = false
        config.playerAttackingStation = true

        if (interactionTarget is CampaignFleetAPI) {

            textPanel.addPara("As the fleet continues through the dark depths, the sensors pick up the signal of a ship, " +
                    "that just mere moment ago has been entirely undetected, appearing right in the vicinity of our fleet. ")

            textPanel.addPara("The ship is unlike any other we've encountered so far. Our visual monitoring equipment is picking up slight gravitational lensing around the ship, and our spatial measuring scans are going off the charts.")

            textPanel.addPara("Furthermore sensors are picking up the signs of multiple ships in close proximity, however further scans are unable to confirm that data. " +
                    "It is possible that strange phenonema surrounding the ship are falsifying our scans to a certain degree.")

            textPanel.addPara("It appears passive to our presence, but our sensors pick up the presence of heavy ordinance. Approach carefully.")


            triggerDefenders(config, interactionTarget as CampaignFleetAPI)

            var path = "graphics/illustrations/rat_genesis.jpg"
            var sprite = Global.getSettings().getAndLoadSprite(path)
            var interactionImage = InteractionDialogImageVisual("graphics/illustrations/rat_genesis.jpg", sprite.width, sprite.height)

            /*interactionImage.subImageDisplayWidth = sprite.width * 0.20f
            interactionImage.subImageDisplayHeight = sprite.height * 0.20f*/
            visualPanel.showImageVisual(interactionImage)
        }
        else {
            addLeaveOption()
        }


    }

    override fun defeatedDefenders() {

        visualPanel.fadeVisualOut()

        generateWreck()
        generateWormWreck()

        textPanel.addPara("Following the fight, an eerie silence befell the fleets the center of command, even the most experienced crew trying to make sense of what they just experienced.")

        textPanel.addPara("That silence is however quickly broken, as a strong, red flash of energy suddenly erupted from the just now defeated ship.")

        createOption("Continue") {
            clearOptions()

            var plugin = VignettePanelPlugin()
            var panel = visualPanel.showCustomPanel(Global.getSettings().screenWidth, Global.getSettings().screenHeight, plugin)

            var skillSpec = Global.getSettings().getSkillSpec("rat_abyssal_requiem")

            var element = panel.createUIElement(Global.getSettings().screenWidth, Global.getSettings().screenHeight, false)
            panel.addUIElement(element)
            panel.position.inTL(0f, 0f)

            textPanel.addPara("The nearby abyssal matter becomes turbolent, moving the fleet around on a whim, when suddenly, " +
                    "the matter concentrates on to your ship, bleeding through the hull, creating a sight not unlike an ocean of blood.")

            textPanel.addPara("Time comes to a still, only you appear to be able to take note. You try to speak but to no avail. " +
                    "Your senses weaken, but a new one peaks through, one that you havent felt before.")

            textPanel.addPara("Acquired a new skill.", AbyssUtils.ABYSS_COLOR, AbyssUtils.ABYSS_COLOR)

            var tooltip = textPanel.beginTooltip()

            tooltip.setParaFont(Fonts.ORBITRON_12)
            tooltip.addPara("(Hover over the icon for a detailed description)", 0f, Misc.getGrayColor(), Misc.getGrayColor())
            var fake = Global.getFactory().createPerson()
            fake.setFaction("rat_abyssals_deep")
            fake.stats.setSkillLevel(skillSpec.id, 1f)
            tooltip.addSkillPanel(fake, 0f)

            textPanel.addTooltip()

            Global.getSector().playerPerson.stats.setSkillLevel(skillSpec.id, 2f)

            createOption("Continue") {
                clearOptions()

                plugin.decreaseVignette = true

                textPanel.addPara("And in a flash, your back standing within your ship, the hull devoid of any abyssal matter, and the crew still at the silence that befell after disabling the ship. " +
                        "You are unsure of what has happened, but you sense that something has plans for you.", Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "something")

               /* var intel = GenesisRefightintel()
                Global.getSector().intelManager.addIntel(intel)
                Global.getSector().intelManager.addIntelToTextPanel(intel, textPanel)*/


                var fracture = AbyssUtils.getAbyssData().hyperspaceFracture
                var probe = fracture!!.containingLocation.addCustomEntity("rat_genesis_refight", "Unknown Memorial", "rat_genesis_memorial", Factions.NEUTRAL)

                probe.setCircularOrbitWithSpin(fracture, MathUtils.getRandomNumberInRange(0f, 360f), 350f, 150f, 1f, 2f)
                probe.addTag("rat_genesis_refight")

                Misc.makeImportant(probe, null)


                createOption("Leave") {
                    closeDialog()
                    MagicAchievementManager.getInstance().completeAchievement("rat_beatSingularity")
                }



            }

        }


    }

    fun generateWreck() {
        val params = DerelictShipEntityPlugin.createVariant("rat_genesis_Hull", Random(), DerelictShipEntityPlugin.getDefaultSModProb())
        val entity = BaseThemeGenerator.addSalvageEntity(Random(), interactionTarget.starSystem, Entities.WRECK, Factions.NEUTRAL, params) as CustomCampaignEntityAPI

        entity.location.set(Vector2f(interactionTarget.location))
        entity.addTag(AbyssTags.ABYSS_WRECK)
        entity.addTag("Dont_Show_Salvage_Option")
        entity.makeImportant("")
    }

    fun generateWormWreck() {
        val params = DerelictShipEntityPlugin.createVariant("rat_genesis_serpent_head_Hull", Random(), DerelictShipEntityPlugin.getDefaultSModProb())
        val entity = BaseThemeGenerator.addSalvageEntity(Random(), interactionTarget.starSystem, Entities.WRECK, Factions.NEUTRAL, params) as CustomCampaignEntityAPI

        var loc = MathUtils.getRandomPointOnCircumference(interactionTarget.location, 100f)
        entity.location.set(loc)
        entity.addTag(AbyssTags.ABYSS_WRECK)
        entity.addTag("Dont_Show_Salvage_Option")
        entity.makeImportant("")
    }

}