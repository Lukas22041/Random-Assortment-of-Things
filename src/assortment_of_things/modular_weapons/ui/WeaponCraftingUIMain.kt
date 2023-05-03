package assortment_of_things.modular_weapons.ui

import assortment_of_things.modular_weapons.data.SectorWeaponData
import assortment_of_things.modular_weapons.util.ModularWeaponLoader
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
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

        weaponListPanel = panel.createCustomPanel(width * 0.2f, height, null)
        panel.addComponent(weaponListPanel)
        weaponListElement = weaponListPanel!!.createUIElement(width * 0.2f, height, true)

        weaponListElement!!.addPara("", 0f).position.inTL(7f, 0f)

        var weapons = ModularWeaponLoader.getAllDataAsList()
        for (data in weapons)
        {

            weaponListElement!!.addLunaElement(width * 0.2f - 14, 50f).apply {
                enableTransparency = true

                addText(data.name, Misc.getBasePlayerColor())
                centerText()

                this.selectionGroup = "WeaponSelection"

                onClick {
                    playClickSound()
                    selectedData = data
                    recreateModifierPanel()
                    select()
                }

                if (selectedData == null){
                    selectedData = data
                    select()
                    recreateModifierPanel()
                }

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
    }

    fun recreateModifierPanel()
    {
        if (selectedData == null) return
        if (weaponListPanel != null)
        {
            panel.removeComponent(modifierPanel)
        }


        var plugin = WeaponCraftingUISub(this, selectedData!!)
        modifierPanel = panel.createCustomPanel(width * 0.79f, height, plugin)
        panel.addComponent(modifierPanel)
        modifierPanel!!.position.inTL(width * 0.21f, 0f)
        plugin.init(modifierPanel!!)






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