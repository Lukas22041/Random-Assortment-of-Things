package assortment_of_things.abyss.hullmods.abyssals

import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.FaderUtil
import org.magiclib.kotlin.setAlpha
import java.awt.Color
import java.util.*

class IntegratedCosmosCore : BaseHullMod() {

    var minCrew = mapOf(
        ShipAPI.HullSize.FRIGATE to 10f,
        ShipAPI.HullSize.DESTROYER to 15f,
        ShipAPI.HullSize.CRUISER to 25f,
        ShipAPI.HullSize.CAPITAL_SHIP to 40f)

    var maxCrew = mapOf(
        ShipAPI.HullSize.FRIGATE to 25f,
        ShipAPI.HullSize.DESTROYER to 35f,
        ShipAPI.HullSize.CRUISER to 60f,
        ShipAPI.HullSize.CAPITAL_SHIP to 85f)

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)

        if (stats!!.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats!!.variant.removePermaMod(HullMods.AUTOMATED)
            if (stats.fleetMember.captain != null)
            {
                if (stats.fleetMember.captain.isAICore)
                {
                    Global.getSector().playerFleet.cargo.addCommodity(stats.fleetMember.captain.aiCoreId, 1f)
                }
                stats.fleetMember.captain = null
            }
        }

        stats.minCrewMod.modifyFlat("rat_core_conversion", minCrew.get(hullSize)!!)
        stats.maxCrewMod.modifyFlat("rat_core_conversion", maxCrew.get(hullSize)!!)
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        tooltip!!.addPara("A ship previously AI controlled, is now merely AI assisted. The integration of the cosmos core allows the ship to retain function of its shipsystem.", 0f)

    }

    override fun isApplicableToShip(ship: ShipAPI?): Boolean {
        return false
    }
}

