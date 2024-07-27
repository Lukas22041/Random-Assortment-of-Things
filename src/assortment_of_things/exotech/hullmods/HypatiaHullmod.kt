package assortment_of_things.exotech.hullmods

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class HypatiaHullmod : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI, id: String?) {
        stats!!.zeroFluxMinimumFluxLevel.modifyFlat(id, 0.1f)
        stats!!.energyWeaponDamageMult.modifyFlat(id, 1.2f)
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        var sprite = Global.getSettings().getAndLoadSprite("graphics/ui/rat_exo_hmod.png")

        var initialHeight = tooltip!!.heightSoFar
        var element = tooltip!!.addLunaElement(0f, 0f)

        tooltip!!.addSpacer(10f)

        tooltip!!.addPara("The ships integrated warp-drive passively emits a charged field that enables the ship to trigger the zero-flux speed boost while the its flux level is below 10%% of its maximum flux.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "zero-flux speed boost", "10%")

        tooltip.addSpacer(10f)

        tooltip.addPara("The field also influences frequencies commonly used by energy weapons, supercharging them to deal 20%% more damage.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "energy weapons", "20%")

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