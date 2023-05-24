package assortment_of_things.modular_weapons.scripts

import assortment_of_things.misc.RATSettings
import assortment_of_things.modular_weapons.ui.WeaponCraftingUIMain
import assortment_of_things.scripts.RATBaseCampaignEventListener
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaProgressBar
import org.lazywizard.lazylib.MathUtils
import kotlin.random.Random

class WeaponComponentsListener : RATBaseCampaignEventListener() {

    override fun reportEncounterLootGenerated(plugin: FleetEncounterContextPlugin?, loot: CargoAPI?) {
        if (!RATSettings.enableModular!! && !WeaponCraftingUIMain.firstBoot!!) return

        var defeated = plugin!!.loserData.destroyedInLastEngagement

        for (member in defeated)
        {
            if (Random.nextFloat() < 0.5f) continue
            var amount = when (member.hullSpec.hullSize) {
                ShipAPI.HullSize.FRIGATE -> 2f
                ShipAPI.HullSize.DESTROYER -> 3f
                ShipAPI.HullSize.CRUISER -> 5f
                ShipAPI.HullSize.CAPITAL_SHIP -> 8f
                else -> 0f
            }
            if (member.isStation) amount = MathUtils.getRandomNumberInRange(10f, 16f)

            loot!!.addCommodity(RATItems.SALVAGED_WEAPON_COMPONENTS, amount)
        }

        Global.getSector().campaignUI.addMessage(object : BaseIntelPlugin() {
            override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
                super.createSmallDescription(info, width, height)
                info!!.addPara("Test", 0f)
            }

            override fun hasSmallDescription(): Boolean {
                return true
            }

            override fun createIntelInfo(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?) {
                info!!.addPara("Weapon Forge Report", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                info.addSpacer(5f)
                info.addPara("- Aquirred x Weapon Scrap", 0f)
                var para = info.addPara("- Aqquired a new Modifier!", 0f)
                info.addSpacer(10f)
                info.addLunaProgressBar(60f, 0f, 100f, para.computeTextWidth(para.text), 20f, Misc.getBasePlayerColor()).apply {
                    enableTransparency = true
                    changePrefix("Modifier Progress")
                    showNumber(false)
                }
            }

            override fun advance(amount: Float) {
                super.advance(amount)

                var test = ""
                var test2 = ""
            }

            override fun getName(): String {
                return "Test"
            }
        })
    }
}