package assortment_of_things.abyss.procgen

object AbyssFleetFPData {

    /*Remnant Fleet Points
    *
    * Remnant Medium Danger Seeded Points (Randomly spawn into the system)
    * Min: 32
    * Max: 96
    *
    * Remnant Medium Danger Station Points (Spawned From Station)
    * Min: 48
    * Max: 96
    *
    * Remnant High Danger Station Points (Spawned From Station)
    * Min: 64
    * Max: 192
    *
    * */

    //Lower sizes than Remnant High, but can be closer to it near the border. Maximum possible is be 128FP.
    //Tranquility has it scaling inverted, so systems closer to the border get more scaled FP.
    var TRANQUILITY_MIN_BASE_FP = 56f
    var TRANQUILITY_MAX_BASE_FP = 96f
    var TRANQUILITY_MIN_SCALED_FP = 16f
    var TRANQUILITY_MAX_SCALED_FP = 32f

}