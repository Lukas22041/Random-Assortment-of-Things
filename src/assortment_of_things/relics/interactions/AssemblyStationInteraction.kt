package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.fixVariant
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomProductionPickerDelegate
import com.fs.starfarer.api.campaign.FactionProductionAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard
import org.magiclib.kotlin.addCreditsLossText
import org.magiclib.kotlin.addFleetMemberGainText

class AssemblyStationInteraction : RATInteractionPlugin() {

    override fun init() {


        textPanel.addPara("Your fleet approaches the assembly station.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        createOption("Explore") {
            clearOptions()

            textPanel.addPara("You approach according to the basic directions transmitted on repeat by the station. On reaching the dock you ignore the message asking you to 'please wait for one of our helpful humdinger staff to help you make the best choice!', sending in your salvors to break in. \n\n" + "Alarms sound, are ignored, and are then killed in short order. Your crew chief, sensor officer and communications officer all crowd around a terminal, arguing about how to best access the station's systems; these stations are well-known for uploading truly cancerous advertisement programs into any ship system they can. You'd rather not have an annoying jingle for a long-dead smarmy ship shifter on repeat sounding from the comms system until the end of time.",
                Misc.getTextColor(), Misc.getHighlightColor(),
            )


            createOption("Continue") {

                clearOptions()

                textPanel.addPara("After setting up a truly mind-boggling complex system of firewalls and isolated communication systems, a salvor plugs in a hardline. Annoyingly the system will require credits to be deposited in order to function - some absurd DRM restriction used to ensure the manufacturer's liquidity. You're not even sure the account even still exists, let alone where the 25%% markup is going. On the plus side you'll be able to use any blueprint in your database, and the ships will be made up to Domain civilian spec, meaning they'll have increased ordnance points. The maximum amount you can order is 300.000.",
                    Misc.getTextColor(), Misc.getHighlightColor(),
                    "the system will require credits to be deposited in order to function", "25%", "any blueprint in your database", "increased ordnance points", "300.000")

                createOption("Choose hulls to assemble") {
                    dialog.showCustomProductionPicker(object : CustomProductionPickerDelegate {
                        override fun getAvailableShipHulls(): MutableSet<String> {
                            return Global.getSector().playerFaction.knownShips
                        }

                        override fun getAvailableWeapons(): MutableSet<String> {
                            return mutableSetOf<String>()
                        }

                        override fun getAvailableFighters(): MutableSet<String>? {
                            return mutableSetOf<String>()
                        }

                        override fun getCostMult(): Float {
                            return 1.25f
                        }

                        override fun getMaximumValue(): Float {
                            return 300000f
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
                                    member.variant.addMod("rat_exceptional_construction")

                                    textPanel.addFleetMemberGainText(member)
                                }
                            }

                            textPanel.addPara("The Assembly Station whirs into life, quickly processing your order as it hungrily slurps away your credits to Ludd knows where. In mere days your new ships emerge, ready to have their internal systems thoroughly cleaned of infuriating advertisements. Once the ships are free the station crunks ominously into inertness - you're not sure if something fundamental has broken, or whatever nanoforge inside has gone rampant, but regardless of cause this station is now useless.")

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

        addLeaveOption()

    }
}

