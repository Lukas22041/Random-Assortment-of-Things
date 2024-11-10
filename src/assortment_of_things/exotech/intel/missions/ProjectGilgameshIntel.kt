package assortment_of_things.exotech.intel.missions

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.misc.baseOrModSpec
import assortment_of_things.misc.fixVariant
import assortment_of_things.misc.getExoData
import assortment_of_things.misc.loadTextureCached
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.comm.CommMessageAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.FleetTypes
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Personalities
import com.fs.starfarer.api.impl.campaign.ids.Skills
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator
import com.fs.starfarer.api.impl.campaign.procgen.themes.BaseThemeGenerator.LocationType
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.makeImportant
import java.awt.Color
import java.util.*
import kotlin.collections.LinkedHashSet

class ProjectGilgameshIntel() : BaseIntelPlugin() {


    var fleet: CampaignFleetAPI
    var finished = false
    var interval = IntervalUtil(0.1f, 0.1f)

    init {
        isImportant = true
        fleet = spawnFleet()
        Global.getSector().addScript(this)
    }

    override fun notifyEnded() {
        super.notifyEnded()
        Global.getSector().removeScript(this)
    }

    override fun getName(): String? {
        var name = "Project Gilgamesh"
        return name
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        if (!Global.getSector().isPaused && !finished) {
            interval.advance(amount)
            if (interval.intervalElapsed() && !hasFleetGilgamesh()) {
                finished = true
                Global.getSector().getExoData().finishedCurrentMission = true
                Global.getSector().getExoData().finishedGilgameshMission = true
                Global.getSector().campaignUI.addMessage(this, CommMessageAPI.MessageClickAction.INTEL_TAB)

              /*  fleet.ai = Global.getFactory().createFleetAI(fleet)

                var base = fleet.starSystem.

                fleet.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN, )*/
            }
        }
    }

    override fun addBulletPoints(info: TooltipMakerAPI?, mode: IntelInfoPlugin.ListInfoMode?, isUpdate: Boolean, tc: Color?, initPad: Float) {

        info!!.addSpacer(2f)


        if (!finished) {
            info.addPara("Recover or destroy the stolen prototype", 0f, tc, Misc.getHighlightColor(), "stolen prototype")
        }
        else {
            info.addPara("The mission has been completed. Return to Xander", 0f, tc, Misc.getHighlightColor())
        }

    }

