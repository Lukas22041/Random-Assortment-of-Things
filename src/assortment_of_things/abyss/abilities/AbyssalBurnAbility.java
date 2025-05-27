package assortment_of_things.abyss.abilities;

import assortment_of_things.abyss.AbyssUtils;
import assortment_of_things.abyss.procgen.biomes.BaseAbyssBiome;
import assortment_of_things.misc.ReflectionUtils;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.BattleAPI;
import com.fs.starfarer.api.campaign.CampaignFleetAPI;
import com.fs.starfarer.api.characters.AbilityPlugin;
import com.fs.starfarer.api.fleet.FleetMemberViewAPI;
import com.fs.starfarer.api.impl.campaign.abilities.BaseDurationAbility;
import com.fs.starfarer.api.impl.campaign.abilities.BaseToggleAbility;
import com.fs.starfarer.api.impl.campaign.ids.Stats;
import com.fs.starfarer.api.ui.LabelAPI;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.input.Keyboard;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class AbyssalBurnAbility extends BaseToggleAbility {

    public static String SB_NO_STOP = "$sb_active";
    public static String SB_NO_SLOW = "$sb_no_slow";
    //public static float SENSOR_RANGE_MULT = 0.75f;
    public static float DETECTABILITY_PERCENT = 80f;
    //public static float MAX_BURN_MOD = 10f;
    public static float MAX_BURN_PERCENT = 75;
    public static float ACCELERATION_MULT = 0.35f;

//	public String getSpriteName() {
//		return Global.getSettings().getSpriteName("abilities", Abilities.SUSTAINED_BURN);
//	}


    @Override
    protected String getActivationText() {
        return super.getActivationText();
        //return "Engaging sustained burn";
    }


    @Override
    protected void activateImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        if (!fleet.getMemoryWithoutUpdate().is(SB_NO_STOP, true)) {
            fleet.setVelocity(0, 0);
        }

        AbilityPlugin goDark = fleet.getAbility("go_dark");
        if (goDark != null) {
            if (goDark.isActive()) {
                goDark.deactivate();
            }
        }

        AbilityPlugin sustained = fleet.getAbility("sustained_burn");
        if (sustained != null) {
            if (sustained.isActive()) {
                sustained.deactivate();
            }
        }
    }

    @Override
    public void advance(float amount) {
        super.advance(amount);

        if (getFleet() == null) return;

        for (FleetMemberViewAPI view : getFleet().getViews()) {
            //view.getContrailColor().shift(getModId(), new Color(50,50,50,155), 1f, 1f, .5f);

            Color color = new Color(196, 20, 35);

            if (AbyssUtils.isPlayerInAbyss()) {
                color = AbyssUtils.getBiomeManager().getCurrentBiomeColor().darker();
                color = new Color(color.getRed(), color.getGreen(), color.getBlue(), 155);
            }

            view.getContrailColor().shift(getModId(), color, 1f, 1f, 0.75f * level);
            view.getEngineColor().shift(getModId(), color, 1f, 1f, 0.5f * level);
            //view.getGlowColor().shift(getModId(), color, 1f, 1f, 0.5f * level);
            view.getEngineGlowColor().shift(getModId(), color, 1f, 1f, 0.5f * level);
            view.getEngineGlowSizeMult().shift(getModId(), 1.5f, 1f, 1f, 1f * level);
            view.getEngineHeightMult().shift(getModId(), 3f, 1f, 1f, 1f * level);
            view.getEngineWidthMult().shift(getModId(), 2f, 1f, 1f, 1f * level);
        }
    }

    @Override
    protected void applyEffect(float amount, float level) {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;


        String stop = Global.getSettings().getControlStringForEnumName("GO_SLOW");
        int key = Keyboard.getKeyIndex(stop);

        if (!Keyboard.isKeyDown(key) && fleet.getGoSlowOneFrame()) {
            ReflectionUtils.set("goSlowOneFrame", fleet, false, null);
        }


        AbilityPlugin goDark = fleet.getAbility("go_dark");
        if (goDark != null) {
            if (goDark.isActive() && isActive()) {
                deactivate();
            }
        }

        AbilityPlugin sustained = fleet.getAbility("sustained_burn");
        if (sustained != null) {
            if (sustained.isActive() && isActive()) {
                deactivate();
            }
        }

        AbilityPlugin emergency = fleet.getAbility("emergency_burn");
        if (emergency != null) {
            if (emergency.isInProgress() && isActive()) {
                deactivate();
            }
        }

        AbilityPlugin interdiction = fleet.getAbility("interdiction_pulse");
        if (interdiction != null) {
            if (interdiction.isInProgress()) {
                deactivate();
            }
        }

        AbilityPlugin transverse = (BaseDurationAbility) fleet.getAbility("fracture_jump");
        if (transverse != null) {
            if (transverse.isInProgress()) {
                deactivate();
            }
        }


        //System.out.println("Level: " + level);
        //level = 0.01f;

        if (level > 0 && !fleet.isAIMode() && fleet.getCargo().getFuel() <= 0 &&
                fleet.getContainingLocation() != null && fleet.getContainingLocation().isHyperspace()) {
            deactivate();
            return;
        }

        fleet.getMemoryWithoutUpdate().set(SB_NO_STOP, true, 0.3f);

        if (level > 0 && level < 1 && amount > 0 && !fleet.getMemoryWithoutUpdate().is(SB_NO_SLOW, true)) {
            float activateSeconds = getActivationDays() * Global.getSector().getClock().getSecondsPerDay();
            float speed = fleet.getVelocity().length();
            float acc = Math.max(speed, 200f) / activateSeconds + fleet.getAcceleration();
            float ds = acc * amount;
            if (ds > speed) ds = speed;
            Vector2f dv = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(fleet.getVelocity()));
            dv.scale(ds);
            fleet.setVelocity(fleet.getVelocity().x - dv.x, fleet.getVelocity().y - dv.y);
            return;
        }

        //fleet.getStats().getSensorRangeMod().modifyMult(getModId(), 1f + (SENSOR_RANGE_MULT - 1f) * level, "Sustained burn");
        fleet.getStats().getDetectedRangeMod().modifyPercent(getModId(), DETECTABILITY_PERCENT * level, "Sustained burn");

        //int burnModifier = (int)(MAX_BURN_MOD * level) - (int)(INITIAL_BURN_PENALTY * (1f - level));
        //int burnModifier = (int)(MAX_BURN_MOD * level);
        int burnModifier = 0;
        float burnMult = 1f;

        float b = fleet.getStats().getDynamic().getValue(Stats.SUSTAINED_BURN_BONUS, 0f);
        //burnModifier = (int)((MAX_BURN_MOD + b) * level);
        burnModifier = (int) ((b) * level);

