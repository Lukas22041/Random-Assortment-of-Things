package assortment_of_things.modular_weapons.scripts

import assortment_of_things.misc.RATSettings
import assortment_of_things.modular_weapons.data.ModularRepo
import assortment_of_things.modular_weapons.ui.WeaponCraftingUIMain
import assortment_of_things.scripts.RATBaseCampaignEventListener
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.BattleAPI
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.FleetEncounterContextPlugin
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDelegates.LunaMemory
import lunalib.lunaExtensions.addLunaProgressBar
import org.lazywizard.lazylib.MathUtils
import kotlin.random.Random

class WeaponComponentsListener : RATBaseCampaignEventListener() {

    var modifierProgress: Float? by LunaMemory("rat_modifier_progress", 0f)

    override fun reportBattleFinished(primaryWinner: CampaignFleetAPI?, battle: BattleAPI?) {
        super.reportBattleFinished(primaryWinner, battle)
        if (WeaponCraftingUIMain.firstBoot!!) return


        if (!battle!!.isPlayerInvolved || !primaryWinner!!.isPlayerFleet ) return

        var unlocked = ModularRepo.unlockRandom()

        if (unlocked == null) return

        Global.getSector().campaignUI.addMessage(object : BaseIntelPlugin() {

            override fun createIntelInfo(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?) {

                if (modifierProgress!! > 50f)
                {
                    info!!.addPara("Weapon Forge Report", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                    info.addSpacer(5f)
                    var para = info.addPara("- Aqquired a new Modifier!", 0f)
                    var para2 = info.addPara("- Modifier: ${unlocked!!.getName()}", 0f)
                    info.addSpacer(5f)

                    modifierProgress = 0f
                }
                else
                {
                    info!!.addPara("Weapon Forge Report", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                    info.addSpacer(5f)
                    var para = info.addPara("- Progress Towards next Modifier:", 0f)
                    info.addSpacer(2f)
                    info.addLunaProgressBar(modifierProgress!!, 0f, 50f, para.computeTextWidth(para.text), 20f, Misc.getBasePlayerColor()).apply {
                        enableTransparency = true
                        showNumber(false)
                    }
                }

            }
        })
    }

    override fun reportEncounterLootGenerated(plugin: FleetEncounterContextPlugin?, loot: CargoAPI?) {
        if (WeaponCraftingUIMain.firstBoot!!) return
        if (!plugin!!.battle.isPlayerInvolved) return

        var defeated = plugin!!.loserData.destroyedInLastEngagement + plugin.loserData.disabledInLastEngagement

        var progress = 0f
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

            modifierProgress = modifierProgress?.plus(amount)

            loot!!.addCommodity(RATItems.SALVAGED_WEAPON_COMPONENTS, amount)
        }



    }
}