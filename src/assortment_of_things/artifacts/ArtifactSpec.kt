package assortment_of_things.artifacts

data class ArtifactSpec(
    var id: String,
    var name: String,
    var pluginPath: String,
    var dropWeight: Float,
    var dropGroup: String,
    var spritePath: String)