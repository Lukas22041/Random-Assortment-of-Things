package assortment_of_things.patches

import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.coreui.CaptainPickerDialog
import patchlib.api.context.BeforeContext
import patchlib.api.context.MethodCallContext
import patchlib.api.match.ClassMatch
import patchlib.api.match.MethodMatch
import patchlib.api.match.MethodType
import patchlib.api.patch.After
import patchlib.api.patch.Before
import patchlib.api.patch.Patch
import patchlib.api.patch.RedirectMethodCall
import patchlib.api.query.FieldQuery

/**
 * Patch that enables RATs hull conversion hullmod, allowing humans to be slot in to automated ships.
 */
@Patch(target = ClassMatch(type = CaptainPickerDialog::class))
object CrewConversionPatch {

    var hmods = ArrayList<String>()
    init {
        hmods.add("rat_abyssal_conversion")
        hmods.add("rat_chronos_conversion")
        hmods.add("rat_cosmos_conversion")
        hmods.add("rat_seraph_conversion")
        hmods.add("rat_primordial_conversion")
    }

    @JvmStatic
    @RedirectMethodCall(target = MethodMatch(methodType = MethodType.CONSTRUCTOR), call = MethodMatch(methodName = "isAutomated"))
    fun redirectIsAutomated(context: MethodCallContext) {
        //Set to false if the ship as the hullmod
        var member = context.getCallArg(0) as FleetMemberAPI?
        var variant = member?.variant
        if (variant != null && variant.hullMods.any() { hmods.contains(it) }) {
            context.setResult(false)
            return
        }

        //Default call
        context.setResult(context.call())
    }

}