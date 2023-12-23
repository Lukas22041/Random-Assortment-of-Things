package assortment_of_things.combat

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageDealtModifier
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

class DPSMeter : BaseEveryFrameCombatPlugin(), DamageListener {


    var font: LazyFont = LazyFont.loadFont(Fonts.DEFAULT_SMALL)
    private var shieldTextDisplay = font.createText("DPS: ", Misc.getHighlightColor(), 15f)
    private var hullTextDisplay = font.createText("DPS: ", Misc.getHighlightColor(), 15f)
    private var armorTextDisplay = font.createText("DPS: ", Misc.getHighlightColor(), 15f)

    init {
        Global.getCombatEngine().listenerManager.addListener(this)
    }

    var maxMeasuringTime = 10f
    var hasBeenMeasuringFor = 0f
    var maxResetMeasuringTime = 10f
    var resetMeasuringTime = maxResetMeasuringTime
    var isMeasuring = false

    data class DealtDamage(var damage: ApplyDamageResultAPI, var timeRemainaing: Float)
    var dealtDamage = ArrayList<DealtDamage>()

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {

        if (dealtDamage.isEmpty()) {

        }

        if (isMeasuring && !Global.getCombatEngine().isPaused) {
            var mult = Global.getCombatEngine().timeMult.modifiedValue
            resetMeasuringTime -= 1 * amount

            if (resetMeasuringTime <= 0f) {
                dealtDamage.clear()
                isMeasuring = false
                return
            }

            var oldest = dealtDamage.sortedBy { it.timeRemainaing }.last()

            hasBeenMeasuringFor +=  1 * amount
            hasBeenMeasuringFor = MathUtils.clamp(hasBeenMeasuringFor, 0f, oldest.timeRemainaing)
        }

    }

    override fun renderInUICoords(viewport: ViewportAPI?) {

        //var dps = damage

        var posX = Global.getSettings().screenWidth - 200
        var posY = 150f

        var shieldDps = 0f
        var hullDps = 0f
        var armorDps = 0f
        for (damage in dealtDamage) {
            shieldDps += damage.damage.damageToShields
            hullDps += damage.damage.damageToHull
            armorDps += damage.damage.totalDamageToArmor
        }
        if (shieldDps != 0f) shieldDps /= hasBeenMeasuringFor
        if (hullDps != 0f) hullDps /= hasBeenMeasuringFor
        if (armorDps != 0f) armorDps /= hasBeenMeasuringFor

        var shieldDpsText = String.format("%.2f", shieldDps)
        var hullDpsText = String.format("%.2f", hullDps)
        var armorDpsText = String.format("%.2f", armorDps)

        shieldTextDisplay.baseColor = Color(0, 150, 255)
        shieldTextDisplay.text = "$shieldDpsText"
        shieldTextDisplay.draw(posX,posY);

        armorTextDisplay.baseColor = Color(245, 207, 20)
        armorTextDisplay.text = "$armorDpsText"
        armorTextDisplay.draw(posX,posY - 25f);

        hullTextDisplay.baseColor = Color(245, 65, 20)
        hullTextDisplay.text = "$hullDpsText"
        hullTextDisplay.draw(posX,posY - 50f);

    }


    override fun reportDamageApplied(source: Any?, target: CombatEntityAPI?, result: ApplyDamageResultAPI) {

        var dealer: Any? = null
        if (source is ShipAPI) {
            if (source.isFighter) {
                if (source.wing != null) {
                    dealer = source.wing.sourceShip
                }
            }
            else {
                dealer = source
            }
        }
        else {
            if (source is WeaponAPI) {
                dealer = source.ship
            }
        }

        if (dealer == Global.getCombatEngine().playerShip) {
            resetMeasuringTime = maxResetMeasuringTime
            isMeasuring = true
            dealtDamage.add(DealtDamage(result, maxMeasuringTime))
        }
    }
}