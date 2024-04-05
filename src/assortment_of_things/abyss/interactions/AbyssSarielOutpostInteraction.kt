package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssDepth
import assortment_of_things.artifacts.ArtifactUtils
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.fixVariant
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.InteractionDialogImageVisual
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.DerelictShipEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.*
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity

import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.collections.ArrayList

class AbyssSarielOutpostInteraction : RATInteractionPlugin() {


    override fun init() {


        if (AbyssUtils.isAnyFleetTargetingPlayer())
        {
            textPanel.addPara("As there are currently hostile targets following the fleets steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet approaches the outpost.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)


        createOption("Explore") {
            clearOptions()

            var path = "graphics/illustrations/rat_unique_sariel.jpg"
            var sprite = Global.getSettings().getAndLoadSprite(path)
            var interactionImage = InteractionDialogImageVisual(path, sprite.width, sprite.height)
            visualPanel.showImageVisual(interactionImage)


            textPanel.addPara("The team enters the outposts docks and quickly make a discovery. A fully intact abyssal droneship just laying passively within the hangar. Theres no signs of recent activity.")

            textPanel.addPara("You try to recover logs from within the stations but to no avail you are unable to find anything, besides locating a room of currently unused cryosleepers, a feature not often seen within stations made for these depths.")

            textPanel.addPara("After you order further inspection of the ship, your engineers make the suprising discovery that the ship has been modified for human use. It is possible that if recovered, the components used for this procedure could be re-used for other ships of its kind.")


            createOption("Inspect the ships specs") {
                clearOptions()

                textPanel.addPara("You order the engineers to take a deeper look at the ships specs to decide if the ship is worth recovering.")

                var member = Global.getFactory().createFleetMember(FleetMemberType.SHIP, "rat_sariel_Strike")
                member.fixVariant()
                member.variant.addMod("rat_abyssal_conversion")

                member.repairTracker.cr = 0.7f
                visualPanel.showFleetMemberInfo(member, true)


                createOption("Recover") {
                    dialog.showFleetMemberRecoveryDialog("Select ships to recover", listOf(member), object :
                        FleetMemberPickerListener {
                        override fun pickedFleetMembers(selected: MutableList<FleetMemberAPI>?) {
                            var playerFleet = Global.getSector().playerFleet
                            for (member in selected!!) {
                                recoverShip(member)
                                closeDialog()
                                Misc.fadeAndExpire(interactionTarget)
                            }
                        }

                        override fun cancelledFleetMemberPicking() {

                        }
                    })
                }

                createOption("Extract the conversion components without ship recovery." ) {
                    clearOptions()

                    textPanel.addPara("You order the crew to rip the relevant components out without much care, leaving the ship in an unuseable state, but allowing the recovery of the conversion kit without much effort.")

                    var special = SpecialItemData("rat_alteration_install", "rat_abyssal_conversion")
                    AddRemoveCommodity.addItemGainText(special, 1, textPanel)
                    Global.getSector().playerFleet.cargo.addSpecial(special, 1f)

                    createOption("Leave") {
                        closeDialog()
                        Misc.fadeAndExpire(interactionTarget)
                    }
                }

                addLeaveOption()


            }

        }

        addLeaveOption()

    }

    fun recoverShip(member: FleetMemberAPI) {
        var playerFleet = Global.getSector().playerFleet
        /*val minHull: Float = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MIN, 0f)
        val maxHull: Float = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_HULL_MAX, 0f)
        val minCR: Float = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MIN, 0f)
        val maxCR: Float = playerFleet.getStats().getDynamic().getValue(Stats.RECOVERED_CR_MAX, 0f)
        var hull = Math.random().toFloat() * (maxHull - minHull) + minHull
        hull = Math.max(hull, member.status.hullFraction)
        member.status.hullFraction = hull
        val cr = Math.random().toFloat() * (maxCR - minCR) + minCR
        member.repairTracker.cr = cr*/
        member.repairTracker.cr = 0.7f
        playerFleet.getFleetData().addFleetMember(member)
    }
}