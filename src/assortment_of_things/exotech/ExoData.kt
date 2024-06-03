package assortment_of_things.exotech

import assortment_of_things.exotech.entities.ExoshipEntity
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken

class ExoData {

    var interactedWithExoship = false
    var hasPartnership = false
    var commPerson = Global.getFactory().createPerson()
    private var exoship: SectorEntityToken? = null

    var tokens = 0f


    fun setExoship(exoship : SectorEntityToken) {
        this.exoship = exoship
    }

    fun getExoship() : SectorEntityToken {
        return exoship!!
    }

    fun getExoshipPlugin() : ExoshipEntity {
        return exoship!!.customPlugin as ExoshipEntity
    }
}