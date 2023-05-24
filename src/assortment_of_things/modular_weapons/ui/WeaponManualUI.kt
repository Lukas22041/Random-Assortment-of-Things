package assortment_of_things.modular_weapons.ui

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.InteractionDialogAPI
import com.fs.starfarer.api.campaign.comm.IntelInfoPlugin
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import lunalib.lunaDelegates.LunaMemory
import lunalib.lunaExtensions.addLunaElement
import org.lazywizard.lazylib.MathUtils

class WeaponManualUI(var parentPanel: WeaponCraftingUIMain, var dialog: InteractionDialogAPI) : CustomUIPanelPlugin {


    lateinit var panel: CustomPanelAPI
    var width = 0f
    var height = 0f

    lateinit var manual: TooltipMakerAPI

    var weaponScrap: Int? by LunaMemory("rat_weapon_scrap", 0)

    enum class BootStage { Start, Progress }
    companion object {
        var currentBootStage = BootStage.Start
    }

    var maxCharacters = 0

    var dointerval = false
    var endProgressInterval = IntervalUtil(1f, 1f)


    fun init(panel: CustomPanelAPI)
    {
        this.panel = panel

        width = panel.position.width
        height = panel.position.height

        manual = panel.createUIElement(width , height, false)
        panel.addUIElement(manual)

        manual.addSpacer(15f)

        var description = manual.addLunaElement(width - 5, height * 0.95f)
        description.enableTransparency = true
        description.position.inTL(0f, 10f)
        var descriptionElement = description.elementPanel.createUIElement(width - 5, height * 0.95f, true)
        descriptionElement.position.inTL(0f, 0f)
        descriptionElement.addPara("", 0f).position.inTL(10f, 0f)

        if (WeaponCraftingUIMain.firstBoot!!)
        {
            if (currentBootStage == BootStage.Start) {
                descriptionElement.addPara("You innitiate comm-contact with the forges interface.\n" +
                        "A connection is established, however the device seems in standby. It seems to require a manual bootup.", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())

                descriptionElement.addSpacer(10f)

                var bootButton = descriptionElement.addLunaElement(150f, 40f)

                bootButton.apply {
                    enableTransparency = true
                    addText("Boot Up", Misc.getBasePlayerColor())
                    centerText()

                    onHoverEnter {
                        borderColor = Misc.getDarkPlayerColor().brighter()
                        playScrollSound()
                    }
                    onHoverExit {
                        borderColor = Misc.getDarkPlayerColor()
                    }

                    onClick {
                        playClickSound()
                        currentBootStage = BootStage.Progress
                        parentPanel.recreateModifierPanel()
                    }

                }
                bootButton.position.inBMid(-50f)
            }

            if (currentBootStage == BootStage.Progress)
            {
                var para = descriptionElement.addPara(getDescription(), 0f, Misc.getBasePlayerColor(), Misc.getHighlightColor())


                description.advance {
                    maxCharacters++
                    if (dointerval)
                    {
                        endProgressInterval.advance(it)
                        if (endProgressInterval.intervalElapsed())
                        {
                            currentBootStage = BootStage.Start
                            WeaponCraftingUIMain.firstBoot = false
                            parentPanel.reset()
                        }
                    }
                    para.text = getDescription()
                }
            }
        }
        else
        {
           // descriptionElement.addPara(getDescription(), 0f, Misc.getBasePlayerColor(), Misc.getHighlightColor())

            descriptionElement.addPara("\"Welcome to the WForge Interface.\" expresses a robotic voice over the comm connection.\n" +
                "\"To ensure optimal useage, my instructions have been to carefully guide the user of their newly aqquired \"WForge-3600\" Model. \"\n\n\n" +

                "\"The WForge-3600 is using the most advanced domain technology to deliver the ultimate nanoforge experience, allowing the user to easily create their own ship weaponry." +
                "This is achieved by combining a weapon body and an assortment of modifiers of the users choice, to create their own, personal, dream weaponry. \n\n" +

                "The body sets which type of mount the weapon can fit in to, while also giving you a base behaviour to build upon on." +
                "Meanwhile, Modifiers add additional effects on to that template. Multiple modifiers can be applied at once, but each type of body has a limited capacity for modifiers. \n\n" +

                "Please keep in mind that there is a limited amount of designs that can be stored within the WForge-3600 database, and that once a design is asssembly-ready, it can not be changed again. " +
                "Please ensure to make good use of the simulation feature to avoid having any regrets. However, if more slots are required, we recommend reserving a purchase for our model 3650." +
                "\"",
                0f, Misc.getBasePlayerColor(), Misc.getHighlightColor(),
                "WForge-3600", "WForge-3600", "weapon body", "modifiers", "dream", "body", "mount", "Modifiers", "effects", "limited capacity", "limited amount of designs", "simulation")



            descriptionElement.addSpacer(20f)
            descriptionElement.addPara("Warning\n" +
                    "" +
                    "Detected a a corruption of some standard modifiers, due to this not all modifiers will be available. To avoid this occuring in the future, please keep the WForge stored within good conditions." +
                    "To aquire more modifiers, the forge can reverse engineer weapons that are beyond repair. The WForge will automaticly take broken weapons from \"unpleasent\" encounters for this goal, and turn the remainder in to " +
                    "scrap that can be used for the assembly of further weapons.\n\n", 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

        }




       /* for (i in 0..100)
        {
            descriptionElement.addPara("Test" + i, 0f, Misc.getBasePlayerColor(), Misc.getHighlightColor())
            descriptionElement.addSpacer(2f)
        }*/

        description.elementPanel.addUIElement(descriptionElement)




    }


    fun getDescription() : String {

        var text = ""
        text +=

        "-> Booting Up <- \n\n" +
        "-> Innitiating Delta Core Interface <- \n" +
        "-> Retrieving Base Blueprints <- \n" +
        "-> Failed Retrieving Base Blueprints <- \n" +
        "-> Retrieving Weapon Bodies <-\n" +
        "-> Retrieving Modifiers <-\n" +
        "-> Partialy Failed Retrieving Modifiers. <-\n" +
        "-> Finished Innitiating Interface <- \n\n" +
        "-> Boot Completed <- \n\n" +
        "-> Please enjoy your time with the WForge-3600! <- \n" +
        ""



        if (WeaponCraftingUIMain.firstBoot!!)
        {

            if (maxCharacters >= text.length )
            {
                dointerval = true
            }

            maxCharacters = MathUtils.clamp(maxCharacters, 0, text.length)

            var shown = text.substring(0, maxCharacters)
            var hidden = text.substring(maxCharacters, text.length)
            hidden = hidden.replace("[^\n ]".toRegex(), " ")

            text = shown + hidden


        }

        return text
    }

    override fun positionChanged(position: PositionAPI?) {

    }

    override fun renderBelow(alphaMult: Float) {

    }

    override fun render(alphaMult: Float) {

    }

    override fun advance(amount: Float) {

    }

    override fun processInput(events: MutableList<InputEventAPI>?) {

    }

    override fun buttonPressed(buttonId: Any?) {

    }

}