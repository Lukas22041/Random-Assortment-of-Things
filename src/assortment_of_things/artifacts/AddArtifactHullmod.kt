package assortment_of_things.artifacts

import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc

class AddArtifactHullmod : EveryFrameScript {

    var interval = IntervalUtil(1f, 1f)

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {

        interval.advance(amount)
        if (interval.intervalElapsed()) {
            var hasArtifact = ArtifactUtils.getActiveArtifact() != null
            var fleet = Global.getSector().playerFleet
            if (hasArtifact) {
                for (member in fleet.fleetData.membersListCopy) {
                    if (!member.variant.hasHullMod("rat_artifact_controller")) {
                        if (member.variant.source != VariantSource.REFIT) {
                            var variant = member.variant.clone();
                            variant.originalVariant = null;
                            variant.hullVariantId = Misc.genUID()
                            variant.source = VariantSource.REFIT
                            member.setVariant(variant, false, true)
                        }

                        member.variant.addMod("rat_artifact_controller")
                    }
                }
            }
        }
    }
}