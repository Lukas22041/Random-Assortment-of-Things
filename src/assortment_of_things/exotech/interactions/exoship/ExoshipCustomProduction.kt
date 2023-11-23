package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomProductionPickerDelegate
import com.fs.starfarer.api.campaign.FactionProductionAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.impl.campaign.ids.Strings
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.addFleetMemberGainText

class ExoshipCustomProduction(var exoDialog: ExoshipInteractions) : CustomProductionPickerDelegate {
    override fun getAvailableShipHulls(): MutableSet<String> {
        return Global.getSettings().allShipHullSpecs.filter { it.hasTag("rat_exotech_bp") }.map { it.hullId }.toMutableSet()
    }

    override fun getAvailableWeapons(): MutableSet<String> {
        return Global.getSettings().allWeaponSpecs.filter { it.hasTag("rat_exotech_bp") }.map { it.weaponId }.toMutableSet()
    }

    override fun getAvailableFighters(): MutableSet<String> {
        return Global.getSettings().allFighterWingSpecs.filter { it.hasTag("rat_exotech_bp") }.map { it.id }.toMutableSet()
    }

    override fun getCostMult(): Float {
        return ExoshipInteractions.tokenConversionRatio
    }

    override fun getMaximumValue(): Float {
        return ExoUtils.getExoData().tokens
    }

    override fun withQuantityLimits(): Boolean {
        return false
    }

    override fun notifyProductionSelected(production: FactionProductionAPI?) {
        var items = production!!.current
        var data = ExoUtils.getExoData()
        var cargo = Global.getSector().playerFleet.cargo
        var fleet = Global.getSector().playerFleet

        for (item in items) {

            if (item.type == FactionProductionAPI.ProductionItemType.SHIP) {
                for (i in 0 until item.quantity) {

                    var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, item.specId + "_Hull")
                    fleet.fleetData.addFleetMember(member)
                    member.repairTracker.cr = 0.7f

                    member.fixVariant()

                    exoDialog.textPanel.addFleetMemberGainText(member)
                }
            }

            else if (item.type == FactionProductionAPI.ProductionItemType.FIGHTER){
                cargo.addFighters(item.getSpecId(), item.quantity)
                AddRemoveCommodity.addFighterGainText(item.specId, item.quantity, exoDialog.textPanel)
            } else if (item.type == FactionProductionAPI.ProductionItemType.WEAPON){
                cargo.addWeapons(item.getSpecId(), item.quantity)
                AddRemoveCommodity.addWeaponGainText(item.specId, item.quantity, exoDialog.textPanel)
            }
        }

        Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)
        var tokens = production.totalCurrentCost
        data.tokens -= tokens
        var tokenString = Misc.getWithDGS(tokens.toFloat()) + Strings.C
        exoDialog.textPanel.addPara("Lost $tokenString tokens", Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

    }

}