package assortment_of_things.exotech.hullmods

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import java.awt.Color

class ExperimentalDesignHullmod : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI, id: String?) {
        stats.suppliesPerMonth.modifyMult(id, 2f)
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }

    override fun getDisplaySortOrder(): Int {
        return 1
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        var sprite = Global.getSettings().getAndLoadSprite("graphics/ui/rat_exo_hmod.png")

        var initialHeight = tooltip!!.heightSoFar
        var element = tooltip!!.addLunaElement(0f, 0f)

        tooltip!!.addSpacer(10f)

        tooltip!!.addPara("A non-standard exogrid hull design. There may be no other ship of its kind in the sector. \n\n" +
                "It comes equiped with a more advanced iteration of the phase-grid, eliminating the exogrids restrictions on active venting rate and flux dissipation. \n\n" +
                "Due to the experimental nature of its build, it requires twice as many supplies for upkeep per month than usual for a ship of its size. "
                , 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),  "eliminating the exogrids restrictions on active venting rate and flux dissipation", "twice as many supplies for upkeep per month")


        element.render {
            sprite.setSize(tooltip.widthSoFar + 20, tooltip.heightSoFar + 10)
            sprite.setAdditiveBlend()
            sprite.render(tooltip.position.x, tooltip.position.y)
        }
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun getNameColor(): Color {
        return Color(217, 164, 57)
    }

    override fun getBorderColor(): Color {
        return Color(217, 164, 57)
    }
}