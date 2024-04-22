package assortment_of_things.backgrounds.bounty

import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.campaign.FactionAPI.ShipPickMode
import com.fs.starfarer.api.campaign.ai.CampaignFleetAIAPI.EncounterOption
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin.ListInfoMode
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3
import com.fs.starfarer.api.impl.campaign.ids.*
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

//Based on VengeanceFleetIntel from Nexerelin
class BountyFleetIntel(var factionId: String, var market: MarketAPI, var nonHostile: Boolean) : BaseIntelPlugin() {

    enum class EndReason {
        FAILED_TO_SPAWN, DEFEATED, EXPIRED, NO_LONGER_HOSTILE, OTHER
    }

    val TRAIL_ASSIGNMENT: FleetAssignment = FleetAssignment.DELIVER_CREW

    var endReason: EndReason? = null
    var assembling: Boolean = true
    var over: Boolean = false
    var daysToLaunch: Float = 0f
    var daysToLaunchFixed: Float = 0f
    var daysLeft: Float = 0f
    var duration: Int = 0
    var fleet: CampaignFleetAPI? = null
    var foundPlayerYet: Boolean = false
    val interval: IntervalUtil = IntervalUtil(0.3f, 0.5f)
    val interval2: IntervalUtil = IntervalUtil(1f, 1.5f)
    val locationTokenInterval = IntervalUtil(5f, 15f)
    var timeSpentLooking: Float = 0f
    var trackingMode: Boolean = false
    var locationToken: SectorEntityToken? = null
    var availableFP: Int = 0

    init {

        availableFP = setFPBudget()

        daysToLaunch = when(availableFP) {
            in 0..50 -> 7f
            in 51..80 -> 15f
            in 81..120-> 20f
            in 121..180-> 35f
            else -> 50f
        }

        daysToLaunch = Math.round(daysToLaunch * MathUtils.getRandomNumberInRange(0.9f, 1.1f)).toFloat()
        daysToLaunchFixed = daysToLaunch

        important = true
    }

    fun setFPBudget() : Int {
        var fp = 0f
        var minimum = MathUtils.getRandomNumberInRange(50f, 60f)
        var maximum = MathUtils.getRandomNumberInRange(200f, 240f)
        var player = Global.getSector().playerFleet
        var playerPoints = player.fleetPoints * MathUtils.getRandomNumberInRange(0.8f, 1.2f)

        fp = playerPoints
        fp = fp.coerceIn(minimum, maximum)
        return fp.toInt()
    }

    fun getFaction() = Global.getSector().getFaction(factionId)

    override fun getName(): String {
        return "${getFaction().displayName} - Bounty Fleet"
    }

    override fun getSmallDescriptionTitle(): String {
        return "${getFaction().displayName} - Bounty Fleet"
    }

    override fun addBulletPoints(info: TooltipMakerAPI, mode: ListInfoMode?, isUpdate: Boolean, tc: Color?,initPad: Float) {
        val faction = Global.getSector().getFaction(factionId)

        val pad = 0f
        val name = Misc.ucFirst(faction.displayName)
        info.addPara("A bounty has been put on you.", initPad,tc, faction.baseUIColor, "bounty")

        var bullet = ""

        if (over) {
            when (endReason) {
                EndReason.FAILED_TO_SPAWN -> bullet = "Fleet failed to assemble"
                EndReason.DEFEATED -> bullet = "Fleet defeated"
                EndReason.EXPIRED -> bullet = "Mission over"
                EndReason.NO_LONGER_HOSTILE -> bullet = "Cancelled: No longer hostile"
            }
            info.addPara(bullet, 0f, tc, Misc.getGrayColor())
        } else if (assembling) {
            val dtl = getDays(daysToLaunch) + " " + getDaysString(daysToLaunch)
            bullet = "The fleet will launch from ${market.name} in $dtl."
            if (!isMarketKnown()) {
                bullet = "Launching in $dtl"
                info.addPara(bullet, 0f, tc, Misc.getHighlightColor(), "$dtl")

            } else {
                info.addPara(bullet, 0f, tc, Misc.getHighlightColor(), "${market.name}", "$dtl")
            }
        } else {
            bullet += "Fleet launched from ${market.name}"
            info.addPara(bullet, 0f, tc, Misc.getHighlightColor(), "${market.name}")
        }
    }

