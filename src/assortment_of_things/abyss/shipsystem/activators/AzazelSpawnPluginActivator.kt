package assortment_of_things.abyss.shipsystem.activators

import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

//Workaround as spawning modular ships can randomly cause ConcurrentModificationExceptions when called from advances and listeners attached to ships
class AzazelSpawnPluginActivator(var activator: PrimordialSeaActivator, var targetLoc: Vector2f) : BaseEveryFrameCombatPlugin() {

    var spawned = false

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)

        if (spawned) return

        activator.spawnAzazel(targetLoc)

        spawned = true
    }
}

