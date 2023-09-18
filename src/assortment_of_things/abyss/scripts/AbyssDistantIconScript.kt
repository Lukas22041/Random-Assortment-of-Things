package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.EveryFrameScript
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.loading.CampaignPingSpec
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import java.awt.Color

class AbyssDistantIconScript(var entity: SectorEntityToken, var maxRange: Float) : EveryFrameScript {

    var interval = IntervalUtil(4f, 8f)
    var done = false

    override fun isDone(): Boolean {
        return entity.isExpired || done
    }

    override fun runWhilePaused(): Boolean {
        return false
    }

    override fun advance(amount: Float) {

        interval.advance(amount)
        if (!interval.intervalElapsed()) return

        var player = Global.getSector().playerFleet
        if (player.containingLocation != entity.containingLocation) return
        if (!entity.isDiscoverable) return
        var distance = MathUtils.getDistance(player, entity)

        if (distance > maxRange) return

        if (entity.containingLocation.customEntities.any { it.id ==  "${entity.id}_icon"} ) return

        var icon = entity.containingLocation.addCustomEntity("${entity.id}_icon", "Unknown Entity", "rat_abyss_icon", Factions.NEUTRAL)

        icon.location.set(entity.location)
        icon.setCircularOrbit(entity, 0f, 0f, 0f)

        Global.getSoundPlayer().playUISound("ui_discovered_entity", 0.5f, 1f)

        val custom = CampaignPingSpec()
        custom.color = Color(0, 150, 100)
        custom.width = 10f
        custom.minRange = player.radius
        custom.range = player.radius + 300
        custom.duration = 2f
        custom.alphaMult = 1f
        custom.inFraction = 0.1f
        custom.num = 1
        custom.isInvert = true

        Global.getSector().addPing(player, custom)
        done = true

    }


}