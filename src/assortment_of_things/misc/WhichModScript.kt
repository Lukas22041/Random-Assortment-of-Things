package assortment_of_things.misc

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.codex.CodexDataV2
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.fleet.FleetMember
import com.fs.starfarer.ui.impl.StandardTooltipV2
import com.fs.state.AppDriver
import second_in_command.misc.getChildrenCopy

class WhichModScript : EveryFrameScript {

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }


    override fun advance(amount: Float) {

        if (!Global.getSector().isPaused) return

        if (!RATSettings.whichModShips!! && !RATSettings.whichModWeapons!! && !RATSettings.whichModFighters!! && !RATSettings.whichModHullmods!!) return

        var state = AppDriver.getInstance().currentState
        if (state !is CampaignState) return

        var screenPanel = ReflectionUtils.get("screenPanel", state) as UIPanelAPI ?: return

        var tooltip = screenPanel.getChildrenCopy().find { it is StandardTooltipV2 } as UIPanelAPI? ?: return

        var codexTooltip = tooltip.getChildrenCopy().find { it is LabelAPI } as LabelAPI?

        if (codexTooltip != null) {
            var codexEntryId = ReflectionUtils.invoke("getCodexEntryId", tooltip) as String?

            if (codexEntryId != null) {
                if (codexEntryId.contains("codex_weapon_") && !RATSettings.whichModWeapons!!) return
                if (codexEntryId.contains("codex_fighter_") && !RATSettings.whichModFighters!!) return
                if (codexEntryId.contains("codex_hullmod_") && !RATSettings.whichModHullmods!!) return
            }

            var codexEntry =  CodexDataV2.getEntry(codexEntryId)
            if (codexEntry != null) {
                var source = codexEntry.sourceMod
                if (source != null) {
                    var modname = source.name
                    if (modname != null) {
                        if (codexTooltip.text == "Press F2 to open Codex" || codexTooltip.text.contains("F2 open Codex")) {

                            var shorten = false
                            var originalText = "F2 open codex"
                            if (codexTooltip.text.contains("F1")) {
                                originalText = "F1 cycle weapons F2 codex"
                                shorten = true
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
            } else if (RATSettings.whichModShips!!) {
                var member = ReflectionUtils.getFieldOfType(FleetMember::class.java, tooltip)
                if (member is FleetMemberAPI) {
                    var spec = member.baseOrModSpec()
                    var mod = member.baseOrModSpec().sourceMod
                    var modname: String? = null
                    if (mod != null) modname = mod.name

                    if (modname != null && codexTooltip.text == "Press F2 to open Codex") {
                        codexTooltip.text += " - Data provided by $modname"
                        codexTooltip.position.setSize(codexTooltip.computeTextWidth(codexTooltip.text), codexTooltip.position.height)
                        codexTooltip.setHighlight("F2", modname)
                        codexTooltip.setHighlightColors(Misc.getHighlightColor(), Misc.getBasePlayerColor())
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