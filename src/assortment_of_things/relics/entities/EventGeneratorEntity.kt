package assortment_of_things.relics.entities

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignTerrainAPI
import com.fs.starfarer.api.campaign.ai.ModularFleetAIAPI
import com.fs.starfarer.api.campaign.listeners.CampaignInputListener
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Tags
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.campaign.CampaignEngine
import org.lazywizard.lazylib.campaign.CampaignUtils
import org.lazywizard.lazylib.ext.campaign.contains
import org.lwjgl.input.Mouse
import org.lwjgl.util.vector.Vector2f

class EventGeneratorEntity : BaseCustomEntityPlugin() {

    var ring: CampaignTerrainAPI? = null
    var inputManager: GeneratorInputListener? = null

    override fun advance(amount: Float) {
        if (ring == null) return
        var ringPlugin = ring!!.plugin
        if (ringPlugin !is RingSystemTerrainPlugin) return

     //   if (ring !is RingSystemTerrainPlugin) return

        var mousePos = Vector2f(Mouse.getX().toFloat(), Mouse.getY().toFloat())


        var mouseX = 0f
        var mouseY = 0f

        if (inputManager != null) {

            mouseX = inputManager!!.x
            mouseY = inputManager!!.y

            var viewport = Global.getSector().viewport

            mousePos = Vector2f(viewport.convertScreenXToWorldX(mouseX), viewport.convertScreenYToWorldY(mouseY))
        }

        entity.orbit = null

        if (ringPlugin!!.containsPoint(mousePos, 10f)  && Global.getSector().playerFleet.interactionTarget != entity) {
            entity.setFixedLocation(mousePos.x, mousePos.y)
        }
        else {
          //  entity.setLocation(100000f, 100000f)
        }
    }


}