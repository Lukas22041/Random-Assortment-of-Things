package assortment_of_things.relics.interactions

import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.loading.Description

class RefurbishmentStationInteraction : RATInteractionPlugin() {

    override fun init() {

        textPanel.addPara("Your fleet approaches the assembly station.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        addLeaveOption()
    }

}