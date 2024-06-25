package assortment_of_things.exotech

import assortment_of_things.exotech.entities.ExoshipEntity
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI

class ExoData {

    private var exoship: SectorEntityToken? = null
    private var playerExoship: SectorEntityToken? = null

    var exoshipRemainsEntity: SectorEntityToken? = null
    var foundExoshipRemains = false
    var defeatedRemainsDefenses = false

    //var showFactionInIntel = false
    var canRepairShips = false

    var amelie = Global.getFactory().createPerson()
    var xander = Global.getFactory().createPerson()

    var lockedOutOfQuest = false

    //First Quest
    var QuestBeginning_Active = true
    var QuestBeginning_StartedFromExoship = false
    var QuestBeginning_StartedFromRemains = false

    var QuestBeginning_Done = false

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

    fun getPlayerExoship() : SectorEntityToken {
        return playerExoship!!
    }

    fun getPlayerExoshipPlugin() : ExoshipEntity {
        return playerExoship!!.customPlugin as ExoshipEntity
    }
}