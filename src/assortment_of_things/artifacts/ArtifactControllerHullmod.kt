package assortment_of_things.artifacts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI

class ArtifactControllerHullmod : BaseHullMod() {

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {

        if (ArtifactUtils.getActiveArtifact() != null)
        {
            ArtifactUtils.getActivePlugin()!!.applyEffectsAfterShipCreation(ship!!, ArtifactUtils.STAT_MOD_ID)
        }
    }

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {

        if (ArtifactUtils.getActiveArtifact() != null)
        {
            ArtifactUtils.getActivePlugin()!!.applyEffectsBeforeShipCreation(hullSize!!, stats!!, ArtifactUtils.STAT_MOD_ID)
        }
    }

    override fun advanceInCombat(ship: ShipAPI?, amount: Float) {
        super.advanceInCombat(ship, amount)

        if (ArtifactUtils.getActiveArtifact() != null)
        {
            ArtifactUtils.getActivePlugin()!!.advanceInCombat(ship!!, amount)
        }
    }

}