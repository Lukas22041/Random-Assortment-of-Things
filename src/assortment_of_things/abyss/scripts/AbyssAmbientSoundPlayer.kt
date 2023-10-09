package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class AbyssAmbientSoundPlayer : EveryFrameScript {

    var interval = IntervalUtil(180f, 300f)

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return false
    }

    override fun advance(amount: Float) {

        var system = Global.getSector()?.playerFleet?.containingLocation ?: return

        if (system.hasTag(AbyssUtils.SYSTEM_TAG)) {
            var mult = 1f
            if (Global.getSector().isFastForwardIteration) {
                mult = Global.getSettings().getFloat("campaignSpeedupMult")
            }


            interval.advance(amount / mult)
        }
        else {
            return
        }

        if (interval.intervalElapsed()) {

            var fleetLocation = Global.getSector().playerFleet.location
            var location = MathUtils.getRandomPointInCircle(fleetLocation, 400f)

            Global.getSoundPlayer().playSound("rat_abyss_ambient_sounds", 1f, 3f, location, Global.getSector().playerFleet.velocity)
        }
    }

}