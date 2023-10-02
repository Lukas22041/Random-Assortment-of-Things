package assortment_of_things.relics.bar

import assortment_of_things.misc.RATSettings
import assortment_of_things.relics.RelicsUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.campaign.rules.MemoryAPI
import com.fs.starfarer.api.characters.FullName
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Ranks
import com.fs.starfarer.api.impl.campaign.intel.bar.events.BaseBarEvent
import com.fs.starfarer.api.util.Misc

class RelicOfThePastBarEvent : BaseBarEvent() {


    var gateSystem: StarSystemAPI? = null
    var finished = false
    var accepted = false

    override fun shouldShowAtMarket(market: MarketAPI?): Boolean {

        if (accepted) return false
        if (market == null) return false
        if (market.isHidden) return false
        if (market.factionId == Factions.PIRATES) return false

        var existingIntel = Global.getSector().intelManager.getIntel(ArchivistIntel::class.java) as MutableList<ArchivistIntel>
        var entities = RelicsUtils.getAllRelicStations()
        entities = entities.filter { it.isDiscoverable && !it.isExpired }
        entities = entities.filter { entity -> existingIntel.none { other -> other.entity == entity } }
        if (entities.isNotEmpty()) {
            return true
        }
        return false

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

        var person: PersonAPI? = Global.getSector().importantPeople.getPerson("rat_archivist_person")
        var known = person?.memoryWithoutUpdate?.getBoolean("\$rat_knownByPlayer") ?: false

        if (known) {
            dialog!!.textPanel.addPara("At the edge of the bar you spot a familiar archivist.")
        }
        else {
            dialog!!.textPanel.addPara("An archivist is excitedly looking for a spacer with time to spare.")
        }

        dialog!!.optionPanel.addOption("Talk to the archivist", this)

    }

    override fun init(dialog: InteractionDialogAPI?, memoryMap: MutableMap<String, MemoryAPI>?) {
        super.init(dialog, memoryMap)

        var person = Global.getSector().importantPeople.getPerson("rat_archivist_person") as PersonAPI?
        if (person == null) {
            person = Global.getFactory().createPerson()
            person!!.id = "rat_archivist_person"
            person!!.setFaction(Factions.INDEPENDENT)
            person!!.rankId = "rat_archivist"
            person!!.postId = "scientist"
            person!!.name = FullName("Sydney", "", FullName.Gender.FEMALE)
            person!!.portraitSprite = "graphics/portraits/portrait16.png"
            Global.getSector().importantPeople.addPerson(person)
        }

        var known = person.memoryWithoutUpdate.getBoolean("\$rat_knownByPlayer") ?: false

        dialog!!.visualPanel.showPersonInfo(person)
      //  dialog.visualPanel.showMapMarker(Global.getSector().starSystems.random().hyperspaceAnchor, "Desination: ", Misc.getHighlightColor(), false, "", "", setOf())

        dialog!!.optionPanel.clearOptions()

        if (known) {
            text.addPara("You approach ${person.nameString}, she seems excited to see you.")

            text.addPara("\"Glad to see you again ${Global.getSector().characterData.honorific}, and you are here just at the right moment in time. " +
                    "I've picked up another rumor that could help me complete my archive of history. Would you be available to investigate this one for me?\"")

        } else {
            text.addPara("You approach the young archivist, upon seeing you she can barely hold the excitement. ")

            text.addPara("\"Lets make it quick, i'm in the process of documenting the history of several long depopulated starsystems. " +
                    "I've come across a rumor with some large implications for the field, but i want to confirm the validity of those before going on with my research.")

            text.addPara("This is where you come in to play, i want you to confirm the existance of the rumors subject for me. " +
                    "This profession isn't one with much credits to spare, so i can't put a reward on it, but i can promise you that if this finding is real, it will be worth for both you and me.\"")

        }


        dialog.optionPanel.addOption("Accept", "ACCEPT")
        dialog.optionPanel.addOption("Decline", "LEAVE")

    }


    override fun optionSelected(optionText: String?, optionData: Any?) {
        super.optionSelected(optionText, optionData)

        var person: PersonAPI? = Global.getSector().importantPeople.getPerson("rat_archivist_person")

        if (optionData is String)
        {
            if (optionData == "LEAVE")
            {
                finished = true
                text.addPara("Thats dissapointing, however if you were to change your mind, i'l still be here.")
            }

            var known = person?.memoryWithoutUpdate?.getBoolean("\$rat_knownByPlayer") ?: false



            if (optionData == "ACCEPT") {


                dialog.optionPanel.clearOptions()
                var existingIntel = Global.getSector().intelManager.getIntel(ArchivistIntel::class.java) as MutableList<ArchivistIntel>
                var entities = RelicsUtils.getAllRelicStations()

                entities = entities.filter { it.isDiscoverable && !it.isExpired }
                entities = entities.filter { entity -> existingIntel.none { other -> other.entity == entity } }

                var entity = entities.random()


                if (known) {
                    text.addPara("\"Great! The rumor i want to confirm is the existance of an old ${entity.name} that is located somewhere in the ${entity.starSystem.nameWithNoType} system. " +
                            "Confirming the rumors validity would be a gateway in to learning much more about the history of this system.",
                        Misc.getTextColor(), Misc.getHighlightColor(), "${entity.name}", "${entity.starSystem.nameWithNoType}")
                }
                else {
                    text.addPara("\"Thank you! The rumor i want to confirm is the existance of an old ${entity.name} that is located somewhere in the ${entity.starSystem.nameWithNoType} system. " +
                            "Confirming the rumors validity would be a gateway in to learning much more about the history of this system.",
                        Misc.getTextColor(), Misc.getHighlightColor(), "${entity.name}", "${entity.starSystem.nameWithNoType}")
                }

                text.addPara("Since there are no previous records of it, so it would be quite likely that none or few salvagers have yet set foot on it. " +
                        "I dont care about what happens with the structure, aslong as you send me the data i want.\"")

                dialog.visualPanel.showMapMarker(entity, "Desination: ${entity.starSystem}", Misc.getHighlightColor(), false, "", "", setOf())

                var intel = ArchivistIntel(entity, person!!)
                Global.getSector().addScript(intel)
                Global.getSector().intelManager.addIntel(intel)
                Global.getSector().intelManager.addIntelToTextPanel(intel, text)
                intel.isImportant = true

                person.memoryWithoutUpdate.set("\$rat_knownByPlayer", true)

                options.addOption("Show specification of the target", "SPECIFICATION")

                finished = true
                accepted = true

            }

            if (optionData == "SPECIFICATION") {
                text.addPara("Test")
                options.removeOption("SPECIFICATION")
            }
        }
    }
}