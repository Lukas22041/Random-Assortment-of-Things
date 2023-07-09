package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.random.Random
import kotlin.random.asJavaRandom

class DomainResearchInteraction : RATInteractionPlugin() {

    var shieldingInterval = 30
    var lastSiphonKey = "\$rat_last_shipon"

    override fun init() {

        if (AbyssUtils.anyNearbyFleetsHostileAndAware())
        {
            textPanel.addPara("As there are currently hostile targets following the fleets steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet is docked at what appears to be a research station. There are no signs life having been anywhere close to it for a long time.")

        //chance for additional interactions, i.e picking a alteration

        //Transfer a bit of shielind material, can do it once a month

        if (interactionTarget.hasTag(AbyssTags.LOOTABLE)) {
            textPanel.addPara("As the stations has been untouched by salvagers, it is likely that there are some valueables to be found within this station.")
            addLootOption()
        }

        if (interactionTarget.hasTag(AbyssTags.DOMAIN_RESEARCH_PRODUCTION)) {
            textPanel.addPara("The station seems to have some kind of production line within it.")
            addProductionOption()
        }
        else if (interactionTarget.hasTag(AbyssTags.DOMAIN_RESEARCH_SURVEY)) {
            textPanel.addPara("Some transponders appear to have send data to this station.")
            addSurveyOption()
        }

       /* textPanel.addPara("The station generates a constant amount of shielding for itself through some unknown process, we could likely take some of it for our own.")
        addSiphonOption()*/


        addLeaveOption()

    }



    fun addLootOption()
    {
        createOption("Survey the station for valueables") {
            clear()
            var tier = AbyssUtils.getTier(interactionTarget.starSystem)

            textPanel.addPara("> Survey the station for valueables", Misc.getBasePlayerColor(), Misc.getBasePlayerColor())

            textPanel.addPara("The crew explores the station in search for anything of value.")
            when (tier) {
                AbyssProcgen.Tier.Low ->
                    textPanel.addPara("After a short exploration of their surroundings, it seems that this station isnt particuarly loaded with items of interest, but that some things may be " +
                            "worth taking.")
                AbyssProcgen.Tier.Mid ->
                    textPanel.addPara("After some time, the crew reports that they suspect a decent sum of valueables to be within this station. It may contain some components that you wouldnt find anywhere within the sector.")
                AbyssProcgen.Tier.High ->
                    textPanel.addPara("Just a few minutes in to the survey process, the crew frantically reports back, it seems that this station may be filled with tons of different artifacts.")
            }

            var random = Random(interactionTarget.getSalvageSeed())
            createOption("Take the valueables") {
                var dropRandom = ArrayList<DropData>()
                var dropValue = ArrayList<DropData>()
                var drop = DropData()

                if (tier == AbyssProcgen.Tier.Low) {

                    drop = DropData()
                    drop.chances = 2
                    drop.group = "abyss_research_loot"
                    dropRandom.add(drop)

                    drop = DropData()
                    drop.chances = 1
                    drop.group = "blueprints_low"
                    dropRandom.add(drop)

                    drop = DropData()
                    drop.chances = 1
                    drop.group = "rare_tech_low"
                    drop.valueMult = 0.1f
                    dropRandom.add(drop)

                    drop = DropData()
                    drop.chances = 1
                    drop.group = "any_hullmod_low"
                    dropRandom.add(drop)

                }
                else
                {

                    drop = DropData()
                    drop.chances = 3
                    drop.group = "abyss_research_loot"
                    dropRandom.add(drop)

                    drop = DropData()
                    drop.chances = 1
                    drop.group = "rare_tech"
                    drop.valueMult = 0.1f
                    dropRandom.add(drop)

                    drop = DropData()
                    drop.chances = 1
                    drop.group = "blueprints"
                    dropRandom.add(drop)

                    drop = DropData()
                    drop.chances = 1
                    drop.group = "any_hullmod_medium"
                    dropRandom.add(drop)
                }

                drop = DropData()
                drop.chances = 5
                drop.group = "weapons2"
                dropRandom.add(drop)

                drop = DropData()
                drop.group = "basic"
                drop.value = 10000
                dropValue.add(drop)

                var mult = when(tier) {
                    AbyssProcgen.Tier.Low -> 1f
                    AbyssProcgen.Tier.Mid -> 2f
                    AbyssProcgen.Tier.High -> 2.5f
                    else -> 1f
                }

                var salvage = SalvageEntity.generateSalvage(random.asJavaRandom(), mult, mult, 1f, 1f, dropValue, dropRandom)

                visualPanel.showLoot("Loot", salvage, true) {

                    interactionTarget.removeTag(AbyssTags.LOOTABLE)
                    reset()
                }
            }
        }
    }

    fun addProductionOption()
    {
        createOption("Investigate assembly line") {
            interactionTarget.removeTag(AbyssTags.DOMAIN_RESEARCH_PRODUCTION)
            clear()
            textPanel.addPara("> Investigate assembly Line", Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
            textPanel.addPara("You come across some form of assembly line. As you further investigate it, it seems that its been in the middle of assembling something with its last batch of resources" +
                    ", but requires some further confirmation to complete it. Close by there is a terminal that reads.")

            textPanel.addPara("\"Select an alteration type to proceed with. \nAvailable options are: \n\n" +
                    "Shipsystem\n" +
                    "Fightersystem\n\n" +
                    "Which one do you want to select?\"", Misc.getTextColor(), Misc.getHighlightColor(), "Shipsystem", "Fightersystem")


            createOption("Select Shipsystem") {
                clearOptions()
                textPanel.addPara("> Select Shipsystem", Misc.getBasePlayerColor(), Misc.getBasePlayerColor())

                var randomAlteration = Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") && it.hasTag("shipsystem") }
                    .random(Random(interactionTarget.getSalvageSeed()))

                textPanel.addPara("The assembly line continues production, proceeding to print out a Hull Alteration. ",
                    Misc.getTextColor(), Misc.getHighlightColor(), "")

                textPanel.addPara("> Aquirred ${randomAlteration.displayName} Hull Alteration", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())
                Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_install", randomAlteration!!.id), 1f)

                createOption("Return to Dock") {
                    reset()
                }
            }

            createOption("Select Fightersystem") {
                clearOptions()
                textPanel.addPara("> Select Shipsystem", Misc.getBasePlayerColor(), Misc.getBasePlayerColor())

                var randomAlteration = Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") && it.hasTag("fightersystem") }
                    .random(Random(interactionTarget.getSalvageSeed()))

                textPanel.addPara("The assembly line continues production, proceeding to print out a Hull Alteration. ",
                    Misc.getTextColor(), Misc.getHighlightColor(), "")

                textPanel.addPara("> Aquirred ${randomAlteration.displayName} Hull Alteration", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())
                Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_secondary_install", randomAlteration!!.id), 1f)

                createOption("Return to Dock") {
                    reset()
                }
            }
        }
    }

    fun addSurveyOption()
    {
        createOption("Collect research data") {
            interactionTarget.removeTag(AbyssTags.DOMAIN_RESEARCH_SURVEY)
            reset()
            textPanel.addPara("> Collect research data", Misc.getBasePlayerColor(), Misc.getBasePlayerColor())

            textPanel.addPara("A long time ago local research transmitters send local terrain data towards this station, piling up its databanks. With nobody" +
                    "there to pick them up, those are now yours.")

            var extra = Random(interactionTarget.getSalvageSeed()).nextFloat() >= 0.5f
            var amount = when(AbyssUtils.getTier(interactionTarget.starSystem)) {
                AbyssProcgen.Tier.Low -> 1
                AbyssProcgen.Tier.Mid -> 2
                AbyssProcgen.Tier.High -> 2
            }
            if (extra) amount += 1

            var tooltip =textPanel.beginTooltip()

            var image = tooltip.beginImageWithText("graphics/icons/cargo/rat_abyss_survey.png", 64f)
            image.addPara("Aquirred ${amount}x Abyssal Survey Data", 0f)
            tooltip.addImageWithText(0f)

            textPanel.addTooltip()

            Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_abyss_survey", null), amount.toFloat())

        }
    }

    fun addSiphonOption()
    {
        var lastSiphon = memory.get(lastSiphonKey) as Long?
        if (lastSiphon == null)
        {
            lastSiphon = 0.toLong()
            memory.set(lastSiphonKey, lastSiphon)
        }

        var tier = AbyssUtils.getTier(interactionTarget.starSystem)
        var shieldingToRestore = when (tier) {
            AbyssProcgen.Tier.Low -> 15f
            AbyssProcgen.Tier.Mid -> 25f
            AbyssProcgen.Tier.High -> 40f
        }

        createOption("Restore shielding resources") {
            lastSiphon = Global.getSector().clock.timestamp
            memory.set(lastSiphonKey, lastSiphon)

            optionPanel.setEnabled("Restore shielding resources", false)

            AbyssUtils.restoreShielding(shieldingToRestore)

        }

        var days = Global.getSector().clock.getElapsedDaysSince(lastSiphon!!)

        if (days < 30)
        {
            var daysLeft = (shieldingInterval - days).toInt()
            optionPanel.setEnabled("Restore shielding resources", false)
            optionPanel.addOptionTooltipAppender("Restore shielding resources") { tooltip: TooltipMakerAPI, hadOtherText: Boolean ->
                tooltip.addPara("This station seems process a low amount of shielding resources, allowing us to take $shieldingToRestore units once a month." +
                        "\n\n" +
                        "There are $daysLeft days remaining until we can take another round of shielding supplies.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
                    "shielding resources", "$shieldingToRestore", "once a month", "$daysLeft")
            }
        }
        else
        {
            optionPanel.addOptionTooltipAppender("Restore shielding resources") { tooltip: TooltipMakerAPI, hadOtherText: Boolean ->
                tooltip.addPara("This station seems process a low amount of shielding resources, allowing us to take $shieldingToRestore units once a month.", 0f, Misc.getTextColor(), Misc.getHighlightColor(),
                    "shielding resources", "$shieldingToRestore", "once a month")
            }
        }

    }

}