package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard

class RefurbishmentStationInteraction : RATInteractionPlugin() {

    override fun init() {

        textPanel.addPara("Your fleet approaches the refurbishment yard.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)


        createOption("Continue") {
            clearOptions()

            textPanel.addPara("On approach the station goes into lockdown. Bulkheads close, doorways seal, and the radio pings for the nearest Domain patrol. While this may have worked back in the day when such a patrol would be a short jump away, now its merely an annoyance. Your salvors are easily able to cut into the craft, and you watch mildly disinterested as they steadily move through the command structure, stripping away the defensive systems until you're able to directly integrate with the repair system. The dockspace opens up with a mildly shuddering motion, revealing warming up repair drones.\n\n" + "There is enough remaining feedstock in the system to improve one ship. This improvement won't change the combat performance of the ship, but will allow the installation of an additional s-mod.",
                Misc.getTextColor(), Misc.getHighlightColor(),
                "improve one ship", "allow the installation of an additional s-mod")

            createOption("Select a ship") {

                dialog.showFleetMemberPickerDialog("Select a ship", "Confirm", "Cancel", 8, 12, 64f, true, false,
                    Global.getSector().playerFleet.fleetData.membersListCopy.filter { !it.variant.hasHullMod("rat_refurbished_hull") }, object : FleetMemberPickerListener {
                        override fun pickedFleetMembers(members: MutableList<FleetMemberAPI>?) {
                            if (members!!.isEmpty()) return

                            Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)

                            clearOptions()

                            var member = members.first()
                            member.fixVariant()

                            member.variant.addPermaMod("rat_refurbished_hull")


                            textPanel.addPara("Your Salvors escape from the station, and from a safe distance activate the repair protocol. You watch as the station devours itself - apparently the 'remaining feedstock' was the station itself. Soon your ship the ${member.shipName} emerges, looking sparkling new as the station disappears into a cloud of useless junk.")

                            textPanel.addPara("> The ${member.shipName} gained the \"Refurbished\" hullmod.", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())

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