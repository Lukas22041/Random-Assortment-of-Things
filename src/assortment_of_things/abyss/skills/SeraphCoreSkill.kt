package assortment_of_things.abyss.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import com.fs.starfarer.api.combat.listeners.DamageTakenModifier
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f

class SeraphCoreSkill : RATBaseShipSkill() {

    var modID = "rat_core_seraph"

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?,  skill: SkillSpecAPI?, info: TooltipMakerAPI?,  width: Float) {
        info!!.addSpacer(2f)

        info.addPara("The ship recovers a small amount of hull whenever it deals damage to opponents hull or armor, up to 50%% of its maximum hitpoints per deployment.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "recovers", "hull or armor", "50%")

        info.addSpacer(10f)

        info.addPara("The ship deals up to 25%% more non-missile damage based on how much damage it has dealt towards hull or armor so far. The maximum damage is 75000/125000/175000 based on hullsize. " +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "25%", "non-missile", "hull or armor", "75000", "125000", "175000")

        info.addSpacer(10f)

        info.addPara("If the maximum is reached, the ship gains the zero-flux speed boost for the remainder of the deployment.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "zero-flux")

        info.addSpacer(10f)

        info.addPara("- This skill has no effect on capital-class hulls.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

        info.addSpacer(2f)
    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        var ship = stats!!.entity
        if (ship is ShipAPI && ship.hullSize != ShipAPI.HullSize.CAPITAL_SHIP) {
            if (!ship.hasListenerOfClass(SeraphCoreSkillListener::class.java)) {
                ship.addListener(SeraphCoreSkillListener(ship))
            }
        }

        /*if (stats.fleetMember != null) {
            if (stats.fleetMember.isCapital) {

                if (stats.fleetMember.fleetData.fleet == Global.getSector().playerFleet) {
                    Global.getSector().playerFleet.cargo.addCommodity(stats.fleetMember.captain.aiCoreId, 1f)
                }

                stats.fleetMember.captain = null
            }
        }*/
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }

    class SeraphCoreSkillListener(var ship: ShipAPI) : AdvanceableListener, DamageDealtModifier {

        var maxDamage = 150000f
        var damageDealt = 0f

        var recoveredHealth = 0f

        var modID = "rat_core_seraph"


        init {
            maxDamage = when(ship.variant.hullSize) {
                ShipAPI.HullSize.FRIGATE -> 75000f
                ShipAPI.HullSize.DESTROYER -> 125000f
                ShipAPI.HullSize.CRUISER -> 175000f
                else -> 1000000f
            }
        }

        override fun advance(amount: Float) {

            var path = "graphics/icons/hullsys/high_energy_focus.png"

            if (ship == Global.getCombatEngine().playerShip) {
                Global.getCombatEngine().maintainStatusForPlayerShip("rat_seraph_core1",
                    path,
                    "Seraphim (Damage)",
                    "${(damageDealt / maxDamage * 100f).toInt()}%",
                    false)

                Global.getCombatEngine().maintainStatusForPlayerShip("rat_seraph_core2",
                    path,
                    "Seraphim (Health)",
                    "${(recoveredHealth / (ship.maxHitpoints * 0.5f) * 100f).toInt()}%",
                    false)
            }




            var stats = ship.mutableStats

            if (damageDealt >= maxDamage) {
                stats.zeroFluxMinimumFluxLevel.modifyFlat(modID, 2f)
            }

            var level = damageDealt / maxDamage
            stats.ballisticWeaponDamageMult.modifyMult(modID, 1 + (0.3f * level))
            stats.energyWeaponDamageMult.modifyMult(modID, 1 + (0.3f * level))
        }

        override fun modifyDamageDealt(param: Any?, target: CombatEntityAPI?, damage: DamageAPI?, point: Vector2f?,  shieldHit: Boolean): String? {
            if (target is ShipAPI && (!target.isAlive || target.owner == ship.owner)) return null
            if (target !is ShipAPI) return null
            if (shieldHit) return null

            var currentDamage = 0f

            if (param is BeamAPI) {
                currentDamage +=  damage!!.damage * damage.dpsDuration
            }
            else {
                currentDamage +=  damage!!.damage
            }

            damageDealt += currentDamage
            damageDealt = MathUtils.clamp(damageDealt, 0f, maxDamage)

            var recovered = currentDamage * 0.1f

            if (ship.hitpoints <= ship.maxHitpoints && recoveredHealth <= ship.maxHitpoints * 0.5f) {

                ship.hitpoints += recovered

                var over = 0f
                if (ship.hitpoints > ship.maxHitpoints) {
                    over = ship.hitpoints - ship.maxHitpoints
                    ship.hitpoints -= over
                }

                recoveredHealth += MathUtils.clamp(recovered - over, 0f, ship.maxHitpoints)
            }

            return null
        }

    }

}