//		if (level > 0.5f) {
//			burnModifier = (int)(MAX_BURN_MOD * (level - 0.5f) / 0.5f);
//		} else {
//			//burnModifier = -1 * (int)(INITIAL_BURN_PENALTY * (1f - level / 0.5f));
//			burnMult = 1f + ((INITIAL_BURN_PENALTY - 1f) * (1f - level / 0.5f));
//		}



        fleet.getStats().getFleetwideMaxBurnMod().modifyFlat(getModId(), burnModifier, "Abyssal burn");
        fleet.getStats().getFleetwideMaxBurnMod().modifyMult(getModId(), burnMult, "Abyssal burn");
        fleet.getStats().getFleetwideMaxBurnMod().modifyPercent(getModId(), MAX_BURN_PERCENT, "Abyssal burn");

        AbilityPlugin sensorBurst = fleet.getAbility("sensor_burst");
        if (sensorBurst != null) {
            if (sensorBurst.isInProgress()) {
                fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
            }
        }


        float accImpact = 0f;
        float burn = Misc.getBurnLevelForSpeed(fleet.getVelocity().length());
        if (burn > 1) {
            float dir = Misc.getDesiredMoveDir(fleet);
//			if (fleet.isPlayerFleet()) {
//				System.out.println("DIR: " + dir);
//			}
            float velDir = Misc.getAngleInDegrees(fleet.getVelocity());
            float diff = Misc.getAngleDiff(dir, velDir);
            //float pad = 90f;
            float pad = 120f;
            diff -= pad;
            if (diff < 0) diff = 0;
            accImpact = 1f - 0.5f * Math.min(1f, (diff / (180f - pad)));
        }

