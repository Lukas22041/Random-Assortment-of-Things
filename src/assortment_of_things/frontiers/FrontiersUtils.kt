package assortment_of_things.frontiers

import assortment_of_things.frontiers.data.FrontiersData
import assortment_of_things.frontiers.data.SettlementModifierSpec
import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.frontiers.data.SiteData
import assortment_of_things.frontiers.plugins.modifiers.BaseSettlementModifier
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils

object FrontiersUtils {

    var modifierSpecs = ArrayList<SettlementModifierSpec>()

    fun loadModifiersFromCSV()
    {
        var CSV = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/campaign/frontiers/rat_frontiers_modifers.csv", "assortment_of_things")

        for (index in 0 until  CSV.length())
        {
            val row = CSV.getJSONObject(index)

            val id = row.getString("id")
            if (id.startsWith("#") || id == "") continue
            val name = row.getString("name")
            val desc = row.getString("desc")
            val isResource = row.getBoolean("isResource")
            val canBeRefined = row.getBoolean("canBeRefined")

            val icon = row.getString("icon")
            val iconRefined = "${icon.replace(".png", "")}_refined.png"
            val iconSil= "${icon.replace(".png", "")}_sil.png"

            Global.getSettings().loadTexture(icon)
            if (isResource) {
                Global.getSettings().loadTexture(iconSil)
            }
            if (canBeRefined) {
                Global.getSettings().loadTexture(iconRefined)
            }

            val chance = row.getDouble("chance").toFloat()

            val conditions = row.getString("conditions").split(",").map { it.trim() }
            val income = row.getString("income").split(",").map { it.trim() }.map { it.toInt() }
            var combined = conditions.zip(income).toMap()

            val pluginPath = row.getString("plugin")

            var spec = SettlementModifierSpec(id, name, desc, isResource, canBeRefined, icon, iconRefined, iconSil, chance, combined, pluginPath)
            modifierSpecs.add(spec)
        }

        var plugins = modifierSpecs.map { getPlugin(it) }
        var text = ""
    }

    fun getPlugin(spec: SettlementModifierSpec) : BaseSettlementModifier {
        var plugin = Global.getSettings().scriptClassLoader.loadClass(spec.plugin).newInstance() as BaseSettlementModifier
        plugin.spec = spec
        return plugin
    }

    fun isFrontiersActive() : Boolean {
        return Global.getSector().memoryWithoutUpdate.get("\$rat_frontiers_active") == true
    }

    fun setFrontiersActive() {
        return Global.getSector().memoryWithoutUpdate.set("\$rat_frontiers_active", true)
    }

    fun getFrontiersData() : FrontiersData {
        return Global.getSector().memoryWithoutUpdate.get("\$rat_frontiers_data") as FrontiersData? ?: FrontiersData()
    }

    fun hasSettlement() = getFrontiersData().hasSettlement

    fun getSettlementData() : SettlementData {
        return getFrontiersData().activeSettlement!!
    }

    fun getModifierByID(id: String) : SettlementModifierSpec {
        return modifierSpecs.find { it.id == id }!!
    }

    fun getModifierPluginsByID(site: SiteData) : List<BaseSettlementModifier> {
        return site.modifierIDs.map { getPlugin(getModifierByID(it)) }
    }

    fun getRessource(site: SiteData): BaseSettlementModifier? {
        return getModifierPluginsByID(site).find { it.isRessource() }
    }

    fun getSiteData(planet: PlanetAPI) : List<SiteData> {
        var key = "\$rat_settlement_site_data"
        if (planet.memoryWithoutUpdate.get(key) == null) {
            planet.memoryWithoutUpdate.set(key, ArrayList<SiteData>())
        }

        return planet.memoryWithoutUpdate.get(key) as List<SiteData>
    }

    fun generateNewSites(planet: PlanetAPI) : List<SiteData> {
        var key = "\$rat_settlement_site_data"
        var sites = ArrayList<SiteData>()

        var modifierPlugins = modifierSpecs.map { getPlugin(it) }
        modifierPlugins = modifierPlugins.filter { mod -> planet.market.conditions.any { mod.includeForCondition(it.id)  } }

        var ressources = WeightedRandomPicker<BaseSettlementModifier>()
        var extraRessources = WeightedRandomPicker<BaseSettlementModifier>()
        var maxExtraResources = MathUtils.getRandomNumberInRange(1, 3)

        for (mod in modifierPlugins) {
            if (mod.isRessource()) {
                ressources.add(mod, mod.getChance())
                extraRessources.add(mod, mod.getChance())
            }
        }


        for (i in 0 until 20) {
            var site = SiteData()
            var modifiers = WeightedRandomPicker<BaseSettlementModifier>()

            for (mod in modifierPlugins) {
                if (!mod.isRessource()) {
                    modifiers.add(mod, mod.getChance())
                }
            }

            var addedRessource = false
            if (!ressources.isEmpty) {
                site.modifierIDs.add(ressources.pickAndRemove().getID())
                addedRessource = true
            }
            else if (maxExtraResources > 0) {
                site.modifierIDs.add(extraRessources.pick().getID())
                addedRessource = true
                maxExtraResources--
            }

            var minModifiers = 1
            var maxModifiers = 2
            if (!addedRessource) maxModifiers += 1

            for (e in minModifiers until maxModifiers) {
                if (!modifiers.isEmpty) {
                    site.modifierIDs.add(modifiers.pickAndRemove().getID())
                }
            }

            sites.add(site)
        }

        sites = sites.shuffled() as ArrayList<SiteData>
        planet.memoryWithoutUpdate.set(key, sites)
        return sites
    }

}