    override fun createSmallDescription(info: TooltipMakerAPI, width: Float, height: Float) {
        val opad = 10f
        val faction = getFaction()


        //if (fleet != null)
        //	info.addImages(width, 128, opad, opad, faction.getCrest(), fleet.getCommander().getPortraitSprite());
        //else
        info.addImage(faction.logo, width, 128f, opad)

        val para = info.addPara("Someone within ${getFaction().displayNameWithArticle} set a bounty on your head due to your continued influence on sector politics. A fleet will soon try to claim it.", opad, Misc.getTextColor(), getFaction().color, getFaction().displayNameWithArticle)
        para.setHighlight(faction.displayNameWithArticleWithoutArticle)
        para.setHighlightColor(faction.baseUIColor)

        if (nonHostile) {
            info.addSpacer(10f)
            info.addPara("This fleet operates without official support of its governing faction. Defeating it is unlikely to cause reductions in reputations.")
        }

        info.addSectionHeading("Status",
            faction.baseUIColor,
            faction.darkUIColor,
            Alignment.MID,
            opad)

        var text = ""

        if (over) {
            when (endReason) {
                EndReason.FAILED_TO_SPAWN -> text = "The fleet has failed to spawn. The event is now over."
                EndReason.DEFEATED -> text = "The fleet has been defeated."
                EndReason.EXPIRED -> text = "The fleet is returning to base."
                EndReason.NO_LONGER_HOSTILE -> text = "${faction.displayNameWithArticle} ${faction.displayNameIsOrAre} no longer hostile. The vengeance mission has been terminated."
            }

            info.addPara(text, opad, Misc.getTextColor(), Misc.getHighlightColor(), "${faction.displayNameWithArticle}")

        } else if (assembling) {
            var dtl = getDays(daysToLaunch) + " " + getDaysString(daysToLaunch)
            text = "The fleet will launch from ${market.name} in $dtl."

            if (!isMarketKnown()) {
                text = "The fleet will launch from an unknown location in $dtl."
            }

            info.addPara(text, opad, Misc.getTextColor(), Misc.getHighlightColor(), "${market.name}", "$dtl")

        } else {
            val durStr = Misc.getAtLeastStringForDays(duration)
            text += "The fleet is currently active and will pursue you for $durStr."
            info.addPara(text, opad, Misc.getTextColor(), Misc.getHighlightColor(), durStr)

        }

        if (fleet != null && Global.getSettings().isDevMode) {
            info.addPara("Debug information", opad)
            bullet(info)
            info.addPara(String.format("Current location: %s", fleet!!.containingLocation.name), 0f)
            if (fleet!!.assignmentsCopy.isNotEmpty()) {
                val assign = fleet!!.assignmentsCopy[0]
                info.addPara(String.format("Current assignment: %s, %s, target %s",
                    assign.assignment,
                    assign.actionText,
                    assign.target), 0f)
            }
            if (locationToken != null) {
                info.addPara(String.format("Location token is in: %s", locationToken!!.containingLocation), 0f)
            }
            info.addPara("Tracking mode: $trackingMode", 0f)
            unindent(info)
        }
    }

    fun isMarketKnown(): Boolean {
        return market.primaryEntity.isVisibleToPlayerFleet
    }

    override fun getIntelTags(map: SectorMapAPI?): Set<String> {
        val tags = super.getIntelTags(map)
        tags.add(factionId)
        return tags
    }

    override fun getIcon(): String {
        //return getFaction().getCrest();
        return getFaction().crest
    }

    override fun getFactionForUIColors(): FactionAPI {
        return getFaction()
    }

    override fun getBaseDaysAfterEnd(): Float {
        return 7f
    }

    override fun getMapLocation(map: SectorMapAPI?): SectorEntityToken? {
        if (assembling && isMarketKnown()) return market.primaryEntity
        return null
    }

