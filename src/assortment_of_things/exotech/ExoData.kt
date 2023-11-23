package assortment_of_things.exotech

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken

class ExoData {

    var interactedWithExoship = false
    var hasPartnership = false
    var commPerson = Global.getFactory().createPerson()
    var exoships = ArrayList<SectorEntityToken>()

    var tokens = 0f

}