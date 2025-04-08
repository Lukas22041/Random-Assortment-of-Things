package assortment_of_things.abyss.shipsystem.activators

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.GraphicLibEffects
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.ShipwideAIFlags.AIFlags
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.combat.MineStrikeStats
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.dark.shaders.util.ShaderLib
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL13
import org.lwjgl.opengl.GL20
import org.lwjgl.util.vector.Vector2f
import org.magiclib.subsystems.MagicSubsystem
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

class PrimordialSeaActivator(var ship: ShipAPI) : MagicSubsystem(ship) {


    var deactivated = false
    var maxRange = 3000f

    var apparations = ArrayList<ShipAPI>()
    var renderer: PrimordialSeaRenderer = PrimordialSeaRenderer(ship, this, apparations)

    var aiInterval = IntervalUtil(3f, 3f)


    init {
        Global.getCombatEngine().addLayeredRenderingPlugin(renderer)
    }

    override fun getBaseActiveDuration(): Float {
        return 12f
    }

    override fun getBaseCooldownDuration(): Float {
        return 20f //20
    }

    override fun getBaseInDuration(): Float {
        return 2f
    }

    override fun getBaseOutDuration(): Float {
        return 1f
    }

    override fun isToggle(): Boolean {
        return false
    }

    override fun canActivate(): Boolean {
        if (!ship.isAlive) return false
        return ship.customData.get("rat_boss_second_phase") != true
    }

    override fun shouldActivateAI(amount: Float): Boolean {
        var targetsInRange = false
        var iterator = Global.getCombatEngine().shipGrid.getCheckIterator(ship.location, 4000f, 4000f)
        for (other in iterator) {
            if (other !is ShipAPI) continue
            if (other == ship) continue
            if (other.owner == ship.owner) continue
            if (other.isFighter) continue

            if (!isInRange(ship, other) && MathUtils.getDistance(ship, other) >= 800f) continue

            targetsInRange = true
        }

        if (targetsInRange) {
            aiInterval.advance(amount)
            if (aiInterval.intervalElapsed()) {
                return true
            }
        }
        else {
            aiInterval = IntervalUtil(3f, 3f)
        }

        return false
    }

    fun isInRange(ship: ShipAPI, target: ShipAPI) : Boolean {
        var range = 0f
        for (weapon in ship.allWeapons) {
            if (weapon.range > range)
            {
                range = weapon.range
            }
        }
        var distance = MathUtils.getDistance(ship, target)
        var inRange = distance <= range + 100
        return inRange
    }

    override fun getDisplayText(): String {
        return "Primordial Sea"
    }

    override fun getHUDColor(): Color {
        return AbyssUtils.GENESIS_COLOR
    }

    override fun onActivate() {
        if (!ship.isAlive) return

        deactivated = false

        Global.getSoundPlayer().playSound("rat_genesis_system_sound", 0.7f, 1f, ship.location, ship.velocity)

        GraphicLibEffects.CustomRippleDistortion(ship!!.location, Vector2f(), ship.collisionRadius + 500, 75f, true, ship!!.facing, 360f, 1f
            ,0.5f, 3f, 1f, 1f, 1f)


        var variants = mutableListOf<String>()

        var extra = 0
        if (ship.variant.hasTag("rat_challenge_mode")) extra+=2

        /*if (ship.mutableStats?.fleetMember?.fleetData?.fleet?.faction?.id == "rat_abyssals_primordials") {
            extra += 1
        }*/

        variants += generateSequence { "rat_genesis_frigate_support_Standard" }.take(4+extra)
        variants += generateSequence { "rat_genesis_frigate_attack_Standard" }.take(4+extra)

        var takenTargets = ArrayList<ShipAPI>()

        for (variant in variants) {

            var targetShips = CombatUtils.getShipsWithinRange(ship.location, maxRange - 500).filter { !takenTargets.contains(it) && !it.isFighter}
            var loc = MathUtils.getRandomPointOnCircumference(ship.location, MathUtils.getRandomNumberInRange(600f, 2000f))
            var facing: Float? = null

            if (takenTargets.size < 3) {
                var target = targetShips.randomOrNull()
                if (target != null) {
                    takenTargets.add(target)
                    loc = MathUtils.getRandomPointOnCircumference(target.location, target.collisionRadius + MathUtils.getRandomNumberInRange(400f, 600f))
                    facing = Misc.getAngleInDegrees(loc, target.location)
                }
            }

            var apparation = spawnApparation(variant, loc)
            if (apparation != null) {
                apparations.add(apparation)
                if (facing != null) {
                    apparation.facing = facing

                }
            }
        }
    }




