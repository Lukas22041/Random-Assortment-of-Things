package assortment_of_things.abyss.interactions.primordial

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.misc.*
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.campaign.FleetMemberPickerListener
import com.fs.starfarer.api.campaign.SpecialItemData
import com.fs.starfarer.api.combat.ShipAPI.HullSize
import com.fs.starfarer.api.fleet.FleetMemberAPI
import com.fs.starfarer.api.impl.campaign.ids.Sounds
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.loading.HullModSpecAPI
import com.fs.starfarer.api.ui.*
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaExtensions.addLunaSpriteElement
import lunalib.lunaUI.elements.LunaSpriteElement
import java.awt.Color

class PrimordialFabricatorInteraction : RATInteractionPlugin() {
    override fun init() {

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        var cargo = Global.getSector().playerFleet.cargo
        var count = cargo.getCommodityQuantity("rat_abyssal_matter").toInt()

        textPanel.addPara("Your fleet approaches the fabricator and teams are send in to inspect its interiors. " +
                "Vast production halls, hangars and development centers can be found in good condition across the structure, albeit all inactive.")

        textPanel.addPara("It appears most utilities within the station require addition abyssal matter to function.")

        var tooltip = textPanel.beginTooltip()

        tooltip.addSpacer(10f)

        var path = "graphics/icons/cargo/rat_abyssal_matter.png"
        Global.getSettings().getAndLoadSprite(path)
        var img = tooltip.beginImageWithText(path, 48f)
        var para = img.addPara("This fabricator can process Abyssal Matter to provide a variety of services. Abyssal Matter can only be found in limited quantities. You currently have $count units within your fleets cargo holds." , 0f,
            Misc.getTextColor(), Misc.getHighlightColor())

        para.setHighlight("Abyssal Matter", "$count")

        tooltip.addImageWithText(0f)

        //Update number mentioned in the tooltip.
        tooltip.addLunaElement(0f, 0f).apply {
            advance {
                count = cargo.getCommodityQuantity("rat_abyssal_matter").toInt()
                para.text = "This fabricator can process Abyssal Matter to provide a variety of services. Abyssal Matter can only be found in very limited quantities. You currently have $count within your fleets cargo holds."
                para.setHighlight("Abyssal Matter", "$count")
            }
        }

        textPanel.addTooltip()



        recreate()




        addLeaveOption()
    }

    fun getAbyssalMatter() : Int {
        var cargo = Global.getSector().playerFleet.cargo
        return cargo.getCommodityQuantity("rat_abyssal_matter").toInt()
    }

    fun consumeAbyssalMatter(subtract: Int) {
        var cargo = Global.getSector().playerFleet.cargo
        cargo.removeCommodity("rat_abyssal_matter", subtract.toFloat())
        var quantity = cargo.getCommodityQuantity("rat_abyssal_matter").toInt()

        textPanel.setFontSmallInsignia()
        textPanel.addParagraph("Lost $subtract units of Abyssal Matter ($quantity remaining)", Misc.getNegativeHighlightColor())
        textPanel.highlightInLastPara(Misc.getHighlightColor(), "$subtract", "$quantity")
        textPanel.setFontInsignia()
    }

    var selected: LongIconedButton? = null

