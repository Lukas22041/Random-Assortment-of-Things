package assortment_of_things.abyss.boss

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.randomAndRemove
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig
import com.fs.starfarer.api.impl.campaign.ids.Entities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.makeImportant
import java.util.*

class GenesisInteraction : RATInteractionPlugin() {
    override fun init() {

        var config = FIDConfig()

        config.leaveAlwaysAvailable = true
        config.showCommLinkOption = false
        config.showEngageText = false
        config.showFleetAttitude = false
        config.showTransponderStatus = false
        config.showWarningDialogWhenNotHostile = false
        config.alwaysAttackVsAttack = true
        config.impactsAllyReputation = true
        config.impactsEnemyReputation = false
        config.pullInAllies = false
        config.pullInEnemies = false
        config.pullInStations = false
        config.lootCredits = false

        config.firstTimeEngageOptionText = "Engage in battle"
        config.afterFirstTimeEngageOptionText = "Re-engage the threat"
        config.noSalvageLeaveOptionText = "Continue"

        config.dismissOnLeave = false
        config.printXPToDialog = true

        config.showVictoryText = false
        config.straightToEngage = false
        config.playerAttackingStation = true

        triggerDefenders(config)
    }

    override fun defeatedDefenders() {

        textPanel.addPara("Test")
        addLeaveOption()

        generateWreck()
    }

    fun generateWreck() {
        val params = DerelictShipEntityPlugin.createHull("rat_genesis_Hull", Random(), DerelictShipEntityPlugin.getDefaultSModProb())
        val entity = BaseThemeGenerator.addSalvageEntity(Random(), interactionTarget.starSystem, Entities.WRECK, Factions.NEUTRAL, params) as CustomCampaignEntityAPI

        entity.location.set(Vector2f(interactionTarget.location))
        entity.addTag(AbyssTags.ABYSS_WRECK)
        entity.makeImportant("")
    }

}