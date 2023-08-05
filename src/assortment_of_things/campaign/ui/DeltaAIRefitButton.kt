package assortment_of_things.campaign.ui

import assortment_of_things.abyss.hullmods.basic.DeltaAIAssistantHullmod
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.skills.BallisticMastery
import com.fs.starfarer.api.impl.campaign.skills.CombatEndurance
import com.fs.starfarer.api.impl.campaign.skills.DamageControl
import com.fs.starfarer.api.impl.campaign.skills.EnergyWeaponMastery
import com.fs.starfarer.api.impl.campaign.skills.FieldModulation
import com.fs.starfarer.api.impl.campaign.skills.GunneryImplants
import com.fs.starfarer.api.impl.campaign.skills.Helmsmanship
import com.fs.starfarer.api.impl.campaign.skills.ImpactMitigation
import com.fs.starfarer.api.impl.campaign.skills.MissileSpecialization
import com.fs.starfarer.api.impl.campaign.skills.OrdnanceExpertise
import com.fs.starfarer.api.impl.campaign.skills.PointDefense
import com.fs.starfarer.api.impl.campaign.skills.PolarizedArmor
import com.fs.starfarer.api.impl.campaign.skills.SystemsExpertise
import com.fs.starfarer.api.impl.campaign.skills.TargetAnalysis
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaRefit.BaseRefitButton
import java.util.logging.Level

class DeltaAIRefitButton : BaseRefitButton() {

    override fun hasTooltip(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return true
    }

    override fun addTooltip(tooltip: TooltipMakerAPI?, member: FleetMemberAPI?,variant: ShipVariantAPI?,market: MarketAPI?) {
        var skill = DeltaAIAssistantHullmod.getCurrentSkill(variant!!)

        tooltip!!.addPara("Allows selecting a skill for the ships Delta-AI integration.", 0f)

        tooltip.addSpacer(10f)

        var path = "graphics/icons/mission_marker.png"
        var name = "No skill set."

        if (skill != null) {
            name = skill.name
            path = skill.spriteName
        }
        Global.getSettings().loadTexture(path)

        var img = tooltip.beginImageWithText(path, 48f)

        img.addPara("Selected Skill:", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        img.addPara(name, 0f)

        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)
    }

