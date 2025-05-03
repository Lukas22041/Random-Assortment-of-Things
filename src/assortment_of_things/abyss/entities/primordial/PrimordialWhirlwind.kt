package assortment_of_things.abyss.entities.primordial

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.biomes.PrimordialWaters
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import java.awt.Color

class PrimordialWhirlwind : BaseCustomEntityPlugin() {

    @Transient
    var wormhole1 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    @Transient
    var wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    @Transient
    var wormhole3 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")

    var manager = AbyssUtils.getBiomeManager()
    var biome = manager.getBiome("primordial_waters") as PrimordialWaters

    override fun init(entity: SectorEntityToken?, params: Any?) {
        this.entity = entity

    }

    override fun advance(amount: Float) {
        if (!Global.getSector().isPaused && wormhole1 != null) {
            wormhole1.angle += 5f * amount
            wormhole2.angle += 2.5f * amount
            wormhole3.angle += 0.25f * amount
        }
    }

    var width = 4000f
    var height = width


    override fun getRenderRange(): Float {
        return width+1000f
    }


    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI?) {
        if (wormhole1 == null) {
            wormhole1 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
            wormhole2 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
            wormhole3 = Global.getSettings().getAndLoadSprite("graphics/fx/wormhole.png")
        }

        if (entity == null) return


        var level = biome.getLevel()

        if (level <= 0) return

        if (level > 0 && level < 1) biome.startStencil(false)




        var x = entity.location.x - width / 2
        var y = entity.location.y - height / 2


        var color = Color(125, 0, 200)
        wormhole1.setSize(width * 0.7f, width *  0.7f)
        wormhole1.setAdditiveBlend()
        wormhole1.alphaMult = 0.25f
        wormhole1.color = color
        wormhole1.renderAtCenter(x + width / 2, y + height / 2)

        color = Color(200, 0, 150)
        wormhole2.setSize(width * 1f, width *  1f)
        wormhole2.setAdditiveBlend()
        wormhole2.alphaMult = 0.125f
        wormhole2.color = color
        wormhole2.renderAtCenter(x + width / 2, y + height / 2)

        color = Color(100, 0, 255)
        wormhole3.setSize(width * 1.5f, width *  1.5f)
        wormhole3.setAdditiveBlend()
        wormhole3.alphaMult = 0.20f
        wormhole3.color = color
        wormhole3.renderAtCenter(x + width / 2, y + height / 2)

        color = Color(125, 0, 200)
        wormhole3.setSize(width * 2.5f, width *  2.5f)
        wormhole3.setAdditiveBlend()
        wormhole3.alphaMult = 0.20f
        wormhole3.color = color
        wormhole3.renderAtCenter(x + width / 2, y + height / 2)

        if (level > 0 && level < 1) biome.endStencil()

    }


}