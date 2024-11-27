package assortment_of_things.backgrounds

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.ExotechGenerator
import assortment_of_things.exotech.entities.ExoshipEntity
import assortment_of_things.exotech.intel.event.BackgroundStartFactor
import assortment_of_things.exotech.intel.event.ExotechEventIntel
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionSpecAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.MusicPlayerPluginImpl
import com.fs.starfarer.api.impl.campaign.DModManager
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import exerelin.campaign.backgrounds.BaseCharacterBackground
import exerelin.utilities.NexFactionConfig
import lunalib.lunaUtil.LunaCommons
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.getStorage
import org.magiclib.kotlin.getStorageCargo
import second_in_command.SCUtils
import second_in_command.specs.SCOfficer
import java.util.*

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
        if (!isUnlocked()) return "Finish the Exotech Questline to unlock this background."
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
                    "Xanders side missions are still available and are not commpleted by this background. The Automated Arkas is stored in the Exoships storage, it is however heavily damaged."
        }
    }

   /* override fun getSpawnLocationOverwrite(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?): SectorEntityToken {
        return ExoUtils.getExoData().exoships.random()
    }*/


    override fun onNewGameAfterTimePass(factionSpec: FactionSpecAPI?, factionConfig: NexFactionConfig?) {
        var data = ExoUtils.getExoData()

        //Add Executive Officer
        if (Global.getSettings().modManager.isModEnabled("second_in_command")) {
            data.claimedExotechOfficer = true

            var person = Global.getSector().getFaction("rat_exotech").createRandomPerson(FullName.Gender.MALE)
            var officer = SCOfficer(person, "rat_exotech")
            officer.person.portraitSprite = "graphics/portraits/rat_exo_exec.png"
            officer.increaseLevel(1)

            SCUtils.getPlayerData().addOfficerToFleet(officer)
            SCUtils.getPlayerData().setOfficerInEmptySlotIfAvailable(officer)
        }


        //Give Processor
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_ai_core_special", "rat_exo_processor"), 1f)

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



        //Add Auto Arkas to Storage
        market.getStorageCargo().initMothballedShips(Factions.PLAYER)
        (market.getStorage() as StoragePlugin).setPlayerPaidToUnlock(true)
        var arkas = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_arkas_Strike")
        arkas.fixVariant()
        arkas.variant.addPermaMod(HullMods.AUTOMATED)
        arkas.variant.addTag(Tags.TAG_AUTOMATED_NO_PENALTY)
        arkas.repairTracker.cr = 1f
        arkas.variant.addTag(Tags.TAG_NO_AUTOFIT)
        arkas.variant.addPermaMod("rat_exo_experimental")
        DModManager.addDMods(arkas, false, 5, Random())
        market.getStorageCargo().mothballedShips.addFleetMember(arkas)


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
