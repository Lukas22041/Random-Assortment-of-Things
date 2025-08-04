package assortment_of_things.exotech

import assortment_of_things.exotech.entities.ExoshipEntity
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI

class ExoData {

    private var exoship: SectorEntityToken? = null
    private var playerExoship: SectorEntityToken? = null

    var preventNPCWarps = false
    var recoveredExoship = false

    var exoshipRemainsEntity: SectorEntityToken? = null
    var foundExoshipRemains = false
    var defeatedRemainsDefenses = false

    var hideout: SectorEntityToken? = null

    //var showFactionInIntel = false
    var canRepairShips = false
    var canBuyItems = false
    var canBuyShips = false

    var amelie = Global.getFactory().createPerson()
    var xander = Global.getFactory().createPerson()

    var lockedOutOfQuest = false

    //First Quest
    var QuestBeginning_Active = true
    var QuestBeginning_StartedFromExoship = false
    var QuestBeginning_StartedFromRemains = false

    var QuestBeginning_Done = false
    var talkedWithXanderOnce = false

    var aiCorePriceMod = 0.25f
    var alterationPriceMod = 0.25f
    var blueprintPriceMod = 0.50f
    var colonyEquipmentPriceMod = 0.75f
    var conversionRatio = 0.001f

    var maximumInfluenceRequired = 750
    var influenceCapPeritem = 100f

    var reachedLeadershipGoal = false
    var readyToRepairExoship = false

    var claimedExotechOfficer = false

    //Missions

    var hasActiveMission = false
    var finishedCurrentMission = false

    var finishedGilgameshMission = false
    var finishedGilgameshMissionEntirely = false

    var finishedRapidMission = false
    var finishedRapidMissionEntirely = false

    var finishedWarpCatalystMission = false
    var finishedWarpCatalystMissionEntirely = false

    fun setExoship(exoship : SectorEntityToken) {
        this.exoship = exoship
    }

    fun getExoship() : SectorEntityToken {
        return exoship!!
    }

    fun getExoshipPlugin() : ExoshipEntity {
        return exoship!!.customPlugin as ExoshipEntity
    }

    fun setPlayerExoship(exoship : SectorEntityToken) {
        this.playerExoship = exoship
    }

    fun getPlayerExoship() : SectorEntityToken? {
        return playerExoship
    }

    fun getPlayerExoshipPlugin() : ExoshipEntity {
        return playerExoship!!.customPlugin as ExoshipEntity
    }
}