package assortment_of_things.modular_weapons.data

class ModularStatInt(private var base: Int) {

    private var mults: MutableMap<String, Float> = HashMap()
    private var flats: MutableMap<String, Int> = HashMap()

    private var originalBase = base

    fun getValue(): Int
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

        if (base == Int.MAX_VALUE)
        {
            return Int.MAX_VALUE
        }

        return ((base + flat) * mult).toInt()
    }

    fun addMult(id: String, value: Float)
    {
        mults.set(id, value)
    }

    fun addFlat(id: String, value: Int)
    {
        flats.set(id, value)
    }

    fun changeBase(base: Int)
    {
        this.base = base
    }

    fun clear()
    {
        base = originalBase
        mults.clear()
        flats.clear()
    }

}