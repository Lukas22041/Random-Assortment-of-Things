package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.InteractionDialogImageVisual
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.RepLevel
import com.fs.starfarer.api.campaign.rules.MemKeys
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.DModManager
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext
import com.fs.starfarer.api.impl.campaign.ghosts.BaseSensorGhost
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import com.fs.starfarer.campaign.CampaignEngine
import data.scripts.utils.SotfMisc
import org.magiclib.kotlin.getSalvageSeed
import java.util.*

class AbyssalRaphaelInteraction : RATInteractionPlugin() {

    lateinit var member: FleetMemberAPI
    lateinit var data: DerelictShipData

    lateinit var random: Random
    var playerFleet = Global.getSector().playerFleet

    override fun init() {
        var params = (interactionTarget as CustomCampaignEntityAPI).customPlugin as DerelictShipEntityPlugin
        data = params.data

        random = Random(interactionTarget.getSalvageSeed())


        var isSierraAround = false
        if (playerFleet.fleetData.membersListCopy.any { it.variant.hasHullMod("sotf_sierrasconcord") }) {
            isSierraAround = true
        }

        if (data.ship.variantId != null) {
            member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, data.ship.variantId)
        } else {
            member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, data.ship.variant)
        }
        member.shipName = "Voidsaint"
        member.owner = 1

        prepareMember(member, data.ship)

        var path = "graphics/illustrations/rat_abyss_wreckage.jpg"
        Global.getSettings().getAndLoadSprite(path)
        var visual = InteractionDialogImageVisual(path, 1735f, 1014f)
        visualPanel.showImageVisual(visual)

        textPanel.addPara("Your fleet approaches a derelict ship.")

        textPanel.addPara("It appears to be some sort of abyssal hull, yet it is lacking systems typical of others that we've yet observed.")

        textPanel.addPara("It may be recoverable, but we cant confirm so with confidence unless we perform a closer examination.")

        createOption("Examine") {
            clearOptions()
            if (!isSierraAround) {
                textPanel.addPara("Your crew boards the ship and begins inspecting its interiors. " +
                        "For indecipherable reasons, the ship appears to come with some crewed compartments, but not with any sort of human bridge on board.")

                textPanel.addPara("As the crew delves deeper, they instead find a silvery chamber, in the center of it is an interface for an ai core, but its sockets dont match the standard of any known core design. " +
                        "Next to it they find a small plaquette, but due to degradation in the hull, only certain parts are readable. " +
                        "\nIt reads: \"..ER.A-0-B\"", Misc.getTextColor(), Misc.getHighlightColor(), "\"..ER.A-0-B\"")

                textPanel.addPara("The crew is unable to find any hook in to the ships control system. It is unlikely that it can be booted up without the non-standard core required by its interface.")

                addLeaveOption()
                return@createOption
            }

            var sierra = Global.getSector().importantPeople.getPerson("sotf_sierra")
            var sierraShip = playerFleet.fleetData.membersListCopy.find { it.captain == sierra }

            CampaignEngine.getInstance().campaignUI.showNoise(0.5f, 0.25f, 1.5f)

            textPanel.addPara("Just as you were about to send your crew, a sudden request for a comlink comes in from Sierra. " +
                    "Thinking it is of possible relevancy to to the inspection, you halt your crew and prepare to open the channel.", Misc.getTextColor(), AbyssUtils.SIERRA_COLOR, "Sierra")

            createOption("Converse with Sierra") {
                clearOptions()
                visualPanel.showPersonInfo(sierra, true)

                var name = Global.getSector().memoryWithoutUpdate.get("\$sotf_Kindred")

                textPanel.addPara("Her avatar appears on your TriPad without delay. \"How is this possible?\" she asks confusedly. \n\n" +
                        "\"It feels like I've seen this ship before.\" She pauses a moment. \"Or rather, like I've flown it, felt its interfaces... but I've never been in this place before, right?\" \n\n" +
                        "\"Can you keep the channel open, $name? And tell the crews to be careful? This all creeps me out.\"",
                    Misc.getTextColor(), AbyssUtils.SIERRA_COLOR,
                    "\"How is this possible?\"",
                    "\"It feels like I've seen this ship before.\"",
                    "\"Or rather, like I've flown it, felt its interfaces... but I've never been in this place before, right?\"",
                    "\"Can you keep the channel open, $name? And tell the crews to be careful? This all creeps me out.\""
                    )


                createOption("Send out the crew") {
                    clearOptions()

                    textPanel.addPara("The crew enters the confines of the ship. Sierras avatar flickers restlessly as the both of you view the livefeed.")

                    textPanel.addPara("Progress is made through the tiny corridors within the ship. " +
                            "As expected of automated specs, no bridge can be found, however unlike the usual outfit, tiny crew compartments can be spotted around the ship, but with no signs of them having been recently used. ")

                    textPanel.addPara("You continue to watch the feed, and as the crew turns around the corner, Sierras avatar blacks out for just a second, as a silver chamber can be seen on your datapad, of the same design of those on Sierras ship.")

                    createOption("\"Sierra?\"") {
                        clearOptions()

                        textPanel.addPara(
                            "You call her name, and she takes a long moment to think. \"Alright, now I'm just confused,\" she finally answers, no explanation having come to mind.\n\n" +
                                    "\"Not a fan of the implications here,\" she continues, \"But I can think on that later - there's more important matters at hand.\" \n\n" +
                                    "\"Heavy ordnance for its size,\" she mutters, her tone gradually shifting as she fawns over the vessel, \"High-spec interdimensional warship, sourced from an extradimensional space exhibiting exotic physical properties...\" " +
                                    "Her concerns drain away and she speaks almost giddily. \"Well, it might be cursed, but there's only one way to know for sure, right?\"",
                            Misc.getTextColor(), AbyssUtils.SIERRA_COLOR,
                            "\"Alright, now I'm just confused,\"",
                            "\"Not a fan of the implications here,\"",
                            "\"But I can think on that later - there's more important matters at hand.\"",
                            "\"Heavy ordnance for its size,\"",
                            "\"High-spec interdimensional warship, sourced from an extradimensional space exhibiting exotic physical properties...\"",
                            "\"Well, it might be cursed, but there's only one way to know for sure, right?\"",
                        )

                        textPanel.addPara("Understanding what she is getting at, you ready the crew for Sierras transfer.")

                        createOption("Transfer Sierra to the Raphael-class") {
                            clearOptions()


                            var name = "her ship"
                            if (sierraShip != null) {
                                name = sierraShip.shipName
                            }

                            textPanel.addPara("Sierra is transfered from the $name over to the ${member.shipName}. The ships systems boot up and Sierra takes it for a test flight, just narrowly avoiding a collission as she exits phase space.")

                            playerFleet.fleetData.addFleetMember(member)

                            member.variant.addTag("sotf_inert")
                            SotfMisc.toggleSierra(member, textPanel)

                            if (sierraShip != null) {
                                SotfMisc.toggleSierra(sierraShip, textPanel)
                            }

                            member.repairTracker.cr = 0.7f

                            Global.getSector().memoryWithoutUpdate["\$sotf_sierra_var"] = member.getHullSpec().getHullId()
                            //FireBest.fire(null, dialog, memoryMap, "SierraSwitchedShip")


                            createOption("Leave") {

                                var ghosts = interactionTarget.starSystem.scripts.filter { it is BaseSensorGhost } as List<BaseSensorGhost>
                                for (ghost in ghosts) {
                                    Misc.fadeAndExpire(ghost.entity, 10f)
                                    interactionTarget.starSystem.removeScript(ghost)
                                }

                                Misc.fadeAndExpire(interactionTarget, 3f)
                                closeDialog()
                            }
                        }

                    }

                }
            }

        }




        //Remove sensor ghosts
        /*var ghosts = interactionTarget.starSystem.scripts.filter { it is BaseSensorGhost } as List<BaseSensorGhost>

        for (ghost in ghosts) {
            Misc.fadeAndExpire(ghost.entity, 10f)
            interactionTarget.starSystem.removeScript(ghost)
        }*/


        addLeaveOption()
    }


    fun recoverShip(member: FleetMemberAPI) {

        member.shipName = data.ship.shipName
        if (data.ship.fleetMemberId != null) {
            member.id = data.ship.fleetMemberId
        }

        val minHull: Float = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MIN, 0f)
        val maxHull: Float = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MAX, 0f)
        val minCR: Float = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MIN, 0f)
        val maxCR: Float = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MAX, 0f)
        var hull = Math.random().toFloat() * (maxHull - minHull) + minHull
        hull = Math.max(hull, member.status.hullFraction)
        member.status.hullFraction = hull
        val cr = Math.random().toFloat() * (maxCR - minCR) + minCR
        member.repairTracker.cr = cr
        playerFleet.getFleetData().addFleetMember(member)
    }

    fun getCrewConversion() : CargoStackAPI? {
        var stacks =  Global.getSector().playerFleet.cargo.stacksCopy.filter { it.isSpecialStack && it.specialItemSpecIfSpecial.id == "rat_alteration_install" }
        for (stack in stacks) {
            if (stack.specialDataIfSpecial.data == "rat_abyssal_conversion")
            {
                return stack
            }
        }
        return null
    }

    fun reduceOrRemoveStack(stack: CargoStackAPI) {
        stack.subtract(1f)
        if (stack.size < 0.1f) {
            stack.cargo.removeStack(stack)
        }
    }


    fun prepareMember(member: FleetMemberAPI, shipData: PerShipData) {
        val hits = getHitsForCondition(member, shipData.condition)
        var dmods = getDmodsForCondition(shipData.condition)
        var reduction: Int = playerFleet.getStats().getDynamic().getValue(Stats.SHIP_DMOD_REDUCTION, 0f).toInt()
        reduction = random.nextInt(reduction + 1)
        dmods -= reduction
        member.status.random = random
        for (i in 0 until hits) {
            member.status.applyDamage(1000000f)
        }
        member.status.hullFraction = getHullForCondition(shipData.condition)
        member.repairTracker.cr = 0f
        var variant = member.variant
        variant = variant.clone()
        variant.originalVariant = null
        val dModsAlready = DModManager.getNumDMods(variant)
        dmods = Math.max(0, dmods - dModsAlready)
        if (dmods > 0 && shipData.addDmods) {
            DModManager.setDHull(variant)
        }
        member.setVariant(variant, false, true)
        if (dmods > 0 && shipData.addDmods) {
            DModManager.addDMods(member, true, dmods, random)
        }
        if (shipData.sModProb > 0 && random.nextFloat() < shipData.sModProb) {
            var num = 1
            val r: Float = random.nextFloat()
            if (r > 0.85f) {
                num = 3
            } else if (r > 0.5f) {
                num = 2
            }
            val picker: WeightedRandomPicker<String> = WeightedRandomPicker<String>(random)
            for (id in variant.hullMods) {
                val spec = Global.getSettings().getHullModSpec(id)
                if (spec.isHidden) continue
                if (spec.isHiddenEverywhere) continue
                if (spec.hasTag(Tags.HULLMOD_DMOD)) continue
                if (spec.hasTag(Tags.HULLMOD_NO_BUILD_IN)) continue
                if (variant.permaMods.contains(spec.id)) continue
                picker.add(id, spec.capitalCost.toFloat())
            }
            var i = 0
            while (i < num && !picker.isEmpty) {
                val id = picker.pickAndRemove()
                variant.addPermaMod(id, true)
                i++
            }
        }
        if (shipData.pruneWeapons) {
            val retain = getFighterWeaponRetainProb(shipData.condition)
            FleetEncounterContext.prepareShipForRecovery(member, false, false, false, retain, retain, random)
            member.variant.autoGenerateWeaponGroups()
        }
    }


    protected fun getHullForCondition(condition: ShipCondition?): Float {
        when (condition) {
            ShipCondition.PRISTINE -> return 1f
            ShipCondition.GOOD -> return 0.6f + random.nextFloat() * 0.2f
            ShipCondition.AVERAGE -> return 0.4f + random.nextFloat() * 0.2f
            ShipCondition.BATTERED -> return 0.2f + random.nextFloat() * 0.2f
            ShipCondition.WRECKED -> return random.nextFloat() * 0.1f
        }
        return 1f
    }


    protected fun getDmodsForCondition(condition: ShipCondition): Int {
        if (condition == ShipCondition.PRISTINE) return 0
        when (condition) {
            ShipCondition.GOOD -> return 1
            ShipCondition.AVERAGE -> return 1 + random.nextInt(2)
            ShipCondition.BATTERED -> return 2 + random.nextInt(2)
            ShipCondition.WRECKED -> return 3 + random.nextInt(2)
        }
        return 1
    }

    protected fun getFighterWeaponRetainProb(condition: ShipCondition?): Float {
        when (condition) {
            ShipCondition.PRISTINE -> return 1f
            ShipCondition.GOOD -> return 0.67f
            ShipCondition.AVERAGE -> return 0.5f
            ShipCondition.BATTERED -> return 0.33f
            ShipCondition.WRECKED -> return 0.2f
        }
        return 0f
    }

    protected fun getHitsForCondition(member: FleetMemberAPI, condition: ShipCondition): Int {
        if (condition == ShipCondition.PRISTINE) return 0
        if (condition == ShipCondition.WRECKED) return 20
        when (member.hullSpec.hullSize) {
            HullSize.CAPITAL_SHIP -> when (condition) {
                ShipCondition.GOOD -> return 2 + random.nextInt(2)
                ShipCondition.AVERAGE -> return 4 + random.nextInt(3)
                ShipCondition.BATTERED -> return 7 + random.nextInt(6)
            }

            HullSize.CRUISER -> when (condition) {
                ShipCondition.GOOD -> return 1 + random.nextInt(2)
                ShipCondition.AVERAGE -> return 2 + random.nextInt(3)
                ShipCondition.BATTERED -> return 4 + random.nextInt(4)
            }

            HullSize.DESTROYER -> when (condition) {
                ShipCondition.GOOD -> return 1 + random.nextInt(2)
                ShipCondition.AVERAGE -> return 2 + random.nextInt(2)
                ShipCondition.BATTERED -> return 3 + random.nextInt(3)
            }

            HullSize.FRIGATE -> when (condition) {
                ShipCondition.GOOD -> return 1
                ShipCondition.AVERAGE -> return 2
                ShipCondition.BATTERED -> return 3
            }
        }
        return 1
    }

}