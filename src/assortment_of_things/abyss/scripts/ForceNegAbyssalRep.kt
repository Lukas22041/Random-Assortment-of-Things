package assortment_of_things.abyss.scripts

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global

class ForceNegAbyssalRep : EveryFrameScript {
    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return false
    }

    override fun advance(amount: Float) {
        var player = Global.getSector().playerFaction
        var faction = Global.getSector().getFaction("rat_abyssals")

        if (faction != null) {
            faction.setRelationship(player.id, -1f)
        }
    }

}