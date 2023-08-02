package assortment_of_things.misc

import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SettingsAPI
import com.fs.starfarer.api.combat.MutableStat
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.combat.StatBonus
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.ui.LabelAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.loading.specs.HullVariantSpec
import org.apache.log4j.Level
import org.apache.log4j.Logger

fun Any.logger() : Logger {
    return Global.getLogger(this::class.java).apply { level = Level.ALL }
}

fun SettingsAPI.getAndLoadSprite(filename: String) : SpriteAPI{
    this.loadTexture(filename)
    return this.getSprite(filename)
}

fun TooltipMakerAPI.addPara(str: String) = this.addPara(str, 0f)

fun TooltipMakerAPI.addNegativePara(str: String) = this.addPara(str, 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

fun FleetMemberAPI.fixVariant() {
    if (this.variant.source != VariantSource.REFIT)
    {
        var variant = this.variant.clone();
        variant.originalVariant = null;
        variant.hullVariantId = Misc.genUID()
        variant.source = VariantSource.REFIT
        this.setVariant(variant, false, true)
    }
    this.updateStats()
}

fun ShipVariantAPI.baseOrModSpec() : ShipHullSpecAPI{
    if (this.hullSpec.baseHull != null) {
        return this.hullSpec.baseHull
    }
    return this.hullSpec
}

fun FleetMemberAPI.baseOrModSpec() : ShipHullSpecAPI{
    if (this.hullSpec.baseHull != null) {
        return this.hullSpec.baseHull
    }
    return this.hullSpec
}

fun ShipAPI.baseOrModSpec() : ShipHullSpecAPI{
    if (this.hullSpec.baseHull != null) {
        return this.hullSpec.baseHull
    }
    return this.hullSpec
}