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

        textPanel.addPara("Despite its age, it appears that some of the machines are still functional. They do however look as if they will fall appart just after one use.")
        textPanel.addPara("Selecting a ship for refurbishment will allow it to install an additional s-mod", Misc.getTextColor(), Misc.getHighlightColor(), "s-mod")


        createOption("Select a ship") {
            dialog.showFleetMemberPickerDialog("Select a ship", "Confirm", "Cancel", 8, 12, 64f, true, false,
            Global.getSector().playerFleet.fleetData.membersListCopy, object : FleetMemberPickerListener {
                    override fun pickedFleetMembers(members: MutableList<FleetMemberAPI>?) {
                        if (members!!.isEmpty()) return

                        Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)

                        clearOptions()

                        var member = members.first()
                        member.fixVariant()

                        member.variant.addPermaMod("rat_refurbished_hull")


                        textPanel.addPara("The station begins the process, and in the end it the ${member.shipName} comes out just as if it were new. Just as it finished, the terminal stops functioning.")

                        textPanel.addPara("> The ${member.shipName} gained the \"Refurbished Hull\" hullmod.", Misc.getPositiveHighlightColor(), Misc.getPositiveHighlightColor())

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

}