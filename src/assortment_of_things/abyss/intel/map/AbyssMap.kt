package assortment_of_things.abyss.intel.map

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssDepth
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.StarSystemAPI
import com.fs.starfarer.api.impl.campaign.intel.BaseIntelPlugin
import com.fs.starfarer.api.ui.CustomPanelAPI
import com.fs.starfarer.api.ui.SectorMapAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI.TooltipCreator
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaExtensions.addLunaSpriteElement
import lunalib.lunaUI.elements.LunaElement
import lunalib.lunaUI.elements.LunaSpriteElement
import org.lazywizard.lazylib.ext.plus
import org.lwjgl.util.vector.Vector2f

class AbyssMap : BaseIntelPlugin() {


    override fun getName(): String {
        return "Abyss Map"
    }

    override fun hasLargeDescription(): Boolean {
        return true
    }

    override fun hasSmallDescription(): Boolean {
        return true
    }

    override fun createSmallDescription(info: TooltipMakerAPI?, width: Float, height: Float) {

    }

    override fun createLargeDescription(panel: CustomPanelAPI?, width: Float, height: Float) {
       // super.createLargeDescription(panel, width, height)

        var plugin = MoveableMapPanel()
        var moveablePanel = panel!!.createCustomPanel(width, height, plugin)
        panel.addComponent(moveablePanel)

        var range = 350f
        var element = moveablePanel.createUIElement(width, height, true)
        plugin.component = MoveableMapPanel.MoveableMapElement(element, Vector2f(-350f, 200f), 0f, 0f, range)

        var innerPanel = Global.getSettings().createCustom(width, height, null)
        element.addCustom(innerPanel, 0f)
        var innerElement = innerPanel.createUIElement(width, height, false)
        innerPanel.addUIElement(innerElement)

        addIconsToMap(innerPanel, innerElement, width, height)

        moveablePanel.addUIElement(element)
        element.position.inTL(350f, 200f)

    }

    fun addIconsToMap(mapPanel: CustomPanelAPI, mapElement: TooltipMakerAPI, width: Float, height: Float) {

        var systemsData = AbyssUtils.getAbyssData().systemsData

        var positions = HashMap<StarSystemAPI, Vector2f>()

        var icons = ArrayList<LunaElement>()
        for (systemData in systemsData)
        {
            var system = systemData.system

            if (!system.isEnteredByPlayer)
            {
                if (Global.getSector().playerFleet.containingLocation != system)
                {
                    continue
                }
            }

            var loc = systemData.mapLocation
            if (loc == null) continue

            var locOnMap = Vector2f().plus(loc)



            var icon = AbyssZoneMapIcon(system, AbyssUtils.ABYSS_COLOR, mapElement, 30f, 30f).apply {
                position.inTL(locOnMap.x, locOnMap.y)
            }
            icons.add(icon)


            var unexplored = 0
            var neighbours = systemData.neighbours
            for (sys in neighbours)
            {
                if (!sys.isEnteredByPlayer) unexplored++
            }

            var unidentified = system.customEntities.filter { it.customEntitySpec.id == "rat_abyss_icon" }.count()

            mapElement.addTooltipToPrevious( object: TooltipCreator {
                override fun isTooltipExpandable(tooltipParam: Any?): Boolean {
                    return false
                }

                override fun getTooltipWidth(tooltipParam: Any?): Float {
                    return 350f
                }

                override fun createTooltip(tooltip: TooltipMakerAPI?, expanded: Boolean, tooltipParam: Any?) {
                    tooltip!!.addPara("${system.baseName}", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "${system.baseName}")
                    tooltip.addSpacer(5f)

                    var depth = systemData.depth
                    if (system.name.contains("Twilight"))
                    {
                        tooltip!!.addPara("The exotic connection between hyperspace and this unique enviroment.", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "")
                        tooltip.addSpacer(5f)
                    }

                    else if (depth == AbyssDepth.Shallow)
                    {
                        tooltip!!.addPara("A zone within the abyss that is relatively close towards the entrance, the abyssal matter isnt very dense.", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "")
                        tooltip.addSpacer(5f)
                    }
                    else if (depth == AbyssDepth.Deep)
                    {
                        tooltip!!.addPara("A zone deep in to the abyss. The exotic matter is dense enough to inhibit the functionality of most of a fleets sensors dramaticly.", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "")
                        tooltip.addSpacer(5f)
                    }

                    if (unexplored != 0)
                    {
                        var prefix = "are"
                        var plural = "s"
                        if (unexplored == 1) prefix = "is"
                        if (unexplored == 1) plural = ""
                        tooltip!!.addPara("There $prefix $unexplored unexplored path$plural connected to this location.", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "")
                        tooltip.addSpacer(2f)
                    }

                    if (unidentified != 0)
                    {
                        var prefix = "are"
                        var plural = "s"
                        if (unidentified == 1) prefix = "is"
                        if (unidentified == 1) plural = ""
                        tooltip!!.addPara("There $prefix atleast $unidentified unidentified structure$plural in this zone.", 0f, Misc.getTextColor(), AbyssUtils.ABYSS_COLOR, "")
                        tooltip.addSpacer(2f)
                    }

                    if (Global.getSettings().isDevMode)
                    {
                        tooltip!!.addPara("[DevMode] Click to teleport towards system", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor(), "")
                        tooltip.addSpacer(2f)
                    }
                }

            }, TooltipMakerAPI.TooltipLocation.BELOW)

            positions.put(system, Vector2f(icon.position.x, icon.position.y))
            // para.position.inTL(locOnMap.x, locOnMap.y)
        }


        var line = MapLine(mapPanel, positions, mapElement, 0f, 0f)
        line.position.inTL(0f, 0f)
        for (icon in icons)
        {
            mapElement.bringComponentToTop(icon.elementPanel)
        }
    }

    override fun getIntelTags(map: SectorMapAPI?): MutableSet<String> {
        return mutableSetOf("Abyssal Depths")
    }

    override fun getIcon(): String {
        Global.getSettings().loadTexture("graphics/icons/intel/rat_map_icon.png")
        return "graphics/icons/intel/rat_map_icon.png"
    }

}