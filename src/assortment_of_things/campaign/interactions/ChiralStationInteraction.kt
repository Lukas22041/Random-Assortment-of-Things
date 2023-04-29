package assortment_of_things.campaign.interactions

import assortment_of_things.combat.hullmods.LifelineHullmod
import assortment_of_things.combat.hullmods.PhaseDiveHullmod
import assortment_of_things.combat.hullmods.SpringHullmod
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.campaign.StoryPointActionDelegate
import com.fs.starfarer.api.campaign.TextPanelAPI
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDelegates.LunaMemory
import lunalib.lunaExtensions.addLunaElement
import org.lwjgl.input.Keyboard

class ChiralStationInteraction : RATInteractionPlugin(), FleetMemberPickerListener {

    var pickedSpec: HullModSpecAPI? = null

    var backgroundColor = Misc.getDarkPlayerColor().darker()

    override fun init() {
        var panel = visualPanel.showCustomPanel(400f, 500f, null)
        var element = panel.createUIElement(420f, 500f, true)

        element.addSpacer(30f)

        var hullmods = listOf<BaseHullMod>(LifelineHullmod(), PhaseDiveHullmod(), SpringHullmod())

        for (hullmod in hullmods)
        {

            var spec = when(hullmod)
            {
                is LifelineHullmod -> Global.getSettings().getHullModSpec("rat_lifeline")
                is PhaseDiveHullmod -> Global.getSettings().getHullModSpec("rat_phase_hop")
                is SpringHullmod -> Global.getSettings().getHullModSpec("rat_spring")
                else -> continue
            }

            var lunaElement = element.addLunaElement(400f, 400f)
            lunaElement.enableTransparency = true

            var inner = lunaElement.innerElement
            inner.addSpacer(10f)

            var imgWithText = inner.beginImageWithText(spec.spriteName, 32f)
            imgWithText.addPara("${spec.displayName}", 0f)
            inner.addImageWithText(0f)

            inner.addSpacer(5f)

            hullmod.addPostDescriptionSection(lunaElement.innerElement, null, null, 400f, false)

            element.addSpacer(30f)

            lunaElement.onHoverEnter {
                lunaElement.playScrollSound()
                lunaElement.borderColor = Misc.getDarkPlayerColor().brighter()
            }
            lunaElement.onHoverExit {
                lunaElement.borderColor = Misc.getDarkPlayerColor()
            }


            lunaElement.onClick {
                var charges: Int? by LunaMemory("rat_hullmod_charges_left", 3, interactionTarget.memoryWithoutUpdate)
                if (charges!! > 0)
                {
                    lunaElement.playClickSound()
                    pickedSpec = spec
                    installScreen()
                }
            }

            lunaElement.advance {

                var charges: Int? by LunaMemory("rat_hullmod_charges_left", 3, interactionTarget.memoryWithoutUpdate)

                if (charges!! < 1)
                {
                    lunaElement.backgroundColor = Misc.getDarkPlayerColor().darker().darker()
                }
                else
                {
                    lunaElement.backgroundColor = Misc.getDarkPlayerColor().darker()
                }
            }
        }

        panel.addUIElement(element)




        addText()
    }



    fun addText()
    {
        var charges: Int? by LunaMemory("rat_hullmod_charges_left", 3, interactionTarget.memoryWithoutUpdate)
        textPanel.addPara("The fleet arrives at a strange station, after arrival, the crew discovers some kind of nanoforge. " +
                "It seems to be able to modify ships to permanently add a secondary shipsystem to them. " +
                "However, the station appears to be running on batteries alone, limiting the amount of operations.", Misc.getTextColor(), Misc.getHighlightColor(),
        "add a secondary shipsystem", "limiting")


        if (charges!! < 1)
        {
            var label = textPanel.addPara("There are no charges left.", Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
        }
        else
        {
            var label = textPanel.addPara("There are $charges charges left, select a system on the right to permanently install it on to a ship. A ship can only have one secondary system installed. Capital ships are to large to have this modification be done to them.", Misc.getTextColor(), Misc.getHighlightColor())
            label.setHighlight("$charges charges left", "permanently")
            label.setHighlightColors(Misc.getNegativeHighlightColor(), Misc.getHighlightColor())

            textPanel.addPara("Integrating a shipsystem requires a storypoint.")
        }

        createOption("Leave") {
            visualPanel.fadeVisualOut()
            closeDialog()
        }
        optionPanel.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, true);
    }

