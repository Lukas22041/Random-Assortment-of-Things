package assortment_of_things.modular_weapons.ui

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.Alignment
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDelegates.LunaMemory
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaUI.panel.LunaBaseCustomPanelPlugin
import org.lwjgl.input.Keyboard

class WeaponCraftingUIMain : LunaBaseCustomPanelPlugin() {

    private var width = 0f
    private var height = 0f

    var weaponListPanel: CustomPanelAPI? = null
    var weaponListElement: TooltipMakerAPI? = null

    var selectedData: SectorWeaponData? = null

    var modifierPanel: CustomPanelAPI? = null

    companion object {
        var firstBoot: Boolean? by LunaMemory("rat_weapon_firstBoot", true)
    }

    override fun init() {

        width = panel.position.width
        height = panel.position.height

        reset()
    }

    fun reset()
    {
        createWeaponList()
        recreateModifierPanel()
    }

    fun createWeaponList()
    {
        if (weaponListPanel != null)
        {
            panel.removeComponent(weaponListPanel)
        }

        weaponListPanel = panel.createCustomPanel(width * 0.2f, height - 15f, null)
        panel.addComponent(weaponListPanel)
        weaponListPanel!!.position.inTL(0f, 0f)

        /*var headerElement = weaponListPanel!!.createUIElement(width * 0.2f - 14, height, false)
        weaponListPanel!!.addUIElement(headerElement)
        var header = headerElement.addSectionHeading("Designs", Alignment.MID, 0f)*/

        // header.position.inTL(7f, 0f)

        weaponListElement = weaponListPanel!!.createUIElement(width * 0.2f, height - 15f, true)
       // var space = weaponListElement!!.addLunaElement(0f, 0f).position.inTL(7f, 0f)

      //  header.position.inTL(7f, 10f)


        var firstFinalized = true
        var firstUnfinished = true

        var weapons = ModularWeaponLoader.getAllDataAsList().sortedWith(compareBy({!it.finalized}, {it.numericalID}))
        var spacing = 10f

        var manualButton = weaponListElement!!.addLunaElement(width * 0.2f - 14, 40f)
        manualButton.apply {
            enableTransparency = true
            position.inTL(0f, spacing)
            selectionGroup = "WeaponSelection"



            addText("Manual", baseColor = Misc.getBasePlayerColor())
            if (firstBoot!!) changeText("######")
            centerText()

            onClick {
                playClickSound()
                selectedData = null
                recreateModifierPanel()
                select()
            }

            if (selectedData == null){
                select()
                recreateModifierPanel()
            }
            onHoverEnter {
                playScrollSound()
            }

            advance {
                if (isSelected())
                {
                    backgroundColor = Misc.getDarkPlayerColor()
                    borderColor = Misc.getDarkPlayerColor().brighter()
                }
                else
                {
                    backgroundColor = Misc.getDarkPlayerColor().darker()

                    if (isHovering)
                    {
                        borderColor = Misc.getDarkPlayerColor().brighter()

                    }
                    else
                    {
                        borderColor = Misc.getDarkPlayerColor()
                    }
                }

            }
        }

        if (firstBoot!!)
        {
            manualButton.position.inTL(10f, spacing)
            weaponListPanel!!.addUIElement(weaponListElement)
            return
        }

        spacing += manualButton.position.height


        for (data in weapons)
        {
            if (firstFinalized && data.finalized)
            {
                firstFinalized = false
                weaponListElement!!.addSpacer(10f)
                spacing += 10f
                var header = weaponListElement!!.addSectionHeading("Finished Designs", Alignment.MID, 0f)
                header.position.setSize(width * 0.2f - 14f, 20f)
                header.position.inTL(0f, spacing)
                weaponListElement!!.addSpacer(5f)
                spacing += header.position.height + 5
            }

            if (firstUnfinished && !data.finalized)
            {
                firstUnfinished = false
                weaponListElement!!.addSpacer(10f)
                spacing += 10f
                var header = weaponListElement!!.addSectionHeading("Unfinished Designs", Alignment.MID, 0f)
                // header.position.inTL(0f, 0f)
                header.position.setSize(width * 0.2f - 14f, 20f)
                header.position.inTL(0f, spacing)
                weaponListElement!!.addSpacer(5f)
                spacing += header.position.height + 5
            }

            weaponListElement!!.addLunaElement(width * 0.2f - 14, 50f).apply {
                enableTransparency = true
                position.inTL(0f, spacing)
                spacing += 55
                addText(data.name, Misc.getBasePlayerColor())
                centerText()

                this.selectionGroup = "WeaponSelection"

                onClick {
                    playClickSound()
                    selectedData = data
                    recreateModifierPanel()
                    select()
                }

               /* if (selectedData == null){
                    selectedData = data
                    select()
                    recreateModifierPanel()
                }*/

                if (selectedData == data)
                {
                    select()
                    recreateModifierPanel()
                }

                onHoverEnter {
                     playScrollSound()
                }

                advance {
                    if (isSelected())
                    {
                        if (paragraph != null)
                        {
                            if (paragraph!!.text != data.name)
                            {
                                changeText(data.name)
                                centerText()
                            }
                        }

                        backgroundColor = Misc.getDarkPlayerColor()
                        borderColor = Misc.getDarkPlayerColor().brighter()
                    }
                    else
                    {
                        backgroundColor = Misc.getDarkPlayerColor().darker()

                        if (isHovering)
                        {
                            borderColor = Misc.getDarkPlayerColor().brighter()

                        }
                        else
                        {
                            borderColor = Misc.getDarkPlayerColor()
                        }
                    }

                }
            }

            weaponListElement!!.addSpacer(5f)
        }
        weaponListElement!!.addPara("", 0f)
        weaponListPanel!!.addUIElement(weaponListElement)
        weaponListElement!!.position.inTL(7f, 0f)

    }



    fun recreateModifierPanel()
    {
        if (weaponListPanel != null)
        {
            panel.removeComponent(modifierPanel)
        }

        if (selectedData != null)
        {
            var plugin = WeaponCraftingUISub(this, selectedData!!, dialog)
            modifierPanel = panel.createCustomPanel(width * 0.79f, height, plugin)
            panel.addComponent(modifierPanel)
            modifierPanel!!.position.inTL(width * 0.21f, 0f)
            plugin.init(modifierPanel!!)
        }
        else
        {
            var plugin = WeaponManualUI(this, dialog)
            modifierPanel = panel.createCustomPanel(width * 0.79f, height, plugin)
            panel.addComponent(modifierPanel)
            modifierPanel!!.position.inTL(width * 0.21f, 0f)
            plugin.init(modifierPanel!!)
        }






    }

    override fun processInput(events: MutableList<InputEventAPI>) {
        super.processInput(events)

        events.forEach { event ->
            if (event.isKeyDownEvent && event.eventValue == Keyboard.KEY_ESCAPE)
            {
                close()

                return@forEach
            }
        }
    }
}