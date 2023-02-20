package assortment_of_things.campaign.interactions

import assortment_of_things.campaign.intel.SpacersGambitIntel
import assortment_of_things.misc.RATInteractionPlugin
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.CoreUITabId
import com.fs.starfarer.api.impl.campaign.ids.Commodities
import com.fs.starfarer.api.impl.campaign.ids.Factions
import com.fs.starfarer.api.util.Misc
import org.lwjgl.input.Keyboard

class SpacersGambitInteraction : RATInteractionPlugin() {

    companion object {
        var isDiscovered: Boolean?
            set(value) { Global.getSector().memoryWithoutUpdate.set("\$playerDiscoveredGambit", value) }
            get() { return Global.getSector().memoryWithoutUpdate.getBoolean("\$playerDiscoveredGambit") }

        var initialCostPaid: Boolean?
            set(value) { Global.getSector().memoryWithoutUpdate.set("\$playerPaidGambit", value) }
            get() { return Global.getSector().memoryWithoutUpdate.getBoolean("\$playerPaidGambit") }

        var agreedCost: Int?
            set(value) { Global.getSector().memoryWithoutUpdate.set("\$agreedCostGambit", value) }
            get() { return Global.getSector().memoryWithoutUpdate.getInt("\$agreedCostGambit") }

    }

    override fun init() {
        if (isDiscovered == null || isDiscovered == false)
        {
            firstDiscovery()
        }
        else if (initialCostPaid == null || initialCostPaid == false)
        {
            textPanel.addPara("Welcome back, captain. To gain access to the market, you have to still pay the agreed payment of $agreedCost Credits.",
            Misc.getTextColor(), Misc.getHighlightColor(), "$agreedCost")
            unpaid()
        }
        else if (initialCostPaid == true)
        {
            mainPage()
        }
    }

    fun firstDiscovery()
    {
        isDiscovered = true
        agreedCost = 100000

        var player = Global.getSector().playerFaction
        var isPositive = player.getRelationshipLevel(Factions.PIRATES).isPositive

        if (isPositive) agreedCost = 50000

        var intel = SpacersGambitIntel(interactionTarget.market)
        Global.getSector().intelManager.addIntel(intel)
        interactionTarget.market.admin

        if (interactionTarget.market.admin == null)
        {
            textPanel.addPara("You close in on the sole planet in this fringe system. On arrival, you are greeted by a comm-request by the Administrator of the planet.")
        }
        else
        {
            textPanel.addPara("You close in on the sole planet in this fringe system. On arrival, you are greeted by a comm-request by the Administrator of the planet, ${interactionTarget.market.admin.nameString}."
            , Misc.getTextColor(), Misc.getHighlightColor(), " ${interactionTarget.market.admin.nameString}")
        }

        textPanel.addPara("\"Welcome to Spacers Gambit, the greatest black market in the sector!. Aslong as you pay the price, no matter your allegiance, you may find some rare commodities not commonly available anywhere else." +
                " However, you see, our policy requires interested captains to offer us an upfront payment to use our service. ")

        if (isPositive)
        {
            textPanel.addPara("The cost of that payment would normaly be be 100.000 Credits, but since you are a fellow friend of us pirates, lets agree to half the cost to 50.000 Credits.\""
                ,Misc.getTextColor(), Misc.getHighlightColor(), "100.000", "50.000")
        }
        else
        {
            textPanel.addPara("The cost of that payment will be 100.000 Credits, while it would be a shame, you are free to return at a later date to pay it.\""
                ,Misc.getTextColor(), Misc.getHighlightColor(), "100.000")

        }
        unpaid()
    }

    fun unpaid()
    {
        var enoughCredits = Global.getSector().playerFleet.cargo.credits.get() >= agreedCost!!

        createOption("Pay the upfront cost") {
            clearOptions()
            initialCostPaid = true

            Global.getSector().playerFleet.cargo.credits.subtract(agreedCost!!.toFloat())

            textPanel.addPara("\"Thanks for the business. You can find an up to date list of our available to sell cargo on your Tri-Pad. I hope you enjoy your stay at our market.\"")
            createOption("Continue") {
                clear()
                mainPage()
            }
        }
        if (!enoughCredits)
        {
            optionPanel.setEnabled("Pay the upfront cost", false)
            optionPanel.setTooltip("Pay the upfront cost", "You do not have enough credits.")
        }
        optionPanel
        addLeaveOption()
    }


    fun mainPage()
    {
        textPanel.addPara("You arrive at Spacers Gambit, there are crowds of wealthy looking people checking out wares.")

        createOption("Purchase from the Blackmarket") {
           dialog.visualPanel.showCore(CoreUITabId.CARGO, interactionTarget, null);
        }

        addLeaveOption()
    }
}