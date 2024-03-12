package assortment_of_things.relics.skills

import assortment_of_things.campaign.skills.RATBaseShipSkill
import assortment_of_things.relics.activators.HyperlinkActivator
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.LevelBasedEffect
import com.fs.starfarer.api.characters.MutableCharacterStatsAPI
import com.fs.starfarer.api.characters.SkillSpecAPI
import com.fs.starfarer.api.combat.MutableShipStatsAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.listeners.AdvanceableListener
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.subsystems.MagicSubsystemsManager

class HyperlinkSkill : RATBaseShipSkill() {

    override fun getScopeDescription(): LevelBasedEffect.ScopeDescription {
        return LevelBasedEffect.ScopeDescription.PILOTED_SHIP
    }

    override fun createCustomDescription(stats: MutableCharacterStatsAPI?, skill: SkillSpecAPI?, info: TooltipMakerAPI?, width: Float) {

        info!!.addPara("This core is able to connect towards the human targets brainwaves. " +
                "This allows it to perform communication with them in combat. " +
                "Through this, if both the flagship and the ship that this core pilots are deployed at the same time, they can swap control without delay.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "flagship", "ship that this core pilots")

        info.addSpacer(5f)

        info.addPara("The cooldown is 3/5/10/15 seconds based on the size of the ship that was switched to.",
            0f, Misc.getTextColor(), Misc.getHighlightColor(), "3", "5", "10", "15")


        info!!.addSpacer(2f)

    }

    override fun apply(stats: MutableShipStatsAPI?, hullSize: ShipAPI.HullSize?, id: String?, level: Float) {
        var ship = stats!!.entity
        if (ship is ShipAPI) {
            if (!ship.hasListenerOfClass(HyperlinkScript::class.java)) {
                ship.addListener(HyperlinkScript())
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

            MagicSubsystemsManager.addSubsystemToShip(playership, HyperlinkActivator(playership, aiship))
            MagicSubsystemsManager.addSubsystemToShip(aiship, HyperlinkActivator(aiship, playership))

            applied = true
        }

    }
}