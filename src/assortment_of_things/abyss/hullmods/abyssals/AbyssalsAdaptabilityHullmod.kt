package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
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
        fun getRenderer(ship: ShipAPI) : AbyssalCoreRenderer? {
            return ship.customData.get("abyssal_glow_renderer") as AbyssalCoreRenderer?
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

        fun getSecondaryColorForCore(ship: ShipAPI) : Color
        {
            var color = Color(150, 0 ,255)

            if (isSeraphCore(ship))
            {
                color = Color(156, 20, 35, 255)
            }

            return color
        }


        fun hasAbyssalCore(ship: ShipAPI) : Boolean {
            if (isSeraphCore(ship)) return true
            if (isChronosCore(ship)) return true
            if (isCosmosCore(ship)) return true
            if (isPrimordialCore(ship)) return true
            return false
        }

        fun hasAbyssalCore(ship: FleetMemberAPI) : Boolean {
            if (isSeraphCore(ship)) return true
            if (isChronosCore(ship)) return true
            if (isCosmosCore(ship)) return true
            if (isPrimordialCore(ship)) return true
            return false
        }

        fun isPrimordialCore(ship: ShipAPI) : Boolean
        {
            if (ship.variant.hasHullMod("rat_primordial_conversion")) return true
            if (ship.captain == null) return false
            if (ship.captain.aiCoreId == RATItems.PRIMORDIAL) return true
            return false
        }

        fun isPrimordialCore(ship: FleetMemberAPI) : Boolean
        {
            if (ship.variant.hasHullMod("rat_primordial_conversion")) return true
            if (ship.captain == null) return false
            if (ship.captain.aiCoreId == RATItems.PRIMORDIAL) return true
            return false
        }

        fun isSeraphCore(ship: ShipAPI) : Boolean
        {
            if (ship.variant.hasHullMod("rat_seraph_conversion")) return true
            if (ship.captain == null) return false
            if (ship.captain.aiCoreId == RATItems.SERAPH_CORE) return true
            return false
        }

        fun isSeraphCore(ship: FleetMemberAPI) : Boolean
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

        fun isChronosCore(ship: FleetMemberAPI) : Boolean
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

        fun isCosmosCore(ship: FleetMemberAPI) : Boolean
        {
            if (ship.variant.hasHullMod("rat_cosmos_conversion")) return true
            if (ship.captain == null) return false
            if (ship.captain.aiCoreId == RATItems.COSMOS_CORE) return true
            return false
        }
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        if (stats!!.fleetMember == null) return

        if (Global.getSector().playerFleet?.fleetData?.membersListCopy?.contains(stats!!.fleetMember) == true) {
            stats!!.variant.removeTag(Tags.SHIP_LIMITED_TOOLTIP)
        }

        if (!stats!!.variant.hasHullMod("rat_abyssal_conversion") && !stats!!.variant.hasHullMod("rat_chronos_conversion") && !stats!!.variant.hasHullMod("rat_cosmos_conversion") && !stats!!.variant.hasHullMod("rat_seraph_conversion") && !stats.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats.variant.addPermaMod(HullMods.AUTOMATED)
        }

        if (stats.fleetMember.captain == null || stats.fleetMember.captain.isDefault) {
            stats.systemCooldownBonus.modifyMult(id, 1.50f)
            stats.systemRegenBonus.modifyMult(id, 0.5f)
        }
        else if (isChronosCore(stats.fleetMember)) {
            stats.timeMult.modifyMult(id, 1.1f)
            stats.systemCooldownBonus.modifyMult(id, 0.9f)
            stats.systemRegenBonus.modifyMult(id, 1.1f)
        }
        else if (isCosmosCore(stats.fleetMember)) {
            stats.hardFluxDissipationFraction.modifyFlat(id, 0.05f)

            stats.recoilPerShotMult.modifyMult(id, 0.5f)
            stats.recoilDecayMult.modifyMult(id, 1.25f)
            stats.maxRecoilMult.modifyMult(id, 0.75f)
        }
        else if (isSeraphCore(stats.fleetMember)) {
            stats.shieldDamageTakenMult.modifyFlat(id, 0.2f)
            var armor = mapOf(
                HullSize.FRIGATE to 100f,
                HullSize.DESTROYER to 150f,
                HullSize.CRUISER to 250f,
                HullSize.CAPITAL_SHIP to 350f,
            )
            stats.armorBonus.modifyFlat(id, armor.get(stats.fleetMember.variant.hullSize)!!)
            stats.hullBonus.modifyMult(id, 1.1f)
        }


        //stats!!.getDynamic().getStat(Stats.CORONA_EFFECT_MULT).modifyMult(id, 0f);

    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

        if (isChronosCore(ship!!)) {
            var mod = 1.1f
            var modID = ship.id + "abyssal_adaptability"

            ship.mutableStats!!.timeMult.modifyMult(modID, mod);
            if (ship == Global.getCombatEngine().playerShip) {
                Global.getCombatEngine().timeMult.modifyMult(modID + ship.id, 1 / mod)
            }
            else {
                Global.getCombatEngine().timeMult.unmodify(modID + ship.id)
            }
        }
    }

    override fun getDisplaySortOrder(): Int {
        return 1
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

        if (Global.getCombatEngine() == null) return
        var renderer = AbyssalCoreRenderer(ship!!)
        ship.setCustomData("abyssal_glow_renderer", renderer)
        Global.getCombatEngine().addLayeredRenderingPlugin(renderer)

        var stats = ship.mutableStats


    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        if (ship == null) return

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight)
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

        tooltip!!.addSpacer(5f)
        tooltip.addPara("This type of hull is sensitive to the kind of ai core that controls it. " +
                "Without an ai-core, the shipsystems cooldown and time to restore charges is worsened by 50%%." +
                "\n\n" +
                "Additionaly, certain mechanisms react differently to abyssal cores when installed in to the hull.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(), "50%")


        //IMGs

        var chronosSelected = isChronosCore(ship)
        var cosmosSelected = isCosmosCore(ship)
        var seraphSelected = isSeraphCore(ship)

        var chronosColor = Misc.getTextColor()
        if (!chronosSelected) chronosColor = Misc.getGrayColor()

        var cosmosColor = Misc.getTextColor()
        if (!cosmosSelected) cosmosColor = Misc.getGrayColor()

        var seraphColor = Misc.getTextColor()
        if (!seraphSelected) seraphColor = Misc.getGrayColor()

        tooltip.addSpacer(10f)

        var chronosImage = tooltip.beginImageWithText("graphics/icons/cargo/rat_chronos_core.png", 32f)
        chronosImage.addPara("Provides the ship with a 10%% increase in timeflow and the shipsystems cooldown recovers 10%% faster.", 0f,
            chronosColor, Misc.getHighlightColor(), "10%", "10%")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var cosmosImage = tooltip.beginImageWithText("graphics/icons/cargo/rat_cosmos_core.png", 32f)
        cosmosImage.addPara("The ship gains the ability to vent hardflux at 5%% of the normal dissipation rate and weapons have decreased recoil.", 0f,
            cosmosColor, Misc.getHighlightColor(), "5%", "decreased")
        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var seraphCore = tooltip.beginImageWithText("graphics/icons/cargo/rat_seraph_core.png", 32f)
        seraphCore.addPara("Worsens the shield efficiency by 0.2 but increases the ships armor by 100/150/250/350 and increases the ships hitpoints by 10%%.", 0f,
            seraphColor, Misc.getHighlightColor(), "0.2", "100", "150", "250", "350", "10%")
        tooltip.addImageWithText(0f)

        //End

        tooltip!!.addLunaElement(0f, 0f).apply {
            render {particleSpawner.renderForeground(element, it)  }
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

