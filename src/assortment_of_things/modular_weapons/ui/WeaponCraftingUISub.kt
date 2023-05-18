package assortment_of_things.modular_weapons.ui

import assortment_of_things.modular_weapons.data.AvailableDamageTypes
import assortment_of_things.modular_weapons.data.ModularRepo
import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.effects.*
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.combat.BattleCreationContext
import com.fs.starfarer.api.combat.CombatEngineAPI
import com.fs.starfarer.api.combat.EveryFrameCombatPlugin
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.fleet.FleetGoal
import com.fs.starfarer.api.fleet.FleetMemberType
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.combat.CombatEngine
import com.fs.state.AppDriver
import lunalib.backend.ui.components.util.TooltipHelper
import lunalib.lunaExtensions.*
import lunalib.lunaUI.elements.LunaSpriteElement
import org.lazywizard.lazylib.ext.campaign.addShip
import org.lwjgl.input.Keyboard
import java.awt.Color


class WeaponCraftingUISub(var parentPanel: WeaponCraftingUIMain, var data: SectorWeaponData, var dialog: InteractionDialogAPI) : CustomUIPanelPlugin{

    lateinit var panel: CustomPanelAPI
    var width = 0f
    var height = 0f

    lateinit var modifierElement: TooltipMakerAPI


    var lastSelectedModifier: ModularWeaponEffect = ModularRepo.modifiers.first()
    var selectedModifier: ModularWeaponEffect = lastSelectedModifier
    var descriptionTooltip: TooltipMakerAPI? = null
    var descriptionPanel: CustomPanelAPI? = null

    fun init(panel: CustomPanelAPI)
    {
        this.panel = panel

        width = panel.position.width
        height = panel.position.height

        modifierElement = panel.createUIElement(width , height, false)
        panel.addUIElement(modifierElement)

        modifierElement.addSpacer(15f)

        if (data.finalized)
        {
            addFinalizedPanel()
        }
        else
        {
            addCraftingPanel()
        }

    }


