package assortment_of_things

import ParallelConstruction
import assortment_of_things.campaign.RATCampaignPlugin
import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.campaign.ui.MinimapUI
import assortment_of_things.abyss.systems.MidnightCoreSystem
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.scripts.DisableTransverseScript
import assortment_of_things.abyss.scripts.ResetBackgroundScript
import assortment_of_things.misc.RATSettings
import assortment_of_things.modular_weapons.scripts.WeaponComponentsListener
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import assortment_of_things.scripts.AtMarketListener
import assortment_of_things.snippets.DropgroupTestSnippet
import assortment_of_things.snippets.ProcgenDebugSnippet
import assortment_of_things.snippets.ResetAllModularSnippet
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.util.Misc
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
        LunaDebug.addSnippet(DropgroupTestSnippet())

        LootModifier.saveOriginalData()

        ModularWeaponLoader.setOGNames()

        LunaSettings.addSettingsListener(RATSettings)
    }

    override fun onDevModeF8Reload() {
        super.onDevModeF8Reload()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        for (hullmod in Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") })
        {
            Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_alteration_install", hullmod.id), 5f)
        }
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_alteration_remover", null), 5f)

        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_automation_converter", null), 5f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_chronos_integration", null), 5f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_cosmos_integration", null), 5f)

        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_abyss_survey", null), 10f)

        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_discovery", null), 5f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_hostility", null), 5f)
        Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_instrument_supplies", null), 5f)

      /*  Global.getSector().getCharacterData().addAbility("rat_singularity_jump_ability")
        Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("\$ability:" + "rat_singularity_jump_ability", true, 0f);
        if (RATSettings.enableAbyss!! && Global.getSector().starSystems.none { it.baseName == "Midnight" })
        {
            MidnightCoreSystem().generate()
        }
        //Global.getSector().addTransientScript(DisableTransverseScript())
        Global.getSector().addScript(DisableTransverseScript())
        for (faction in Global.getSector().allFactions)
        {
            if (faction.id == AbyssUtils.FACTION_ID) continue
            faction.adjustRelationship(AbyssUtils.FACTION_ID, -100f)
        }*/

        /*var fleet = Global.getSector().playerFleet
        for (member in fleet.fleetData.membersListCopy)
        {
            if (member.variant.source != VariantSource.REFIT)
            {
                var variant = member.variant.clone()
                variant.originalVariant = null;
                variant.hullVariantId = Misc.genUID()
                variant.source = VariantSource.REFIT
                member.setVariant(variant, false, true)
            }
            member.variant.addPermaMod(HullMods.ADVANCED_TARGETING_CORE, true)
            member.updateStats()
        }
*/

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

    override fun onNewGameAfterEconomyLoad() {
        super.onNewGameAfterEconomyLoad()


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

        if (RATSettings.enableAbyss!!)
        {
            MidnightCoreSystem().generate()
            Global.getSector().getCharacterData().addAbility("rat_singularity_jump_ability")
            Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("\$ability:" + "rat_singularity_jump_ability", true, 0f);

            Global.getSector().addScript(DisableTransverseScript())
            Global.getSector().addScript(ResetBackgroundScript())
            for (faction in Global.getSector().allFactions)
            {
                if (faction.id == AbyssUtils.FACTION_ID) continue
                faction.adjustRelationship(AbyssUtils.FACTION_ID, -100f)
            }
        }
    }
}