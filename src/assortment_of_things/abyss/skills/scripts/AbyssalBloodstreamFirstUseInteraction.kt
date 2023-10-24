package assortment_of_things.abyss.skills.scripts

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.VignettePanelPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.util.Misc

class AbyssalBloodstreamFirstUseInteraction(var reason: String?) : RATInteractionPlugin() {
    override fun init() {

        var plugin = VignettePanelPlugin()
        var panel = visualPanel.showCustomPanel(Global.getSettings().screenWidth, Global.getSettings().screenHeight, plugin)
        var skillSpec = Global.getSettings().getSkillSpec("rat_abyssal_bloodstream")
        skillSpec.name = "Abyssal Requiem"

        var element = panel.createUIElement(Global.getSettings().screenWidth, Global.getSettings().screenHeight, false)
        panel.addUIElement(element)
        panel.position.inTL(0f, 0f)


        var reasonText = "As the opposing ship"
        if (reason != null) reasonText = "As the opposing $reason-class"

        textPanel.addPara("In that moment, your life was flashing before your eyes. $reasonText took its aim and its impact was about to reduce you and your ship to just atoms, your vision darkened, time came to a still, and that unique feeling came back.")

        textPanel.addPara("But this time, you weren't alone. The whole ship and its crew was pulled in to this trance. A void surrounded the ship, and by no comprehensible method, restored the ship to proper form.")

        textPanel.addPara("You felt the same resonance as before, what felt like a voice reaching closer. The hand of a loved that that wont let go. Whatever it is, its not letting its kin vanish in to nothingness.")


        textPanel.addPara("Increased Comprehension.", AbyssUtils.ABYSS_COLOR, AbyssUtils.ABYSS_COLOR)

        var tooltip = textPanel.beginTooltip()

        tooltip.setParaFont(Fonts.ORBITRON_12)
        tooltip.addPara("(Hover over the icon for a detailed description)", 0f, Misc.getGrayColor(), Misc.getGrayColor())
        var fake = Global.getFactory().createPerson()
        fake.setFaction("rat_abyssals_deep")
        fake.stats.setSkillLevel(skillSpec.id, 1f)
        tooltip.addSkillPanel(fake, 0f)

        textPanel.addTooltip()

        addLeaveOption()
    }
}