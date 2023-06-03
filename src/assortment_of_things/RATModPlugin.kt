package assortment_of_things

import ParallelConstruction
import assortment_of_things.campaign.RATCampaignPlugin
import assortment_of_things.campaign.procgen.LootModifier
import assortment_of_things.campaign.skills.util.SkillManager
import assortment_of_things.campaign.ui.MinimapUI
import assortment_of_things.misc.RATSettings
import assortment_of_things.modular_weapons.scripts.WeaponComponentsListener
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import assortment_of_things.snippets.ProcgenDebugSnippet
import assortment_of_things.snippets.ResetAllModularSnippet
import assortment_of_things.strings.RATTags
import com.fs.starfarer.api.BaseModPlugin
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.campaign.BaseLocation
import com.fs.starfarer.campaign.CampaignEngine
import lunalib.lunaDebug.LunaDebug
import lunalib.lunaExtensions.getSystemsWithTag
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

        LunaSettings.addListener(RATSettings)
    }

    override fun onDevModeF8Reload() {
        super.onDevModeF8Reload()
    }

    override fun onGameLoad(newGame: Boolean) {
        super.onGameLoad(newGame)

       // Global.getSector().addTransientScript(SystemTooltipReplacer())

        if (RATSettings.enableMinimap!!){
            Global.getSector().addTransientScript(MinimapUI())
        }

        LootModifier.modifySpawns()

        Global.getSector().registerPlugin(RATCampaignPlugin())
        Global.getSector().addTransientScript(ParallelConstruction())

        Global.getSector().addTransientListener(WeaponComponentsListener())

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


        var location = BaseLocation(true)
        location.backgroundTextureFilename = "graphics/backgrounds/pocket_bg.jpg"

        location.objects.listener = CampaignEngine.getInstance()
        location.addScript(object : EveryFrameScript {
            override fun isDone(): Boolean {
                return false
            }

            override fun runWhilePaused(): Boolean {
                return true
            }

            override fun advance(amount: Float) {

            }

        })

      /*  var token = location.createToken(0f, 0f)
        location.addEntity(token)

        location.addPlanet("Test", token, "place", Planets.PLANET_TERRAN, 300f, 300f, 0f, 200f)

        Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, Global.getSector().playerFleet, JumpPointAPI.JumpDestination(token, ""))*/

     /*   var system = Global.getSector().createStarSystem("Test")
        var star = system.initStar("Test", StarTypes.ORANGE, 800f, 200f)

        var planet = system.addPlanet("Test2", star, "Test Planet", Planets.PLANET_TERRAN, 0f, 700f, 2000f, 90f)
        system.autogenerateHyperspaceJumpPoints(true, true)

        ReflectionUtils.set("hyperspaceMode", system, true)*/
    }

    override fun onNewGame() {
        super.onNewGame()
        ModularWeaponLoader.applyStatToSpecsForAll()

    }

    override fun onNewGameAfterTimePass() {
        super.onNewGameAfterTimePass()

        if (RATSettings.enableModular!!)
        {
            Global.getSector().getCharacterData().addAbility("rat_weapon_forge")
            Global.getSector().getCharacterData().getMemoryWithoutUpdate().set("\$ability:" + "rat_weapon_forge", true, 0f);
        }

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