package assortment_of_things.modular_weapons.ui

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.effects.*
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.*
import java.awt.Color

class WeaponCraftingUISub(var parentPanel: WeaponCraftingUIMain, var data: SectorWeaponData) : CustomUIPanelPlugin{

    lateinit var panel: CustomPanelAPI
    var width = 0f
    var height = 0f

    lateinit var modifierElement: TooltipMakerAPI

    var effects = listOf(OnHitExplosiveCharge(), VisualTrail(), PassiveGuidance(), OnHitOvercharged(), StatBurst(), StatDampener(),
    StatAmplifier(), StatHeavyMunition(), StatEscapeVelocity(), StatDoubleBarrel(), StatAutoloader(), PassiveOvervolt(),
    StatImprovedCoils(), StatEfficientGyro(), PassiveClover(), StatHighValueMunition(), PassiveDefenseProtocol(), PassiveAcidicPayload(),
    OnHitLifesteal())

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
        var bar = modifierElement.addLunaProgressBar(data.getBudget(), 0f, data.maxBudget, width - 25, height * 0.075f, Misc.getTextColor())
        bar.enableTransparency = true
        bar.run {
            position.inTL(0f, 15f)

            var inner = bar.innerElement.addLunaElement(bar.position.width, bar.position.height)
            inner.position.inTL(0f, 0f)
            inner.enableTransparency = true
            inner.backgroundAlpha = 0.3f

            showNumber(false)


            bar.advance {
                changeValue(data.getBudget())
                changePrefix("Budget Left: ${(data.maxBudget - data.getBudget()).toInt()}")
            }
        }


        var sizeSelector = modifierElement.addLunaElement(width / 2 - 25, height * 0.15f)
        sizeSelector.position.belowLeft(bar.elementPanel, height * 0.02f)
        sizeSelector.enableTransparency = true
        sizeSelector.addText("Choose Weapon Size (TBD, Medium only for the moment.)", Misc.getBasePlayerColor())
        sizeSelector.centerText()

        var damageTypeSelector = modifierElement.addLunaElement(width / 2 - 25, height * 0.15f)
        damageTypeSelector.position.rightOfMid(sizeSelector.elementPanel, 25f)
        damageTypeSelector.enableTransparency = true
        var damageTypeSelectorElement = damageTypeSelector.elementPanel.createUIElement(width / 2 - 25, height * 0.15f, true)
        addDamageTypeSelector(damageTypeSelectorElement, width / 2 - 25, height * 0.15f )
        damageTypeSelector.elementPanel.addUIElement(damageTypeSelectorElement)

        var effectPicker = modifierElement.addLunaElement(width - 25, height * 0.40f)
        effectPicker.position.belowLeft(sizeSelector.elementPanel, height * 0.02f)
        effectPicker.enableTransparency = true
        effectPicker.backgroundAlpha = 0.8f

        var effectPickerElement = effectPicker.elementPanel.createUIElement(width - 25, height * 0.40f, true)
        addEffectsSelector(effectPickerElement, width - 25, height * 0.40f )
        effectPicker.elementPanel.addUIElement(effectPickerElement)


        var visuals = modifierElement.addLunaElement(width / 3 - 25, height* 0.20f)
        visuals.position.belowLeft(effectPicker.elementPanel, height * 0.02f)
        visuals.enableTransparency = true

        visuals.innerElement.addPara("Change the Weapons Visuals.", 0f, Misc.getBasePlayerColor(), Misc.getHighlightColor()).position.inTL(5f, 10f)
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



