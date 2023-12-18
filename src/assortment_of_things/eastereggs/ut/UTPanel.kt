package assortment_of_things.eastereggs.ut

import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.PositionAPI
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.input.Keyboard
import org.lwjgl.util.vector.Vector2f

class UTPanel {

    var player = UTPlayer()
    var box = UTBoundingBox()

    var x = 0f
    var y = 0f
    var width = 1920f / Global.getSettings().screenScaleMult * 1.0f
    var height = 1080f / Global.getSettings().screenScaleMult  * 1.0f

    var speed = 60f

    var background = Global.getSettings().getAndLoadSprite("graphics/eastereggs/ut/background.png")
    var seb = Global.getSettings().getAndLoadSprite("graphics/eastereggs/ut/boss2.png")

    fun updatePanelLocation(position: PositionAPI) {
        var utX = position.centerX - width / 2
        var utY = position.centerY - height / 2

        this.x = utX
        this.y = utY

        box.position = Vector2f(x + (width * 0.5f) - (box.size.x * 0.5f), y + (height * 0.5f) - (box.size.y * 0.5f) - 50f)

        player.position.set(Vector2f(box.position.x + box.size.x / 2, box.position.y + box.size.y / 2))
    }

    fun init() {

    }

    fun advance(amount: Float) {

        box.maxSize = Vector2f(350f, 200f)

        if (box.size.x >= box.maxSize.x) box.size.x -= 500 * amount
        if (box.size.x < box.maxSize.x) box.size.x += 500 * amount

        if (box.size.y > box.maxSize.y) box.size.y -= 500 * amount
        if (box.size.y < box.maxSize.y) box.size.y += 500 * amount

        box.position = Vector2f(x + (width * 0.5f) - (box.size.x * 0.5f), y + (height * 0.5f) - (box.size.y * 0.5f) - 100f)

        player.velocity = Vector2f()

        var speedAdd = 5f
        if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) speedAdd = 2.5f

        if (Keyboard.isKeyDown(Keyboard.KEY_W)) player.velocity.y = speedAdd
        if (Keyboard.isKeyDown(Keyboard.KEY_S)) player.velocity.y = -speedAdd
        if (Keyboard.isKeyDown(Keyboard.KEY_A)) player.velocity.x = -speedAdd
        if (Keyboard.isKeyDown(Keyboard.KEY_D)) player.velocity.x = speedAdd

        var posX = player.position.x + (player.velocity.x * speed * amount)
        var posY = player.position.y + (player.velocity.y * speed * amount)

        posX = MathUtils.clamp(posX, box.position.x + (player.size.x ), box.position.x + box.size.x - (player.size.x))
        posY = MathUtils.clamp(posY, box.position.y + (player.size.y ), box.position.y + box.size.y - (player.size.y))

        player.position.set(Vector2f(posX, posY))
    }


    fun render(alpha: Float) {
        background.alphaMult = alpha
        seb.alphaMult = alpha
        player.sprite.alphaMult = alpha
        box.sprite.alphaMult = alpha
        box.line.alphaMult = alpha

        //Background
        background.setSize(width, height)
        background.render(x, y)


        seb.setSize(432f, 591f)
        seb.renderAtCenter(box.position.x + box.size.x / 2, box.position.y + 550)

        //Box
       /* box.sprite.setSize(box.size.x, box.size.y)
        box.sprite.render(box.position.x, box.position.y)*/

        box.line.setSize(10f, box.size.y + 10)
        box.line.angle = 0f
        box.line.renderAtCenter(box.position.x, box.position.y + box.size.y / 2)
        box.line.renderAtCenter(box.position.x + box.size.x, box.position.y + box.size.y / 2)
      //  box.line.render(box.position.x + box.size.x, box.position.y)

        box.line.setSize(10f, box.size.x + 10)
        box.line.angle = 90f
        box.line.renderAtCenter(box.position.x + box.size.x / 2 , box.position.y + box.size.y)
        box.line.renderAtCenter(box.position.x + box.size.x / 2 , box.position.y )
  //      box.line.render(box.position.x + box.size.x / 2, box.position.y - box.size.x / 2)

        //Player
        var playerSprite = player.sprite
        playerSprite.setSize(player.size.x, player.size.y)
        playerSprite.renderAtCenter(player.position.x, player.position.y)
    }

    fun input(events: MutableList<InputEventAPI>) {

    }

}