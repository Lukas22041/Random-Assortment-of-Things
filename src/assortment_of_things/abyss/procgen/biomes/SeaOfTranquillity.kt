package assortment_of_things.abyss.procgen.biomes

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class SeaOfTranquillity : BaseAbyssBiome() {

    //Notes
    //No Seraphs

    override fun getId(): String {
        return "sea_of_tranquillity"
    }

    override fun getName(): String {
        return "Sea of Tranquillity"
    }

    override fun getColor(): Color {
        return Color(200, 0, 0)
    }

    override fun getLabelColor(): Color {
        return Color(200, 0, 0)
    }

    override fun getLightColor(): Color {
        return Color(200, 0, 0)
    }

    override fun getEnviromentColor(): Color {
        return Color(100, 0, 0)
    }

    override fun generate() {

        //Generate connection between hyperspace and abyss

       /* var system = AbyssUtils.getSystem()
        for (cell in cells) {
            var entitiy = system.addCustomEntity("test${Misc.genUID()}", "Test", "orbital_habitat", Factions.NEUTRAL)
            entitiy.location.set(cell.getCenterInRealLoc())
        }*/
    }


}