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
        var system = Global.getSector().playerFleet.containingLocation
        if (system.hasTag(AbyssUtils.SYSTEM_TAG)) {
            var ability = Global.getSector().playerFleet.getAbility(Abilities.TRANSVERSE_JUMP)
            ability?.cooldownLeft = 1f

           /* Global.getSettings().getFactionSpec("player").color = AbyssUtils.ABYSS_COLOR
            Global.getSettings().getFactionSpec("player").baseUIColor = AbyssUtils.ABYSS_COLOR
            Global.getSettings().getFactionSpec("player").darkUIColor = AbyssUtils.ABYSS_COLOR
            Global.getSettings().getFactionSpec("player").brightUIColor = AbyssUtils.ABYSS_COLOR
            Global.getSettings().getFactionSpec("player").secondaryUIColor = AbyssUtils.ABYSS_COLOR
            Global.getSettings().getFactionSpec("player").gridUIColor = AbyssUtils.ABYSS_COLOR

            Global.getSettings().basePlayerColor*/
        }
    }
}