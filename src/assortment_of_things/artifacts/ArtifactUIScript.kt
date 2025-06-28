package assortment_of_things.artifacts

import assortment_of_things.artifacts.ui.ArtifactDisplayElement
import assortment_of_things.artifacts.ui.ArtifactSelectorElement
import assortment_of_things.artifacts.ui.ArtifactTooltip
import assortment_of_things.misc.*
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignState
import com.fs.starfarer.campaign.ui.UITable
import com.fs.state.AppDriver
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaUI.elements.LunaElement
import lunalib.lunaUtil.LunaCommons
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.input.Keyboard
import java.awt.Color

class ArtifactUIScript : EveryFrameScript {

    @Transient
    var panel: CustomPanelAPI? = null

    @Transient
    var fleetPanel: UIPanelAPI? = null

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }

    override fun advance(amount: Float) {

        if (!Global.getSector().isPaused) return
        if (Global.getSector().campaignUI.currentCoreTab != CoreUITabId.FLEET) return

        if (!RATSettings.enableAbyss!! && !RATSettings.relicsEnabled!!) return //Return if no modules that use artifacts are active

        var state = AppDriver.getInstance().currentState
        if (state !is CampaignState) return

        var core: UIPanelAPI? = null

        var docked = false

        var dialog = ReflectionUtils.invoke("getEncounterDialog", state)
        if (dialog != null)
        {
            docked = true
            core = ReflectionUtils.invoke("getCoreUI", dialog) as UIPanelAPI?
        }

        if (core == null) {
            core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI?
        }

        if (core == null) return



        fleetPanel = ReflectionUtils.invoke("getCurrentTab", core) as UIPanelAPI? ?: return
        var leftPanel = fleetPanel!!.getChildrenCopy().find { if (it is UIPanelAPI && it.getChildrenCopy().any { it is LabelAPI }) true else false } as UIPanelAPI ?: return

        var children = leftPanel.getChildrenCopy()
        if (panel != null && children.contains(panel!!)) {
            return
        }

        var table = children.find { it is UITable } as UIComponentAPI

        var w = table.getWidth()
        var h = 60f

        table.position.setYAlignOffset(-h-20f)

        panel = Global.getSettings().createCustom(300f, h, null)
        leftPanel.addComponent(panel)
        panel!!.position.aboveLeft(table, 10f)


        recreate(panel!!, w, h)


        var last = children.filter { it is LabelAPI }.lastOrNull()
        if (last is UIComponentAPI) {
            last.position.setYAlignOffset(-10f)
        }

    }

    fun recreateFleetList() {
        if (fleetPanel != null) {
            var fleetListPanel = ReflectionUtils.invoke("getFleetPanel", fleetPanel!!)
            ReflectionUtils.invoke("recreateUI", fleetListPanel!!, true)
        }
    }

    fun recreate(panel: CustomPanelAPI, w: Float, h: Float) {
        panel.clearChildren()

        var element = panel!!.createUIElement(300f, h, false)
        panel!!.addUIElement(element)

        var hasArtifact = ArtifactUtils.getArtifactsInFleet().isNotEmpty()
        var fade = 0f
        var fader = FaderUtil(0f, 1f, 1f, false, false)

        var container = element.addLunaElement(w, h).apply {
            enableTransparency = true
            borderAlpha = 0.7f
            backgroundAlpha = 0.6f
            backgroundColor = Color(0, 0, 0)

            onHoverEnter {
                playSound("ui_button_mouseover", 1f, 1f)
                borderAlpha = 1f
                backgroundColor = Color(10, 10, 20)
                //backgroundAlpha = 0.7f
            }
            onHoverExit {
                borderAlpha = 0.7f
                backgroundColor = Color(0, 0, 0)
               // backgroundAlpha = 0.5f
            }

            advance {
                if (hasArtifact && LunaCommons.getBoolean("assortment_of_things", "hasHoveredOverArtifactBefore") != true) {

                    fader.advance(it)
                    if (fader.brightness >= 1)
                    {
                        fader.fadeOut()
                    }
                    else if (fader.brightness <= 0)
                    {
                        fader.fadeIn()
                    }

                    backgroundColor = Color(10, 25, 35)

                    borderAlpha = 0.7f + (0.3f * fader.brightness)
                    backgroundAlpha = 0.6f + (0.4f * fader.brightness)

                    if (isHovering) {
                        borderAlpha = 1f
                        backgroundColor = Color(10, 10, 20)
                        backgroundAlpha = 0.6f
                        LunaCommons.set("assortment_of_things", "hasHoveredOverArtifactBefore", true)
                    }
                }
            }

        }
        container.elementPanel.position.setXAlignOffset(0f)

        var inner = container.innerElement

        var artifact = ArtifactUtils.getActiveArtifact()

        element.addTooltipToPrevious(ArtifactTooltip(artifact, true), TooltipMakerAPI.TooltipLocation.BELOW)

        if (artifact == null) {
            var inactivePara = inner.addPara("No Artifact Active", 0f, Misc.getGrayColor(), Misc.getTextColor())
            inactivePara.position.inTL(w/2-inactivePara.computeTextWidth(inactivePara.text)/2, h/2-inactivePara.computeTextHeight(inactivePara.text)/2)
        } else {
            var plugin = ArtifactUtils.getPlugin(artifact!!)
            var display = ArtifactDisplayElement(artifact, inner, 40f, 40f)

            display.position.inTL(10f, h/2 - display.height/2)

            var designType = artifact.designType
            var designColor = Misc.getDesignTypeColor(designType)

            var title =  inner.addTitle("$designType", Misc.getBasePlayerColor())
            var para = inner.addTitle("${artifact.name}", designColor)
            //para.position.inTL(w/2-para.computeTextWidth(para.text)/2+display.width/2, h/2-para.computeTextHeight(para.text)/2)

            title.position.inTL(display.width +20f, h/2-title.computeTextHeight(title.text)/2-para.computeTextHeight(para.text)/2)
            para.position.belowLeft(title as UIComponentAPI, 0f)
        }

        container.onClick {
            var stacks = ArtifactUtils.getArtifactsInCargo()
            if (stacks.isNotEmpty() || ArtifactUtils.getActiveArtifact() != null) {
                container.playClickSound()
                var popupH = getHeightRequired()+50+20
                createPopupPanel(550f, popupH) {
                    addArtifactSelector(it, 550f, popupH, panel, w, h)
                }
            } else {
                container.playSound("ui_char_can_not_increase_skill_or_aptitude", 1f, 1f)
            }
        }
    }

    fun getHeightRequired() : Float {
        var artifacts = getArtifacts()
        var count = MathUtils.clamp(((artifacts.count() - 1) / 6) + 1, 0, 4)
        return 90f * count
    }

    fun getArtifacts() : HashMap<ArtifactSpec, CargoStackAPI?> {
        var map = HashMap<ArtifactSpec, CargoStackAPI?>()

        var stacks = ArtifactUtils.getArtifactsInCargo()
        for (stack in stacks) {
            var data = stack.specialDataIfSpecial.data
            var artifact = ArtifactUtils.artifacts.find { it.id == data }
            map.put(artifact!!, stack)
        }

        var active = ArtifactUtils.getActiveArtifact()
        if (active != null) {
            map.put(active, null)
        }

        return map
    }

    fun addArtifactSelector(parentElement: TooltipMakerAPI, w: Float, h: Float, ogPanel: CustomPanelAPI, ogWidth: Float, ogHeight: Float) {

        parentElement.addSpacer(5f)
        var header = parentElement.addSectionHeading("Integrate Artifacts", Alignment.MID, 0f)
        header.position.setSize(header.position.width - 10f, header.position.height)
        header.position.setXAlignOffset(5f)


        var scrollerPanel = Global.getSettings().createCustom(w , h - 30f, null)
        parentElement.addCustom(scrollerPanel, 0f)

        var scrollerElement = scrollerPanel.createUIElement(w, h - 30f, true)
        //scrollerElement.addSpacer(10f)
        //scrollerElement.addPara("").position.inTL(5f, 5f)

        var priorArtifact = ArtifactUtils.getActiveArtifact()

        var heightSoFar = 10f

        var artifacts = getArtifacts()
        var elements = ArrayList<ArtifactSelectorElement>()
        var previous: CustomPanelAPI? = null
        var firstInRow: CustomPanelAPI? = null
        var index = 0
        for ((artifact, stack) in artifacts) {


            var selector = ArtifactSelectorElement(artifact, scrollerElement, 0f, 0f)
            selector.position.setSize(80f, 80f)
            elements.add(selector)

            if (priorArtifact == artifact) selector.selectedArtifact = true

            scrollerElement.addTooltipTo(ArtifactTooltip(artifact, false), selector.elementPanel, TooltipMakerAPI.TooltipLocation.BELOW)

            selector.onClick {
                selector.playClickSound()

                elements.forEach { it.selectedArtifact = false }
                selector.selectedArtifact = true


                //Uninstall Prior one
                priorArtifact = ArtifactUtils.getActiveArtifact()
                if (priorArtifact != null) {
                    var plugin = ArtifactUtils.getPlugin(priorArtifact!!)
                    //plugin.onRemove(Global.getSector().playerFleet, ArtifactUtils.STAT_MOD_ID)
                    Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_artifact", priorArtifact!!.id), 1f)
                    ArtifactUtils.deactivateArtifact()
                }

                //Installation
                if (priorArtifact?.id != artifact.id) {
                    if (stack != null) {
                        reduceOrRemoveStack(stack)
                    }
                    ArtifactUtils.setActiveArtifact(artifact)
                    var plugin = ArtifactUtils.getPlugin(artifact)
                    plugin.onInstall(Global.getSector().playerFleet, ArtifactUtils.STAT_MOD_ID)
                } else {
                    selector.selectedArtifact = false
                }

                recreate(ogPanel, ogWidth, ogHeight)
                recreateFleetList()
                closePopupPanel()
            }

            if (previous == null) {
                selector.position.inTL(10f, heightSoFar)
            } else if (index == 0) {
                selector.position.belowLeft(firstInRow, 10f)
            } else {
                selector.position.rightOfTop(previous, 10f)
            }

            previous = selector.elementPanel

            if (index == 0) {
                heightSoFar += 80f
                heightSoFar += 10f
                firstInRow = selector.elementPanel
            }

            index += 1

            if (index >= 6) index = 0
        }
        scrollerElement.addSpacer(heightSoFar)





        scrollerPanel.addUIElement(scrollerElement)



        //Bottom Section
        var bottomPanel = Global.getSettings().createCustom(w, 50f, null)
        parentElement.addCustom(bottomPanel, 0f)
        bottomPanel.position.belowLeft(scrollerPanel, -40f)

        var bottomElement = bottomPanel.createUIElement(w, 50f, false)
        bottomPanel.addUIElement(bottomElement)

        bottomElement.addSpacer(15f)

        var button = bottomElement.addButton("Close", "", Misc.getBasePlayerColor(), Misc.getDarkPlayerColor(), Alignment.MID, CutStyle.TOP, w-10, 30f, 0f)
        button.onClick {
            closePopupPanel()
        }
    }

    fun reduceOrRemoveStack(stack: CargoStackAPI) {
        stack.subtract(1f)
        if (stack.size < 0.1f) {
            stack.cargo.removeStack(stack)
        }
    }

    var popupPanelParent: CustomPanelAPI? = null
    var popupPanelBackground: LunaElement? = null
    var increase = true
    var isClosing = false
    fun createPopupPanel(width: Float, height: Float, lambda: (TooltipMakerAPI) -> Unit) {
        if (popupPanelParent != null) return //Dont let it open twice

        var state = AppDriver.getInstance().currentState
        var screenPanel = ReflectionUtils.get("screenPanel", state) as UIPanelAPI

        var sW = Global.getSettings().screenWidth
        var sH = Global.getSettings().screenHeight

        popupPanelParent = Global.getSettings().createCustom(sW, sH, null)
        screenPanel.addComponent(popupPanelParent)
        popupPanelParent!!.position.inTL(0f, 0f)

        var element = popupPanelParent!!.createUIElement(sW, sH, false)
        popupPanelParent!!.addUIElement(element)

        var opacity = 0f

        popupPanelBackground = element.addLunaElement(sW, sH).apply {
            enableTransparency = true
            renderBorder = false
            backgroundColor = Color(0, 0, 0)
            backgroundAlpha = 0.5f

            advance {
                if (increase) {
                    opacity += 5 * it
                    if (opacity >= 1f) {
                        opacity = MathUtils.clamp(opacity, 0f, 1f)
                        increase = false
                    }
                    popupPanelParent!!.setOpacity(opacity)
                }
            }

            onInput {
                for (event in it) {
                    if (event.isConsumed) continue
                    if (event.isKeyDownEvent && event.eventValue == Keyboard.KEY_ESCAPE) {
                        closePopupPanel()
                        event.consume()
                        continue
                    }
                    //Consuming Mouse up breaks shit for some reason
                    if (!event.isMouseUpEvent) {
                        event.consume()
                    }
                }
            }
        }

        popupPanelBackground!!.position.inTL(0f, 0f)

        var panel = Global.getSettings().createCustom(width, height, null)
        popupPanelParent!!.addComponent(panel)
        panel.position.inTL(sW/2-width/2, sH/2-height/2)

        var tooltip = panel.createUIElement(width, height, false)
        panel.addUIElement(tooltip)
        tooltip.position.inTL(0f, 0f)

        var b = tooltip.addLunaElement(0f, 0f).apply {
            enableTransparency = true
            borderAlpha = 0.7f
            backgroundAlpha = 1f
            backgroundColor = Color(0, 0, 0)
        }

        lambda(tooltip)

        b.position.setSize(width, height)

        popupPanelParent!!.setOpacity(0f)

    }

    fun closePopupPanel() {

        if (isClosing) return

        isClosing = true

        var state = AppDriver.getInstance().currentState
        var screenPanel = ReflectionUtils.get("screenPanel", state) as UIPanelAPI

        var opacity = 1f

        popupPanelBackground!!.advance {
            if (increase) return@advance
            opacity -= 5 * it
            popupPanelParent!!.setOpacity(opacity)

            if (opacity <= 0f) {
                screenPanel.removeComponent(popupPanelParent)
                popupPanelParent = null
                popupPanelBackground = null
                increase = true
                isClosing = false
            }
        }
    }

}