package assortment_of_things.exotech

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken

object ExoUtils {

    fun getExoData() : ExoData {
        var data = Global.getSector().memoryWithoutUpdate.get("\$rat_exo_data") as ExoData?
        if (data == null) {
            data = ExoData()
            Global.getSector().memoryWithoutUpdate.set("\$rat_exo_data", data)
        }
        return data
    }

    fun getExoshipData(exoship: SectorEntityToken) : ExoShipData {
        var data = exoship.memoryWithoutUpdate.get("\$rat_exoship_data") as ExoShipData?
        if (data == null) {
            data = ExoShipData()
            exoship.memoryWithoutUpdate.set("\$rat_exoship_data", data)
        }
        return data
    }
}