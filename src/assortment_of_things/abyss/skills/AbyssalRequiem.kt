package assortment_of_things.abyss.skills

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.misc.GraphicLibEffects
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.HullDamageAboutToBeTakenListener
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.loading.DamagingExplosionSpec
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.Misc
import org.dark.shaders.post.PostProcessShader
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.entities.SimpleEntity
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class AbyssalRequiem : RATBaseShipSkill() {

    var modID = "rat_abyssal_bloodstream"

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?,  skill: SkillSpecAPI?, info: TooltipMakerAPI?,  width: Float) {
        info!!.addSpacer(2f)


        info.addPara("A special bond has been formed. ",
            0f, AbyssUtils.ABYSS_COLOR, AbyssUtils.ABYSS_COLOR)

        info.addSpacer(10f)

        info.addPara("Once per deployment, if the piloted ship is about to be destroyed, time stops, an infinite void forms and the ships hull is restored to operateable conditions. ",
            0f, AbyssUtils.ABYSS_COLOR, AbyssUtils.ABYSS_COLOR)

        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {



        var ship = stats!!.entity
        if (ship is ShipAPI) {
            if (!ship.hasListenerOfClass(AbyssalBloodstreamListener::class.java)) {
                var listener = AbyssalBloodstreamListener(ship)
                ship.addListener(listener)
                Global.getCombatEngine().addLayeredRenderingPlugin(listener)
            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

    class AbyssalBloodstreamListener(var ship: ShipAPI) : BaseCombatLayeredRenderingPlugin(), HullDamageAboutToBeTakenListener {

        var triggered = false
        var done = false
        var decrease = false

        var darken = Global.getSettings().getSprite("graphics/fx/rat_black.png")
        var vignette = Global.getSettings().getSprite("graphics/fx/rat_darkness_vignette_reversed.png")

        var fader = FaderUtil(1f, 6f, 8f, true, true)
        var level = 0f

        var color = Color(196, 20, 35, 50)

        override fun notifyAboutToTakeHullDamage(param: Any?, ship: ShipAPI?, point: Vector2f?, damageAmount: Float): Boolean {

            if (Global.getCombatEngine().isCombatOver) return false
            if (Global.getCombatEngine().playerShip != ship) return false

            if (triggered && !done) {
                return true
            }

            var custom = Global.getCombatEngine().customData
            if (custom.contains("requirem_triggered")) return false

            if (ship!!.variant.hasHullMod(HullMods.PHASE_ANCHOR)) return false


            if (ship!!.hitpoints - damageAmount <= 0 && !triggered) {
                ship.hitpoints = 10f
                triggered = true

                var custom = Global.getCombatEngine().customData
                if (ship!!.captain == Global.getSector().playerPerson) {
                    custom.set("requirem_triggered", true)
                }
                if (ship!!.captain?.aiCoreId == "rat_neuro_core") {
                    custom.set("requirem_triggered_neuro", true)
                }

                for (i in 0..100) {
                    ship!!.exactBounds.update(ship!!.location, ship!!.facing)
                    var from = Vector2f(ship!!.exactBounds.segments.random().p1)

                    var angle = Misc.getAngleInDegrees(ship.location, from)
                    var to = MathUtils.getPointOnCircumference(ship.location, MathUtils.getRandomNumberInRange(100f, 300f) + ship.collisionRadius, angle + MathUtils.getRandomNumberInRange(-30f, 30f))

                    Global.getCombatEngine().spawnEmpArcVisual(from, ship, to, SimpleEntity(to), 5f, Color(196, 20, 35, 255), Color(196, 20, 35, 255))
                }

                var explosionSpec = DamagingExplosionSpec.explosionSpecForShip(ship)
                explosionSpec.radius *= 1.5f
                explosionSpec.coreRadius *= 1.5f
                explosionSpec.detailedExplosionRadius *= 1.5f
                explosionSpec.detailedExplosionFlashRadius *= 1.5f
                explosionSpec.particleSpawnRadius *= 1.5f

                Global.getCombatEngine().spawnDamagingExplosion(explosionSpec, ship, ship.location, false)
                for (weapon in ship.allWeapons) {
                    weapon.repair()
                }
                ship.fluxTracker.stopOverload()
                ship.engineController.shipEngines.forEach { it.repair(); it.hitpoints = it.maxHitpoints }

                Global.getSoundPlayer().playSound("rat_bloodstream_trigger", 1f, 2f, ship.location, ship.velocity)
                Global.getSoundPlayer().playSound("system_entropy", 1f, 1.5f, ship.location, ship.velocity)
                Global.getSoundPlayer().playSound("explosion_ship", 1f, 1f, ship.location, ship.velocity)

                GraphicLibEffects.CustomRippleDistortion(Vector2f(ship.location), Vector2f(), 3000f, 10f, true, ship.facing, 360f, 1f
                    ,1f, 1f, 1f, 1f, 1f)

                GraphicLibEffects.CustomBubbleDistortion(Vector2f(ship.location), Vector2f(), 1000f + ship.collisionRadius, 25f, true, ship.facing, 360f, 1f
                    ,0.1f, 0.1f, 1f, 0.3f, 1f)


            }

            if (triggered && !done) {
                return true
            }

            return false
        }

        override fun advance(amount: Float) {

            /*GraphicLibEffects.CustomBubbleDistortion(Vector2f(ship.location), Vector2f(), ship.collisionRadius * 3f, 50f, false, ship.facing, 360f, 1f
                ,0f, 3f, 1f, 0f, 1f)*/

            /*GraphicLibEffects.CustomRippleDistortion(Vector2f(ship.location), Vector2f(), ship.collisionRadius * 3f, 100f, false, ship.facing, 360f, 1f
                ,0f, 3f, 1f, 0f, 1f)*/

            val shipTimeMult = 1 + (60f * level)
            var actualAmount = amount * shipTimeMult


            if (triggered && !done && !decrease) {
                level += 2f * actualAmount
            }
            if (decrease) {
                level -= 0.33f * actualAmount

                if (level <= 0f) {
                    done = true
                    for (weapon in ship.allWeapons) {
                        weapon.repair()
                    }

                    PostProcessShader.resetDefaults()
                }
            }


            level = MathUtils.clamp(level, 0f, 1f)

            fader.advance(actualAmount)
            if (fader.brightness >= 1)
            {
                fader.fadeOut()
            }
            else if (fader.brightness <= 0.6)
            {
                fader.fadeIn()
            }

            var player = ship == Global.getCombatEngine().playerShip
            var id = "rat_bloodstream_${ship.id}"
            var stats = ship.mutableStats

            stats.getTimeMult().modifyMult(id, shipTimeMult)
            if (player) {
                Global.getCombatEngine().timeMult.modifyMult(id, 1f / shipTimeMult)
            } else {
                Global.getCombatEngine().timeMult.unmodify(id)
            }

            if (triggered && !done) {


                PostProcessShader.setNoise(false, 0.4f * level)

                PostProcessShader.setSaturation(false, 1f + (0.3f * level))

                Global.getSoundPlayer().applyLowPassFilter(1f, 1 - (0.3f * level))

                ship!!.setJitterUnder(this, color, 1f * level, 25, 2f, 14f)

                ship.hitpoints += (ship.maxHitpoints * 0.1f) * actualAmount
                ship.hitpoints = MathUtils.clamp(ship.hitpoints, 0f, ship.maxHitpoints)

                var percentPerSecond = 0.1f
                if (ship.fluxLevel > 0f) {
                    ship.fluxTracker.decreaseFlux((ship.fluxTracker.maxFlux * percentPerSecond) * actualAmount)
                }

                if (ship.hitpoints >= ship.maxHitpoints) {
                    decrease = true
                }
            }

        }

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return EnumSet.of(CombatEngineLayers.JUST_BELOW_WIDGETS, CombatEngineLayers.BELOW_PLANETS)
        }

        override fun getRenderRadius(): Float {
            return 1000000f
        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {
            if (triggered) {


                if (layer == CombatEngineLayers.BELOW_PLANETS) {
                    darken.color = Color(0, 0, 0)
                    darken.alphaMult = level
                    darken.setSize(viewport!!.visibleWidth, viewport!!.visibleHeight)
                    darken.render(viewport!!.llx, viewport!!.lly)
                }

                if (layer == CombatEngineLayers.JUST_BELOW_WIDGETS) {
                    vignette.color = Color(50, 0, 0)
                    vignette.alphaMult = level * fader.brightness


                    var offset = 300
                    vignette.setSize(viewport!!.visibleWidth + offset, viewport!!.visibleHeight + offset)
                    vignette.render(viewport!!.llx - (offset * 0.5f), viewport!!.lly - (offset * 0.5f))
                }
            }
        }
    }
}