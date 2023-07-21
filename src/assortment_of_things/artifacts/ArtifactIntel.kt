package assortment_of_things.artifacts

import assortment_of_things.misc.BorderedPanelPlugin
import assortment_of_things.misc.addPara
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaExtensions.addLunaSpriteElement
import lunalib.lunaUI.elements.LunaElement
import lunalib.lunaUI.elements.LunaSpriteElement

class ArtifactIntel : BaseIntelPlugin() {

    @Transient
    var panel: CustomPanelAPI? = null

    @Transient
    var element: TooltipMakerAPI? = null

    @Transient
    var artifactsListPanel: CustomPanelAPI? = null

    override fun getName(): String {
        return "Artifacts"
    }

    override fun isPlayerVisible(): Boolean {
        return true
    }

    override fun hasLargeDescription(): Boolean {
        return true
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return mutableSetOf("Personal")
    }

    override fun getIcon(): String {
        var path = "graphics/icons/intel/rat_artifacts.png"
        Global.getSettings().loadTexture(path)
        return path
    }

    override fun createLargeDescription(panel: CustomPanelAPI?, width: Float, height: Float) {
        this.panel = panel
        recreatePanel()
    }

    init {
        important = true
    }

    fun recreatePanel()
    {
        if (panel == null) return
        if (element != null)
        {
            if (element!!.externalScroller != null)
            {
                element!!.removeComponent(element!!.externalScroller)
                panel!!.removeComponent(element!!.externalScroller)
            }
            panel!!.removeComponent(element)
        }
        if (artifactsListPanel != null)
        {
            panel!!.removeComponent(artifactsListPanel)
        }

        var spacing = 0f

        element = panel!!.createUIElement(panel!!.position.width / 2, panel!!.position.height, true)

        element!!.setTitleFont(Fonts.ORBITRON_24AABOLD)
        element!!.addTitle("Artifacts")
        element!!.addSpacer(5f)
        var description = element!!.addPara("Artifacts are long lost devices that can be recovered by the fleet. " +
                "An active artifact may give a variety of benefits to a fleet, however only one artifact can be active at a time. ")

        description.setHighlight("storypoint")
        description.setHighlightColors(Misc.getStoryBrightColor())

        panel!!.addUIElement(element)
        element!!.position.inTL(0f, 10f)



        element!!.addSpacer(30f)
        var artifactDisplay = Global.getSettings().createCustom(panel!!.position.width * 0.45f, 300f, BorderedPanelPlugin())
        element!!.addCustom(artifactDisplay, 0f)
        var displayElement = artifactDisplay.createUIElement(panel!!.position.width * 0.45f, 300f, false)
        artifactDisplay!!.addUIElement(displayElement)
        displayElement.addSectionHeading("Active Artifact", Alignment.MID, 0f)

        var activeArtifact = ArtifactUtils.getActiveArtifact()
        if (activeArtifact == null)
        {
            displayElement.addSpacer(10f)
            var path = "graphics/icons/mission_marker.png"
            Global.getSettings().loadTexture(path)
            var img = displayElement.beginImageWithText(path, 64f)

            img.addPara("There is currently no Artifact active")

            displayElement.addImageWithText(0f)
        }
        else
        {
            var plugin = ArtifactUtils.getActivePlugin()
            displayElement.addSpacer(10f)
            var path = activeArtifact.spritePath
            Global.getSettings().loadTexture(path)
            var img = displayElement.beginImageWithText(path, 64f)

            img.addTitle(activeArtifact.name)
            plugin!!.addDescription(img)
            //img.addPara("Test Description")

            displayElement.addImageWithText(0f)

            element!!.addSpacer(3f)
            element!!.addLunaElement(panel!!.position.width * 0.45f, 30f).apply {
                enableTransparency = true
                borderAlpha = 0.7f

                addText("Remove active Artifact", baseColor = Misc.getBasePlayerColor())
                centerText()

                onHoverEnter {
                    playScrollSound()
                    borderAlpha = 1f
                }

                onHoverExit { borderAlpha = 0.7f }

                onClick {
                    ArtifactUtils.deactivateArtifact()
                    playClickSound()
                    recreatePanel()
                }
            }
        }




        artifactsListPanel = Global.getSettings().createCustom(panel!!.position.width * 0.5f, panel!!.position.height * 0.75f, BorderedPanelPlugin())
        panel!!.addComponent(artifactsListPanel)
        artifactsListPanel!!.position.inTL(element!!.position.width, (panel!!.position.height / 2) - artifactsListPanel!!.position.height / 2)
        var artifactsListElement = artifactsListPanel!!.createUIElement(artifactsListPanel!!.position.width , artifactsListPanel!!.position.height, true)

        var artifacts = ArtifactUtils.getArtifactsInFleet()
        if (Global.getSettings().isDevMode) {
            artifacts = ArtifactUtils.artifacts
            var titleHeader = artifactsListElement.addSectionHeading("Artifacts in Fleet (Devmode)", Alignment.MID, 0f)
            titleHeader.position.setSize(artifactsListPanel!!.position.width , 20f)
        }
        else
        {
            var titleHeader = artifactsListElement.addSectionHeading("Artifacts in Fleet", Alignment.MID, 0f)
            titleHeader.position.setSize(artifactsListPanel!!.position.width , 20f)
        }

        var count = 1
        var currentRow = 0
        var lastSprite: LunaElement? = null
        spacing += 30

        for (artifact in artifacts)
        {
            if (ArtifactUtils.getActiveArtifact() != null && ArtifactUtils.getActiveArtifact()!!.id == artifact.id) continue

            var sprite = artifactsListElement!!.addLunaSpriteElement(artifact.spritePath, LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, 96f, 0f).apply {
                renderBorder = false
                enableTransparency = true

                borderAlpha = 0.8f
                getSprite().alphaMult = 0.8f

                onHoverEnter {
                    getSprite().alphaMult = 1f
                    playScrollSound()
                    borderAlpha = 1f

                }
                onHoverExit {
                    getSprite().alphaMult = 0.8f
                    borderAlpha = 0.8f
                }

                onClick {

                    playClickSound()

                    if (ArtifactUtils.getActiveArtifact() != null)
                    {
                        ArtifactUtils.getActivePlugin()!!.onRemove(Global.getSector().playerFleet, ArtifactUtils.STAT_MOD_ID)
                    }

                    ArtifactUtils.setActiveArtifact(artifact)

                    var plugin =  ArtifactUtils.getActivePlugin()!!
                    plugin.onInstall(Global.getSector().playerFleet, ArtifactUtils.STAT_MOD_ID)
                    for (member in Global.getSector().playerFleet.fleetData.membersListCopy)
                    {
                        if (!member.variant.hasHullMod("rat_artifact_controller"))
                        {
                            if (member.variant.source != VariantSource.REFIT)
                            {
                                var variant = member.variant.clone();
                                variant.originalVariant = null;
                                variant.hullVariantId = Misc.genUID()
                                variant.source = VariantSource.REFIT
                                member.setVariant(variant, false, true)
                            }

                            member.variant.addMod("rat_artifact_controller")
                        }
                    }

                    recreatePanel()
                }
            }

            artifactsListElement!!.addTooltipToPrevious( object : TooltipMakerAPI.TooltipCreator {
                override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
                    return false
                }

                override fun getTooltipWidth(tooltipParam: Any?): Float {
                    return 350f
                }

                override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {

                    tooltip!!.addTitle(artifact.name)
                    ArtifactUtils.getPlugin(artifact).addDescription(tooltip!!)
                   /* tooltip!!.addImage(portrait, 128f, 0f)
                    tooltip.addSpacer(3f)
                    tooltip.addPara("Sprite: $portrait", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Sprite: ")*/

                }

            }, TooltipMakerAPI.TooltipLocation.BELOW)


            sprite.position.setSize(96f, 96f)
            sprite.getSprite().setSize(96f, 96f)

            if (currentRow == 0)
            {
                sprite.position.inTL(10f, spacing)
                spacing += 96f + 20f
            }
            else
            {
                sprite.position.rightOfMid(lastSprite!!.elementPanel, 30f)
            }

            lastSprite = sprite
            currentRow++
            count++

            if (currentRow > 3)
            {
                currentRow = 0
            }

        }

        artifactsListPanel!!.addUIElement(artifactsListElement)

    }
}