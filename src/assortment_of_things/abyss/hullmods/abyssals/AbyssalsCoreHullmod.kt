package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.HullmodUtils
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.FaderUtil
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class AbyssalsCoreHullmod : BaseHullMod() {

    companion object {
        fun getRenderer(ship: ShipAPI) : AbyssalCoreRenderer {
            return ship.customData.get("abyssal_glow_renderer") as AbyssalCoreRenderer
        }

        fun getColorForCore(ship: ShipAPI) : Color
        {
            var color = Color(130, 27, 150,255)

            if (isCosmosCore(ship))
            {
                color = Color(255, 0, 100)
            }
            if (isChronosCore(ship))
            {
                color = Color(0, 150, 255)
            }

            return color
        }

        fun isChronosCore(ship: ShipAPI) : Boolean
        {
            if (ship.variant.hasHullMod("rat_chronos_conversion")) return true
            if (ship.captain == null) return false
            if (ship.captain.aiCoreId == RATItems.CHRONOS_CORE) return true
            return false
        }

        fun isCosmosCore(ship: ShipAPI) : Boolean
        {
            if (ship.variant.hasHullMod("rat_cosmos_conversion")) return true
            if (ship.captain == null) return false
            if (ship.captain.aiCoreId == RATItems.COSMOS_CORE) return true
            return false
        }

        fun isHullmodIntegration(ship: ShipAPI) : Boolean {
            if (ship.variant.hasHullMod("rat_chronos_conversion")) return true
            if (ship.variant.hasHullMod("rat_cosmos_conversion")) return true
            return false
        }
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        if (!stats!!.variant.hasHullMod("rat_abyssal_conversion") && !stats!!.variant.hasHullMod("rat_chronos_conversion") && !stats!!.variant.hasHullMod("rat_cosmos_conversion") && !stats.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats.variant.addPermaMod(HullMods.AUTOMATED)
        }

        //stats!!.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, 0f);

    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {


    }



    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (Global.getCombatEngine() == null) return
        var renderer = AbyssalCoreRenderer(ship!!)
        ship.setCustomData("abyssal_glow_renderer", renderer)
        Global.getCombatEngine().addLayeredRenderingPlugin(renderer)


    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        tooltip!!.addSpacer(5f)
        tooltip.addPara("This hull synergises well with the Chronos and Cosmos AI cores, and are required for the shipsystem to function. The installed core influences the effect of it.", 0f)
        tooltip.addSpacer(10f)
      /*  tooltip.addSectionHeading("AI Core Synergy", Alignment.MID, 0f)
        tooltip.addSpacer(10f)*/

        AbyssalsHullmodDescriptions.createDescription(tooltip, hullSize, ship, width, isForModSpec)

        tooltip.addSpacer(5f)
       /* tooltip.addSectionHeading("Enviroment", Alignment.MID, 0f)
        tooltip.addSpacer(10f)

        tooltip.addPara("Abyssal hulls are immune to damage from Abyssal Storms, but also from similar hazards" +
                " in other enviroments.", 0f)
*/

    }

    class
    AbyssalCoreRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin(CombatEngineLayers.ABOVE_SHIPS_LAYER) {

        var fader = FaderUtil(1f, 0.3f, 0.2f, false, false)
        var sprite: SpriteAPI? = null
        var phaseSprite: SpriteAPI? = null

        private var baseGlowAlpha = 0.5f
        private var additiveGlowAlpha = 0.3f
        private var blink: Boolean = false
        private var lowest = 0.05f

        init {
            var path = ship.hullSpec.spriteName.replace(".png", "") + "_glow.png"

            Global.getSettings().loadTexture(path)
            sprite = Global.getSettings().getSprite(path)
        }

        fun enableBlink() { blink = true }
        fun disableBlink() { blink = false }

        fun configureBlink(lowest: Float, inDuration: Float, outDuration: Float)
        {
            this.lowest = lowest
            fader.setDuration(inDuration, outDuration)
        }

        override fun advance(amount: Float) {
            super.advance(amount)
            fader.advance(amount)
            if (fader.brightness >= 1 && blink)
            {
                fader.fadeOut()
            }
            else if (fader.brightness <= lowest)
            {
                fader.fadeIn()
            }
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
            super.render(layer, viewport)

            if (sprite == null) return
            if (!ship.isAlive || ship.isHulk) return

            var c = getColorForCore(ship)

            sprite!!.angle = ship.facing + 270
            sprite!!.color = c
            ship.engineController.fadeToOtherColor("rat_abyssals_enginefade", c, c.setAlpha(75), 1f, 1f)

            sprite!!.alphaMult = baseGlowAlpha
            sprite!!.setNormalBlend()
            sprite!!.renderAtCenter(ship.location.x, ship.location.y)
            if (ship.captain == null && !isHullmodIntegration(ship)) return
            if ((ship.captain != null && ship.captain.isAICore) || isHullmodIntegration(ship))
            {

                if (ship.baseOrModSpec().hullId == "rat_aboleth" || ship.baseOrModSpec().hullId == "rat_aboleth_m" || ship.baseOrModSpec().hullId == "rat_makara" ) {
                    for (weapon in ship.allWeapons.filter { it.isDecorative }) {
                        weapon.sprite.color = Color(255, 255, 255, (254 * (1 - ship.system.effectLevel)).toInt())
                    }
                }

                if (ship.isPhased)
                {

                    if (ship.baseOrModSpec().hullId == "rat_aboleth" || ship.baseOrModSpec().hullId == "rat_aboleth_m" || ship.baseOrModSpec().hullId == "rat_makara" )
                    {
                        if (phaseSprite == null)
                        {
                            var path = ship.hullSpec.spriteName.replace(".png", "") + "_phaseglow.png"
                            Global.getSettings().loadTexture(path)
                            phaseSprite = Global.getSettings().getSprite(path)
                        }

                        phaseSprite!!.angle = ship.facing + 270
                        phaseSprite!!.color = c

                        phaseSprite!!.setAdditiveBlend()
                        phaseSprite!!.alphaMult = 0.7f + (0.3f * fader.brightness)
                        phaseSprite!!.renderAtCenter(ship.location.x, ship.location.y)
                    }

                    sprite!!.setAdditiveBlend()
                    sprite!!.alphaMult = 1 * fader.brightness
                    sprite!!.renderAtCenter(ship.location.x, ship.location.y)
                }
                else
                {
                    sprite!!.setAdditiveBlend()
                    sprite!!.alphaMult = additiveGlowAlpha * fader.brightness
                    sprite!!.renderAtCenter(ship.location.x, ship.location.y)
                }
            }

        }

        override fun isExpired(): Boolean {
            return false
        }

        override fun getRenderRadius(): Float {
            return 10000f
        }

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return EnumSet.of(CombatEngineLayers.BELOW_PHASED_SHIPS_LAYER)
        }

    }
}

