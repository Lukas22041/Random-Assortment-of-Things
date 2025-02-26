package assortment_of_things.abyss.misc;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.impl.campaign.ids.Stats;

public class MiscReplacement {

    public static boolean isSlowMoving(CampaignFleetAPI fleet) {
        return fleet.getCurrBurnLevel() <= getGoSlowBurnLevel(fleet);
    }

    public static float SNEAK_BURN_MULT = Global.getSettings().getFloat("sneakBurnMult");

    public static float getGoSlowBurnLevel(CampaignFleetAPI fleet) {
//		if (fleet.isPlayerFleet()) {
//			System.out.println("fewfewfe");
//		}
        float bonus = fleet.getStats().getDynamic().getMod(Stats.MOVE_SLOW_SPEED_BONUS_MOD).computeEffective(0);
        //int burn = (int)Math.round(MAX_SNEAK_BURN_LEVEL + bonus);
        //int burn = (int)Math.round(fleet.getFleetData().getMinBurnLevelUnmodified() * SNEAK_BURN_MULT);
        float burn = fleet.getFleetData().getMinBurnLevel() * SNEAK_BURN_MULT;
        burn += bonus;
        //burn = (int) Math.min(burn, fleet.getFleetData().getBurnLevel() - 1);
        return burn;
    }

}
