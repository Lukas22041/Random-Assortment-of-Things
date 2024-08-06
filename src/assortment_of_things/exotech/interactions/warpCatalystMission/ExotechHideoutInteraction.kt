package assortment_of_things.exotech.interactions.warpCatalystMission

import assortment_of_things.exotech.ExoUtils
import assortment_of_things.exotech.intel.missions.WarpCatalystMissionIntel
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.util.Misc

class ExotechHideoutInteraction() : RATInteractionPlugin() {

    var data = ExoUtils.getExoData()

    override fun init() {

        var intel = Global.getSector().intelManager.getFirstIntel(WarpCatalystMissionIntel::class.java)

        if (data.finishedWarpCatalystMission || data.finishedWarpCatalystMissionEntirely) {
            textPanel.addPara("Your fleet approaches the abandoned station.")

            textPanel.addPara("There is nothing left here anymore.")

            addLeaveOption()
        }
        else if (intel == null) {

            textPanel.addPara("Your fleet approaches the abandoned station.")

            textPanel.addPara("Scans in closer proximity reveal extensive damage to the structure. " +
                    "The station appears to have been used as a habitat before, but without extensive work, it likely cant be used to house anything anymore.")


            addLeaveOption()
        }
        else {
            textPanel.addPara("Your fleet approaches the abandoned station.")

            textPanel.addPara("Close scans indicate recent activity within the station, we should approach carefully. ")

            createOption("Continue") {

                textPanel.addPara("Within moments a whole fleet emerges from remains of the station, they hold their positions close towards it, and don't appear to be on an attack run yet. " +
                        "Any attempt to close in will escalate the situation.")

                textPanel.addPara("Judging by the specs visible on the fleet's scopes, this is the exotech defector fleet you have been tasked to eliminate. " +
                        "It has to be defeated to get access to the Warp Catalyst.",
                    Misc.getTextColor(), Misc.getHighlightColor(), "this is the exotech defector fleet you have been tasked to eliminate", "Warp Catalyst")

                triggerDefenders()
            }
        }

    }

    override fun defeatedDefenders() {
        clearOptions()

        textPanel.addPara("With the fleet defeated, you order an inspection of the cargo holds of all the wrecks in the vicinity, but none of them appear to contain the Warp Catalyst.")

        textPanel.addPara("As extended scans indiciate, no one is on the station anymore, most appear to have joined the fight, though some may have escaped in shuttles undetected. You order your crew to dock at the station and further search through its interiors.")

        createOption("Continue") {
            clearOptions()

            textPanel.addPara("Your crew continues through the corridors, most of them are decayed beyond airtightness, but some appear to have been recently restored to operatable conditions. " +
                    "The defectors likely tried to make this place their new home.")

            textPanel.addPara("As most corridors appear empty, the crew makes quick progress through the station. Rather suddenly your crew calls in discovering a heavily protected cache.")

            createOption("Order a specialist to open the cache") {
                clearOptions()

                textPanel.addPara("The specialist appears on the scene and takes a good look on the crate. After several hours, he forces it open, and a worthwhile reward jumps out.")

                var tooltip = textPanel.beginTooltip()

                var img = tooltip.beginImageWithText(Global.getSettings().getSpecialItemSpec("rat_warp_catalyst").iconName, 48f)
                img.addPara("Acquired a \"Warp Catalyst\". It has been transferred to the fleet's cargo hold.", 0f,
                    Misc.getTextColor(), Misc.getHighlightColor(), "Warp Catalyst")
                tooltip.addImageWithText(0f)

                textPanel.addTooltip()

                textPanel.addPara("With the catalyst in hand, you should return to Xander to discuss how to continue.")

                var intel = Global.getSector().intelManager.getFirstIntel(WarpCatalystMissionIntel::class.java) as WarpCatalystMissionIntel
                intel.finish()

                Global.getSector().intelManager.addIntelToTextPanel(intel, textPanel)

                Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_warp_catalyst", ""), 1f)

                addLeaveOption()
            }
        }
    }
}