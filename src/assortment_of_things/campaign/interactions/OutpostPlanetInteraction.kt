package assortment_of_things.campaign.interactions

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.RATSettings
import assortment_of_things.strings.RATItems
import assortment_of_things.strings.RATTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.impl.campaign.FleetInteractionDialogPluginImpl.FIDConfig
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.getFactionsWithMarkets
import lunalib.lunaExtensions.getKnownHullmodSpecs
import lunalib.lunaExtensions.getKnownShipSpecs
import lunalib.lunaExtensions.getKnownWeaponSpecs
import org.lazywizard.lazylib.MathUtils
import kotlin.random.Random
import kotlin.random.nextInt

class OutpostPlanetInteraction : RATInteractionPlugin() {
    override fun init() {

        var target = interactionTarget as PlanetAPI
        visualPanel.showPlanetInfo(target)

        var station = targetMemory.get("\$defenderStation")
        if (station != null && station is CampaignFleetAPI && station.isAlive && !station.isEmpty && !station.isDespawning)
        {
            textPanel.addPara("You arrive at the orbit of a planet emitting more radar signatures than usual for a fringe system.\n\nThe fleet detects a hostile orbital station that will prevent us from approach any closer.")
            addLeaveOption()

            return
        }

        /*var defenderFleet = targetMemory.get("\$defenderFleet")
        if (defenderFleet != null && defenderFleet is CampaignFleetAPI && !defenderFleet.isEmpty )
        {
            var label = textPanel.addPara("You arrive at the orbit of a planet emitting more radar signatures than usual for a fringe system.\n\nThe fleets sensors detect some kind of construction at the surface. Its impossible to asume what this construction is for, but the multiple patrol fleets " +
                    "\nDescending any lower will ensure that they will start their fire towards us.")
            label.setHighlight("${interactionTarget.faction.displayName}")
            label.setHighlightColors(interactionTarget.faction.baseUIColor)

            var config = FIDConfig()
            triggerDefenders(config)

            return
        }*/
        else
        {
            noDefendersDialog()
        }
    }

    fun noDefendersDialog()
    {
        var l = textPanel.addPara("With the orbital station disabled, nothings stopping our fleet from descending towards the surface.")
        var label = textPanel.addPara("After flying down, your crew eventualy arrives at some kind of facility. After an \"unlucky\" encounter with some hostile forces remaining on ground" +
                "and some further exploration of it, they discover the purpose of the facility.\n\nIt holds archive to some Blueprints of ${interactionTarget.faction.displayNameWithArticle}'s fleet doctrine. Additionaly they appear to be storing commodities that are usually illegal under their own law.")
        label.setHighlight("${interactionTarget.faction.displayName}")
        label.setHighlightColors(interactionTarget.faction.baseUIColor)

        createOption("Loot the Facility of Valueables") {
            lootVault()
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

        if (faction.id == Factions.TRITACHYON && RATSettings.sillyContentEnabled!!) cargo.addCommodity(RATItems.JEFF, 1f)

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
}

private class LootListener(var dialog: RATInteractionPlugin) : CoreInteractionListener
{
    override fun coreUIDismissed() {
        var target = dialog.interactionTarget

        target.setFaction(Factions.NEUTRAL)
        target.removeTag(RATTags.TAG_OUTPOST_PLANET)

        if (target is PlanetAPI)
        {
            target.customDescriptionId = "rat_looted_outpost_planet"
        }

        dialog.closeDialog()
    }
}