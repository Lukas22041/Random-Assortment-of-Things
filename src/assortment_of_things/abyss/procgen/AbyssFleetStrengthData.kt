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
    var SOLITUDE_MIN_BASE_FP = 88f
    var SOLITUDE_MAX_BASE_FP = 192f
    var SOLITUDE_ZERO_SMODS_WEIGHT = 0.75f
    var SOLITUDE_ONE_SMODS_WEIGHT = 1f
    var SOLITUDE_TWO_SMODS_WEIGHT = 0.5f

    //Serenity
    //Maximum Possible is 192FP
    var SERENITY_ALTERATION_CHANCE = 0.4f
    var SERENITY_AI_CORE_CHANCE = 0.4f //AI Core chance is 0.3 + 0.1 for depth scaling, this is added on top of it.
    var SERENITY_MIN_BASE_FP = 92f
    var SERENITY_MAX_BASE_FP = 192f
    var SERENITY_ZERO_SMODS_WEIGHT = 0.75f
    var SERENITY_ONE_SMODS_WEIGHT = 0.5f
    var SERENITY_TWO_SMODS_WEIGHT = 1f

    //Harmony
    //Maximum Possible is 192FP
    var HARMONY_ALTERATION_CHANCE = 0.4f
    var HARMONY_AI_CORE_CHANCE = 0.4f //AI Core chance is 0.3 + 0.1 for depth scaling, this is added on top of it.
    var HARMONY_MIN_BASE_FP = 104f
    var HARMONY_MAX_BASE_FP = 192f
    var HARMONY_ZERO_SMODS_WEIGHT = 0.75f
    var HARMONY_ONE_SMODS_WEIGHT = 0.5f
    var HARMONY_TWO_SMODS_WEIGHT = 1f


}