    fun handleFleetAssignment(playerFleet: CampaignFleetAPI) {
        //updateLocationToken()

        var playerVisible = false
        var fleetVisible = false
        if (fleet!!.containingLocation == playerFleet.containingLocation) {
            playerVisible = playerFleet.isVisibleToSensorsOf(fleet)
            fleetVisible = fleet!!.isVisibleToSensorsOf(playerFleet)
        }
        if (playerVisible && fleetVisible) {
            foundPlayerYet = true
        }


        // player and enemy fleet are close to each other, deactivate tracking mode
        if (trackingMode && fleet!!.containingLocation == playerFleet.containingLocation) {

            var detectRange = (1.5f * max(fleet!!.getMaxSensorRangeToDetect(playerFleet).toDouble(), playerFleet.getMaxSensorRangeToDetect(fleet).toDouble()))

            if (Misc.getDistance(fleet!!.location,
                    playerFleet.location) <= 1000f + detectRange) {
                trackingMode = false
            }
        }

        if (fleet!!.containingLocation == playerFleet.containingLocation) {
            //2f, originaly 1.5f
            var detectRange = (2f * max(fleet!!.getMaxSensorRangeToDetect(playerFleet).toDouble(), playerFleet.getMaxSensorRangeToDetect(fleet).toDouble()))

            if (Misc.getDistance(fleet!!.location, playerFleet.location) <= 1000f + detectRange) {
                foundPlayerYet = true //Set to true to cause the fleet to not home on to the player.
            }
        }

        val targetName = "your fleet"


        // my understanding of tracking mode:
        // it activates when player [has been encountered at least once or is currently visible], and [is no longer in same system]
        // while tracking mode is active, fleet can travel to player without an actual sensor lock
        // tracking mode is deactivated once the two fleets are in same location again and get sufficiently close
        val option = fleet!!.ai.pickEncounterOption(null, playerFleet)
        if (option == EncounterOption.ENGAGE || option == EncounterOption.HOLD_VS_STRONGER) {
            // can see player or has encountered player at least once
            if (playerVisible || foundPlayerYet) {
                // in same system but not currently in tracking mode, look around the system?
                // this means that if fleet has been shaken once,
                // as long as player doesn't leave the system, player is safe
                if (fleet!!.containingLocation == playerFleet.containingLocation && !trackingMode) {
                    if (fleet!!.ai.currentAssignmentType != FleetAssignment.PATROL_SYSTEM) {

                        fleet!!.clearAssignments()
                        fleet!!.addAssignment(FleetAssignment.PATROL_SYSTEM,
                            locationToken,
                            1000f,
                            "hunting $targetName")
                        fleet!!.getAbility(Abilities.EMERGENCY_BURN).activate()
                        (fleet!!.ai as ModularFleetAIAPI).tacticalModule.setPriorityTarget(playerFleet, MathUtils.getRandomNumberInRange(5f, 7f), false)
                    }
                    // not in same system, or currently tracking; activate tracking mode
                } else {
                     trackingMode = true
                    if (fleet!!.containingLocation == playerFleet.containingLocation) {
                        // tracking in same system, intercept
                        // 0.95: intercept only works if we can see player
                        // don't think it's possible to have sight on the player without tracking mode turning off
                        if (playerVisible) {
                            if (fleet!!.ai.currentAssignmentType != FleetAssignment.INTERCEPT) {
                                fleet!!.clearAssignments()
                                fleet!!.addAssignment(FleetAssignment.INTERCEPT,
                                    playerFleet,
                                    1000f,
                                   "intercepting $targetName")
                            }
                        } else {
                            if (fleet!!.ai.currentAssignmentType != FleetAssignment.ATTACK_LOCATION) {
                                fleet!!.clearAssignments()
                                fleet!!.addAssignment(FleetAssignment.ATTACK_LOCATION,
                                    locationToken,
                                    1000f,
                                    "hunting $targetName")
                                (fleet!!.ai as ModularFleetAIAPI).tacticalModule.setPriorityTarget(playerFleet,
                                    1000f,
                                    false)
                            }
                        }
                    } else {
                        // player not in same system, maphack our way to player
                        if (fleet!!.ai.currentAssignmentType != TRAIL_ASSIGNMENT) {
                            fleet!!.clearAssignments()
                            fleet!!.addAssignment(TRAIL_ASSIGNMENT,
                                locationToken,
                                1000f,
                                "trailing $targetName")
                        }
                    }
                }
            } else {
                if (fleet!!.ai.currentAssignmentType != TRAIL_ASSIGNMENT) {
                    fleet!!.clearAssignments()
                    fleet!!.addAssignment(TRAIL_ASSIGNMENT,
                        locationToken,
                        1000f,
                        "trailing $targetName")
                }
            }
        } else {
            endEvent(EndReason.DEFEATED)
            return
        }

        if (!fleetVisible || !playerVisible) {
            if (daysLeft <= 0f) {
                endEvent(EndReason.EXPIRED)
            }
        }
    }

