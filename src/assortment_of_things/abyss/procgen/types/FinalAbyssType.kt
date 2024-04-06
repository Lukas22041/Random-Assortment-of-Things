package assortment_of_things.abyss.procgen.types

import assortment_of_things.abyss.items.cores.officer.PrimordialCore
import assortment_of_things.abyss.procgen.*
import assortment_of_things.misc.fixVariant
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.FleetAssignment
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.impl.campaign.ids.MemFlags
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.makeImportant
import java.awt.Color
import java.util.*

class FinalAbyssType : BaseAbyssType() {
    override fun getWeight() : Float{
        return 0f
    }

    override fun getTerrainFraction(): Float {
        return 0.35f
    }

    override fun pregenerate(data: AbyssSystemData) {
        var system = data.system

        data.fracturePoints.add(Vector2f())
    }

    override fun generate(data: AbyssSystemData) {
        var system = data.system
        AbyssProcgen.addAbyssParticles(system)
        AbyssProcgen.clearTerrainAround(system, Vector2f(), 10000f)

        data.system.addCustomEntity("rat_final_system_ambience", "", "rat_abyss_final_system_ambience", Factions.NEUTRAL)

        var fleet = createFleet(data)
    }

    fun createFleet(data: AbyssSystemData) : CampaignFleetAPI {
        var fleet = Global.getFactory().createEmptyFleet("rat_abyssals_primordials", "Singularity", true)
        fleet.isNoFactionInName = true

        var token = data.system.createToken(Vector2f())
        data.system.addEntity(token)

        var boss = fleet.fleetData.addFleetMember("rat_genesis_Standard")
        boss.fixVariant()

        boss.variant.addTag(Tags.TAG_NO_AUTOFIT)
        boss.variant.addTag(Tags.VARIANT_UNBOARDABLE)
        boss.variant.addTag(Tags.SHIP_LIMITED_TOOLTIP)
        boss!!.variant.addTag("rat_really_not_recoverable")

        var core = PrimordialCore().createPerson(RATItems.PRIMORDIAL, "rat_abyssals_primordials", Random())
        boss.captain = core

        fleet.addTag("rat_genesis_fleet")
        fleet.memoryWithoutUpdate.set("\$defenderFleet", fleet)

        data.system.addEntity(fleet)

        RemnantSeededFleetManager.addRemnantInteractionConfig(fleet)

        /*fleet.clearAssignments()
        fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, token, 9999999f, "Waiting")
        fleet.setLocation(token.location.x, token.location.y)
        fleet.facing = Random().nextFloat() * 360f*/
        fleet.stats.sensorProfileMod.modifyMult("rat_genesis", 3f)

        fleet.makeImportant("")

        fleet.setAI(null)
        fleet.setCircularOrbit(token, MathUtils.getRandomNumberInRange(0f, 360f), 300f, 90f)

        /*fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOLD_VS_STRONGER] = true
        *//*fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_PURSUE_PLAYER] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_ALWAYS_PURSUE] = true
        fleet.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_ALLOW_DISENGAGE] = true*//*
        fleet.memoryWithoutUpdate[MemFlags] = true*/
        fleet.memoryWithoutUpdate[MemFlags.FLEET_IGNORES_OTHER_FLEETS] = true
        fleet.memoryWithoutUpdate[MemFlags.FLEET_IGNORED_BY_OTHER_FLEETS] = true
        fleet.addScript(PrimFleetScript(fleet, token))


        return fleet
    }

    override fun setupColor(data: AbyssSystemData) {
        var h = MathUtils.getRandomNumberInRange(0.925f, 1f)
        if (Random().nextFloat() > 0.5f) h = MathUtils.getRandomNumberInRange(0.0f, 0.035f)
        var color = Color.getHSBColor(h, 1f, 1f)

        var depth = data.depth
        var s = 1f
        var b = 1f
        b = when (depth) {
            AbyssDepth.Shallow ->  0.3f
            AbyssDepth.Deep -> 0.2f
        }

        var darkColor = Color.getHSBColor(h, s, b)

        data.baseColor = color
        data.baseDarkColor = darkColor
    }

    class PrimFleetScript(var fleet: CampaignFleetAPI, var token: SectorEntityToken) : EveryFrameScript {

        override fun isDone(): Boolean {
            return false
        }

        override fun runWhilePaused(): Boolean {
            return true
        }

        override fun advance(amount: Float) {

            /*if (fleet.isAlive && !fleet.isDespawning && !fleet.memoryWithoutUpdate.contains(MemFlags.ENTITY_MISSION_IMPORTANT)) {
                fleet.makeImportant("")
            }*/

           /* if (fleet.isAlive && !fleet.isDespawning &&!fleet.isCurrentAssignment(FleetAssignment.ORBIT_PASSIVE)) {
                fleet.clearAssignments()
                fleet.addAssignment(FleetAssignment.ORBIT_PASSIVE, token, 9999999f, "Waiting")
                fleet.facing = Random().nextFloat() * 360f
            }*/
        }

    }
}