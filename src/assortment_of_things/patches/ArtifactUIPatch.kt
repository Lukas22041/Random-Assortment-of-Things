package assortment_of_things.patches

import assortment_of_things.artifacts.ArtifactUIPanel
import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.getChildrenCopy
import assortment_of_things.misc.getWidth
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUIAPI
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.campaign.ui.UITable
import patchlib.api.context.AfterContext
import patchlib.api.match.ClassMatch
import patchlib.api.match.FieldMatch
import patchlib.api.match.MethodMatch
import patchlib.api.patch.After
import patchlib.api.patch.Patch
import patchlib.api.query.FieldQuery
import patchlib.api.query.MethodQuery

/**
 * Patch that attaches RATs artifact Ui widget to the fleet sidebar
 */
@Patch(target = ClassMatch(subtype = UIPanelAPI::class, packageName = "com.fs.starfarer.coreui.refit.auto",
    fieldMatches = [FieldMatch(type = UITable::class)]))
object ArtifactUIPatch {

    private val CORE_UI_QUERY = FieldQuery.create().subtype(CoreUIAPI::class.java).build()
    private val CURRENT_TAB_QUERY = MethodQuery.create().methodName("getCurrentTab").parameterCount(0).build()

    @JvmStatic
    @After(target = MethodMatch(methodName = "sizeChanged", parameterCount = 2))
    fun afterSizeChanged(context: AfterContext) {

        if (!RATSettings.enableAbyss!! && !RATSettings.relicsEnabled!!) return

        val leftPanel = context.self as UIPanelAPI
        val children = leftPanel.getChildrenCopy()
        val table = children.find { it is UITable } as UIComponentAPI? ?: return

        val core = context.getField<Any?>(CORE_UI_QUERY).get()
        if (core == null || !context.hasMethod(CURRENT_TAB_QUERY, core)) return

        var fleetPanel = context.getMethod(CURRENT_TAB_QUERY, core).call<Any?>() as UIPanelAPI?

        val artifactPanel = ArtifactUIPanel(fleetPanel)

        val w = table.getWidth()
        val h = 60f
        table.position.setYAlignOffset(-h - 20f)

        val panel = Global.getSettings().createCustom(300f, h, null)
        leftPanel.addComponent(panel)
        panel.position.aboveLeft(table, 10f)
        artifactPanel.recreate(panel, w, h)

        (children.lastOrNull { it is LabelAPI } as? UIComponentAPI)?.position?.setYAlignOffset(-10f)

    }


}