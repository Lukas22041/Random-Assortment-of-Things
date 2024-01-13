package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class AbyssalsAdaptabilityHullmod : BaseHullMod() {

    companion object {
        fun getRenderer(ship: ShipAPI) : AbyssalCoreRenderer {
            return ship.customData.get("abyssal_glow_renderer") as AbyssalCoreRenderer
        }

        fun getColorForCore(ship: ShipAPI) : Color
        {
            var color = Color(130, 27, 150,255)

            if (isSeraphCore(ship))
            {
                color = Color(196, 20, 35, 255)
            }
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


        fun hasAbyssalCore(ship: ShipAPI) : Boolean {
            if (isSeraphCore(ship)) return true
            if (isChronosCore(ship)) return true
            if (isCosmosCore(ship)) return true
            return false
        }

        fun isSeraphCore(ship: ShipAPI) : Boolean
        {
            if (ship.variant.hasHullMod("rat_seraph_conversion")) return true
            if (ship.captain == null) return false
            if (ship.captain.aiCoreId == RATItems.SERAPH_CORE) return true
            return false
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
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        if (Global.getSector().playerFleet?.fleetData?.membersListCopy?.contains(stats!!.fleetMember) == true) {
            stats!!.variant.removeTag(Tags.SHIP_LIMITED_TOOLTIP)
        }

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

        var stats = ship.mutableStats

        if (!hasAbyssalCore(ship)) {
            stats.systemCooldownBonus.modifyMult(id, 1.333f)
            stats.systemRegenBonus.modifyMult(id, 0.666f)
        }
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight)
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

        tooltip!!.addSpacer(5f)
        tooltip.addPara("This type of hull is sensitive to the kind of ai core that controls it. " +
                "Without an abyssal ai-core, the shipsystems cooldown and time to restore charges is worsened by 33%%.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "33%")

        tooltip!!.addLunaElement(0f, 0f).apply {
            render {particleSpawner.renderVignette(element, it)  }
        }
    }

    class AbyssalCoreRenderer(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin(CombatEngineLayers.ABOVE_SHIPS_LAYER) {

        var fader = FaderUtil(1f, 0.3f, 0.2f, false, false)
        var sprite: SpriteAPI? = null

        private var baseGlowAlpha = 0.5f
        private var additiveGlowAlpha = 0.3f
        private var blink: Boolean = false
        private var lowest = 0.05f

        init {
            var path = ship.hullSpec.spriteName.replace(".png", "") + "_glow.png"
            sprite = Global.getSettings().getAndLoadSprite(path)
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

            var glowColor = getColorForCore(ship)

            sprite!!.angle = ship.facing + 270
            sprite!!.color = glowColor
            ship.engineController.fadeToOtherColor("rat_abyssals_enginefade", glowColor, glowColor.setAlpha(75), 1f, 1f)

            sprite!!.alphaMult = baseGlowAlpha
            sprite!!.setNormalBlend()
            sprite!!.renderAtCenter(ship.location.x, ship.location.y)

            sprite!!.setAdditiveBlend()
            sprite!!.alphaMult = additiveGlowAlpha * fader.brightness
            sprite!!.renderAtCenter(ship.location.x, ship.location.y)
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

