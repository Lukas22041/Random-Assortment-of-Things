package assortment_of_things.campaign.interactions

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.RATStrings
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import lunalib.lunaExtensions.getFactionsWithMarkets
import lunalib.lunaExtensions.getKnownHullmodSpecs
import lunalib.lunaExtensions.getKnownShipSpecs
import lunalib.lunaExtensions.getKnownWeaponSpecs
import org.lazywizard.lazylib.MathUtils
import kotlin.random.Random
import kotlin.random.nextInt

class OutpostPlanetInteraction : RATInteractionPlugin() {
    override fun init() {

        var station = targetMemory.get("\$defenderStation")
        if (station != null && station is CampaignFleetAPI && station.isAlive && !station.isEmpty && !station.isDespawning)
        {
            textPanel.addPara("You arrive at the heavily guarded planet. Due to the orbiting Station, it would be impossible the descend to its surface.")
            addLeaveOption()

            return
        }

        var defenderFleet = targetMemory.get("\$defenderFleet")
        if (defenderFleet != null && defenderFleet is CampaignFleetAPI && !defenderFleet.isEmpty )
        {
            var label = textPanel.addPara("You arrive at the heavily guarded planet. Your sensors detect some kind of facility at the surface, but they also detect multiple ${interactionTarget.faction.displayName} patrols close to the surface." +
                    "\nDescending any lower will ensure that they will start their fire towards us.")
            label.setHighlight("${interactionTarget.faction.displayName}")
            label.setHighlightColors(interactionTarget.faction.baseUIColor)

            triggerDefenders()

            return
        }
        else
        {
            noDefendersDialog()
        }
    }

    fun noDefendersDialog()
    {
        var l = textPanel.addPara("With the defending patrols gone, nothings stopping our fleet from descending towards the surface. However, due to the level of defenses within this system, it is safe to asume that ${interactionTarget.faction.displayNameWithArticle} did not leave the facility without defenses either.")
        l.setHighlight("${interactionTarget.faction.displayNameWithArticle}")
        l.setHighlightColors(interactionTarget.faction.baseUIColor)
        textPanel.addPara("\nTo guarantee success, we will need a strong force of marines and supplies. Additionaly, we require a solid amount of fuel to disable their ground defenses.")

        var required = createCostPanel(mapOf(Commodities.MARINES to 100, Commodities.SUPPLIES to 50, Commodities.FUEL to 250), true)
        createOption("Descend towards the planet") {
            clearOptions()
            textPanel.addPara("> Descend towards the planet", playerColor)
            var playerCargo = Global.getSector().playerFleet.cargo

            var lostSupplies = MathUtils.getRandomNumberInRange(25, 45)
            var lostMarines = MathUtils.getRandomNumberInRange(25, 60)
            playerCargo.removeCommodity(Commodities.MARINES, lostMarines.toFloat())
            playerCargo.removeCommodity(Commodities.SUPPLIES, lostSupplies.toFloat())
            playerCargo.removeCommodity(Commodities.FUEL, 250f)

            textPanel.addPara("You order your fleet to descend towards the planet, your ships start dropping antimatter fuel containers towards the facilities defenses.")

            textPanel.addPara("> Lost 250 units of fuel", negativeColor)
            createOption("Continue") {
                textPanel.addPara("> Continue", playerColor)
                clearOptions()
                
                textPanel.addPara("With the defenses disrupted, your landers descend and arrive at the facility. The Marines breach through the entrances and quickly overun the ground defenses. " +
                        "After just a few hours, the Marines managed to empty it of any threats.")

                textPanel.addPara("> Lost $lostMarines Marines", negativeColor)
                textPanel.addPara("> Lost $lostSupplies Supplies", negativeColor)

                if (Global.getSettings().modManager.isModEnabled("nexerelin"))
                {
                    var prisoners = MathUtils.getRandomNumberInRange(2, 5)
                    var label = textPanel.addPara("While exploring the facility, the Marines discovered ${interactionTarget.faction.displayName} scientists in hiding. When questioned they didnt spit out anything, so the Marines took them in as prisoners.")
                    label.setHighlight("${interactionTarget.faction.displayName}")
                    label.setHighlightColors(interactionTarget.faction.baseUIColor)

                    textPanel.addPara("> Gained $prisoners prisoners", positiveColor)
                    playerCargo.addCommodity("prisoner", prisoners.toFloat())
                }

                createOption("Continue") {
                    clearOptions()
                    textPanel.addPara("> Continue", playerColor)

                    var label = textPanel.addPara("After some time, they arrive at the core of the facility. They discover a vault filled to the brim with both highly secure blueprints, and items that are highly illegal under ${interactionTarget.faction.displayName} law.")
                    label.setHighlight("vault", "blueprints", "illegal", "${interactionTarget.faction.displayName}")
                    label.setHighlightColors(highlightColor, positiveColor, negativeColor, interactionTarget.faction.baseUIColor)

                    var label2 = textPanel.addPara("Gaining access to such items would likely be in the interests of ${interactionTarget.faction.displayNameWithArticle} competitors, they may reward you greatly for sharing its items instead.")
                    label2.setHighlight("${interactionTarget.faction.displayNameWithArticle}", "competitors")
                    label2.setHighlightColors(interactionTarget.faction.baseUIColor, negativeColor)

                    createOption("Loot the Vault") {
                        lootVault()
                    }

                    var faction = interactionTarget.faction
                    var otherFactions = Global.getSector().getFactionsWithMarkets().filter { faction.getRelationshipLevel(it.id).isNegative }

                    if (otherFactions.isNotEmpty())
                    {
                        createOption("Sell location of the Vault to competitors") {
                            shareTheLocation(otherFactions.random())
                        }
                    }
                }
            }


        }
        if (!required && !Global.getSettings().isDevMode)
        {
            optionPanel.setTooltip("Descend towards the planet", "Missing the required Commodities.")
            optionPanel.setEnabled("Descend towards the planet", required)
        }
        addLeaveOption()
    }

