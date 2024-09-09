package assortment_of_things.backgrounds

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.ExotechGenerator
import assortment_of_things.exotech.entities.ExoshipEntity
import assortment_of_things.exotech.intel.event.BackgroundStartFactor
import assortment_of_things.exotech.intel.event.ExotechEventIntel
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Conditions
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Industries
import com.fs.starfarer.api.impl.campaign.ids.Submarkets
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.utilities.NexFactionConfig
import lunalib.lunaUtil.LunaCommons
import org.lazywizard.lazylib.MathUtils

class ExoBackground : BaseCharacterBackground() {


    fun isUnlocked() : Boolean {
        //return MagicAchievementManager.getInstance().getAchievement("rat_beatExotechQuestline")!!.isComplete
        return LunaCommons.getBoolean("assortment_of_things", "exotechQuestlineDone") == true
    }

    override fun getTitle(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        if (isUnlocked()) return spec.title
        else return "??? [Locked]"
    }

    override fun shouldShowInSelection(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): Boolean {
        return RATSettings.backgroundsEnabled!! && RATSettings.exoEnabled!!
    }

    override fun canBeSelected(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): Boolean {
        return isUnlocked()
    }

    override fun getShortDescription(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        if (!isUnlocked()) return "Finish the Exotech Questline to unlock this backgriund."
        else return "You assisted Amelie and Xander in restoring an Exoship to good condition, and you are ready to make new use of it."
    }

    override fun getLongDescription(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): String {
        if (!isUnlocked()) return ""
        else {
            return "You begin with the Exotech Questline already completed, with a personal Exoship under your command. \n\n" +
                    "" +
                    "The Exoship will be orbiting a planet within the starting system. " +
                    "If theres no suitable location, it will be somewhere else, and you can call it to you with the \"Manage Exoship\" ability.\n\n" +
                    "" +
                    "Xanders side missions are still available and are not commpleted by this background."
        }
    }

   /* override fun getSpawnLocationOverwrite(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): SectorEntityToken {
        return ExoUtils.getExoData().exoships.random()
    }*/


    override fun onNewGameAfterTimePass(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        var data = ExoUtils.getExoData()

        //Set Tags

        data.foundExoshipRemains = true
        data.defeatedRemainsDefenses = true

        data.finishedWarpCatalystMission = false
        data.finishedWarpCatalystMissionEntirely = true

        data.QuestBeginning_StartedFromExoship = true
        data.QuestBeginning_Done = true

        data.talkedWithXanderOnce = true

        data.readyToRepairExoship = true

        //Add Intel
        var intel = ExotechEventIntel()
        BackgroundStartFactor(ExotechEventIntel.Stage.LEADERSHIP.progress)

        //Remove Hideout
        data.hideout!!.starSystem.removeEntity(data.hideout)

        //Remove Remains
        var remains = data.exoshipRemainsEntity
        remains!!.containingLocation.removeEntity(remains)

        //Spawn Exoship

        var player = Global.getSector().playerFleet
        var spawnEntity = player.containingLocation.planets.filter { !it.isStar }.minByOrNull { MathUtils.getDistance(player.location, player.location) }

        if (spawnEntity == null) spawnEntity = Global.getSector().starSystems.flatMap { it.planets }.random()

        data.recoveredExoship = true

        var playerExoship = spawnEntity!!.starSystem.addCustomEntity("exoship_${Misc.genUID()}", "Aurora", "rat_exoship", "rat_exotech")
        var playerPlugin = playerExoship.customPlugin as ExoshipEntity

        playerExoship.memoryWithoutUpdate.set(MusicPlayerPluginImpl.MUSIC_SET_MEM_KEY, "rat_exo_market")

        var market = ExotechGenerator.addMarketplace(Factions.PLAYER,
            playerExoship,
            arrayListOf(),
            "Aurora",
            3,
            arrayListOf(Conditions.OUTPOST),
            arrayListOf("rat_exoship_market", Submarkets.SUBMARKET_STORAGE),
            arrayListOf(Industries.MEGAPORT),
            0.3f,
            false,
            false)
        market.isHidden = true

        //Should prevent them from building stuff on it
        market.isPlayerOwned = false

        market.admin = data.amelie
        data.amelie.postId = "stationCommander"


        playerExoship.setCircularOrbit(spawnEntity, MathUtils.getRandomNumberInRange(0f, 360f), spawnEntity.radius + playerExoship.radius + 100f, 120f)

        data.setPlayerExoship(playerExoship)
        playerPlugin.npcModule.isPlayerOwned = true
        playerPlugin.playerModule.isPlayerOwned = true


        //Last
        var abilityId = "rat_exoship_management"
        Global.getSector().characterData.addAbility(abilityId)
        val slots = Global.getSector().uiData.abilitySlotsAPI
        slots.currBarIndex = 0
        slots.currSlotsCopy[9].abilityId = abilityId
    }

    override fun addTooltipForSelection(tooltip: TooltipMakerAPI?, factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?, expanded: Boolean) {
        super.addTooltipForSelection(tooltip, factionSpec, factionConfig, expanded)

        if (!isUnlocked()) return

    }

    override fun getOrder(): Float {
        if (!isUnlocked()) return (Int.MAX_VALUE - spec.order).toFloat()
        return spec.order
    }
}