    fun addCraftingPanel()
    {
        var bar = modifierElement.addLunaProgressBar(data.getCapacity(), 0f, data.maxCapacity, width - 25, height * 0.075f, Misc.getTextColor())
        bar.enableTransparency = true
        bar.run {
            position.inTL(0f, 15f)

            var inner = bar.innerElement.addLunaElement(bar.position.width, bar.position.height)
            inner.position.inTL(0f, 0f)
            inner.enableTransparency = true
            inner.backgroundAlpha = 0.3f

            showNumber(false)

            bar.advance {
                changeBoundaries(0f, data.maxCapacity)
                changeValue(data.getCapacity())
                changePrefix("Capacity Remaining: ${(data.maxCapacity - data.getCapacity()).toInt()}")
            }
        }


        var bodySelector = modifierElement.addLunaElement(width / 2 - 25, height * 0.20f)
        bodySelector.position.belowLeft(bar.elementPanel, height * 0.02f)
        bodySelector.enableTransparency = true
        /*bodySelector.addText("Choose Weapon Size (TBD, Medium only for the moment.)", Misc.getBasePlayerColor())
        bodySelector.centerText()*/

        var bodySelectorHeaderElement = bodySelector.elementPanel.createUIElement(width / 2 - 25, height * 0.20f, false)
        bodySelector.elementPanel.addUIElement(bodySelectorHeaderElement)
        var bodyHeader = bodySelectorHeaderElement.addSectionHeading("Body", Alignment.MID, 0f)
        bodySelectorHeaderElement.addTooltipToPrevious(TooltipHelper("The weapon body determines the weapons size, base stats and sprite.", 300f), TooltipMakerAPI.TooltipLocation.ABOVE)

        var bodySelectorElement = bodySelector.elementPanel.createUIElement(width / 2 - 25, height * 0.20f - bodyHeader.position.height, true)
        addBodySelector(bodySelectorElement, width / 2 - 25, height * 0.20f - bodyHeader.position.height)
        bodySelector.elementPanel.addUIElement(bodySelectorElement)



        //Damage Type Selector
        var damageTypeSelector = modifierElement.addLunaElement(width / 2 - 25, height * 0.20f)
        damageTypeSelector.position.rightOfMid(bodySelector.elementPanel, 25f)
        damageTypeSelector.enableTransparency = true

        var damageTypeHeaderElement = damageTypeSelector.elementPanel.createUIElement(width / 2 - 25, height * 0.20f, false)
        damageTypeSelector.elementPanel.addUIElement(damageTypeHeaderElement)
        var damageHeader = damageTypeHeaderElement.addSectionHeading("Damage Type", Alignment.MID, 0f)

        var damageTypeSelectorElement = damageTypeSelector.elementPanel.createUIElement(width / 2 - 25, height * 0.20f - damageHeader.position.height, false)
        addDamageTypeSelector(damageTypeSelectorElement, width / 2 - 25, height * 0.20f - damageHeader.position.height)
        damageTypeSelector.elementPanel.addUIElement(damageTypeSelectorElement)



        //Effect Picker
        var effectPicker = modifierElement.addLunaElement(width * 0.4f - 25, height * 0.40f)
        effectPicker.position.belowLeft(bodySelector.elementPanel, height * 0.02f)
        effectPicker.enableTransparency = true
        effectPicker.backgroundAlpha = 0.8f

        var effectPickerHeaderElement = effectPicker.elementPanel.createUIElement(width * 0.4f - 25, height * 0.40f, false)
        effectPicker.elementPanel.addUIElement(effectPickerHeaderElement)
        var effectHeader = effectPickerHeaderElement.addSectionHeading("Modifiers", Alignment.MID, 0f)
        effectPickerHeaderElement.addTooltipToPrevious(TooltipHelper("Modifiers add a variety of effects to the weapon, but each one adds to the capacity limit.", 300f), TooltipMakerAPI.TooltipLocation.ABOVE)


        var effectPickerElement = effectPicker.elementPanel.createUIElement(width * 0.4f - 25, height * 0.40f - effectHeader.position.height, true)
        addEffectsSelector(effectPickerElement, width * 0.4f - 25, height * 0.40f - effectHeader.position.height)
        effectPicker.elementPanel.addUIElement(effectPickerElement)

        //Description
        var modifierDescriptionElement = modifierElement.addLunaElement(width * 0.6f - 25 , height * 0.40f)
        modifierDescriptionElement.position.rightOfMid(effectPicker.elementPanel, 25f)
        modifierDescriptionElement.enableTransparency = true
        modifierDescriptionElement.backgroundAlpha = 0.8f

        var modifierDescriptionHeaderElement = modifierDescriptionElement.elementPanel.createUIElement(width * 0.6f - 25, height * 0.40f, false)
        modifierDescriptionElement.elementPanel.addUIElement(modifierDescriptionHeaderElement)
        var modifierDescriptionHeader = modifierDescriptionHeaderElement.addSectionHeading("Modifier Description", Alignment.MID, 0f)

        descriptionTooltip = modifierDescriptionElement.elementPanel.createUIElement(width * 0.6f , height * 0.40f - modifierDescriptionHeader.position.height, false)
        //addDescription(descriptionElement, width / 2 , height * 0.40f - modifierDescriptionHeader.position.height)
        modifierDescriptionElement.elementPanel.addUIElement(descriptionTooltip)
        addDescription()

        //Visuals
        var visuals = modifierElement.addLunaElement(width / 3 - 25, height* 0.20f)
        visuals.position.belowLeft(effectPicker.elementPanel, height * 0.02f)
        visuals.enableTransparency = true

        visuals.innerElement.addSectionHeading("Visuals", Alignment.MID, 0f)
        visuals.innerElement.addSpacer(15f)

        var textfield = visuals.innerElement.addLunaTextfield("${data.name}", false, visuals.width - 10f, visuals.height * 0.25f)
        textfield.enableTransparency = true
        textfield.addTooltip("Adjust the Weapons Name", 200f, TooltipMakerAPI.TooltipLocation.RIGHT)
        textfield.advance {
            data.name = textfield.getText()
        }

        visuals.innerElement.addSpacer(15f)
        var c = data.color
        var colorPicker = visuals.innerElement.addLunaColorPicker(Color.RGBtoHSB(c.red, c.green, c.blue, null).get(0), visuals.width - 10f, visuals.height * 0.15f)
        colorPicker.addTooltip("Determines the color of the projectile and some effects.", 200f, TooltipMakerAPI.TooltipLocation.RIGHT)
        colorPicker.advance {
            data.color = colorPicker.getColor()
        }


        //Simulation
        var simulation = modifierElement.addLunaElement(width / 3 - 25 , height * 0.20f)
        simulation.position.rightOfMid(visuals.elementPanel, 25f)
        simulation.enableTransparency = true
        simulation.addText("Simulation", baseColor = Misc.getBasePlayerColor())
        simulation.centerText()
        simulation.addTooltip("Starts a quick simulation. Both the player piloted ship and the enemies ship have the currently selected weapon design equipped twice." +
                "\n\n" +
                "WARNING: Campaign Ships can be selected for deployment, damage to them will actually damage them.", 500f, TooltipMakerAPI.TooltipLocation.ABOVE)

        simulation.onHoverEnter() {
            simulation.borderColor = Misc.getDarkPlayerColor().brighter()
        }
        simulation.onHoverExit {
            simulation.borderColor = Misc.getDarkPlayerColor()
        }

        simulation.onClick {
            ModularWeaponLoader.applyStatsToSpec(data)
            startSimulation()
        }

        var finalize = modifierElement.addLunaChargeButton(width / 3 - 25 , height* 0.20f)
        finalize.parentElement.addTooltipToPrevious(object : TooltipCreator {
            override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
                return false
            }

            override fun getTooltipWidth(tooltipParam: Any?): Float {
                return width * 0.666f - 50
            }

            override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                tooltip!!.addSectionHeading("Info", Alignment.MID, 0f)
                tooltip.addSpacer(5f)
                tooltip.addPara("Hold this button to finalize all changes, doing so allows you to craft the weapon, but no more changes can ever be done to it in the future. " +
                        "Can only be finalized if the current capacity is below the maximum value." +
                        "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Hold", "finalize", "craft", "no more changes", "capacity")

                tooltip.addSpacer(5f)
                tooltip.addPara("Weapon Name: ${data.name}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Weapon Name")
                tooltip.addPara("Body Type: ${data.body.getName()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Body Type")
                tooltip.addPara("Capacity Remaining: ${data.maxCapacity - data.getCapacity()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Capacity Remaining")
                tooltip.addSpacer(5f)


                tooltip.addSectionHeading("Stats", Alignment.MID, 0f)
                tooltip.addSpacer(5f)
                generateStatsTooltip(tooltip)

            }
        }, TooltipMakerAPI.TooltipLocation.ABOVE)


        finalize.position.rightOfMid(simulation.elementPanel, 25f)
        finalize.enableTransparency = true
        finalize.addText("Finalize", baseColor = Misc.getBasePlayerColor())
        finalize.centerText()


        finalize.onHoverEnter() {
            finalize.borderColor = Misc.getDarkPlayerColor().brighter()
        }
        finalize.onHoverExit {
            finalize.borderColor = Misc.getDarkPlayerColor()
        }

        finalize.onFinish {
            data.finalized = true
            parentPanel.reset()
            ModularWeaponLoader.applyStatsToSpec(data)
        }

        finalize.advance {
            if (data.getCapacity() > data.maxCapacity)
            {
                finalize.increaseRate = 0f
                finalize.backgroundColor = Color(0, 0, 0)
            }
            else
            {
                finalize.backgroundColor = Misc.getDarkPlayerColor().darker()
                finalize.increaseRate = 0.025f
            }
        }
    }

    fun addBodySelector(element: TooltipMakerAPI, w: Float, h: Float)
    {
        var bods = ModularRepo.bodies.sortedBy { it.getCapacity() }

        var first = true
        for (body in bods)
        {
            var lunaEle = element.addLunaElement(w, h * 0.3f)

            if (first)
            {
                first = false

                lunaEle.position.inTL(0f, 0f)
            }


            lunaEle.addText("${body.getName()} (${body.getCapacity().toInt()})", Misc.getBasePlayerColor(), Misc.getHighlightColor(), listOf("${body.getName()}"))
            lunaEle.centerText()
            lunaEle.selectionGroup = "BodyType"
            lunaEle.enableTransparency = true
            lunaEle.backgroundAlpha = 0.1f

            if (body.getName() == data.body.getName())
            {
                lunaEle.select()
                data.body = body
            }

            lunaEle.parentElement.addTooltipToPrevious(object : TooltipCreator {
                override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
                    return false
                }

                override fun getTooltipWidth(tooltipParam: Any?): Float {
                    return w
                }


                override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {

                    tooltip!!.addSectionHeading("General", Alignment.MID, 0f)
                    tooltip.addSpacer(5f)
                    tooltip.addPara("Body: ${body.getName()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Body")
                    tooltip.addPara("Size: ${body.getSize().displayName}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Size")
                    tooltip.addPara("Total Capacity: ${body.getCapacity()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Total Capacity")
                    tooltip.addSpacer(5f)

                    tooltip.addSectionHeading("Info", Alignment.MID, 0f)
                    tooltip.addSpacer(5f)
                    body.addTooltip(tooltip)
                    tooltip.addSpacer(5f)

                    tooltip.addSectionHeading("Sprite", Alignment.MID, 0f)
                    tooltip.addSpacer(5f)
                    var image = tooltip.addLunaSpriteElement(body.getTurretSprite(), LunaSpriteElement.ScalingTypes.STRETCH_ELEMENT, 100f, 0f).apply {
                        enforceSize(40f, 40f, 100f, 100f)
                    }
                    tooltip.addSpacer(image.getSprite().height)
                    tooltip.addSpacer(5f)

                }
            }, TooltipMakerAPI.TooltipLocation.RIGHT)

            lunaEle.onHoverEnter {
                lunaEle.playScrollSound()
                lunaEle.borderColor = Misc.getDarkPlayerColor().brighter()
            }
            lunaEle.onHoverExit {
                lunaEle.borderColor = Misc.getDarkPlayerColor()
            }

            lunaEle.onClick {
                lunaEle.select()
                lunaEle.playClickSound()
                data.body = body
            }

            lunaEle.advance {
                if (lunaEle.isSelected())
                {
                    lunaEle.backgroundAlpha = 1f
                }
                else
                {
                    lunaEle.backgroundAlpha = 0.1f
                }
            }
        }
    }


    fun addDamageTypeSelector(element: TooltipMakerAPI, w: Float, h: Float)
    {
        var types = AvailableDamageTypes.values()

        var first = true
        for (type in types)
        {
            var lunaEle = element.addLunaElement(w, h / 3)

            if (first)
            {
                first = false

                lunaEle.position.inTL(0f, 0f)
            }


            lunaEle.addText("${type.displayName} (Cost: ${type.cost.toInt()})", Misc.getBasePlayerColor(), type.color, listOf(type.displayName) )
            lunaEle.centerText()
            lunaEle.selectionGroup = "DamageTypes"
            lunaEle.enableTransparency = true
            lunaEle.backgroundAlpha = 0.1f

            if (type.damageType == data.damageType)
            {
                lunaEle.select()
                data.damageType = type.damageType
                data.capacityAdditions.set("damage_type", type.cost)
            }

            lunaEle.onHoverEnter {
                lunaEle.playScrollSound()
                lunaEle.borderColor = Misc.getDarkPlayerColor().brighter()
            }
            lunaEle.onHoverExit {
                lunaEle.borderColor = Misc.getDarkPlayerColor()
            }

            lunaEle.onClick {
                lunaEle.select()
                lunaEle.playClickSound()
                data.damageType = type.damageType
                data.capacityAdditions.set("damage_type", type.cost)
            }

            lunaEle.advance {
                if (lunaEle.isSelected())
                {
                    lunaEle.backgroundAlpha = 1f
                }
                else
                {
                    lunaEle.backgroundAlpha = 0.1f
                }
            }
        }
    }

    fun addEffectsSelector(element: TooltipMakerAPI, w: Float, h: Float)
    {

        var first = true
        ModularEffectModifier.values().forEach { currentType ->
            for (effect in ModularRepo.modifiers.filter { it.getType() == currentType }.sortedByDescending { it.getCost() })
            {

                var lunaEle = element.addLunaElement(w, h * 0.15f)

                lunaEle.onHoverEnter {
                    lunaEle.playScrollSound()
                    lunaEle.borderColor = Misc.getDarkPlayerColor().brighter()
                    selectedModifier = effect
                }
                lunaEle.onHoverExit {
                    lunaEle.borderColor = Misc.getDarkPlayerColor()
                }



                if (first)
                {
                    first = false
                    lunaEle.position.inTL(0f, 0f)
                    lastSelectedModifier = effect
                    selectedModifier = effect
                }


                var prefixPara = lunaEle.innerElement.addPara("", 0f, effect.getType().color, effect.getType().color)
                prefixPara!!.position.inTL(5f, lunaEle.position.height / 2 - prefixPara.computeTextHeight("") / 2)


                var namePara = lunaEle.innerElement.addPara("${effect.getName()}", 0f, effect.getType().color, effect.getType().color)
                namePara!!.position.inTL(20f, lunaEle.position.height / 2 - namePara.computeTextHeight("") / 2)

                var budgetpara = lunaEle.innerElement.addPara("Cost: ${effect.getCost()}", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                budgetpara!!.position.inTL(w - budgetpara.computeTextWidth("Cost: ${effect.getCost()}") - 20, lunaEle.position.height / 2 - budgetpara.computeTextHeight("") / 2)


                lunaEle.selectionGroup = "own_${effect.getName()}"
                lunaEle.enableTransparency = true
                lunaEle.backgroundAlpha = 0.1f

                if (data.effects.any {it.getName() == effect.getName()})
                {
                    lunaEle.select()
                    data.capacityAdditions.set(effect.getName(), effect.getCost().toFloat())
                }


                lunaEle.onClick {
                    lunaEle.playClickSound()

                    if (lunaEle.isSelected())
                    {
                        lunaEle.unselect()
                        var eff = data.effects.find { it.getName() == effect.getName() }
                        if (eff != null)
                        {
                            data.effects.remove(eff)
                            data.capacityAdditions.set(effect.getName(), 0f)
                        }
                    }
                    else
                    {
                        lunaEle.select()

                        var eff = data.effects.find { it.getName() == effect.getName() }
                        if (eff == null)
                        {
                            data.effects.add(effect)
                        }
                        data.capacityAdditions.set(effect.getName(), effect.getCost().toFloat())
                    }
                }

                lunaEle.advance {
                    if (lunaEle.isSelected())
                    {
                        lunaEle.backgroundAlpha = 1f
                    }
                    else
                    {
                        lunaEle.backgroundAlpha = 0.1f
                    }

                    if (selectedModifier == effect)
                    {
                        prefixPara.text = ">"
                    }
                    else
                    {
                        prefixPara.text = ""
                    }
                }
            }
        }
    }

    fun addDescription()
    {
        lastSelectedModifier = selectedModifier

        if (descriptionTooltip == null) return
        if (descriptionPanel != null)
        {
            descriptionTooltip!!.removeComponent(descriptionPanel)
        }

        descriptionPanel = Global.getSettings().createCustom(width * 0.6f - 25 , height * 0.40f - 20f, null)
        descriptionTooltip!!.addComponent(descriptionPanel)
        var element = descriptionPanel!!.createUIElement(width * 0.6f - 25 , height * 0.40f - 20f, true)

        element.addPara("Modifier: ${selectedModifier.getName()}", 0f, Misc.getBasePlayerColor(), Misc.getHighlightColor(), "Modifier").position.inTL(5f, 5f)
        element.addPara("Type: ${selectedModifier.getType().displayName}", 0f, Misc.getBasePlayerColor(), Misc.getHighlightColor(), "Type")
        element.addPara("Cost: ${selectedModifier.getCost()}", 0f, Misc.getBasePlayerColor(), Misc.getHighlightColor(), "Cost")

        element.addSpacer(5f)
        element.addPara("Description:", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
        element.addSpacer(1f)

        selectedModifier.getTooltip(element)

        descriptionPanel!!.addUIElement(element)
    }

    fun addFinalizedPanel() {
        var craftButton = modifierElement.addLunaElement(width / 2 - 25, height * 0.15f)
        //sizeSelector.position.belowLeft(bar.elementPanel, height * 0.02f)
        craftButton.position.inTL(0f, 15f)
        craftButton.enableTransparency = true
        craftButton.addText("Craft Weapon", Misc.getBasePlayerColor())
        craftButton.centerText()

        craftButton.onClick {
            craftButton.playSound("ui_acquired_blueprint")
            Global.getSector().playerFleet.cargo.addWeapons(data.id, 1)
            Global.getSector().campaignUI.messageDisplay.addMessage("Crafted 1 " + data.name + " !")



        }

        craftButton.onHoverEnter {
            craftButton.borderColor = Misc.getDarkPlayerColor().brighter()
        }
        craftButton.onHoverExit {
            craftButton.borderColor = Misc.getDarkPlayerColor()
        }


        var resourceCosts = modifierElement.addLunaElement(width / 2 - 25, height * 0.15f)
        resourceCosts.position.rightOfMid(craftButton.elementPanel, 25f)
        resourceCosts.enableTransparency = true
        resourceCosts.addText("Crafting Cost (TBD)", Misc.getBasePlayerColor())
        resourceCosts.centerText()

        var stats = modifierElement.addLunaElement(width - 25, height * 0.70f)
        stats.position.belowLeft(craftButton.elementPanel, height * 0.02f)
        stats.enableTransparency = true

        var inner = stats.innerElement
        inner.addSpacer(5f)
        inner.addPara("Weapon Name: ${data.name}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Weapon Name")
        inner.addPara("Body Type: ${data.body.getName()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Body Type")
        inner.addPara("Color: color", 0f, data.color, Misc.getHighlightColor(), "Color:")
        generateStatsTooltip(stats.innerElement)

    }

    fun generateStatsTooltip(tooltip: TooltipMakerAPI)
    {

        tooltip.addSpacer(5f)

        tooltip.addPara("Ordnance Points: ${data.op.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Ordnance Points")
        tooltip.addPara("Weapon Size: ${data.weaponSize.displayName}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Weapon Size")
        tooltip.addPara("Damage Type: ${data.damageType.displayName}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Damage Type")

        tooltip.addSpacer(5f)

        tooltip.addPara("Range: ${data.range.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Range")

        tooltip.addSpacer(5f)

        tooltip.addPara("Damage/Shot: ${data.damagePerShot.modifiedValue.toInt()}x${data.burstSize.getValue().toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Damage/Shot")
        if (data.empDamage.modifiedValue > 1)
        {
            tooltip.addPara("EMP: ${data.empDamage.modifiedValue.toInt()}x${data.burstSize.getValue().toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "EMP")
        }

        tooltip.addPara("Energy/Shot: ${data.energyPerShot.modifiedValue.toInt()}x${data.burstSize.getValue().toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Energy/Shot")
        tooltip.addSpacer(5f)

        if (data.maxAmmo.getValue() != Int.MAX_VALUE)
        {
            tooltip.addPara("Ammo: ${data.maxAmmo.getValue().toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Ammo")
            tooltip.addPara("Ammo/Second: ${data.ammoPerSecond.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Ammo/Second")
        }
        tooltip.addSpacer(5f)

        tooltip.addPara("Charge Up: ${data.chargeUp.modifiedValue}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Charge Up")
        tooltip.addPara("Charge Down: ${data.chargeDown.modifiedValue}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Charge Down")
        tooltip.addPara("Burst Delay: ${data.burstDelay.modifiedValue}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Burst Delay")
        tooltip.addSpacer(5f)

        tooltip.addPara("Projectile Speed: ${data.projectileSpeed.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Projectile Speed")
        tooltip.addPara("Projectile Length: ${data.projectileLength.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Projectile Length")
        tooltip.addPara("Projectile Width: ${data.projectileWidth.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Projectile Width")
        tooltip.addSpacer(5f)

        tooltip.addPara("Minimum Spread: ${data.minSpread.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Minimum Spread")
        tooltip.addPara("Maximum Spread: ${data.maxSpread.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Maximum Spread")
        tooltip.addSpacer(5f)

        tooltip.addPara("Turn Rate: ${data.turnrate.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Turn Rate")


        tooltip.addSpacer(5f)

        tooltip.addPara("Effects: ${data.effects.map { it.getName() }.toString().replace("[", "").replace("]", "")}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Effects")




    }

    override fun positionChanged(position: PositionAPI?) {

    }

    override fun renderBelow(alphaMult: Float) {

    }

    override fun render(alphaMult: Float) {

    }

    override fun advance(amount: Float) {

        if (lastSelectedModifier != selectedModifier)
        {
            addDescription()
        }

        ModularWeaponLoader.calculateEffectStats(data)
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {

    }

    override fun buttonPressed(buttonId: Any?) {

    }

    fun startSimulation()
    {
        var playerFleet = Global.getFactory().createEmptyFleet(Factions.PLAYER, "Test", true)
        Global.getSector().playerFleet.starSystem.addEntity(playerFleet)
        playerFleet.setCircularOrbit(Global.getSector().playerFleet, 0f, 0f, 0f)

        var member = playerFleet.addShip("medusa_Hull", FleetMemberType.SHIP)
        var variant = member.variant

        variant.addWeapon("WS 006", data.id)
        variant.addWeapon("WS 007", data.id)
        variant.numFluxVents = 30
        variant.numFluxCapacitors = 30

        var enemyFleet = Global.getFactory().createEmptyFleet(Factions.HEGEMONY, "Test", true)
        var enemyMember = enemyFleet.addShip("medusa_Hull", FleetMemberType.SHIP)
        var enemyVariant = enemyMember.variant

        enemyVariant.addWeapon("WS 006", data.id)
        enemyVariant.addWeapon("WS 007", data.id)
        enemyVariant.numFluxVents = 30
        enemyVariant.numFluxCapacitors = 30

        val bcc = BattleCreationContext(playerFleet, FleetGoal.ATTACK, enemyFleet, FleetGoal.ATTACK)
        bcc.aiRetreatAllowed = false
        bcc.objectivesAllowed = false
        bcc.standoffRange = 1000f
        dialog.startBattle(bcc)


        Global.getSector().playerFleet.starSystem.removeEntity(playerFleet)

        Global.getCombatEngine().addPlugin( object : EveryFrameCombatPlugin {
            override fun init(engine: CombatEngineAPI?) {
            }

            override fun processInputPreCoreControls(amount: Float, events: MutableList<InputEventAPI>?) {
                events!!.forEach {
                    if (it.isConsumed) return@forEach
                    if (it.isKeyDownEvent && it.eventValue == Keyboard.KEY_ESCAPE)
                    {
                        Global.getCombatEngine().endCombat(0f)
                    }
                }
            }

            override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {

            }

            override fun renderInWorldCoords(viewport: ViewportAPI?) {

            }

            override fun renderInUICoords(viewport: ViewportAPI?) {

            }

        })
       // Global.getCombatEngine().endCombat(10f)
    }
}