        var finalize = modifierElement.addLunaChargeButton(width * 0.666f - 25 , height* 0.20f)
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
                        "Can only be finalized if the current budget is below the maximum value." +
                        "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Hold", "finalize", "craft", "no more changes", "budget")

                tooltip.addSpacer(5f)
                tooltip.addPara("Weapon Name: ${data.name}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Weapon Name")
                tooltip.addPara("Budget Left: ${data.maxBudget - data.getBudget()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Budget Left")
                tooltip.addSpacer(5f)


                tooltip.addSectionHeading("Stats", Alignment.MID, 0f)
                tooltip.addSpacer(5f)
                generateStatsTooltip(tooltip)

            }
        }, TooltipMakerAPI.TooltipLocation.ABOVE)


        finalize.position.rightOfMid(visuals.elementPanel, 25f)
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
            parentPanel.recreateModifierPanel()
            ModularWeaponLoader.applyStatsToSpec(data)
        }

        finalize.advance {
            if (data.getBudget() > data.maxBudget)
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

    fun addDamageTypeSelector(element: TooltipMakerAPI, w: Float, h: Float)
    {
        var types = AvailableDamageTypes.values()

        var first = true
        for (type in types)
        {
            var lunaEle = element.addLunaElement(w, h * 0.4f)

            if (first)
            {
                first = false
                lunaEle.position.inTL(0f, 0f)
            }


            lunaEle.addText("Damage Type: ${type.displayName} (${type.cost.toInt()}B)", Misc.getBasePlayerColor(), type.color, listOf(type.displayName) )
            lunaEle.centerText()
            lunaEle.selectionGroup = "DamageTypes"
            lunaEle.enableTransparency = true
            lunaEle.backgroundAlpha = 0.1f

            if (type.damageType == data.damageType)
            {
                lunaEle.select()
                data.damageType = type.damageType
                data.budgetAdditions.set("damage_type", type.cost)
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
                data.budgetAdditions.set("damage_type", type.cost)
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
        ModularEffectType.values().forEach { currentType ->
            for (effect in effects.filter { it.getType() == currentType }.sortedByDescending { it.getCost() })
            {

                var lunaEle = element.addLunaElement(w, h * 0.15f)

                lunaEle.onHoverEnter {
                    lunaEle.playScrollSound()
                    lunaEle.borderColor = Misc.getDarkPlayerColor().brighter()
                }
                lunaEle.onHoverExit {
                    lunaEle.borderColor = Misc.getDarkPlayerColor()
                }

                lunaEle.parentElement.addTooltipToPrevious(object : TooltipCreator {
                    override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
                        return false
                    }

                    override fun getTooltipWidth(tooltipParam: Any?): Float {
                        return w / 2
                    }


                    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {

                        tooltip!!.addSectionHeading("General", Alignment.MID, 0f)
                        tooltip.addSpacer(5f)
                        tooltip.addPara("Effect Name: ${effect.getName()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Effect Name")
                        tooltip.addPara("Effect Type: ${effect.getType().displayName}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Effect Type")
                        tooltip.addPara("Budget Cost: ${effect.getCost()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Budget Cost")
                        tooltip.addSpacer(5f)

                        tooltip.addSectionHeading("Effect", Alignment.MID, 0f)
                        tooltip.addSpacer(5f)
                        effect.getTooltip(tooltip)
                        tooltip.addSpacer(5f)

                    }
                }, TooltipMakerAPI.TooltipLocation.RIGHT)

                if (first)
                {
                    first = false
                    lunaEle.position.inTL(0f, 0f)
                }



                // lunaEle.centerText()
                /* var para = lunaEle.innerElement.addPara("(${effect.getType().displayName}) ${effect.getName()} (${effect.getCost()}B)", 0f)
                 para.setHighlight("(${effect.getType().displayName})", "${effect.getName()}")
                 para.setHighlightColors(effect.getType().color, Misc.getHighlightColor())
                 para!!.position.inTL(20f, lunaEle.position.height / 2 - para.computeTextHeight("") / 2)*/

                var effectPara = lunaEle.innerElement.addPara("${effect.getType().displayName}", 0f, effect.getType().color, effect.getType().color)
                effectPara!!.position.inTL(20f, lunaEle.position.height / 2 - effectPara.computeTextHeight("") / 2)

                var namePara = lunaEle.innerElement.addPara("${effect.getName()}", 0f,  Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                namePara!!.position.inTL(w / 2 - namePara.computeTextWidth("${effect.getName()}") / 2, lunaEle.position.height / 2 - namePara.computeTextHeight("") / 2)

                var budgetpara = lunaEle.innerElement.addPara("Cost: ${effect.getCost()}", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
                budgetpara!!.position.inTL(w - budgetpara.computeTextWidth("Cost: ${effect.getCost()}") - 20, lunaEle.position.height / 2 - budgetpara.computeTextHeight("") / 2)


                lunaEle.selectionGroup = "own_${effect.getName()}"
                lunaEle.enableTransparency = true
                lunaEle.backgroundAlpha = 0.1f

                if (data.effects.any {it.getName() == effect.getName()})
                {
                    lunaEle.select()
                    data.budgetAdditions.set(effect.getName(), effect.getCost().toFloat())
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
                            data.budgetAdditions.set(effect.getName(), 0f)
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
                        data.budgetAdditions.set(effect.getName(), effect.getCost().toFloat())
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
                }
            }
        }
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
        inner.addPara("Color: color", 0f, data.color, Misc.getHighlightColor(), "Color")
        generateStatsTooltip(stats.innerElement)

    }

    fun generateStatsTooltip(tooltip: TooltipMakerAPI)
    {

        tooltip.addSpacer(5f)

        tooltip.addPara("Weapon Size: ${data.weaponSize.displayName}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Weapon Size")
        tooltip.addPara("Damage Type: ${data.damageType.displayName}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Damage Type")

        tooltip.addSpacer(5f)

        tooltip.addPara("Ordnance Points: ${data.op.modifiedValue}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Ordnance Points")

        tooltip.addSpacer(5f)

        tooltip.addPara("Range: ${data.range.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Range")

        tooltip.addSpacer(5f)

        tooltip.addPara("Damage/Shot: ${data.damagePerShot.modifiedValue.toInt()}x${data.burstSize.getValue().toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Damage/Shot")
        if (data.empDamage.modifiedValue > 1)
        {
            tooltip.addPara("EMP: ${data.empDamage.modifiedValue.toInt()}x${data.burstSize.getValue().toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "EMP")
        }

        tooltip.addSpacer(5f)
        tooltip.addPara("Energy/Shot: ${data.energyPerShot.modifiedValue.toInt()}x${data.burstSize.getValue().toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Energy/Shot")
        tooltip.addSpacer(5f)

        if (data.maxAmmo.getValue() != Int.MAX_VALUE)
        {
            tooltip.addPara("Ammo: ${data.maxAmmo.getValue().toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Ammo")
            tooltip.addPara("Ammo/Second: ${data.ammoPerSecond.modifiedValue.toInt()}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Ammo/Second")
        }

        tooltip.addSpacer(5f)

        tooltip.addPara("Effects: ${data.effects.map { it.getName() }.toString().replace("[", "").replace("]", "")}", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "Effects")




    }

    fun calculateBudget()
    {

    }

    override fun positionChanged(position: PositionAPI?) {

    }

    override fun renderBelow(alphaMult: Float) {

    }

    override fun render(alphaMult: Float) {

    }

    override fun advance(amount: Float) {
        ModularWeaponLoader.calculateEffectStats(data)
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {

    }

}