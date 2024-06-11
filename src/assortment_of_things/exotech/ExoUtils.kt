package assortment_of_things.exotech

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import java.awt.Color

object ExoUtils {

    val color1 = Color(248,172,44, 255)
    val color2 = Color(247,121,86, 255)

    fun getExoData() : ExoData {
        var data = Global.getSector().memoryWithoutUpdate.get("\$rat_exo_data") as ExoData?
        if (data == null) {
            data = ExoData()
            Global.getSector().memoryWithoutUpdate.set("\$rat_exo_data", data)
        }
        return data
    }
}