    override fun onFinished() {
        for (apparation in apparations) {
            Global.getCombatEngine().removeEntity(apparation)
        }

        apparations.clear()
    }

    override fun getAdvancesWhileDead(): Boolean {
        return true
    }

    override fun advance(amount: Float, isPaused: Boolean) {


        var range = getCurrentRange()
        for (apparation in apparations) {
           /* ReflectionUtils.invoke("setSuppressFloaties", apparation, true)*/
           // ReflectionUtils.set("visible", apparation, false)
          //  apparation.addTag("fx_drone")

            for (engine in apparation.engineController.shipEngines) {

                if (MathUtils.getDistance(engine.location, ship.location) <= range && apparation.isAlive) {
                    //engine.repair()
                    engine.engineSlot.color = Color(178, 36, 69, 255)
                    engine.engineSlot.glowAlternateColor = Color(178, 36, 69,255)
                    engine.engineSlot.glowSizeMult = 0.8f

                }
                else {
                    engine.engineSlot.color = Color(0, 0, 0, 0)
                    engine.engineSlot.glowAlternateColor = Color(0, 0, 0, 0)
                    engine.engineSlot.glowSizeMult = 0f
                    //engine.disable()
                }

            }

            //Hides that square that appears around opposing ships
            apparation.isForceHideFFOverlay = true

            if (MathUtils.getDistance(apparation.location, ship.location) <= range - apparation.collisionRadius && state != State.OUT ) {
                apparation.isPhased = false
                apparation.isHoldFire = false
                apparation.mutableStats.hullDamageTakenMult.modifyMult("rat_construct", 1f)
                apparation.alphaMult = 1f
            }
            else {
                apparation.isPhased = true
                apparation.isHoldFireOneFrame = true
                apparation.allWeapons.forEach { it.stopFiring() }
                apparation.mutableStats.hullDamageTakenMult.modifyMult("rat_construct", 0f)

                for (weapon in apparation.allWeapons) {
                    weapon.stopFiring()
                    weapon.setRemainingCooldownTo(0.5f)
                }
            }
        }

        if (isActive ||isIn || isOut) {
            Global.getSoundPlayer().playLoop("mote_attractor_loop_dark", ship, 0.5f, 1f * effectLevel, ship.location, ship.velocity)
        }


        if (ship.customData.get("rat_boss_second_phase") == true && state != State.OUT && state != State.COOLDOWN && state != State.READY) {
            setState(State.OUT)
        }

        if (state == State.OUT && !deactivated) {
            deactivated = true

            Global.getSoundPlayer().playSound("rat_genesis_system_sound", 0.6f, 0.8f, ship.location, ship.velocity)

            GraphicLibEffects.CustomBubbleDistortion(ship!!.location, Vector2f(), 1000f + ship!!.collisionRadius, 50f, true, ship!!.facing, 360f, 1f
                ,0.75f, 0.1f, 0.75f, 0.3f, 1f)
        }
    }

    fun getCurrentRange() : Float {
        return maxRange * effectLevel * effectLevel
    }