    var width = 480f
    var height = 600f
    fun recreate() {

        var parentPanel = visualPanel.showCustomPanel(width, height, null)
        //panel.position.inTL(panel.position.x+10, panel.position.y+40)

        var panel = parentPanel.createCustomPanel(width, height, null)
        parentPanel.addComponent(panel)
        panel.position.inTL(20f, 0f)

        var tabberElement = panel.createUIElement(width, height, false)
        panel.addUIElement(tabberElement)

        var button1Icon = Global.getSettings().getAndLoadSprite("graphics/ui/rat_fabricator_refit.png")
        var tab1 = LongIconedButton(button1Icon, "Integrate Grid", Misc.getDarkPlayerColor(), tabberElement, 150f, 32f).apply {

            backgroundAlpha = 0.5f
            borderAlpha = 0.5f

            if (selected == null) {
                selected = this
            }

            if (selected?.text == this.text) {
                backgroundAlpha = 1f
                borderAlpha = 1f
            }

            onClick {
                selected = this
                playClickSound()
                recreate()
            }
        }

        var button2Icon = Global.getSettings().getAndLoadSprite("graphics/ui/rat_fabricator_refit.png")
        var tab2 = LongIconedButton(button2Icon, "Alterations", Misc.getDarkPlayerColor(), tabberElement, 150f, 32f).apply {

            backgroundAlpha = 0.5f
            borderAlpha = 0.5f

            if (selected?.text == this.text) {
                backgroundAlpha = 1f
                borderAlpha = 1f
            }

            onClick {
                selected = this
                playClickSound()
                recreate()
            }
        }
        tab2.elementPanel.position.rightOfTop(tab1.elementPanel, 10f)

        var button3Icon = Global.getSettings().getAndLoadSprite("graphics/ui/rat_fabricator_refit.png")
        var tab3 = LongIconedButton(button3Icon, "Artifacts", Misc.getDarkPlayerColor(), tabberElement, 150f, 32f).apply {

            backgroundAlpha = 0.5f
            borderAlpha = 0.5f

            if (selected?.text == this.text) {
                backgroundAlpha = 1f
                borderAlpha = 1f
            }

            onClick {
                selected = this
                playClickSound()
                recreate()
            }
        }
        tab3.elementPanel.position.rightOfTop(tab2.elementPanel, 10f)


        if (selected?.text == "Integrate Grid") {
            integrateGrid(panel)
        }
        else if (selected?.text == "Alterations") {
            fabricateAlterations(panel)
        }
        else if (selected?.text == "Artifacts") {
            fabricateArtifacts(panel)
        }

    }

