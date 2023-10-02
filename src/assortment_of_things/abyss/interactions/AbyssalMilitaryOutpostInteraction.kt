package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard

class AbyssalMilitaryOutpostInteraction : RATInteractionPlugin() {

    override fun init() {

        if (AbyssUtils.isAnyFleetTargetingPlayer())
        {
            textPanel.addPara("As there are currently hostile targets following the fleets steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet approaches the military outpost.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)


        createOption("Explore") {
            clearOptions()

            textPanel.addPara("You make way through the outpost, all valueables seem to have been taken when the last man left. However you soon discover the controls for some kind of modification bay on the station.")

            textPanel.addPara("This bay appears to use technology developed with abyssal matter to improve the energy mounts on a ship. It is able to decrease the ordnance cost of energy weapons by 1/2/3 based on mount size. " + "However, the bay is not large enough to be able to service capital ships, and the stations capacitators can likely only sustain operation on one single ship.",
                Misc.getTextColor(), Misc.getHighlightColor(),
                "ordnance cost", "energy weapons", "1/2/3", "capital ships", "one single ship")

            createOption("Select a ship") {

                dialog.showFleetMemberPickerDialog("Select a ship", "Confirm", "Cancel", 8, 12, 64f, true, false,
                    Global.getSector().playerFleet.fleetData.membersListCopy.filter { !it.isCapital }, object : FleetMemberPickerListener {
                        override fun pickedFleetMembers(members: MutableList<FleetMemberAPI>?) {
                            if (members!!.isEmpty()) return

                            Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)

                            clearOptions()

                            var member = members.first()
                            member.fixVariant()

                            member.variant.addPermaMod("rat_ordnance_redistributor")


                            textPanel.addPara("The ${member.shipName} enters the bay and the station immediately starts with the procedure. After a short while it finishes, and the lights immediately turn dark.")

                            textPanel.addPara("> The ${member.shipName} gained the \"Ordnance Redistributor\" hullmod.", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())

                            createOption("Leave") {
                                closeDialog()
                                Misc.fadeAndExpire(interactionTarget)
                            }
                            optionPanel.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, true);
                        }

                        override fun cancelledFleetMemberPicking() {

                        }

                    })
            }

            addLeaveOption()


        }


        addLeaveOption()
    }

}