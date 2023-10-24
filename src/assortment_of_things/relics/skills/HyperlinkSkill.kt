package assortment_of_things.relics.skills

import activators.ActivatorManager
import assortment_of_things.abyss.skills.AbyssalBloodstream
import assortment_of_things.abyss.skills.scripts.AbyssalBloodstreamCampaignScript
import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.relics.activators.HyperlinkActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreOfficerPlugin
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc

class HyperlinkSkill : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {
        info!!.addSpacer(2f)

        info.addPara("This core is able to connect towards the human targets brainwaves. This allows it to perform communication with them in combat. Through this, if both the targets ship and this cores ship are deployed at the same time, they can swap control without delay.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())

        info.addSpacer(5f)

        info.addPara("The cooldown is 2.5/5/10/15 seconds based on the size of the ship that was switched to.", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())


        info!!.addSpacer(2f)

    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        var ship = stats!!.entity
        if (ship is ShipAPI) {
            if (!ship.hasListenerOfClass(HyperlinkScript::class.java)) {
                ship.addListener(HyperlinkScript())
            }

            var script = Global.getSector().scripts.find { it::class.java == AbyssalBloodstreamCampaignScript::class.java } as AbyssalBloodstreamCampaignScript?

            if (script != null && script.shownFirstDialog) {
                var listener = AbyssalBloodstream.AbyssalBloodstreamListener(ship)
                ship.addListener(listener)
                Global.getCombatEngine().addLayeredRenderingPlugin(listener)
            }
        }
    }

    override fun unapply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?) {

    }
}

class HyperlinkScript() : AdvanceableListener {

    var applied = false

    override fun advance(amount: Float) {

        if (!applied) {
            var engine = Global.getCombatEngine()
            var player = Global.getSector().playerPerson
            var playership = engine.ships.find { it.fleetMember?.captain != null && it.fleetMember?.captain == player } ?: return
            var aiship = engine.ships.find { it.fleetMember?.captain != null && it.fleetMember.captain.isAICore && it.fleetMember.captain.aiCoreId == "rat_neuro_core" } ?: return

            ActivatorManager.addActivator(playership, HyperlinkActivator(playership, aiship))
            ActivatorManager.addActivator(aiship, HyperlinkActivator(aiship, playership))

            applied = true
        }

    }
}