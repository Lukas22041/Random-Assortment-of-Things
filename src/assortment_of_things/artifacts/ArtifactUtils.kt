package assortment_of_things.artifacts

import assortment_of_things.scripts.AtMarketListener
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CargoAPI
import com.fs.starfarer.api.campaign.CargoStackAPI
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.util.WeightedRandomPicker
import org.magiclib.kotlin.getStorageCargo
import java.util.Random

object ArtifactUtils {

    //var artifactsListKey = "\$rat_artifacts"
    var activeArtifactKey = "\$rat_active_artifact"
    var artifacts = ArrayList<ArtifactSpec>()

    var STAT_MOD_ID = "artifact_stat_mod"

    fun loadArtifactsFromCSV()
    {
        var CSV = Global.getSettings().getMergedSpreadsheetDataForMod("id", "data/campaign/rat_artifacts.csv", "assortment_of_things")

        for (index in 0 until  CSV.length())
        {
            val row = CSV.getJSONObject(index)

            val id = row.getString("id")
            if (id.startsWith("#") || id == "") continue
            val name = row.getString("name")
            val designType = row.getString("designType")
            val pluginPath = row.getString("plugin")
            var tags = row.getString("tags").split(",").map { it.trim() }
            val dropWeight = row.getDouble("dropWeight").toFloat()
            val dropGroup = row.getString("dropGroup")
            val spritePath = row.getString("spritePath")
            Global.getSettings().loadTexture(spritePath)

            var spec = ArtifactSpec(id, name, designType, pluginPath, tags, dropWeight, dropGroup, spritePath)
            artifacts.add(spec)
        }
    }

    fun getPlugin(spec: ArtifactSpec) = Global.getSettings().scriptClassLoader.loadClass(spec.pluginPath).newInstance() as BaseArtifactPlugin

    fun getActivePlugin(): BaseArtifactPlugin? {
        if (getActiveArtifact() == null) return null
        return getPlugin(getActiveArtifact()!!)
    }

   /* fun addArtifactToFleet(spec: ArtifactSpec) {
        var player = Global.getSector().playerFleet
        var memory = player.memoryWithoutUpdate

        if (!memory.contains(artifactsListKey))
        {
            memory.set(artifactsListKey, ArrayList<String>())
        }

        var artifactIDs = memory.get(artifactsListKey) as ArrayList<String>
        artifactIDs.add(spec.id)
        artifactIDs.distinct()
        memory.set(artifactsListKey, artifactIDs)
    }*/

    fun getArtifactsInCargo() : List<CargoStackAPI> {
        var artifactStacks = ArrayList<CargoStackAPI>()
        var cargo = Global.getSector()?.playerFleet?.cargo ?: return artifactStacks

        var stacks = cargo.stacksCopy

        var marketListener = Global.getSector().allListeners.find { it is AtMarketListener } as AtMarketListener
        if (marketListener.market != null) {
            var storage = marketListener.market?.getStorageCargo()
            if (storage != null) {
                stacks.addAll(storage.stacksCopy)
            }
        }

        for (stack in stacks) {
            if (stack.specialItemSpecIfSpecial?.id != "rat_artifact") continue
            artifactStacks.add(stack)
        }

        return artifactStacks
    }

    fun getArtifactsInFleet() : List<ArtifactSpec> {
        var list = ArrayList<ArtifactSpec>()
        var cargo = Global.getSector()?.playerFleet?.cargo ?: return list

        var stacks = getArtifactsInCargo()
        for (stack in stacks) {
            var data = stack.specialDataIfSpecial.data
            var spec = artifacts.find { it.id == data }
            list.add(spec!!)
        }

        return list
    }

    fun getActiveArtifact() : ArtifactSpec? {
        var player = Global.getSector()?.playerFleet ?: return null
        var memory = player.memoryWithoutUpdate
        var artifactID = memory.get(activeArtifactKey) as String?
        /*if (artifactID == null)
        {
            var spec = getArtifactsInFleet().first()
            memory.set(activeArtifactKey, spec.id)
            return spec
        }*/
        return artifacts.find { it.id == artifactID }
    }

    fun deactivateArtifact()
    {
        var player = Global.getSector().playerFleet
        var memory = player.memoryWithoutUpdate
        ArtifactUtils.getActivePlugin()?.onRemove(Global.getSector().playerFleet, ArtifactUtils.STAT_MOD_ID)
        memory.set(activeArtifactKey, null)
    }

    fun setActiveArtifact(spec: ArtifactSpec) {
        var player = Global.getSector().playerFleet
        var memory = player.memoryWithoutUpdate
        memory.set(activeArtifactKey, spec.id)
    }

   /* fun generateArtifactLoot(cargo: CargoAPI, dropGroup: String, chance: Float, attempts: Int, random: Random)
    {
        var picker = WeightedRandomPicker<ArtifactSpec>()
        picker.random = random

        for (artifact in artifacts)
        {
            if (artifact.dropGroup != dropGroup) continue
            picker.add(artifact, artifact.dropWeight)
        }

        for (i in 0 until attempts)
        {
            if (random.nextFloat() > chance) continue
            var pick = picker.pick()
            cargo.addSpecial(SpecialItemData("rat_artifact", pick.id), 1f)
        }
    }*/

    /*fun generateArtifactNoDupe(cargo: CargoAPI, dropGroup: String, random: Random)
    {

        var artifactsInFleet = ArtifactUtils.getArtifactsInFleet()

        var picker = WeightedRandomPicker<ArtifactSpec>()
        picker.random = random

        for (artifact in artifacts)
        {
            if (artifact.dropGroup != dropGroup) continue
            if (artifactsInFleet.any { it.id == artifact.id }) continue
            picker.add(artifact, artifact.dropWeight)
        }

        if (picker.isEmpty) {
            generateArtifactLoot(cargo, dropGroup, 1f, 1, random)
            return
        }

        var pick = picker.pick()
        cargo.addSpecial(SpecialItemData("rat_artifact", pick.id), 1f)
    }*/
}