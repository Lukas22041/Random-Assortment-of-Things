package assortment_of_things.misc.escort

import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.addPara
import assortment_of_things.misc.addTooltip
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.GameState
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaRefit.BaseRefitButton

class EscortRefitButton : BaseRefitButton() {

    var width = 300f
    var height = 500f

    override fun getButtonName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "Escort Planner"
    }

    override fun shouldShow(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return RATSettings.escortEnabled!! && Global.getCurrentState() == GameState.CAMPAIGN
    }

    override fun getIconName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "graphics/icons/codex/abilities.png"
    }

    override fun getPanelWidth(member: FleetMemberAPI?, variant: ShipVariantAPI?): Float {
        return width
    }

    override fun getPanelHeight(member: FleetMemberAPI?, variant: ShipVariantAPI?): Float {
        return height
    }

    override fun hasPanel(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return true
    }

    override fun initPanel(backgroundPanel: CustomPanelAPI?, member: FleetMemberAPI?,variant: ShipVariantAPI?, market: MarketAPI?) {

        EscortOrdersManager.fillNewEntries()

        var panel = backgroundPanel!!.createCustomPanel(width, height, null)
        backgroundPanel.addComponent(panel)

        var element = panel.createUIElement(width, height, true)

        var data = EscortOrdersManager.getEscortData()
        var entry = data.get(member!!.id)


        var checkbox = EscortCheckbox(entry!!.isEscortMode, element, 24f, 24f)
        checkbox.position.inTL(20f, 20f)

        var checkboxPara = element.addPara("Enable automatic escort assignment", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor()) as UIComponentAPI
        checkboxPara.position.rightOfMid(checkbox.elementPanel, 8f)

        element.addTooltip(checkbox.elementPanel, TooltipMakerAPI.TooltipLocation.BELOW, 350f) { tooltip ->
            tooltip.addPara("Enabling this causes this specific ship to escort the ships listed below. You can assign a priority value on the right, a higher number is a higher priority and will always be escorted first if the ship is deployed and alive.")
        }


        checkbox.advance {
            entry.isEscortMode = checkbox.value
        }

        element.addLunaElement(0f, 0f).position.inTL(7.5f, 50f)

        element.addSpacer(10f)
        element.addSectionHeading("Escort Targets", Alignment.MID, 0f)
        element.addSpacer(10f)

        var first = true
        for (other in HashMap(entry.escorts)) {
            if (other.key == member.id) continue

            var member = Global.getSector().playerFleet.fleetData.membersListCopy.find { it.id == other.key } ?: continue

            var container = element.addLunaElement(width-10f, 72f).apply {
                enableTransparency = true
                borderAlpha = 0.3f
                backgroundAlpha = 0.4f

                var priority = other.value


                innerElement.addSpacer(6f)
                innerElement.addShipList(1, 1, 60f, Misc.getBasePlayerColor(), mutableListOf(member), 0f)

                innerElement.addTitle("${member!!.baseOrModSpec().hullName}-Class").position.inTL(70f, 20f)
                innerElement.addPara("${member.shipName}", 0f)

                innerElement.setParaFont(Fonts.ORBITRON_24AABOLD)
                var priorityPara = innerElement.addPara("$priority", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                priorityPara.position.inTL(260f - priorityPara.computeTextWidth(priorityPara.text) / 2, 22f)

                onHoverEnter {
                    playScrollSound()
                    borderAlpha = 0.5f
                    backgroundAlpha = 0.7f
                }
                onHoverExit {
                    borderAlpha = 0.3f
                    backgroundAlpha = 0.4f
                }

                onClick {
                    playClickSound()
                    if (it.isLMBDownEvent && priority < 10) {
                        playScrollSound()
                        priority += 1
                    }
                    if (it.isRMBDownEvent && priority > 1){
                        playScrollSound()
                        priority -= 1
                    }
                    priorityPara.text = "$priority"
                    priorityPara.position.inTL(260f - priorityPara.computeTextWidth(priorityPara.text) / 2, 22f)
                    entry.escorts.put(other.key, priority)
                }

                /*onInput {
                    if (isHovering) {
                        for (event in it) {
                            if (event.isMouseScrollEvent) {
                                if (event.eventValue > 0 && priority < 10) {
                                    playScrollSound()
                                    priority += 1
                                }
                                if (event.eventValue < 0 && priority > 1){
                                    playScrollSound()
                                    priority -= 1
                                }
                                priorityPara.text = "$priority"
                                priorityPara.position.inTL(250f - priorityPara.computeTextWidth(priorityPara.text) / 2, 22f)
                                entry.escorts.put(other.key, priority)
                            }
                        }
                    }

                }*/

                /*advance {
                    priorityPara.text = "$priority"
                }*/

            }

            element.addTooltip(container.elementPanel, TooltipMakerAPI.TooltipLocation.BELOW, 450f) { tooltip ->
                tooltip.addPara("Left-click to increase the priority. Right-click to decrease the priority. A ship with a higher priority will always be picked over one with lower priority.", 0f
                , Misc.getTextColor(), Misc.getHighlightColor(), "Left-click", "Right-click")
            }

            if (first) {
                first = false
                container.position.inTL(5f, 90f)
            }

            element.addSpacer(10f)
        }




        panel.addUIElement(element)
    }


   /* override fun hasTooltip(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return true
    }

    override fun addTooltip(tooltip: TooltipMakerAPI?,
                            member: FleetMemberAPI?,
                            variant: ShipVariantAPI?,
                            market: MarketAPI?) {
    }*/

    override fun onPanelClose(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?) {

    }

    override fun getOrder(member: FleetMemberAPI?, variant: ShipVariantAPI?): Int {
        return 1110
    }
}