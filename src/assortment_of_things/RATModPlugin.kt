package assortment_of_things

import ParallelConstruction
import assortment_of_things.campaign.RATCampaignPlugin
import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.campaign.skills.util.SkillManager
import assortment_of_things.modular_weapons.data.RATModifieableProjectileWeaponSpec
import assortment_of_things.misc.RATSettings
import assortment_of_things.modular_weapons.data.RATModifieableProjectileSpec
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import assortment_of_things.snippets.ProcgenDebugSnippet
import assortment_of_things.snippets.ResetAllModularSnippet
import assortment_of_things.strings.RATTags
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.DamageType
import com.fs.starfarer.api.combat.WeaponAPI
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import lunalib.lunaDebug.LunaDebug
import lunalib.lunaExtensions.getSystemsWithTag
import java.awt.Color


class RATModPlugin : BaseModPlugin() {

    companion object {
        var added = false
    }

    override fun onApplicationLoad() {
        super.onApplicationLoad()

        if (Global.getSettings().modManager.isModEnabled("parallel_construction"))
        {
            throw Exception("\"Parallel Construction\" as a mod is included in \"Random Assortment of Things\" and should be disabled/removed to avoid issues.")
        }


        LunaDebug.addSnippet(ProcgenDebugSnippet())
        LunaDebug.addSnippet(ResetAllModularSnippet())

        LootModifier.saveOriginalData()

        ModularWeaponLoader.setOGNames()

    }

    override fun onDevModeF8Reload() {
        super.onDevModeF8Reload()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

        RATSettings.loadSettings()

        Global.getSector().listenerManager.addListener(RATSettings, true)
        Global.getSector().registerPlugin(RATCampaignPlugin())
        Global.getSector().addTransientScript(ParallelConstruction())

        LootModifier.modifySpawns()

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

        SkillManager.update()

        //ModularWeaponLoader.resetAllData()
        ModularWeaponLoader.applyStatToSpecsForAll()

        if (!Global.getSector().characterData.abilities.contains("rat_weapon_forge") && RATSettings.enableModular!!)
        {
            Global.getSector().getCharacterData().addAbility("rat_weapon_forge")
            Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("\$ability:" + "rat_weapon_forge", true, 0f);
        }
    }

    override fun onNewGame() {
        super.onNewGame()
    }

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()

        var chirality = Global.getSector().getFaction("chirality")
        chirality?.adjustRelationship("player", -1f)

        for (system in Global.getSector().getSystemsWithTag(RATTags.THEME_CHIRAL_COPY))
        {
            for (entity in system.customEntities)
            {
                if (entity.customEntityType == Entities.INACTIVE_GATE)
                {
                    system.removeEntity(entity)
                }
            }
        }

        for (system in Global.getSector().getSystemsWithTag(RATTags.THEME_CHIRAL_MAIN))
        {
            for (entity in system.customEntities)
            {
                if (entity.customEntityType == Entities.INACTIVE_GATE)
                {
                    system.removeEntity(entity)
                }
            }
        }
    }
}