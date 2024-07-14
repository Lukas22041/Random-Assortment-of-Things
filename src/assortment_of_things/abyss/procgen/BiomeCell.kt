package assortment_of_things.abyss.procgen

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.graphics.SpriteAPI
import org.lazywizard.lazylib.MathUtils

data class BiomeCell(var x: Int, var y: Int, var size: Float) {

    var isFake = false

    var spriteAngle = MathUtils.getRandomNumberInRange(0f, 360f)
    var spriteAlpha = MathUtils.getRandomNumberInRange(0.6f, 1f)

}