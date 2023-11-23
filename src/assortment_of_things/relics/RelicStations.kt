package assortment_of_things.relics

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickParams
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType
import com.fs.starfarer.api.impl.campaign.procgen.themes.OmegaOfficerGeneratorPlugin
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantOfficerGeneratorPlugin
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldParams
import com.fs.starfarer.api.impl.campaign.terrain.DebrisFieldTerrainPlugin.DebrisFieldSource
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import java.util.*

class RelicStations {

    //Default weight is 10f
    var stations = listOf<RelicStation>(

       /* RelicStation("rat_development_station").apply {
            systemFilter = { system -> true}
            amount = MathUtils.getRandomNumberInRange(2,3)
            weight = 1000f
        },*/

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
            systemFilter = { system -> system.planets.any { !it.isStar } }
            amount = 2
            locations = linkedMapOf(LocationType.PLANET_ORBIT to 100f)
            weight = 20f
        },

     /*   RelicStation("rat_training_station").apply {
            systemFilter = { system -> true }
            locations = linkedMapOf(LocationType.PLANET_ORBIT to 5f, LocationType.IN_ASTEROID_BELT to 5f, LocationType.STAR_ORBIT to 1f)
        },
*/
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

                    person.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_LEVEL, 7f)

                    person.setPersonality(Personalities.RECKLESS)
                    person.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_LEVEL, 7)

                    var backstory = "The file on ${person.nameString} reveals a history of high-speed notoriety. With a list of commendations for daring and initiative only matched by a list of citations for ‘race-dueling’ and reckless behaviour, ${person.nameString} seemed to be climbing towards either an Admiral position or dying in a ship explosion. Instead neither happened - during an impromptu ‘race-duel’ with a  fellow officer their reckless behaviour resulted in the death of a lesser domain dignitary, resulting in ${person.hisOrHer} sentencing to this station."
                    person.memoryWithoutUpdate.set("\$rat_prisoner_backstory", backstory)
                }

                let {
                    var person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson()
                    people.add(person)
                    person.stats.setSkillLevel("rat_maintaining_momentum", 1f)

                    person.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_LEVEL, 7f)

                    person.setPersonality(Personalities.AGGRESSIVE)
                    person.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_LEVEL, 7)

                    var backstory = "The record of ${person.nameString} shows a wild history that starts out similar to a bog standard Domain Officer, that then goes on to their capture by rebels, taking over and leading the rebels against the Domain, being captured by the Domain, and then re-earning their position in the Domain Navy. This was followed by almost non-stop, continuous combat missions, with ${person.nameString} pushing their ship with each kill. In the end this blood-thirstiness became problematic, so ${person.nameString} was put on ice until ${person.heOrShe} was needed again."
                    person.memoryWithoutUpdate.set("\$rat_prisoner_backstory", backstory)
                }

                let {
                    var person = Global.getSector().getFaction(Factions.INDEPENDENT).createRandomPerson()
                    people.add(person)
                    person.stats.setSkillLevel("rat_perfect_planning", 1f)

                    person.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_LEVEL, 7f)

                    person.setPersonality(Personalities.STEADY)
                    person.memoryWithoutUpdate.set(MemFlags.OFFICER_MAX_LEVEL, 7)

                    var backstory = "${person.nameString}’s file indicates a well-catalogued history of labyrinthine contingency measures and hard drilling of ${person.hisOrHer} crew. ${person.hisOrHer} anticipatory methods have ensured in the past that their ship has not only weathered staggering damage, but simultaneously improved in combat performance. ${person.nameString}’s rampant paranoia was their drawback - they were consigned to this station for killing several command staff ${person.heOrShe} were convinced were out to get them."
                    person.memoryWithoutUpdate.set("\$rat_prisoner_backstory", backstory)
                }

                it.memoryWithoutUpdate.set("\$rat_prisoners", people)
            }
        },

        RelicStation("rat_gravitational_dynamo").apply {
            systemFilter = { system -> system.planets.any { it.typeId == "black_hole" } && system.planets.filter { !it.isStar }.size >= 2 }
            weight = 1000f

            postGeneration = { station ->
                var blackhole = station.containingLocation.planets.find { it.typeId == "black_hole" }
                station.setCircularOrbit(blackhole, MathUtils.getRandomNumberInRange(0f, 360f), blackhole!!.radius + station.radius + 250f, 120f)

                var fleet = Global.getFactory().createEmptyFleet(Factions.REMNANTS, "Automated Defenses", false)
                fleet.isNoFactionInName = true

                var variants = listOf("facet_Attack", "facet_Shieldbreaker")

                for (variant in variants) {
                    var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variant)
                    fleet.fleetData.addFleetMember(member)

                    member.repairTracker.cr = member.repairTracker.maxCR
                    member.variant.addTag(Tags.VARIANT_DO_NOT_DROP_AI_CORE_FROM_CAPTAIN)
                    member.variant.addTag(Tags.SHIP_LIMITED_TOOLTIP)
                }

                var officerGenerator = OmegaOfficerGeneratorPlugin()
                officerGenerator.addCommanderAndOfficers(fleet, null, Random())

                station.memoryWithoutUpdate.set("\$defenderFleet", fleet)

                var planets = station.containingLocation.planets.filter { !it.isStar }
                for (planet in planets) {
                    var market = planet.market ?: continue
                    market.addCondition("rat_grav_dynamo_condition")
                    var condition = market.getFirstCondition("rat_grav_dynamo_condition")
                    condition.isSurveyed = true
                }
            }
        },


        RelicStation("rat_damaged_cryosleeper").apply {
            systemFilter = { system -> system.planets.any { !it.isStar } && system.hasTag(Tags.THEME_DERELICT)}
            locations = linkedMapOf(LocationType.PLANET_ORBIT to 10f)

            postGeneration = {station ->
                var fleet = Global.getFactory().createEmptyFleet(Factions.DERELICT, "Automated Defenses", false)

                for (pick in fleet.faction.pickShip(ShipRoles.COMBAT_CAPITAL, ShipPickParams.all(), null, Random())) {
                    fleet.fleetData.addFleetMember(pick.variantId)
                }

                val plugin = Misc.getAICoreOfficerPlugin(Commodities.ALPHA_CORE)
                val person = plugin.createPerson(Commodities.ALPHA_CORE, fleet.faction.id, Random())

                person.portraitSprite = fleet.faction.createRandomPerson().portraitSprite
                fleet.commander = person
                fleet.flagship.captain = person
                RemnantOfficerGeneratorPlugin.integrateAndAdaptCoreForAIFleet(fleet.flagship)

                for (member in fleet.fleetData.membersListCopy) {
                    member.repairTracker.cr = member.repairTracker.maxCR
                }

                station.memoryWithoutUpdate.set("\$defenderFleet", fleet)

                val params = DebrisFieldParams(300f,  // field radius - should not go above 1000 for performance reasons
                        -1f,  // density, visual - affects number of debris pieces
                        10000000f,  // duration in days
                        0f) // days the field will keep generating glowing pieces


                params.source = DebrisFieldSource.GEN
                val debris = Misc.addDebrisField(station.getContainingLocation(), params, Random())
                debris.setCircularOrbit(station, 0f, 0f, 100f);

            }
        },

       /* RelicStation("rat_exo_cache").apply {
            systemFilter = { system -> system.hasBlackHole() }
            locations = linkedMapOf(LocationType.NEAR_STAR to 10f, LocationType.PLANET_ORBIT to 0.1f)
            weight = 1000f
        },*/



      /*  RelicStation("rat_event_generator").apply {
            systemFilter = { system -> system.hasBlackHole() }
            locations = linkedMapOf(LocationType.STAR_ORBIT to 100f)

            postGeneration = { entity ->
                var system = entity.containingLocation
                var plugin = entity.customPlugin as EventGeneratorEntity

                var blackhole = system.planets.find { it.typeId == "black_hole" }

                var terrain = system.addRingBand(blackhole, "misc", "rings_asteroids0", 256f, 3, Color(50, 50, 50),
                    150f, blackhole!!.radius + 2000f, 200f, Terrain.RING, "Event Horizon Generator") as CampaignTerrainAPI
                plugin.ring = terrain

                var listener = GeneratorInputListener()
                Global.getSector().listenerManager.addListener(listener)
                plugin.inputManager = listener
            }
        },*/
    )
}