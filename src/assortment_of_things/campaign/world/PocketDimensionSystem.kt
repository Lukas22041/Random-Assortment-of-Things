package assortment_of_things.campaign.world

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.ids.Terrain
import com.fs.starfarer.api.impl.campaign.procgen.StarGenDataSpec
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin.CoronaParams
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import java.util.*


class PocketDimensionSystem {

    fun generate() : SectorEntityToken
    {
        var system = Global.getSector().createStarSystem("Pocket Dimension")

        system.addTag(Tags.THEME_HIDDEN)
        system.addTag(Tags.SYSTEM_CUT_OFF_FROM_HYPER)

        system.backgroundTextureFilename = "graphics/backgrounds/pocket_bg.jpg"


        var star = system.initStar("rat_pocket_star", "rat_pocket_blackhole", 200f, 10f)
        val starData = Global.getSettings().getSpec(StarGenDataSpec::class.java, star.getSpec().getPlanetType(), false) as StarGenDataSpec
        val corona: Float = star.radius * (starData.coronaMult + starData.coronaVar * (Random().nextFloat() - 0.5f))
        var horizon = system.addTerrain(Terrain.EVENT_HORIZON, CoronaParams(star.radius + corona, star.radius / 2 + corona, star
            , starData.solarWind, ((starData.getMinFlare() + (starData.getMaxFlare() - starData.getMinFlare()) * Random().nextFloat()).toFloat()), starData.crLossMult))

        horizon.setCircularOrbit(star, 0f, 0f, 100f)
        star.id = "rat_pocket_center"



        var station = system.addCustomEntity("rat_pocket_entity", "Abandoned Station", "station_side06", Factions.NEUTRAL)
        station.setCircularOrbitPointingDown(star, MathUtils.getRandomNumberInRange(0f, 360f), 800f, 300f)

        Misc.setAbandonedStationMarket("rat_pocket_market", station)

        //system.generateAnchorIfNeeded()

        system.addScript(object : EveryFrameScript {
            override fun isDone(): Boolean {
                return false
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {
                var trans = Global.getSector().playerFleet.abilities.get(Abilities.TRANSVERSE_JUMP)
                if (trans != null)
                {
                    if (system == Global.getSector().playerFleet.starSystem)
                    {
                        trans.cooldownLeft = 1f
                    }
                }
            }

        })

        return station
    }

}