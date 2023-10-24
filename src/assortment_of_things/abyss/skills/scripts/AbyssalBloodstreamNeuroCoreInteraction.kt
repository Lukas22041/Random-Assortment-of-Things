package assortment_of_things.abyss.skills.scripts

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.VignettePanelPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.util.Misc

class AbyssalBloodstreamNeuroCoreInteraction() : RATInteractionPlugin() {
    override fun init() {

        var plugin = VignettePanelPlugin()
        var panel = visualPanel.showCustomPanel(Global.getSettings().screenWidth, Global.getSettings().screenHeight, plugin)
        var skillSpec = Global.getSettings().getSkillSpec("rat_abyssal_bloodstream")

        var element = panel.createUIElement(Global.getSettings().screenWidth, Global.getSettings().screenHeight, false)
        panel.addUIElement(element)
        panel.position.inTL(0f, 0f)

        textPanel.addPara("Moments before disaster, your neural link suddenly cut. A void appears around where just moments ago you've piloted the ship.")

        textPanel.addPara("And just as quick as it came, the fog lifted with the ship in pristine conditions. You attempted to make contact with the core, but even though succesful, you hear nothing but a strong silence coming from it.")

        textPanel.addPara("Neuro-Core gained the \"Seraphim\" Skill.", AbyssUtils.ABYSS_COLOR, AbyssUtils.ABYSS_COLOR)

        var tooltip = textPanel.beginTooltip()

        tooltip.setParaFont(Fonts.ORBITRON_12)
        tooltip.addPara("(Hover over the icon for a detailed description)", 0f, Misc.getGrayColor(), Misc.getGrayColor())
        var fake = Global.getFactory().createPerson()
        fake.setFaction("rat_abyssals_deep")
        fake.stats.setSkillLevel(skillSpec.id, 1f)
        fake.stats.setSkillLevel("rat_core_seraph", 1f)
        tooltip.addSkillPanel(fake, 0f)

        textPanel.addTooltip()


        addLeaveOption()
    }
}