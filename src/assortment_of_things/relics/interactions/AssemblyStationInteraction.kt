package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomProductionPickerDelegate
import com.fs.starfarer.api.campaign.FactionProductionAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.magiclib.kotlin.addCreditsLossText
import org.magiclib.kotlin.addFleetMemberGainText

class AssemblyStationInteraction : RATInteractionPlugin() {

    override fun init() {


        textPanel.addPara("Your fleet approaches the assembly station.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        textPanel.addPara("The station allows for assembling any known ships in a matter of days and improves the maximum combat readiness and armor of any ship constructed.",
        Misc.getTextColor(), Misc.getHighlightColor(), "days", "maximum combat readiness", "armor")

        textPanel.addPara("Despite being abandoned, the stations terminal requires an upfront payment for the production of the selected goods. It is unclear if the account it is wired to even exists in this day and age. " +
                "All ships have a marked up price of 25%%. The maximum amount of hulls to construct is equal to 250.000 credits.", Misc.getTextColor(), Misc.getHighlightColor(),
        "payment", "25%", "250.000")

        createOption("Choose hulls to assemble") {
            dialog.showCustomProductionPicker(object : CustomProductionPickerDelegate {
                override fun getAvailableShipHulls(): MutableSet<String> {
                    return Global.getSector().playerFaction.knownShips
                }

                override fun getAvailableWeapons(): MutableSet<String>? {
                    return null
                }

                override fun getAvailableFighters(): MutableSet<String>? {
                    return null
                }

                override fun getCostMult(): Float {
                    return 1.25f
                }

                override fun getMaximumValue(): Float {
                    return 250000f
                }

                override fun withQuantityLimits(): Boolean {
                    return true
                }

                override fun notifyProductionSelected(production: FactionProductionAPI?) {
                    var ships = production!!.current

                    clearOptions()

                    var fleet = Global.getSector().playerFleet

                    for (ship in ships) {

                        for (i in 0 until ship.quantity) {
                            var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, ship.specId + "_Hull")
                            fleet.fleetData.addFleetMember(member)
                            member.repairTracker.cr = member.repairTracker.baseCR

                            member.fixVariant()
                            member.variant.addPermaMod("rat_exceptional_construction")

                            textPanel.addFleetMemberGainText(member)
                        }
                    }

                    textPanel.addPara("The station constructs the selected hulls, making them available to the fleet. It seems that this exhausted the last of the stations capabilities.")

                    Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)
                    var cost = production.totalCurrentCost
                    fleet.cargo.credits.subtract(cost.toFloat())
                    textPanel.addCreditsLossText(cost)

                    createOption("Leave") {
                        closeDialog()
                        Misc.fadeAndExpire(interactionTarget)
                    }
                    optionPanel.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, true);
                }
            })
        }


        addLeaveOption()


    }
}

