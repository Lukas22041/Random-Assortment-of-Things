package assortment_of_things.abyss.shipsystem

import assortment_of_things.abyss.hullmods.abyssals.AbyssalsCoreHullmod
import assortment_of_things.combat.AfterImageRenderer
import assortment_of_things.combat.CombatWarpingSpriteRenderer
import assortment_of_things.misc.getAndLoadSprite
import com.fs.graphics.Sprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.impl.combat.BaseShipSystemScript
import com.fs.starfarer.api.plugins.ShipSystemStatsScript
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class ChuulShipsystem : BaseShipSystemScript() {

    var ship: ShipAPI? = null

    var added = false


    class TestRender(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin() {

        //var sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2.jpg")
        var sprite2 = Global.getSettings().getAndLoadSprite("graphics/fx/rat_darkener.png")

        var sprite: Sprite

        init {
            Global.getSettings().loadTexture("graphics/backgrounds/abyss/Abyss2.jpg")
            sprite = Sprite("graphics/backgrounds/abyss/Abyss2.jpg")
        }

        var renderer = CombatWarpingSpriteRenderer(16, 1f)

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return EnumSet.of(CombatEngineLayers.UNDER_SHIPS_LAYER, CombatEngineLayers.ABOVE_PARTICLES)
        }

        override fun getRenderRadius(): Float {
            return 10000000f
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
            super.render(layer, viewport)

            var level = ship.system.effectLevel

            var mult = ship.mutableStats.timeMult.modified

            if (layer == CombatEngineLayers.UNDER_SHIPS_LAYER) {


                renderer.advance(Global.getCombatEngine().elapsedInLastFrame * mult)
                renderer.overwriteColor = Color(0, 120, 255).setAlpha((230 * level).toInt())
                renderer.render(sprite, false, viewport!!)

                /*sprite.alphaMult = 0.9f * level
                sprite.color = Color(0, 120, 255)
                sprite.setSize(viewport!!.visibleWidth , viewport.visibleHeight)
                sprite.render(viewport!!.llx , viewport.lly )*/
            }

            if (layer == CombatEngineLayers.ABOVE_PARTICLES) {
                sprite2.setSize(viewport!!.visibleWidth + 200f, viewport.visibleHeight + 200f)
                sprite2.setNormalBlend()
                sprite2.alphaMult = 0.1f * level
                sprite2.color =  Color(0, 120, 255, 155)
                sprite2.render(viewport.llx - 100f, viewport.lly - 100f)
            }

           // sprite.setAdditiveBlend()




        }
    }

    var afterimageInterval = IntervalUtil(0.2f, 0.2f)


    override fun apply(stats: MutableShipStatsAPI?, id: String?, state: ShipSystemStatsScript.State?, effectLevel: Float) {
        super.apply(stats, id, state, effectLevel)

        ship = stats!!.entity as ShipAPI

       /* if (!added) {
            Global.getCombatEngine().addLayeredRenderingPlugin(TestRender(ship!!))

            added = true
        }*/

        if (ship!!.system.isActive)
        {
            AbyssalsCoreHullmod.getRenderer(ship!!).enableBlink()
        }
        else
        {
            AbyssalsCoreHullmod.getRenderer(ship!!).disableBlink()
        }

        if (AbyssalsCoreHullmod.isChronosCore(ship!!))
        {


/*
            val shipTimeMult = 1f + (100f) * effectLevel
            stats.timeMult.modifyMult(id, shipTimeMult)
            Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)*/

            stats.ballisticRoFMult.modifyMult(id, 1f + (0.33f * effectLevel))
            stats.energyRoFMult.modifyMult(id, 1f + (0.33f * effectLevel))
            stats.missileRoFMult.modifyMult(id, 1f + (0.33f * effectLevel))

            stats.energyWeaponFluxCostMod.modifyMult(id, 1f - (0.33f * effectLevel));
            stats.ballisticWeaponFluxCostMod.modifyMult(id, 1f - (0.33f * effectLevel))
            stats.missileWeaponFluxCostMod.modifyMult(id, 1f - (0.33f * effectLevel))
        }

        if (AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            stats.ballisticWeaponDamageMult.modifyMult(id, 1f + (0.40f * effectLevel))
            stats.energyWeaponDamageMult.modifyMult(id, 1f + (0.40f * effectLevel))
            stats.missileWeaponDamageMult.modifyMult(id, 1f + (0.40f * effectLevel))
        }
    }

    override fun isUsable(system: ShipSystemAPI?, ship: ShipAPI?): Boolean {
        if (AbyssalsCoreHullmod.isCosmosCore(ship!!)) return true
        if (AbyssalsCoreHullmod.isChronosCore(ship!!)) return true
        return false
    }

    override fun getDisplayNameOverride(state: ShipSystemStatsScript.State?, effectLevel: Float): String {
        if (ship == null) return "Inactive Shipsystem"
        if (AbyssalsCoreHullmod.isChronosCore(ship!!))
        {
            return "Temporal Burst"
        }
        else if ( AbyssalsCoreHullmod.isCosmosCore(ship!!))
        {
            return "Cosmal Burst"
        }
        return "Inactive Shipsystem"
    }
}