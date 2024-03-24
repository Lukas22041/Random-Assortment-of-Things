package assortment_of_things.abyss.bar

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.intel.map.AbyssMap
import assortment_of_things.relics.RelicsUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent
import com.fs.starfarer.api.util.Misc

class AbyssHintBarEvent : BaseBarEvent() {

    var finished = false

    override fun shouldShowAtMarket(market: MarketAPI?): Boolean {

        if (AbyssUtils.getAbyssData().systemsData.isEmpty()) return false
        if (market == null) return false
        if (market.isHidden) return false
        if (market.factionId == Factions.PIRATES) return false
        if (Global.getSector().intelManager.hasIntelOfClass(AbyssHintIntel::class.java)) return false
        if (Global.getSector().intelManager.hasIntelOfClass(AbyssMap::class.java)) return false

        return true
    }



    override fun isDialogFinished(): Boolean {

        if (finished)  {
            finished = false
            return true
        }
        return false
    }

    override fun addPromptAndOption(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.addPromptAndOption(dialog, memoryMap)

        dialog!!.textPanel.addPara("A researcher of hyperspace phenonema can be spotted analysing his data.")

        dialog!!.optionPanel.addOption("Talk with the hyperspace researcher", this)

    }

    override fun init(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.init(dialog, memoryMap)

        var person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson()

        dialog!!.visualPanel.showPersonInfo(person)
        dialog!!.optionPanel.clearOptions()

        text.addPara("You innitiate a friendly discussion with the researcher, offering a drink in exchange for possible intel.")

        text.addPara("\"I'm not quite sure if i can give much practical advice to a trained captain such as yourself, " +
                "the topography of the persean sectors hyperspace is a standard element of the officers academic lectures afterall.")

        text.addPara("Though, i could share some of my fascination of a specific region of hyperspace that isn't discussed quite as much, though i doubt it would be of much worth for you. \"")

        dialog.optionPanel.addOption("Continue", "CONTINUE")

    }


    override fun optionSelected(optionText: String?, optionData: Any?) {
        super.optionSelected(optionText, optionData)


        if (optionData is String)
        {

            if (optionData == "CONTINUE") {

                dialog.optionPanel.clearOptions()

                text.addPara("\"Within the south-western corner of the sector, deep within the persean abyss, there is a unique formation of hyperspatial matter that hasnt been discovered in any other colonised sector. \n\n" +
                        "Not to much is known of it nowadays, but its been said that during the colonisation of this sector, some kind of exotic matter has been discovered, causing an economic rush for exploration and extraction of resources in its proximity.")

                text.addPara("The space is said to be rather dangerous, but it was nothing that couldn't be handled with the assistance of gates. And there lies the issue, with the collapse, its been almost impossible to reach that far in to the persean abyss, even less so to explore it in detail, making it quasi isolated from the sector since.\"")


                dialog.optionPanel.addOption("Continue", "CONTINUE2")

            }

            if (optionData == "CONTINUE2") {

                dialog.optionPanel.clearOptions()

                text.addPara("\"So while it fascinates me, i dont think we will learn much more of it within my lifetime, as only barely sane captains would venture in to the depths and hope to return in one piece.\"")

                var intel = AbyssHintIntel()
                Global.getSector().addScript(intel)
                Global.getSector().intelManager.addIntel(intel)
                Global.getSector().intelManager.addIntelToTextPanel(intel, text)

                finished = true

            }
        }
    }
}