    fun lootVault() {
        clearOptions()
        var listener = LootListener(this)
        var cargo = Global.getFactory().createCargo(true)

        var faction = interactionTarget.faction

        var seed = targetMemory.get("\$salvageSeed")
        var random = when (seed) {
            null -> Random(1)
            else -> Random(seed as Long)
        }
        random.nextInt()

       /* cargo.addCommodity(Commodities.GAMMA_CORE, random.nextInt(1..7).toFloat())
        cargo.addCommodity(Commodities.BETA_CORE, random.nextInt(1..5).toFloat())
        cargo.addCommodity(Commodities.ALPHA_CORE, random.nextInt(1..2).toFloat())
*/

        for (commodity in interactionTarget.faction.illegalCommodities)
        {
            var test = commodity
            var test2 = commodity
            when (commodity) {
                Commodities.AI_CORES -> {
                    cargo.addCommodity(Commodities.GAMMA_CORE, random.nextInt(2..5).toFloat())
                    cargo.addCommodity(Commodities.BETA_CORE, random.nextInt(1..5).toFloat())
                    cargo.addCommodity(Commodities.ALPHA_CORE, random.nextInt(1..2).toFloat())
                }

                Commodities.DRUGS -> cargo.addCommodity(Commodities.DRUGS, random.nextInt(50..400).toFloat())
                Commodities.ORGANS -> cargo.addCommodity(Commodities.ORGANS, random.nextInt(25..200).toFloat())
                Commodities.LUXURY_GOODS -> cargo.addCommodity(Commodities.LUXURY_GOODS, random.nextInt(250..500).toFloat())
                Commodities.DOMESTIC_GOODS -> cargo.addCommodity(Commodities.DOMESTIC_GOODS, random.nextInt(250..500).toFloat())
                Commodities.HEAVY_MACHINERY -> cargo.addCommodity(Commodities.HEAVY_MACHINERY, random.nextInt(150..300).toFloat())
            }
        }

        var ships = faction.getKnownShipSpecs().filter { !it.hints.contains(ShipHullSpecAPI.ShipTypeHints.UNBOARDABLE) && !it.hints.contains(ShipHullSpecAPI.ShipTypeHints.HIDE_IN_CODEX) }
        var weapons = faction.getKnownWeaponSpecs().filter { it.getOrdnancePointCost(Global.getSector().playerStats) != 0f }
        var hullmods = faction.getKnownHullmodSpecs().filter { !it.isHidden && !it.isHiddenEverywhere }
        var fighters = faction.knownFighters

        if (ships.isNotEmpty())
        {
            for (i in 0 until random.nextInt(2..5))
            {
                cargo.addSpecial(SpecialItemData("ship_bp", ships.random(random).hullId), 1f)
            }
        }

        if (weapons.isNotEmpty())
        {
            for (i in 0 until random.nextInt(0..6))
            {
                cargo.addSpecial(SpecialItemData("weapon_bp", weapons.random(random).weaponId), 1f)
            }
        }

        if (hullmods.isNotEmpty())
        {
            for (i in 0 until random.nextInt(0..4))
            {
                cargo.addSpecial(SpecialItemData("modspec", hullmods.random(random).id), 1f)
            }
        }

        if (fighters.isNotEmpty())
        {
            for (i in 0 until random.nextInt(0..3))
            {
                cargo.addSpecial(SpecialItemData("fighter_bp", fighters.random(random)), 1f)
            }
        }


        visualPanel.showLoot("Loot the Vault", cargo, true, listener)
    }

