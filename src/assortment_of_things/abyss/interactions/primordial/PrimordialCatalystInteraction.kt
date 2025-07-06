package assortment_of_things.abyss.interactions.primordial

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.items.cores.officer.PrimordialCore
import assortment_of_things.abyss.procgen.biomes.PrimordialWaters
import assortment_of_things.misc.*
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import com.fs.state.AppDriver
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.makeImportant
import java.util.*

class PrimordialCatalystInteraction : RATInteractionPlugin() {

    override fun init() {

        if (interactionTarget.hasTag("completed")) {
            textPanel.addPara("The station continues to generate small pulses, the transformed landscape providing more energy dense material to sustain itself through.")
            addLeaveOption()
            return
        }

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)


        textPanel.addPara("The fleet closes in for further investigation. " +
                "Scans of the interior structure reveal lines of circuitry spread across, with no hollow spaces to be seen, " +
                "with the exception of what appears to be some kind of storage facility.")

        textPanel.addPara("More detailed analysis discover some kind of intake for surrounding abyssal matter, and when another pulse of energy has been released, the facility all but emptied once more. ")

        textPanel.addPara("This construct appears to have some practical use case for abyssal matter, but this regions local density does not appear to be enough to fuel its purpose. Providing it with more may have some effect.")

        var tooltip = textPanel.beginTooltip()

        tooltip.addSpacer(10f)

        var path = "graphics/icons/cargo/rat_abyssal_matter.png"
        Global.getSettings().getAndLoadSprite(path)
        var img = tooltip.beginImageWithText(path, 48f)
        img.addPara("Pure samples of Abyssal Matter are required for this action. Atleast 200 units are needed for a larger reaction to occur. " /*+
                "Supplying even more may increase the intensity of whatever will occur."*/, 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Abyssal Matter", "200")
        tooltip.addImageWithText(0f)

        textPanel.addTooltip()

        var cargo = Global.getSector().playerFleet.cargo
        var count = cargo.getCommodityQuantity("rat_abyssal_matter")

        createOption("Supply 200 units of abyssal matter to the catalyst") {
            clearOptions()

            interactionTarget.addTag("completed")

            cargo.removeCommodity("rat_abyssal_matter", 200f)

            textPanel.addPara("Your team prepares the transfer of abyssal matter towards the entities storage hold, " +
                    "waiting for the next pulse before making their transfer, and rushing to be done before the next one. ")

            AddRemoveCommodity.addCommodityLossText("rat_abyssal_matter", 200, textPanel)

            textPanel.addPara("With the transfer completed, there is nothing left to be done but to wait for the next emission.")


            createOption("Wait") {
                Global.getSector().addScript(BiomeTransformScript(interactionTarget))

                var state = AppDriver.getInstance().currentState
                var core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI?
                core?.opacity = 0f

                closeDialog()
            }

        }
        if (count < 200 && !Global.getSettings().isDevMode) {
            optionPanel.setEnabled("Supply 200 units of abyssal matter to the catalyst", false)
            optionPanel.setTooltip("Supply 200 units of abyssal matter to the catalyst", "You do not have enough abyssal matter for this option.")
        }

       /* createOption("Supply 300 units of abyssal matter to the catalyst") {
            clearOptions()

            interactionTarget.addTag("completed")

            cargo.removeCommodity("rat_abyssal_matter", 300f)

            textPanel.addPara("Your team prepares the transfer of abyssal matter towards the entities storage hold, " +
                    "waiting for the next pulse before making their transfer, and rushing to be done before the next one. ")

            AddRemoveCommodity.addCommodityLossText("rat_abyssal_matter", 300, textPanel)

            textPanel.addPara("With the transfer completed, there is nothing left to be done but to wait for the next emission.")

            createOption("Wait") {
                Global.getSector().addScript(BiomeTransformScript(interactionTarget))
                closeDialog()
            }
        }
        if (count < 300 && !Global.getSettings().isDevMode) {
            optionPanel.setEnabled("Supply 300 units of abyssal matter to the catalyst", false)
            optionPanel.setTooltip("Supply 300 units of abyssal matter to the catalyst", "You do not have enough abyssal matter for this option.")
        }*/

        addLeaveOption()
    }

    class FadeInUIAFterTransformScript() : EveryFrameScript {

        var finished = false
        var fade = 0f

        override fun isDone(): Boolean {
            return finished
        }

        override fun runWhilePaused(): Boolean {
           return true
        }


        override fun advance(amount: Float) {
            if (!Global.getSector().isPaused) {
                fade += 1f * amount
            }

            var alpha = MathUtils.clamp(fade, 0f, 1f)

            var state = AppDriver.getInstance().currentState
            var core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI?
            core?.setOpacity(alpha)

            if (fade >= 1f) {
                finished = true
            }
        }

    }

    class BiomeTransformScript(var catalyst: SectorEntityToken) : EveryFrameScript {

        var delay = 3f
        var finished = false
        var playedSound = false
        var spawnedFleet = false

