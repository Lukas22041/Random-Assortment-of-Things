package assortment_of_things.campaign.interactions

import assortment_of_things.misc.RATInteractionPlugin

class ChiralNebulaInteraction : RATInteractionPlugin()
{
    override fun init() {

        textPanel.addPara("After passing through the gate, you eyes see a strange world. Sensors show a system almost identical to that which you just came from, but everything seems strange.\n\n" +
                "" +
                "Close to the gate the fleet discovers a Nebula-Hull, or atleast what seems left of it. Due to the sad state of the ship, its unlikely we could recover any logs from it, but it is almost safe to asume " +
                "it's the remains of the Tri-Tachyon crew that send their expedition here.")

        addLeaveOption()
    }
}