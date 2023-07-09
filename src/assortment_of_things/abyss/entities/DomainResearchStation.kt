package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.util.FaderUtil
import com.fs.starfarer.api.util.IntervalUtil
import java.util.*

class DomainResearchStation : BaseCustomEntityPlugin() {

  /*  @Transient
    var stationSprite = Global.getSettings().getSprite("rat_entities", "research_station")*/

    @Transient
    var stationGlow = Global.getSettings().getSprite("rat_entities", "research_station_glow")


    var randomRotation = Random().nextFloat() > 0.5
    var fader = FaderUtil(0f, 3f, 4f, true, true)

    var interval = IntervalUtil(5f, 10f)

    override fun advance(amount: Float) {
        super.advance(amount)

        if (stationGlow == null)
        {
            //stationSprite = Global.getSettings().getSprite("rat_entities", "research_station")

            stationGlow = Global.getSettings().getSprite("rat_entities", "research_station_glow")
        }


        if (randomRotation)
        {
            entity.facing += 0.02f
        }
        else
        {
            entity.facing -= 0.02f
        }

        fader.advance(amount)
        if (fader.brightness >= 1)
        {
            fader.fadeOut()
        }
        if (fader.brightness <= 0.2f)
        {
            fader.fadeIn()
        }

        interval.advance(amount)

        var player = Global.getSector().playerFleet

        if (interval.intervalElapsed())  {
            AbyssUtils.createSensorIcon(entity, 12500f, AbyssUtils.SUPERCHARGED_COLOR)
        }
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        super.render(layer, viewport)

        /*stationSprite.angle = entity.facing
        stationSprite.renderAtCenter(entity.location.x, entity.location.y)*/

        stationGlow.angle = entity.facing + 90
        stationGlow.setAdditiveBlend()
        stationGlow.alphaMult = fader.brightness
        stationGlow.renderAtCenter(entity.location.x, entity.location.y)
    }



}