package assortment_of_things.backgrounds.zero_day

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
import org.magiclib.kotlin.setAlpha
import org.magiclib.util.MagicUI
import java.awt.Color
import java.util.*

class ZeroDayScript : BaseEveryFrameCombatPlugin() {

    var sprite = Global.getSettings().getAndLoadSprite("graphics/ui/rat_ship_marker_hostile.png")

    var selectedShip: ShipAPI? = null
    var lastSelectedShip: ShipAPI? = null

    var interval = IntervalUtil(0.1f, 0.1f)
    var rotation = MathUtils.getRandomNumberInRange(0f, 90f)
    var alpha = 0f

    var maxDp = 35f

    var previous: ShipAPI? = null
    var controlled: ShipAPI? = null

    var duration = 0f
    var relativeMaxDuration = 1f
    var maxDuration = 20f
    var cooldown = 0f
    var maxCooldown = 20f

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {
        interval.advance(amount)

        var playership = Global.getCombatEngine().playerShip

        var paused = Global.getCombatEngine().isPaused
        if (cooldown > 0f) {
            if (!paused) cooldown -= 1 * amount

            var level = 1-(cooldown/maxCooldown)
            level = level.coerceIn(0f, 1f)
            var color = Misc.interpolateColor(Misc.getNegativeHighlightColor(), Misc.getPositiveHighlightColor(), level)

            MagicUI.drawInterfaceStatusBar(playership, level , color, color, 1f, "Hack", cooldown.toInt())

            return
        }

        if (!paused) {
            rotation -= 10f * amount
            if (rotation >= 360f) {
                rotation = 0f
            }
        }


        var person = Global.getSector().playerPerson.setPersonality(Personalities.AGGRESSIVE)

        if (controlled != null) {
            if (!paused) duration -= 1f * amount

            if (controlled!!.isRetreating) {
                controlled!!.setRetreating(false, false)
            }

            var level = duration/relativeMaxDuration
            level = level.coerceIn(0f, 1f)
            var color = Misc.interpolateColor(Misc.getNegativeHighlightColor(), Misc.getPositiveHighlightColor(), level)

            MagicUI.drawInterfaceStatusBar(controlled,duration/relativeMaxDuration, color, color, 1f, "Hack", duration.toInt())

            if (duration <= 0f || !controlled!!.isAlive) {
                switchBack()
            }
        }
        else {
            if (!Global.getCombatEngine().combatUI.isShowingCommandUI) {
                MagicUI.drawInterfaceStatusBar(playership, 1f , Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor(), 1f, "Hack", 0)
            }
        }

        if (playership != null && controlled == null) {

            var mouseLoc = playership.mouseTarget
            var shipsNearMouse = Global.getCombatEngine().shipGrid.getCheckIterator(mouseLoc, 2000f, 2000f).iterator()

            var any = false
            var closest = 10000000f
            for (shipObj in shipsNearMouse) {
                var ship = shipObj as ShipAPI

                if (ship.owner != 1) continue
                if (ship.isFighter) continue
                if ((ship.fleetMember?.deploymentPointsCost ?: ship.hullSpec.suppliesToRecover) > 35) continue

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

    var doubleTimer = 0f
    var doubleTimerMax = 1f

    override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {
        super.processInputPreCoreControls(amount, events)

        if (cooldown > 0f) return

        doubleTimer -= 1 * amount
        doubleTimer = doubleTimer.coerceIn(0f, doubleTimerMax)

        var playership = Global.getCombatEngine().playerShip

        for (event in events!!) {
            if (!event.isConsumed) {
                var triggered = false
                if (RATSettings.backgroundsAbilityKeybind == 0 && event.isMouseDownEvent && event.isRMBDownEvent) {
                    triggered = true
                }
                else if (event.isKeyDownEvent && event.eventValue == RATSettings.backgroundsAbilityKeybind) {
                    triggered = true
                }

                if (triggered && event.isDoubleClick && controlled != null) {
                    switchBack()
                    event.consume()
                    continue
                }

                if (triggered && controlled != null) {

                    if (doubleTimer > 0) {
                        switchBack()
                        event.consume()
                        continue
                    }

                    doubleTimer = doubleTimerMax
                }

                if (triggered && selectedShip != null && playership != null) {

                    previous = playership
                    controlled = selectedShip
                    controlled!!.owner = 0
                    controlled!!.originalOwner = 0
                    selectedShip = null

                    var dp = (controlled!!.fleetMember?.deploymentPointsCost ?: controlled!!.hullSpec.suppliesToRecover)

                    var level = (dp - 0f) / (maxDp - 0f)
                    level = 1 - level

                    duration = maxDuration * level
                    duration = duration.coerceIn(10f, maxDuration)
                    relativeMaxDuration = duration

                    switchShip(playership, controlled!!)

                    event.consume()
                    break
                }


            }
        }



    }

    fun switchShip(current: ShipAPI, new: ShipAPI) {

        saveControlState(current)

        Global.getSoundPlayer().playUISound("ui_neural_transfer_complete", 1f, 1f)
        Global.getCombatEngine().combatUI.reFanOutShipInfo()

        Global.getCombatEngine().setPlayerShipExternal(new)

        restoreControlState(new)

        Global.getCombatEngine().addPlugin(TemporarySlowdown(3f, 0.5f))

        selectedShip = null
    }

    fun switchBack() {

        if (!controlled!!.isHulk) {
            controlled!!.owner = 1
            controlled!!.originalOwner = 1
        }

        switchShip(controlled!!, previous!!)

        cooldown = maxCooldown

        controlled = null
        previous = null
    }



    fun saveControlState(ship: ShipAPI) {

        var controllState = ShipControllState()
        for (group in ship.weaponGroupsCopy) {
            controllState!!.autofiring.put(group, group.isAutofiring)
        }
        controllState!!.selected = ship.selectedGroupAPI

        ship.setCustomData("rat_neuro_state", controllState)
    }

    fun restoreControlState(ship: ShipAPI) {
        var controllState = ship.customData.get("rat_neuro_state") as ShipControllState? ?: return

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
            sprite.color = Misc.getNegativeHighlightColor()
            sprite.alphaMult = alpha

            var degrees = rotation
            for (i in 0 until 5) {

                var maxRadius = 600f
                var radius = lastSelectedShip!!.collisionRadius

                var level = radius / maxRadius
                level = level.coerceIn(0.15f, 1f)

                var loc = MathUtils.getPointOnCircumference(lastSelectedShip!!.shieldCenterEvenIfNoShield, lastSelectedShip!!.shieldRadiusEvenIfNoShield * 0.95f, degrees)

                //sprite.setSize(35f, 96f)
                sprite.setSize(140f * level, 383f * level)
                sprite.angle = degrees
                sprite.renderAtCenter(loc.x, loc.y)

                degrees += 72
            }
        }
    }

    class ShipControllState {
        var autofiring: MutableMap<WeaponGroupAPI, Boolean> = LinkedHashMap()
        var selected: WeaponGroupAPI? = null
    }

}

