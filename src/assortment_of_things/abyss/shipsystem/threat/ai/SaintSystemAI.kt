package assortment_of_things.abyss.shipsystem.threat.ai

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class SaintSystemAI : ShipSystemAIScript {

    var ship: ShipAPI? = null


    var interval = IntervalUtil(0.2f, 0.25f)

    override fun init(ship: ShipAPI?, system: ShipSystemAPI?, flags: ShipwideAIFlags?, engine: CombatEngineAPI?) {
        this.ship = ship
    }

    override fun advance(amount: Float, missileDangerDir: Vector2f?, collisionDangerDir: Vector2f?, target: ShipAPI?) {
        if (ship == null) return
        var flags = ship!!.aiFlags
        var system = ship!!.system

        //Decide on if it should deactivate early
        if (system.state == ShipSystemAPI.SystemState.ACTIVE) {

            //Hulltank instead of overloading when possible
            if (ship!!.fluxLevel >= 0.85f && (ship!!.hitpoints / ship!!.maxHitpoints) >= 0.8f) {
                ship!!.aiFlags.setFlag(ShipwideAIFlags.AIFlags.DO_NOT_USE_SHIELDS, 3f)
                if (ship!!.shield != null && ship!!.shield.isOn) {
                    ship!!.shield.toggleOff()
                }
            }

            if (ship!!.fluxLevel >= 0.95f) {
                ship!!.useSystem()
            }

            //Dont call any of the next code
            return
        }

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            if (ship!!.fluxLevel >= 0.86f) return

            var iter = Global.getCombatEngine().shipGrid.getCheckIterator(ship!!.location, 2000f, 2000f)
            for (target in iter) {
                if (target !is ShipAPI) continue
                if (!target.isAlive) continue
                if (target.owner == ship!!.owner) continue


                //Ignore the requirement later on in to fights.
                if ((ship!!.hitpoints / ship!!.maxHitpoints) >= 0.7 || ship!!.currentCR <= 0.4f) {
                    if (target!!.hullSize == ShipAPI.HullSize.FRIGATE && target.deployCost <= 14) return
                    if (target!!.hullSize == ShipAPI.HullSize.DESTROYER && target.deployCost <= 15) return
                }

                var maxDist = 1600f
                if (target.isCapital) maxDist = 2000f
                if (MathUtils.getDistance(ship, target) >= maxDist) continue

                ship!!.useSystem()
                return
            }
        }


       /* var threat = flags.getCustom(ShipwideAIFlags.AIFlags.BIGGEST_THREAT) as ShipAPI?
        if (threat == null) threat = target

        if (threat == null) return
        if (MathUtils.getDistance(ship!!, target) >= 1600f) return
        if (threat!!.hullSize == ShipAPI.HullSize.FRIGATE && threat.deployCost <= 14) return
        if (threat!!.hullSize == ShipAPI.HullSize.DESTROYER && threat.deployCost <= 18) return

        ship!!.useSystem()*/
    }
}