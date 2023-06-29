package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Pings
import com.fs.starfarer.api.loading.CampaignPingSpec
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.campaign.DynamicRingBand
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.setAlpha
import java.awt.Color


class AbyssalFractureJumpvisual : BaseCustomEntityPlugin() {

    var connectedEntity: SectorEntityToken? = null
    val radius = 100.0f


    @Transient
    var band1: DynamicRingBand? = null

    @Transient
    var band2: DynamicRingBand? = null

    var sizeMult = 0f
    var speedMult = 0f

    var timestamp = Global.getSector().clock.timestamp

    var pingInterval = IntervalUtil(0.25f, 0.25f)

    init {
        initBandIfNull()
    }

    override fun advance(amount: Float) {
        super.advance(amount)

        initBandIfNull()

        band1!!.advance(amount)
        band2!!.advance(amount)

        var player = Global.getSector().playerFleet

        if (!Global.getSector().isPaused)
        {
            var daysSince = Global.getSector().clock.getElapsedDaysSince(timestamp)
            if (daysSince < 0.6)
            {
                sizeMult += 0.5f * amount
                sizeMult = MathUtils.clamp(sizeMult, 0f, 1f)

                speedMult += 2f * amount
                speedMult = MathUtils.clamp(speedMult, 0f, 10f)

                pingInterval.advance(amount)
                if (pingInterval.intervalElapsed())
                {
                    val custom = CampaignPingSpec()
                    custom.color = AbyssUtils.ABYSS_COLOR
                    custom.width = 10f
                    custom.minRange = 100f
                    custom.range = 500f
                    custom.duration = 2f
                    custom.alphaMult = 0.5f
                    custom.inFraction = 0.2f
                    custom.num = 1

                    Global.getSector().addPing(entity, custom)
                }

            }
            else if (daysSince in 0.5f..0.8f)
            {
                speedMult -= 5f * amount
                speedMult = MathUtils.clamp(speedMult, 0f, 10f)
            }
            else if (daysSince in 0.8f..2f)
            {
                sizeMult -= 0.25f * amount
                sizeMult = MathUtils.clamp(sizeMult, 0f, 1f)

                speedMult -= 4f * amount
                speedMult = MathUtils.clamp(speedMult, -10f, 10f)
            }
            else
            {
                entity.containingLocation.removeEntity(entity)
            }
        }




    }



    fun initBandIfNull()
    {
        if (band1 != null) return
        val var1: Float = radius * 0.45f
        val var2: Float = radius * 3.1415927f * 2.0f
        var var3 = var2 / 50.0f
        if (var3 < 3.0f) {
            var3 = 3.0f
        }

        if (var3 > 10.0f) {
            var3 = 10.0f
        }

        val var4: Float = radius * 1.25f
        var var5: Float = radius * 0.005f
        if (var5 > 0.5f) {
            var5 = 0.5f
        }



        band1 = DynamicRingBand("rat_terrain", "wormhole_bands", 64.0f, 2, Color.white, var4,  var3,  var1 + radius * 0.25f - var4 * 0.05f, 5.0f, var5, 100.0f, 10.0f, true)
        band2 = DynamicRingBand("rat_terrain", "wormhole_bands", 64.0f, 2, Color.white, var4,  var3,  var1 + radius * 0.25f - var4 * 0.05f, -5.0f, var5, 100.0f, 10.0f, true)

    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)



        var curRadius = radius * sizeMult

        val var1: Float = curRadius * 0.45f
        val var2: Float = curRadius * 3.1415927f * 2.0f
        var var3 = var2 / 50.0f
        if (var3 < 3.0f) {
            var3 = 3.0f
        }

        if (var3 > 10.0f) {
            var3 = 10.0f
        }

        val var4: Float = curRadius * 1.25f
        var var5: Float = curRadius * 0.005f
        if (var5 > 0.5f) {
            var5 = 0.5f
        }

        band1!!.bandWidthInEngine = var4
        band1!!.pixelsPerSegment = var3
        band1!!.innerRadius = var1 + radius * 0.25f - var4 * 0.05f
        band1!!.angularVelocity = 50f * speedMult

        band2!!.bandWidthInEngine = var4
        band2!!.pixelsPerSegment = var3
        band2!!.innerRadius = var1 + radius * 0.25f - var4 * 0.05f
        band2!!.angularVelocity = 25f * speedMult

       /* band1!!.extraAlphaMult = sizeMult
        band2!!.extraAlphaMult = sizeMult*/

        band1!!.color = AbyssUtils.ABYSS_COLOR.setAlpha(100)
        band2!!.color = AbyssUtils.ABYSS_COLOR.setAlpha(200)

        band1!!.render(entity.location.x, entity.location.y, viewport!!.alphaMult)
        band2!!.render(entity.location.x, entity.location.y, viewport!!.alphaMult)


    }


}