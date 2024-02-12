package assortment_of_things.exotech.hullmods.alteration

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.misc.addNegativePara
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker

class AutonomousBaysHullmod : BaseAlteration() {

    var modID = "rat_autonomous_bays"

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        stats!!.numFighterBays.modifyFlat(id, 1f)
        stats.dynamic.getStat(Stats.FIGHTER_CREW_LOSS_MULT).modifyMult(id, 0f)
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {

    }




    override fun applyEffectsToFighterSpawnedByShip(fighter: ShipAPI?, ship: ShipAPI?, id: String?) {
        var aiCoreId = Commodities.GAMMA_CORE

        val core = Global.getFactory().createPerson()
        core.setFaction(Factions.PLAYER)
        core.aiCoreId = aiCoreId

        core.portraitSprite = "graphics/portraits/portrait_ai1b.png"
        core.stats.level = 3

        var availableSkills = WeightedRandomPicker<String>()
        availableSkills.add(Skills.HELMSMANSHIP, 1f)
        availableSkills.add(Skills.TARGET_ANALYSIS, 1f)
        availableSkills.add(Skills.MISSILE_SPECIALIZATION, 1f)
        availableSkills.add(Skills.FIELD_MODULATION, 1f)
        availableSkills.add(Skills.GUNNERY_IMPLANTS, 1f)
        availableSkills.add(Skills.SYSTEMS_EXPERTISE, 1f)
        availableSkills.add(Skills.DAMAGE_CONTROL, 1f)
        availableSkills.add(Skills.IMPACT_MITIGATION, 1f)

        core.stats.setSkillLevel(availableSkills.pickAndRemove(), 2f)
        core.stats.setSkillLevel(availableSkills.pickAndRemove(), 2f)
        core.stats.setSkillLevel(availableSkills.pickAndRemove(), 2f)

        core.setPersonality(Personalities.RECKLESS)
        core.setRankId(Ranks.SPACE_CAPTAIN)
        core.setPostId(null)

        fighter!!.captain = core

        if (core.stats.hasSkill(Skills.MISSILE_SPECIALIZATION)) {
            for (weapon in fighter!!.allWeapons) {
                if (weapon.type == WeaponAPI.WeaponType.MISSILE || weapon.type == WeaponAPI.WeaponType.COMPOSITE)
                weapon.maxAmmo = fighter.mutableStats.missileAmmoBonus.computeEffective(weapon.spec.maxAmmo.toFloat()).toInt()
                weapon.resetAmmo()
            }
        }
    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?, isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {

        tooltip!!.addSpacer(5f)
        tooltip!!.addPara("The ship is modified to deploy fighters that have been manually alterated to allow for the control from a central gamma-core AI. This negates the fighter crew loss, allows for an additional bay to be installed and provides each individual fighter with gamma-core skills.", 0f
        ,Misc.getTextColor(), Misc.getHighlightColor(), "crew loss", "additional bay", "gamma-core")

    }

    override fun canInstallAlteration(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?): Boolean {

        var builtIns = 0

        for (i in 0 until member!!.stats.numFighterBays.modifiedValue.toInt()) {
            var wingID = variant!!.fittedWings.getOrNull(i) ?: continue
            var isBuiltin = !variant.nonBuiltInWings.contains(wingID)
            if (isBuiltin) {
                builtIns++
            }
        }

        return member!!.baseOrModSpec().fighterBays != 0 && builtIns != member.stats.numFighterBays.modifiedValue.toInt()
    }

    override fun cannotInstallAlterationTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?, variant: ShipVariantAPI?, width: Float) {
        if (member!!.baseOrModSpec().fighterBays == 0) {
            tooltip!!.addNegativePara("Can only be installed on ships that can deploy fighters even without installing other modifications.")
        }

        var builtIns = 0

        for (i in 0 until member!!.stats.numFighterBays.modifiedValue.toInt()) {
            var wingID = variant!!.wings.getOrNull(i) ?: continue
            var isBuiltin = !variant.nonBuiltInWings.contains(wingID)
            if (isBuiltin) {
                builtIns++
            }
        }

        if (builtIns == member.stats.numFighterBays.modifiedValue.toInt()) {
            tooltip!!.addNegativePara("Can not be installed on ships that only have built-in fighters.")
        }
    }

    override fun onAlterationRemove(member: FleetMemberAPI?, variant: ShipVariantAPI?, marketAPI: MarketAPI?) {
        super.onAlterationRemove(member, variant, marketAPI)

        if (variant!!.fittedWings.size >= member!!.stats.numFighterBays.modifiedValue) {
            var last = variant.fittedWings.lastOrNull() ?: return
            var index = variant.wings.lastIndexOf(last)
            variant.setWingId(index, null)
            Global.getSector().playerFleet.cargo.addFighters(last, 1)
        }
    }
}