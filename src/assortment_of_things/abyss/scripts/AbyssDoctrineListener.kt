package assortment_of_things.abyss.scripts

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.intel.DoctrineReportAbyssal
import assortment_of_things.abyss.intel.DoctrineReportSeraph
import assortment_of_things.misc.DelayedCampaignCodeExecution
import assortment_of_things.misc.baseOrModSpec
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.*

class AbyssDoctrineListener(permaRegister: Boolean) : BaseCampaignEventListener(permaRegister) {



    override fun reportEncounterLootGenerated(plugin: FleetEncounterContextPlugin?, loot: CargoAPI?) {

        if (plugin == null) {
            return
        }

        if (!plugin.battle.isPlayerInvolved) return

        if (plugin.loser.faction.id == "rat_abyssals" || plugin.loser.faction.id == "rat_abyssals_deep") {
            var defeated = plugin!!.loserData.destroyedInLastEngagement + plugin.loserData.disabledInLastEngagement

            defeated = defeated.distinct()

            var defeatedAbyssals = defeated.filter { it.baseOrModSpec().hasTag("rat_abyssals") }.size
            var defeatedSeraphs = defeated.filter { it.baseOrModSpec().hasTag("rat_seraph") }.size

            var data = AbyssUtils.getAbyssData()
            data.abyssalsDestroyed += defeatedAbyssals
            data.seraphsDestroyed += defeatedSeraphs

            var intel = Global.getSector().intelManager
            if (data.abyssalsDestroyed >= 12 && !data.hasAbyssalDoctrine) {
                data.hasAbyssalDoctrine = true
                DelayedCampaignCodeExecution(1f) {
                    intel.addIntel(DoctrineReportAbyssal())
                    for (listener in data.doctrineLearnedListeners) {
                        listener.learnedAbyssalDoctrine()
                    }
                }
            }

            if (data.seraphsDestroyed >= 2 && !data.hasSeraphDoctrine) {
                data.hasSeraphDoctrine = true
                DelayedCampaignCodeExecution(1f) {
                    intel.addIntel(DoctrineReportSeraph())
                    for (listener in data.doctrineLearnedListeners) {
                        listener.learnedSeraphDoctrine()
                    }
                }
            }
        }
    }
}