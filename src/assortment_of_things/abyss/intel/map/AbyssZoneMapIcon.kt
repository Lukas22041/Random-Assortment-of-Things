package assortment_of_things.abyss.intel.map

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssDepth
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaUI.elements.LunaElement
import org.lwjgl.util.vector.Vector2f
import java.awt.Color

class AbyssZoneMapIcon(var system: StarSystemAPI, var color: Color, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var sprite = Global.getSettings().getSprite("graphics/planets/sun_halo.png")

    var connectedSystems = ArrayList<StarSystemAPI>()

    init {
        renderBorder = false
        renderBackground = false
        enableTransparency = true

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
               // Global.getSector().doHyperspaceTransition(Global.getSector().playerFleet, Global.getSector().playerFleet, JumpPointAPI.JumpDestination(system.createToken(0f, 0f), ""), 0.1f)

                var playerFleet = Global.getSector().playerFleet
                var currentLocation = playerFleet.containingLocation

                currentLocation.removeEntity(playerFleet)
                system.addEntity(playerFleet)
                Global.getSector().setCurrentLocation(system)
                playerFleet.location.set(Vector2f(0f, 0f))
            }
        }
    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)



        if (Global.getSector().playerFleet.containingLocation == system)
        {
            sprite.color = Misc.getDarkPlayerColor().brighter()
        }
        else
        {
            var data = AbyssUtils.getSystemData(system)
            var depth = data.depth
            var color = data.getColor().darker().darker()
            sprite.color = color
            if (depth == AbyssDepth.Shallow) color = color.brighter()
            if (depth == AbyssDepth.Deep) color = color.darker()
            if (isHovering) color = color.brighter()
            if (system.name.contains("Twilight")) color = color.brighter()
            sprite.color = color

        }


        if (isHovering) {
            sprite.setSize(width + 10, height + 10)
            sprite.render(x - 5, y - 5)
        }
        else {
            sprite.setSize(width, height)
            sprite.render(x, y)
        }
    }

}