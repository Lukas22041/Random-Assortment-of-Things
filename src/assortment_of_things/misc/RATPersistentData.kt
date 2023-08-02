package assortment_of_things.misc

import org.lazywizard.lazylib.JSONUtils
import org.lazywizard.lazylib.JSONUtils.CommonDataJSONObject

object RATPersistentData {

    var data: CommonDataJSONObject? = null

    fun loadData() {
        data = JSONUtils.loadCommonJSON("rat_data")
    }

}