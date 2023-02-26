package assortment_of_things.combat.hullmods

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.campaign.procgen.StarAge
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator.StarSystemType
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import data.scripts.plugins.MagicTrailPlugin
import data.scripts.util.MagicIncompatibleHullmods
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.rotate
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*


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
        if(stats!!.getVariant().getHullMods().contains("targetingunit"))
        {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(),"targetingunit","rat_chiral_hull");
        }
        if(stats!!.getVariant().getHullMods().contains("dedicated_targeting_core"))
        {
            MagicIncompatibleHullmods.removeHullmodWithWarning(stats.getVariant(),"targetingunit","rat_chiral_hull");
        }

        stats!!.ventRateMult.modifyMult(id, 0f);
        stats.getZeroFluxMinimumFluxLevel().modifyFlat(id, 2f); // set to two, meaning boost is always on

        stats.getFluxDissipation().modifyMult(id, 1.5f);
        stats.ballisticWeaponRangeBonus.modifyMult(id,0.9f)
        stats.energyWeaponRangeBonus.modifyMult(id,0.9f)

    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?)
    {
        if (ship == null) return
        Global.getCombatEngine().addLayeredRenderingPlugin(ChiralHullCombat(ship, id))


    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        tooltip.addSectionHeading("Stats", Alignment.MID, 10f);

        var string1 = "This hulls flux systems are higly optimised, allowing it to always make use of the zero-flux engine boost. Additionaly it increases the passive flux dissipation by %s \n" +
                "\nCompromises had to be made to reach this level of optimisation, which caused the removal of the active-venting component and" +
                " a reduction in the ballistic and energy weapon range of %s"

        var label1 = tooltip.addPara(string1, 3f, Misc.getHighlightColor(), "50%", "10%")
        label1.setHighlight("highly", "optimised", "zero-flux engine boost", "flux", "dissipation", "50%", "removal", "active-venting", "reduction", "ballistic", "energy", "10%")

        tooltip.addSectionHeading("In Combat", Alignment.MID, 10f);

        var string2 = "Increases the ships max speed and shield efficiency up to %s based on the amount of enemy hulls nearby, with it reaching its highest strength at 5 ships.\n\n" +
                "The effects radius is 600/650/700/800 based on the ships hull size."

        var label2 = tooltip.addPara(string2, 3f, Misc.getHighlightColor(), "25%")
        label2.setHighlight("max speed", "shield efficiency", "25%", "enemy", "hulls", "nearby", "highest", "strength", "5", "ships", "600/650/700/800", "hullsize")

        tooltip.addSectionHeading("Incompatibility", Alignment.MID, 10f);

        var label3 = tooltip.addPara("Incompatible with Safety Overrides\nIncompatible with Dedicated Targeting Core \nIncompatible with Integrated Targeting Unit", 3f)
        label3.setHighlight("Safety Overrides", "Dedicated Targeting Core", "Integrated Targeting Unit")
        label3.setHighlightColor(Misc.getNegativeHighlightColor())
    }



    public class ChiralHullCombat(ship: ShipAPI, id: String?) : CombatLayeredRenderingPlugin
    {
        var ship: ShipAPI
        var id: String
        var range = 700f

        var color = Color(122,139,221,255)

        var targetedShips: MutableList<ShipAPI> = ArrayList()
        var hullsize: ShipAPI.HullSize

        init
        {
            this.ship = ship
            this.id = id!!
            hullsize = ship.hullSize

            range = when (hullsize)
            {
                ShipAPI.HullSize.FRIGATE -> 600f
                ShipAPI.HullSize.DESTROYER -> 650f
                ShipAPI.HullSize.CRUISER -> 700f
                ShipAPI.HullSize.CAPITAL_SHIP -> 800f
                else -> 500f
            }
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
            var enemiesInrangeWithFighters = CombatUtils.getShipsWithinRange(ship.location, range)
            var enemiesInrange: MutableList<ShipAPI> = ArrayList()
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

            for (enemy in enemiesInrangeWithFighters)
            {
                if (enemy.hullSize != ShipAPI.HullSize.FIGHTER && enemy.owner != ship.owner && enemy.isAlive)
                {
                    enemiesInrange.add(enemy)
                }
            }



            var enemies = MathUtils.clamp(enemiesInrange.size, 0, 5)

            var hue = 0.6f
            var saturation = 0.3f
            if (enemies != 0)
            {
                saturation = 0.3f + (enemies / 7f)
            }
            var luminance = 0.9f
            var color = Color.getHSBColor(hue, saturation, luminance)

            for (enemy in enemiesInrange)
            {
                if (!targetedShips.contains(enemy) && MathUtils.getDistance(ship, enemy) <= range && enemies < 5 && ship.isAlive)
                {
                    targetedShips.add(enemy)
                }
            }

            var shipsToBeRemoved: MutableList<ShipAPI> = ArrayList()
            for (enemy in targetedShips)
            {
                if (MathUtils.getDistance(ship, enemy) >= range || !ship.isAlive || !enemy.isAlive)
                {
                    shipsToBeRemoved.add(enemy)
                }
            }
            targetedShips.removeAll(shipsToBeRemoved)
            Global.getCombatEngine()
            var stats = ship.mutableStats
            if (enemies != 0)
            {
                stats.maxSpeed.modifyMult(id, 1 + (0.05f * enemies))
                stats.shieldDamageTakenMult.modifyMult(id, 1 - (0.05f * enemies))
            }
            else
            {
                stats.maxSpeed.unmodify(id)
                stats.shieldDamageTakenMult.unmodify(id)
            }

            //ship.setJitterUnder(this, color, 1f, 0, 0f, 0f);
            //ship.engineController.fadeToOtherColor(this, color, Color(color.red, color.green, color.blue, 150), 1f, 1f)
            ship.engineController.extendFlame("otherhull_id", 0f + (enemies), 0f, 0f)
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?)
        {
            val alphaMult = viewport!!.alphaMult * 1f

           /* GL11.glPushMatrix()

            GL11.glTranslatef(0f, 0f, 0f)
            GL11.glRotatef(0f, 0f, 0f, 1f)

            GL11.glDisable(GL11.GL_TEXTURE_2D)
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE)
            GL11.glColor4ub(color.getRed().toByte(), color.getGreen().toByte(), color.getBlue().toByte(), (color.getAlpha() * alphaMult).toInt().toByte())

            for (target in targetedShips)
            {
                GL11.glEnable(GL11.GL_LINE_SMOOTH)
                GL11.glBegin(GL11.GL_LINE_STRIP)

                GL11.glVertex2f(ship.location.x, ship.location.y) // from
                GL11.glVertex2f(target.location.x, target.location.y) // to

                GL11.glEnd()
            }

            GL11.glPopAttrib()
            GL11.glPopMatrix()*/
        }

        override fun getRenderRadius(): Float {
            return 10000f
        }

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return EnumSet.of(CombatEngineLayers.BELOW_PHASED_SHIPS_LAYER)
        }
    }
}