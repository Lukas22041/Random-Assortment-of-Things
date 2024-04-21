package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.*
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class AbyssalRaphaelsGrace : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {

        if (Global.getSettings().modManager.isModEnabled("secretsofthefrontier") && !stats!!.variant.hasHullMod("sotf_sierrasconcord")) {
            stats!!.variant.addPermaMod("sotf_sierrasconcord")
        }


        stats!!.maxSpeed.modifyFlat(id, 15f)
        stats!!.armorBonus.modifyFlat(id, 100f)
        stats!!.fluxDissipation.modifyFlat(id, 100f)
        stats!!.fluxCapacity.modifyFlat(id, 500f)

        stats!!.combatWeaponRepairTimeMult.modifyMult(id, 0.5f)
        stats.weaponHealthBonus.modifyPercent(id, 50f)


    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        var isInAbyss = false
        if (Global.getSector() != null && Global.getSector().playerFleet != null) {
            if (Global.getSector().playerFleet.containingLocation != null)
            {
                if (Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG)) {
                    isInAbyss = true
                }
            }
        }

        if (!isInAbyss) isInAbyss = ship!!.variant.hasHullMod("rat_sarakiels_blessing")

        var abyssColor = Misc.getGrayColor()
        var sectorColor = Misc.getHighlightColor()

        if (isInAbyss) {
            abyssColor = Misc.getHighlightColor()
            sectorColor = Misc.getGrayColor()
        }

        tooltip!!.addSpacer(5f)

        tooltip.addPara("A unique hull, almost unlike any other observed within the abyssal depths. It appears alike to some, but its mechanisms function entirely differently.")

        tooltip.addSpacer(10f)

        tooltip.addPara("The ships maximum speed is increased by 15 units, its armor and flux dissipation is improved by 100 and the flux capacity is increased by 500.\n\n" +
                "Additionaly the ships weapon durability and repair time is also improved by 50%%.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "15", "100", "500", "50%")

        tooltip.addSpacer(10f)

        tooltip.addSectionHeading("Saving Grace", Alignment.MID, 0f)

        tooltip.addSpacer(5f)

        tooltip.addPara("Every 100 units of armor or hull damage taken grant the ship a stack of Saving Grace. The ship can have up to 30 stacks at once. " +
                "\n\n" +
                "Each stack provides the ship with a 1%% increase in max speed/rate of fire/flux dissipation/damage reduction and will dissipate after 15 seconds or if it gets replaced by a new stack.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
        "100", "armor or hull", "Saving Grace", "30", "1%", "max speed", "rate of fire", "flux dissipation", "damage reduction", "15")


    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {

        if (Global.getCombatEngine() != null) {
            Global.getCombatEngine().addLayeredRenderingPlugin(SeraphRenderer(ship!!))
        }


        ship!!.addListener(RaphaelsGraceListener(ship))
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to hulls."
    }

    override fun getDisplaySortOrder(): Int {
        return -1
    }

    override fun getBorderColor(): Color {
        return AbyssUtils.SIERRA_COLOR
    }

    override fun getNameColor(): Color {
        return AbyssUtils.SIERRA_COLOR
    }

    class RaphaelsGraceListener(var ship: ShipAPI) : AdvanceableListener, DamageListener {

        class RaphaelsGraceStack(var duration: Float)

        var damagePerStack = 100f
        var stackDuration = 15f
        var damageSoFar = 0f
        var stacks = ArrayList<RaphaelsGraceStack>()

        var maxStacks = 30

        override fun advance(amount: Float) {

            for (stack in ArrayList(stacks)) {
                if (!ship.isPhased) {
                    stack.duration -= 1 * amount
                    if (stack.duration < 0) {
                        stacks.remove(stack)
                    }
                }
            }

            if (ship == Global.getCombatEngine().playerShip) {
                Global.getCombatEngine().maintainStatusForPlayerShip(
                    "rat_seraphs_grace_status", "graphics/icons/hullsys/high_energy_focus.png", "Saving Grace", "Stacks: ${stacks.size}", false)

            }

            var mod = 0.01f * stacks.size

            ship.mutableStats.maxSpeed.modifyMult("rat_saving_grace_stack", 1f + mod)

            ship.mutableStats.fluxDissipation.modifyMult("rat_saving_grace_stack", 1f + mod)

            ship.mutableStats.energyRoFMult.modifyMult("rat_saving_grace_stack", 1f + mod)
            ship.mutableStats.ballisticRoFMult.modifyMult("rat_saving_grace_stack", 1f + mod)
            ship.mutableStats.missileRoFMult.modifyMult("rat_saving_grace_stack", 1f + mod)

            ship.mutableStats.energyWeaponFluxCostMod.modifyMult("rat_saving_grace_stack", 1f - mod)
            ship.mutableStats.ballisticWeaponFluxCostMod.modifyMult("rat_saving_grace_stack", 1f - mod)
            ship.mutableStats.missileWeaponFluxCostMod.modifyMult("rat_saving_grace_stack", 1f - mod)


            ship.mutableStats.armorDamageTakenMult.modifyMult("rat_saving_grace_stack", 1f - mod)
            ship.mutableStats.hullDamageTakenMult.modifyMult("rat_saving_grace_stack", 1f - mod)
            ship.mutableStats.shieldDamageTakenMult.modifyMult("rat_saving_grace_stack", 1f - mod)
            ship.mutableStats.empDamageTakenMult.modifyMult("rat_saving_grace_stack", 1f - mod)

        }

        override fun reportDamageApplied(source: Any?, target: CombatEntityAPI?, result: ApplyDamageResultAPI?) {

            var hull = result!!.damageToHull
            var armor = result.totalDamageToArmor

            var total = hull + armor

            damageSoFar += total

            var divided = damageSoFar / damagePerStack
            if (divided < 1f) return

            var dividedInt = divided.toInt()
            for (i in 0 until dividedInt) {

                if (stacks.size < maxStacks) {
                    stacks.add(RaphaelsGraceStack(stackDuration))
                }
                else if (stacks.isNotEmpty()) {
                    var stack = stacks.sortedBy { it.duration }.first()

                    stacks.remove(stack)

                    stacks.add(RaphaelsGraceStack(stackDuration))
                }
            }

            damageSoFar = 0f
        }
    }
}