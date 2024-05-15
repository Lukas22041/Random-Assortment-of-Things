package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.hullmods.HullmodTooltipAbyssParticles
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.*
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement

class AbyssalSeraphsGrace : BaseHullMod() {


    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {

        if (Global.getSector().playerFleet?.fleetData?.membersListCopy?.contains(stats!!.fleetMember) == true) {
            stats!!.variant.removeTag(Tags.SHIP_LIMITED_TOOLTIP)
        }

        if (!stats!!.variant.hasHullMod("rat_abyssal_conversion") && !stats!!.variant.hasHullMod("rat_chronos_conversion") && !stats!!.variant.hasHullMod("rat_cosmos_conversion") && !stats!!.variant.hasHullMod("rat_seraph_conversion")  && !stats.variant.hasHullMod(HullMods.AUTOMATED)) {
            stats.variant.addPermaMod(HullMods.AUTOMATED)
        }

        var isInAbyss = false
        if (Global.getSector() != null && Global.getSector().playerFleet != null) {
            if (Global.getSector().playerFleet.containingLocation != null)
            {
                if (Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG)) {
                    isInAbyss = true
                }
            }
        }

        if (!isInAbyss) isInAbyss = stats.variant.hasHullMod("rat_sarakiels_blessing")


        if (isInAbyss) {
            stats!!.maxSpeed.modifyFlat(id, 15f)
            stats!!.armorBonus.modifyFlat(id, 100f)
            stats!!.fluxDissipation.modifyFlat(id, 100f)
            stats!!.fluxCapacity.modifyFlat(id, 500f)

            stats!!.combatWeaponRepairTimeMult.modifyMult(id, 0.5f)
            stats.weaponHealthBonus.modifyPercent(id, 50f)
        }
        else {

            stats!!.combatWeaponRepairTimeMult.modifyMult(id, 0.8f)
            stats.weaponHealthBonus.modifyPercent(id, 20f)
        }

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        var initialHeight = tooltip!!.heightSoFar
        var particleSpawner = HullmodTooltipAbyssParticles(tooltip, initialHeight)
        var element = tooltip!!.addLunaElement(0f, 0f).apply {
            advance { particleSpawner.advance(this, it) }
            render { particleSpawner.renderBelow(this, it) }
        }

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

        tooltip.addPara("This hull comes with a unique apparatus that allows it enhance the ship through flux residue absorbed from the enviroment. " +
                "\n\nThis effect is much stronger while the ship is moving through the abyss.", 0f)

        tooltip.addSpacer(10f)

        val col1W = 160f
        val colW = ((width - col1W - 12f) / 2f).toInt().toFloat()

        var entries = arrayOf<Any>("Stat", col1W, "In Sector", colW, "In Abyss", colW)

        tooltip.beginTable(Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Misc.getBrightPlayerColor(), 20f, true, true,
            *entries)


        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Saving Grace Stacks",
            Alignment.MID, sectorColor, "20",
            Alignment.MID, abyssColor, "30",
        )

        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Max Speed",
            Alignment.MID, sectorColor, "+0",
            Alignment.MID, abyssColor, "+15",
            )

        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Armor",
            Alignment.MID, sectorColor, "+0",
            Alignment.MID, abyssColor, "+100",
        )

        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Flux Dissipation",
            Alignment.MID, sectorColor, "+0",
            Alignment.MID, abyssColor, "+100",
        )

        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Flux Capacity",
            Alignment.MID, sectorColor, "+0",
            Alignment.MID, abyssColor, "+500",
        )

        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Weapon Durability",
            Alignment.MID, sectorColor, "+20%",
            Alignment.MID, abyssColor, "+50%",
        )

        tooltip.addRow(
            Alignment.MID, Misc.getTextColor(), "Weapon Repair Time",
            Alignment.MID, sectorColor, "-20%",
            Alignment.MID, abyssColor, "-50%",
        )

        tooltip.addTable("", 0, 0f)

        tooltip.addSpacer(15f)

        tooltip.addSectionHeading("Saving Grace", Alignment.MID, 0f)

        tooltip.addSpacer(5f)

        tooltip.addPara("Every 100 units of armor or hull damage taken grant the ship a stack of Saving Grace. The amount of stacks can't exceed the maximum mentioned in the stat grid above. " +
                "\n\n" +
                "Each stack provides the ship with a 1%% increase in max speed/rate of fire/flux dissipation/damage reduction and will dissipate after 15 seconds or if it gets replaced by a new stack.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(),
        "100", "armor or hull", "Saving Grace", "1%", "max speed", "rate of fire", "flux dissipation", "damage reduction", "15")

        tooltip.addSpacer(5f)
        tooltip.addPara("While in the abyss, whenever a stack dissipates it vents 10 flux with it.", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "10")

        tooltip!!.addLunaElement(0f, 0f).apply {
            render {particleSpawner.renderForeground(element, it)  }
        }
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {

        if (Global.getCombatEngine() != null) {
            Global.getCombatEngine().addLayeredRenderingPlugin(SeraphRenderer(ship!!))
        }

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

        ship!!.addListener(SeraphsGraceListener(ship, isInAbyss))
    }

    override fun getDisplaySortOrder(): Int {
        return 1
    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }

    override fun getUnapplicableReason(ship: ShipAPI?): String {
        return "Can only be prebuilt in to abyssal hulls."
    }

    class SeraphsGraceListener(var ship: ShipAPI, var isInAbyss: Boolean) : AdvanceableListener, DamageListener {

        class SeraphsGraceStack(var duration: Float)

        var damagePerStack = 100f
        var stackDuration = 15f
        var damageSoFar = 0f
        var stacks = ArrayList<SeraphsGraceStack>()

        var maxStacks = 20

        init {
            if (isInAbyss) maxStacks = 30
            else maxStacks = 20
        }

        override fun advance(amount: Float) {

            for (stack in ArrayList(stacks)) {
                if (!ship.isPhased) {
                    stack.duration -= 1 * amount
                    if (stack.duration < 0) {
                        stacks.remove(stack)

                        if (isInAbyss) {
                            ship.fluxTracker.decreaseFlux(10f)
                        }
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
                    stacks.add(SeraphsGraceStack(stackDuration))
                }
                else if (stacks.isNotEmpty()) {
                    var stack = stacks.sortedBy { it.duration }.first()

                    stacks.remove(stack)

                    if (isInAbyss) {
                        ship.fluxTracker.decreaseFlux(10f)
                    }

                    stacks.add(SeraphsGraceStack(stackDuration))
                }
            }

            damageSoFar = 0f
        }
    }
}