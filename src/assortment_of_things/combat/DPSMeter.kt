/*
package assortment_of_things.combat

import assortment_of_things.misc.RATSettings
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.combat.*
import com.fs.starfarer.api.combat.listeners.ApplyDamageResultAPI
import com.fs.starfarer.api.combat.listeners.DamageListener
import com.fs.starfarer.api.input.InputEventAPI
import com.fs.starfarer.api.ui.Fonts
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lazywizard.lazylib.ui.LazyFont
import java.awt.Color

class DPSMeter : BaseEveryFrameCombatPlugin(), DamageListener {


    var font: LazyFont = LazyFont.loadFont(Fonts.ORBITRON_20AABOLD)
    private var shieldTextDisplay = font.createText("DPS: ", Misc.getHighlightColor(), 15f)
    private var hullTextDisplay = font.createText("DPS: ", Misc.getHighlightColor(), 15f)
    private var armorTextDisplay = font.createText("DPS: ", Misc.getHighlightColor(), 15f)


    var updateInterval = IntervalUtil(0.1f, 0.1f)

    var shieldDpsText = "0.00"
    var hullDpsText = "0.00"
    var armorDpsText = "0.00"

    var shieldBanner = Global.getSettings().getAndLoadSprite("graphics/ui/rat_dps_shield.png")
    var hullBanner = Global.getSettings().getAndLoadSprite("graphics/ui/rat_dps_hull.png")
    var armorBanner = Global.getSettings().getAndLoadSprite("graphics/ui/rat_dps_armor.png")
    var dpsBackground = Global.getSettings().getAndLoadSprite("graphics/ui/rat_dps_background.png")

    //Make configureable through luna
    var timeForReset = RATSettings.dpsMeterSeconds!!.toFloat()
    var measuredTime = 0f
    var resetTime = 0f

    var recentDamage = ArrayList<ApplyDamageResultAPI>()


    init {
        Global.getCombatEngine().listenerManager.addListener(this)
        updateDps()
    }

    override fun advance(amount: Float, events: MutableList<InputEventAPI>?) {

        if (recentDamage.isNotEmpty()) {
            if (!Global.getCombatEngine().isPaused) {

                resetTime += 1 * amount

                if (resetTime >= timeForReset) {
                    recentDamage.clear()
                    measuredTime = 0f
                    resetTime = 0f
                }
                else {
                    measuredTime += 1 * amount
                }
            }
        }
        else {
            measuredTime = 0f
        }

        updateInterval.advance(amount)
        if (updateInterval.intervalElapsed() && !Global.getCombatEngine().isPaused) {
            updateDps()
        }


    }

    override fun renderInUICoords(viewport: ViewportAPI?) {

        //var dps = damage

        var posX = Global.getSettings().screenWidth - 80
        var posY = 120f

        var armorOffset = 40f
        var hullOffset = 80f

        dpsBackground.renderAtCenter(posX, posY - armorOffset)

        shieldBanner.renderAtCenter(posX, posY)
        armorBanner.renderAtCenter(posX, posY - armorOffset)
        hullBanner.renderAtCenter(posX, posY - hullOffset)

        shieldTextDisplay.baseColor =  Color.LIGHT_GRAY
        shieldTextDisplay.text = "$shieldDpsText"
        shieldTextDisplay.draw(posX - shieldTextDisplay.width / 2,posY + shieldTextDisplay.height / 2);



        armorTextDisplay.baseColor = Color.LIGHT_GRAY
        armorTextDisplay.text = "$armorDpsText"
        armorTextDisplay.draw(posX - armorTextDisplay.width / 2,posY + armorTextDisplay.height / 2 - armorOffset);

        hullTextDisplay.baseColor =  Color.LIGHT_GRAY
        hullTextDisplay.text = "$hullDpsText"
        hullTextDisplay.draw(posX - hullTextDisplay.width / 2,posY + hullTextDisplay.height / 2 - hullOffset);

    }


    fun updateDps() {
        //var oldest = recentDamage.sortedBy { it.timeRemainaing }.lastOrNull()?.timeRemainaing

        var shieldDps = 0f
        var hullDps = 0f
        var armorDps = 0f
        for (damage in recentDamage) {
            shieldDps += damage.damageToShields
            hullDps += damage.damageToHull
            armorDps += damage.totalDamageToArmor
        }

        */
/*if (oldest != null) {
            if (shieldDps != 0f) shieldDps /= oldest
            if (hullDps != 0f) hullDps /= oldest
            if (armorDps != 0f) armorDps /= oldest
        }*//*


        if (measuredTime != 0f) {
            shieldDps /= measuredTime
            hullDps /= measuredTime
            armorDps /= measuredTime
        }

        shieldDpsText = String.format("%.2f", shieldDps)
        hullDpsText = String.format("%.2f", hullDps)
        armorDpsText = String.format("%.2f", armorDps)
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
            */
/*resetMeasuringTime = maxResetMeasuringTime
            isMeasuring = true*//*

            resetTime = 0f
            recentDamage.add(result)
        }
    }
}*/
