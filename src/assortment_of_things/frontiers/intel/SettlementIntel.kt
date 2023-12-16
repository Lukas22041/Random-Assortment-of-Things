package assortment_of_things.frontiers.intel

import assortment_of_things.frontiers.SettlementData
import assortment_of_things.misc.RATSettings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class SettlementIntel(var settlement: SettlementData) : BaseIntelPlugin() {


    init {
        isImportant = true
    }

    var shownFirst = false

    override fun getName(): String? {
        return "Settlement"
    }

    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {

        if (mode == IntelInfoPlugin.ListInfoMode.MESSAGES && shownFirst) {
            //Production result announcement here
            info!!.addSpacer(2f)
            info!!.addPara("Production Report",
                0f, Misc.getHighlightColor(), Misc.getHighlightColor(), "")
        }
        else {
            info!!.addSpacer(2f)
            info!!.addPara("In ${settlement.primaryPlanet.starSystem.nameWithNoType}, on ${settlement.primaryPlanet.name}.",
                0f, Misc.getGrayColor(), Misc.getHighlightColor(), "${settlement.primaryPlanet.starSystem.nameWithNoType}", "${settlement.primaryPlanet.name}")
            shownFirst = true
        }

    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info!!.addSpacer(10f)

        var income = settlement.stats.income.modifiedValue * RATSettings.frontiersIncomeMult!!
        var incomeString = Misc.getDGSCredits(income)
        info.addPara("The settlement is located on ${settlement.primaryPlanet.name} within the ${settlement.primaryPlanet.starSystem.nameWithNoType} system. " +
                "It makes an income of $incomeString credits per month.", 0f,
        Misc.getTextColor(), Misc.getHighlightColor(),
            "${settlement.primaryPlanet.name}", "${settlement.primaryPlanet.starSystem.nameWithNoType}", "$incomeString")

        var cargo = settlement.previousMonthsProduction

        info.addSpacer(10f)
        info.addSectionHeading("Facilities", Alignment.MID, 0f)
        info.addSpacer(10f)

        for (slot in settlement.facilitySlots.filter { it.facilityID != "" }) {
            var text = slot.getPlugin()!!.getName()
            if (slot.isBuilding) {
                slot.updateDays()
                text += "\nBuilding, ${slot.daysRemaining.toInt()} days remaining"
            }
            var img = info.beginImageWithText(slot.getPlugin()!!.getIcon(), 32f)
            img.addPara(text, 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${slot.getPlugin()!!.getName()}")
            info.addImageWithText(0f)
            info.addSpacer(3f)
        }

        info.addSpacer(10f)
        info.addSectionHeading("Last Months Production", Alignment.MID, 0f)
        info.addSpacer(10f)

        info.addPara("Ship weapons and fighters: ", 0f)
        info.addSpacer(10f)
        info.showCargo(cargo, 20, true, 0f)
        info.addSpacer(10f)

        info.addPara("Ship hulls: ", 0f)
        info.addSpacer(10f)
        info.showShips(cargo.mothballedShips.membersListCopy,20, true,
            false, 0f)
        info.addSpacer(10f)

        var count = 0
        for (order in settlement.productionOrders) {
            count++
            var sinceTimestamp = Global.getSector().clock.getElapsedDaysSince(order.timestamp)
            var left = order.days - sinceTimestamp
            info.addSpacer(10f)
            info.addSectionHeading("Order #$count - ${left.toInt()} days remaining", Alignment.MID, 0f)
            info.addSpacer(10f)

            info.addPara("Ship weapons and fighters: ", 0f)
            info.addSpacer(10f)
            info.showCargo(order.cargo, 20, true, 0f)
            info.addSpacer(10f)

            info.addPara("Ship hulls: ", 0f)
            info.addSpacer(10f)
            info.showShips(order.cargo.mothballedShips.membersListCopy,20, true,
                false, 0f)
            info.addSpacer(10f)
        }
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        val tags: MutableSet<String> = LinkedHashSet()
        tags.add("Personal")
        return tags
    }

    override fun getIcon(): String {
        return "graphics/icons/intel/rat_settlement.png"
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken {
        return settlement.primaryPlanet
    }

}