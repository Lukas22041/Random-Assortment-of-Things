package assortment_of_things.exotech.hullmods

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicUI
import java.awt.Color
import java.util.*

class PhaseriftShield : BaseHullMod() {

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,   isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        var sprite = Global.getSettings().getAndLoadSprite("graphics/ui/rat_exo_hmod.png")

        var initialHeight = tooltip!!.heightSoFar
        var element = tooltip!!.addLunaElement(0f, 0f)

        tooltip!!.addSpacer(10f)

        tooltip!!.addPara("The Gilgamesh is capable of generating a thin layer of energy around its hull through activation of its phase-cloak and shipsystem. " +
                "This layer of energy performs similar to shields on a normal ship. "
            , 0f,
            Misc.getTextColor(), Misc.getHighlightColor(),  " ")

        tooltip.addSpacer(10f)

        tooltip.addPara("This shield can only take up to 3000 units of damage. It can be recharged for 300 units per second while phased, or 20%% of it can be restored from shipsystem activation. ", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "3000", "300", "20%")


        tooltip.addSpacer(10f)

        tooltip.addPara("It has a shield efficiency of 0.8. Damage past your flux capacity can overload the ship and venting will rapidly drain the shield of charge.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "0.8", "overload")


        element.render {
            sprite.setSize(tooltip.widthSoFar + 20, tooltip.heightSoFar + 10)
            sprite.setAdditiveBlend()
            sprite.render(tooltip.position.x, tooltip.position.y)
        }
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI, id: String?) {


    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI, id: String?) {

        if (Global.getCombatEngine() == null) return

        ship.addListener(PhaseriftShieldListener(ship))

    }

    override fun getDisplaySortOrder(): Int {
        return 2
    }

    override fun getNameColor(): Color {
        return Color(217, 164, 57)
    }

    override fun getBorderColor(): Color {
        return Color(217, 164, 57)
    }


    class PhaseriftShieldListener(var ship: ShipAPI) : AdvanceableListener {
        init {
            ship.addListener(PhaseriftShieldDamageModifier(this))
            ship.addListener(PhaseriftShieldDamageConverter(this))
            Global.getCombatEngine().addLayeredRenderingPlugin(PhaseriftShieldRenderer(ship, this))
        }

        var maxShieldHP = 3000f
        var shieldHP = maxShieldHP
        var regenerationRate = 300f
        var shieldEfficiency = 0.8f
        var regenPerSystemUse = 0.2f

        var effectLevel = 1f
        var mostRecentDamage: Float? = null
        var mostRecentDamageHardflux: Boolean? = null
        var mostRecentDamagePoint: Vector2f? = null

        override fun advance(amount: Float) {

            var cloakLevel = ship.phaseCloak.effectLevel

            if (ship.fluxTracker.isVenting) {
                shieldHP -= regenerationRate * amount * 3
            }

            if (ship.isPhased) {
                var regen = regenerationRate * amount * cloakLevel
                shieldHP += regen
            }
            shieldHP = MathUtils.clamp(shieldHP, 0f, maxShieldHP)

            var shieldLevel = shieldHP / maxShieldHP
            shieldLevel = MathUtils.clamp(shieldLevel, 0f, 1f)


            //MagicUI.drawHUDStatusBar(ship, 1f, ExoUtils.color1, ExoUtils.color1, 0f, "Shield", "", false)
            MagicUI.drawInterfaceStatusBar(ship, shieldLevel, Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor(), 1f, "Shield", shieldHP.toInt())

            if (ship.isPhased) {
               effectLevel -= 3f * amount
            } else {
                effectLevel += 1 * amount
            }
            effectLevel = MathUtils.clamp(effectLevel, 0f, 1f)

            var renderLevel = shieldHP.levelBetween(0f, maxShieldHP*0.2f)

            //var colorShiftLevel = shieldLevel * shieldLevel * shieldLevel
            var color = Misc.interpolateColor(ExoUtils.color1, Color(130,4,189, 255), 0f + ((1f-shieldLevel) * 0.4f))

            ship.setJitter(this, color, 0.1f * effectLevel * renderLevel, 3, 0f, 0 + 2f)
            ship.setJitterUnder(this,  color, 0.5f * effectLevel * renderLevel, 25, 0f, 7f + 2)


            if (shieldHP > 0.1) {
                ship.mutableStats.armorDamageTakenMult.modifyMult("phaserift_shield", 0.00001f)
                ship.mutableStats.hullDamageTakenMult.modifyMult("phaserift_shield", 0.00001f)
            } else {
                ship.mutableStats.armorDamageTakenMult.unmodify("phaserift_shield")
                ship.mutableStats.hullDamageTakenMult.unmodify("phaserift_shield")
            }
        }
    }

    class PhaseriftShieldRenderer(var ship: ShipAPI, var listener: PhaseriftShieldListener) : BaseCombatLayeredRenderingPlugin() {


        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return super.getActiveLayers()
        }

        override fun getRenderRadius(): Float {
            return 1000000f
        }

        override fun advance(amount: Float) {

        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

        }

    }

    class PhaseriftShieldDamageConverter(var listener: PhaseriftShieldListener) : DamageListener {
        override fun reportDamageApplied(source: Any?, target: CombatEntityAPI?, result: ApplyDamageResultAPI?) {

            var recent = listener.mostRecentDamage ?: return
            var hardflux = listener.mostRecentDamageHardflux ?: return
            var point = listener.mostRecentDamagePoint ?: return

            var damage = recent * listener.shieldEfficiency

            var active = false
            //Check if Shield is active
            if (listener.shieldHP > 0.1) {
                active = true
            }

            if (active) {

                //Apply Damage
                var ship = listener.ship
                var tracker = ship.fluxTracker

                tracker.increaseFlux(damage, hardflux)

                listener.shieldHP -= damage
                listener.shieldHP = MathUtils.clamp(listener.shieldHP, 0f, listener.maxShieldHP)

                //Spawn Distortions if the damage was significant


                //Ensure onhits are triggered as shield hits
                result?.damageToShields = damage
            }

            listener.mostRecentDamage = null
            listener.mostRecentDamageHardflux = null
            listener.mostRecentDamagePoint = null

        }
    }

    class PhaseriftShieldDamageModifier(var listener: PhaseriftShieldListener) : DamageTakenModifier {

        override fun modifyDamageTaken(param: Any?, target: CombatEntityAPI?, damage: DamageAPI?,  point: Vector2f?,
                                       shieldHit: Boolean): String? {

            //Transfer Damage to next listener
            var dam = damage!!.damage
            if (param is BeamAPI) {
                dam = damage.damage * damage.dpsDuration
            }

            dam *= damage.type.shieldMult //Since this value is only used if the shield is active, apply the shield mult in this place already.

            listener.mostRecentDamage = dam
            listener.mostRecentDamageHardflux = !damage.isSoftFlux || damage.isForceHardFlux
            listener.mostRecentDamagePoint = point

            return null
        }

    }



}