    fun hasFleetGilgamesh() : Boolean {
        return fleet.fleetData.membersListCopy.any { it.baseOrModSpec().baseHullId == "rat_gilgamesh" }
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info!!.addSpacer(10f)


        if (!finished) {

            info.addPara("You have been tasked to prevent a fleet from transfering a stolen exotech prototype. " +
                    "The fleet is within the ${fleet.starSystem.nameWithNoType} starsystem. ${getOrbitLocationDescription()}.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "prevent a fleet from transfering a stolen exotech prototype.")

            info.addSpacer(10f)

            info.addPara("Below is an overview of the fleet, but it is likely missing many of their ships", 0f)

            var shipsInList = 7
            var shipListCount = 0
            var previewList = ArrayList<FleetMemberAPI>()
            for (member in fleet.fleetData.membersListCopy) {
                if (shipListCount >= shipsInList) break
                shipListCount += 1

                previewList.add(member)
            }

            info.addSpacer(10f)

            info.addShipList(6, 2, 48f, Misc.getBasePlayerColor(), previewList, 0f)

        } else {
            info.addPara("The mission has been fullfilled, return to Xander to finish it.", 0f)
        }

    }

    override fun getArrowData(map: SectorMapAPI?): MutableList<IntelInfoPlugin.ArrowData> {
        var list = mutableListOf<ArrowData>()
        return list
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        val tags: MutableSet<String> = LinkedHashSet()
        tags.add("rat_exotech")
        tags.add(Tags.INTEL_MISSIONS)
        return tags
    }

    override fun getIcon(): String {
        var path = "graphics/icons/intel/missions/rat_project_gilgamesh.png"
        Global.getSettings().loadTextureCached(path)
        return path
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken? {
        if (finished) return null
        return fleet.starSystem?.center
    }

    fun getOrbitLocationDescription() : String {
        var focus = fleet.orbitFocus
        var name = "The fleet's location within the system is unknown"

        if (focus is PlanetAPI) {
            if (focus.isStar) {
                name = "The fleet is orbiting somewhere around ${focus.name}. How close to it however is unknown."
            }
            else {
                name = "The fleet is suspected to be orbiting a local planet"
            }
        }

        else if (focus is CampaignTerrainAPI) {
            name = "The fleet is suspected to be hiding within an asteroid belt"
        }

        return name
    }

    fun setLocationForFleet() {
        var systems = Global.getSector().starSystems.filter { it != Global.getSector().playerFleet.containingLocation && !it.hasTag(Tags.THEME_CORE) && !it.hasTag(Tags.THEME_REMNANT) && !it.hasPulsar() && !it.hasTag(
            Tags.THEME_HIDDEN)}

        var filtered = systems.filter { system ->
            system.planets.none { Global.getSector().economy.marketsCopy.contains(it.market) }
        }

        var system = filtered.randomOrNull()

        var locationTypes = linkedMapOf<LocationType, Float>(LocationType.PLANET_ORBIT to 5f, LocationType.IN_ASTEROID_BELT to 1f, LocationType.STAR_ORBIT to 0.2f)
        var locations = BaseThemeGenerator.getLocations(Random(), system, fleet.radius + 50, locationTypes)
        var location = locations.pick()

        system!!.addEntity(fleet)
        fleet.orbit = location.orbit
    }

    fun spawnFleet() : CampaignFleetAPI {

        fleet = Global.getFactory().createEmptyFleet(Factions.PIRATES, "Raiders", true)

        fleet.makeImportant("")

        var officer = Global.getSector().getFaction(Factions.PIRATES).createRandomPerson()
        officer.setPersonality(Personalities.AGGRESSIVE) //Make the Gilgamesh less afraid


        officer.stats.level = 7
        officer.stats.setSkillLevel(Skills.HELMSMANSHIP, 2f)
        officer.stats.setSkillLevel(Skills.COMBAT_ENDURANCE, 2f)
        officer.stats.setSkillLevel(Skills.IMPACT_MITIGATION, 2f)
        officer.stats.setSkillLevel(Skills.DAMAGE_CONTROL, 2f)
        officer.stats.setSkillLevel(Skills.FIELD_MODULATION, 2f)
        officer.stats.setSkillLevel(Skills.GUNNERY_IMPLANTS, 2f)
        officer.stats.setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2f)


        var gilgamesh = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_gilgamesh_Attack")
        gilgamesh.fixVariant()
        gilgamesh.variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE)

        gilgamesh.captain = officer

        fleet.fleetData.addFleetMember(gilgamesh)

        setLocationForFleet()


        val params = FleetParamsV3(null,
            fleet.locationInHyperspace,
            Factions.PIRATES,
            5f,
            FleetTypes.PATROL_MEDIUM,
            120f,  // combatPts
            0f,  // freighterPts
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            0f // qualityMod
        )

        var secondFleet = FleetFactoryV3.createFleet(params)
        for (member in secondFleet.fleetData.membersListCopy) {
            fleet.fleetData.addFleetMember(member)
        }

        for (member in fleet.fleetData.membersListCopy) {
            member.repairTracker.cr = 0.7f
        }
        fleet.inflateIfNeeded()
        fleet.ai = null

        fleet.memoryWithoutUpdate.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true)
        fleet.memoryWithoutUpdate.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true)
        fleet.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true)
        fleet.memoryWithoutUpdate.set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true)


        return fleet
    }

}
