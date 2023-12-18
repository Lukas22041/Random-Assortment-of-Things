package assortment_of_things.eastereggs.ut

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import org.lwjgl.util.vector.Vector2f

class UTPlayer {

    var maxHp = 20
    var hp = 20
    var position = Vector2f()
    var sprite = Global.getSettings().getAndLoadSprite("graphics/eastereggs/ut/player.png")

    var size = Vector2f(30f, 30f)

    var velocity = Vector2f()
}