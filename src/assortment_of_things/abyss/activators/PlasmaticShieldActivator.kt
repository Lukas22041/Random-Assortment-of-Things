package assortment_of_things.abyss.activators

import assortment_of_things.combat.PidController
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.DamagingProjectileAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipCommand
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.mission.FleetSide
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.combat.CombatUtils
import org.magiclib.kotlin.setAlpha
import org.magiclib.subsystems.MagicSubsystem
import java.awt.Color

class PlasmaticShieldActivator(ship: ShipAPI?) : MagicSubsystem(ship) {

    var id = "rat_plasmatic_shield"

    val jitterColor = Color(100, 0, 250, 150)
    val jitterUnderColor = Color(100, 0, 250, 200)

    val interval = IntervalUtil(0.25f, 0.75f)

    var fighter: ShipAPI? = null

    override fun getBaseInDuration(): Float {
        return 0.25f
    }

    override fun getBaseActiveDuration(): Float {
        return 10f
    }

    override fun getBaseOutDuration(): Float {
        return 1f
    }

    override fun getBaseCooldownDuration(): Float {
        return 15f
    }

    override fun advance(amount: Float, isPaused: Boolean) {
        super.advance(amount, isPaused)

        if (!ship.isAlive || ship.isHulk)
        {
            if (fighter != null)
            {
                fighter!!.hitpoints = 0f
            }
            return
        }

        if (fighter != null)
        {
            fighter!!.location.set(ship.location)
            fighter!!.spriteAPI.alphaMult = 0f
            fighter!!.spriteAPI.setSize(0f, 0f)


            if (state == State.ACTIVE)
            {
                if (!fighter!!.shield.isOn) {
                    fighter!!.shield.toggleOn()
                }
            }

            if (state == State.OUT)
            {
                if (fighter!!.shield.isOn) {
                    fighter!!.shield.toggleOff()
                }
            }

            if (fighter!!.shield != null)
            {
                var radius = ship.collisionRadius + 10f
                fighter!!.collisionRadius = radius

                if (ship.shield != null)
                {
                    fighter!!.shield.radius = ship!!.shield.radius + 5f

                }
                else
                {
                    fighter!!.shield.radius = radius + 2.5f
                }
                fighter!!.shield.ringColor = jitterUnderColor
                fighter!!.shield.innerColor = jitterColor.setAlpha(50)
            }
        }

        interval.advance(amount)

        ship.engineController.fadeToOtherColor(this, jitterColor, jitterColor.setAlpha(10), effectLevel, 0.5f)
        ship.engineController.extendFlame(this, -0.25f, -0.25f, -0.25f)
    }


    override fun onFinished() {
        super.onFinished()

        if (fighter != null)
        {
            fighter!!.hitpoints = 0f
        }
    }

    override fun onActivate() {
        super.onActivate()

        Global.getCombatEngine().getFleetManager(ship.owner).isSuppressDeploymentMessages = true

         fighter = CombatUtils.spawnShipOrWingDirectly("rat_shield_object_wing", FleetMemberType.FIGHTER_WING, FleetSide.PLAYER, 0.7f, ship.location, ship.facing)

        //activeWings.put(fighter, controller)

        fighter!!.shipAI = null
        fighter!!.giveCommand(ShipCommand.SELECT_GROUP, null, 99);
        fighter!!.spriteAPI.alphaMult = 0f

        fighter!!.addListener(object : AdvanceableListener {
            override fun advance(amount: Float) {
               if (ship == null || !Global.getCombatEngine().ships.contains(ship)
               )
               {
                   if (fighter != null)
                   {
                       fighter!!.hitpoints = 0f
                   }
               }
            }
        })


        Global.getCombatEngine().getFleetManager(ship.owner).isSuppressDeploymentMessages = false

    }

    override fun shouldActivateAI(amount: Float): Boolean {


        interval.advance(amount)

        if (interval.intervalElapsed())
        {
            if (ship.shipTarget != null)
            {
                return true
            }

            var iter = Global.getCombatEngine().shipGrid.getCheckIterator(ship.location, 800f, 800f)

            for (it in iter)
            {
                if (it is ShipAPI)
                {
                    if (it.isAlive && !it.isHulk && it.owner != ship.owner && !it.isPiece)
                    {
                        return true
                    }
                }
            }
        }
        return false

    }

    override fun getDisplayText(): String {
        return ""
    }
}