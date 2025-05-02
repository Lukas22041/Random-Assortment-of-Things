package assortment_of_things.abyss.interactions.primordial

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.PrimordialWaters
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import org.lwjgl.util.vector.Vector2f

class PrimordialCatalystInteraction : RATInteractionPlugin() {
    override fun init() {

        createOption("Test") {
            Global.getSector().addScript(ZoneExpandingScript(interactionTarget))
            closeDialog()
        }

        addLeaveOption()
    }


    class ZoneExpandingScript(var entity: SectorEntityToken) : EveryFrameScript {

        var delay = 3f
        var finished = false
        var playedSound = false

        override fun isDone(): Boolean {
            return finished
        }


        override fun runWhilePaused(): Boolean {
            return false
        }

        override fun advance(amount: Float) {
            var biome = AbyssUtils.getBiomeManager().getBiomeOfClass(PrimordialWaters::class.java) as PrimordialWaters

            if (delay >= 0f) {
                delay -= amount
                return
            }

            if (!playedSound) {
                playedSound = true
                Global.getSoundPlayer().playSound("rat_genesis_system_sound", 0.8f, 1.2f, entity.location, Vector2f())
            }

            biome.effectLevel += 0.33f * amount
            if (biome.effectLevel >= 1f) {
                finished = true
            }

        }

    }
}