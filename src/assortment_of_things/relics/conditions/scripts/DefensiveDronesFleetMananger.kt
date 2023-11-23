package assortment_of_things.relics.conditions.scripts

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FactionDoctrineAPI
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.enc.EncounterManager
import com.fs.starfarer.api.impl.campaign.enc.EncounterPoint
import com.fs.starfarer.api.impl.campaign.enc.EncounterPointProvider
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager
import org.lazywizard.lazylib.MathUtils
import java.util.*

class DefensiveDronesFleetMananger(source: SectorEntityToken?, thresholdLY: Float, respawnDelay: Float, minFleets: Int, maxFleets: Int) :

    SourceBasedFleetManager(source, thresholdLY, minFleets, maxFleets, respawnDelay) {
    inner class RemnantSystemEPGenerator : EncounterPointProvider {
        override fun generateEncounterPoints(where: LocationAPI): List<EncounterPoint>? {
            if (!where.isHyperspace) return null
            if (totalLost > 0 && source != null) {
                val id = "ep_" + source.id
                val ep = EncounterPoint(id, where, source.locationInHyperspace, EncounterManager.EP_TYPE_OUTSIDE_SYSTEM)
                ep.custom = this
                val result: MutableList<EncounterPoint> = ArrayList()
                result.add(ep)
                return result //source.getContainingLocation().getName()
            }
            return null
        }
    }

    var totalLost = 0

    var launchDelayDays = 3
    var delayTimestamp: Long? = null

    @Transient
    protected var epGen: RemnantSystemEPGenerator? = null
    protected fun readResolve(): Any {
        return this
    }

    @Transient
    protected var addedListener = false
    override fun advance(amount: Float) {
        if (!addedListener) {
            //totalLost = 1;
            /* best code ever -dgb
			if (Global.getSector().getPlayerPerson() != null && 
					Global.getSector().getPlayerPerson().getNameString().equals("Dave Salvage") &&
					Global.getSector().getClock().getDay() == 15 &&
							Global.getSector().getClock().getMonth() == 12 && 
							Global.getSector().getClock().getCycle() == 206) {
				totalLost = 0;
			}*/
            // global listener needs to be not this class since SourceBasedFleetManager
            // adds it to all fleets as their event listener
            // and so you'd get reportFleetDespawnedToListener() called multiple times
            // from global listeners, and from fleet ones
            epGen = RemnantSystemEPGenerator()
            Global.getSector().listenerManager.addListener(epGen, true)
            addedListener = true
        }
        super.advance(amount)
    }

    override fun spawnFleet(): CampaignFleetAPI? {
        if (source == null) return null
        if (source.market == null) return null
        if (source.market.size == 1) return null
        if (source.market.faction == null) return null

        if (delayTimestamp != null) {

            if (Global.getSector().clock.getElapsedDaysSince(delayTimestamp!!) > launchDelayDays) {
                delayTimestamp = null
            }
            else {
                return null
            }

        }

        var market = source.market

        var basePoints = MathUtils.getRandomNumberInRange(30f, 40f)
        var modifier = market.stats.dynamic.getMod(Stats.COMBAT_FLEET_SIZE_MULT).computeEffective(0f)

        var points = basePoints * modifier
        points = MathUtils.clamp(points, 30f, 150f)

        val random = Random()

        var type = FleetTypes.PATROL_SMALL
        if (points > 80) type = FleetTypes.PATROL_MEDIUM
        if (points > 140) type = FleetTypes.PATROL_LARGE

        val params = FleetParamsV3(source.market,
            source.locationInHyperspace,
            Factions.DERELICT,
            3f,
            type,
            points,  // combatPts
            0f,  // freighterPts 
            0f,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            0f,  // utilityPts
            0f // qualityMod
        )

        params.random = random
        params.maxShipSize = 3

        val fleet = FleetFactoryV3.createFleet(params) ?: return null
        val location = source.containingLocation
        location.addEntity(fleet)

        initDerelictProperties(random, fleet, false)

        fleet.setLocation(source.location.x, source.location.y)
        fleet.facing = random.nextFloat() * 360f
        fleet.addScript(RemnantAssignmentAI(fleet, source.containingLocation as StarSystemAPI, source))
        fleet.memoryWithoutUpdate["\$sourceId"] = source.id

        fleet.setFaction(market.factionId)
        fleet.name = "Defense Drones"

        delayTimestamp = Global.getSector().clock.timestamp
        launchDelayDays = MathUtils.getRandomNumberInRange(2, 4)

        return fleet
    }

    fun initDerelictProperties(random: Random?, fleet: CampaignFleetAPI, dormant: Boolean) {
        var random = random
        if (random == null) random = Random()
        fleet.removeAbility(Abilities.EMERGENCY_BURN)
        fleet.removeAbility(Abilities.SENSOR_BURST)
        fleet.removeAbility(Abilities.GO_DARK)

        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_PATROL_FLEET] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_ALLOW_LONG_PURSUIT] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER] = true

        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_JUMP] = true

        RemnantSeededFleetManager.addRemnantInteractionConfig(fleet)
        val salvageSeed = random.nextLong()
        fleet.memoryWithoutUpdate[MemFlags.SALVAGE_SEED] = salvageSeed
    }

    override fun reportFleetDespawnedToListener(fleet: CampaignFleetAPI?, reason: FleetDespawnReason?, param: Any?) {
        super.reportFleetDespawnedToListener(fleet, reason, param)
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            val sid = fleet!!.memoryWithoutUpdate.getString("\$sourceId")
            if (sid != null && source != null && sid == source.id) {
                //if (sid != null && sid.equals(source.getId())) {
                totalLost++
            }
        }
    }
}