package assortment_of_things.misc

import com.fs.starfarer.api.Global
import org.apache.log4j.Level
import org.apache.log4j.Logger

fun Any.logger() : Logger {
    return Global.getLogger(this::class.java).apply { level = Level.ALL }
}