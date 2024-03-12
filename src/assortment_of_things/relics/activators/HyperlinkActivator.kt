package assortment_of_things.relics.activators

import assortment_of_things.combat.TemporarySlowdown
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.WeaponGroupAPI
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Mouse
import org.magiclib.subsystems.MagicSubsystem
import org.magiclib.subsystems.MagicSubsystemsManager
import java.awt.Color

class HyperlinkActivator(ship: ShipAPI, var other: ShipAPI) : MagicSubsystem(ship) {

    var color = Color(255, 125, 0)
    var id = "rat_hyperlink"

    var interval = IntervalUtil(0.1f, 0.1f)

    override fun getBaseActiveDuration(): Float {
       return  0f
    }

    override fun getBaseCooldownDuration(): Float {
        return 1f
    }

    override fun shouldActivateAI(amount: Float): Boolean {
        return false
    }

    override fun getBaseInDuration(): Float {
        return 0f
    }

    override fun getOutDuration(): Float {
        return 0f
    }


    override fun advance(amount: Float, isPaused: Boolean) {

    }

    override fun onFinished() {
        super.onFinished()

    }

    override fun canActivate(): Boolean {
        return other.isAlive && !other.isHulk
    }

    override fun onActivate() {
        super.onActivate()

        var engine = Global.getCombatEngine()
        var playerShip = engine.playerShip

        var otherActivator = MagicSubsystemsManager.getSubsystemsForShipCopy(other)?.find { it::class.java == HyperlinkActivator::class.java }
        if (otherActivator != null) {
            otherActivator.setState(State.COOLDOWN)

            var cooldown = when (other.hullSize) {
                ShipAPI.HullSize.FRIGATE -> 3f
                ShipAPI.HullSize.DESTROYER -> 5f
                ShipAPI.HullSize.CRUISER -> 10f
                ShipAPI.HullSize.CAPITAL_SHIP -> 15f
                else -> 5f
            }

            otherActivator.setCooldownDuration(cooldown, false)
        }

        Global.getSoundPlayer().playUISound("ui_neural_transfer_complete", 1f, 1f)
        engine.combatUI.reFanOutShipInfo()

        Mouse.setCursorPosition(Global.getSettings().screenWidthPixels.toInt() / 2,
            Global.getSettings().screenHeightPixels.toInt() / 2)

        saveControlState(playerShip)

        val playerShipAI = playerShip.shipAI

        val prevTargetAI = other.shipAI
        engine.setPlayerShipExternal(other)

        if (playerShipAI != null) {
            playerShip.shipAI = playerShipAI
        }

        val autopilot = engine.combatUI.isAutopilotOn

       if (autopilot) {
            val manager = engine.getFleetManager(FleetSide.PLAYER)
            val member = manager.getDeployedFleetMember(other)
            other.shipAI = Global.getSettings().pickShipAIPlugin(member?.member, other)
        }

        restoreControlState(other)

        Global.getCombatEngine().addPlugin(TemporarySlowdown(3f, 0.25f))
    }



    override fun getDisplayText(): String {
        return "Hyperlink"
    }

    override fun getHUDColor(): Color {
        if (other.isAlive && !other.isHulk) return color
        return Misc.getGrayColor()
    }

    class ShipControllState {
        var autofiring: MutableMap<WeaponGroupAPI, Boolean> = LinkedHashMap()
        var selected: WeaponGroupAPI? = null
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
}