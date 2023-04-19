package assortment_of_things.campaign.interactions

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDelegates.LunaMemory
import kotlin.random.Random

class TrainingStationInteraction : RATInteractionPlugin() {



    override fun init() {

        var looted: Boolean? by LunaMemory("looted_skill", false, interactionTarget.memoryWithoutUpdate)
        var faction = interactionTarget.faction

        if (!looted!!)
        {
            textPanel.addPara("The fleet closes on to some kind of training facility from ${faction.displayNameWithArticle}. It seems that no other fleets are currently docked." +
                    "\n\n" +
                    "However, it seems that some of their target practice is set to defend the place, making it impossible to land without clearing them out.",
                Misc.getTextColor(), faction.baseUIColor, faction.displayNameWithArticle)

            triggerDefenders()
        }
        else
        {
            textPanel.addPara("The fleet closes on tosome kind of training facility from ${faction.displayNameWithArticle}. It seems that no other fleets are currently docked." +
                    "\n\n" +
                    "Your fleet already emptied this place out of its valueables.",
                Misc.getTextColor(), Misc.getHighlightColor(), faction.displayNameWithArticle)
            addLeaveOption()
        }

        //addLeaveOption()
    }

    override fun defeatedDefenders() {
        super.defeatedDefenders()

        clear()

        textPanel.addPara("After the defending fleet has been defeated, a expedition was send towards the facility, they did not find much of any value." +
                "\n\nThat is until they discovered a storage unit containing a chip that appears to hold information related to the training of commanders.")

        createOption("Take the chip and leave") {
            clearOptions()

            var cargo = Global.getFactory().createCargo(true)
            var seed: Long? by LunaMemory("rat_skill_seed", Misc.genRandomSeed(), interactionTarget.memoryWithoutUpdate)

            var skills = Global.getSettings().skillIds.map { Global.getSettings().getSkillSpec(it) }.filter { it.hasTag("rat_learnable") }
            var skill = skills.random(Random(seed!!))

            cargo.addSpecial(SpecialItemData("rat_skill_bp", skill.id), 1f)

            visualPanel.showLoot("Loot", cargo, true) {
                var looted: Boolean? by LunaMemory("looted_skill", false, interactionTarget.memoryWithoutUpdate)
                looted = true

                closeDialog()
            }
        }
    }

    fun explore()
    {

    }
}