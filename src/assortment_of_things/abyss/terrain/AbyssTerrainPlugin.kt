package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.MiscReplacement
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin
import assortment_of_things.abyss.terrain.terrain_copy.OldHyperspaceTerrainPlugin.CellStateTracker
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.abilities.EmergencyBurnAbility
import com.fs.starfarer.api.impl.campaign.ids.Abilities
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*
import kotlin.math.min

class AbyssTerrainPlugin : BaseTerrain() {

    var data = AbyssUtils.getData()
    var manager = AbyssUtils.getBiomeManager()

    override fun containsEntity(other: SectorEntityToken?): Boolean {
        return true
    }

    override fun containsPoint(point: Vector2f?, radius: Float): Boolean {
        return true
    }

    override fun getRenderRange(): Float {
        return 10000000f
    }

    var layer = EnumSet.of(CampaignEngineLayers.ABOVE)
    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return layer
    }


    fun isAnyInCloud(entityToken: SectorEntityToken) : Boolean {
        for (biome in manager.biomes) {
            if (biome.terrain is BaseFogTerrain) {
                if ((biome.terrain as BaseFogTerrain).isInClouds(entityToken)) {
                    return true
                }
            }
        }

        return false
    }

    fun getAnyTilePreferStorm(loc: Vector2f, radius: Float) : Pair<CellStateTracker?, BaseFogTerrain> {
        var any: Pair<CellStateTracker?, BaseFogTerrain>? = null
        for (biome in manager.biomes) {
            if (biome.terrain is BaseFogTerrain) {

                var tile = (biome.terrain as BaseFogTerrain).getTilePreferStorm(loc, radius)
                var cell: CellStateTracker? = null
                if (tile != null) {
                    cell = (biome.terrain as BaseFogTerrain).activeCells.get(tile[0]).get(tile[1])
                }

                if (cell?.isStorming == true) {
                    return Pair(cell, biome.terrain as BaseFogTerrain)
                }

                any = Pair(cell, biome.terrain as BaseFogTerrain) //Return non storming
            }
        }

        return any!!
    }

    override fun getEffectCategory(): String {
        return "rat_abyss_terrain"
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {
        if (entity !is CampaignFleetAPI) return
        var fleet = entity

        var isInclouds = isAnyInCloud(entity)
        if (isInclouds) {

            var cell = getAnyTilePreferStorm(fleet.location, fleet.radius)

            if (fleet.isPlayerFleet) {
                if (!fleet.hasScriptOfClass(SpeedBoostScript::class.java)) {
                    fleet.addScript(SpeedBoostScript(
                        fleet,this))
                }
            } else {
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_1", "In abyssal fog", 1.5f, fleet.stats.fleetwideMaxBurnMod)
                fleet.stats.addTemporaryModMult(0.1f, this.modId + "fog_2", "In abyssal fog", 1.5f, fleet.stats.accelerationMult)
            }

            if (cell.first != null && cell.first!!.isSignaling && cell.first!!.signal < 0.2f) {
                cell.first!!.signal = 0f
            }

            var goDark = Global.getSector().playerFleet.getAbility(Abilities.GO_DARK)
            var goDarkActive = false

            if (goDark != null) {
                goDarkActive = goDark.isActive
            }

            if (cell.first?.isStorming == true && !MiscReplacement.isSlowMoving(fleet) && !goDarkActive) {
                //cell.second.applyStormStrikes(cell.first, fleet, days)
               applyStormStrikes(cell.first!!, fleet, days)
            }
        }

    }

    override fun getTerrainName(): String {
        var name = ""
        var player = Global.getSector().playerFleet

        var dominant = manager.getDominantBiome()

        val inCloud = isAnyInCloud(player)
        var cell = getAnyTilePreferStorm(player.location, player.radius)

        name = dominant.getDisplayName()

        if (cell.first?.isStorming == true) name += " (Storm)"
        else if (inCloud) name += " (Fog)"

        return name
    }


    override fun hasTooltip(): Boolean {
        return true
    }

    override fun isTooltipExpandable(): Boolean {
        return false
    }

    override fun getNameColor(): Color {
        var dominant = manager.getDominantBiome()
        //return manager.getCurrentTooltipColor()
        return dominant.getTooltipColor()
    }

    override fun createTooltip(tooltip: TooltipMakerAPI, expanded: Boolean) {
        var player = Global.getSector().playerFleet

        var dominant = manager.getDominantBiome()

        var name = dominant.getDisplayName()

        val inCloud = isAnyInCloud(player)
        var cell = getAnyTilePreferStorm(player.location, player.radius)
        var isStorming = cell.first?.isStorming == true

        tooltip.addTitle(name)
        tooltip.addSpacer(10f)

        //Fog & Storm stuff here

        dominant.addBiomeTooltip(tooltip)
        tooltip.addSpacer(10f)

        if (inCloud)
        {
            tooltip.addSpacer(5f)
            tooltip.addSectionHeading("Abyssal Fog", Alignment.MID, 0f)
            tooltip.addSpacer(5f)
            tooltip!!.addPara("The charged particles within the fog combine with the exhaust of the fleet's engines, increasing the burn speed by 50%%" +
                    "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "burn speed", "50%")
        }

        if (isStorming)
        {
            tooltip.addSpacer(5f)
            tooltip.addSectionHeading("Abyssal Storm", Alignment.MID, 0f)
            tooltip.addSpacer(5f)
            tooltip!!.addPara("Abyssal Storms may damage members of the fleet if it is moving through it at high speeds."
                , 0f, Misc.getTextColor(), Misc.getHighlightColor(), "damage")

        }
    }

    var STORM_DAMAGE_FRACTION = 0.2f //Hyperspace is 0.3f
    var STORM_MIN_STRIKE_DAMAGE = 0.05f //Hyperspace is 0.05f
    var STORM_MAX_STRIKE_DAMAGE = 0.50f //Hyperspace is 0.95f
    fun applyStormStrikes(cell: CellStateTracker, fleet: CampaignFleetAPI, days: Float) {
        if (cell.flicker != null && cell.flicker.wait > 0) {
            cell.flicker.numBursts = 0
            cell.flicker.wait = 0f
            cell.flicker.newBurst()
        }

        if (cell.flicker == null || !cell.flicker.isPeakFrame) return


        //fleet.addScript(new HyperStormBoost(cell, fleet));
        val key = "\$stormStrikeTimeout"
        val mem = fleet.memoryWithoutUpdate
        if (mem.contains(key)) return
        //boolean canDamage = !mem.contains(key);
        mem[key, true] =
            (OldHyperspaceTerrainPlugin.STORM_MIN_TIMEOUT + (OldHyperspaceTerrainPlugin.STORM_MAX_TIMEOUT - OldHyperspaceTerrainPlugin.STORM_MIN_TIMEOUT) * Math.random()).toFloat()


        //if ((float) Math.random() > STORM_STRIKE_CHANCE && false) return;
        val members = fleet.fleetData.membersListCopy
        if (members.isEmpty()) return

        var totalValue = 0f
        for (member in members) {
            totalValue += member.stats.suppliesToRecover.modifiedValue
        }
        if (totalValue <= 0) return

        val strikeValue = totalValue * STORM_DAMAGE_FRACTION * (0.5f + Math.random().toFloat() * 0.5f)


//		int index = Misc.random.nextInt(members.size());
//		FleetMemberAPI member = members.get(index);
        val ebCostThresholdMult = 4f

        val picker = WeightedRandomPicker<FleetMemberAPI>()
        val preferNotTo = WeightedRandomPicker<FleetMemberAPI>()
        for (member in members) {
            var w = 1f
            if (member.isMothballed) w *= 0.1f


            val ebCost = EmergencyBurnAbility.getCRCost(member, fleet)
            if (ebCost * ebCostThresholdMult > member.repairTracker.cr) {
                preferNotTo.add(member, w)
            } else {
                picker.add(member, w)
            }
        }
        if (picker.isEmpty) {
            picker.addAll(preferNotTo)
        }

        val member = picker.pick() ?: return

        val crPerDep = member.deployCost
        val suppliesPerDep = member.stats.suppliesToRecover.modifiedValue
        if (suppliesPerDep <= 0 || crPerDep <= 0) return

        var strikeDamage = crPerDep * strikeValue / suppliesPerDep
        if (strikeDamage < STORM_MIN_STRIKE_DAMAGE) strikeDamage = STORM_MIN_STRIKE_DAMAGE

        val resistance = member.stats.dynamic.getValue(Stats.CORONA_EFFECT_MULT)
        strikeDamage *= resistance

        if (strikeDamage > STORM_MAX_STRIKE_DAMAGE) strikeDamage = STORM_MAX_STRIKE_DAMAGE


//		if (fleet.isPlayerFleet()) {
//			System.out.println("wefw34gerg");
//		}
        val currCR = member.repairTracker.baseCR
        var crDamage = min(currCR, strikeDamage)

        val ebCost = EmergencyBurnAbility.getCRCost(member, fleet)
        if (currCR >= ebCost * ebCostThresholdMult) {
            crDamage = min(currCR - ebCost * 1.5f, crDamage)
        }

        if (crDamage > 0) {
            member.repairTracker.applyCREvent(-crDamage, "hyperstorm", "Hyperspace storm strike")
        }

        var hitStrength = member.stats.armorBonus.computeEffective(member.hullSpec.armorRating)
        hitStrength *= strikeDamage / crPerDep
        if (hitStrength > 0) {
            member.status.applyDamage(hitStrength)
            if (member.status.hullFraction < 0.01f) {
                member.status.hullFraction = 0.01f
            }
        }

        if (fleet.isPlayerFleet) {
            var verb = "suffers"
            var c = Misc.getNegativeHighlightColor()
            if (hitStrength <= 0) {
                verb = "avoids"
                //c = Misc.getPositiveHighlightColor();
                c = Misc.getTextColor()
            }
            Global.getSector().campaignUI.addMessage(member.shipName + " " + verb + " damage from the storm", c)

            Global.getSector().campaignUI.showHelpPopupIfPossible("chmHyperStorm")
        }
    }

    class SpeedBoostScript(var fleet: CampaignFleetAPI, var terrain: AbyssTerrainPlugin) : EveryFrameScript {

        override fun isDone(): Boolean {
            return false
        }

        override fun runWhilePaused(): Boolean {
            return false
        }

        override fun advance(amount: Float) {

            var inCloud = false
            if (AbyssUtils.isPlayerInAbyss()) {
                inCloud = terrain.isAnyInCloud(fleet)
            }

            if (inCloud) {
                fleet.stats.fleetwideMaxBurnMod.modifyMult(terrain.modId + "fog_1", 1.5f, "In abyssal fog")
                fleet.stats.accelerationMult.modifyMult(terrain.modId + "fog_2", 1.5f, "In abyssal fog")
            } else {
                fleet.stats.fleetwideMaxBurnMod.unmodify(terrain.modId + "fog_1")
                fleet.stats.accelerationMult.unmodify(terrain.modId + "fog_2")
            }
        }
    }
}