package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.activators.PerseveranceActivator
import assortment_of_things.abyss.boss.GenesisBossScript
import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import assortment_of_things.abyss.shipsystem.activators.PrimordialSeaActivator
import assortment_of_things.combat.AfterImageRenderer
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import org.magiclib.subsystems.MagicSubsystemsManager

class PrimordialSeaHullmod : BaseHullMod() {

    var color = AbyssUtils.GENESIS_COLOR

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?,  ship: ShipAPI?,   isForModSpec: Boolean): Boolean {
        return false
    }

    override fun affectsOPCosts(): Boolean {
        return true
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)
        if (ship == null) return
        MagicSubsystemsManager.addSubsystemToShip(ship, PrimordialSeaActivator(ship))
    }
    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  width: Float, isForModSpec: Boolean) {

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight, color.brighter())
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

        tooltip!!.addSpacer(5f)
        tooltip.addPara("The spacewarping capabilities of the ship enable the Primordial Sea subsystem. " +
                "When activated, it can overwrite nearby space with its own pocket dimension." +
                "\n\n" +
                "This dimension is home to exotic matter that the ship is able to freely shape in to small formations, spawning an infinite amount of frigates within. " +
                "This matter however is unable to exist within real space, and as such the ships are unable to operate outside of the overwriten reality." +
                "\n\n" +
                "Those frigates are controlled by the same core as the ships.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
            "Primordial Sea", "infinite", "frigates", "unable to operate", "controlled by the same core")

        tooltip!!.addLunaElement(0f, 0f).apply {
            render {particleSpawner.renderForeground(element, it)  }
        }

    }




    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }
}