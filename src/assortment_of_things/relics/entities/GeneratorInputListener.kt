package assortment_of_things.relics.entities

import com.fs.starfarer.api.campaign.listeners.CampaignInputListener
import com.fs.starfarer.api.input.InputEventAPI

class GeneratorInputListener : CampaignInputListener {
    /**
     * Higher number = higher priority, i.e. gets to process input first.
     * @return
     */

    var x = 0f
    var y = 0f

    override fun getListenerInputPriority(): Int {
        return 1000
    }

    override fun processCampaignInputPreCore(events: MutableList<InputEventAPI>?) {

    }

    override fun processCampaignInputPreFleetControl(events: MutableList<InputEventAPI>?) {
        for (event in events!!) {
            if (event.isConsumed) continue
            if (event.isMouseEvent) {
                x = event.x.toFloat()
                y = event.y.toFloat()
            }
        }
    }

    override fun processCampaignInputPostCore(events: MutableList<InputEventAPI>?) {

    }

}