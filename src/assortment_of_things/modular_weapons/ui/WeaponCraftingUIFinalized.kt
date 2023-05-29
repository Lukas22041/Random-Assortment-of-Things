package assortment_of_things.modular_weapons.ui

import assortment_of_things.misc.RATSettings
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
import lunalib.backend.ui.components.util.TooltipHelper
import lunalib.lunaExtensions.*
import lunalib.lunaUI.elements.LunaSpriteElement
import org.lazywizard.lazylib.ext.campaign.addShip
import org.lwjgl.input.Keyboard
import java.awt.Color


class WeaponCraftingUIFinalized(var parentPanel: WeaponCraftingUIMain, var data: SectorWeaponData, var dialog: InteractionDialogAPI) : CustomUIPanelPlugin{

    lateinit var panel: CustomPanelAPI
    var width = 0f
    var height = 0f

    lateinit var modifierElement: TooltipMakerAPI


    var lastSelectedModifier: ModularWeaponEffect = ModularRepo.getUnlockedModifier().first()
    var selectedModifier: ModularWeaponEffect = lastSelectedModifier

    var descriptionTooltip: TooltipMakerAPI? = null
    var descriptionPanel: CustomPanelAPI? = null

    var craftingTooltip: TooltipMakerAPI? = null
    var craftingPanel: CustomPanelAPI? = null

    fun init(panel: CustomPanelAPI)
    {
        this.panel = panel

        width = panel.position.width
        height = panel.position.height

        modifierElement = panel.createUIElement(width , height, false)
        panel.addUIElement(modifierElement)

        modifierElement.addSpacer(15f)

        addFinalizedPanel()
    }

    fun addFinalizedPanel() {
        var craftButton = modifierElement.addLunaElement(width / 2 - 25, height * 0.20f)
        //sizeSelector.position.belowLeft(bar.elementPanel, height * 0.02f)
        craftButton.position.inTL(0f, 15f)
        craftButton.enableTransparency = true
        craftButton.addText("Craft Weapon", Misc.getBasePlayerColor())
        craftButton.centerText()

        craftButton.onClick {
            if (isCraftable() || RATSettings.modularDevmode!!)
            {
                craftButton.playSound("ui_acquired_blueprint")
                Global.getSector().playerFleet.cargo.addWeapons(data.id, 1)
                Global.getSector().campaignUI.messageDisplay.addMessage("Crafted ${data.name} (In Inventory: ${Global.getSector().playerFleet.cargo.getNumWeapons(data.id)}x)")

                if (!RATSettings.modularDevmode!!)
                {
                    var cargo = Global.getSector().playerFleet.cargo
                    for (cost in data.craftingCosts)
                    {
                        cargo.removeCommodity(cost.commodityId, cost.quantity.modifiedValue)
                    }
                }

                addCraftingCost()
            }
        }

        craftButton.onHoverEnter {
            craftButton.borderColor = Misc.getDarkPlayerColor().brighter()
        }
        craftButton.onHoverExit {
            craftButton.borderColor = Misc.getDarkPlayerColor()
        }

        craftButton.advance {
            if (isCraftable() || RATSettings.modularDevmode!!)
            {
                craftButton.backgroundColor = Misc.getDarkPlayerColor().darker()
            }
            else
            {
                craftButton.backgroundColor = Color(0, 0, 0)

            }
        }

        var craftingCostElement = modifierElement.addLunaElement(width / 2 - 25, height * 0.20f)
        craftingCostElement.position.rightOfMid(craftButton.elementPanel, 25f)
        craftingCostElement.enableTransparency = true
        craftingCostElement.backgroundAlpha = 0.8f

        var craftingCostHeaderElement = craftingCostElement.elementPanel.createUIElement(width / 2 - 25, height * 0.20f, false)
        craftingCostElement.elementPanel.addUIElement(craftingCostHeaderElement)
        var craftingCostHeader = craftingCostHeaderElement.addSectionHeading("Crafting Cost", Alignment.MID, 0f)

        craftingTooltip = craftingCostElement.elementPanel.createUIElement(width / 2 - 25, height * 0.20f - craftingCostHeader.position.height, false)
        craftingCostElement.elementPanel.addUIElement(craftingTooltip)
        addCraftingCost()



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

    fun isCraftable() : Boolean
    {
        var craftable = true
        var cargo = Global.getSector().playerFleet.cargo
        for (cost in data.craftingCosts)
        {
            if (cargo.getCommodityQuantity(cost.commodityId) < cost.quantity.modifiedValue)
            {
                craftable = false
            }
        }
        return craftable
    }

    fun addCraftingCost()
    {

        if (craftingTooltip == null) return
        if (craftingPanel != null)
        {
            craftingTooltip!!.removeComponent(craftingPanel)
        }

        data.generateCraftingCosts()

        craftingPanel = Global.getSettings().createCustom(width / 2 - 25, height * 0.20f - 20f, null)
        craftingTooltip!!.addComponent(craftingPanel)
        var element = craftingPanel!!.createUIElement(width / 2 - 25, height * 0.20f - 20f, true)
        element.addSpacer(5f)

        var cargo = Global.getSector().playerFleet.cargo
        for (cost in data.craftingCosts.sortedByDescending { it.quantity.modifiedValue })
        {
            var spec = Global.getSettings().getCommoditySpec(cost.commodityId)
            var img = element.beginImageWithText(spec.iconName, 20f)

            var inventoryColor = Misc.getTextColor()
            if (cost.quantity.modifiedValue > cargo.getCommodityQuantity(cost.commodityId)) inventoryColor = Misc.getNegativeHighlightColor()

            var para = img.addPara("${spec.name}\nx${cost.quantity.modifiedValue.toInt()}  (In Inventory: ${cargo.getCommodityQuantity(cost.commodityId).toInt()})", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "${spec.name}")
            para.setHighlight(spec.name, "x${cost.quantity.modifiedValue.toInt()}  (In Inventory: ${cargo.getCommodityQuantity(cost.commodityId).toInt()})")
            para.setHighlightColors(Misc.getHighlightColor(), inventoryColor)
            element.addImageWithText(0f)
            element.addSpacer(5f)
        }

        craftingPanel!!.addUIElement(element)
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

        ModularWeaponLoader.calculateEffectStats(data)
    }

    override fun processInput(events: MutableList<InputEventAPI>?) {

    }

    override fun buttonPressed(buttonId: Any?) {

    }
}