    fun hasWorkingSpaceport(market: MarketAPI): Boolean {
        for (ind in market.industries) {
            if (!ind.spec.hasTag(Industries.TAG_SPACEPORT)) continue
            if (ind.isDisrupted) continue
            return true
        }
        return false
    }

    public override fun advanceImpl(amount: Float) {
        if (over) {
            return
        }

        if (getFaction().isAtWorst(Factions.PLAYER, RepLevel.INHOSPITABLE) && !nonHostile) {
            endEvent(EndReason.NO_LONGER_HOSTILE)
            return
        }

        if (assembling) {
            if (market.factionId != factionId) endEvent(EndReason.FAILED_TO_SPAWN)

            if (!hasWorkingSpaceport(market)) {
                endEvent(EndReason.FAILED_TO_SPAWN)
            }

            daysToLaunch -= Global.getSector().clock.convertToDays(amount)
            if (daysToLaunch < 0) {
                assembling = false
                fleet = spawnFleet()
                if (fleet != null) sendUpdateIfPlayerHasIntel(null, false)
                else endEvent(EndReason.FAILED_TO_SPAWN)
            }
            return
        }

        val playerFleet = Global.getSector().playerFleet ?: return

        if (!fleet!!.isAlive) {
            endEvent(EndReason.DEFEATED)
            return
        }


        // fleet took too many losses, quit
        if (fleet!!.memoryWithoutUpdate.contains("\$startingFP") && fleet!!.fleetPoints < 0.4 * fleet!!.memoryWithoutUpdate.getFloat(
                "\$startingFP")) {
            endEvent(EndReason.DEFEATED)
            return
        }

        /* Advance faster and faster if they lost you */
        val days = Global.getSector().clock.convertToDays(amount)
        if (foundPlayerYet) {
            timeSpentLooking += days
            //daysLeft -= days * (2f + (escalationLevel * timeSpentLooking / duration))
            daysLeft -= days * (2f + (timeSpentLooking / duration))
        } else {
            daysLeft -= days
        }
        interval.advance(days)
        interval2.advance(days)

        if (interval2.intervalElapsed()) {
            if (fleet!!.ai.currentAssignmentType == FleetAssignment.PATROL_SYSTEM && (fleet!!.ai as ModularFleetAIAPI).tacticalModule.target !== playerFleet) {
                (fleet!!.ai as ModularFleetAIAPI).tacticalModule.setPriorityTarget(playerFleet, 1000f, false)
                (fleet!!.ai as ModularFleetAIAPI).tacticalModule.target = playerFleet
            }
        }

        locationTokenInterval.advance(amount)
        if (locationTokenInterval.intervalElapsed()) {
            updateLocationToken()
        }

        if (!interval.intervalElapsed()) {
            return
        }


        handleFleetAssignment(playerFleet)
    }

    fun updateLocationToken() {
        val player = Global.getSector().playerFleet
        if (player == null || player.containingLocation == null) return

        val loc = player.location
        if (locationToken == null) {
            locationToken = player.containingLocation.createToken(loc)
        }
        if (locationToken!!.containingLocation !== player.containingLocation) {
            // since we can't change its system, just rebuild it
            locationToken = player.containingLocation.createToken(loc)
            if (fleet != null) fleet!!.clearAssignments()
        }
        locationToken!!.setLocation(loc.x, loc.y)
    }

    fun startEvent() {

        val playerFleet = Global.getSector().playerFleet
        if (playerFleet == null) {
            endEvent(EndReason.OTHER, 0f)
            return
        }

        updateLocationToken()

        val distance = Misc.getDistanceToPlayerLY(market.primaryEntity)
        val distBonus = 30 + distance * 1.5f // don't crank it up too much, I don't think this is the important component

        duration = when (availableFP) {
            in 0..80 -> max(70.0,
                min(90.0, Math.round(distBonus * MathUtils.getRandomNumberInRange(0.75f, 1f)).toDouble())).toInt()

            in 81..180 -> max(100.0,
                min(100.0, Math.round(distBonus * MathUtils.getRandomNumberInRange(1.25f, 1.75f)).toDouble())).toInt()

            else -> max(130.0,
                min(120.0, Math.round(distBonus * MathUtils.getRandomNumberInRange(2f, 2.5f)).toDouble())).toInt()
        }
        duration += 15

        /*if (Global.getSector().memoryWithoutUpdate.contains(VengeanceBuff.MEMORY_KEY)) {
            duration = (duration * VengeanceBuff.SEARCH_TIME_MULT).toInt()
        }*/

        daysLeft = duration.toFloat()

        Global.getSector().intelManager.addIntel(this)
        Global.getSector().addScript(this)
    }

