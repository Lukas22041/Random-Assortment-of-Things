package assortment_of_things.abyss.hullmods.abyssals;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.BaseHullMod;
import com.fs.starfarer.api.combat.DamageType;
import com.fs.starfarer.api.combat.MutableShipStatsAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.ShipAPI.HullSize;
import com.fs.starfarer.api.combat.ShipCommand;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI;
import com.fs.starfarer.api.combat.ShipEngineControllerAPI.ShipEngineAPI;
import com.fs.starfarer.api.combat.ShipVariantAPI;
import com.fs.starfarer.api.combat.ShipwideAIFlags;
import com.fs.starfarer.util.IntervalTracker;
import org.lazywizard.lazylib.MathUtils;
import org.lazywizard.lazylib.VectorUtils;
import org.lazywizard.lazylib.combat.CombatUtils;
import java.util.ArrayList;
import java.util.List;


public class CharybdisHullmod extends BaseHullMod {
    public static final float ZEROFLUXSPEED_MOD = 0.30F;
    public static final float PROFILE_MOD = 150f;

    //Based on KT_SinuousBody by Sinosauropteryx (Kingdom of Terra mod)
    public static final int NUMBER_OF_SEGMENTS = 5;
    public static final float RANGE = 75; // Flexibility constant. Range of movement of each segment.
    public static final float REALIGNMENT_CONSTANT = 5f; // Elasticity constant. How quickly the body unfurls after being curled up.

    private static final String[] SEGMENT_NAMES = new String[]{
        "SEGMENT1",
        "SEGMENT2",
        "SEGMENT3",
        "SEGMENT4",
        "SEGMENT5"
    };

    private final IntervalTracker _parentInterval = new IntervalTracker(.15f, .25f);

    private final IntervalTracker _repulseInterval = new IntervalTracker(.1f, .1f);

    //NOTE! Careful trying to optimize this code, things can easily stop working

    public String getDescriptionParam(int index, HullSize hullSize) {
        return null;
    }

    @Override
    public void applyEffectsBeforeShipCreation(HullSize hullSize, MutableShipStatsAPI stats, String id) {
        stats.getZeroFluxSpeedBoost().modifyMult(id, ZEROFLUXSPEED_MOD);
        stats.getSensorProfile().modifyFlat(id, PROFILE_MOD);

        ShipVariantAPI ship = stats.getVariant();
        if (ship != null) {
            float fuel = 0;
            float crew = 0;
            float cargo = 0;
            float fuelCost = 0;
            float crewLimit = 0;
            float supplyCost = 0;
            for (int i = 1; i <= NUMBER_OF_SEGMENTS; i++) {
                cargo += 1250f;
                fuel += 500f;
                crew += 50f;
                crewLimit += 250f;
                fuelCost += 2f;
                supplyCost += 4f;
            }
            stats.getFuelMod().modifyFlat("trainModuleBonus", fuel);
            stats.getCargoMod().modifyFlat("trainModuleBonus", cargo);
            stats.getMinCrewMod().modifyFlat("trainModuleBonus", crew);
            stats.getFuelUseMod().modifyFlat("trainModuleBonus", fuelCost);
            stats.getMaxCrewMod().modifyFlat("trainModuleBonus", crewLimit);
            stats.getSuppliesPerMonth().modifyFlat("trainModuleBonus", supplyCost);
        }
    }

    @Override
    public void applyEffectsAfterShipCreation(ShipAPI ship, String id) {

    }

    @Override
    public boolean isApplicableToShip(ShipAPI ship) {
        return true;
    }

