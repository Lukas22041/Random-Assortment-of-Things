package assortment_of_things.abyss.procgen

object AbyssFleetStrengthData {

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
    var TRANQUILITY_ALTERATION_CHANCE = 0.15f
    var TRANQUILITY_AI_CORE_CHANCE = 0f //AI Core chance is 0.3 + 0.1 for depth scaling, this is added on top of it.
    var TRANQUILITY_MIN_BASE_FP = 56f
    var TRANQUILITY_MAX_BASE_FP = 96f
    var TRANQUILITY_MIN_SCALED_FP = 8f
    var TRANQUILITY_MAX_SCALED_FP = 16f
    var TRANQUILITY_ZERO_SMODS_WEIGHT = 2f
    var TRANQUILITY_ONE_SMODS_WEIGHT = 1f
    var TRANQUILITY_TWO_SMODS_WEIGHT = 0f

    //Solitude
    //Maximum Possible is 192FP
    var SOLITUDE_ALTERATION_CHANCE = 0.3f
    var SOLITUDE_AI_CORE_CHANCE = 0.35f //AI Core chance is 0.3 + 0.1 for depth scaling, this is added on top of it.
    var SOLITUDE_MIN_BASE_FP = 64f
    var SOLITUDE_MAX_BASE_FP = 144f
    var SOLITUDE_MIN_SCALED_FP = 24f
    var SOLITUDE_MAX_SCALED_FP = 48f
    var SOLITUDE_ZERO_SMODS_WEIGHT = 0.75f
    var SOLITUDE_ONE_SMODS_WEIGHT = 1f
    var SOLITUDE_TWO_SMODS_WEIGHT = 0.5f

}