package assortment_of_things.abyss.hullmods

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

class AIConvertedShip : BaseHullMod() {

    override fun applyEffectsBeforeShipCreation(hullSize: ShipAPI.HullSize?, stats: MutableShipStatsAPI?, id: String?) {
        super.applyEffectsBeforeShipCreation(hullSize, stats, id)


        if (!stats!!.variant.hasHullMod(HullMods.AUTOMATED))
        {
            stats.variant.addPermaMod(HullMods.AUTOMATED)
            stats.variant.addTag("no_auto_penalty")
            stats.fleetMember.captain = null
        }
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)

    }

    override fun shouldAddDescriptionToTooltip(hullSize: ShipAPI.HullSize?, ship: ShipAPI?,  isForModSpec: Boolean): Boolean {
        return false
    }

    override fun addPostDescriptionSection(tooltip: TooltipMakerAPI?, hullSize: ShipAPI.HullSize?, ship: ShipAPI?, width: Float, isForModSpec: Boolean) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)

        tooltip!!.addPara("This ship was previously crewed by humans, but has now been converted in to a hull exclusively controlled by AI. \n\n" +
                "This specific conversion is able to negate the combat readiness penality from the \"Automated Ship\" Hullmod.", 0f)

    }
}