    fun spawnApparation(variantId: String, targetLoc: Vector2f) : ShipAPI?{
        var variant = Global.getSettings().getVariant(variantId)
        var manager = Global.getCombatEngine().getFleetManager(ship!!.owner)

        Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = true
        var apparation = spawnShipOrWingDirectly(variant, FleetMemberType.SHIP, ship!!.owner, ship!!.currentCR, Vector2f(100000f, 100000f), ship!!.facing) ?: return null
        apparation!!.fleetMember.id = Misc.genUID()
        Global.getCombatEngine().getFleetManager(ship!!.owner).isSuppressDeploymentMessages = false

        //apparation!!.captain = ship.captain

        var ogCaptain = ship.captain

        var core = Global.getSettings().createPerson()
        core.portraitSprite = ogCaptain.portraitSprite
        core.name = ship.captain.name

        for (skill in ogCaptain.stats.skillsCopy) {
            core.stats.setSkillLevel(skill.skill.id, skill.level)
        }

        apparation.captain = core

      /*  apparation.isPhased = true
        apparation.isHoldFireOneFrame = true
        apparation.isHoldFire = true

        for (weapon in apparation.allWeapons) {
            weapon.stopFiring()
            weapon.setForceNoFireOneFrame(true)
            weapon.setRemainingCooldownTo(0.3f)
        }
*/
        manager.removeDeployed(apparation, true)

        //apparation.alphaMult = 0f
       // apparation.spriteAPI.alphaMult = 0f
        apparation.spriteAPI.color = Color(0, 0 ,0 ,0)
     /*   apparation.extraAlphaMult = 0f
        apparation.setApplyExtraAlphaToEngines(false) //Disable to make engines not get way to small*/
        //apparation.addTag("module_no_status_bar")
        //Might be needed to hide bars on fighters, not sure
        //apparation.mutableStats.hullDamageTakenMult.modifyMult("rat_prim_sea", 0f)

        Global.getCombatEngine().addEntity(apparation)

        /*var loc = MathUtils.getRandomPointOnCircumference(ship.location, MathUtils.getRandomNumberInRange(600f, 1600f))*/
        var loc = findClearLocation(apparation, targetLoc)
        apparation.location.set(loc)
        var closest = CombatUtils.getShipsWithinRange(loc, 600f).filter { it != apparation }.randomOrNull()
        if (closest != null) {
            var angle = Misc.getAngleInDegrees(apparation.location, closest.location)
            apparation.facing = angle
        }
        else {
            apparation.facing = ship.facing
        }

        apparation.captain.setPersonality(Personalities.RECKLESS)
        apparation.shipAI = Global.getSettings().createDefaultShipAI(apparation, ShipAIConfig().apply { alwaysStrafeOffensively = true })
        apparation.shipAI.forceCircumstanceEvaluation()
        apparation.captain.setPersonality(Personalities.RECKLESS)

        //Randomise range a little so that constructs dont all orbit on the same radius as eachother.
        var rangeMod = MathUtils.getRandomNumberInRange(0.85f, 1.15f)
        apparation.mutableStats.energyWeaponRangeBonus.modifyMult("apparation_range", rangeMod)
        apparation.mutableStats.ballisticWeaponRangeBonus.modifyMult("apparation_range", rangeMod)
        apparation.mutableStats.missileWeaponRangeBonus.modifyMult("apparation_range", rangeMod)



        return apparation
    }

    fun spawnShipOrWingDirectly(variant: ShipVariantAPI?, type: FleetMemberType?, owner: Int, combatReadiness: Float, location: Vector2f?, facing: Float): ShipAPI? {
        val member = Global.getFactory().createFleetMember(type, variant)
        member.owner = owner
        member.crewComposition.addCrew(member.neededCrew)

        val ship = Global.getCombatEngine().getFleetManager(owner).spawnFleetMember(member, location, facing, 0f)
        ship.crAtDeployment = combatReadiness
        ship.currentCR = combatReadiness
        ship.owner = owner
        ship.shipAI.forceCircumstanceEvaluation()

        return ship
    }


