package assortment_of_things

import ParallelConstruction
import assortment_of_things.campaign.RATCampaignPlugin
import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.campaign.ui.MinimapUI
import assortment_of_things.abyss.MidnightCoreSystem
import assortment_of_things.abyss.AbyssShielding
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.RATSettings
import assortment_of_things.modular_weapons.scripts.WeaponComponentsListener
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import assortment_of_things.scripts.AtMarketListener
import assortment_of_things.snippets.ProcgenDebugSnippet
import assortment_of_things.snippets.ResetAllModularSnippet
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.campaign.CampaignEngine
import lunalib.lunaDebug.LunaDebug
import lunalib.lunaSettings.LunaSettings


class RATModPlugin : BaseModPlugin() {

    companion object {
        var added = false
    }

    override fun onApplicationLoad() {
        super.onApplicationLoad()

        LunaDebug.addSnippet(ProcgenDebugSnippet())
        LunaDebug.addSnippet(ResetAllModularSnippet())

        LootModifier.saveOriginalData()

        ModularWeaponLoader.setOGNames()

        LunaSettings.addSettingsListener(RATSettings)
    }

    override fun onDevModeF8Reload() {
        super.onDevModeF8Reload()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        /*for (hullmod in Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") })
        {
            Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_install", hullmod.id), 5f)
        }
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_remover", null), 5f)

        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_abyss_survey", null), 5f)

        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_discovery", null), 5f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_hostility", null), 5f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_supplies", null), 5f)

        Global.getSector().getCharacterData().addAbility("rat_singularity_jump_ability")
        Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("\$ability:" + "rat_singularity_jump_ability", true, 0f);
        if (RATSettings.enableAbyss!! && Global.getSector().starSystems.none { it.baseName == "Midnight" })
        {
            MidnightCoreSystem().generate()
        }*/

        //Global.getSector().addTransientScript(AbyssShielding())

        // Global.getSector().intelManager.addIntel(AbyssMap())

        // Global.getSector().addTransientScript(SystemTooltipReplacer())

        if (RATSettings.enableMinimap!!){
            Global.getSector().addTransientScript(MinimapUI())
        }

        LootModifier.modifySpawns()

        Global.getSector().registerPlugin(RATCampaignPlugin())
        Global.getSector().addTransientScript(ParallelConstruction())

        Global.getSector().addTransientListener(WeaponComponentsListener())

        LootModifier.modifySpawns()

        Global.getSector().addTransientListener(AtMarketListener())

        if (RATSettings.disableHelp!!)
        {
            try {
                CampaignEngine.getInstance().campaignHelp.isEnabled = false
            }
            catch (e: Throwable)
            {
                Global.getLogger(this.javaClass).error("Failed to disable Help Popups.")
            }
        }


        //ModularWeaponLoader.resetAllData()
        ModularWeaponLoader.applyStatToSpecsForAll()




    }

    override fun onNewGame() {
        super.onNewGame()
        ModularWeaponLoader.applyStatToSpecsForAll()

    }

    override fun beforeGameSave() {
        super.beforeGameSave()

        for (system in AbyssUtils.getAllAbyssSystems())
        {
            var abyssPlugin = AbyssUtils.getAbyssTerrainPlugin(system)
            if (abyssPlugin != null)
            {
                abyssPlugin.save()
            }
            var superchargedAbyss = AbyssUtils.getSuperchargedAbyssTerrainPlugin(system)
            if (superchargedAbyss != null)
            {
                superchargedAbyss.save()
            }
        }
    }

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()

        if (RATSettings.enableModular!!)
        {
            Global.getSector().getCharacterData().addAbility("rat_weapon_forge")
            Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("\$ability:" + "rat_weapon_forge", true, 0f);
        }

    }
}