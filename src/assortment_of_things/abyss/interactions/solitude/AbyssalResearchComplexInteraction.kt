package assortment_of_things.abyss.interactions.solitude

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgenUtils
import assortment_of_things.abyss.procgen.MapRevealerScript
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.MapParams
import com.fs.starfarer.api.ui.MarkerData
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.getSalvageSeed
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

class AbyssalResearchComplexInteraction : RATInteractionPlugin() {
    override fun init() {

        if (AbyssUtils.isAnyFleetTargetingPlayer())
        {
            textPanel.addPara("As there are currently hostile targets following the fleet's steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }






        textPanel.addPara("Your fleet approaches the abyssal research complex.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        var defender = interactionTarget.memoryWithoutUpdate.get("\$defenderFleetDefeated")
        if (defender != null) {
            textPanel.addPara("Scans indicate that there is nothing of value remaining.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Immediately upon closing in, a fleet makes its presence more than just known.")

        triggerDefenders()
    }

    override fun defeatedDefenders() {
        clearOptions()

        textPanel.addPara("With the enemy fleet gone, nows the time to move closer to the station. Crew makes its way in to the internals of the complex.")

        textPanel.addPara("Unsuprisingly, multiple aspects of interest are quickly being discovered. Your intel team prepares a list of discovered logs, while the salvage team prepares to extract worthwhile resources.")

        createOption("Read log \"Abyssal Matter\"") {
            optionPanel.setEnabled("Read log \"Abyssal Matter\"", false)

            var tooltip = textPanel.beginTooltip()
            tooltip.addTitle("Abyssal Matter")
            tooltip.addSpacer(5f)
            tooltip.addPara("“Abyssal Matter, the reason all of us are here, is the most exciting thing i’ve been able to study in my whole life…It is the most mutable material we’ve come across, impulses from electromagnetic waves, gravitational condensing and further physical stimuli allow us to manipulate it in any way we want. Be that reshaping the material in to new forms, or creating controlled emissions of energy\n" + "\n" +
                    "…this would be if we fully understand how it reacted to those, yet. We come closer with most attempts, but others entirely upend everything we know about it.\n" + "\n" +
                    "One of our recent experiments has been the most interesting so far. When exposed to electric stimuli, it transmits on to other attached samples. Now, if those samples are separated and the same test is repeated, the signal continues to transmit on to the other sample, despite their physical distance.”\n",
                0f, Misc.getTextColor(), Misc.getHighlightColor(), "")

            textPanel.addTooltip()
        }


        createOption("Read log \"Enviromental Study\"") {
            optionPanel.setEnabled("Read log \"Enviromental Study\"", false)

            var tooltip = textPanel.beginTooltip()
            tooltip.addTitle("Enviromental Study")
            tooltip.addSpacer(5f)
            var para = tooltip.addPara("“The depths exhibit a unique amount of variance for something within the bounds of hyperspace, perhaps even space in general. Be it the quiet Sea of Tranquility, the shining Resplendent Sea, or the warm embrace of the Sea of Harmony. Each of those, despite their common root, invoke different emotions in those treading through them, as no doubt can be felt by the names assigned to them.\n" + "\n" +
                    "As with many things, the “why” has yet to be worked out though…for what reason do those differences exist, and how did they come to be?”\n")

            para.setHighlight("Sea of Tranquility", "Resplendent Sea", "Sea of Harmony")
            para.setHighlightColors(Color(255, 0, 50), Color(199, 191, 117), Color(255, 84, 50))

            textPanel.addTooltip()
        }

        createOption("Read log \"Blackout\"") {
            optionPanel.setEnabled("Read log \"Blackout\"", false)

            var tooltip = textPanel.beginTooltip()
            tooltip.addTitle("Blackout")
            tooltip.addSpacer(5f)
            var para = tooltip.addPara("“It has been weeks since what we call the “Blackout” has occurred. Nothing but our emergency power works, and none of our autonomous systems respond. Our communication arrays lack the power for far-distance exchanges, but we managed to establish a wide-range link through a chain of stations transmitting messages from one area of the depths to the other.\n" + "\n" +
                    "It appears all other stations have experienced a similar fate. Analysing the logs of several stations however reveals a slight deviation in timing, stations closer to the Sea of Tranquility experienced the blackout earlier than others have, with the earliest occurrence appearing close to the biomes edge. If we could conduct an expedition out there, we may be able to find the cause, but the blackout left our ships entirely inert.\n" + "\n" +
                    "We can only hope that our engineers find a way to bring the power back online, or hope the next expedition finally arrives from beyond the gates, which is assuming their equipment doesnt malfunction on entry either.”\n")

            para.setHighlight("Sea of Tranquility")
            para.setHighlightColors(Color(255, 0, 50))

            textPanel.addTooltip()

            displayBlackoutLocation()

        }

        createOption("Salvage and Leave") {
            MapRevealerScript.tempEnableReveal = false
            clearOptions()

            var random = Random(interactionTarget.getSalvageSeed())

            var dropRandom = ArrayList<DropData>()
            var dropValue = ArrayList<DropData>()
            var drop = DropData()



            drop = DropData()
            drop.group = "basic"
            drop.value = 12500
            dropValue.add(drop)

            /* drop = DropData()
             drop.chances = 1
             drop.group = "abyss_guaranteed_alt"
             dropRandom.add(drop)*/

            drop = DropData()
            drop.chances = 3
            drop.group = "rat_abyss_alteration_common"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 14
            drop.group = "rat_abyss_weapons"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "rat_abyss_artifact_rare"
            dropRandom.add(drop)


            drop = DropData()
            drop.chances = 1
            drop.group = "rare_tech"
            drop.valueMult = 1f
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "blueprints"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "any_hullmod_medium"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 3
            drop.group = "rat_abyss_transmitter"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 4
            drop.group = "weapons2"
            dropRandom.add(drop)

            var biome = AbyssUtils.getBiomeManager().getCell(interactionTarget).getBiome()
            var mult = biome?.getLootMult() ?: 1f

            var salvage = SalvageEntity.generateSalvage(random, mult, mult, 1f, 1f, dropValue, dropRandom)
            salvage.addCommodity("rat_abyssal_matter", AbyssProcgenUtils.getAbyssalMatterDrop(interactionTarget))
            salvage.sort()

            visualPanel.showLoot("Loot", salvage, true) {
                closeDialog()
            }
        }


    }

    fun displayBlackoutLocation() {
        var catalyst = AbyssUtils.getSystem()?.customEntities?.find { it.customEntitySpec.id == "rat_abyss_primordial_activator" }

        //Reveal on Map
        MapRevealerScript.tempEnableReveal = true
        var biomeManager = AbyssUtils.getBiomeManager()
        var playercell = biomeManager.getCell(catalyst!!)
        playercell.isDiscovered = true

        var rad = 3
        playercell.getAround(rad).forEach {
            it.isDiscovered = true
            it.getAdjacent().forEach { it.isPartialyDiscovered = true }
        }

        //Add UI
        var panel = visualPanel.showCustomPanel(500f, 600f, null)
        var element = panel.createUIElement(500f, 600f, false)
        panel.addUIElement(element)

        var params = MapParams()
        params.location = AbyssUtils.getSystem()
        var marker = MarkerData(catalyst!!.location, AbyssUtils.getSystem())
        params.markers = listOf(marker)
        params.zoomLevel = 10f
        params.centerOn = catalyst.location

        element.addTitle("Estimated location of the blackouts source")
        var map = element.createSectorMap(400f, 200f, params, null)
        map.position.inTL(5f, 20f)
        element.addComponent(map)
    }

}