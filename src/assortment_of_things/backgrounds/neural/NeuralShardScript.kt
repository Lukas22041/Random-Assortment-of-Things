package assortment_of_things.backgrounds.neural

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseCombatLayeredRenderingPlugin
import com.fs.starfarer.api.combat.BaseEveryFrameCombatPlugin
import com.fs.starfarer.api.combat.CombatEngineLayers
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.isAutomated
import java.awt.Color
import java.util.*

class NeuralShardScript : BaseEveryFrameCombatPlugin() {

    var sprite = Global.getSettings().getAndLoadSprite("graphics/ui/rat_ship_marker.png")

    var selectedShip: ShipAPI? = null
    var lastSelectedShip: ShipAPI? = null

    var interval = IntervalUtil(0.1f, 0.1f)
    var rotation = MathUtils.getRandomNumberInRange(0f, 90f)
    var alpha = 0f

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        interval.advance(amount)

        if (!Global.getCombatEngine().isPaused) {
            rotation -= 10f * amount
            if (rotation >= 360f) {
                rotation = 0f
            }
        }


        if (interval.intervalElapsed()) {
            var shipsWithoutOfficer = Global.getCombatEngine().ships.filter { it.owner == 0 && !it.isFighter && !it.isAutomated() && (it.captain == null || it.captain.isDefault) }

            for (ship in shipsWithoutOfficer) {
                addShardOfficer(ship)
            }
        }

        var person = Global.getSector().playerPerson.setPersonality(Personalities.AGGRESSIVE)

        var playership = Global.getCombatEngine().playerShip
        if (playership != null && playership.mouseTarget != null) {
            var shipsNearMouse = Global.getCombatEngine().shipGrid.getCheckIterator(playership.mouseTarget, 2000f, 2000f).iterator()

            var any = false
            var closest = 10000000f
            for (shipObj in shipsNearMouse) {
                var ship = shipObj as ShipAPI

                if (ship.owner != 0) continue
                if (ship.owner != 0) continue

                var distance = MathUtils.getDistance(playership.mouseTarget, ship.shieldCenterEvenIfNoShield)
                if (distance <= ship.shieldRadiusEvenIfNoShield * 0.95f) {
                    if (distance <= closest) {
                        closest = distance
                        selectedShip = ship
                        lastSelectedShip = ship
                        any = true
                    }
                }
            }

            if (!any) {
                selectedShip = null
            }
        }

        if (selectedShip != null) {
            alpha += 4f * amount
        }
        else {
            alpha -= 4f * amount
        }
        alpha = alpha.coerceIn(0f, 1f)
    }


    fun addShardOfficer(ship: ShipAPI) {

        var person = Global.getFactory().createPerson()
        person.setPersonality(Personalities.AGGRESSIVE)

        var player = Global.getSector().playerPerson

        person.name = player.name
        person.gender = player.gender
        person.portraitSprite = player.portraitSprite

        var skills = player.stats.skillsCopy.filter { it.skill.isCombatOfficerSkill && !it.skill.hasTag("npc_only") }
        var count = MathUtils.getRandomNumberInRange(2, 3)
        for (i in 0 until count) {
            var skill = skills.randomOrNull() ?: continue

            person.stats.setSkillLevel(skill.skill.id, skill.level)
        }

        person.addTag("rat_player_shard")

        ship.captain = person
        ship.resetOriginalOwner()
    }

    override fun renderInWorldCoords(viewport: ViewportAPI?) {
        super.renderInWorldCoords(viewport)

        if (lastSelectedShip != null) {
            sprite.color = Misc.getPositiveHighlightColor()
            sprite.color = Color(255, 150, 0)
            sprite.alphaMult = alpha

            var degrees = rotation
            for (i in 0 until 3) {

                var maxRadius = 600f
                var radius = lastSelectedShip!!.collisionRadius

                var level = radius / maxRadius
                level = level.coerceIn(0.15f, 1f)

                var loc = MathUtils.getPointOnCircumference(lastSelectedShip!!.shieldCenterEvenIfNoShield, lastSelectedShip!!.shieldRadiusEvenIfNoShield * 0.95f, degrees)

                //sprite.setSize(35f, 96f)
                sprite.setSize(140f * level, 383f * level)
                sprite.angle = degrees
                sprite.renderAtCenter(loc.x, loc.y)

                degrees += 120f
            }
        }
    }

}
