package assortment_of_things.abyss.scripts

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI
import com.fs.starfarer.api.util.IntervalUtil
import org.magiclib.kotlin.tryGet

class ForceNegAbyssalRep : EveryFrameScript {
    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return false
    }

    var interval = IntervalUtil(0.3f, 0.5f)

    override fun advance(amount: Float) {

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            var player = Global.getSector().playerFaction

            var factions = ArrayList<FactionAPI>()
            for (faction in Global.getSector().allFactions) {
                if (faction.custom.tryGet<Boolean>("rat_abyss_faction") { false }) {
                    factions.add(faction)
                }
            }

            for (faction in factions) {
                faction.setRelationship(player.id, -1f)
                for (other in factions) {
                    if (other == faction) continue
                    faction.setRelationship(other.id, 1f)
                }
            }

            /*Global.getSector().getFaction("rat_abyssals")?.setRelationship(player.id, -1f)
            Global.getSector().getFaction("rat_abyssals_deep")?.setRelationship(player.id, -1f)
            Global.getSector().getFaction("rat_abyssals_deep_seraph")?.setRelationship(player.id, -1f)
            Global.getSector().getFaction("rat_abyssals_primordials")?.setRelationship(player.id, -1f)

            Global.getSector().getFaction("rat_abyssals_deep")?.setRelationship("rat_abyssals_deep_seraph", 1f)*/
        }

    }

}