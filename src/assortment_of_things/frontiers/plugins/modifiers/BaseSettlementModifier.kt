package assortment_of_things.frontiers.plugins.modifiers

import assortment_of_things.frontiers.data.SettlementData
import assortment_of_things.frontiers.data.SettlementModifierSpec

open class BaseSettlementModifier() {

    lateinit var spec: SettlementModifierSpec

    open fun getID() : String {
        return spec.id
    }

    open fun getName() : String {
        return spec.name
    }

    open fun getDescription(): String {
        return spec.desc
    }


    open fun isRessource() : Boolean {
        return spec.isResource
    }

    open fun canBeRefined() : Boolean {
        return spec.canBeRefined
    }


    open fun getIcon() : String {
        return spec.icon
    }

    open fun getRefinedIcon() : String {
        return spec.iconRefined
    }

    open fun includeForCondition(conditionID : String) : Boolean {
        return spec.conditions.contains(conditionID)
    }

    open fun getChance() : Float {
        return spec.chance
    }

    open fun apply(data: SettlementData) {

    }
}