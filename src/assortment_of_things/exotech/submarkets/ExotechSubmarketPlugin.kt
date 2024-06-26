package assortment_of_things.exotech.submarkets

import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.SubmarketPlugin.OnClickAction
import com.fs.starfarer.api.campaign.SubmarketPlugin.PlayerEconomyImpactMode
import com.fs.starfarer.api.campaign.econ.CommodityOnMarketAPI
import com.fs.starfarer.api.campaign.econ.SubmarketAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.submarkets.BaseSubmarketPlugin
import com.fs.starfarer.api.loading.WeaponSpecAPI
import com.fs.starfarer.api.util.WeightedRandomPicker
import com.fs.starfarer.loading.specs.FighterWingSpec
import org.lazywizard.lazylib.MathUtils
import java.util.*

class ExotechSubmarketPlugin : BaseSubmarketPlugin(), EveryFrameScript {
    override fun init(submarket: SubmarketAPI?) {
        super.init(submarket)
        market.primaryEntity.addScript(this)
    }

    override fun updateCargoPrePlayerInteraction() {
        val seconds = Global.getSector().clock.convertToSeconds(sinceLastCargoUpdate)
        sinceLastCargoUpdate = 0f
        if (okToUpdateShipsAndWeapons()) {

            if (cargo == null) {
                cargo = Global.getFactory().createCargo(true)
            }

            sinceSWUpdate = 0f
            cargo.clear()
            cargo.initMothballedShips("rat_exotech")
            cargo.mothballedShips.clear()

            addAlterations()
            addEquipment()
            addExoShips()
        }
        getCargo().sort()
    }


    override fun isDone(): Boolean {
        return false
    }


    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {
        super.advance(amount)
    }

    fun addAlterations() {
        var count = MathUtils.getRandomNumberInRange(3, 6)

        var hmods = Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") && it.hasTag("exo") }

        var weights = WeightedRandomPicker<String>()
        for (hmod in hmods) {
            weights.add(hmod.id, hmod.rarity)
        }

        for (i in 0 until count) {
            cargo.addSpecial(SpecialItemData("rat_alteration_install", weights.pick()), 1f)
        }
    }

    fun addEquipment() {

        var count = MathUtils.getRandomNumberInRange(20, 30)

        var weights = WeightedRandomPicker<Any>()

        weights.add(Global.getSettings().getFighterWingSpec("rat_protostar_wing"), 0.5f)
        weights.add(Global.getSettings().getFighterWingSpec("rat_nightblade_wing"), 0.5f)
        weights.add(Global.getSettings().getFighterWingSpec("rat_dawnblade_wing"), 0.25f)

        weights.add(Global.getSettings().getWeaponSpec("rat_hyper_dart"), 1f)
        weights.add(Global.getSettings().getWeaponSpec("rat_hyper_javelin"), 0.5f)
        weights.add(Global.getSettings().getWeaponSpec("rat_p_wave_launcher"), 1f)
        weights.add(Global.getSettings().getWeaponSpec("rat_starburst"), 1f)

        for (i in 0 until count) {
            var pick = weights.pick()
            if (pick is FighterWingSpec) {
                cargo.addFighters(pick.id, 1)
            }
            if (pick is WeaponSpecAPI) {
                cargo.addWeapons(pick.weaponId, 1)
            }
        }
    }



    fun addExoShips() {
        val params = FleetParamsV3(null,
            submarket.market.locationInHyperspace,
            "rat_exotech",
            5f,
            FleetTypes.PATROL_MEDIUM,
            140f,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            5f,  // utilityPts
            5f // qualityMod
        )
        params.random = Random()
        params.withOfficers = false
        params.forceAllowPhaseShipsEtc = true

        val fleet = FleetFactoryV3.createFleet(params)
        fleet.cargo.addCrew(1000)
        for (member in fleet.fleetData.membersListCopy) {
            cargo.mothballedShips.addFleetMember(member)
            member.repairTracker.cr = 0.7f
        }
    }

    override fun getName(): String? {
        return submarket.spec.name
    }

    override fun getStockpileLimit(com: CommodityOnMarketAPI?): Int {
        return 9999
    }



    //Lock Items
    override fun isIllegalOnSubmarket(commodityId: String, action: SubmarketPlugin.TransferAction): Boolean {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL) return true
        return !ExoUtils.getExoData().canBuyItems
    }

    override fun isIllegalOnSubmarket(stack: CargoStackAPI, action: SubmarketPlugin.TransferAction): Boolean {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL) return true
        return !ExoUtils.getExoData().canBuyItems
    }

    override fun getIllegalTransferText(stack: CargoStackAPI, action: SubmarketPlugin.TransferAction): String? {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL) {
            return "You can not sell commodities to Amelie"
        }
        else if (!ExoUtils.getExoData().canBuyItems) {
            return "Amelie needs more influence to be able to transfer items to you."
        }
        return ""
    }

    private fun getRequiredLevelAssumingLegal(stack: CargoStackAPI, action: SubmarketPlugin.TransferAction): RepLevel? {
        return RepLevel.VENGEFUL
    }

    private fun getRequiredLevelAssumingLegal(commodityId: String, action: SubmarketPlugin.TransferAction): RepLevel? {
        return RepLevel.VENGEFUL
    }



    //Lock Ships
    override fun isIllegalOnSubmarket(member: FleetMemberAPI, action: SubmarketPlugin.TransferAction): Boolean {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL) return true
        return !ExoUtils.getExoData().canBuyShips
    }

    override fun getIllegalTransferText(member: FleetMemberAPI, action: SubmarketPlugin.TransferAction): String? {
        if (action == SubmarketPlugin.TransferAction.PLAYER_SELL) {
            return "You can not sell ships to Amelie"
        }
        else if (!ExoUtils.getExoData().canBuyItems) {
            return "Amelie needs more influence to be able to transfer ships to you."
        }
        return ""
    }

    private fun getRequiredLevelAssumingLegal(member: FleetMemberAPI, action: SubmarketPlugin.TransferAction): RepLevel? {
        return RepLevel.VENGEFUL
    }



    override fun isEnabled(ui: CoreUIAPI): Boolean {
        return true
    }

    override fun getOnClickAction(ui: CoreUIAPI?): OnClickAction? {
        return OnClickAction.OPEN_SUBMARKET
    }

    override fun getPlayerEconomyImpactMode(): PlayerEconomyImpactMode? {
        return PlayerEconomyImpactMode.NONE
    }

    override fun isMilitaryMarket(): Boolean {
        return true
    }
}