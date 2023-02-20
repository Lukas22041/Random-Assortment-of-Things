package assortment_of_things.campaign.submarkets

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.SubmarketPlugin
import com.fs.starfarer.api.campaign.econ.MonthlyReport
import com.fs.starfarer.api.campaign.econ.MonthlyReport.FDNode
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import kotlin.random.Random


class SpacersGambitBlackmarket : BaseSubmarketPlugin() {

    var random = Random(Misc.genRandomSeed())

    override fun updateCargoPrePlayerInteraction() {
        super.updateCargoPrePlayerInteraction()
        sinceLastCargoUpdate = 0f;
        if (okToUpdateShipsAndWeapons())
        {
            sinceSWUpdate = 0f;
            var weapons = Global.getSector().getFaction(Factions.PIRATES).knownWeapons
            weapons.addAll(Global.getSector().getFaction(Factions.TRITACHYON).knownWeapons)
            weapons.addAll(Global.getSector().getFaction(Factions.HEGEMONY).knownWeapons)
            weapons.addAll(Global.getSector().getFaction(Factions.PERSEAN).knownWeapons)
            weapons.addAll(Global.getSector().getFaction(Factions.DIKTAT).knownWeapons)

            var distinctWeps = weapons.distinct()

            var itemPicker = WeightedRandomPicker<String>()
            itemPicker.add(Commodities.GAMMA_CORE, 1f)
            itemPicker.add(Commodities.BETA_CORE, 0.5f)
            itemPicker.add(Commodities.ALPHA_CORE, 0.2f)
            itemPicker.add("weps", 30f)

            var specialItemPicker = WeightedRandomPicker<String>()
            specialItemPicker.add("corrupted_nanoforge", 1f)
            specialItemPicker.add("pristine_nanoforge", 0.2f)
            specialItemPicker.add("synchrotron", 0.5f)
            specialItemPicker.add("orbital_fusion_lamp", 1f)
            specialItemPicker.add("coronal_portal", 1f)
            specialItemPicker.add("mantle_bore", 1f)
            specialItemPicker.add("catalytic_core", 1f)
            specialItemPicker.add("soil_nanites", 1f)
            specialItemPicker.add("biofactory_embryo", 1f)
            specialItemPicker.add("fullerene_spool", 1f)
            specialItemPicker.add("plasma_dynamo", 1f)
            specialItemPicker.add("cryoarithmetic_engine", 0.7f)
            specialItemPicker.add("drone_replicator", 1f)
            specialItemPicker.add("dealmaker_holosuite", 1f)

            var newCargo = Global.getFactory().createCargo(true)

            for (i in 0 until random.nextInt(2,5))
            {
                newCargo.addSpecial(SpecialItemData(specialItemPicker.pick(), null), 1f)
            }

            for (i in 0 until random.nextInt(50,80))
            {
                var pick = itemPicker.pick()
                if (pick == "weps")
                {
                    newCargo.addWeapons(distinctWeps.random(), 1)
                }
                else
                {
                    newCargo.addCommodity(pick, 1f)
                }
            }

            newCargo.sort()

            setCargo(newCargo)
        }
    }

    override fun isIllegalOnSubmarket(commodityId: String?, action: SubmarketPlugin.TransferAction?): Boolean {
        return action == SubmarketPlugin.TransferAction.PLAYER_SELL
    }

    override fun isIllegalOnSubmarket(stack: CargoStackAPI?, action: SubmarketPlugin.TransferAction?): Boolean {
        return action == SubmarketPlugin.TransferAction.PLAYER_SELL
    }

    override fun isIllegalOnSubmarket(member: FleetMemberAPI?, action: SubmarketPlugin.TransferAction?): Boolean {
        return action == SubmarketPlugin.TransferAction.PLAYER_SELL
    }

    override fun getIllegalTransferText(member: FleetMemberAPI?, action: SubmarketPlugin.TransferAction?): String {
        return "This market does not accept wares."
    }

    override fun getIllegalTransferText(stack: CargoStackAPI?, action: SubmarketPlugin.TransferAction?): String {
        return "This market does not accept wares."
    }


    override fun getTariff(): Float {
        return 3f
    }

    override fun isBlackMarket(): Boolean {
        return true
    }

    override fun isParticipatesInEconomy(): Boolean {
        return false
    }

    override fun isOpenMarket(): Boolean {
        return false
    }

    override fun isMilitaryMarket(): Boolean {
        return false
    }
}