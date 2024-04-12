package assortment_of_things.abyss.boss

import assortment_of_things.abyss.items.cores.officer.PrimordialCore
import assortment_of_things.misc.FIDOverride
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.addPara
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.util.Misc
import org.magiclib.achievements.MagicAchievementManager
import java.util.*
import kotlin.collections.HashMap

class GenesisReencounterInteractionPlugin : RATInteractionPlugin() {

    var originalCr = HashMap<FleetMemberAPI, Float>()
    var originalCHull = HashMap<FleetMemberAPI, Float>()


    override fun init() {

        for (member in Global.getSector().playerFleet.fleetData.membersListCopy) {
            originalCr.put(member, member.repairTracker.cr)
        }



        textPanel.addPara("You reach a memorial of unknown origin, yet you know intrinsicly what it is for.")

        var tooltip = textPanel.beginTooltip()

        var img = tooltip.beginImageWithText("graphics/portraits/cores/rat_primordial_core.png", 48f)

        img.addPara("You can repeat the battle against the Genesis-class at will. It will be of no rewards or damage for your fleet, and the outcome does not matter.",
            0f, Misc.getGrayColor(), Misc.getHighlightColor(), "Genesis-class")

        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        tooltip.addPara("Despite having no influence on reality, making a copy of your save is recommended in case of interference from other mods or issues with the game.",
            0f, Misc.getGrayColor(), Misc.getHighlightColor(), "copy")

        textPanel.addTooltip()

        createOption("Relive the singularity encounter") {
            beginFight(false)
        }
        createOption("Experience a more challenging encounter") {
            beginFight(true)
        }
        optionPanel.setTooltip("Experience a more challenging encounter", "Fight a genesis with stronger stats and slight difference in behaviour. Rewards an achievement for defeating it.")

        addLeaveOption()
    }

    fun beginFight(challenge: Boolean) {
        clearOptions()

        //Fleet Creation
        var enemyFleet = Global.getFactory().createEmptyFleet("rat_abyssals_primordials", "Singularity", true)
        enemyFleet.isNoFactionInName = true

        var boss = enemyFleet.fleetData.addFleetMember("rat_genesis_Standard")
        boss.fixVariant()

        boss.variant.addTag(Tags.TAG_NO_AUTOFIT)
        boss.variant.addTag(Tags.VARIANT_UNBOARDABLE)
        boss.variant.addTag(Tags.SHIP_LIMITED_TOOLTIP)
        boss.repairTracker.cr = 0.7f
        boss!!.variant.addTag("rat_really_not_recoverable")
        if (challenge) {
            boss.variant.addTag("rat_challenge_mode")
        }
        enemyFleet.addTag("rat_challenge_mode")

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
        config.withSalvage = false
        config.leaveAlwaysAvailable = true
        config.dismissOnLeave = false

        val seed = Random().nextLong()
        config.salvageRandom = Misc.getRandom(seed, 75)

        var originalPlugin = dialog.plugin

        //Plugin
        var plugin = GenesisReEncounterFIDPlugin(config, this);

        config.delegate = FIDOverride(enemyFleet!!, dialog, plugin, originalPlugin, this)

        dialog.setPlugin(plugin);
        dialog.setInteractionTarget(enemyFleet);
        plugin.init(dialog);
    }

    override fun defeatedDefenders() {
        clearOptions()

        for ((member, cr) in originalCr) {
            member.repairTracker.cr = cr
            member.repairTracker.performRepairsFraction(1f)
        }

        var defeatedOnHard = false
        if (Global.getSector().memoryWithoutUpdate.get("\$defeated_singularity_on_hard") == true) {
            defeatedOnHard = true
        }

        dialog.plugin = this



        createOption("Leave") {
            closeDialog()
            if (defeatedOnHard) {
                MagicAchievementManager.getInstance().completeAchievement("rat_beatSingularityChallenge")
            }
        }

        /*closeDialog()*/
    }
}