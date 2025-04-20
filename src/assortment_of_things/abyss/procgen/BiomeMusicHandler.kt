package assortment_of_things.abyss.procgen

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl
import com.fs.starfarer.api.util.IntervalUtil

class BiomeMusicHandler : EveryFrameScript {

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return false
    }


    var skipInterval = true

    var checkInterval = IntervalUtil(0.25f, 0.3f)

    override fun advance(amount: Float) {

        if (!AbyssUtils.isPlayerInAbyss()) {
            skipInterval = true
            return
        }

        checkInterval.advance(amount)
        if (checkInterval.intervalElapsed() || skipInterval) {

            var data = AbyssUtils.getData()
            var mananger = data.biomeManager

            var dominant = mananger.getDominantBiome()

            //Just to ensure the right music is played when entering the system
            if (skipInterval) {
                skipInterval = false
                var key = dominant.getMusicKeyId()
                if (key != null)  {
                    AbyssUtils.getData().system!!.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, key)
                }
                return
            }

            //To make it so that flying a tiny bit backwards does not immediately change the music set again
            var level = mananger.getBiomeLevels().get(dominant)!!
            if (level >= 0.60f) {
                var key = dominant.getMusicKeyId()
                if (key != null) {
                    AbyssUtils.getData().system!!.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, key)
                }
            }

        }

    }

}