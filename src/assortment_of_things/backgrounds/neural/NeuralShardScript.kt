package assortment_of_things.backgrounds.neural

import assortment_of_things.combat.TemporarySlowdown
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse
import org.lwjgl.util.vector.Vector2f
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
            var shipsWithoutOfficer = Global.getCombatEngine().ships.filter { it.owner == 0 && !it.isAlly && !it.isFighter && !it.isAutomated() && (it.captain == null || it.captain.isDefault) }

            for (ship in shipsWithoutOfficer) {

                if (ship.parentStation != null) {
                    if (ship.parentStation.isAutomated()) continue
                }

                addShardOfficer(ship)
            }

            //Replace player with shard if they swapped
            for (ship in Global.getCombatEngine().ships) {
                if (ship.captain == Global.getSector().playerPerson) {
                    if (ship != Global.getCombatEngine().playerShip) {
                        addShardOfficer(ship)
                    }
                }
            }

        }

        //Switch to closest ship on ship death.
        var playership = Global.getCombatEngine().playerShip
        if (playership != null && !playership.isAlive) {
            var others = Global.getCombatEngine().ships.filter { it.owner == 0 && it.captain != null && it.captain.hasTag("rat_neuro_shard") }
            var closest = others.sortedBy { MathUtils.getDistance(playership.location, it.location) }.firstOrNull()

            if (closest != null) {
                switchShip(playership, closest)
            }

        }

        var person = Global.getSector().playerPerson.setPersonality(Personalities.AGGRESSIVE)

        if (playership != null) {

            var mouseLoc = playership.mouseTarget
            var shipsNearMouse = Global.getCombatEngine().shipGrid.getCheckIterator(mouseLoc, 2000f, 2000f).iterator()

            var any = false
            var closest = 10000000f
            for (shipObj in shipsNearMouse) {
                var ship = shipObj as ShipAPI

                if (ship.owner != 0) continue
                if (ship.isFighter) continue
                if (ship == playership) continue
                if (ship.captain?.hasTag("rat_neuro_shard") != true) continue

                var distance = MathUtils.getDistance(playership.mouseTarget, ship.shieldCenterEvenIfNoShield)
                if (distance <= ship.shieldRadiusEvenIfNoShield * 1.1f) {
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



    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {
        super.processInputPreCoreControls(amount, events)

        var playership = Global.getCombatEngine().playerShip

        for (event in events!!) {
            if (!event.isConsumed) {
                if (RATSettings.backgroundsAbilityKeybind == 0 && event.isMouseDownEvent && event.isRMBDownEvent) {
                    if (selectedShip != null && playership != null) {

                        switchShip(playership, selectedShip!!)

                        event.consume()
                        break
                    }
                }
                else if (event.isKeyDownEvent && event.eventValue == RATSettings.backgroundsAbilityKeybind) {
                    if (selectedShip != null && playership != null) {

                        switchShip(playership, selectedShip!!)

                        event.consume()
                        break
                    }
                }
            }
        }
    }

    fun switchShip(current: ShipAPI, new: ShipAPI) {

        saveControlState(current)

        Global.getSoundPlayer().playUISound("ui_neural_transfer_complete", 1f, 1f)
        Global.getCombatEngine().combatUI.reFanOutShipInfo()

        Global.getCombatEngine().setPlayerShipExternal(new)
        new!!.captain = Global.getSector().playerPerson


        var shard = current.customData.get("rat_neuro_shard") as PersonAPI?
        if (shard != null) {
            current.captain = shard
        }
        else {
            addShardOfficer(current)
        }

        restoreControlState(new)

        Global.getCombatEngine().addPlugin(TemporarySlowdown(3f, 0.25f))

        selectedShip = null
    }


    fun addShardOfficer(ship: ShipAPI) {

        var person = Global.getFactory().createPerson()
        person.setPersonality(Personalities.AGGRESSIVE)

        var player = Global.getSector().playerPerson

        person.name = player.name
        person.gender = player.gender
        person.portraitSprite = player.portraitSprite

        var level = MathUtils.getRandomNumberInRange(2, 3)
        person.stats.level = level

        var skills = player.stats.skillsCopy.filter { it.skill.isCombatOfficerSkill && !it.skill.hasTag("npc_only") && it.level >= 0.5f }.toMutableList()
        for (i in 0 until  level) {
            var skill = skills.randomOrNull() ?: continue

            skills.remove(skill)
            person.stats.setSkillLevel(skill.skill.id, skill.level)
        }

        person.addTag("rat_neuro_shard")

        ship.captain = person
        ship.setCustomData("rat_neuro_shard", person)
    }

    fun saveControlState(ship: ShipAPI) {

        var controllState = ShipControllState()
        for (group in ship.weaponGroupsCopy) {
            controllState!!.autofiring.put(group, group.isAutofiring)
        }
        controllState!!.selected = ship.selectedGroupAPI

        ship.setCustomData("rat_neural_shard_state", controllState)
    }

    fun restoreControlState(ship: ShipAPI) {
        var controllState = ship.customData.get("rat_neural_shard_state") as ShipControllState? ?: return

        for (group in ship.weaponGroupsCopy) {
            var auto: Boolean? = controllState!!.autofiring.get(group)
            if (auto == null) auto = false
            if (auto) {
                group.toggleOn()
            } else {
                group.toggleOff()
            }
        }
        val index = ship.weaponGroupsCopy.indexOf(controllState!!.selected)
        if (index > 0) {
            ship.giveCommand(ShipCommand.SELECT_GROUP, null, index)
        }
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

    class ShipControllState {
        var autofiring: MutableMap<WeaponGroupAPI, Boolean> = LinkedHashMap()
        var selected: WeaponGroupAPI? = null
    }

}

