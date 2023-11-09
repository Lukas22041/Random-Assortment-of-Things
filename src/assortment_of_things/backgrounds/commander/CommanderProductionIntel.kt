package assortment_of_things.backgrounds.commander

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.comm.CommMessageAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.impl.campaign.missions.CustomProductionContract
import com.fs.starfarer.api.ui.IntelUIAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.magiclib.kotlin.getStorage
import org.magiclib.kotlin.getStorageCargo
import java.awt.Color

class CommanderProductionIntel(var market: MarketAPI, var cargo: CargoAPI, var days: Float) : BaseIntelPlugin() {

    var startTimestamp = Global.getSector().clock.timestamp
    var delivered = false
    var remove = false

    init {
        Global.getSector().addScript(this)
        isImportant = true
    }

    override fun getName(): String {
        return "${market.name}: Production Contract"
    }


    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {
        info!!.addSpacer(2f)

        if (!delivered) {
            var remainingDays = (days - Global.getSector().clock.getElapsedDaysSince(timestamp)).toInt()
            info!!.addPara("Will be delivered to ${market.name}", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "${market.name}")
            info!!.addPara("In $remainingDays days", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "$remainingDays")
        }
        else {
            info.addPara("Production finished at ${market.name}", 0f, Misc.getGrayColor(), Misc.getHighlightColor(), "${market.name}")
        }
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        info.addSpacer(3f)

        info.addPara("Ship weapons and fighters: ", 0f)
        info.addSpacer(10f)
        info.showCargo(cargo, 20, true, 0f)
        info.addSpacer(10f)

        info.addPara("Ship hulls: ", 0f)
        info.addSpacer(10f)
        info.showShips(cargo.mothballedShips.membersListCopy,20, true,
            false, 0f)
        info.addSpacer(10f)

        if (delivered) {
            info.addSpacer(10f)
            var delete = addGenericButton(info, width, "Remove Intel", "DELETE")
            delete.setShortcut(Keyboard.KEY_G, true)
        }
    }

    override fun buttonPressConfirmed(buttonId: Any?, ui: IntelUIAPI?) {
        if (buttonId == "DELETE") {
            remove = true
            ui!!.recreateIntelUI()
        }
    }

    override fun advanceImpl(amount: Float) {
        super.advanceImpl(amount)

        if (!delivered) {
            var sinceTimestamp = Global.getSector().clock.getElapsedDaysSince(timestamp)
            if (sinceTimestamp > days) {

                var marketStorage = market.getStorageCargo()
                marketStorage.addAll(cargo)

                if (cargo.mothballedShips == null) {
                    cargo.initMothballedShips(Factions.PLAYER)
                }

                for (member in cargo.mothballedShips.membersListCopy) {
                    marketStorage.mothballedShips.addFleetMember(member)
                }

                delivered = true
                Global.getSector().campaignUI.addMessage(this, CommMessageAPI.MessageClickAction.INTEL_TAB)
            }
        }

        if (delivered) {
            var sinceTimestamp = Global.getSector().clock.getElapsedDaysSince(timestamp)
            if (sinceTimestamp > days + 7) {
                remove = true
            }
        }

    }

    override fun shouldRemoveIntel(): Boolean {
        return remove
    }

    override fun reportRemovedIntel() {
        super.reportRemovedIntel()
        Global.getSector().removeScript(this)
    }


    override fun getIcon(): String {
        return "graphics/icons/intel/production_report.png"
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken? {
        return market.starSystem?.hyperspaceAnchor
    }

}