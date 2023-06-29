package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.AbyssTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.loading.CampaignPingSpec
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.IntervalUtil
import org.lazywizard.lazylib.MathUtils
import org.magiclib.kotlin.fadeAndExpire
import java.util.*

class AbyssMapIcon : BaseCustomEntityPlugin() {

    var timestamp = Global.getSector().clock.timestamp

    var maxDays = MathUtils.getRandomNumberInRange(6f, 8f)

    override fun advance(amount: Float) {
        super.advance(amount)

        if (entity.orbitFocus != null)
        {
            if (entity.containingLocation != entity.orbitFocus.containingLocation)
            {
                entity.fadeAndExpire(0f)
            }
        }

        if (entity.hasTag(AbyssTags.HOSTILE_ICON))
        {
            if (Global.getSector().clock.getElapsedDaysSince(timestamp) > maxDays)
            {
                entity.fadeAndExpire(0f)
            }
            if (entity.orbitFocus != null)
            {
                if (entity.orbitFocus.isVisibleToPlayerFleet)
                {
                    entity.fadeAndExpire(0f)
                }
            }
        }
        else
        {
            if (entity.orbitFocus != null)
            {
                if (!entity.orbitFocus.isDiscoverable)
                {
                    entity.fadeAndExpire(0f)
                }
            }
        }
    }
}