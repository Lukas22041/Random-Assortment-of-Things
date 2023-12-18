package assortment_of_things.eastereggs.ut

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import org.lwjgl.util.vector.Vector2f

class UTBoundingBox {

    var position = Vector2f()
    var size = Vector2f(150f, 200f)
    var maxSize = Vector2f(150f, 200f)
    var sprite = Global.getSettings().getAndLoadSprite("graphics/eastereggs/ut/battlebox.png")
    var line = Global.getSettings().getAndLoadSprite("graphics/eastereggs/ut/boxline.png")

}