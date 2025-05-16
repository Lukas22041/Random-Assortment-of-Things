package assortment_of_things.abyss.interactions.tranquility

import assortment_of_things.abyss.AbyssUtils
import assortment_of_things.abyss.procgen.AbyssProcgenUtils
import assortment_of_things.misc.RATInteractionPlugin
import assortment_of_things.misc.addPara
import assortment_of_things.misc.getAndLoadSprite
import com.fs.starfarer.api.Global
import com.fs.starfarer.api.impl.campaign.procgen.SalvageEntityGenDataSpec.DropData
import com.fs.starfarer.api.impl.campaign.rulecmd.salvage.SalvageEntity
import com.fs.starfarer.api.loading.Description
import com.fs.starfarer.api.ui.TooltipMakerAPI
import com.fs.starfarer.api.util.Misc
import lunalib.lunaExtensions.addLunaElement
import lunalib.lunaUI.elements.LunaElement
import org.lwjgl.opengl.GL11
import org.magiclib.kotlin.fadeAndExpire
import org.magiclib.kotlin.getSalvageSeed
import java.awt.Color
import java.util.*
import kotlin.collections.ArrayList

class AbyssalShipyardInteraction : RATInteractionPlugin() {
    override fun init() {

        if (AbyssUtils.isAnyFleetTargetingPlayer())
        {
            textPanel.addPara("As there are currently hostile targets following the fleet's steps, safe docking at the station seems impossible.")
            addLeaveOption()
            return
        }

        textPanel.addPara("Your fleet approaches the abyssal shipyard.")

        textPanel.addPara(Global.getSettings().getDescription(interactionTarget.customDescriptionId, Description.Type.CUSTOM).text1)

        var defender = interactionTarget.memoryWithoutUpdate.get("\$defenderFleet")
        if (defender == null) {
            textPanel.addPara("Scans indicate that there is nothing of value remaining.")
            addLeaveOption()
            return
        }

        textPanel.addPara("From less distance, the stations gigantic size comes even more in to focus, casting a giant behind the side facing away from the photosphere.")

        createOption("Close in towards the station") {

            textPanel.addPara("On approach, multiple sensor readings appear on your fleets terminals. One reading appears especialy potent, larger than others within this region of the depths.")

            triggerDefenders()
        }

        addLeaveOption()

    }

    override fun defeatedDefenders() {
        clearOptions()

        //showInteractionImage()

        textPanel.addPara("With the fleet out of the way, a further approach to the shipyard is now possible.")

        textPanel.addPara("Treading through this unit of a station requires more active personal than usual. " +
                "Many reports come in, most without a clue of exciting information, much of the construct seeming as inert as first expected.")

        textPanel.addPara("Some reports are of larger interest, while there are no functioning ships remaining, " +
                "individual components can be found here and there, analysis revealing their origin to span multiple time periods, " +
                "post-collapse, pre-collapse, and dates that the average spacer would not ever spend a thought about. However none can be definitly be traced back to known components.")

        textPanel.addPara("Following, your salvage officer returns with a list of potential salvage to be extracted.")

        createOption("Salvage") {
            clearOptions()

            var random = Random(interactionTarget.getSalvageSeed())

            var dropRandom = ArrayList<DropData>()
            var dropValue = ArrayList<DropData>()
            var drop = DropData()



            drop = DropData()
            drop.group = "basic"
            drop.value = 12500
            dropValue.add(drop)

            /* drop = DropData()
             drop.chances = 1
             drop.group = "abyss_guaranteed_alt"
             dropRandom.add(drop)*/

            drop = DropData()
            drop.chances = 3
            drop.group = "rat_abyss_alteration_common"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 14
            drop.group = "rat_abyss_weapons"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "rat_abyss_artifact_rare"
            dropRandom.add(drop)


            drop = DropData()
            drop.chances = 1
            drop.group = "rare_tech"
            drop.valueMult = 1f
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "blueprints"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 1
            drop.group = "any_hullmod_medium"
            dropRandom.add(drop)

            drop = DropData()
            drop.chances = 4
            drop.group = "weapons2"
            dropRandom.add(drop)

            var biome = AbyssUtils.getBiomeManager().getCell(interactionTarget).getBiome()
            var mult = biome?.getLootMult() ?: 1f

            var salvage = SalvageEntity.generateSalvage(random, mult, mult, 1f, 1f, dropValue, dropRandom)
            salvage.addCommodity("rat_abyssal_matter", AbyssProcgenUtils.getAbyssalMatterDrop(interactionTarget))
            salvage.sort()

            //ArtifactUtils.generateArtifactLoot(salvage, "abyss", 0.05f, 1, random)

            visualPanel.showLoot("Loot", salvage, true) {
                afterSalvage()
            }
        }

    }

