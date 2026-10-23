package assortment_of_things.patches

import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.codex.CodexDataV2
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.fleet.FleetMember
import com.fs.starfarer.ui.impl.StandardTooltipV2
import patchlib.api.context.AfterContext
import patchlib.api.match.ClassMatch
import patchlib.api.match.MethodMatch
import patchlib.api.patch.After
import patchlib.api.patch.Patch
import patchlib.api.query.FieldQuery
import kotlin.text.lastIndex
import kotlin.text.substring

@Patch(target = ClassMatch(type = StandardTooltipV2::class))
object WhichModPatch {

    private var EXPAND_LABEL_QUERY = FieldQuery.create().fieldName("expandLabel").build()
    private var CODEX_ID_QUERY = FieldQuery.create().fieldName("codexEntryId").build()
    private var FLEET_MEMBER_QUERY = FieldQuery.create().type(FleetMember::class.java).build()

    private val settings = mapOf<String, () -> Boolean?>(
        "codex_hull_" to { RATSettings.whichModShips },
        "codex_weapon_" to { RATSettings.whichModWeapons },
        "codex_fighter_" to { RATSettings.whichModFighters },
        "codex_hullmod_" to { RATSettings.whichModHullmods },
        "codex_item_" to { RATSettings.whichModCargo },
        "codex_commodity_" to { RATSettings.whichModCargo },
        "codex_artifacts_" to { RATSettings.whichModCargo },
        "codex_industry_" to { RATSettings.whichModIndustries },
        "codex_condition_" to { RATSettings.whichModConditions },
        "codex_skill_" to { RATSettings.whichModSkills },
        "codex_ability_" to { RATSettings.whichModSkills },
        "codex_sic_aptitude_" to { RATSettings.whichModSkills },
    )

    @JvmStatic
    @After(target = MethodMatch(methodName = "makeExpandable"))
    fun afterMakeExpandable(context: AfterContext) {
        addWhichModText(context)
    }

    @JvmStatic
    @After(target = MethodMatch(methodName = "toggleExpandedLabelAndBoolean"))
    fun toggleExpandedLabelAndBoolean(context: AfterContext) {
        addWhichModText(context)
    }

    fun addWhichModText(context: AfterContext) {
        val tooltip = context.self as UIPanelAPI;
        val expandLabel = context.getField<LabelAPI>(EXPAND_LABEL_QUERY).get() ?: return
        val codexEntryId = context.getField<String?>(CODEX_ID_QUERY).get()



        var codexEntry =  CodexDataV2.getEntry(codexEntryId)
        if (codexEntry != null) {
            val isEnabledForCodexEntryType = settings.entries.find { codexEntryId.startsWith(it.key) }?.value ?: return
            if (isEnabledForCodexEntryType() != true) return

            var source = codexEntry.sourceMod

            if (source != null) {
                var modname = source.name

                if (modname != null) {
                    if (expandLabel.text == "Press F2 to open Codex" || expandLabel.text.contains("F2 open Codex")) {

                        var originalText = "F2 open codex"
                        if (expandLabel.text.contains("F1 cycle weapon info")) {
                            //originalText = "F1 cycle weapons F2 codex"
                            originalText = "F1 weapons F2 codex"
                        }
                        if (expandLabel.text.contains("F1 more info")) {
                            originalText = "F1 more info F2 codex"
                        }
                        else if (expandLabel.text.contains("F1 hide")) {
                            originalText = "F1 hide F2 codex"
                        }

                        var text = originalText + " - $modname"

                        var toLong = false

                        expandLabel.text = text
                        expandLabel.position.setSize(expandLabel.computeTextWidth(expandLabel.text), expandLabel.position.height)

                        val maxWidth = maxOf(tooltip.position.width, 200f)
                        while (expandLabel.position.width +30 > maxWidth) {
                            toLong = true
                            modname = modname.substring(0, modname.lastIndex-1)
                            text = originalText + " - $modname"
                            expandLabel.text = text
                            expandLabel.position.setSize(expandLabel.computeTextWidth(expandLabel.text), expandLabel.position.height)
                        }

                        if (toLong) {
                            modname = modname.substring(0, modname.lastIndex-1) + "..."
                            text = originalText + " - $modname"
                            expandLabel.text = text
                            expandLabel.position.setSize(tooltip.position.width-30, expandLabel.position.height)
                        }


                        expandLabel.setHighlight("F1","F2", modname)
                        expandLabel.setHighlightColors(Misc.getHighlightColor(), Misc.getHighlightColor(), Misc.getBasePlayerColor())
                    }
                }
            }
        }
        else if (RATSettings.whichModShips!! && context.hasField(FLEET_MEMBER_QUERY)) {
            var member = context.getField<FleetMemberAPI>(FLEET_MEMBER_QUERY).get()
            if (member is FleetMemberAPI) {
                var spec = member.hullSpec
                var mod = spec.sourceMod
                if (mod == null) mod = member.baseOrModSpec().sourceMod //Skins might have issues otherwise

                var modname: String? = null
                if (mod != null) modname = mod.name

                if (modname != null && (expandLabel.text == "Press F2 to open Codex" || expandLabel.text == "F1 more info  F2 open Codex" || expandLabel.text == "F1 hide  F2 open Codex")) {

                    var originalText = expandLabel.text

                    var text = "$originalText - Data provided by $modname"

                    expandLabel.text = text
                    expandLabel.position.setSize(expandLabel.computeTextWidth(expandLabel.text), expandLabel.position.height)

                    val maxWidth = maxOf(tooltip.position.width, 200f)
                    if (expandLabel.position.width + 30 > maxWidth) {
                        text = "$originalText - $modname"
                        expandLabel.text = text
                        expandLabel.position.setSize(expandLabel.computeTextWidth(expandLabel.text), expandLabel.position.height)
                    }

                    expandLabel.setHighlight("F1", "F2", modname)
                    expandLabel.setHighlightColors(Misc.getHighlightColor(), Misc.getHighlightColor(), Misc.getBasePlayerColor())
                }
            }
        }
    }

}