package assortment_of_things.abyss;

import assortment_of_things.abyss.procgen.AbyssDepth;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.combat.*;
import com.fs.starfarer.api.fleet.FleetGoal;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.ids.Planets;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.terrain.HyperspaceAbyssPluginImpl;
import com.fs.starfarer.api.impl.campaign.terrain.PulsarBeamTerrainPlugin;
import com.fs.starfarer.api.impl.campaign.terrain.StarCoronaTerrainPlugin;
import com.fs.starfarer.api.impl.combat.BattleCreationPluginImpl;
import com.fs.starfarer.api.impl.combat.EscapeRevealPlugin;
import com.fs.starfarer.api.input.InputEventAPI;
import com.fs.starfarer.api.mission.FleetSide;
import com.fs.starfarer.api.mission.MissionDefinitionAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.WeightedRandomPicker;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AbyssBattleCreationPlugin implements BattleCreationPlugin {




    public static float ABYSS_SHIP_SPEED_PENALTY = 20f;
    public static float ABYSS_MISSILE_SPEED_PENALTY = 20f;
    //public static float ABYSS_MISSILE_FLIGHT_TIME_MULT = 1.25f;
    public static float ABYSS_OVERLAY_ALPHA = 0.2f;

    protected float width, height;

    protected float xPad = 2000;
    protected float yPad = 2000;

    protected List<String> objs = null;

    protected float prevXDir = 0;
    protected float prevYDir = 0;
    protected boolean escape;

    protected BattleCreationContext context;
    protected MissionDefinitionAPI loader;

    public void initBattle(final BattleCreationContext context, MissionDefinitionAPI loader) {

        this.context = context;
        this.loader = loader;
        CampaignFleetAPI playerFleet = context.getPlayerFleet();
        CampaignFleetAPI otherFleet = context.getOtherFleet();
        FleetGoal playerGoal = context.getPlayerGoal();
        FleetGoal enemyGoal = context.getOtherGoal();

        // doesn't work for consecutive engagements; haven't investigated why
        //Random random = Misc.getRandom(Misc.getNameBasedSeed(otherFleet), 23);

        Random random = Misc.getRandom(Misc.getSalvageSeed(otherFleet) *
                (long)otherFleet.getFleetData().getNumMembers(), 23);
        //System.out.println("RNG: " + random.nextLong());
        //random = new Random(1213123L);
        //Random random = Misc.random;

        escape = playerGoal == FleetGoal.ESCAPE || enemyGoal == FleetGoal.ESCAPE;

        int maxFP = (int) Global.getSettings().getFloat("maxNoObjectiveBattleSize");
        int fpOne = 0;
        int fpTwo = 0;
        for (FleetMemberAPI member : playerFleet.getFleetData().getMembersListCopy()) {
            if (member.canBeDeployedForCombat() || playerGoal == FleetGoal.ESCAPE) {
                fpOne += member.getUnmodifiedDeploymentPointsCost();
            }
        }
        for (FleetMemberAPI member : otherFleet.getFleetData().getMembersListCopy()) {
            if (member.canBeDeployedForCombat() || playerGoal == FleetGoal.ESCAPE) {
                fpTwo += member.getUnmodifiedDeploymentPointsCost();
            }
        }

        int smaller = Math.min(fpOne, fpTwo);

        boolean withObjectives = smaller > maxFP;
        if (!context.objectivesAllowed) {
            withObjectives = false;
        }

        int numObjectives = 0;
        if (withObjectives) {
//			if (fpOne + fpTwo > maxFP + 70) {
//				numObjectives = 3 
//				numObjectives = 3 + (int)(Math.random() * 2.0);
//			} else {
//				numObjectives = 2 + (int)(Math.random() * 2.0);
//			}
            if (fpOne + fpTwo > maxFP + 70) {
                numObjectives = 4;
                //numObjectives = 3 + (int)(Math.random() * 2.0);
            } else {
                numObjectives = 3 + random.nextInt(2);
                //numObjectives = 2 + (int)(Math.random() * 2.0);
            }
        }

        // shouldn't be possible, but..
        if (numObjectives > 4) {
            numObjectives = 4;
        }

        int baseCommandPoints = (int) Global.getSettings().getFloat("startingCommandPoints");

        // 
        loader.initFleet(FleetSide.PLAYER, "ISS", playerGoal, false,
                context.getPlayerCommandPoints() - baseCommandPoints,
                (int) playerFleet.getCommanderStats().getCommandPoints().getModifiedValue() - baseCommandPoints);
        loader.initFleet(FleetSide.ENEMY, "", enemyGoal, true,
                (int) otherFleet.getCommanderStats().getCommandPoints().getModifiedValue() - baseCommandPoints);

        List<FleetMemberAPI> playerShips = playerFleet.getFleetData().getCombatReadyMembersListCopy();
        if (playerGoal == FleetGoal.ESCAPE) {
            playerShips = playerFleet.getFleetData().getMembersListCopy();
        }
        for (FleetMemberAPI member : playerShips) {
            loader.addFleetMember(FleetSide.PLAYER, member);
        }


        List<FleetMemberAPI> enemyShips = otherFleet.getFleetData().getCombatReadyMembersListCopy();
        if (enemyGoal == FleetGoal.ESCAPE) {
            enemyShips = otherFleet.getFleetData().getMembersListCopy();
        }
        for (FleetMemberAPI member : enemyShips) {
            loader.addFleetMember(FleetSide.ENEMY, member);
        }

        width = 18000f;
        height = 18000f;

        if (escape) {
            width = 18000f;
            //height = 24000f;
            height = 18000f;
        } else if (withObjectives) {
            width = 24000f;
            if (numObjectives == 2) {
                height = 14000f;
            } else {
                height = 18000f;
            }
        }

        createMap(random);

        context.setInitialDeploymentBurnDuration(1.5f);
        context.setNormalDeploymentBurnDuration(6f);
        context.setEscapeDeploymentBurnDuration(1.5f);

        xPad = 2000f;
        yPad = 3000f;

        if (escape) {
//			addEscapeObjectives(loader, 4);
//			context.setInitialEscapeRange(7000f);
//			context.setFlankDeploymentDistance(9000f);
            addEscapeObjectives(loader, 2, random);
//			context.setInitialEscapeRange(4000f);
//			context.setFlankDeploymentDistance(8000f);

            context.setInitialEscapeRange(Global.getSettings().getFloat("escapeStartDistance"));
            context.setFlankDeploymentDistance(Global.getSettings().getFloat("escapeFlankDistance"));

            loader.addPlugin(new EscapeRevealPlugin(context));
        } else {
            if (withObjectives) {
                addObjectives(loader, numObjectives, random);
                context.setStandoffRange(height - 4500f);
            } else {
                context.setStandoffRange(6000f);
            }

            context.setFlankDeploymentDistance(height/2f); // matters for Force Concentration
        }
    }

    public void afterDefinitionLoad(final CombatEngineAPI engine) {
        if (coronaIntensity > 0 && (corona != null || pulsar != null)) {
            String name = "Corona";
            if (pulsar != null) name = pulsar.getTerrainName();
            else if (corona != null) name = corona.getTerrainName();

            final String name2 = name;

//			CombatFleetManagerAPI manager = engine.getFleetManager(FleetSide.PLAYER);
//			for (FleetMemberAPI member : manager.getReservesCopy()) {
//			}
            final Object key1 = new Object();
            final Object key2 = new Object();
            final String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_cr_penalty");
            engine.addPlugin(new BaseEveryFrameCombatPlugin() {
                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    engine.maintainStatusForPlayerShip(key1, icon, name2, "reduced peak time", true);
                    engine.maintainStatusForPlayerShip(key2, icon, name2, "faster CR degradation", true);
                }
            });
        }

        if (abyssalDepth > 0) {
            Color color = Misc.scaleColor(Color.white, 1f - abyssalDepth);
            engine.setBackgroundColor(color);

            color = Misc.scaleAlpha(Color.black, abyssalDepth * ABYSS_OVERLAY_ALPHA);
            engine.setBackgroundGlowColor(color);
            engine.setBackgroundGlowColorNonAdditive(true);

            if (abyssalDepth > HyperspaceAbyssPluginImpl.DEPTH_THRESHOLD_FOR_NO_DUST_PARTICLES_IN_COMBAT) {
                engine.setRenderStarfield(false);
            }

            final Object key1 = new Object();
            final Object key2 = new Object();
            final String icon = Global.getSettings().getSpriteName("ui", "icon_tactical_engine_damage");
            final String name = "Abyssal hyperspace";
            engine.addPlugin(new BaseEveryFrameCombatPlugin() {
                @Override
                public void advance(float amount, List<InputEventAPI> events) {
                    String percentSpeed = "-" + (int)Math.round(ABYSS_SHIP_SPEED_PENALTY) + "%";
                    String percentMissile = "-" + (int)Math.round(ABYSS_MISSILE_SPEED_PENALTY) + "%";
                    engine.maintainStatusForPlayerShip(key1, icon, name, percentSpeed + " top speed", true);
                    engine.maintainStatusForPlayerShip(key2, icon, name, percentMissile + " missle speed / range", true);

                    String modId = "abyssal";
                    float modW = -0.0f * abyssalDepth;
                    float modL = -0.33f * abyssalDepth;
                    float modG = -0.5f * abyssalDepth;

                    for (ShipAPI curr : engine.getShips()) {
                        if (curr.isHulk()) continue;

                        curr.getEngineController().fadeToOtherColor(this, Color.black, null, 1f, abyssalDepth * 0.4f);
                        curr.getEngineController().extendFlame(this, modL, modW, modG);

                        curr.getMutableStats().getMaxSpeed().modifyMult(modId,
                                1f - abyssalDepth * ABYSS_SHIP_SPEED_PENALTY * 0.01f);
                        curr.getMutableStats().getMissileWeaponRangeBonus().modifyMult(modId,
                                1f - abyssalDepth * ABYSS_MISSILE_SPEED_PENALTY * 0.01f);
                        curr.getMutableStats().getMissileMaxSpeedBonus().modifyMult(modId,
                                1f - abyssalDepth * ABYSS_MISSILE_SPEED_PENALTY * 0.01f);
                    }

                    for (MissileAPI missile : engine.getMissiles()) {
                        missile.getEngineController().fadeToOtherColor(this, Color.black, null, 1f, abyssalDepth * 0.4f);
                        missile.getEngineController().extendFlame(this, modL, modW, 0f);
                    }

                }
            });

        }
    }


    protected float abyssalDepth = 0f;
    protected float coronaIntensity = 0f;
    protected StarCoronaTerrainPlugin corona = null;
    protected PulsarBeamTerrainPlugin pulsar = null;
    protected void createMap(Random random) {
        loader.initMap((float)-width/2f, (float)width/2f, (float)-height/2f, (float)height/2f);

        CampaignFleetAPI playerFleet = context.getPlayerFleet();
        String nebulaTex = null;
        String nebulaMapTex = null;
        boolean inNebula = false;

        boolean protectedFromCorona = false;
        for (CustomCampaignEntityAPI curr : playerFleet.getContainingLocation().getCustomEntitiesWithTag(Tags.PROTECTS_FROM_CORONA_IN_BATTLE)) {
            if (Misc.getDistance(curr.getLocation(), playerFleet.getLocation()) <= curr.getRadius() + Global.getSector().getPlayerFleet().getRadius() + 10f) {
                protectedFromCorona = true;
                break;
            }
        }

        abyssalDepth = Misc.getAbyssalDepth(playerFleet);

        float numRings = 0;

        Color coronaColor = null;
        // this assumes that all nebula in a system are of the same color
        for (CampaignTerrainAPI terrain : playerFleet.getContainingLocation().getTerrainCopy()) {
            //if (terrain.getType().equals(Terrain.NEBULA)) {
            if (terrain.getPlugin() instanceof BattleCreationPluginImpl.NebulaTextureProvider) {
                if (terrain.getPlugin().containsEntity(playerFleet)) {
                    inNebula = true;
                    if (terrain.getPlugin() instanceof BattleCreationPluginImpl.NebulaTextureProvider) {
                        BattleCreationPluginImpl.NebulaTextureProvider provider = (BattleCreationPluginImpl.NebulaTextureProvider) terrain.getPlugin();
                        nebulaTex = provider.getNebulaTex();
                        nebulaMapTex = provider.getNebulaMapTex();
                    }
                } else {
                    if (nebulaTex == null) {
                        if (terrain.getPlugin() instanceof BattleCreationPluginImpl.NebulaTextureProvider) {
                            BattleCreationPluginImpl.NebulaTextureProvider provider = (BattleCreationPluginImpl.NebulaTextureProvider) terrain.getPlugin();
                            nebulaTex = provider.getNebulaTex();
                            nebulaMapTex = provider.getNebulaMapTex();
                        }
                    }
                }
            } else if (terrain.getPlugin() instanceof StarCoronaTerrainPlugin && pulsar == null && !protectedFromCorona) {
                StarCoronaTerrainPlugin plugin = (StarCoronaTerrainPlugin) terrain.getPlugin();
                if (plugin.containsEntity(playerFleet)) {
                    float angle = Misc.getAngleInDegrees(terrain.getLocation(), playerFleet.getLocation());
                    Color color = plugin.getAuroraColorForAngle(angle);
                    float intensity = plugin.getIntensityAtPoint(playerFleet.getLocation());
                    intensity = 0.4f + 0.6f * intensity;
                    int alpha = (int)(80f * intensity);
                    color = Misc.setAlpha(color, alpha);
                    if (coronaColor == null || coronaColor.getAlpha() < alpha) {
                        coronaColor = color;
                        coronaIntensity = intensity;
                        corona = plugin;
                    }
                }
            } else if (terrain.getPlugin() instanceof PulsarBeamTerrainPlugin && !protectedFromCorona) {
                PulsarBeamTerrainPlugin plugin = (PulsarBeamTerrainPlugin) terrain.getPlugin();
                if (plugin.containsEntity(playerFleet)) {
                    float angle = Misc.getAngleInDegreesStrict(terrain.getLocation(), playerFleet.getLocation());
                    Color color = plugin.getPulsarColorForAngle(angle);
                    float intensity = plugin.getIntensityAtPoint(playerFleet.getLocation());
                    intensity = 0.4f + 0.6f * intensity;
                    int alpha = (int)(80f * intensity);
                    color = Misc.setAlpha(color, alpha);
                    if (coronaColor == null || coronaColor.getAlpha() < alpha) {
                        coronaColor = color;
                        coronaIntensity = intensity;
                        pulsar = plugin;
                        corona = null;
                    }
                }
            } else if (terrain.getType().equals(Terrain.RING)) {
                if (terrain.getPlugin().containsEntity(playerFleet)) {
                    numRings++;
                }
            }
        }

        nebulaTex = "graphics/terrain/rat_combat_depths1.png";
        nebulaMapTex = "graphics/terrain/rat_combat_depths1_map.png";

        if (nebulaTex != null) {
            loader.setNebulaTex(nebulaTex);
            loader.setNebulaMapTex(nebulaMapTex);
        }

        if (coronaColor != null) {
            loader.setBackgroundGlowColor(coronaColor);
        }

        int numNebula = 15;
        if (inNebula) {
            numNebula = 100;
        }
        if (!inNebula && playerFleet.isInHyperspace()) {
            numNebula = 0;
        }

        for (int i = 0; i < numNebula; i++) {
            float x = random.nextFloat() * width - width/2;
            float y = random.nextFloat() * height - height/2;
            float radius = 100f + random.nextFloat() * 400f;
            if (inNebula) {
                radius += 100f + 500f * random.nextFloat();
            }
            loader.addNebula(x, y, radius);
        }

        //Dont need asteroids in the abyss

      /*  float numAsteroidsWithinRange = countNearbyAsteroids(playerFleet);

        int numAsteroids = Math.min(400, (int)((numAsteroidsWithinRange + 1f) * 20f));

        loader.addAsteroidField(0, 0, random.nextFloat() * 360f, width,
                20f, 70f, numAsteroids);

        if (numRings > 0) {
            int numRingAsteroids = (int) (numRings * 300 + (numRings * 600f) * random.nextFloat());
            //int numRingAsteroids = (int) (numRings * 1600 + (numRings * 600f) * (float) Math.random());
            if (numRingAsteroids > 1500) {
                numRingAsteroids = 1500;
            }
            loader.addRingAsteroids(0, 0, random.nextFloat() * 360f, width,
                    100f, 200f, numRingAsteroids);
        }*/

        //setRandomBackground(loader);
        loader.setBackgroundSpriteName(playerFleet.getContainingLocation().getBackgroundTextureFilename());
//		loader.setBackgroundSpriteName("graphics/backgrounds/hyperspace_bg_cool.jpg");
//		loader.setBackgroundSpriteName("graphics/ships/onslaught/onslaught_base.png");

        if (playerFleet.getContainingLocation() == Global.getSector().getHyperspace()) {
            loader.setHyperspaceMode(true);
        } else {
            loader.setHyperspaceMode(false);
        }


        //addMultiplePlanets();
        addClosestPlanet();
    }

    protected void addClosestPlanet() {
        float bgWidth = 2048f;
        float bgHeight = 2048f;

        PlanetAPI planet = getClosestPlanet(context.getPlayerFleet());
        if (planet == null) return;

        float dist = Vector2f.sub(context.getPlayerFleet().getLocation(), planet.getLocation(), new Vector2f()).length() - planet.getRadius();
        if (dist < 0) dist = 0;
        float baseRadius = planet.getRadius();
        float scaleFactor = 1.5f;
        float maxRadius = 500f;
        float minRadius = 100f;

//		if (planet.isStar()) {
//			scaleFactor = 0.01f;
//			maxRadius = 20f;
//		}

        float maxDist = SINGLE_PLANET_MAX_DIST - planet.getRadius();
        if (maxDist < 1) maxDist = 1;


        boolean playerHasStation = false;
        boolean enemyHasStation = false;

        for (FleetMemberAPI curr : context.getPlayerFleet().getFleetData().getMembersListCopy()) {
            if (curr.isStation()) {
                playerHasStation = true;
                break;
            }
        }

        for (FleetMemberAPI curr : context.getOtherFleet().getFleetData().getMembersListCopy()) {
            if (curr.isStation()) {
                enemyHasStation = true;
                break;
            }
        }

        float planetYOffset = 0;

        if (playerHasStation) {
            planetYOffset = -bgHeight / 2f * 0.5f;
        }
        if (enemyHasStation) {
            planetYOffset = bgHeight / 2f * 0.5f;
        }


        float f = (maxDist - dist) / maxDist * 0.65f + 0.35f;
        float radius = baseRadius * f * scaleFactor;
        if (radius > maxRadius) radius = maxRadius;
        if (radius < minRadius) radius = minRadius;
        loader.setPlanetBgSize(bgWidth * f, bgHeight * f);
        loader.addPlanet(0f, planetYOffset, radius, planet, 0f, true);
    }

    protected void addMultiplePlanets() {
        float bgWidth = 2048f;
        float bgHeight = 2048f;
        loader.setPlanetBgSize(bgWidth, bgHeight);


        List<NearbyPlanetData> planets = getNearbyPlanets(context.getPlayerFleet());
        if (!planets.isEmpty()) {
            float maxDist = PLANET_MAX_DIST;
            for (NearbyPlanetData data : planets) {
                float dist = Vector2f.sub(context.getPlayerFleet().getLocation(), data.planet.getLocation(), new Vector2f()).length();
                float baseRadius = data.planet.getRadius();
                float scaleFactor = 1.5f;
                float maxRadius = 500f;

                if (data.planet.isStar()) {
                    // skip stars in combat, bright and annoying
                    continue;
//					scaleFactor = 0.1f;
//					maxRadius = 50f;
                }

                float f = (maxDist - dist) / maxDist * 0.65f + 0.35f;
                float radius = baseRadius * f * scaleFactor;
                if (radius > maxRadius) radius = maxRadius;

                loader.addPlanet(data.offset.x * bgWidth / PLANET_AREA_WIDTH * scaleFactor,
                        data.offset.y * bgHeight / PLANET_AREA_HEIGHT * scaleFactor,
                        radius, data.planet.getTypeId(), 0f, true);
            }

        }
    }

    protected static String COMM = "comm_relay";
    protected static String SENSOR = "sensor_array";
    protected static String NAV = "nav_buoy";

    public String pickObjective(WeightedRandomPicker<String> nonCentral) {
        String pick = nonCentral.pick();
        nonCentral.remove(pick);
        return pick;
    }

    //For places close or at center which can have more strong objects spawn.
    public String pickObjectiveCentral(WeightedRandomPicker<String> nonCentral, WeightedRandomPicker<String> central) {
        WeightedRandomPicker<String> combined = new WeightedRandomPicker<>();

        combined.addAll(nonCentral);
        combined.addAll(central);

        String pick = combined.pick();
        combined.remove(pick);

        if (central.getItems().contains(pick)) {
            central.remove(pick);
        } else {
            nonCentral.remove(pick);
        }

        return pick;
    }

    public WeightedRandomPicker<String> getAvailableObjectives() {
        WeightedRandomPicker<String> objectivePicker = new WeightedRandomPicker<>();

        objectivePicker.add(SENSOR, 1f);
        objectivePicker.add(SENSOR, 1f);
        objectivePicker.add(NAV, 1f);
        objectivePicker.add(NAV, 1f);
        objectivePicker.add(COMM, 1f);
        objectivePicker.add("rat_deactivated_drone", 0.9f);
        objectivePicker.add("rat_deactivated_drone", 0.1f);

        return objectivePicker;
    }

    public WeightedRandomPicker<String> getAvailableCentralObjectives() {
        WeightedRandomPicker<String> objectivePicker = new WeightedRandomPicker<>();

        float chance = 0.2f;
        if (AbyssUtils.INSTANCE.getSystemData(Global.getSector().getPlayerFleet().getStarSystem()).getDepth() == AbyssDepth.Deep) {
            chance += 0.3f;
        }

        objectivePicker.add("rat_deactivated_drone_large", chance);

        return objectivePicker;
    }

    protected void addObjectives(MissionDefinitionAPI loader, int num, Random random) {

        WeightedRandomPicker<String> objectivePicker = getAvailableObjectives();
        WeightedRandomPicker<String> centralObjectivePicker = getAvailableCentralObjectives();

        if (num == 2) { // minimum is 3 now, so this shouldn't happen
            addObjectiveAt(0.25f, 0.5f, 0f, 0f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
            addObjectiveAt(0.75f, 0.5f, 0f, 0f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
        } else if (num == 3) {
            float r = random.nextFloat();
            if (r < 0.33f) {
                addObjectiveAt(0.25f, 0.7f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.25f, 0.3f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.5f, 1f, 1f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
            } else if (r < 0.67f) {
                addObjectiveAt(0.75f, 0.7f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.3f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.25f, 0.5f, 1f, 1f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
            } else {
                if (random.nextFloat() < 0.5f) {
                    addObjectiveAt(0.22f, 0.7f, 1f, 1f, pickObjective(objectivePicker), random);
                    addObjectiveAt(0.5f, 0.5f, 1f, 1f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
                    addObjectiveAt(0.78f, 0.3f, 1f, 1f, pickObjective(objectivePicker), random);
                } else {
                    addObjectiveAt(0.22f, 0.3f, 1f, 1f, pickObjective(objectivePicker), random);
                    addObjectiveAt(0.5f, 0.5f, 1f, 1f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
                    addObjectiveAt(0.78f, 0.7f, 1f, 1f, pickObjective(objectivePicker), random);
                }
            }
        } else if (num == 4) {
            float r = random.nextFloat();
            if (r < 0.33f) {
                addObjectiveAt(0.25f, 0.25f, 2f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.25f, 0.75f, 2f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.25f, 2f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.75f, 2f, 1f, pickObjective(objectivePicker), random);
            } else if (r < 0.67f) {
                addObjectiveAt(0.25f, 0.5f, 1f, 1f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
                addObjectiveAt(0.5f, 0.75f, 1f, 1f,  pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.5f, 1f, 1f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
                addObjectiveAt(0.5f, 0.25f, 1f, 1f, pickObjective(objectivePicker), random);
            } else {
                if (random.nextFloat() < 0.5f) {
                    addObjectiveAt(0.25f, 0.25f, 1f, 0f, pickObjective(objectivePicker), random);
                    addObjectiveAt(0.4f, 0.6f, 1f, 0f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
                    addObjectiveAt(0.6f, 0.4f, 1f, 0f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
                    addObjectiveAt(0.75f, 0.75f, 1f, 0f, pickObjective(objectivePicker), random);
                } else {
                    addObjectiveAt(0.25f, 0.75f, 1f, 0f, pickObjective(objectivePicker), random);
                    addObjectiveAt(0.4f, 0.4f, 1f, 0f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
                    addObjectiveAt(0.6f, 0.6f, 1f, 0f, pickObjectiveCentral(objectivePicker, centralObjectivePicker), random);
                    addObjectiveAt(0.75f, 0.25f, 1f, 0f, pickObjective(objectivePicker), random);
                }
            }
        }
    }



    protected void addEscapeObjectives(MissionDefinitionAPI loader, int num, Random random) {

        WeightedRandomPicker<String> objectivePicker = getAvailableObjectives();

        if (num == 2) {
            float r = random.nextFloat();
            if (r < 0.33f) {
                addObjectiveAt(0.25f, 0.25f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.75f, 1f, 1f, pickObjective(objectivePicker), random);
            } else if (r < 0.67f) {
                addObjectiveAt(0.75f, 0.25f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.25f, 0.75f, 1f, 1f, pickObjective(objectivePicker), random);
            } else {
                addObjectiveAt(0.5f, 0.25f, 4f, 2f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.5f, 0.75f, 4f, 2f, pickObjective(objectivePicker), random);
            }
        } else if (num == 3) {
            float r = random.nextFloat();
            if (r < 0.33f) {
                addObjectiveAt(0.25f, 0.75f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.25f, 0.25f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.5f, 1f, 6f, pickObjective(objectivePicker), random);
            } else if (r < 0.67f) {
                addObjectiveAt(0.25f, 0.5f, 1f, 6f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.75f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.25f, 1f, 1f, pickObjective(objectivePicker), random);
            } else {
                addObjectiveAt(0.5f, 0.25f, 4f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.5f, 0.5f, 4f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.5f, 0.75f, 4f, 1f, pickObjective(objectivePicker), random);
            }
        } else if (num == 4) {
            float r = random.nextFloat();
            if (r < 0.33f) {
                addObjectiveAt(0.25f, 0.25f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.25f, 0.75f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.25f, 1f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.75f, 0.75f, 1f, 1f, pickObjective(objectivePicker), random);
            } else if (r < 0.67f) {
                addObjectiveAt(0.35f, 0.25f, 2f, 0f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.65f, 0.35f, 2f, 0f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.5f, 0.6f, 4f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.5f, 0.8f, 4f, 1f, pickObjective(objectivePicker), random);
            } else {
                addObjectiveAt(0.65f, 0.25f, 2f, 0f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.35f, 0.35f, 2f, 0f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.5f, 0.6f, 4f, 1f, pickObjective(objectivePicker), random);
                addObjectiveAt(0.5f, 0.8f, 4f, 1f, pickObjective(objectivePicker), random);
            }
        }
    }

    protected void addObjectiveAt(float xMult, float yMult, float xOff, float yOff, String type, Random random) {
        //String type = pickAny();

        float minX = -width/2 + xPad;
        float minY = -height/2 + yPad;

        float x = (width - xPad * 2f) * xMult + minX;
        float y = (height - yPad * 2f) * yMult + minY;

        x = ((int) x / 1000) * 1000f;
        y = ((int) y / 1000) * 1000f;

        float offsetX = Math.round((random.nextFloat() - 0.5f) * xOff * 1f) * 1000f;
        float offsetY = Math.round((random.nextFloat() - 0.5f) * yOff * 1f) * 1000f;

//		offsetX = 0;
//		offsetY = 0;

        float xDir = (float) Math.signum(offsetX);
        float yDir = (float) Math.signum(offsetY);

        if (xDir == prevXDir && xOff > 0) {
            xDir = -xDir;
            offsetX = Math.abs(offsetX) * -prevXDir;
        }

        if (yDir == prevYDir && yOff > 0) {
            yDir = -yDir;
            offsetY = Math.abs(offsetY) * -prevYDir;
        }

        prevXDir = xDir;
        prevYDir = yDir;

        x += offsetX;
        y += offsetY;

        loader.addObjective(x, y, type);

        if (random.nextFloat() > 0.6f && loader.hasNebula()) {
            float nebulaSize = random.nextFloat() * 1500f + 500f;
            loader.addNebula(x, y, nebulaSize);
        }
    }



    protected float countNearbyAsteroids(CampaignFleetAPI playerFleet) {
        float numAsteroidsWithinRange = 0;
        LocationAPI loc = playerFleet.getContainingLocation();
        if (loc instanceof StarSystemAPI) {
            StarSystemAPI system = (StarSystemAPI) loc;
            List<SectorEntityToken> asteroids = system.getAsteroids();
            for (SectorEntityToken asteroid : asteroids) {
                float range = Vector2f.sub(playerFleet.getLocation(), asteroid.getLocation(), new Vector2f()).length();
                if (range < 300) numAsteroidsWithinRange ++;
            }
        }
        return numAsteroidsWithinRange;
    }

    protected static class NearbyPlanetData {
        protected Vector2f offset;
        protected PlanetAPI planet;
        public NearbyPlanetData(Vector2f offset, PlanetAPI planet) {
            this.offset = offset;
            this.planet = planet;
        }
    }

    protected static float PLANET_AREA_WIDTH = 2000;
    protected static float PLANET_AREA_HEIGHT = 2000;
    protected static float PLANET_MAX_DIST = (float) Math.sqrt(PLANET_AREA_WIDTH/2f * PLANET_AREA_WIDTH/2f + PLANET_AREA_HEIGHT/2f * PLANET_AREA_WIDTH/2f);

    protected static float SINGLE_PLANET_MAX_DIST = 1000f;

    protected List<NearbyPlanetData> getNearbyPlanets(CampaignFleetAPI playerFleet) {
        LocationAPI loc = playerFleet.getContainingLocation();
        List<NearbyPlanetData> result = new ArrayList<NearbyPlanetData>();
        if (loc instanceof StarSystemAPI) {
            StarSystemAPI system = (StarSystemAPI) loc;
            List<PlanetAPI> planets = system.getPlanets();
            for (PlanetAPI planet : planets) {
                float diffX = planet.getLocation().x - playerFleet.getLocation().x;
                float diffY = planet.getLocation().y - playerFleet.getLocation().y;

                if (Math.abs(diffX) < PLANET_AREA_WIDTH/2f && Math.abs(diffY) < PLANET_AREA_HEIGHT/2f) {
                    result.add(new NearbyPlanetData(new Vector2f(diffX, diffY), planet));
                }
            }
        }
        return result;
    }

    protected PlanetAPI getClosestPlanet(CampaignFleetAPI playerFleet) {
        LocationAPI loc = playerFleet.getContainingLocation();
        PlanetAPI closest = null;
        float minDist = Float.MAX_VALUE;
        if (loc instanceof StarSystemAPI) {
            StarSystemAPI system = (StarSystemAPI) loc;
            List<PlanetAPI> planets = system.getPlanets();
            for (PlanetAPI planet : planets) {
                if (planet.isStar()) continue;
                if (Planets.PLANET_LAVA.equals(planet.getTypeId())) continue;
                if (Planets.PLANET_LAVA_MINOR.equals(planet.getTypeId())) continue;
                if (planet.getSpec().isDoNotShowInCombat()) continue;

                float dist = Vector2f.sub(context.getPlayerFleet().getLocation(), planet.getLocation(), new Vector2f()).length();
                if (dist < minDist && dist < SINGLE_PLANET_MAX_DIST) {
                    closest = planet;
                    minDist = dist;
                }
            }
        }
        return closest;
    }
}