    fun afterSalvage() {
        clearOptions()

        textPanel.addPara("Just after you confirm the salvage order, yet another part of your crew, your engineering officer, barges in to the command deck. " +
                "Having analysed different pieces of ship components across the station, the team managed to recreate a specific drive spec that has been destined to be used by ships created from this station. " +
                "Judging by how other fleets within this place make no use of it, it likely never got to leave this place. ")


        var tooltip = textPanel.beginTooltip()


        var container = tooltip.addLunaElement(500f, 48f).apply {

            renderBorder = false
            renderBackground = false

            var widget = AbilityWidgetElement("graphics/icons/abilities/rat_abyssal_burn.png", AbyssUtils.ABYSS_COLOR, innerElement, 48f, 48f)

            var title = innerElement.addTitle("Abyssal Burn", Misc.getBasePlayerColor())
            title.position.rightOfTop(widget.elementPanel, 10f)
            var desc = innerElement.addPara("An active ability that can be used in place of \"Sustained Burn\".\nEquip it from your ability bar.",
                1f, Misc.getGrayColor(), Misc.getHighlightColor(), "Sustained Burn")
        }

        textPanel.addTooltip()

        textPanel.addPara("You order your officer to prepare the necessary actions to have this be of use for fleet operations. ")

        Global.getSector().characterData.addAbility("rat_abyssal_burn")

        addLeaveOption()
    }

}

class AbilityWidgetElement(var iconPath: String, var color: Color, tooltip: TooltipMakerAPI, width: Float, height: Float) : LunaElement(tooltip, width, height) {

    var sprite = Global.getSettings().getSprite(iconPath)
    var inactiveBorder = Global.getSettings().getAndLoadSprite("graphics/icons/skills/rat_skillBorderInactive.png")
    var activeBorder = Global.getSettings().getAndLoadSprite("graphics/icons/skills/rat_skillBorderActive.png")

    var hoverFade = 0f
    var time = 0f

    init {
        enableTransparency = true
        backgroundAlpha = 0f
        borderAlpha = 0f

        onHoverEnter {
            playSound("ui_button_mouseover")
        }

    }

    override fun advance(amount: Float) {
        super.advance(amount)

        time += 1 * amount

        if (isHovering) {
            hoverFade += 10f * amount
        } else {
            hoverFade -= 3f * amount
        }
        hoverFade = hoverFade.coerceIn(0f, 1f)

    }

    override fun render(alphaMult: Float) {
        super.render(alphaMult)

        /* var mult = 1f
         if (!activated) mult = 0.5f*/


        sprite.setNormalBlend()
        sprite.setSize(width-8, height-8)
        sprite.alphaMult = alphaMult

        sprite.color = Color(255, 255, 255)

        sprite.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())

        activeBorder.setNormalBlend()
        activeBorder.color = color
        activeBorder.setSize(width, height)
        activeBorder.alphaMult = alphaMult
        activeBorder.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())


        activeBorder.setAdditiveBlend()
        activeBorder.color = color
        activeBorder.setSize(width, height)
        activeBorder.alphaMult = alphaMult * 0.2f
        activeBorder.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())


        sprite.setAdditiveBlend()
        sprite.setSize(width-8, height-8)
        sprite.alphaMult = alphaMult * 0.5f * hoverFade
        sprite.renderAtCenter(x + (width / 2).toInt(), y + (height / 2).toInt())

    }

    override fun renderBelow(alphaMult: Float) {
        super.renderBelow(alphaMult)

        var backgroundColor = Color(0, 0, 0)

        GL11.glPushMatrix()
        GL11.glDisable(GL11.GL_TEXTURE_2D)
        GL11.glDisable(GL11.GL_CULL_FACE)

        if (alphaMult <= 0.8f) {
            GL11.glEnable(GL11.GL_BLEND)
            GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA)
        } else {
            GL11.glDisable(GL11.GL_BLEND)
        }

        GL11.glColor4f(backgroundColor.red / 255f,
            backgroundColor.green / 255f,
            backgroundColor.blue / 255f,
            backgroundColor.alpha / 255f * (alphaMult * backgroundAlpha))

        GL11.glRectf(x, y , x + width, y + height)

        GL11.glPopMatrix()
    }
}