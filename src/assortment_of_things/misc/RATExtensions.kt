package assortment_of_things.misc

import assortment_of_things.abyss.AbyssData
import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.exotech.ExoData
import assortment_of_things.exotech.ExoUtils
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.SettingsAPI
import com.fs.starfarer.api.campaign.SectorAPI
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.combat.ShipHullSpecAPI
import com.fs.starfarer.api.combat.ShipVariantAPI
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.loading.VariantSource
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipLocation
import com.fs.starfarer.api.ui.UIComponentAPI
import com.fs.starfarer.api.util.Misc
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.lazywizard.lazylib.MathUtils

object LoadedAssets {
    var previouslyLoadedSprite = HashMap<String, Boolean>()

    //For Java Use
    @JvmStatic
    fun loadTextureCached(filename: String) {
        Global.getSettings().loadTextureCached(filename)
    }
}

fun Float.levelBetween(min: Float, max: Float) : Float {
    var level = (this - min) / (max - min)
    level = MathUtils.clamp(level, 0f, 1f)
    return level
}

fun Float.levelBetweenReversed(min: Float, max: Float) : Float {

    return 1 - this.levelBetween(min, max)
}

fun Any.logger() : Logger {
    return Global.getLogger(this::class.java).apply { level = Level.ALL }
}

fun SettingsAPI.loadTextureCached(filename: String) {
    if (!LoadedAssets.previouslyLoadedSprite.contains(filename)) {
        this.loadTexture(filename)
        LoadedAssets.previouslyLoadedSprite.put(filename, true)
    }
}

fun SettingsAPI.getAndLoadSprite(filename: String) : SpriteAPI{
    if (!LoadedAssets.previouslyLoadedSprite.contains(filename)) {
        this.loadTexture(filename)
        LoadedAssets.previouslyLoadedSprite.put(filename, true)
    }
    return this.getSprite(filename)
}



fun TooltipMakerAPI.addPara(str: String) = this.addPara(str, 0f)

fun TooltipMakerAPI.addNegativePara(str: String) = this.addPara(str, 0f, Misc.getNegativeHighlightColor(), Misc.getNegativeHighlightColor())

fun TooltipMakerAPI.addTooltip(to: UIComponentAPI, location: TooltipLocation, width: Float, lambda: (TooltipMakerAPI) -> Unit) {
    this.addTooltipTo(object: TooltipCreator {
        override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
            return false
        }

        override fun getTooltipWidth(tooltipParam: Any?): Float {
            return width
        }

        override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
            lambda(tooltip!!)
        }

    }, to, location)
}

fun FleetMemberAPI.fixVariant() {
    if (this.variant.source != VariantSource.REFIT)
    {
        var variant = this.variant.clone();

        //Setting the original variant causes weird things, so just give it a tag of the OG variant instead.
        if (variant.originalVariant != null) {
            variant.addTag("rat_og_variant_${variant.originalVariant}")
        }

        variant.originalVariant = null;
        variant.hullVariantId = Misc.genUID()
        variant.source = VariantSource.REFIT
        this.setVariant(variant, false, true)
    }
    this.updateStats()
}

fun ShipVariantAPI.getOriginalVariantRAT() : String? {
    var og = this.originalVariant
    if (og == null) {
        for (tag in this.tags) {
            if (tag.contains("rat_og_variant_")){
                return tag.replace("rat_og_variant_", "")
            }
        }
    }
    return og
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

fun SectorAPI.instantTeleport(destination: SectorEntityToken) {
    var playerFleet = Global.getSector().playerFleet
    var currentLocation = playerFleet.containingLocation

    currentLocation.removeEntity(playerFleet)
    destination.containingLocation.addEntity(playerFleet)
    Global.getSector().setCurrentLocation(destination.containingLocation)
    playerFleet.setLocation(destination.location.x, destination.location.y)
}

public fun <T> MutableCollection<T>.randomAndRemove(): T {
    val pick = this.random()
    this.remove(pick)
    return pick
}


fun SectorEntityToken.setLooted() {
    this.addTag("rat_looted")
}

fun SectorEntityToken.isLooted() : Boolean {
    return this.hasTag("rat_looted")
}

fun SectorAPI.getAbyssData() : AbyssData {
    return AbyssUtils.getData()
}

fun SectorAPI.getExoData() : ExoData {
    return ExoUtils.getExoData()
}