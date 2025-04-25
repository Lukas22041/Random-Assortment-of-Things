package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssSensorEntity
import assortment_of_things.abyss.procgen.BiomeDepth
import assortment_of_things.abyss.procgen.biomes.*
import assortment_of_things.abyss.procgen.scripts.AbyssalLightDiscovery
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.addPara
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc

class AbyssSensorInteraction : RATInteractionPlugin() {
    override fun init() {

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        if (!interactionTarget.hasTag("scanned")) {

            textPanel.addPara("Your crews ready to salvage what information this array may hold.")

            createOption("Order your crew to extract the arrays data") {
                clearOptions()

                var biome = (interactionTarget.customPlugin as AbyssSensorEntity).biome

                when (biome) {
                    is SeaOfTranquility -> addTranquilityDialog()
                    is SeaOfSerenity -> addSerenityDialog()
                    is SeaOfHarmony -> addHarmonyDialog()
                    is SeaOfSolitude -> addSolitudeDialog()
                    is AbyssalWastes -> addWastesDialog()
                    else -> closeDialog()
                }
            }
        }
        else {
            textPanel.addPara("Any potential information has already been extracted.")
        }


        addLeaveOption()
    }

    fun revealBiome() {
        var biome = (interactionTarget.customPlugin as AbyssSensorEntity).biome!!

        biome.isSensorRevealed = true

        for (cell in biome.cells) {
            if (cell.depth != BiomeDepth.BORDER) {
                cell.isDiscovered = true
                cell.isPartialyDiscovered = true
            } else {
                cell.isPartialyDiscovered = true
            }
        }

        //Reveal major lightsources
        var lightRevealer = Global.getSector().scripts.find { it is AbyssalLightDiscovery } as AbyssalLightDiscovery
        for (light in biome.majorLightsources) {
            lightRevealer.reveal(light)
        }

        var tooltip = textPanel.beginTooltip()

        var path = "graphics/icons/intel/rat_abyss_sensor.png"
        Global.getSettings().getAndLoadSprite(path)
        var img = tooltip.beginImageWithText(path, 48f)
        var para = img.addPara("Updated map data for the ${biome.getDisplayName()}. Your fleets sensor range is increased by 100% while moving throughout the biome.")

        para.setHighlight("${biome.getDisplayName()}", "100%")
        para.setHighlightColors(biome.getTooltipColor(), Misc.getHighlightColor())

        tooltip.addImageWithText(0f)

        textPanel.addTooltip()
    }

    fun addTranquilityDialog() {

        textPanel.addPara("The sensors team extracts and collates the array’s data for analysis, " +
                "letting the sensors software pick through it for hyperwave signals, high-energy readings, neutrino spikes, EM-fields indicative of conscious engineering.")

        textPanel.addPara("But at first, all it reports is undefined “exotic matter”, swirling in cyclical motions, waxing and waning like the tides, choking out everything else." +
                " When it’s filtered out - along with the consistent EM-charge readings of the local storms - the same story is told again and again.")

        textPanel.addPara("Drive outputs, moving with trademark human irrationality, clear against the harmony of the roiling depths. Then, high-energy spikes, a final comms burst, and silence. " +
                "Until the next bubble arrives, and until the last one goes quiet, a final drifting hulk illuminated by the fading neutrino of its dead reactor. And now, you.")

        revealBiome()

        interactionTarget.addTag("scanned")

        addLeaveOption()
    }

    fun addSerenityDialog() {

        textPanel.addPara("The chronological playback of the visual data from the array is choked in darkness, " +
                "a suffocating mire of exotic matter readings and transient drive signatures from what lurks just out of sight.")

        textPanel.addPara("The only reprieve you see are the amber beacons scattered throughout the fog like lighthouses, lonely stars in the night sky. " +
                "They loose luminous cries into the abyss to beat back the darkness, never for long, but long enough to save themselves from drowning in it all. " +
                "The futility never seems to dawn on them.")

        textPanel.addPara("Sensors collates last known vectors for the beacons for future usage.")

        revealBiome()

        interactionTarget.addTag("scanned")

        addLeaveOption()

    }

    fun addHarmonyDialog() {

        textPanel.addPara("A persistent, amber warmth pervades the visual data presented to you by your sensors officer. Played back chronologically, it’s soothing, almost welcoming. Like gentle flame.")

        textPanel.addPara("The vast photospheres that illuminate the environment give it a degree of hospitality that you could get lost in. " +
                "That could let you forget for just a moment the danger and cruelty of this place, that it is indeed a sea of unidentified exotic matter and vicious automated hunters.")

        textPanel.addPara("Looking at it, you could almost feel at peace.")

        textPanel.addPara("You snap to attention as Sensors reports that all useful data has been integrated into the nav team’s workflow.")

        revealBiome()

        interactionTarget.addTag("scanned")

        addLeaveOption()

    }

    fun addSolitudeDialog() {

        textPanel.addPara("Sensors pulls the data from the array, reads the life in the localized spectrograms. " +
                "Some are quiet, calm, the exotic matter levels a fuzzy noise like you’d hear from a dying comms unit, EM-wash from flittering particles.")

        textPanel.addPara("Where the fog is thin, however, there are bright ripples through the visual-spectrum snapshots. Powerful, drowning, but arrhythmic. " +
                "Somewhere deeper where it can’t be seen, the sickened heart of this place beats brief vigor into the data, and everything not enshrouded is a clear shadow cast against the light, again and again with each pulse.")

        textPanel.addPara("The sensors team collects a list of these pinprick shadows for later reference alongside probabilities - derelict ship, decaying orbital, hostile fleet…")

        revealBiome()

        interactionTarget.addTag("scanned")

        addLeaveOption()

    }

    fun addWastesDialog() {

        textPanel.addPara("There’s an agitated undertone to the call-and-response of your sensors officer and their team as they work on collecting and making sense of the array’s data.")

        textPanel.addPara("\"There’s, um, a problem,” they finally report.\"")

        createOption("\"A problem?\"") {
            clearOptions()

            textPanel.addPara("\"The sensor array is on standby. It’s been idle for who-knows-how-long.\"")

            textPanel.addPara("You’re handed a TriPad displaying what parsed visual data could be extracted. " +
                    "Photospheres across the locality cast vibrant golden light through the abyssal stormclouds, illuminating a seascape with a persistent impulse of life. " +
                    "Exotic charged particles whirl in a circular motion around its border, like the edge of a whirlpool, and…")

            textPanel.addPara("… you take a moment to cast a gaze out at the suffocating, all-encompassing darkness.")

            textPanel.addPara("Skimming to the end, the sensor software catches a single anomaly, a misshapen low-emission splotch like a shadow cast against the brilliance for only a single breath. Then, nothing. Nothing at all.")

            revealBiome()

            interactionTarget.addTag("scanned")

            addLeaveOption()
        }
    }
}