    fun shareTheLocation(fac: FactionAPI)
    {
        clearOptions()
        var playerFaction = Global.getSector().playerFaction.id
        textPanel.addPara("> Sell location of the Vault to competitors", playerColor)

        var label = textPanel.addPara("You post a sell offer in a secure network, after a barrage of requests, you manage to get in to an agreement with a representitive of ${fac.displayNameWithArticle}." +
                " They are more than willing to offer you you the compensation you expect.")
        label.setHighlight("${fac.displayNameWithArticle}")
        label.setHighlightColors(fac.baseUIColor)

        createOption("Sell the Vault to ${fac.displayName}") {
            clearOptions()
            textPanel.addPara("> Sell the Vault to ${fac.displayName}", playerColor)

            Global.getSector().playerFleet.cargo.credits.add(450000f)

            var relation = RepLevel.getRepInt(fac.getRelationship(playerFaction))
            fac.adjustRelationship(Global.getSector().playerFaction.id, MathUtils.getRandomNumberInRange(0.25f, 0.35f))
            var relation2 = RepLevel.getRepInt(fac.getRelationship(playerFaction))
            var relationChange = relation2 - relation

            textPanel.addPara("> Gained 450.000 Credits", textColor, highlightColor, "450.000")
            textPanel.addPara("> Improved relations to ${fac.displayNameWithArticle} by $relationChange", textColor, positiveColor, "$relationChange")

            textPanel.addPara("The representitive seems glad to have made business with you.", textColor, positiveColor)

            var target = dialog.interactionTarget
            target.setFaction(Factions.NEUTRAL)
            target.removeTag(RATStrings.TAG_OUTPOST_PLANET)

            if (target is PlanetAPI)
            {
                target.customDescriptionId = "rat_looted_outpost_planet"
            }

            addLeaveOption()
        }

        createOption("Sell the Vault to ${fac.displayName} and then loot it yourself.") {
            clearOptions()

            textPanel.addPara("> Sell the Vault to ${fac.displayName} and then loot it yourself.", playerColor)
            Global.getSector().playerFleet.cargo.credits.add(450000f)

            var relation = RepLevel.getRepInt(fac.getRelationship(playerFaction))
            fac.adjustRelationship(Global.getSector().playerFaction.id, MathUtils.getRandomNumberInRange(-0.40f, -0.65f))
            var relation2 = RepLevel.getRepInt(fac.getRelationship(playerFaction))
            var relationChange = relation2 - relation

            textPanel.addPara("> Gained 450.000 Credits", textColor, highlightColor, "450.000")

            textPanel.addPara("At first, the representitive seems glad to have made business with you. That feeling might not stick around after they arrive at an empty vault.")

            textPanel.addPara("> Worsened relations to ${fac.displayNameWithArticle} by $relationChange", textColor, negativeColor, "$relationChange")

            createOption("Loot the Vault") {
                lootVault()
            }
        }
    }

    override fun defeatedDefenders() {
        super.defeatedDefenders()
        clear()
        noDefendersDialog()
    }
}

private class LootListener(var dialog: RATInteractionPlugin) : CoreInteractionListener
{
    override fun coreUIDismissed() {
        var target = dialog.interactionTarget

        target.setFaction(Factions.NEUTRAL)
        target.removeTag(RATStrings.TAG_OUTPOST_PLANET)

        if (target is PlanetAPI)
        {
            target.customDescriptionId = "rat_looted_outpost_planet"
        }

        dialog.closeDialog()
    }
}