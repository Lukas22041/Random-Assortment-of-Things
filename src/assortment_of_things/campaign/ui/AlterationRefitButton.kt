package assortment_of_things.campaign.ui

import assortment_of_things.abyss.hullmods.BaseAlteration
import assortment_of_things.misc.BorderedPanelPlugin
import assortment_of_things.misc.addPara
import assortment_of_things.strings.RATItems
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Stats
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaExtensions.addLunaSpriteElement
import lunalib.lunaRefit.BaseRefitButton
import lunalib.lunaUI.elements.LunaSpriteElement
import org.magiclib.kotlin.getStorage

class AlterationRefitButton : BaseRefitButton() {

    var backgroundPanel: CustomPanelAPI? = null
    var panel: CustomPanelAPI? = null

    override fun getButtonName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "Alteration"
    }

    override fun getIconName(member: FleetMemberAPI?, variant: ShipVariantAPI?): String {
        return "graphics/icons/missions/blueprint_location.png"
    }

    override fun getOrder(member: FleetMemberAPI?, variant: ShipVariantAPI?): Int {
        return 80
    }

    override fun getPanelWidth(member: FleetMemberAPI?, variant: ShipVariantAPI?): Float {
        return 900f
    }

    override fun getPanelHeight(member: FleetMemberAPI?, variant: ShipVariantAPI?): Float {
        return 500f
    }

    override fun hasPanel(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
        return true
    }

    override fun onPanelClose(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?) {
        backgroundPanel = null
        panel = null
    }

    override fun initPanel(backgroundPanel: CustomPanelAPI?, member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?) {
        this.backgroundPanel = backgroundPanel
        recreatePanel(member, variant, market)
    }

    fun recreatePanel(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?) {
        if (backgroundPanel == null) return
        if (panel != null) {
            backgroundPanel!!.removeComponent(panel)
        }

        var installedAlteration = variant!!.hullMods.map { Global.getSettings().getHullModSpec(it) }.find { it.hasTag("rat_alteration") }
        var hasAlteration = installedAlteration != null

        var width = getPanelWidth(member, variant)
        var height = getPanelHeight(member, variant)

        panel = backgroundPanel!!.createCustomPanel(width, height, null)
        backgroundPanel!!.addComponent(panel)
        panel!!.position.inTL(0f, 0f)

        var headerElement = panel!!.createUIElement(width, height, false)
        panel!!.addUIElement(headerElement)
        headerElement!!.position.inTL(0f, 0f)
        headerElement.addSectionHeading("Hull Alterations", Alignment.MID, 0f)
        headerElement.position.inTL(0f, 0f)



        var aboutPanel = panel!!.createCustomPanel(400f, 150f, BorderedPanelPlugin().apply { renderBackground = true })
        panel!!.addComponent(aboutPanel)
        aboutPanel.position.inTL(20f, 45f)

        var aboutElement = aboutPanel.createUIElement(400f, 150f, false)
        aboutPanel.addUIElement(aboutElement)
        aboutElement.position
        aboutElement.addSectionHeading("About", Alignment.MID, 0f)
        aboutElement.addSpacer(10f)
        aboutElement.addPara("Hull-Alterations are a unique type of consumeable hullmod. Only one can be installed per ship at a time. \n\n" + "They provide powerful and unique effects that can hardly be found anywhere else. \n\n" + /*"Alterations count towards the ships s-mod limit, and they cant be installed if the ship already reached the limit.\n\n" +*/ "This menu can pull alterations from the fleet inventory or from the storage of the docked-at colony.",
            0f,
            Misc.getTextColor(),
            Misc.getHighlightColor(),
            "Hull-Alterations",
            "consumeable hullmod",
            "powerful",
            "unique",
            "fleet inventory",
            "storage").position.inTL(10f, 30f)



        var alterationDisplayPanel = panel!!.createCustomPanel(400f, 200f, BorderedPanelPlugin().apply { renderBackground = true })
        panel!!.addComponent(alterationDisplayPanel)
        alterationDisplayPanel.position.belowLeft(aboutPanel, 20f)

        var displayElement = alterationDisplayPanel.createUIElement(400f, 200f, true)
        var head = displayElement.addSectionHeading("Installed Alteration", Alignment.MID, 0f)
        head.position.setSize(head.position.width + 10, head.position.height)


        if (installedAlteration == null) {
            displayElement.addSpacer(50f)
            var path = "graphics/icons/mission_marker.png"
            Global.getSettings().loadTexture(path)
            var img = displayElement.beginImageWithText(path, 64f)

            img.addPara("This ship does not have an installed alteration.")

            displayElement.addImageWithText(0f)
        }
        else {
            var plugin = Global.getSettings().scriptClassLoader.loadClass(installedAlteration.effectClass).newInstance() as BaseAlteration
            displayElement.addSpacer(10f)
            var path = installedAlteration.spriteName
            Global.getSettings().loadTexture(path)
            var img = displayElement.beginImageWithText(path, 40f)

            img.addTitle(installedAlteration.displayName)
            plugin!!.addPostDescriptionSection(img, ShipAPI.HullSize.FRIGATE, null, 300f, false)
            //img.addPara("Test Description")

            displayElement.addImageWithText(0f)
            displayElement.addSpacer(10f)
        }

        alterationDisplayPanel.addUIElement(displayElement)



        var canBeRemoved = true
        if (installedAlteration != null) {
            var plugin = Global.getSettings().scriptClassLoader.loadClass(installedAlteration.effectClass).newInstance() as BaseAlteration
            canBeRemoved = plugin.canUninstallAlteration(member, variant, market)
        }

        var removeButtonPanel = panel!!.createCustomPanel(400f, 30f, null)
        panel!!.addComponent(removeButtonPanel)
        removeButtonPanel.position.belowLeft(alterationDisplayPanel, 10f)

        var removeButtonElement = removeButtonPanel.createUIElement(400f, 30f, false)
        removeButtonPanel.addUIElement(removeButtonElement)
        removeButtonElement.position.inTL(-5f ,0f)
        removeButtonElement.addLunaElement(400f, 30f).apply {
            enableTransparency = true
            addText("Remove Alteration", baseColor = Misc.getBasePlayerColor())
            centerText()

            if (hasAlteration && canBeRemoved) {
                backgroundAlpha = 0.8f
                borderAlpha = 1f

                onHoverEnter {
                    playScrollSound()
                    backgroundAlpha = 1f
                }

                onHoverExit {
                    backgroundAlpha = 0.8f
                }

                onClick {
                    playClickSound()

                    variant.removeMod(installedAlteration!!.id)
                    var plugin = Global.getSettings().scriptClassLoader.loadClass(installedAlteration.effectClass).newInstance() as BaseAlteration
                    plugin.onAlterationRemove(member, variant, market)

                    Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData(RATItems.ALTERATION_INSTALLER, installedAlteration.id), 1f)

                    refreshVariant()
                    refreshButtonList()
                    recreatePanel(member, variant, market)
                }
            }
            else {
                backgroundAlpha = 0.3f
                borderAlpha = 0.6f

                onClick {
                    playSound("ui_button_disabled_pressed", 1f, 1f)
                }
            }
        }

        removeButtonElement!!.addTooltipToPrevious( object : TooltipMakerAPI.TooltipCreator {
            override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
                return false
            }

            override fun getTooltipWidth(tooltipParam: Any?): Float {
                return 400f
            }

            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                tooltip!!.addPara("Removes the alteration from the ship and return its installer back in to the inventory.", 0f)
                tooltip.addSpacer(5f)

                if (!hasAlteration) {
                    tooltip!!.addPara("This ship does not have any alteration installed.", 0f, Misc.getNegativeHighlightColor(), Misc.getHighlightColor())
                }
                else if (!canBeRemoved) {
                    var plugin = Global.getSettings().scriptClassLoader.loadClass(installedAlteration!!.effectClass).newInstance() as BaseAlteration
                    plugin.cannotUninstallAlterationTooltip(tooltip, member, variant, 400f)
                }
            }

        }, TooltipMakerAPI.TooltipLocation.RIGHT)





        var alterationsPanel = panel!!.createCustomPanel(440f, 420f, BorderedPanelPlugin())
        panel!!.addComponent(alterationsPanel)
        alterationsPanel.position.rightOfTop(aboutPanel, 20f)

        var alterationsElement = alterationsPanel.createUIElement(440f, 420f, true)
        alterationsElement.addSectionHeading("Alterations in Storage", Alignment.MID, 0f).apply {
            position.setSize(position.width + 10f, position.height)
        }
        alterationsElement.addSpacer(5f)

        var lastPanel: CustomPanelAPI? = null

        var alterations = Global.getSettings().allHullModSpecs.filter { it.hasTag("rat_alteration") }

        alterations = alterations.sortedBy { it.displayName }

        var stacks = Global.getSector().playerFleet.cargo.stacksCopy.filter { it.isSpecialStack && it.specialItemSpecIfSpecial.id == "rat_alteration_install" }.toMutableList()
        if (market != null) {
            if (market.getStorage() != null) {
                stacks += market.getStorage().cargo.stacksCopy.filter { it.isSpecialStack && it.specialItemSpecIfSpecial.id == "rat_alteration_install" }
            }
        }

       /* for (stack in stacks.sortedWith(compareBy({!
            (Global.getSettings().scriptClassLoader.loadClass(Global.getSettings().getHullModSpec(it.specialDataIfSpecial.data).effectClass).newInstance() as BaseAlteration).
            canInstallAlteration(member, variant, market) }, {it.specialDataIfSpecial.data}) ) ) {*/

        for (alteration in alterations) {

            var playerStacks = Global.getSector().playerFleet.cargo.stacksCopy.filter { it.specialDataIfSpecial != null && it.specialDataIfSpecial.data == alteration.id  }
            var marketStacks = market?.getStorage()?.cargo?.stacksCopy?.filter { it.specialDataIfSpecial != null && it.specialDataIfSpecial.data == alteration.id  } ?: ArrayList()

            if (playerStacks.isEmpty() && marketStacks.isEmpty()) continue

            var stacksCount = playerStacks.sumOf { it.size.toInt() } + marketStacks.sumOf { it.size.toInt() }

            var stack: CargoStackAPI? = null
            if (marketStacks.isNotEmpty()) stack = marketStacks.random()
            if (playerStacks.isNotEmpty()) stack = playerStacks.random()
            if (stack == null) continue

            var alteration = Global.getSettings().getHullModSpec(stack.specialDataIfSpecial.data)
            var plugin = Global.getSettings().scriptClassLoader.loadClass(alteration.effectClass).newInstance() as BaseAlteration

            var isInstallable = plugin.canInstallAlteration(member, variant, market)
            if (variant.hasHullMod(alteration.id)) isInstallable = false

            var lunaElement = alterationsElement.addLunaElement(430f, 40f).apply {
                renderBorder = false
                renderBackground = true
                enableTransparency = true
                backgroundAlpha = 0.3f

                var sprite = innerElement!!.addLunaSpriteElement(alteration.spriteName,
                    LunaSpriteElement.ScalingTypes.STRETCH_SPRITE,
                    40f,
                    40f).apply {
                    renderBorder = false
                    enableTransparency = true

                    borderAlpha = 0.8f
                    getSprite().alphaMult = 0.8f
                }
                sprite.position.inTL(0f, 0f)


                var nameColor = Misc.getTextColor()
                if (!isInstallable || hasAlteration) nameColor = Misc.getNegativeHighlightColor()

                var namePara = innerElement.addPara(alteration.displayName, 0f, nameColor, nameColor)
                namePara.position.rightOfMid(sprite.elementPanel, 10f)

                var storageText = "(In Storage: $stacksCount)"
                var storagePara = innerElement.addPara(storageText, 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                storagePara.position.rightOfMid(sprite.elementPanel, 380f - storagePara.computeTextWidth(storageText))

                onHoverEnter {
                    playScrollSound()
                    sprite.getSprite().alphaMult = 1f
                    backgroundAlpha = 0.7f
                }

                onHoverExit {
                    sprite.getSprite().alphaMult = 0.8f
                    backgroundAlpha = 0.3f
                }

                onClick {
                    if (!it.isLMBEvent) return@onClick
                    playClickSound()

                    if (it.isDoubleClick ) {
                        if (isInstallable && !hasAlteration) {
                            variant.addMod(alteration.id)

                            stack.subtract(1f)
                            if (stack.size < 0.1f) {
                               stack.cargo.removeStack(stack)
                            }

                            Global.getSoundPlayer().playUISound("ui_acquired_blueprint", 1f, 1f)
                            refreshVariant()
                            refreshButtonList()
                            recreatePanel(member, variant, market)
                        }
                    }
                }

            }

            alterationsElement!!.addTooltipToPrevious( object : TooltipMakerAPI.TooltipCreator {
                override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
                    return false
                }

                override fun getTooltipWidth(tooltipParam: Any?): Float {
                    return 400f
                }

                override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                    plugin.addPostDescriptionSection(tooltip, variant.hullSize, null, 400f, false)
                    tooltip!!.addSpacer(10f)

                    if (isInstallable && !hasAlteration) {
                        tooltip!!.addPara("Double-Click to install", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
                    }
                    else if (hasAlteration) {
                        tooltip!!.addPara("This ship already has an alteration installed.", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())
                    }
                    else {
                        plugin.cannotInstallAlterationTooltip(tooltip,  member, variant,400f)
                    }
                }

            }, TooltipMakerAPI.TooltipLocation.BELOW)

            if (lastPanel != null) {
                lunaElement.elementPanel.position.belowLeft(lastPanel, 5f)
                alterationsElement.addSpacer(5f)
            }
            lastPanel = lunaElement.elementPanel
        }

        alterationsElement.addSpacer(5f)
        alterationsPanel.addUIElement(alterationsElement)
    }

    override fun shouldShow(member: FleetMemberAPI?, variant: ShipVariantAPI?, market: MarketAPI?): Boolean {
       /* if (variant!!.hullMods.any { Global.getSettings().getHullModSpec(it).hasTag("rat_alteration") }) return true
        if (Global.getSector().playerFleet.cargo.stacksCopy
                .any { it.isSpecialStack && (it.specialItemSpecIfSpecial.id == "rat_alteration_install" || it.specialItemSpecIfSpecial.id == "rat_alteration_remover" )}) return true

        if (market != null && market?.getStorage()?.cargo?.stacksCopy != null && market.getStorage().cargo.stacksCopy
                .any { it.isSpecialStack && (it.specialItemSpecIfSpecial.id == "rat_alteration_install" || it.specialItemSpecIfSpecial.id == "rat_alteration_remover" )}) return true


        return false*/

        return true
    }
}