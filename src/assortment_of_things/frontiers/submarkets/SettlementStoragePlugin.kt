package assortment_of_things.frontiers.submarkets

import assortment_of_things.frontiers.FrontiersUtils
import com.fs.starfarer.api.campaign.CargoAPI
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
            if (!cargo.isEmpty) {
                primStorage.addAll(cargo)
                cargo.clear()
            }
            cargo = primStorage
        }
        return cargo
    }

}