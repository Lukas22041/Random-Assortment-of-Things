package assortment_of_things.abyss.procgen.scripts

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.light.AbyssalLight
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.lazywizard.lazylib.MathUtils

class AbyssalLightDiscovery() : EveryFrameScript {

    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }

    var discovered = ArrayList<SectorEntityToken>()

    override fun advance(amount: Float) {
        var system = AbyssUtils.getSystem()
        if (system == Global.getSector().currentLocation) {

            var manager = AbyssUtils.getBiomeManager()

            var viewport = Global.getSector().viewport

            var player = Global.getSector().playerFleet
            var radar = Global.getSettings().getFloat("campaignRadarRadius") + 600f

            var devmode = Global.getSettings().isDevMode

            for (entity in system!!.customEntities) {

                var plugin = entity.customPlugin
                if (plugin !is AbyssalLight) continue

                var isDiscovered = discovered.contains(entity)

                if (!isDiscovered) {
                    if (MathUtils.getDistance(player, entity) <= radar || viewport.isNearViewport(entity.location, plugin.radius/10)) {
                        //entity.setSensorProfile(null)
                        var cell = manager.getCell(entity)
                        cell.isDiscovered = true

                        for (adjacent in cell.getAdjacent()) {
                            if (adjacent.getAdjacent().any { it.isPartialyDiscovered }) {
                                adjacent.isPartialyDiscovered = true
                            }
                        }

                        discovered.add(entity)
                    }
                }

                if (devmode) {
                    entity.setSensorProfile(null)
                } else if (!isDiscovered) {
                    entity.setSensorProfile(1f)
                } else {
                    entity.setSensorProfile(null)
                }

            }
        }
    }
}