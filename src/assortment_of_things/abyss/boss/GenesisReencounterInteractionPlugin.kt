package assortment_of_things.abyss.boss

import assortment_of_things.abyss.items.cores.officer.PrimordialCore
import assortment_of_things.misc.FIDOverride
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import java.util.*

class GenesisReencounterInteractionPlugin : RATInteractionPlugin() {
    override fun init() {

        //Fleet Creation
        var enemyFleet = Global.getFactory().createEmptyFleet("rat_abyssals_primordials", "Singularity", true)
        enemyFleet.isNoFactionInName = true

        var boss = enemyFleet.fleetData.addFleetMember("rat_genesis_Standard")
        boss.fixVariant()

        boss.variant.addTag(Tags.TAG_NO_AUTOFIT)
        boss.variant.addTag(Tags.VARIANT_UNBOARDABLE)
        boss.variant.addTag(Tags.SHIP_LIMITED_TOOLTIP)
        boss!!.variant.addTag("rat_really_not_recoverable")

        var core = PrimordialCore().createPerson(RATItems.PRIMORDIAL, "rat_abyssals_primordials", Random())
        boss.captain = core

        enemyFleet.addTag("rat_genesis_fleet")
        enemyFleet.memoryWithoutUpdate.set("\$defenderFleet", enemyFleet)

        var config = FleetInteractionDialogPluginImpl.FIDConfig()

        config.leaveAlwaysAvailable = true
        config.showCommLinkOption = false
        config.showEngageText = false
        config.showFleetAttitude = false
        config.showTransponderStatus = false
        config.showWarningDialogWhenNotHostile = false
        config.alwaysAttackVsAttack = true
        config.impactsAllyReputation = true
        config.impactsEnemyReputation = false
        config.pullInAllies = false
        config.pullInEnemies = false
        config.pullInStations = false
        config.lootCredits = false

        config.firstTimeEngageOptionText = "Engage the defenses"
        config.afterFirstTimeEngageOptionText = "Re-engage the defenses"
        config.noSalvageLeaveOptionText = "Continue"

        config.dismissOnLeave = false
        config.printXPToDialog = true

        val seed = Random().nextLong()
        config.salvageRandom = Misc.getRandom(seed, 75)

        var originalPlugin = dialog.plugin

        //Plugin
        var plugin = GenesisReEncounterFIDPlugin(config, this);

      //  config.delegate = FIDOverride(enemyFleet!!, dialog, plugin, originalPlugin, this)

        dialog.setPlugin(plugin);
        dialog.setInteractionTarget(enemyFleet);
        plugin.init(dialog);

    }

    override fun defeatedDefenders() {
        clearOptions()

        addLeaveOption()
    }
}