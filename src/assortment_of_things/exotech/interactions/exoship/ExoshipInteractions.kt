package assortment_of_things.exotech.interactions.exoship

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.ids.Tags
import org.lwjgl.util.vector.Vector2f

class ExoshipInteractions : RATInteractionPlugin() {
    override fun init() {

        createOption("Move") {
            var ship = interactionTarget
            ship.orbit = null
            ship.velocity.set(Vector2f(0f, 0f))
            ship.addTag(Tags.NON_CLICKABLE)

            var system = Global.getSector().starSystems.filter { it.planets.any { planet -> !planet.isStar } && it != ship.starSystem }.random()
            var planet = system.planets.filter { !it.isStar }.random()

            Global.getSector().addScript(ExoshipMoveScript(ship, planet))

            closeDialog()
        }

        addLeaveOption()
    }

}