    fun installScreen()
    {
        var mods = listOf("rat_lifeline", "rat_spring", "rat_phase_hop")
        var fleet = Global.getSector().playerFleet.fleetData.membersInPriorityOrder.filter { member ->
            member.variant.hullMods.none { mods.contains(it) } && member.variant.hullSize != ShipAPI.HullSize.CAPITAL_SHIP
        }

        dialog.showFleetMemberPickerDialog("Choose a ship to install the system in to", "Install", "Cancel", 5, 10, 64f, true, false,
            fleet, this)

    }

    override fun pickedFleetMembers(members: MutableList<FleetMemberAPI>?) {
        if (members.isNullOrEmpty()) return
        if (pickedSpec == null) return


        var member = members.first()

        textPanel.clear()
        addText()
        textPanel.addPara("\nAre you sure you want to install ${pickedSpec!!.displayName} in to the ${member.shipName}?", Misc.getTextColor(), Misc.getHighlightColor(),
        "${pickedSpec!!.displayName}", "${members.first().shipName}")

        clearOptions()
        createOption("Install ${pickedSpec!!.displayName}") {
            var charges: Int? by LunaMemory("rat_hullmod_charges_left", 3, interactionTarget.memoryWithoutUpdate)
            if (charges == null) return@createOption

            charges = charges!! - 1

            member.variant.addMod(pickedSpec!!.id)
            member.variant.addPermaMod(pickedSpec!!.id)

            clear()
            addText()

            var text = textPanel.addPara("\n\nSucessfully installed ${pickedSpec!!.displayName} in to the ${member.shipName}!", Misc.getTextColor(), Misc.getHighlightColor())
            text.setHighlight("${pickedSpec!!.displayName}", "${member.shipName}")
            text.setHighlightColors(Misc.getHighlightColor(), Misc.getHighlightColor())

            pickedSpec = null
        }
        dialog.makeStoryOption("Install ${pickedSpec!!.displayName}", 1, 1f, "ui_char_spent_story_point")
        optionPanel.addOptionConfirmation("Install ${pickedSpec!!.displayName}", object: StoryPointActionDelegate {
            override fun getTitle(): String {
                return "Install ${pickedSpec!!.displayName}"
            }

            override fun withDescription(): Boolean {
                return true
            }

            override fun withSPInfo(): Boolean {
                return true
            }

            override fun createDescription(info: TooltipMakerAPI?) {
                info!!.addPara("Are you sure to spend a storypoint on integrating ${pickedSpec!!.displayName} in to the ${member.shipName} (${member.hullSpec.hullName}-Class)?", 0f)
            }

            override fun getBonusXPFraction(): Float {
                return 1f
            }

            override fun getTextPanel(): TextPanelAPI? {
                return dialog.textPanel
            }

            override fun preConfirm() {

            }

            override fun confirm() {

            }

            override fun getConfirmSoundId(): String {
                return "ui_char_spent_story_point"
            }

            override fun getRequiredStoryPoints(): Int {
                return 1
            }

            override fun getLogText(): String {
                return ""
            }

        })

        createOption("Leave") {
            visualPanel.fadeVisualOut()
            closeDialog()
        }
        optionPanel.setShortcut("Leave", Keyboard.KEY_ESCAPE, false, false, false, true);

    }

    override fun cancelledFleetMemberPicking() {

    }
}