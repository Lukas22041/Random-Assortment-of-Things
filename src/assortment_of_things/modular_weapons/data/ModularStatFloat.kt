package assortment_of_things.modular_weapons.data

import com.fs.starfarer.campaign.comms.F

class ModularStatFloat(var base: Float) {

    private var mults: MutableMap<String, Float> = HashMap()
    private var flats: MutableMap<String, Float> = HashMap()

    fun getValue(): Float
    {
        var flat = flats.values.sum()

        var mult = 1f
        for (m in mults)
        {
            mult *= m.value
        }

        if (mults.values.isEmpty() )
        {
            mult = 1f
        }

        return (base + flat) * mult
    }

    fun addMult(id: String, value: Float)
    {
        mults.set(id, value)
    }

    fun addFlat(id: String, value: Float)
    {
        flats.set(id, value)
    }

    fun clear()
    {
        mults.clear()
        flats.clear()
    }

}