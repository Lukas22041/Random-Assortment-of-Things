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

        Global.getSector().getFaction("rat_abyssals")?.setRelationship(player.id, -1f)
        Global.getSector().getFaction("rat_abyssals_deep")?.setRelationship(player.id, -1f)
        Global.getSector().getFaction("rat_abyssals_deep_seraph")?.setRelationship(player.id, -1f)
        Global.getSector().getFaction("rat_abyssals_primordials")?.setRelationship(player.id, -1f)

        Global.getSector().getFaction("rat_abyssals_deep")?.setRelationship("rat_abyssals_deep_seraph", 1f)

    }

}