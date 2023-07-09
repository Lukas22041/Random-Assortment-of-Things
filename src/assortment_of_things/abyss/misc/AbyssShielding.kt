package assortment_of_things.abyss.misc

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.ReflectionUtils
import assortment_of_things.misc.setOpacity
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.LocationAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignClock
import com.fs.starfarer.campaign.CampaignState
import com.fs.state.AppDriver
import lunalib.lunaDelegates.LunaMemory
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaExtensions.addLunaProgressBar
import lunalib.lunaUI.elements.LunaProgressBar
import org.lazywizard.lazylib.MathUtils
import java.awt.Color

class AbyssShielding : EveryFrameScript {

    var frames = 0f
    var added = false
    var panel: CustomPanelAPI? = null
    var map: UIPanelAPI? = null
    var lastLocation: LocationAPI = Global.getSector().playerFleet.containingLocation

    var alphaMult = 1f
    var bar: LunaProgressBar? = null

    var baseShielding = 30f
    var maxShielding: Float = 0f
    var siphon: Float = 0f
    var decayrate: Float = 0f

    var shielding: Float? by LunaMemory("rat_abyss_currentShielding", 50000f)
    var ejectSeconds: Float? by LunaMemory("rat_abyss_eject", 20f)

    var ejecting = false

    companion object {
        var reset = false
    }

    init {
        reset = false
    }

    override fun isDone(): Boolean {
        return false
    }

    override fun runWhilePaused(): Boolean {
        return true
    }



    override fun advance(amount: Float) {
        frames++
        frames = MathUtils.clamp(frames, 0f, 100f)
        var state = AppDriver.getInstance().currentState
        if (state !is CampaignState) return
        var core = ReflectionUtils.invoke("getCore", state) as UIPanelAPI

        siphon = AbyssUtils.getSiphonPerDay()
        maxShielding = AbyssUtils.getMaxShielding()
        decayrate = 0.1f + AbyssUtils.getShieldLossPerDay()

        shielding = MathUtils.clamp(shielding!!, 0f, baseShielding + maxShielding)

        var paused =
            (Global.getSector().getCampaignUI().getCurrentCoreTab() == null &&
                    !Global.getSector().getCampaignUI().isShowingDialog() &&
                    !Global.getSector().getCampaignUI().isShowingMenu());


        var player = Global.getSector().playerFleet


        if (ejecting)
        {
            if (!Global.getSector().playerFleet.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))
            {
                ejecting = false
                ejectSeconds = 30f
            }
        }

        if (bar != null)
        {




            if (shielding!! < 0.1 && player.containingLocation.hasTag(AbyssUtils.SYSTEM_TAG))
            {

                var clock = Global.getSector().clock
                ejectSeconds = ejectSeconds!! - (amount)

                var days = ejectSeconds!! / 10
                days = MathUtils.clamp(days, 0f, 100f)

                if (ejectSeconds != null)
                {
                    var dayOrDays = "DAYS"
                    if (days in 1f..2f)
                    {
                        dayOrDays = "DAY"
                    }
                    bar!!.changePrefix("OUT OF SHIELDING: EJECT IN ${days.toInt()} $dayOrDays")
                }


                if (ejecting)
                {
                    bar!!.changePrefix("EJECTING")
                }
                else if (days <= 0 && !ejecting)
                {
                    var key = "\$rat_abyss_token"
                    var memory = Global.getSector().memoryWithoutUpdate



                    var hyperspaceToken: SectorEntityToken? = null
                    if (!memory.contains(key))
                    {
                        hyperspaceToken = Global.getSector().hyperspace.createToken(0f, 0f)
                    }
                    else
                    {
                        hyperspaceToken = memory.get(key) as SectorEntityToken
                    }
                    Global.getSector().doHyperspaceTransition(player, player, JumpPointAPI.JumpDestination(hyperspaceToken, ""), 0.2f)

                    ejecting = true
                    bar!!.changePrefix("EJECTING")
                }
            }
            else
            {
                ejecting = false
                ejectSeconds = 30f

                if (!Global.getSector().isPaused)
                {
                    shielding = shielding!! - decayrate / CampaignClock.SECONDS_PER_GAME_DAY * amount
                }

                bar!!.changePrefix("Abyssal Shielding")
                bar!!.changeBoundaries(0f, baseShielding + maxShielding)
                bar!!.changeValue(shielding!!)
            }
        }

