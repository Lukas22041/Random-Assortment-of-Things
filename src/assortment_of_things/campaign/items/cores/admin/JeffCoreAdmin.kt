package assortment_of_things.campaign.items.cores.admin

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.AICoreAdminPlugin
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.AICoreAdminPluginImpl
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.ids.Skills

class JeffCoreAdmin : AICoreAdminPlugin {
    override fun createPerson(aiCoreId: String?, factionId: String?, seed: Long): PersonAPI {
        val jeff = Global.getFactory().createPerson()
        val spec = Global.getSettings().getCommoditySpec(aiCoreId)
        jeff.setFaction(factionId)
        jeff.aiCoreId = aiCoreId
        jeff.name = FullName("Jeff", "", FullName.Gender.ANY)
        jeff.portraitSprite = spec.iconName

        jeff.rankId = null
        jeff.postId = Ranks.POST_ADMINISTRATOR

//		person.getStats().setSkillLevel(Skills.PLANETARY_OPERATIONS, 1);
//		person.getStats().setSkillLevel(Skills.SPACE_OPERATIONS, 1);

        jeff.stats.setSkillLevel(Skills.INDUSTRIAL_PLANNING, 1f)
        jeff.stats.setSkillLevel(Skills.HYPERCOGNITION, 1f)
        return jeff
    }
}