//		if (fleet.isPlayerFleet()) {
//			System.out.println("Acc mult: " + (1f - (1f - ACCELERATION_MULT) * accImpact));
//		}
        fleet.getStats().getAccelerationMult().modifyMult(getModId(), 1f - (1f - ACCELERATION_MULT) * accImpact);
        //fleet.getStats().getAccelerationMult().modifyMult(getModId(), 1f + (ACCELERATION_MULT - 1f) * level);




        if (level <= 0) {
            cleanupImpl();
        }
    }

    @Override
    protected void deactivateImpl() {
        //cleanupImpl();
    }

    @Override
    protected void cleanupImpl() {
        CampaignFleetAPI fleet = getFleet();
        if (fleet == null) return;

        //fleet.getStats().getSensorRangeMod().unmodify(getModId());
        fleet.getStats().getDetectedRangeMod().unmodify(getModId());
        fleet.getStats().getFleetwideMaxBurnMod().unmodify(getModId());
        fleet.getStats().getAccelerationMult().unmodify(getModId());
    }

    @Override
    public boolean showProgressIndicator() {
        return super.showProgressIndicator();
        //return false;
    }

    @Override
    public boolean showActiveIndicator() {
        //super.showActiveIndicator()
        return isActive();
    }

    @Override
    public void createTooltip(TooltipMakerAPI tooltip, boolean expanded) {
        Color gray = Misc.getGrayColor();
        Color highlight = Misc.getHighlightColor();

        String status = " (off)";
        if (turnedOn) {
            status = " (on)";
        }

        if (!Global.CODEX_TOOLTIP_MODE) {
            LabelAPI title = tooltip.addTitle("Abyssal Burn" + status);
            title.highlightLast(status);
            title.setHighlightColor(gray);
        } else {
            tooltip.addSpacer(-10f);
        }

        float pad = 10f;

        tooltip.addPara("Enables a procedure based on the sustained burn on all ships in the fleet, specificly tweaked for the landscape of the abyssal depths.", pad);

        /*tooltip.addPara("Switch the drives of all ships in the fleet to a mode suited for long-distance travel. " +
                "The fleet has to stop briefly to make the switch-over. ", pad);*/

//		public static final float SENSOR_RANGE_MULT = 0.75f;
//		public static final float DETECTABILITY_PERCENT = 50f;
//		public static final float MAX_BURN_MOD = 10f;
//		private static final float ACCELERATION_MULT = 0.25f;
        tooltip.addPara("Increases the maximum burn level by %s, at the expense of slightly lower acceleration, " +
                        "especially in the direction of the fleet's movement. " +
                        "Also increases the range at which the fleet can be detected by %s.", pad,
                highlight,
                //"" + (int) MAX_BURN_MOD,
                "" + (int) Math.round(MAX_BURN_PERCENT) + "%",
                "" + (int) (DETECTABILITY_PERCENT) + "%"
        );

        tooltip.addPara("An Active Sensor Burst will disable the burn increase of this ability, but unlike Sustained Burn, will not bring the fleet to a halt.",
                pad, Misc.getTextColor(), Misc.getHighlightColor(), "Active Sensor Burst", "Sustained Burn");

        if (!Global.CODEX_TOOLTIP_MODE) {
            CampaignFleetAPI fleet = getFleet();
            if (fleet != null) {
                if (!fleet.isAIMode() && fleet.getCargo().getFuel() <= 0 &&
                        fleet.getContainingLocation() != null && fleet.getContainingLocation().isHyperspace()) {
                    tooltip.addPara("Out of fuel.", Misc.getNegativeHighlightColor(), pad);
                }
            }
        }

//		tooltip.addPara("Increases the maximum burn level by %s, Acceleration is greatly reduced, and " +
//				"higher drive emissions interfere with sensors, decreasing their range by %s. " +
//				"The fleet is also %s easier to detect.", pad,
//				highlight,
//				"" + (int) MAX_BURN_MOD,
//				"" + (int)((1f - SENSOR_RANGE_MULT) * 100f) + "%",
//				"" + (int)(DETECTABILITY_PERCENT) + "%"
//		);
        //tooltip.addPara("Disables the transponder when activated.", pad);
        addIncompatibleToTooltip(tooltip, expanded);
    }

    public boolean isUsable() {
        if (!super.isUsable()) return false;
        if (getFleet() == null) return false;

        CampaignFleetAPI fleet = getFleet();
        if (!fleet.isAIMode() && fleet.getCargo().getFuel() <= 0 &&
                fleet.getContainingLocation() != null && fleet.getContainingLocation().isHyperspace()) {
            return false;
        }

        AbilityPlugin emergency = (BaseDurationAbility) fleet.getAbility("emergency_burn");
        if (emergency != null) {
            if (emergency.isInProgress()) {
                return false;
            }
        }

        AbilityPlugin interdict = (BaseDurationAbility) fleet.getAbility("interdiction_pulse");
        if (interdict != null) {
            if (interdict.isInProgress()) {
                return false;
            }
        }

        AbilityPlugin transverse = (BaseDurationAbility) fleet.getAbility("fracture_jump");
        if (transverse != null) {
            if (transverse.isInProgress()) {
                return false;
            }
        }

        return true;
    }

    public boolean hasTooltip() {
        return true;
    }


    @Override
    public void fleetLeftBattle(BattleAPI battle, boolean engagedInHostilities) {
//		if (battle.isPlayerInvolved() && engagedInHostilities) {
//			deactivate();
//		}
    }


    @Override
    public void fleetJoinedBattle(BattleAPI battle) {
        if (!battle.isPlayerInvolved()) {
            deactivate();
        }
    }
}