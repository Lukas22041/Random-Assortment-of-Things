package assortment_of_things.abyss.entities

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.intel.AbyssWarningBeaconIntel
import assortment_of_things.strings.RATTags
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CampaignEngineLayers
import com.fs.starfarer.api.campaign.SectorEntityToken
import com.fs.starfarer.api.combat.ViewportAPI
import com.fs.starfarer.api.graphics.SpriteAPI
import com.fs.starfarer.api.impl.campaign.BaseCustomEntityPlugin
import com.fs.starfarer.api.impl.campaign.ids.Pings
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantThemeGenerator
import com.fs.starfarer.api.ui.TooltipMakerAPI
import java.awt.Color

class AbyssWarningBeacon : BaseCustomEntityPlugin() {

    var GLOW_COLOR_KEY = "\$core_beaconGlowColor"
    var PING_COLOR_KEY = "\$core_beaconPingColor"
    var PING_ID_KEY = "\$core_beaconPingId"
    var PING_FREQ_KEY = "\$core_beaconPingFreq"

    var GLOW_FREQUENCY = 1f

    @Transient
    private var sprite: SpriteAPI? = null

    @Transient
    private var glow: SpriteAPI? = null

    var addedIntel = false

    override fun init(entity: SectorEntityToken, pluginParams: Any?) {
        super.init(entity, pluginParams)
        //this.entity = entity;
        entity.detectionRangeDetailsOverrideMult = 0.75f
        readResolve()
    }

    fun readResolve(): Any {
        sprite = Global.getSettings().getSprite("campaignEntities", "warning_beacon")
        glow = Global.getSettings().getSprite("campaignEntities", "warning_beacon_glow")
        return this
    }

    private var phase = 0f
    private var freqMult = 1f
    private var sincePing = 10f
    override fun advance(amount: Float) {
        phase += amount * GLOW_FREQUENCY * freqMult
        while (phase > 1) phase--
        if (!entity.isDiscoverable && !addedIntel) {
            val intel = AbyssWarningBeaconIntel(entity, entity.faction)
            Global.getSector().intelManager.addIntel(intel)
            addedIntel = true
        }
        if (entity.isInCurrentLocation) {
            sincePing += amount
            if (sincePing >= 6f && phase > 0.1f && phase < 0.2f) {
                sincePing = 0f
                val playerFleet = Global.getSector().playerFleet
                if (playerFleet != null && entity.getVisibilityLevelTo(playerFleet) == SectorEntityToken.VisibilityLevel.COMPOSITION_AND_FACTION_DETAILS) {
                    var pingId: String? = Pings.WARNING_BEACON1
                    freqMult = 1f
                    if (entity.memoryWithoutUpdate.getBoolean(RemnantThemeGenerator.RemnantSystemType.SUPPRESSED.beaconFlag)) {
                        pingId = Pings.WARNING_BEACON2
                        freqMult = 1.25f
                    } else if (entity.memoryWithoutUpdate.getBoolean(RemnantThemeGenerator.RemnantSystemType.RESURGENT.beaconFlag)) {
                        pingId = Pings.WARNING_BEACON3
                        freqMult = 1.5f
                    }
                    if (entity.memoryWithoutUpdate.contains(PING_ID_KEY)) {
                        pingId = entity.memoryWithoutUpdate.getString(PING_ID_KEY)
                    }
                    if (entity.memoryWithoutUpdate.contains(PING_FREQ_KEY)) {
                        freqMult = entity.memoryWithoutUpdate.getFloat(PING_FREQ_KEY)
                    }

                    var pingColor = AbyssUtils.ABYSS_COLOR
                    Global.getSector().addPing(entity, pingId, pingColor)
                }
            }
        }
    }

    override fun getRenderRange(): Float {
        return entity.radius + 100f
    }

    override fun render(layer: CampaignEngineLayers?, viewport: ViewportAPI) {
        var alphaMult = viewport.alphaMult
        alphaMult *= entity.sensorFaderBrightness
        alphaMult *= entity.sensorContactFaderBrightness
        if (alphaMult <= 0f) return
        val spec = entity.customEntitySpec ?: return
        val w = spec.spriteWidth
        val h = spec.spriteHeight
        val loc = entity.location
        sprite!!.angle = entity.facing - 90f
        sprite!!.setSize(w, h)
        sprite!!.alphaMult = alphaMult
        sprite!!.setNormalBlend()
        sprite!!.renderAtCenter(loc.x, loc.y)
        var glowAlpha = 0f
        if (phase < 0.5f) glowAlpha = phase * 2f
        if (phase >= 0.5f) glowAlpha = 1f - (phase - 0.5f) * 2f
        val glowAngle1 = (phase * 1.3f % 1 - 0.5f) * 12f
        val glowAngle2 = (phase * 1.9f % 1 - 0.5f) * 12f

        var glowColor = AbyssUtils.ABYSS_COLOR
        if (entity.memoryWithoutUpdate.contains(GLOW_COLOR_KEY)) {
            glowColor = entity.memoryWithoutUpdate[GLOW_COLOR_KEY] as Color
        }

        glow!!.color = glowColor
        glow!!.setSize(w, h)
        glow!!.alphaMult = alphaMult * glowAlpha
        glow!!.setAdditiveBlend()
        glow!!.angle = entity.facing - 90f + glowAngle1
        glow!!.renderAtCenter(loc.x, loc.y)
        glow!!.angle = entity.facing - 90f + glowAngle2
        glow!!.alphaMult = alphaMult * glowAlpha * 0.5f
        glow!!.renderAtCenter(loc.x, loc.y)
    }

    override fun createMapTooltip(tooltip: TooltipMakerAPI, expanded: Boolean) {
        val color = entity.faction.baseUIColor
        var postColor = color
        tooltip.addPara(entity.name, 0f, color, postColor, "")
    }

    override fun hasCustomMapTooltip(): Boolean {
        return true
    }

    override fun appendToCampaignTooltip(tooltip: TooltipMakerAPI, level: SectorEntityToken.VisibilityLevel?) {

    }
}