        if (panel != null)
        {
            panel!!.setOpacity(alphaMult)
        }

        if (paused)
        {
            alphaMult = MathUtils.clamp(alphaMult + 0.3f, 0f, 1f)
        }
        else
        {
            alphaMult = MathUtils.clamp(alphaMult - 0.3f, 0f, 1f)
        }

        if ((frames > 3 && !added) || lastLocation != Global.getSector().playerFleet.containingLocation || reset)
        {
            lastLocation = Global.getSector().playerFleet.containingLocation

            if (!Global.getSector().currentLocation.hasTag(AbyssUtils.SYSTEM_TAG))
            {
                if (panel != null)
                {
                    core.removeComponent(panel)
                    bar = null
                    panel = null
                }
                return
            }

            if (panel != null)
            {
                core.removeComponent(panel)
                bar = null
                panel = null
            }

            var width = 300f
            var height = 20f

            panel = Global.getSettings().createCustom(width, height, null)
            core.addComponent(panel)

            panel!!.position.inTL(0f, 0f)

            var element = panel!!.createUIElement(width, height, false)
            panel!!.addUIElement(element)


            var lunaElement = element.addLunaElement(width, height)
            lunaElement.backgroundColor = Color(30, 30, 30)
            lunaElement.borderColor = Color(0, 0, 0)
            bar = lunaElement.innerElement.addLunaProgressBar(shielding!!, 0f, baseShielding + maxShielding, width, height/* - header.position.height*/, Misc.getTextColor())
            bar!!.position.inTL(0f, 0f)

            bar!!.backgroundColor = AbyssUtils.SUPERCHARGED_COLOR
            bar!!.renderBorder = false
            bar!!.enableTransparency = false
            bar!!.borderAlpha = 0.2f

            var tooltip = object : TooltipCreator {
                override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
                    return false
                }

                override fun getTooltipWidth(tooltipParam: Any?): Float {
                    return 400f
                }

                override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {

                    var maxShield = (baseShielding + maxShielding).toInt()
                    var days = (shielding!! / decayrate).toInt()

                    tooltip!!.addPara("To stay within the abyss, some method of shielding is required. \n\n" +
                            "This shielding is slowly used up over the next $days days. Each ship increases the amount of shielding used up per day.\n\n" +
                            "Without any shielding, the fleet is unable to withstand the forces and will slowly be ejected back in to Hyperspace.\n\n" +
                            "" +
                            "It can be siphoned from supercharged abyssal fog.\n\n" +
                            "" +
                            "Current Shielding: ${shielding!!.toInt()}\n" +
                            "Max Shielding: ${maxShield}\n" +
                            "Loss per day: ${decayrate}\n" +
                            "Siphon per day: ${siphon.toInt()}\n"

                        , 0f, Misc.getTextColor(), Misc.getHighlightColor(), "$days days", "Current Shielding:", "Max Shielding:", "Loss per day:", "Siphon per day:")
                }

            }
            lunaElement.innerElement.addTooltipToPrevious(tooltip, TooltipMakerAPI.TooltipLocation.BELOW, true)





            bar!!.changePrefix("Abyssal Shielding")
            bar!!.showNumber(false)
            //panel!!.position.aboveMid(core.getChildrenCopy().get(3), 30f)
            panel!!.position.inTL(Global.getSettings().screenWidth * 0.5f - width / 2, 30f)
            element.position.inTL(0f, 0f)


            reset = false
            added = true
        }
    }
}