package assortment_of_things.campaign.skills.util

import com.fs.starfarer.api.characters.DescriptionSkillEffect
import com.fs.starfarer.api.impl.campaign.skills.CrewTraining
import com.fs.starfarer.api.util.Misc
import java.awt.Color

class MiscAptitudeDesc : DescriptionSkillEffect {
    override fun getTextColor(): Color {
        return Misc.getTextColor();

    }

    override fun getString(): String {
        return "Skills within this category can be aqquirred within the campaign."
    }

    override fun getHighlights(): Array<String> {
        return arrayOf("")
    }

    override fun getHighlightColors(): Array<Color> {
        val h = Misc.getHighlightColor()
        val s = Misc.getStoryOptionColor()
        return arrayOf(h, h, h, s)
    }


}