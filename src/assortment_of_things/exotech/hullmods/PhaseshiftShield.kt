package assortment_of_things.exotech.hullmods

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.GraphicLibEffects
import assortment_of_things.misc.getAndLoadSprite
import assortment_of_things.misc.levelBetween
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import org.dark.shaders.util.ShaderLib
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.util.vector.Vector2f
import org.magiclib.util.MagicUI
import java.awt.Color
import java.util.*

class PhaseshiftShield : BaseHullMod() {

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

        tooltip.addPara("This shield can only take up to ${PhaseshiftShieldListener.maxShieldHP.toInt()} units of damage. It can be recharged for ${PhaseshiftShieldListener.regenerationRate.toInt()} units per second while phased, and ${(PhaseshiftShieldListener.regenPerSystemUse * 100).toInt()}%% of it can be restored on shipsystem activation. ", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${PhaseshiftShieldListener.maxShieldHP.toInt()}", "${PhaseshiftShieldListener.regenerationRate.toInt()}", "${(PhaseshiftShieldListener.regenPerSystemUse * 100).toInt()}%")


        tooltip.addSpacer(10f)

        tooltip.addPara("It has a shield efficiency of ${PhaseshiftShieldListener.shieldEfficiency}. Damage past your flux capacity can overload the ship and venting will rapidly drain the shield of charge.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${PhaseshiftShieldListener.shieldEfficiency}", "overload")


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

        ship.addListener(PhaseshiftShieldListener(ship))

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


    class PhaseshiftShieldListener(var ship: ShipAPI) : AdvanceableListener {
        init {
            ship.addListener(PhaseshiftShieldDamageModifier(this))
            ship.addListener(PhaseshiftShieldDamageConverter(this))
            Global.getCombatEngine().addLayeredRenderingPlugin(PhaseshiftShieldRenderer(ship, this))
        }

        companion object {
            var maxShieldHP = 4000f
            var regenerationRate = 400f
            var shieldEfficiency = 0.8f
            var regenPerSystemUse = 0.25f
        }

        var shieldHP = maxShieldHP

        var effectLevel = 1f
        var mostRecentDamage: Float? = null
        var mostRecentDamageHardflux: Boolean? = null
        var mostRecentDamagePoint: Vector2f? = null

        override fun advance(amount: Float) {

            var cloakLevel = ship.phaseCloak.effectLevel

            if (ship.fluxTracker.isVenting) {
                shieldHP -= regenerationRate * amount * 2 //Punish Venting
            }

            if (ship.isPhased) {
                var regen = regenerationRate * amount * (cloakLevel * cloakLevel * cloakLevel)
                shieldHP += regen
            }
            shieldHP = MathUtils.clamp(shieldHP, 0f, maxShieldHP)

            var shieldLevel = shieldHP / maxShieldHP
            shieldLevel = MathUtils.clamp(shieldLevel, 0f, 1f)





            if (ship.isPhased) {
               effectLevel -= 3f * amount
            } else {
                effectLevel += 1 * amount
            }
            effectLevel = MathUtils.clamp(effectLevel, 0f, 1f)

            var renderLevel = shieldHP.levelBetween(-100f, maxShieldHP*0.2f)
            renderLevel = MathUtils.clamp(renderLevel, 0.7f, 1f)
            if (shieldHP <= 0) renderLevel = 0f

            //var colorShiftLevel = shieldLevel * shieldLevel * shieldLevel
            var color = Misc.interpolateColor(ExoUtils.color1, Color(130,4,189, 255), 0f + ((1f-shieldLevel) * 0.4f))

            ship.setJitter(this, color, 0.1f * effectLevel * renderLevel, 3, 0f, 0 + 2f)
            ship.setJitterUnder(this,  color, 0.5f * effectLevel * renderLevel, 25, 0f, 7f + 2)

            if (shieldHP > 0.1) {
                ship.mutableStats.armorDamageTakenMult.modifyMult("phaserift_shield", 0.00001f)
                ship.mutableStats.hullDamageTakenMult.modifyMult("phaserift_shield", 0.00001f)
                ship.mutableStats.empDamageTakenMult.modifyMult("phaserift_shield", 0.00001f)
                ship.mutableStats.weaponDamageTakenMult.modifyMult("phaserift_shield", 0.00001f)
                ship.mutableStats.engineDamageTakenMult.modifyMult("phaserift_shield", 0.00001f)
            } else {
                ship.mutableStats.armorDamageTakenMult.unmodify("phaserift_shield")
                ship.mutableStats.hullDamageTakenMult.unmodify("phaserift_shield")
                ship.mutableStats.empDamageTakenMult.unmodify("phaserift_shield")
                ship.mutableStats.weaponDamageTakenMult.unmodify("phaserift_shield")
                ship.mutableStats.engineDamageTakenMult.unmodify("phaserift_shield")
            }

            MagicUI.drawInterfaceStatusBar(ship, shieldLevel, Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor(), 1f, "Shield", shieldHP.toInt())


        }

        fun getShieldLevel() = MathUtils.clamp(shieldHP / maxShieldHP, 0f, 1f)
    }

    class PhaseshiftShieldRenderer(var ship: ShipAPI, var listener: PhaseshiftShieldListener) : BaseCombatLayeredRenderingPlugin() {

        //var sprite = ship.spriteAPI
        var noise1 = Global.getSettings().getAndLoadSprite("graphics/fx/rat_phaseshift_shield_noise1.png")
        var noise2 = Global.getSettings().getAndLoadSprite("graphics/fx/rat_phaseshift_shield_noise2.png")

        var shader: Int = 0

        init {
            if (shader == 0) {

            }
        }

        override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
            return EnumSet.of(CombatEngineLayers.ABOVE_SHIPS_LAYER)
        }

        override fun getRenderRadius(): Float {
            return 1000000f
        }

        override fun advance(amount: Float) {

        }

        override fun render(layer: CombatEngineLayers?, viewport: ViewportAPI?) {

            shader = ShaderLib.loadShader(
                Global.getSettings().loadText("data/shaders/baseVertex.shader"),
                Global.getSettings().loadText("data/shaders/rat_phaseshift_shield.shader"))
            if (shader != 0) {
                GL20.glUseProgram(shader)

                GL20.glUniform1i(GL20.glGetUniformLocation(shader, "tex"), 0)
                GL20.glUniform1i(GL20.glGetUniformLocation(shader, "noiseTex1"), 1)
                GL20.glUniform1i(GL20.glGetUniformLocation(shader, "noiseTex2"), 2)

                GL20.glUseProgram(0)
            } else {
                var test = ""
            }

            var phaseLevel = 1-(ship.phaseCloak.effectLevel * ship.phaseCloak.effectLevel * ship.phaseCloak.effectLevel * ship.phaseCloak.effectLevel)
            if (ship.phaseCloak.state != ShipSystemAPI.SystemState.IN) {
                phaseLevel = listener.effectLevel
            }

            var renderLevel = listener.shieldHP.levelBetween(-100f, PhaseshiftShieldListener.maxShieldHP *0.2f)
            renderLevel = MathUtils.clamp(renderLevel, 0.7f, 1f)
            if (listener.shieldHP <= 0) renderLevel = 0f

            var lCover = ship!!.allWeapons.find { it.spec.weaponId == "rat_gilgamesh_cover_left" } ?: return
            var rCover = ship!!.allWeapons.find { it.spec.weaponId == "rat_gilgamesh_cover_right" } ?: return

            var lWing = ship!!.allWeapons.find { it.spec.weaponId == "rat_gilgamesh_wing_left" } ?: return
            var rWing = ship!!.allWeapons.find { it.spec.weaponId == "rat_gilgamesh_wing_right" } ?: return

            var sprite = ship.spriteAPI
            renderGlow(sprite, ship.location, ship.facing, 1f * renderLevel * phaseLevel, 1.25f)

            //Wings
            reRenderDeco(lWing.sprite, lWing.location, lWing.currAngle, 1f * phaseLevel)
            reRenderDeco(rWing.sprite, rWing.location, rWing.currAngle, 1f * phaseLevel)

            renderGlow(lWing.sprite, lWing.location, lWing.currAngle, 1f * renderLevel * phaseLevel, 1.5f)
            renderGlow(rWing.sprite, rWing.location, rWing.currAngle, 1f * renderLevel * phaseLevel, 1.5f)

            //Covers
            reRenderDeco(lCover.sprite, lCover.location, lCover.currAngle, 1f * phaseLevel)
            reRenderDeco(rCover.sprite, rCover.location, rCover.currAngle, 1f * phaseLevel)

            renderGlow(lCover.sprite, lCover.location, lCover.currAngle, 1f * renderLevel * phaseLevel, 3f)
            renderGlow(rCover.sprite, rCover.location, rCover.currAngle, 1f * renderLevel * phaseLevel, 3f)

            //Apply glow, as the medium hardpoint can be past the boundary, which makes it not fully encompassed
            var frontWeapon = ship.allWeapons.find { it.slot.id == "WS0010" }
            if (frontWeapon != null) {
                renderGlow(frontWeapon.sprite, frontWeapon.location, frontWeapon.currAngle, 0.75f * renderLevel * phaseLevel, 1.5f)
            }


        }

        fun reRenderDeco(sprite: SpriteAPI, loc: Vector2f, angle: Float, alpha: Float) {
            sprite.setNormalBlend()
            sprite.alphaMult = alpha
            sprite.angle = angle - 90f
            //sprite.setSize(width, h -20f)
            sprite.renderAtCenter(loc.x, loc.y)
        }

        fun renderGlow(sprite: SpriteAPI, loc: Vector2f, angle: Float, alpha: Float, intensity: Float) {

            var level = listener.getShieldLevel()

            level = 1f - ((1f-level) * 0.4f)

            GL20.glUseProgram(shader)

            GL20.glUniform1f(GL20.glGetUniformLocation(shader, "iTime"), Global.getCombatEngine().getTotalElapsedTime(false) / 12f)
            GL20.glUniform1f(GL20.glGetUniformLocation(shader, "alphaMult"),  alpha)
            GL20.glUniform1f(GL20.glGetUniformLocation(shader, "level"),  level)
            GL20.glUniform1f(GL20.glGetUniformLocation(shader, "intensity"),  intensity)


            //Bind Texture
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, sprite.textureId)

            //Setup Noise1
            //Noise texture needs to be power of two or it wont repeat correctly! (32x32, 64x64, 128x128)
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 1)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, noise1!!.textureId)

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)

            //Setup Noise2
            //Noise texture needs to be power of two or it wont repeat correctly! (32x32, 64x64, 128x128)
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 2)
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, noise1!!.textureId)

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST)

            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT)
            GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT)


            //Reset Texture
            GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0)

            var width = sprite.width
            var height = sprite.width

            sprite.setNormalBlend()
            sprite.alphaMult = 1f
            sprite.angle = angle - 90f
            //sprite.setSize(width, h -20f)
            sprite.renderAtCenter(loc.x, loc.y)

            GL20.glUseProgram(0)
        }

    }

    class PhaseshiftShieldDamageConverter(var listener: PhaseshiftShieldListener) : DamageListener {
        override fun reportDamageApplied(source: Any?, target: CombatEntityAPI?, result: ApplyDamageResultAPI?) {

            var recent = listener.mostRecentDamage ?: return
            var hardflux = listener.mostRecentDamageHardflux ?: return
            var point = listener.mostRecentDamagePoint ?: return

            var damage = recent * PhaseshiftShieldListener.shieldEfficiency

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
                listener.shieldHP = MathUtils.clamp(listener.shieldHP, 0f, PhaseshiftShieldListener.maxShieldHP)

                //Spawn Distortions if the damage was significant


                //Ensure onhits are triggered as shield hits
                result?.damageToShields = damage

                if (listener.shieldHP <= 0) {
                    Global.getSoundPlayer().playSound("rat_gilgamesh_shield_burnout", 0.65f + MathUtils.getRandomNumberInRange(-0.2f, 0.2f), 1.3f, ship.location, ship.velocity)

                    GraphicLibEffects.CustomRippleDistortion(ship.location,
                        Vector2f(),
                        ship.collisionRadius + 200f,
                        3f,
                        false,
                        0f,
                        360f,
                        1f,
                        0f,0f,3f,
                        0.3f,0f
                    )
                }


                var rippleLevel = damage.levelBetween(100f, 400f)

                if (rippleLevel >= 0.05) {
                    GraphicLibEffects.CustomRippleDistortion(point,
                        Vector2f(),
                        300f * rippleLevel,
                        2f,
                        false,
                        0f,
                        360f,
                        1f,
                        0f,0f,0.6f,
                        0.3f,0f
                    )
                }




            }

            listener.mostRecentDamage = null
            listener.mostRecentDamageHardflux = null
            listener.mostRecentDamagePoint = null

        }
    }

    class PhaseshiftShieldDamageModifier(var listener: PhaseshiftShieldListener) : DamageTakenModifier {

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

