package assortment_of_things.abyss.interactions

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.misc.AbyssTags
import assortment_of_things.abyss.procgen.AbyssProcgen
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
import java.util.*
import kotlin.random.Random
import kotlin.random.asJavaRandom

class CacheInteraction : RATInteractionPlugin() {


    override fun init() {

        if (AbyssUtils.anyNearbyFleetsHostileAndAware())
        {
            textPanel.addPara("As there are currently hostile targets following the fleets steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet approaches a crate of cargo.")
        textPanel.addPara("It seems to have been located here untouched for ages. There may be some treasures yet to be taken in it.")


        createOption("Loot the crate") {
            clearOptions()
            var random = Random(interactionTarget.getSalvageSeed())
            var tier = AbyssUtils.getTier(interactionTarget.starSystem)

            var dropRandom = ArrayList<DropData>()
            var dropValue = ArrayList<DropData>()
            var drop = DropData()

            if (tier == AbyssProcgen.Tier.Low) {
                drop = DropData()
                drop.chances = 2
                drop.group = "abyss_cache_loot"
                dropRandom.add(drop)

                drop = DropData()
                drop.group = "basic"
                drop.value = 1000
                dropValue.add(drop)

            }
            else
            {
                drop = DropData()
                drop.chances = 2
                drop.group = "abyss_cache_loot"
                dropRandom.add(drop)

                drop = DropData()
                drop.group = "basic"
                drop.value = 3000
                dropValue.add(drop)
            }

            drop = DropData()
            drop.chances = 3
            drop.group = "weapons2"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "any_hullmod_medium"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "rare_tech_low"
            dropRandom.add(drop)

            var salvage = SalvageEntity.generateSalvage(random.asJavaRandom(), 1f, 1f, 1f, 1f, dropValue, dropRandom)

            visualPanel.showLoot("Loot", salvage, true) {
                closeDialog()

                interactionTarget.fadeAndExpire(3f)
                interactionTarget.isExpired = true
            }
        }




        addLeaveOption()

    }




}