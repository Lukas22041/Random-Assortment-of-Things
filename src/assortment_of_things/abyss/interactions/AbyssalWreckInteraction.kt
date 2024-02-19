package assortment_of_things.abyss.interactions

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CustomCampaignEntityAPI
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.DModManager
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin.DerelictShipData
import com.fs.starfarer.api.impl.campaign.FleetEncounterContext
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.PerShipData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.special.ShipRecoverySpecial.ShipCondition
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.collections.ArrayList

class AbyssalWreckInteraction : RATInteractionPlugin() {

    lateinit var member: FleetMemberAPI
    lateinit var data: DerelictShipData

    lateinit var random: Random
    var playerFleet = Global.getSector().playerFleet

    override fun init() {
        var params = (interactionTarget as CustomCampaignEntityAPI).customPlugin as DerelictShipEntityPlugin
        data = params.data

        random = Random(interactionTarget.getSalvageSeed())

        if (data.ship.variantId != null) {
            member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, data.ship.variantId)
        } else {
            member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, data.ship.variant)
        }
        member.owner = 1

        prepareMember(member, data.ship)


        visualPanel.showFleetMemberInfo(member, true)

        textPanel.addPara("The derelict of an inactive ship.")

        textPanel.addPara("After a short inspection, it is determined that the ship seems to be automated, recovery of it wont be possible through normal means.")

        if (!interactionTarget.hasTag("Dont_Show_Salvage_Option")) {
            createOption("Salvage") {
                var dropRandom = ArrayList<SalvageEntityGenDataSpec.DropData>()
                var dropValue = ArrayList<SalvageEntityGenDataSpec.DropData>()
                var drop = SalvageEntityGenDataSpec.DropData()

                drop = SalvageEntityGenDataSpec.DropData()
                drop.chances = 1
                drop.group = "weapons2"
                dropRandom.add(drop)

                drop = SalvageEntityGenDataSpec.DropData()
                drop.chances = 1
                drop.valueMult = 0.1f
                drop.group = "any_hullmod_medium"
                dropRandom.add(drop)

                drop = SalvageEntityGenDataSpec.DropData()
                drop.group = "basic"
                drop.value = 500
                dropValue.add(drop)

                var mult = when(member.hullSpec.hullSize) {
                    HullSize.FRIGATE -> 1f
                    HullSize.DESTROYER -> 1.5f
                    HullSize.CRUISER -> 2f
                    HullSize.CAPITAL_SHIP -> 3f
                    else -> 1f
                }

                var salvage = SalvageEntity.generateSalvage(random, mult, mult, 1f, 1f, dropValue, dropRandom)

                visualPanel.showLoot("Loot", salvage, true) {
                    closeDialog()
                    Misc.fadeAndExpire(interactionTarget)
                }
            }
        }

        var hasCrewed = getCrewConversion() != null

        var tooltip = textPanel.beginTooltip()

        tooltip.addSpacer(10f)

        var img = tooltip.beginImageWithText("graphics/hullmods/rat_crew_conversion.png", 32f)
        img.addPara("By using the \"Abyssal Crew Conversion\" alteration, the ship can be made recoverable and boardable by human crew. ", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Abyssal Crew Conversion")

        tooltip.addImageWithText(0f)

        tooltip.addSpacer(10f)

        var img2 = tooltip.beginImageWithText("graphics/icons/skills/automated_ships.png", 32f)
        img2.addPara("The ship can be recovered as an automated ship through the \"Automated Ships\" Skill.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Automated Ships")

        tooltip.addImageWithText(0f)

        textPanel.addTooltip()

        var hasAutomated = !Misc.isUnboardable(member)

        var crewedConversion = "Recover with crew conversion"
        var automatedData = "Recover as automated ship."


        createOption(crewedConversion) {

            member.fixVariant()
            if (!member.variant.hasHullMod("rat_abyssal_conversion")) {
                member.variant.addMod("rat_abyssal_conversion")
            }

            dialog.showFleetMemberRecoveryDialog("Select ships to recover", listOf(member), object :
                FleetMemberPickerListener {
                override fun pickedFleetMembers(selected: MutableList<FleetMemberAPI>?) {
                    var playerFleet = Global.getSector().playerFleet
                    for (member in selected!!) {

                        recoverShip(member)
                        reduceOrRemoveStack(getCrewConversion()!!)
                        closeDialog()
                        Misc.fadeAndExpire(interactionTarget)
                    }
                }

                override fun cancelledFleetMemberPicking() {

                }
            })
        }

        if (!hasCrewed) {
            optionPanel.setEnabled(crewedConversion, false)
            optionPanel.setTooltip(crewedConversion, "Requires the \"Abyssal Crew Conversion\" alteration in the fleets storage.")

        }
        else {
            optionPanel.setTooltip(crewedConversion, "By using an \"Abyssal Crew Conversion\", the ship can be made recoverable and pilotable by humans. This consumes the item.")
        }

        //Automated
        createOption(automatedData) {

            member.fixVariant()
            if (member.variant.hasHullMod("rat_abyssal_conversion")) {
                member.variant.removePermaMod("rat_abyssal_conversion")
            }

            dialog.showFleetMemberRecoveryDialog("Select ships to recover", ArrayList<FleetMemberAPI>(), listOf(member), object :
                FleetMemberPickerListener {
                override fun pickedFleetMembers(selected: MutableList<FleetMemberAPI>?) {
                    for (member in selected!!) {
                        recoverShip(member)

                        closeDialog()
                        Misc.fadeAndExpire(interactionTarget)
                    }
                }

                override fun cancelledFleetMemberPicking() {

                }
            })
        }

        if (!hasAutomated) {
            optionPanel.setEnabled(automatedData, false)
            optionPanel.setTooltip(automatedData, "Requires the automated ships skill.")

        }
        else {
            optionPanel.setTooltip(automatedData, "Recovers the ship using the automated ships skill.")
        }

        dialog.makeStoryOption(automatedData, 1, 1f, Sounds.STORY_POINT_SPEND)



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