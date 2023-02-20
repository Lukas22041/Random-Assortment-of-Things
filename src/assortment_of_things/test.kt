package assortment_of_things

import com.fs.starfarer.api.campaign.impl.items.BaseSpecialItemPlugin

class test : BaseSpecialItemPlugin()
{
    override fun performRightClickAction() {
        super.performRightClickAction()
//
        stack.specialDataIfSpecial.data
    }


}