    private fun findClearLocation(apparation: ShipAPI, dest: Vector2f): Vector2f? {
        if (isLocationClear(apparation, dest)) return dest
        val incr = 50f
        val tested = WeightedRandomPicker<Vector2f>()
        var distIndex = 1f
        while (distIndex <= 32f) {
            val start = Math.random().toFloat() * 360f
            var angle = start
            while (angle < start + 360) {
                val loc = Misc.getUnitVectorAtDegreeAngle(angle)
                loc.scale(incr * distIndex)
                Vector2f.add(dest, loc, loc)
                tested.add(loc)
                if (isLocationClear(apparation, loc)) {
                    return loc
                }
                angle += 60f
            }
            distIndex *= 2f
        }
        return if (tested.isEmpty) dest else tested.pick() // shouldn't happen
    }

    private fun isLocationClear(apparation: ShipAPI, loc: Vector2f): Boolean {
        for (other in Global.getCombatEngine().ships) {
            if (other.isShuttlePod) continue
            //if (other.isFighter) continue

            var otherLoc = other.shieldCenterEvenIfNoShield
            var otherR = other.shieldRadiusEvenIfNoShield
            if (other.isPiece) {
                otherLoc = other.location
                otherR = other.collisionRadius
            }


            val dist = Misc.getDistance(loc, otherLoc)
            val r = otherR
            var checkDist = apparation.shieldRadiusEvenIfNoShield
            if (dist < r + checkDist) {
                return false
            }
        }
        for (other in Global.getCombatEngine().asteroids) {
            val dist = Misc.getDistance(loc, other.location)
            if (dist < other.collisionRadius + MineStrikeStats.MIN_SPAWN_DIST) {
                return false
            }
        }
        return true
    }

}

class PrimordialSeaRenderer(var ship: ShipAPI, var activator: PrimordialSeaActivator, var apparations: List<ShipAPI>) : CombatLayeredRenderingPlugin {

    var sprite = Global.getSettings().getAndLoadSprite("graphics/backgrounds/abyss/Abyss2ForRift.jpg")
    var wormhole = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
    var wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    var systemGlow: SpriteAPI = Global.getSettings().getAndLoadSprite(ship.hullSpec.spriteName.replace(".png", "") + "_glow.png")
    var systemGlow2: SpriteAPI = Global.getSettings().getAndLoadSprite(ship.hullSpec.spriteName.replace(".png", "") + "_glow_secondary.png")
    var fader = FaderUtil(1f, 2f, 1.5f, false, false)
    var lastJitterLocations = ArrayList<Vector2f>()
    var lastSecondJitterLocations = ArrayList<Vector2f>()

    companion object {
        var shader = 0
    }


    init {
        shader = ShaderLib.loadShader(
            Global.getSettings().loadText("data/shaders/baseVertex.shader"),
            Global.getSettings().loadText("data/shaders/rat_primordialSeaShader.shader"))
        if (shader != 0) {
            GL20.glUseProgram(shader)

            GL20.glUniform1i(GL20.glGetUniformLocation(shader, "tex"), 0)

            GL20.glUseProgram(0)
        } else {
            var test = ""
        }
    }


    override fun init(entity: CombatEntityAPI?) {

    }

    override fun cleanup() {

    }

    override fun isExpired(): Boolean {
        return false
    }

    override fun advance(amount: Float) {
        fader.advance(amount)
        if (fader.brightness >= 1)
        {
            fader.fadeOut()
        }
        else if (fader.brightness <= 0)
        {
            fader.fadeIn()
        }
    }

    override fun getActiveLayers(): EnumSet<CombatEngineLayers> {
        return EnumSet.of(CombatEngineLayers.BELOW_PLANETS, CombatEngineLayers.ABOVE_SHIPS_LAYER, CombatEngineLayers.UNDER_SHIPS_LAYER, CombatEngineLayers.JUST_BELOW_WIDGETS)
    }

