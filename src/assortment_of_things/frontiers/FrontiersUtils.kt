package assortment_of_things.frontiers

import assortment_of_things.frontiers.data.*
import assortment_of_things.frontiers.plugins.facilities.BaseSettlementFacility
import assortment_of_things.frontiers.plugins.modifiers.BaseSettlementModifier
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.PlanetAPI
import com.fs.starfarer.api.util.Misc
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.lazywizard.lazylib.MathUtils

object FrontiersUtils {

    var modifierSpecs = ArrayList<SettlementModifierSpec>()
    var facilitySpecs = ArrayList<SettlementFacilitySpec>()


    //Settlement


    fun isFrontiersActive() : Boolean {
        return Global.getSector().memoryWithoutUpdate.get("\$rat_frontiers_active") == true
    }

    fun setFrontiersActive() {
        return Global.getSector().memoryWithoutUpdate.set("\$rat_frontiers_active", true)
    }

    fun getFrontiersData() : FrontiersData {
        var data =  Global.getSector().memoryWithoutUpdate.get("\$rat_frontiers_data") as FrontiersData?
        if (data == null) {
            data = FrontiersData()
            Global.getSector().memoryWithoutUpdate.set("\$rat_frontiers_data", data)
        }
        return data
    }

    fun hasSettlement() = getFrontiersData().activeSettlement != null

    fun getSettlementData() : SettlementData {
        return getFrontiersData().activeSettlement!!
    }



    //Modifiers
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
    }

    fun getModifierPlugin(spec: SettlementModifierSpec) : BaseSettlementModifier {
        var plugin = Global.getSettings().scriptClassLoader.loadClass(spec.plugin).newInstance() as BaseSettlementModifier
        plugin.spec = spec
        return plugin
    }


    fun getModifierByID(id: String) : SettlementModifierSpec {
        return modifierSpecs.find { it.id == id }!!
    }

    fun getModifierPluginsByID(site: SiteData) : List<BaseSettlementModifier> {
        return site.modifierIDs.map { getModifierPlugin(getModifierByID(it)) }
    }

    fun getRessource(site: SiteData): BaseSettlementModifier? {
        return getModifierPluginsByID(site).find { it.isRessource() }
    }

    fun getRessource(data: SettlementData): BaseSettlementModifier? {
        var spec = data.modifiers.map { id -> FrontiersUtils.getModifierByID(id) }.find { it.isResource } ?: return null
        return FrontiersUtils.getModifierPlugin(spec)
    }




    //Facilities
    fun loadFacilitiesFromCSV()
    {
        var CSV = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/campaign/frontiers/rat_frontiers_facilities.csv", "assortment_of_things")

        for (index in 0 until  CSV.length())
        {
            val row = CSV.getJSONObject(index)

            val id = row.getString("id")
            if (id.startsWith("#") || id == "") continue
            val name = row.getString("name")
            val short = row.getString("shortDesc")
            val descendDesc = row.getString("descendDesc")

            val icon = row.getString("icon")
            val cost = row.getString("cost").toFloat()

            Global.getSettings().loadTexture(icon)

            val pluginPath = row.getString("plugin")

            var spec = SettlementFacilitySpec(id, name, short, descendDesc, icon, cost, pluginPath)
            facilitySpecs.add(spec)
        }
    }

    fun getFacilityPlugin(spec: SettlementFacilitySpec) : BaseSettlementFacility {
        var plugin = Global.getSettings().scriptClassLoader.loadClass(spec.plugin).newInstance() as BaseSettlementFacility
        plugin.spec = spec
        return plugin
    }


    fun getFacilityByID(id: String) : SettlementFacilitySpec {
        return facilitySpecs.find { it.id == id }!!
    }

    fun getFacilityPluginsByID(list: List<String>) : List<BaseSettlementFacility> {


        var multiplyBySelf: (Int) -> Unit = {
            it * it
        }

        multiplyBySelf(5)

        return list.map { getFacilityPlugin(getFacilityByID(it)) }
    }

    fun getAllFacilityPlugins() : List<BaseSettlementFacility> {
        return facilitySpecs.map { getFacilityPlugin(it) }
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

        var modifierPlugins = modifierSpecs.map { getModifierPlugin(it) }
        modifierPlugins = modifierPlugins.filter { mod -> planet.market.conditions.any { mod.includeForCondition(it.id)  } }

        var ressources = WeightedRandomPicker<BaseSettlementModifier>()
        var extraRessources = WeightedRandomPicker<BaseSettlementModifier>()
        var maxExtraResources = MathUtils.getRandomNumberInRange(1, 3)

        for (mod in modifierPlugins) {
            if (mod.isRessource()) {
                ressources.add(mod)
                extraRessources.add(mod, mod.getChance())
            }
        }


        for (i in 0 until 19) {
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