package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Abilities

class DisableTransverseScript : EveryFrameScript {
    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
        if (Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))
        {
            var transverse = Global.getSector().playerFleet.getAbility(Abilities.TRANSVERSE_JUMP)
            if (transverse != null)
            {
                transverse.cooldownLeft = transverse.spec.deactivationCooldown
            }
        }
    }

}