    override fun getRenderRadius(): Float {
        return 10000000f
    }

    override fun render(layer: CombatEngineLayers, viewport: ViewportAPI) {
        var width = viewport.visibleWidth
        var height = viewport.visibleHeight

        var x = viewport.llx
        var y = viewport.lly

        var color = Color(100, 0, 255)

        var radius = activator.getCurrentRange()
        var segments = 100

        if (layer == CombatEngineLayers.JUST_BELOW_WIDGETS) {
            if (ShaderLib.getScreenTexture() != 0) {



                var screenLoc = ShaderLib.transformWorldToScreen(ship.location)
                var screenLocUV = ShaderLib.transformScreenToUV(screenLoc)

                var secPoint = MathUtils.getPointOnCircumference(ship.location, radius-2, 0f)
                var secPointScreen = ShaderLib.transformWorldToScreen(secPoint)
                var secPointsUV = ShaderLib.transformScreenToUV(secPointScreen)

                var pointDist = MathUtils.getDistance(screenLocUV, secPointsUV)

                var noiseMult = 1f
                if (Global.getCombatEngine().isSimulation) noiseMult = 0.5f;

                ShaderLib.beginDraw(shader);
                GL20.glUniform1f(GL20.glGetUniformLocation(shader, "intensity"), 1f)

                GL20.glUniform2f(GL20.glGetUniformLocation(shader, "screenLocUV"), screenLocUV.x, screenLocUV.y)
                GL20.glUniform1f(GL20.glGetUniformLocation(shader, "range"), pointDist)

                GL13.glActiveTexture(GL13.GL_TEXTURE0 + 0);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, ShaderLib.getScreenTexture());

                GL11.glDisable(GL11.GL_BLEND);
                ShaderLib.screenDraw(ShaderLib.getScreenTexture(), GL13.GL_TEXTURE0 + 0)
                ShaderLib.exitDraw()
            }
        }

        if (layer == CombatEngineLayers.ABOVE_SHIPS_LAYER) {
            systemGlow2.setNormalBlend()
            systemGlow2.alphaMult = (0.8f + (0.2f * fader.brightness))
            systemGlow2.angle = ship.facing - 90
            systemGlow2.renderAtCenter(ship.location.x, ship.location.y)

            systemGlow2.setNormalBlend()
            systemGlow2.alphaMult = ((0.5f * fader.brightness))
            systemGlow2.angle = ship.facing - 90
            systemGlow2.renderAtCenter(ship.location.x, ship.location.y)
        }

        startStencil(ship!!, radius, segments)

        if (layer == CombatEngineLayers.ABOVE_SHIPS_LAYER) {
            systemGlow.setAdditiveBlend()
            systemGlow.alphaMult = (0.8f + (0.2f * fader.brightness))
            systemGlow.angle = ship.facing - 90
            systemGlow.renderAtCenter(ship.location.x, ship.location.y)

            systemGlow.setAdditiveBlend()
            systemGlow.alphaMult = ((0.5f * fader.brightness))
            systemGlow.angle = ship.facing - 90
            systemGlow.renderAtCenter(ship.location.x, ship.location.y)

            doJitter(ship, systemGlow, 0.5f, lastJitterLocations, 5, 2f)
            doJitter(ship, systemGlow, 0.3f, lastSecondJitterLocations, 5, 12f)
        }

        if (layer == CombatEngineLayers.BELOW_PLANETS) {

            sprite.setSize(width, height)
            sprite.color = color
            sprite.alphaMult = 1f
            sprite.render(x, y)

            wormhole.setSize(width * 1.3f, width *  1.3f)
            wormhole.setAdditiveBlend()
            wormhole.alphaMult = 0.3f
            if (!Global.getCombatEngine().isPaused) wormhole.angle += 0.075f
            wormhole.color = color
            wormhole.renderAtCenter(x + width / 2, y + height / 2)

            wormhole2.setSize(width * 1.35f, width *  1.35f)
            wormhole2.setAdditiveBlend()
            wormhole2.alphaMult = 0.2f
            if (!Global.getCombatEngine().isPaused) wormhole2.angle += 0.05f
            wormhole2.color = Color(50, 0, 255)
            wormhole2.renderAtCenter(x + width / 2, y + height / 2)
        }

