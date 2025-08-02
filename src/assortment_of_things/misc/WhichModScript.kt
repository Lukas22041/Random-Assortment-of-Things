package assortment_of_things.misc

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ModSpecAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.codex.CodexDataV2
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.fleet.FleetMember
import com.fs.starfarer.ui.impl.StandardTooltipV2
import com.fs.state.AppDriver

class WhichModScript : EveryFrameScript {

    companion object {
        //Randomises what mod it tells you something is from
        var isAprilFirst = false
        var rememberedAprilFirstMods = HashMap<String, ModSpecAPI>()
    }

    var allowed = listOf(
        "codex_hull_",
        "codex_weapon_",
        "codex_fighter_",
        "codex_hullmod_",
        "codex_item_",
        "codex_commodity_",
        "codex_artifacts_",
        "codex_industry_",
        "codex_condition_",
        "codex_skill_",
        "codex_ability_",
        "codex_sic_aptitude_"
    )

    /*var screenPanelField = ReflectionUtils.getField("screenPanel", CampaignState::class.java)!!
    var getCodexEntryMethod = ReflectionUtils.getMethod("getCodexEntryId", StandardTooltipV2::class.java)!!
    var getChildrenCopyMethod: ReflectionUtils.ReflectedMethod? = null*/

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }


    override fun advance(amount: Float) {

        //if (!Global.getSector().isPaused) return

        //if (!RATSettings.whichModShips!! && !RATSettings.whichModWeapons!! && !RATSettings.whichModFighters!! && !RATSettings.whichModHullmods!!) return

        var state = AppDriver.getInstance().currentState
        if (state !is CampaignState) return

        //var screenPanel = ReflectionUtils.get("screenPanel", state) as UIPanelAPI ?: return
        var screenPanel = ReflectionUtils.get("screenPanel", state) as UIPanelAPI ?: return

        var tooltip = screenPanel.getChildrenCopy().find { it is StandardTooltipV2 } as UIPanelAPI? ?: return

        var codexTooltip = tooltip.getChildrenCopy().find { it is LabelAPI } as LabelAPI?

        if (codexTooltip != null) {

            /*if (getCodexEntryMethod == null) {
                getCodexEntryMethod = ReflectionUtils.getMethod("getCodexEntryId", tooltip)
            }*/



            var codexEntryId = ReflectionUtils.invoke("getCodexEntryId", tooltip) as String?

            if (codexEntryId != null) {

                if (allowed.none { codexEntryId.contains(it) }) return

                if (codexEntryId.contains("codex_hull_") && !RATSettings.whichModShips!!) return
                else if (codexEntryId.contains("codex_weapon_") && !RATSettings.whichModWeapons!!) return
                else if (codexEntryId.contains("codex_fighter_") && !RATSettings.whichModFighters!!) return
                else if (codexEntryId.contains("codex_hullmod_") && !RATSettings.whichModHullmods!!) return
                else if ((codexEntryId.contains("codex_item_") || codexEntryId.contains("codex_commodity_") || codexEntryId.contains("codex_artifacts_")) && !RATSettings.whichModCargo!!) return
                else if (codexEntryId.contains("codex_industry_")  && !RATSettings.whichModIndustries!!) return
                else if (codexEntryId.contains("codex_condition_")  && !RATSettings.whichModConditions!!) return
                else if ((codexEntryId.contains("codex_skill_") || codexEntryId.contains("codex_ability_") || codexEntryId.contains("codex_sic_aptitude_")) && !RATSettings.whichModSkills!!) {
                    return
                }

            }

            var codexEntry =  CodexDataV2.getEntry(codexEntryId)
            if (codexEntry != null) {
                var source = codexEntry.sourceMod

                //Pick a random mod on april first, but keep it consistent, shouldnt re-randomise on each hover
                if (isAprilFirst) {
                    source = rememberedAprilFirstMods.getOrPut(codexEntryId!!) {
                        Global.getSettings().modManager.availableModsCopy.random()
                    }
                }

                if (source != null) {
                    var modname = source.name

                    if (modname != null) {
                        if (codexTooltip.text == "Press F2 to open Codex" || codexTooltip.text.contains("F2 open Codex")) {

                            var originalText = "F2 open codex"
                            if (codexTooltip.text.contains("F1 cycle weapon info")) {
                                //originalText = "F1 cycle weapons F2 codex"
                                originalText = "F1 weapons F2 codex"
                            }
                            if (codexTooltip.text.contains("F1 more info")) {
                                originalText = "F1 more info F2 codex"
                            }
                            else if (codexTooltip.text.contains("F1 hide")) {
                                originalText = "F1 hide F2 codex"
                            }

                            var text = originalText + " - $modname"

                            var toLong = false

                            codexTooltip.text = text
                            codexTooltip.position.setSize(codexTooltip.computeTextWidth(codexTooltip.text), codexTooltip.position.height)

                            while (codexTooltip.position.width +30> tooltip.position.width) {
                                toLong = true
                                modname = modname.substring(0, modname.lastIndex-1)
                                text = originalText + " - $modname"
                                codexTooltip.text = text
                                codexTooltip.position.setSize(codexTooltip.computeTextWidth(codexTooltip.text), codexTooltip.position.height)
                            }

                            if (toLong) {
                                modname = modname.substring(0, modname.lastIndex-1) + "..."
                                text = originalText + " - $modname"
                                codexTooltip.text = text
                                codexTooltip.position.setSize(tooltip.position.width-30, codexTooltip.position.height)
                            }


                            codexTooltip.setHighlight("F1","F2", modname)
                            codexTooltip.setHighlightColors(Misc.getHighlightColor(), Misc.getHighlightColor(), Misc.getBasePlayerColor())
                        }
                    }
                }
            } else if (RATSettings.whichModShips!! && ReflectionUtils.hasVariableOfType(FleetMember::class.java, tooltip)) {
                var member = ReflectionUtils.get(null, tooltip, FleetMember::class.java)
                if (member is FleetMemberAPI) {
                    var spec = member.hullSpec
                    var mod = spec.sourceMod
                    if (mod == null) mod = member.baseOrModSpec().sourceMod //Skins might have issues otherwise

                    if (isAprilFirst) {
                        mod = rememberedAprilFirstMods.getOrPut(spec.baseHullId) {
                            Global.getSettings().modManager.availableModsCopy.random()
                        }
                    }

                    var modname: String? = null
                    if (mod != null) modname = mod.name

                    if (modname != null && (codexTooltip.text == "Press F2 to open Codex" || codexTooltip.text == "F1 more info  F2 open Codex" || codexTooltip.text == "F1 hide  F2 open Codex")) {

                        var originalText = codexTooltip.text

                        var text = "$originalText - Data provided by $modname"

                        codexTooltip.text = text
                        codexTooltip.position.setSize(codexTooltip.computeTextWidth(codexTooltip.text), codexTooltip.position.height)

                        if (codexTooltip.position.width +30> tooltip.position.width) {
                            text = "$originalText - $modname"
                            codexTooltip.text = text
                            codexTooltip.position.setSize(codexTooltip.computeTextWidth(codexTooltip.text), codexTooltip.position.height)
                        }

                        codexTooltip.setHighlight("F1", "F2", modname)
                        codexTooltip.setHighlightColors(Misc.getHighlightColor(), Misc.getHighlightColor(), Misc.getBasePlayerColor())
                    }
                }
            }
        }



       /* if (codexEntryId != null) {

        }
        var member = ReflectionUtils.getFieldOfType(FleetMember::class.java, tooltip)
        else if (member is FleetMemberAPI) {
            var spec = member.baseOrModSpec()
            var mod = member.baseOrModSpec().sourceMod
            var modname = "Starsector"
            if (mod != null) modname = mod.name

            if (codexTooltip != null) {
                if (codexTooltip.text == "Press F2 to open Codex") {
                    codexTooltip.text += " - Data provided by $modname"
                    codexTooltip.position.setSize(codexTooltip.computeTextWidth(codexTooltip.text), codexTooltip.position.height)
                    codexTooltip.setHighlight("F2", modname)
                    codexTooltip.setHighlightColors(Misc.getHighlightColor(), Misc.getBasePlayerColor())
                }
            }
        }*/
    }

    fun abbreviate(text: String) : String
    {
        var input = this
        var words = text.split(" ")
        var abbreviation = ""
        for (word in words) {
            var char: Char? = null
            try {
                char = word.get(0)
                if (char.isUpperCase()) abbreviation += char
            } catch (e: Throwable) {}
        }
        return abbreviation
    }

}