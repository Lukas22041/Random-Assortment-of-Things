package assortment_of_things.frontiers.submarkets

import assortment_of_things.frontiers.FrontiersUtils
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.SubmarketPlugin
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Submarkets
import com.fs.starfarer.api.impl.campaign.submarkets.StoragePlugin
import org.magiclib.kotlin.getStorageCargo

class SettlementStoragePlugin : StoragePlugin() {

    override fun getCargo(): CargoAPI {
        var cargo = super.getCargo()
        var data = FrontiersUtils.getSettlementData()
        var primStorage = data.primaryPlanet.market?.getStorageCargo()
        if (primStorage != null) {
            (data.primaryPlanet.market.getSubmarket(Submarkets.SUBMARKET_STORAGE).plugin as StoragePlugin).setPlayerPaidToUnlock(true)

                primStorage.addAll(cargo)

                cargo.initMothballedShips(Factions.PLAYER)
                for (ship in cargo.mothballedShips.membersListCopy) {
                    primStorage.mothballedShips.addFleetMember(ship)
                }

                cargo.mothballedShips.clear()
                cargo.clear()
            cargo = primStorage
        }
        return cargo
    }

    override fun isIllegalOnSubmarket(commodityId: String?, action: SubmarketPlugin.TransferAction?): Boolean {
        return false
    }

    override fun isIllegalOnSubmarket(member: FleetMemberAPI?, action: SubmarketPlugin.TransferAction?): Boolean {
        return false
    }

    override fun isIllegalOnSubmarket(stack: CargoStackAPI?, action: SubmarketPlugin.TransferAction?): Boolean {
        return false
    }

}