package assortment_of_things.abyss.procgen.scripts

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Pings
import com.fs.starfarer.api.loading.CampaignPingSpec
import com.fs.starfarer.api.util.IntervalUtil
import org.magiclib.kotlin.setAlpha
import java.awt.Color

class PrimordialCatalystPingScript(var entity: SectorEntityToken) : EveryFrameScript {

    var interval = IntervalUtil(14f, 16f)

    override fun isDone(): Boolean {
        return !entity.isDiscoverable
    }


    override fun runWhilePaused(): Boolean {
        return false
    }


    override fun advance(amount: Float) {

        interval.advance(amount)
        if (interval.intervalElapsed()) {

            var ping = CampaignPingSpec()
            ping.color = AbyssUtils.GENESIS_COLOR.setAlpha(60)
            ping.width = 10f
            ping.minRange = entity.radius
            ping.range = 9000f
            ping.duration = 15f
            ping.alphaMult = 1f
            ping.inFraction = 0.05f
            ping.num = 1
            ping.isInvert = false

            Global.getSector().addPing(entity, ping)
        }
    }
}