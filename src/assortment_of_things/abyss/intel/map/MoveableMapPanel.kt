package assortment_of_things.abyss.intel.map

import com.fs.starfarer.api.campaign.CustomUIPanelPlugin
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import com.fs.starfarer.api.ui.UIPanelAPI
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ext.minus
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f
import org.magiclib.kotlin.isPointInBounds

class MoveableMapPanel : CustomUIPanelPlugin {


    class MoveableMapElement(var element: UIPanelAPI, var original: Vector2f, var x: Float, var y: Float, var range: Float)

    var component: MoveableMapElement? = null

    var mouseStart: Vector2f? = null


    override fun positionChanged(position: PositionAPI?) {

    }

    override fun renderBelow(alphaMult: Float) {

    }

    override fun render(alphaMult: Float) {

    }

    override fun advance(amount: Float) {

    }

    override fun processInput(events: MutableList<InputEventAPI>) {
        for (event in events) {
            if (event.isConsumed) continue
            if (event.isMouseDownEvent && event.eventValue == 1) {
               mouseStart = Vector2f(event.x.toFloat(), event.y.toFloat())
            }

            var range = component!!.range

            if (event.isMouseMoveEvent && mouseStart != null) {

                var current = Vector2f(event.x.toFloat(), event.y.toFloat())
                var difference = mouseStart!!.minus(current)

                var element = component!!.element
                var base = component!!.original


                var x = MathUtils.clamp(difference.x + component!!.x, -range, range)
                var y = MathUtils.clamp(difference.y + component!!.y, -range, range)


                //location = location.plus(difference)
                var location = base.plus(Vector2f(x, y))

                element.position.inTL(-location.x, location.y)

            }
            if (event.isMouseUpEvent && mouseStart != null) {

                var current = Vector2f(event.x.toFloat(), event.y.toFloat())
                var difference = mouseStart!!.minus(current)

                var element = component!!.element
                var base = component!!.original


                var x = MathUtils.clamp(difference.x + component!!.x, -range, range)
                var y = MathUtils.clamp(difference.y + component!!.y, -range, range)

                component!!.x = x
                component!!.y = y

                //location = location.plus(difference)
                var location = base.plus(Vector2f(x, y))

                element.position.inTL(-location.x, location.y)

                mouseStart = null

            }
        }
    }

    override fun buttonPressed(buttonId: Any?) {

    }

}