        var system = AbyssUtils.getSystem()
        var biome = AbyssUtils.getBiomeManager().getBiome(PrimordialWaters::class.java) as PrimordialWaters
        var photosphere = biome.majorLightsources.first()

        var spawnDistance = 200f
        var angle = MathUtils.getRandomNumberInRange(0f, 360f)

        var singularitySpawnLoc = MathUtils.getPointOnCircumference(photosphere.location, photosphere.radius + spawnDistance, angle)

        var fleet: CampaignFleetAPI? = null
        var teleportedNextFrame = false

        override fun isDone(): Boolean {
            return finished
        }


        override fun runWhilePaused(): Boolean {
            return false
        }

        override fun advance(amount: Float) {

            var mult = 1f
            if (Global.getSector().isFastForwardIteration) {
                mult = Global.getSettings().getFloat("campaignSpeedupMult")
            }

            if (delay >= 0f) {
                delay -= amount / mult
                return
            }

            if (!playedSound) {
                playedSound = true
                Global.getSoundPlayer().playSound("rat_genesis_system_sound", 0.8f, 1.2f, catalyst.location, Vector2f())

                GraphicLibEffects.CustomCampaignRippleDistortion(catalyst.location, Vector2f(), 2000f, 15f, true, 0f, 360f, 1f
                    ,1f, 1f, 1f, 1f, 1f)

                GraphicLibEffects.CustomCampaignBubbleDistortion(catalyst.location, Vector2f(), 1000f , 25f, true, 0f, 360f, 1f
                    ,0.1f, 0.1f, 1f, 0.3f, 1f)
            }

            if (!teleportedNextFrame && fleet != null) {
                teleportedNextFrame = true
                fleet!!.setCircularOrbitWithSpin(photosphere, angle, spawnDistance, -90f, 3f, 3f)
            }

            if (!spawnedFleet && MathUtils.getDistance(catalyst, singularitySpawnLoc) < biome.getRadius()) {
                spawnedFleet = true
                fleet = createFleet()
            }

            //Force invisible ui, just in case the player reloaded
            var state = AppDriver.getInstance().currentState
            var core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI?
            core?.opacity = 0f

            biome.effectLevel += 0.25f * amount / mult
            if (biome.effectLevel >= 1f) {
                finished = true
                Global.getSector().addScript(FadeInUIAFterTransformScript())
            }

        }

        fun createFleet() : CampaignFleetAPI {
            var fleet = Global.getFactory().createEmptyFleet("rat_abyssals_primordials", "Singularity", true)
            fleet.isNoFactionInName = true

            var boss = fleet.fleetData.addFleetMember("rat_genesis_Standard")
            boss.fixVariant()

            boss.variant.addTag(Tags.TAG_NO_AUTOFIT)
            boss.variant.addTag(Tags.VARIANT_UNBOARDABLE)

            //boss.variant.addTag(Tags.SHIP_LIMITED_TOOLTIP)
            boss!!.variant.addTag("rat_really_not_recoverable")

            var core = PrimordialCore().createPerson(RATItems.PRIMORDIAL, "rat_abyssals_primordials", Random())
            boss.captain = core

            fleet.addTag("rat_genesis_fleet")
            fleet.memoryWithoutUpdate.set("\$defenderFleet", fleet)

            system!!.addEntity(fleet)

            RemnantSeededFleetManager.addRemnantInteractionConfig(fleet)

            fleet.stats.sensorProfileMod.modifyMult("rat_genesis", 1f)

            fleet.makeImportant("")

            fleet.setAI(null)
            //fleet.setCircularOrbitWithSpin(photosphere, angle, radius, 90f, 3f, 3f)
            fleet.location.set(Vector2f(1000000f, 100000f))

            fleet.memoryWithoutUpdate[MemFlags.FLEET_IGNORES_OTHER_FLEETS] = true
            fleet.memoryWithoutUpdate[MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS] = true
            fleet.addScript(PrimFleetScript(fleet, photosphere))

            fleet.setSensorProfile(null)
            fleet.setDiscoverable(null)
            fleet.forceNoSensorProfileUpdate = true

            //fleet.forceSensorFaderBrightness(0f)
            //fleet.fadeInIndicator()

            return fleet
        }

        class PrimFleetScript(var fleet: CampaignFleetAPI, var token: SectorEntityToken) : EveryFrameScript {

            override fun isDone(): Boolean {
                return false
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {

                /*if (fleet.isAlive && !fleet.isDespawning && !fleet.memoryWithoutUpdate.contains(MemFlags.ENTITY_MISSION_IMPORTANT)) {
                    fleet.makeImportant("")
                }*/

                /* if (fleet.isAlive && !fleet.isDespawning &&!fleet.isCurrentAssignment(FleetAssignment.ORBIT_PASSIVE)) {
                     fleet.clearAssignments()
                     fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, token, 9999999f, "Waiting")
                     fleet.facing = Random().nextFloat() * 360f
                 }*/
            }

        }

    }
}