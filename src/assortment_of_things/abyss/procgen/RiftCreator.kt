package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.RiftEntrance
import assortment_of_things.abyss.entities.RiftExit
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

object RiftCreator {

    fun createRift(system: StarSystemAPI, location: Vector2f) : StarSystemAPI {
        var riftEntrance = system.addCustomEntity("rift_entrance_${Misc.genUID()}", "Abyssal Rift", "rat_abyss_rift_entrance", Factions.NEUTRAL)
        riftEntrance.location.set(location)
        AbyssProcgen.clearTerrainAround(riftEntrance, 1000f)

        var riftEntrancePlugin = riftEntrance.customPlugin as RiftEntrance


        var riftSystem = Global.getSector().createStarSystem("${system.baseName} Rift")
        riftSystem.name = "${system.baseName} Rift"

        AbyssProcgen.setupSystem(riftSystem, 0f, AbyssDepth.Deep)


        var data = AbyssUtils.getSystemData(riftSystem)
        data.neighbours.add(system)
        data.mapLocation = null
        AbyssUtils.getSystemData(system).neighbours.add(riftSystem)

        riftSystem.backgroundTextureFilename = "graphics/backgrounds/abyss/darkness.jpg"



        var riftExit = riftSystem.addCustomEntity("rift_entrance_${Misc.genUID()}", "Abyssal Rift", "rat_abyss_rift_exit", Factions.NEUTRAL)
        var riftExitPlugin = riftExit.customPlugin as RiftExit

        riftEntrance.addScript(object: EveryFrameScript {

            var playSound = false

            override fun isDone(): Boolean {
                return false
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {
                var player = Global.getSector().playerFleet

                if (playSound) {
                    Global.getSoundPlayer().playSound("jump_point_open", 1f, 0.8f, Global.getSector().playerFleet.location, Vector2f() )

                    playSound = false
                }

                if (player.containingLocation != riftEntrance.containingLocation) return
                if (MathUtils.getDistance(player.location, riftEntrance.location) < riftEntrancePlugin.radius) {

                    var playerFleet = Global.getSector().playerFleet
                    var currentLocation = playerFleet.containingLocation

                    var angle = Misc.getAngleInDegrees(riftEntrance.location, playerFleet.location)
                    var radius = riftExitPlugin.radius - playerFleet.radius
                    var point = MathUtils.getPointOnCircumference(riftExit.location, radius, angle)


                    currentLocation.removeEntity(playerFleet)
                    riftSystem.addEntity(playerFleet)
                    Global.getSector().setCurrentLocation(riftSystem)


                    playerFleet.setLocation(point.x, point.y)
                    playSound = true

                    riftSystem.isEnteredByPlayer = true

                    CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                }
            }
        })

        riftExit.addScript(object: EveryFrameScript {

            var playSound = false


            override fun isDone(): Boolean {
                return false
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {
                var player = Global.getSector().playerFleet

                if (playSound) {
                    Global.getSoundPlayer().playSound("jump_point_close", 1f, 0.8f, Global.getSector().playerFleet.location, Vector2f() )

                    playSound = false
                }

                if (player.containingLocation != riftExit.containingLocation) return
                if (MathUtils.getDistance(player, riftExit) > riftExitPlugin.radius - player.radius) {

                    var playerFleet = Global.getSector().playerFleet
                    var currentLocation = playerFleet.containingLocation

                    var angle = Misc.getAngleInDegrees(riftExit.location, playerFleet.location)
                    var radius = riftEntrancePlugin.radius + playerFleet.radius + 20f
                    var point = MathUtils.getPointOnCircumference(riftEntrance.location, radius, angle)

                    currentLocation.removeEntity(playerFleet)
                    system.addEntity(playerFleet)
                    Global.getSector().setCurrentLocation(system)

                    playerFleet.setLocation(point.x, point.y)

                    playSound = true

                    Global.getSector().campaignUI
                    CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

                }
            }
        })








        return riftSystem

    }

}