    fun integrateGrid(parent: CustomPanelAPI) {



        var subpanel = parent.createCustomPanel(width, height, null)
        parent.addComponent(subpanel)

        subpanel.position.inTL(0f, 42f)

        var subelement = subpanel.createUIElement(width, height, false)
        subpanel.addUIElement(subelement)

        var header = subelement.addSectionHeading("Integrate Grid", Alignment.MID, 0f)
        subelement.addSpacer(10f)

        subelement.addPara("Your team approaches the stations hangar. The docks more closely resemble those of a shipyard than a spaceport. Mechanical arms with advanced multi-tools can be found all around.")
        subelement.addSpacer(10f)

        subelement.addPara("This hangar enables the modification of existing ships to integrate the Abyssal Grid hullmod. " +
                "This process requires 40/50/75/100 units of Abyssal Matter, based on the hullsize of the selected ship.", 0f,
            Misc.getTextColor(), Misc.getHighlightColor(), "Abyssal Grid","40","50","75", "100", "Abyssal Matter" )

/*
        var list = ArrayList<FleetMemberAPI>()
        list.add(Global.getSector().playerFleet.fleetData.membersListCopy.first())
        subelement.addShipList(1, 1, 64f, Misc.getBasePlayerColor(), list, 0f)*/

        subelement.addSpacer(10f)

        //subelement.addPara("test")

        var containerHeight = 155f
        var container = subelement.addLunaElement(width-10f, containerHeight).apply {
            enableTransparency = true
            borderAlpha = 0.9f
            backgroundAlpha = 0.2f
            backgroundColor = Color(0, 0, 0)

            innerElement.position.setXAlignOffset(5f)
            innerElement.position.setYAlignOffset(-6f)
            innerElement.addSpacer(10f)
            innerElement.addTitle("Abyssal Grid")
            innerElement.addSpacer(5f)
            innerElement.addPara("Design Type: Abyssal", 0f, Misc.getGrayColor(), AbyssUtils.ABYSS_COLOR, "Abyssal")
            innerElement.addSpacer(5f)

            innerElement.addPara("This ships flux grid is highly optimised for the use of energy weapons. " +
                    "This allows them to operate at a 10%% lower flux cost than normal and increases their base range by 100 units. " +
                    "\n\n" +
                    "This unique grid can also absorb the impact of charged particles, enabling it to resist emp damage up to 25%% better than other hulls and provides an immunity against abyssal storms and similar hazards." +
                    "",
                0f, Misc.getTextColor(), Misc.getHighlightColor(),
                "energy weapons", "10%", "100", "emp", "25%", "abyssal storms")




        }
        container.position.setXAlignOffset(0f)

     /*   subelement.addSpacer(10f)
        subelement.addSectionHeading("Modify", Alignment.MID, 0f)*/
        subelement.addSpacer(10f)

        var modify = LongIconedButton(Global.getSettings().getAndLoadSprite("graphics/ui/rat_fabricator_refit.png"), "Select a ship to modify", Misc.getDarkPlayerColor(), subelement, width-10f, 32f).apply {
            borderAlpha = 0.6f
            backgroundAlpha = 0.6f

            onHoverEnter {
                playScrollSound()
                borderAlpha = 0.9f
                backgroundAlpha = 0.9f
            }
            onHoverExit {
                borderAlpha = 0.6f
                backgroundAlpha = 0.6f
            }
        }
        subelement.addTooltip(modify.elementPanel, TooltipMakerAPI.TooltipLocation.BELOW, width-30) { tooltip ->
            tooltip.addPara("Select a ship to install this modification in to. Only ships that you can afford to modify with the amount of abyssal matter in your cargo holds will be displayed.")
        }

        modify.onClick {
            modify.playClickSound()

            var costs = HashMap<HullSize, Int>()
            costs.put(HullSize.FRIGATE, 40)
            costs.put(HullSize.DESTROYER, 50)
            costs.put(HullSize.CRUISER, 75)
            costs.put(HullSize.CAPITAL_SHIP, 100)

            var picks = Global.getSector().playerFleet.fleetData.membersListCopy
            picks = picks.filter { getAbyssalMatter() > costs.get(it.hullSpec.hullSize)!!  }
            picks = picks.filter { !it.variant.hasHullMod("rat_abyssal_grid") }
            picks = picks.filter { !it.variant.hasHullMod("rat_abyssal_threat") }
            picks = picks.filter { !it.variant.hasHullMod("rat_genesis_hullmod") }
            picks = picks.filter { !it.variant.hasHullMod("rat_genesis_serpent_hullmod") }

            dialog.showFleetMemberPickerDialog("Install the \"Abyssal Grid\" in to a ship", "Confirm", "Cancel", 5, 10, 64f,
            true, false, picks, object : FleetMemberPickerListener {
                override fun pickedFleetMembers(members: MutableList<FleetMemberAPI>) {
                    var pick = members.firstOrNull() ?: return
                    var cost = costs.get(pick.hullSpec.hullSize)

                    consumeAbyssalMatter(cost!!)

                    textPanel.setFontSmallInsignia()
                    textPanel.addParagraph("Installed the Abyssal Grid in to the ${pick.shipName}", Misc.getPositiveHighlightColor())
                    textPanel.setFontInsignia()

                    pick.fixVariant()
                    pick.variant.addPermaMod("rat_abyssal_grid")

                    Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)
                }

                override fun cancelledFleetMemberPicking() {

                }

            })

        }

    }

    fun getAlterationCosts() : HashMap<HullModSpecAPI, Int> {
        var map = HashMap<HullModSpecAPI, Int>()
        var specs = Global.getSettings().allHullModSpecs

        for (hullmod in specs) {
            for (tag in hullmod.tags) {
                if (tag.contains("rat_fabricatable")) {
                    var cost = tag.trim().replace("rat_fabricatable_", "").toInt()
                    map.put(hullmod, cost)
                }
            }
        }

        return map
    }

    fun fabricateAlterations(parent: CustomPanelAPI) {

        var subpanel = parent.createCustomPanel(width, height, null)
        parent.addComponent(subpanel)

        subpanel.position.inTL(0f, 42f)

        var subelement = subpanel.createUIElement(width, height, false)
        subpanel.addUIElement(subelement)

        var header = subelement.addSectionHeading("Fabricate Alterations", Alignment.MID, 0f)
        subelement.addSpacer(10f)

        subelement.addPara("Your team enters the control center of the stations production halls. After hooking up to the facilities interface, a long list of produceable modification kits appear on your screen. ")
        subelement.addSpacer(10f)

        subelement.addSectionHeading("Selection", Alignment.MID, 0f)

        subelement.addSpacer(5f)
        var h1 = subelement.addSectionHeading("Alteration", Alignment.MID, 0f)
        var h2 = subelement.addSectionHeading("Cost", Alignment.MID, 0f)
        var h3 = subelement.addSectionHeading("Fabricate", Alignment.MID, 0f)

        h1.position.setSize(280f, 20f)
        h2.position.setSize(90f, 20f)
        h3.position.setSize(100f, 20f)

        h2.position.rightOfTop(h1 as UIComponentAPI, 5f)
        h3.position.rightOfTop(h2 as UIComponentAPI, 5f)

        var alterationCosts = getAlterationCosts()

        var scrollerPanel = subpanel.createCustomPanel(width, 400f, null)
        subpanel.addComponent(scrollerPanel)
        scrollerPanel.position.inTL(0f, 130f)

        var scroller = scrollerPanel.createUIElement(width, 400f, true)
        scroller.addSpacer(10f)

        var list = alterationCosts.toList().sortedWith(compareByDescending<Pair<HullModSpecAPI, Int>>({it.second}).thenBy { it.first.displayName })

        for ((hullmod, cost) in list) {

            var container = scroller.addLunaElement(width, 50f).apply {
                enableTransparency = true
                borderAlpha = 0.0f
                backgroundAlpha = 0.0f
            }

            var designType = hullmod.manufacturer
            var col = Misc.getDesignTypeColor(designType)

            scroller.addTooltip(container.elementPanel, TooltipMakerAPI.TooltipLocation.BELOW, width) { tooltip ->
                tooltip.addTitle("${hullmod.displayName}")
                tooltip.addSpacer(10f)
                tooltip.addPara("Design type: $designType", 0f, Misc.getGrayColor(), col, designType)
                tooltip.addSpacer(10f)

                tooltip.addPara("A hull alteration is a special, consumable hull modification that can be installed through the \"Additional Options\" section at the top of the refit screen.",
                    0f, Misc.getGrayColor(), Misc.getHighlightColor(), "\"Additional Options\"")
                tooltip.addSpacer(10f)

                hullmod.effect.addPostDescriptionSection(tooltip, HullSize.FRIGATE, null, tooltip.widthSoFar, true)
            }

            var inner = container.innerElement

            var icon = inner.addLunaSpriteElement(hullmod.spriteName, LunaSpriteElement.ScalingTypes.STRETCH_SPRITE, 32f, 32f).apply {

                enableTransparency = true
                renderBorder = true
                borderAlpha = 0.9f
            }
            icon.position.setXAlignOffset(10f)
            icon.position.setYAlignOffset(-9f)

            var name = inner.addPara("${hullmod.displayName}", 0f, Misc.getBasePlayerColor(), Misc.getBasePlayerColor())
            //name.position.rightOfMid(icon.elementPanel, 10f)
            name.position.inTL(165f - name.computeTextWidth(name.text) / 2, 25f - name.computeTextHeight(name.text) / 2)



            inner.setParaFont(Fonts.ORBITRON_20AABOLD)
            var costPara = inner.addPara("$cost", 0f, Misc.getHighlightColor(), Misc.getHighlightColor())
            costPara.position.inTL(325f - costPara.computeTextWidth(costPara.text) / 2, 25f - costPara.computeTextHeight(costPara.text) / 2)

            inner.setParaSmallInsignia()

            var checkmark = CheckmarkButton(Global.getSettings().getAndLoadSprite("graphics/ui/rat_fabricator_refit.png"), Misc.getBasePlayerColor(), inner, 32f, 32f).apply {

                borderAlpha = 0.8f
                backgroundAlpha = 0.8f

                onHoverEnter {
                    playScrollSound()
                    borderAlpha = 1f
                    backgroundAlpha = 1f
                    extraAlpha = 0.3f
                }
                onHoverExit {
                    borderAlpha = 0.8f
                    backgroundAlpha = 0.8f
                    extraAlpha = 0f
                }

                onClick {
                    if (getAbyssalMatter() < cost) {
                        playSound("ui_char_can_not_increase_skill_or_aptitude", 1f, 1f)
                        return@onClick
                    }

                    Global.getSoundPlayer().playUISound(Sounds.STORY_POINT_SPEND, 1f, 1f)
                    consumeAbyssalMatter(cost)

                    textPanel.setFontSmallInsignia()
                    textPanel.addParagraph("Acquirred the \"${hullmod.displayName}\" Alteration", Misc.getPositiveHighlightColor())
                    textPanel.setFontInsignia()

                    Global.getSector().playerFleet.cargo.addSpecial(SpecialItemData("rat_alteration_install", hullmod.id), 1f)
                }
            }
            checkmark.position.inTL(425f - checkmark.width / 2, 25f - checkmark.height / 2)



            scroller.addLunaElement(0f, 0f).apply {
                advance {
                    if (getAbyssalMatter() < cost) {
                        costPara.color = Misc.getNegativeHighlightColor()
                    } else {
                        costPara.color = Misc.getHighlightColor()
                    }
                }
            }



            scroller.addSpacer(10f)


        }





        scrollerPanel.addUIElement(scroller)

    }

    fun fabricateArtifacts(parent: CustomPanelAPI) {

    }


}