    @Override
    public void advanceInCombat(ShipAPI ship, float amount) {

        //Setting broadside hints does nothing.  :(  Ship is good in player hands, total garbage in the hands of the AI.

        super.advanceInCombat(ship, amount);

        List<ShipAPI> children = ship.getChildModulesCopy();

        advanceParent(ship, children, amount);
        for (ShipAPI s : children) {
            advanceChild(s, ship);
        }

        TrainWagon[] wagonSegments = getTrainWagon(children);


        float hitpoints = ship.getHitpoints();
        for (ShipAPI child : children) {
            if (child.getHitpoints() < hitpoints) {
                hitpoints = child.getHitpoints();
            }
        }

        ship.setHitpoints(hitpoints);
        for (ShipAPI child : children) {
            child.setHitpoints(hitpoints);
        }

       // wagonSegments = removeDeadSegments(wagonSegments);

        // Iterates through each SinuousSegment
        for(TrainWagon tw : wagonSegments) {
            try {
                // First segment is "vanilla" / attached to mothership. Rest are pseudo-attached to previous segment's SEGMENT slot
                if (!tw.isFirst()) {
                    tw.ship.getLocation().set(tw.previousSegment.ship.getHullSpec().getWeaponSlotAPI("SEGMENT").computePosition(tw.previousSegment.ship));
                }

                float difference = ship.getAngularVelocity() * amount;
                float angle = tw.ship.getStationSlot().getAngle() - difference;

                // angle of module is offset by angle of previous module, normalized to between 180 and -180
                float angleOffset = normalizeAngle(tw.ship.getFacing() - tw.previousSegment.ship.getFacing());
                //float angleOffset = normalizeAngle(90f - tw.previousSegment.ship.getFacing());

                if (angleOffset > 180f)
                    angleOffset -= 360f;

                // angle of range check is offset by angle of previous segment in relation to mothership
                float localMod = normalizeAngle(tw.previousSegment.ship.getFacing() - tw.ship.getParentStation().getFacing());

                // range limit handler. If the tail is outside the max range, it won't swing any farther.
                if (angleOffset < RANGE * -0.5)
                    angle = normalizeAngle(RANGE * -0.5f + localMod);
                if (angleOffset > RANGE * 0.5)
                    angle = normalizeAngle(RANGE * 0.5f + localMod);

                // Tail returns to straight position, moving faster the more bent it is - spring approximation
                angle -= (angleOffset / RANGE * 0.5f) * REALIGNMENT_CONSTANT;

                tw.ship.getStationSlot().setAngle(angle);
            } catch (Exception ignored) {
                // This covers the gap between when a segment and its dependents die
            }

            try{
                // parent vents, children vent
                if (ship.getFluxTracker().isVenting() && !tw.ship.getFluxTracker().isVenting() && tw.ship.getFluxTracker().getCurrFlux() > 1000 && tw.ship.isAlive()) {
                    tw.ship.giveCommand(ShipCommand.VENT_FLUX, null, 0);
                    return;
                }

                // try to shoot what the player is targeting
                if (ship.getShipTarget() != null && ship.getShipTarget() != ship.getShipTarget()) {
                    tw.ship.setShipTarget(ship.getShipTarget());
                }

                //propagate fighter commands
                if (tw.ship.hasLaunchBays()) {
                    if (tw.ship.isPullBackFighters() != tw.ship.isPullBackFighters()) {
                        tw.ship.setPullBackFighters(tw.ship.isPullBackFighters());
                    }
                    if (tw.ship.getAIFlags() != null) {
                        if (((Global.getCombatEngine().getPlayerShip() == ship) || (ship.getAIFlags() == null))
                                && (ship.getShipTarget() != null)) {
                            tw.ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET, 1f, ship.getShipTarget());
                        } else if ((ship.getAIFlags() != null)
                                && ship.getAIFlags().hasFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET)
                                && (ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET) != null)) {
                            tw.ship.getAIFlags().setFlag(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET, 1f, ship.getAIFlags().getCustom(ShipwideAIFlags.AIFlags.CARRIER_FIGHTER_TARGET));
                        }
                    }
                }

            } catch (Exception ignored){}
        }

        repulseNearbyShips(wagonSegments, amount);
    }

    private void repulseNearbyShips(TrainWagon[] wagonSegments, float amount){
        _repulseInterval.advance(amount);
        if(!_repulseInterval.intervalElapsed()){
            return;
        }

        // to try and prevent collisions, "push" nearby ships away tangentially from this segment
        try {
            for(TrainWagon tw : wagonSegments) {
                for (ShipAPI near : CombatUtils.getShipsWithinRange(tw.ship.getLocation(), 150)) {
                    if (near.isShuttlePod() || near.isFighter() || near.isDrone() || !near.isAlive()) {
                        continue;
                    }

                    if(near.isPhased() || near.getOwner() != tw.ship.getOwner()){
                        continue;
                    }

                    // exempt both the front and the wagons
                    if (near.getHullSpec().getBaseHullId().startsWith("rat_charybdis")) {
                        continue;
                    }

                    // some ships have insane collision radii, protect against that
                    if(MathUtils.getDistance(near.getLocation(), tw.ship.getLocation()) > 350){
                        continue;
                    }

                    // don't make them go crazy fast, that causes other problems
                    if(near.getVelocity().length() < 100) {
                        int force = 300;
                        // if they are getting really close, push harder
                        if(MathUtils.isWithinRange(tw.ship.getLocation(), near.getLocation(), 25)){
                            force *= 2;
                        }

                        CombatUtils.applyForce(getRoot(near), VectorUtils.getDirectionalVector(tw.ship.getLocation(), near.getLocation()), force);
                    }

                    //todo check nearby ships to see if they're on a vector to collide and apply to them
                }
            }
        } catch (Exception ignored) {

        }
    }

    private float normalizeAngle(float f) {
        if (f < 0f)
            return f + 360f;
        if (f > 360f)
            return f - 360f;
        return f;
    }

    //////////
    // This section of code was taken largely from the Ship and Weapon Pack mod.
    // I did not create it. Credit goes to DarkRevenant.
    //////////
    private static void advanceChild(ShipAPI child, ShipAPI parent) {
        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            if (parent.isAlive()) {
                if (ec.isAccelerating()) {
                    child.giveCommand(ShipCommand.ACCELERATE, null, 0);
                }
                if (ec.isAcceleratingBackwards()) {
                    child.giveCommand(ShipCommand.ACCELERATE_BACKWARDS, null, 0);
                }
                if (ec.isDecelerating()) {
                    child.giveCommand(ShipCommand.DECELERATE, null, 0);
                }
                if (ec.isStrafingLeft()) {
                    child.giveCommand(ShipCommand.STRAFE_LEFT, null, 0);
                }
                if (ec.isStrafingRight()) {
                    child.giveCommand(ShipCommand.STRAFE_RIGHT, null, 0);
                }
                if (ec.isTurningLeft()) {
                    child.giveCommand(ShipCommand.TURN_LEFT, null, 0);
                }
                if (ec.isTurningRight()) {
                    child.giveCommand(ShipCommand.TURN_RIGHT, null, 0);
                }
            }

            ShipEngineControllerAPI cec = child.getEngineController();
            if (cec != null) {
                if ((ec.isFlamingOut() || ec.isFlamedOut()) && !cec.isFlamingOut() && !cec.isFlamedOut()) {
                    child.getEngineController().forceFlameout(true);
                }
            }
        }
    }

    private void advanceParent(ShipAPI parent, List<ShipAPI> children, float amount) {
        _parentInterval.advance(amount);
        if(!_parentInterval.intervalElapsed()){
            return;
        }

        ShipEngineControllerAPI ec = parent.getEngineController();
        if (ec != null) {
            float originalMass = 17000;
            int originalEngines = 16;

            float thrustPerEngine = originalMass / originalEngines;

            /* Don't count parent's engines for this stuff - game already affects stats */
            float workingEngines = ec.getShipEngines().size();
            for (ShipAPI child : children) {
                if ((child.getParentStation() == parent) && (child.getStationSlot() != null) && child.isAlive()) {
                    ShipEngineControllerAPI cec = child.getEngineController();
                    if (cec != null) {
                        float contribution = 0f;
                        for (ShipEngineAPI ce : cec.getShipEngines()) {
                            if (ce.isActive() && !ce.isDisabled() && !ce.isPermanentlyDisabled() && !ce.isSystemActivated()) {
                                contribution += ce.getContribution();
                            }
                        }
                        workingEngines += cec.getShipEngines().size() * contribution;
                    }
                }
            }

            float thrust = workingEngines * thrustPerEngine;
            float enginePerformance = thrust / Math.max(1f, getTrainMass(parent, children));
           /* parent.getMutableStats().getZeroFluxSpeedBoost().modifyMult("ED_trainlocomotive", enginePerformance);
            parent.getMutableStats().getTurnAcceleration().modifyMult("ED_trainlocomotive", enginePerformance);
            parent.getMutableStats().getAcceleration().modifyMult("ED_trainlocomotive", enginePerformance);
            parent.getMutableStats().getMaxTurnRate().modifyMult("ED_trainlocomotive", enginePerformance);
            parent.getMutableStats().getMaxSpeed().modifyMult("ED_trainlocomotive", enginePerformance);*/
        }
    }

    private static float getTrainMass(ShipAPI ship, List<ShipAPI> modules) {
        float mass = ship.getMass();
        if (modules != null) {
            for (ShipAPI m : modules) {
                if (m != null && m.isAlive()) {
                    mass += m.getMass();
                }
            }
        }
        return mass;
    }

    public TrainWagon[] getTrainWagon(List<ShipAPI> childModules) {
        // yeah, this is inefficient building this every frame, but if you cache this value,
        // sometimes the ship, or another ship in combat stops working.  Don't mess with this.
        TrainWagon[] segments = new TrainWagon[NUMBER_OF_SEGMENTS];

        for (int f = 0; f < segments.length; f++){
            // Iterates through SinuousSegment array and connects them in order
            segments[f] = new TrainWagon();
            if (f > 0) {
                segments[f].previousSegment = segments[f - 1];
                segments[f - 1].nextSegment = segments[f];
            }

            // Assigns each module to a segment based on its station slot name
            for (ShipAPI s : childModules) {
                s.ensureClonedStationSlotSpec();

                if (s.getStationSlot() != null && s.getStationSlot().getId().equals(SEGMENT_NAMES[f])) {
                    segments[f].ship = s;

                    // First module: Assigns mothership as its previousSegment
                    if (f == 0) {
                        segments[f].previousSegment = new TrainWagon();
                        segments[f].previousSegment.ship = s.getParentStation();
                        segments[f].previousSegment.nextSegment = segments[f];
                    }
                }
            }
        }

        return segments;
    }

    public TrainWagon[] removeDeadSegments(TrainWagon[] wagonSegments) {
        ArrayList<TrainWagon> ret = new ArrayList<>();

        for (int f = 0; f < NUMBER_OF_SEGMENTS; f++) {
            TrainWagon tw = wagonSegments[f];
            if (tw != null && tw.ship != null && tw.ship.isAlive()) {
                ret.add(tw);
            } else {
                // When a segment dies, remove all dependent segments
                for (int g = f; g < NUMBER_OF_SEGMENTS; g++) {
                    if (wagonSegments[g] != null && wagonSegments[g].ship != null && wagonSegments[g].ship.isAlive()) {
                        wagonSegments[g].ship.setHitpoints(1f);
                        try {
                            Global.getCombatEngine().applyDamage(wagonSegments[g].ship, wagonSegments[g].ship.getLocation(), 1000, DamageType.HIGH_EXPLOSIVE, 0f, true, false, null);
                        } catch (Exception ignored) {
                        }
                    }
                }
            }
        }

        return ret.toArray(new TrainWagon[]{});
    }

    public static boolean isMultiShip(ShipAPI ship) {
        return ship.getParentStation() != null || ship.isShipWithModules();
    }

    public static ShipAPI getRoot(ShipAPI ship) {
        if (isMultiShip(ship)) {
            ShipAPI root = ship;
            while (root.getParentStation() != null) {
                root = root.getParentStation();
            }
            return root;
        } else {
            return ship;
        }
    }

    public static class TrainWagon {
        //Based on KT_SinuousSegment by Sinosauropteryx (Kingdom of Terra mod)
        public ShipAPI ship = null; // ShipAPI means we can't keep a member reference to any TrainWagon objects
        public TrainWagon nextSegment = null;
        public TrainWagon previousSegment = null;

        boolean isFirst(){
            return previousSegment != null && previousSegment.ship.getHullSpec().getBaseHullId().equals("rat_charybdis");
        }
    }
}