    override fun getButtonName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "Delta-AI Skill"
    }

    override fun getPanelWidth(member: FleetMemberAPI?, variant: ShipVariantAPI?): Float {
        return 300f
    }

    override fun getPanelHeight(member: FleetMemberAPI?, variant: ShipVariantAPI?): Float {
        return 600f
    }

    override fun hasPanel(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return true
    }

    override fun getIconName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "graphics/hullmods/rat_delta_ai.png"
    }

    override fun initPanel(backgroundPanel: CustomPanelAPI?, member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?) {
        super.initPanel(backgroundPanel, member, variant, market)

        var width = getPanelWidth(member, variant)
        var height = getPanelHeight(member, variant)
        //  var skills = Global.getSettings().skillIds.map { Global.getSettings().getSkillSpec(it) }
         var skills = deltaSkills.keys.map { Global.getSettings().getSkillSpec(it) }

        var element =backgroundPanel!!.createUIElement(width, height, true)
       // element!!.addSkillPanelOneColumn(Global.getSector().playerPerson, 0f)


        element.addLunaElement(width - 20f, 25f).apply {
            enableTransparency = true
            backgroundAlpha = 0.7f
            borderAlpha = 0.8f

            addText("Click to select a Skill", baseColor = Misc.getBasePlayerColor())
            centerText()

            position.inTL(10f, 10f)
        }

        element.addSpacer(10f)

        for (skill in skills) {
           // if (skill.hasTag("npc_only") || skill.hasTag("deprecated") || skill.hasTag("ai_core_only")) continue

            var name = skill.name

            element.addLunaElement(width - 20f, 60f).apply {
                enableTransparency = true
                backgroundAlpha = 0.5f

                onHoverEnter {
                    playScrollSound()
                    backgroundAlpha = 0.7f
                }

                onHoverExit {
                    backgroundAlpha = 0.5f
                }

                onClick {
                    playClickSound()

                    var tag = variant!!.tags.find { it.contains("rat_deltaskill_") }
                    if (tag != null) {
                        variant.tags.remove(tag)
                    }

                    variant!!.addTag("rat_deltaskill_${skill.id}")

                    closePanel()
                    refreshVariant()
                    refreshButtonList()
                }

                var fake = Global.getFactory().createPerson()
                fake.stats.setSkillLevel(skill.id, 2f)

                innerElement!!.addSkillPanelOneColumn(fake, 0f)
            }

            element.addSpacer(10f)

        }

        element.addSpacer(10f)

        backgroundPanel.addUIElement(element)

    }

    override fun shouldShow(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return variant!!.hasHullMod("rat_delta_assistant")
    }




    companion object {
        @JvmStatic
        var deltaSkills = mapOf<String, (stats: MutableShipStatsAPI, hullsize: HullSize, id: String, level: Float) -> Unit>(

            "helmsmanship" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                Helmsmanship.Level1().apply(stats, size, id, level)
                Helmsmanship.Level2().apply(stats, size, id, level)
                Helmsmanship.Level3().apply(stats, size, id, level)
                Helmsmanship.Level4().apply(stats, size, id, level)
            },

            "combat_endurance" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                CombatEndurance.Level1().apply(stats, size, id, level)
                CombatEndurance.Level2().apply(stats, size, id, level)
                CombatEndurance.Level3().apply(stats, size, id, level)
                CombatEndurance.Level4().apply(stats, size, id, level)
            },

            "impact_mitigation" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                ImpactMitigation.Level2().apply(stats, size, id, level)
                ImpactMitigation.Level4().apply(stats, size, id, level)
                ImpactMitigation.Level6().apply(stats, size, id, level)
            },

            "damage_control" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                DamageControl.Level2().apply(stats, size, id, level)
                DamageControl.Level3().apply(stats, size, id, level)
                DamageControl.Level4().apply(stats, size, id, level)
                DamageControl.Level6().apply(stats, size, id, level)
            },

            "field_modulation" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                FieldModulation.Level1().apply(stats, size, id, level)
                FieldModulation.Level2().apply(stats, size, id, level)
                FieldModulation.Level3().apply(stats, size, id, level)
                FieldModulation.Level4().apply(stats, size, id, level)
            },

            "point_defense" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                PointDefense.Level1().apply(stats, size, id, level)
                PointDefense.Level2().apply(stats, size, id, level)
                PointDefense.Level3().apply(stats, size, id, level)
            },

            "target_analysis" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                TargetAnalysis.Level1().apply(stats, size, id, level)
                TargetAnalysis.Level2().apply(stats, size, id, level)
                TargetAnalysis.Level3().apply(stats, size, id, level)
                TargetAnalysis.Level4().apply(stats, size, id, level)
            },

            "ballistic_mastery" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                BallisticMastery.Level1().apply(stats, size, id, level)
                BallisticMastery.Level2().apply(stats, size, id, level)
                BallisticMastery.Level3().apply(stats, size, id, level)
            },

            "systems_expertise" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                SystemsExpertise.Level1().apply(stats, size, id, level)
                SystemsExpertise.Level2().apply(stats, size, id, level)
                SystemsExpertise.Level3().apply(stats, size, id, level)
                SystemsExpertise.Level4().apply(stats, size, id, level)
                SystemsExpertise.Level5().apply(stats, size, id, level)
                SystemsExpertise.Level6().apply(stats, size, id, level)
                SystemsExpertise.Level7().apply(stats, size, id, level)
            },

            "systems_expertise" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                SystemsExpertise.Level1().apply(stats, size, id, level)
                SystemsExpertise.Level2().apply(stats, size, id, level)
                SystemsExpertise.Level3().apply(stats, size, id, level)
                SystemsExpertise.Level4().apply(stats, size, id, level)
                SystemsExpertise.Level5().apply(stats, size, id, level)
                SystemsExpertise.Level6().apply(stats, size, id, level)
                SystemsExpertise.Level7().apply(stats, size, id, level)
            },

            "missile_specialization" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                MissileSpecialization.Level1().apply(stats, size, id, level)
                MissileSpecialization.Level2().apply(stats, size, id, level)
                MissileSpecialization.Level3().apply(stats, size, id, level)
                MissileSpecialization.Level4().apply(stats, size, id, level)
            },


            //Tech
            "gunnery_implants" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                GunneryImplants.Level1().apply(stats, size, id, level)
                GunneryImplants.Level2().apply(stats, size, id, level)
                GunneryImplants.Level3().apply(stats, size, id, level)
                GunneryImplants.Level1A().apply(stats, size, id, level)
            },

            "energy_weapon_mastery" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                EnergyWeaponMastery.Level1().apply(stats, size, id, level)
                EnergyWeaponMastery.Level2().apply(stats, size, id, level)
            },


            //Logi
            "ordnance_expert" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                OrdnanceExpertise.Level1().apply(stats, size, id, level)
                OrdnanceExpertise.Level3().apply(stats, size, id, level)
            },

            "polarized_armor" to { stats: MutableShipStatsAPI, size: HullSize, id: String, level: Float ->
                PolarizedArmor.Level1().apply(stats, size, id, level)
                PolarizedArmor.Level2().apply(stats, size, id, level)
                PolarizedArmor.Level3().apply(stats, size, id, level)
                PolarizedArmor.Level4().apply(stats, size, id, level)
            },
        )
    }
}