        if (layer == CombatEngineLayers.UNDER_SHIPS_LAYER) {
            renderShips()
        }

        if (layer == CombatEngineLayers.ABOVE_SHIPS_LAYER) {
            renderApparationGlow()
        }

        endStencil()

        if (layer == CombatEngineLayers.BELOW_PLANETS) {
            renderBorder(ship!!, radius, color, segments)
        }



    }

    fun renderShips() {

        var range = activator.getCurrentRange()
        for (apparation in apparations ) {

            if (!apparation.isAlive) continue

            var inRange = MathUtils.getDistance(apparation.location, ship.location) <= range - apparation.collisionRadius

            if (inRange) {
                apparation.spriteAPI.color = Color(255, 255, 255, 255)
            }
            else {
                apparation.spriteAPI.color = Color(255, 255, 255, 255)
                apparation.spriteAPI.renderAtCenter(apparation.location.x, apparation.location.y)
                apparation.spriteAPI.color = Color(0, 0 ,0 ,0)
            }





            /*for (weapon in apparation.allWeapons) {
                weapon.sprite?.alphaMult = 1f
                weapon.sprite?.renderAtCenter(weapon.location.x, weapon.location.y)
                weapon.sprite?.alphaMult = 0f
            }*/
        }
    }

    fun renderApparationGlow() {

        var range = activator.getCurrentRange()

        for (apparation in apparations ) {

            if (!apparation.isAlive) continue

            var inRange = MathUtils.getDistance(apparation.location, ship.location) <= range - apparation.collisionRadius

            apparation.spriteAPI.color = Color(255, 255, 255, 255)

            var apparationGlow = apparation.customData.get("rat_ship_glow") as SpriteAPI?
            if (apparationGlow == null) {
                apparationGlow = Global.getSettings().getAndLoadSprite(apparation.hullSpec.spriteName.replace(".png", "") + "_glow.png")
                apparation.setCustomData("rat_ship_glow", apparationGlow)
            }

            var lastLocation = apparation.customData.get("rat_apparation_glow_locations") as ArrayList<Vector2f>?
            if (lastLocation == null) {
                lastLocation = ArrayList<Vector2f>()
                apparation.setCustomData("rat_apparation_glow_locations", lastLocation)
            }

            apparationGlow.setAdditiveBlend()
            apparationGlow.alphaMult = (0.8f + (0.2f * fader.brightness))
            apparationGlow.angle = apparation.facing - 90
            apparationGlow.renderAtCenter(apparation.location.x, apparation.location.y)

            apparationGlow.setAdditiveBlend()
            apparationGlow.alphaMult = ((0.5f * fader.brightness))
            apparationGlow.angle = apparation.facing - 90
            apparationGlow.renderAtCenter(apparation.location.x, apparation.location.y)

            doJitter(apparation, apparationGlow, 0.5f, lastLocation, 5, 3f)

            if (!inRange) {
                apparation.spriteAPI.color = Color(0, 0, 0, 0)
            }
        }
    }

    fun doJitter(ship: ShipAPI, sprite: SpriteAPI, level: Float, lastLocations: ArrayList<Vector2f>, jitterCount: Int, jitterMaxRange: Float) {

        var paused = Global.getCombatEngine().isPaused
        var jitterAlpha = 0.2f


        if (!paused) {
            lastLocations.clear()
        }

        for (i in 0 until jitterCount) {

            var jitterLoc = Vector2f()

            if (!paused) {
                var x = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)
                var y = MathUtils.getRandomNumberInRange(-jitterMaxRange, jitterMaxRange)

                jitterLoc = Vector2f(x, y)
                lastLocations.add(jitterLoc)
            }
            else {
                jitterLoc = lastLocations.getOrElse(i) {
                    Vector2f()
                }
            }

            sprite.setAdditiveBlend()
            sprite.alphaMult = level * jitterAlpha
            sprite.renderAtCenter(ship.location.x + jitterLoc.x, ship.location.y + jitterLoc.y)
        }
    }

    fun startStencil(ship: ShipAPI, radius: Float, circlePoints: Int) {

        GL11.glClearStencil(0);
        GL11.glStencilMask(0xff);
        //set everything to 0
        GL11.glClear(GL11.GL_STENCIL_BUFFER_BIT);

        //disable drawing colour, enable stencil testing
        GL11.glColorMask(false, false, false, false); //disable colour
        GL11.glEnable(GL11.GL_STENCIL_TEST); //enable stencil

        // ... here you render the part of the scene you want masked, this may be a simple triangle or square, or for example a monitor on a computer in your spaceship ...
        //begin masking
        //put 1s where I want to draw
        GL11.glStencilFunc(GL11.GL_ALWAYS, 1, 0xff); // Do not test the current value in the stencil buffer, always accept any value on there for drawing
        GL11.glStencilMask(0xff);
        GL11.glStencilOp(GL11.GL_REPLACE, GL11.GL_REPLACE, GL11.GL_REPLACE); // Make every test succeed

        // <draw a quad that dictates you want the boundaries of the panel to be>

        GL11.glBegin(GL11.GL_POLYGON) // Middle circle

        val x = ship.location.x
        val y = ship.location.y

        for (i in 0..circlePoints) {

            val angle: Double = (2 * Math.PI * i / circlePoints)
            val vertX: Double = Math.cos(angle) * (radius)
            val vertY: Double = Math.sin(angle) * (radius)
            GL11.glVertex2d(x + vertX, y + vertY)
        }

        GL11.glEnd()

        //GL11.glRectf(x, y, x + width, y + height)

        GL11.glStencilOp(GL11.GL_KEEP, GL11.GL_KEEP, GL11.GL_KEEP); // Make sure you will no longer (over)write stencil values, even if any test succeeds
        GL11.glColorMask(true, true, true, true); // Make sure we draw on the backbuffer again.

        GL11.glStencilFunc(GL11.GL_EQUAL, 1, 0xFF); // Now we will only draw pixels where the corresponding stencil buffer value equals 1
        //Ref 0 causes the content to not display in the specified area, 1 causes the content to only display in that area.

        // <draw the lines>

    }

    fun endStencil() {
        GL11.glDisable(GL11.GL_STENCIL_TEST);
    }

    fun renderBorder(ship: ShipAPI, radius: Float, color: Color, circlePoints: Int) {
        var c = color
        GL11.glPushMatrix()

        GL11.glTranslatef(0f, 0f, 0f)
        GL11.glRotatef(0f, 0f, 0f, 1f)

        GL11.glDisable(GL11.GL_TEXTURE_2D)


        GL11.glEnable(GL11.GL_BLEND)
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)


        GL11.glColor4f(c.red / 255f,
            c.green / 255f,
            c.blue / 255f,
            c.alpha / 255f * (1f))

        GL11.glEnable(GL11.GL_LINE_SMOOTH)
        GL11.glBegin(GL11.GL_LINE_STRIP)

        val x = ship.location.x
        val y = ship.location.y


        for (i in 0..circlePoints) {
            val angle: Double = (2 * Math.PI * i / circlePoints)
            val vertX: Double = Math.cos(angle) * (radius)
            val vertY: Double = Math.sin(angle) * (radius)
            GL11.glVertex2d(x + vertX, y + vertY)
        }

        GL11.glEnd()
        GL11.glPopMatrix()
    }
}