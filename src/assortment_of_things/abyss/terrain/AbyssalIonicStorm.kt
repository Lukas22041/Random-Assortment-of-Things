package assortment_of_things.abyss.terrain

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.entities.AbyssalLightsource
import assortment_of_things.abyss.entities.AbyssalPhotosphere
import assortment_of_things.abyss.procgen.AbyssDepth
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.CampaignFleetAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.impl.campaign.terrain.BaseTerrain
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.IntervalUtil
import com.fs.starfarer.api.util.Misc
import org.lazywizard.lazylib.MathUtils
import org.lwjgl.util.vector.Vector2f
import java.awt.Color
import java.util.*

class AbyssalIonicStorm : BaseTerrain() {

    var id = Misc.genUID()
    var supplyModID = "rat_ionic_storm_supply"

    override fun getActiveLayers(): EnumSet<CampaignEngineLayers> {
        return EnumSet.of(CampaignEngineLayers.ABOVE)
    }

    override fun getRenderRange(): Float {
        return 100000f
    }

    override fun getEffectCategory(): String {
        return "rat_ionincstorm"
    }

    override fun getModId(): String {
        return super.getModId() + id
    }

    override fun getTerrainId(): String {
        return super.getTerrainId() + id
    }

    override fun containsPoint(point: Vector2f?, radius: Float): Boolean {
        return true
    }

    override fun containsEntity(other: SectorEntityToken?): Boolean {
        return entity.starSystem == other!!.starSystem
    }

    override fun applyEffect(entity: SectorEntityToken?, days: Float) {

    }

    override fun getTerrainName(): String {
        var system = entity.starSystem
        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        return "Ionic Storm"
    }

    override fun hasTooltip(): Boolean {
        return true
    }

    override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean) {
        super.createTooltip(tooltip, expanded)

        var system = entity.starSystem
        var data = AbyssUtils.getSystemData(system)
        var depth = data.depth

        tooltip!!.addTitle(terrainName)
        tooltip.addSpacer(5f)

        tooltip!!.addPara("The entire enviroment is covered in an extreme flow of charged particles that violently collide with anything around them. " +
                "Interferes with shields, decreasing their efficiency by 15%%. Also overwhelms the emp resistance of fluxgrids, increasing emp damage taken by 20%%." +
                "\n\n" +
                "The particles cause energy weapon projectiles to become overcharged, dealing 10%% more damage." +
                "", 0f, Misc.getTextColor(), Misc.getHighlightColor(), "15%", "20%", "10%")

    }
}