package assortment_of_things.relics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType
import org.lazywizard.lazylib.MathUtils

class RelicStations {

    //Default weight is 10f
    var stations = listOf<RelicStation>(

        RelicStation("rat_development_station").apply {
            systemFilter = { system -> true}
            amount = MathUtils.getRandomNumberInRange(3,4)
            weight = 1000f
        },

        //Skill Stations
        RelicStation("rat_bioengineering_station").apply {
            systemFilter = { system -> true}
            locations.put(LocationType.NEAR_STAR, 1f)
        },

        RelicStation("rat_augmentation_station").apply {
            systemFilter = { system -> true }
            locations.put(LocationType.NEAR_STAR, 1f)
        },

        RelicStation("rat_neural_laboratory").apply {
            systemFilter = { system -> true }
            locations.put(LocationType.NEAR_STAR, 1f)
        },


        //Misc
        RelicStation("rat_orbital_construction_station").apply {
            systemFilter = { system -> system.planets.size >= 3 }
            amount = 2
            locations = linkedMapOf(LocationType.PLANET_ORBIT to 100f)
            weight = 20f
        },

        RelicStation("rat_refurbishment_station").apply {
            amount = 2
            systemFilter = { system -> true }
        },

        RelicStation("rat_spatial_laboratory").apply {
            systemFilter = { system -> system.jumpPoints.isNotEmpty() }
            locations = linkedMapOf(LocationType.JUMP_ORBIT to 10f)
        },

        RelicStation("rat_medical_laboratory").apply {
            systemFilter = { system -> system.planets.any { !it.isStar }}
            locations = linkedMapOf(LocationType.PLANET_ORBIT to 10f)
        },

        RelicStation("rat_cryochamber").apply {
            systemFilter = { system -> true }
            weight = 100f
            postGeneration = {
                var people = ArrayList<PersonAPI>()

                let {
                    var person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson()
                    people.add(person)
                    person.stats.setSkillLevel("rat_maverick", 1f)

                    person.setPersonality(Personalities.RECKLESS)

                    var backstory = "The file on ${person.nameString} reveals a history of high-speed notoriety. With a list of commendations for daring and initiative only matched by a list of citations for ‘race-dueling’ and reckless behaviour, ${person.nameString} seemed to be climbing towards either an Admiral position or dying in a ship explosion. Instead neither happened - during an impromptu ‘race-duel’ with a  fellow officer their reckless behaviour resulted in the death of a lesser domain dignitary, resulting in ${person.hisOrHer} sentencing to this station."
                    person.memoryWithoutUpdate.set("\$rat_prisoner_backstory", backstory)
                }

                let {
                    var person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson()
                    people.add(person)
                    person.stats.setSkillLevel("rat_maintaining_momentum", 1f)

                    person.setPersonality(Personalities.AGGRESSIVE)

                    var backstory = "The record of ${person.nameString} shows a wild history that starts out similar to a bog standard Domain Officer, that then goes on to their capture by rebels, taking over and leading the rebels against the Domain, being captured by the Domain, and then re-earning their position in the Domain Navy. This was followed by almost non-stop, continuous combat missions, with ${person.nameString} pushing their ship with each kill. In the end this blood-thirstiness became problematic, so ${person.nameString} was put on ice until ${person.heOrShe} was needed again."
                    person.memoryWithoutUpdate.set("\$rat_prisoner_backstory", backstory)
                }

                let {
                    var person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson()
                    people.add(person)
                    person.stats.setSkillLevel("rat_perfect_planning", 1f)

                    person.setPersonality(Personalities.STEADY)

                    var backstory = "${person.nameString}’s file indicates a well-catalogued history of labyrinthine contingency measures and hard drilling of ${person.hisOrHer} crew. ${person.hisOrHer} anticipatory methods have ensured in the past that their ship has not only weathered staggering damage, but simultaneously improved in combat performance. ${person.nameString}’s rampant paranoia was their drawback - they were consigned to this station for killing several command staff ${person.heOrShe} were convinced were out to get them."
                    person.memoryWithoutUpdate.set("\$rat_prisoner_backstory", backstory)
                }

                it.memoryWithoutUpdate.set("\$rat_prisoners", people)
            }
        },
    )
}