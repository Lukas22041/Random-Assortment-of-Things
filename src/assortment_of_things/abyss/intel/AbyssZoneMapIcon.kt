package assortment_of_things.abyss.intel

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgen
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.campaign.JumpPointAPI
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUI.elements.LunaElement
import org.lazywizard.console.commands.Jump
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.opengl.GL11
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AbyssZoneMapIcon(var system: StarSystemAPI, var color: Color, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var sprite = Global.getSettings().getSprite("graphics/icons/jump-point2.png")

    var connectedSystems = ArrayList<StarSystemAPI>()

    init {
        renderBorder = false
        renderBackground = false

        sprite.color = color

        sprite.alphaMult = 0.75f
        onHover {
            sprite.angle += 0.3f
        }
        onHoverEnter {
            sprite.alphaMult = 1f
            playScrollSound()
        }
        onHoverExit { sprite.alphaMult = 0.75f }

        onClick {
            playClickSound()

            if (Global.getSettings().isDevMode)
            {
                Global.getSector().campaignUI.messageDisplay.addMessage("Devmode: Jumping to ${system.baseName}")
                Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, Global.getSector().playerFleet, JumpPointAPI.JumpDestination(system.createToken(0f, 0f), ""), 0.1f)
            }
        }
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)



        if (Global.getSector().playerFleet.containingLocation == system)
        {
            sprite.color = Misc.getBasePlayerColor()
        }
        else
        {
            var tier = AbyssUtils.getTier(system)
            var color = AbyssUtils.getSystemColor(system)
            sprite.color = color
            if (tier == AbyssProcgen.Tier.Mid) sprite.color = color.brighter()
            if (tier == AbyssProcgen.Tier.High) sprite.color = color.brighter()
            /*var tier = AbyssUtils.getTier(system)
            when (tier) {
                AbyssProcgen.Tier.Low -> sprite.color = color.brighter()
                AbyssProcgen.Tier.Mid -> sprite.color = color
                AbyssProcgen.Tier.High -> sprite.color = color.darker()
                else -> sprite.color = color
            }*/
        }

        sprite.setSize(width, height)
        sprite.render(x, y)
    }

}