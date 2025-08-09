package assortment_of_things.exotech.intel.missions

import assortment_of_things.misc.fixVariant
import assortment_of_things.misc.getExoData
import assortment_of_things.misc.loadTextureCached
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.comm.CommMessageAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ArrowData
import com.fs.starfarer.api.characters.PersonAPI
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.AICoreOfficerPluginImpl
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.*
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.HullMods
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
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

class RapidResponseIntel() : BaseIntelPlugin() {


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
        var name = "Rapid Response"
        return name
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        if (!Global.getSector().isPaused && !finished) {
            interval.advance(amount)
            if (interval.intervalElapsed() && !hasCarriers()) {
                finished = true
                Global.getSector().getExoData().finishedCurrentMission = true
                Global.getSector().getExoData().finishedRapidMission = true
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
            info.addPara("Disable the fleet fielding the prototype fighter", 0f, tc, Misc.getHighlightColor(), "prototype fighter")
        }
        else {
            info.addPara("The mission has been completed. Return to Xander", 0f, tc, Misc.getHighlightColor())
        }

    }

    fun hasCarriers() : Boolean {
        return fleet.fleetData.membersListCopy.any { it.isCarrier }
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {
        info!!.addSpacer(10f)


        if (!finished) {

            info.addPara("You have been tasked with disabling an autonomous fleet fielding a new and experimental fighter. \n\n" +
                    "The fleet is within the ${fleet.starSystem.nameWithNoType} starsystem. ${getOrbitLocationDescription()}.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "disabling an autonomous", "${fleet.starSystem.nameWithNoType}")

            info.addSpacer(10f)

            info.addPara("Displayed below is an overview of the fleet.", 0f)

            var shipsInList = 999
            var shipListCount = 0
            var previewList = ArrayList<FleetMemberAPI>()
            for (member in fleet.fleetData.membersListCopy) {
                if (shipListCount >= shipsInList) break
                shipListCount += 1

                previewList.add(member)
            }

            info.addSpacer(10f)

            info.addShipList(6, 3, 48f, Misc.getBasePlayerColor(), previewList, 0f)

        } else {
            info.addPara("The missions objective has been fullfilled, return to Xander to finish it.", 0f)
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
        var path = "graphics/icons/intel/missions/rat_rapid_response.png"
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

        fleet = Global.getFactory().createEmptyFleet("rat_exotech", "Autonomous Response Unit", true)
        fleet.name = "Autonomous Response Unit"
        fleet.isNoFactionInName = true

        fleet.makeImportant("")

        data class spawnData(var variantId: String, var recoverable: Boolean)


        var variants = ArrayList<spawnData>()
        variants.add(spawnData("rat_leanira_Rapid_Response", true))

        variants.add(spawnData("rat_thestia_Rapid_Response", false))
        variants.add(spawnData("rat_thestia_Rapid_Response", false))
        variants.add(spawnData("rat_thestia_Rapid_Response", false))

        variants.add(spawnData("rat_hypatia_Strike", false))
        variants.add(spawnData("rat_hypatia_Strike", false))

        variants.add(spawnData("rat_hypatia_Stardust", true))
        variants.add(spawnData("rat_hypatia_Stardust", false))
        variants.add(spawnData("rat_hypatia_Stardust", false))

        variants.add(spawnData("rat_hypatia_Moonlight", true))
        variants.add(spawnData("rat_hypatia_Moonlight", false))
        variants.add(spawnData("rat_hypatia_Moonlight", false))

        for ((variantId, recoverable) in variants) {

            var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, variantId)
            member.fixVariant()

            var variant = member.variant

            variant.addTag(Tags.TAG_NO_AUTOFIT)

            variant.addPermaMod(HullMods.AUTOMATED)
            variant.addTag(Tags.TAG_AUTOMATED_NO_PENALTY)
            variant.addPermaMod("rat_exo_experimental")

            if (recoverable) {
                variant.addTag(Tags.VARIANT_ALWAYS_RECOVERABLE)
                variant.addTag(Tags.SHIP_RECOVERABLE)
                variant.addTag(Tags.VARIANT_CONSISTENT_WEAPON_DROPS)
            } else {
                variant.addTag(Tags.VARIANT_UNBOARDABLE)
                variant.addTag(Tags.UNRECOVERABLE)
            }

            var core: PersonAPI? = null
            if (variantId == "rat_leanira_Rapid_Response") core = AICoreOfficerPluginImpl().createPerson(Commodities.ALPHA_CORE, "rat_exotech", Random())
            else if (variantId == "rat_thestia_Rapid_Response") core = AICoreOfficerPluginImpl().createPerson(Commodities.BETA_CORE, "rat_exotech", Random())
            else core = AICoreOfficerPluginImpl().createPerson(Commodities.GAMMA_CORE, "rat_exotech", Random())

            member.captain = core

            fleet.fleetData.addFleetMember(member)


        }



        setLocationForFleet()


        for (member in fleet.fleetData.membersListCopy) {
            member.repairTracker.cr = 0.7f
        }
        fleet.inflateIfNeeded()
        fleet.ai = null

        fleet.memoryWithoutUpdate.set(MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS, true)
        fleet.memoryWithoutUpdate.set(MemFlags.FLEET_IGNORES_OTHER_FLEETS, true)
        fleet.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true)
        fleet.memoryWithoutUpdate.set(MemFlags.FLEET_FIGHT_TO_THE_LAST, true)

        fleet.memoryWithoutUpdate.set(MemFlags.MEMORY_KEY_NO_REP_IMPACT, true)

        fleet.addTag("rat_rapid_response_fleet")
        fleet.memoryWithoutUpdate.set("\$rat_rapid_response_fleet", true)

        return fleet
    }

}

