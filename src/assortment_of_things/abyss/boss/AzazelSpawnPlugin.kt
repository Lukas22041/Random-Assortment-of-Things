package assortment_of_things.abyss.boss

import assortment_of_things.abyss.items.cores.officer.ChronosCore
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.fs.starfarer.api.input.InputEventAPI
import java.util.*

//Work around as spawning moduled ships can randomly cause ConcurrentModificationExceptions when called from advances and listeners attached to ships
class AzazelSpawnPlugin(var bossScript: GenesisBossScript) : BaseEveryFrameCombatPlugin() {

    var spawned = false

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        super.advance(amount, events)

        if (spawned) return

        var hard = bossScript.ship.variant.hasTag("rat_challenge_mode")

        bossScript.azazel1 = bossScript.spawnApparation("rat_genesis_serpent_head_Standard", ChronosCore().createPerson(RATItems.CHRONOS_CORE, "rat_abyssals_primordials", Random()))
        bossScript.azazel2 = bossScript.spawnApparation("rat_genesis_serpent_head_Standard", ChronosCore().createPerson(RATItems.CHRONOS_CORE, "rat_abyssals_primordials", Random()))

        if (hard) {
            bossScript.azazel3 = bossScript.spawnApparation("rat_genesis_serpent_head_Standard", ChronosCore().createPerson(RATItems.CHRONOS_CORE, "rat_abyssals_primordials", Random()))
            bossScript.azazel4 = bossScript.spawnApparation("rat_genesis_serpent_head_Standard", ChronosCore().createPerson(RATItems.CHRONOS_CORE, "rat_abyssals_primordials", Random()))
        }


        spawned = true
    }

}