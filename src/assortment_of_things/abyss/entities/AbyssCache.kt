package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.util.IntervalUtil
import java.util.*

class AbyssCache : BaseCustomEntityPlugin() {


    var randomRotation = Random().nextFloat() > 0.5

    var interval = IntervalUtil(5f, 10f)

    override fun advance(amount: Float) {
        super.advance(amount)



        if (randomRotation)
        {
            entity.facing += 0.02f
        }
        else
        {
            entity.facing -= 0.02f
        }

        interval.advance(amount)

        var player = Global.getSector().playerFleet

        if (interval.intervalElapsed())  {
            AbyssUtils.createSensorIcon(entity, 7500f, AbyssUtils.SUPERCHARGED_COLOR)
        }
    }
}