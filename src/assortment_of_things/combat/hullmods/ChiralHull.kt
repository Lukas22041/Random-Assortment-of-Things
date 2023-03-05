package assortment_of_things.combat.hullmods

import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import data.scripts.plugins.MagicTrailPlugin
import data.scripts.util.MagicIncompatibleHullmods
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import javax.xml.bind.annotation.XmlElementDecl.GLOBAL


class ChiralHull : BaseHullMod()
{
    override fun advanceInCombat(ship: ShipAPI?, amount: Float)
    {
        if (ship == null) return
    }


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?)
    {

        if(stats!!.getVariant().getHullMods().contains("safetyoverrides"))
        {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(),"safetyoverrides","rat_chiral_hull");
        }
        /*if(stats!!.getVariant().getHullMods().contains("targetingunit"))
        {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(),"targetingunit","rat_chiral_hull");
        }
        if(stats!!.getVariant().getHullMods().contains("dedicated_targeting_core"))
        {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(),"targetingunit","rat_chiral_hull");
        }*/

        stats.ventRateMult.modifyMult(id, 0f);
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on

        stats.getFluxDissipation().modifyMult(id, 1.25f);
        /*stats.ballisticWeaponRangeBonus.modifyMult(id,0.9f)
        stats.energyWeaponRangeBonus.modifyMult(id,0.9f)*/

        if (stats.fleetMember != null && stats.fleetMember.captain != null && stats.fleetMember.captain.aiCoreId == RATItems.SCARLET_PROCESSOR)
        {
            stats.ballisticRoFMult.modifyMult(id + "rat_processor", 1.2f)
            stats.energyRoFMult.modifyMult(id + "rat_processor", 1.2f)
            stats.missileRoFMult.modifyMult(id + "rat_processor", 1.2f)
        }
        else if (stats.fleetMember != null && stats.fleetMember.captain != null && stats.fleetMember.captain.aiCoreId == RATItems.AZURE_PROCESSOR)
        {
            stats.shieldAbsorptionMult.modifyMult(id + "rat_processor", 0.9f)
        }
        else if (stats.fleetMember != null && stats.fleetMember.captain != null && stats.fleetMember.captain.aiCoreId == RATItems.AMBER_PROCESSOR)
        {
            stats.timeMult.modifyMult(id + "rat_processor", 100.1f)
        }
        else
        {
            stats.ballisticRoFMult.unmodify(id + "rat_processor")
            stats.energyRoFMult.unmodify(id + "rat_processor")
            stats.missileRoFMult.unmodify(id + "rat_processor")

            stats.timeMult.unmodify(id + "rat_processor")
            stats.shieldAbsorptionMult.unmodify(id + "rat_processor")
        }
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?)
    {
        if (ship == null) return
        Global.getCombatEngine().addLayeredRenderingPlugin(ChiralHullListener(ship, id))

    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI, width: Float, isForModSpec: Boolean) {
        tooltip.addSectionHeading("Stats", Alignment.MID, 10f);

        var string1 = "This hulls flux systems are higly optimised, allowing it to always make use of the zero-flux engine boost. Additionaly it increases the passive flux dissipation by %s \n" +
                "\nAssumingly due to this high flux-efficiency, the hull does not contain an active-venting system."

        var label1 = tooltip.addPara(string1, 3f, Misc.getHighlightColor(), "25%")
        label1.setHighlight("highly", "optimised", "zero-flux engine boost", "flux", "dissipation", "25%", "does not contain", "active-venting")

        if (ship.fleetMember.captain != null && ship.fleetMember.captain.aiCoreId == RATItems.SCARLET_PROCESSOR)
        {
            tooltip.addSectionHeading("Scarlet Processor", Alignment.MID, 10f);
            tooltip.addPara("The connected Scarlet Processor causes the ship to behave more aggressively in combat and boosts the weapons rate of fire by 20%%.", 3f, Misc.getHighlightColor()).apply {
                setHighlight("Scarlet Processor","rate of fire","20%")
                setHighlightColors(Misc.getHighlightColor())
            }
        }
        if (ship.fleetMember.captain != null && ship.fleetMember.captain.aiCoreId == RATItems.AZURE_PROCESSOR)
        {
            tooltip.addSectionHeading("Azure Processor", Alignment.MID, 10f);
            tooltip.addPara("The connected Azure Processor causes the ship to behave more defensively in combat and reduces damage taken by the ships shields by 10%%.", 3f, Misc.getHighlightColor()).apply {
                setHighlight("Azure Processor","reduces damage taken", "shields" ,"10%")
                setHighlightColors(Misc.getHighlightColor())
            }
        }
        if (ship.fleetMember.captain != null && ship.fleetMember.captain.aiCoreId == RATItems.AMBER_PROCESSOR)
        {
            tooltip.addSectionHeading("Amber Processor", Alignment.MID, 10f);
            tooltip.addPara("The connected Amber Processor increases the ships timeflow by 10%%.", 3f, Misc.getHighlightColor()).apply {
                setHighlight("Amber Processor", "time diallation","10%")
                setHighlightColors(Misc.getHighlightColor())
            }
        }
        else
        {
            tooltip.addSectionHeading("Processor: None", Alignment.MID, 10f);
            tooltip.addPara("No Chiral Processor Installed. Installing a Chiral Processor enables different effects.", 3f, Misc.getHighlightColor()).apply {
                setHighlight("Chiral Processor", "Chiral Processor", "effects")
                setHighlightColors(Misc.getHighlightColor())
            }
        }

        tooltip.addSectionHeading("Incompatibility", Alignment.MID, 10f);

        var label3 = tooltip.addPara("Incompatible with Safety Overrides", 3f)
        label3.setHighlight("Safety Overrides")
        label3.setHighlightColor(Misc.getNegativeHighlightColor())
    }



    public class ChiralHullListener(ship: ShipAPI, id: String?) : CombatLayeredRenderingPlugin
    {
        var ship: ShipAPI
        var id: String

        var color = Color(122,139,221,255)

        init
        {
            this.ship = ship
            this.id = id!!
        }

        override fun init(entity: CombatEntityAPI?) {

        }

        override fun cleanup() {

        }

        override fun isExpired(): Boolean {
            return false
        }

        var trailID = MagicTrailPlugin.getUniqueID()
        var interval = IntervalUtil(0.05f, 0.05f)
        override fun advance(amount: Float)
        {
            var thrusterID = 1000
            interval.advance(amount)
            for (engine in ship.engineController.shipEngines)
            {
                if (!ship.isAlive) continue
                if (ship.isHulk) continue
                thrusterID += 1000
                var color = engine.engineColor
                if (ship.hullSpec.hullId == "rat_phenix" && ship.system.isActive)
                {
                    color = ship.engineController.flameColorShifter.curr
                }
                var angle = ship.spriteAPI.angle - 90
                var vector = Vector2f(1f, 0f).rotate(angle)

                if (interval.intervalElapsed())
                {
                    MagicTrailPlugin.AddTrailMemberSimple(ship, trailID + thrusterID, Global.getSettings().getSprite("fx", "base_trail_aura"),
                        Vector2f(engine.location.x + vector.x, engine.location.y + vector.y) , 0f, angle, 5f, 10f, color, 1f, 0.5f, 0.25f, 0.3f, true )

                }

                //Idea from Epta, but modified for my use case.
                if (ship.engineController.isAccelerating || ship.engineController.isStrafingLeft || ship.engineController.isStrafingRight || ship.engineController.isDecelerating) {

                    var combatEngine = Global.getCombatEngine()
                    var thrustVector = Vector2f(0f, ship.velocity.length() / 2).rotate(angle -90)
                    var colorWLessAlpha = Color(color.red, color.green, color.blue, 150)

                    var size = engine.engineSlot.width * 1f
                    var sizeEndMult = engine.engineSlot.width * 0.7f

                    combatEngine.addNebulaParticle(engine.getLocation(), thrustVector, size, sizeEndMult , 0.2f, 0.2f, MathUtils.clamp(size / 70, 0.10f, 0.2f), colorWLessAlpha);
                    //combatEngine.addNebulaParticle(engine.getLocation(), thrustVector, size,sizeEndMult , 0.2f, 0.2f, MathUtils.clamp(size / 70, 0.10f, 0.2f), colorWLessAlpha);
                }
            }
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?)
        {
            val alphaMult = viewport!!.alphaMult * 1f

        }

        override fun getRenderRadius(): Float {
            return 10000f
        }

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return EnumSet.of(CombatEngineLayers.BELOW_PHASED_SHIPS_LAYER)
        }
    }
}