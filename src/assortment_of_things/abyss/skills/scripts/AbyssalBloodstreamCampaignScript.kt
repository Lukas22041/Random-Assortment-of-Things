package assortment_of_things.abyss.skills.scripts

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl
import com.fs.starfarer.api.util.IntervalUtil
import org.lwjgl.util.vector.Vector2f

class AbyssalBloodstreamCampaignScript : EveryFrameScript {

    var done = false
    var shownFirstDialog = false
    var deathReason: String? = ""

    var triggeredNeuro = false
    var shownNeuroDialog = false

    @Transient
    var interval: IntervalUtil? = IntervalUtil(1f, 1f)

    override fun isDone(): Boolean {
        return done
    }


    override fun runWhilePaused(): Boolean {
       return false
    }

    override fun advance(amount: Float) {

        if (interval == null) {
            interval = IntervalUtil(1f, 1f)
        }

        interval!!.advance(amount)


        if (interval!!.intervalElapsed()) {
            if (!shownFirstDialog && Global.getSector().campaignUI != null && !Global.getSector().campaignUI.isShowingDialog) {
                var token = Global.getSector().playerFleet.containingLocation.createToken(Vector2f())
                token.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "rat_abyss_interaction2")
                Global.getSector().campaignUI.showInteractionDialog(AbyssalBloodstreamFirstUseInteraction(deathReason), token)
                shownFirstDialog = true
            }

            if (!shownNeuroDialog && triggeredNeuro && Global.getSector().campaignUI != null && !Global.getSector().campaignUI.isShowingDialog) {
                var token = Global.getSector().playerFleet.containingLocation.createToken(Vector2f())
                token.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "rat_abyss_interaction2")
                Global.getSector().campaignUI.showInteractionDialog(AbyssalBloodstreamNeuroCoreInteraction(), token)
                shownNeuroDialog = true
            }
        }

    }
}