    fun spawnFleet(): CampaignFleetAPI? {
        if (market.factionId != factionId) return null
        if (!market.isInEconomy || !market.primaryEntity.isAlive) return null

        val playerFleet = Global.getSector().playerFleet
        var combat = availableFP * 0.8f
        var freighter = availableFP * 0.2f
        var tanker = availableFP * 0.2f
        var utility= availableFP * 0.1f

        val total = combat + freighter + tanker + utility

        val params = FleetParamsV3(market,  // market
            FleetTypes.MERC_BOUNTY_HUNTER, combat,  // combatPts
            freighter,  // freighterPts
            tanker,  // tankerPts
            0f,  // transportPts
            0f,  // linerPts
            utility,  // utilityPts
            3f // qualityMod
        )
        params.ignoreMarketFleetSizeMult = true // only use doctrine size, not source market size
        params.modeOverride = ShipPickMode.PRIORITY_THEN_ALL

        if (availableFP >= 140f) {
            params.averageSMods = 1
        }

        fleet = FleetFactoryV3.createFleet(params)

        if (fleet == null) {
            return null
        }

        fleet!!.memoryWithoutUpdate["\$startingFP"] = fleet!!.fleetPoints
        fleet!!.memoryWithoutUpdate["\$clearCommands_no_remove"] = true

        market.primaryEntity.containingLocation.addEntity(fleet)
        fleet!!.setLocation(market.primaryEntity.location.x, market.primaryEntity.location.y)

        fleet!!.addAssignment(FleetAssignment.ORBIT_PASSIVE,
            market.primaryEntity,
            3f + Math.random().toFloat() * 2f,
            "orbiting ${market.name}")

        fleet!!.stats.fleetwideMaxBurnMod.modifyFlat("rat_bounty_fleet", -1f)

        fleet!!.memoryWithoutUpdate[MemFlags.MEMORY_KEY_SAW_PLAYER_WITH_TRANSPONDER_ON] = true
        fleet!!.memoryWithoutUpdate[MemFlags.MEMORY_KEY_LOW_REP_IMPACT] = true
        //fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_PATROL_FLEET, true);
        fleet!!.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE] = true

        fleet!!.addEventListener(BountyFleetListener())

        if (nonHostile) {
            fleet!!.memoryWithoutUpdate[MemFlags.MEMORY_KEY_NO_REP_IMPACT] = true
            fleet!!.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_HOSTILE] = true
        }

        return fleet
    }

    fun endEvent(reason: EndReason) {
        endEvent(reason, baseDaysAfterEnd)
    }

    fun endEvent(reason: EndReason, time: Float) {
        over = true
        endReason = reason
        if (fleet != null && fleet!!.isAlive) {
            fleet!!.clearAssignments()
            fleet!!.addAssignment(FleetAssignment.GO_TO_LOCATION_AND_DESPAWN,
                market.primaryEntity,
                1000f,
                "returning to ${market.name}")
            fleet!!.memoryWithoutUpdate[MemFlags.MEMORY_KEY_PATROL_FLEET] = false
            fleet!!.memoryWithoutUpdate[MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE] = false
            (fleet!!.ai as ModularFleetAIAPI).tacticalModule.forceTargetReEval()
        }

        if (reason == EndReason.DEFEATED) {
            if (fleet!!.memoryWithoutUpdate.contains("\$fought_player_in_battle")) {
                var manager = Global.getSector().scripts.find { it is BackgroundBountyManager } as BackgroundBountyManager?

                if (manager != null) {
                    var startingPoints  = fleet!!.memoryWithoutUpdate.getInt("\$startingFP") ?: 60
                    manager.totalFPDefeated += startingPoints
                    manager.defeated += 1
                }
            }

        sendUpdateIfPlayerHasIntel(null, false)

        endAfterDelay(time)


        }
    }

}