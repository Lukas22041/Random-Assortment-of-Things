package assortment_of_things.scripts;

import assortment_of_things.campaign.items.cores.AICoreUtil;
import assortment_of_things.strings.RATItems;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.CampaignEventListener.FleetDespawnReason;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.fleet.FleetMemberAPI;
import com.fs.starfarer.api.impl.campaign.enc.EncounterManager;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPoint;
import com.fs.starfarer.api.impl.campaign.enc.EncounterPointProvider;
import com.fs.starfarer.api.impl.campaign.fleets.FleetFactoryV3;
import com.fs.starfarer.api.impl.campaign.fleets.FleetParamsV3;
import com.fs.starfarer.api.impl.campaign.fleets.SourceBasedFleetManager;
import com.fs.starfarer.api.impl.campaign.ids.*;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantAssignmentAI;
import com.fs.starfarer.api.impl.campaign.procgen.themes.RemnantSeededFleetManager;
import org.lazywizard.lazylib.MathUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


//Just a copy of the Script that spawns fleets for Remnant bases.
public class ChiralBaseFleetManager extends SourceBasedFleetManager {

    public class UnknownSystemEPGenerator implements EncounterPointProvider {
        public List<EncounterPoint> generateEncounterPoints(LocationAPI where) {
            if (!where.isHyperspace()) return null;
            if (totalLost > 0 && source != null) {
                String id = "ep_" + source.getId();
                EncounterPoint ep = new EncounterPoint(id, where, source.getLocationInHyperspace(), EncounterManager.EP_TYPE_OUTSIDE_SYSTEM);
                ep.custom = this;
                List<EncounterPoint> result = new ArrayList<EncounterPoint>();
                result.add(ep);
                return result;//source.getContainingLocation().getName()
            }
            return null;
        }
    }

    int minPts;
    int maxPts;
    int totalLost;
    String factionId;
    protected transient UnknownSystemEPGenerator epGen;

    public ChiralBaseFleetManager(SectorEntityToken source, float thresholdLY, int minFleets, int maxFleets, float respawnDelay,
                                  int minPts, int maxPts, String factionId) {
        super(source, thresholdLY, minFleets, maxFleets, respawnDelay);
        this.minPts = minPts;
        this.maxPts = maxPts;
        this.factionId = factionId;
    }

    protected Object readResolve() {
        return this;
    }

    protected transient boolean addedListener = false;
    @Override
    public void advance(float amount) {
        if (!addedListener) {
            //totalLost = 1;
			/* best code ever -dgb
			if (Global.getSector().getPlayerPerson() != null &&
					Global.getSector().getPlayerPerson().getNameString().equals("Dave Salvage") &&
					Global.getSector().getClock().getDay() == 15 &&
							Global.getSector().getClock().getMonth() == 12 &&
							Global.getSector().getClock().getCycle() == 206) {
				totalLost = 0;
			}*/
            // global listener needs to be not this class since SourceBasedFleetManager
            // adds it to all fleets as their event listener
            // and so you'd get reportFleetDespawnedToListener() called multiple times
            // from global listeners, and from fleet ones
            epGen = new UnknownSystemEPGenerator();
            Global.getSector().getListenerManager().addListener(epGen, true);
            addedListener = true;
        }
        super.advance(amount);
    }


    @Override
    protected CampaignFleetAPI spawnFleet() {
        if (source == null) return null;

        Random random = new Random();

        int combatPoints = MathUtils.getRandomNumberInRange(minPts, maxPts);

        FleetParamsV3 params = new FleetParamsV3(
                source.getMarket(),
                source.getLocationInHyperspace(),
                factionId,
                3f,
                FleetTypes.PATROL_MEDIUM,
                combatPoints, // combatPts
                0f, // freighterPts
                0f, // tankerPts
                0f, // transportPts
                0f, // linerPts
                0f, // utilityPts
                0f // qualityMod
        );
        //params.officerNumberBonus = 10;
        params.random = random;
        params.withOfficers = false;
        params.averageSMods = 0;
       // params.averageSMods = 1;

        CampaignFleetAPI fleet = FleetFactoryV3.createFleet(params);
        if (fleet == null) return null;;

        fleet.addAbility(Abilities.TRANSPONDER);
        fleet.getAbility(Abilities.TRANSPONDER).activate();
        LocationAPI location = source.getContainingLocation();
        location.addEntity(fleet);

        RemnantSeededFleetManager.initRemnantFleetProperties(random, fleet, false);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_AGGRESSIVE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_MAKE_HOSTILE, true);
        fleet.getMemoryWithoutUpdate().set(MemFlags.MEMORY_KEY_NO_JUMP, true);
        fleet.setLocation(source.getLocation().x, source.getLocation().y);
        fleet.setFacing(random.nextFloat() * 360f);

        fleet.addScript(new RemnantAssignmentAI(fleet, (StarSystemAPI) source.getContainingLocation(), source));
        fleet.getMemoryWithoutUpdate().set("$sourceId", source.getId());


        for (FleetMemberAPI member : fleet.getFleetData().getMembersInPriorityOrder())
        {
            int rng = MathUtils.getRandomNumberInRange(0, 2);

            if (rng == 0)
            {
                PersonAPI core = AICoreUtil.createCorePerson(RATItems.INSTANCE.getSCARLET_PROCESSOR(), factionId);
                core.getStats().setLevel(3);
                core.setPersonality(Personalities.RECKLESS);
                core.setRankId(Ranks.SPACE_CAPTAIN);

                core.getStats().setSkillLevel(Skills.HELMSMANSHIP, 2F);
                core.getStats().setSkillLevel(Skills.GUNNERY_IMPLANTS, 2F);
                core.getStats().setSkillLevel(Skills.TARGET_ANALYSIS, 2F);

                core.getMemoryWithoutUpdate().set(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT, 2f);

                member.setCaptain(core);
            }
            if (rng == 1)
            {
                PersonAPI core = AICoreUtil.createCorePerson(RATItems.INSTANCE.getAZURE_PROCESSOR(), factionId);
                core.getStats().setLevel(3);
                core.setPersonality(Personalities.CAUTIOUS);
                core.setRankId(Ranks.SPACE_CAPTAIN);

                core.getStats().setSkillLevel(Skills.FIELD_MODULATION, 2F);
                core.getStats().setSkillLevel(Skills.DAMAGE_CONTROL, 2F);
                core.getStats().setSkillLevel(Skills.POINT_DEFENSE, 2F);

                core.getMemoryWithoutUpdate().set(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT, 2f);

                member.setCaptain(core);
            }
            if (rng == 2)
            {
                PersonAPI core = AICoreUtil.createCorePerson(RATItems.INSTANCE.getAMBER_PROCESSOR(), factionId);
                core.getStats().setLevel(3);
                core.setPersonality(Personalities.STEADY);
                core.setRankId(Ranks.SPACE_CAPTAIN);

                core.getStats().setSkillLevel(Skills.SYSTEMS_EXPERTISE, 2F);
                core.getStats().setSkillLevel(Skills.ORDNANCE_EXPERTISE, 2F);
                core.getStats().setSkillLevel(Skills.POLARIZED_ARMOR, 2F);

                core.getMemoryWithoutUpdate().set(AICoreOfficerPlugin.AUTOMATED_POINTS_MULT, 2f);

                member.setCaptain(core);
            }
        }

        return fleet;
    }


    @Override
    public void reportFleetDespawnedToListener(CampaignFleetAPI fleet, FleetDespawnReason reason, Object param) {
        super.reportFleetDespawnedToListener(fleet, reason, param);
        if (reason == FleetDespawnReason.DESTROYED_BY_BATTLE) {
            String sid = fleet.getMemoryWithoutUpdate().getString("$sourceId");
            if (sid != null && source != null && sid.equals(source.getId())) {
                //if (sid != null && sid.equals(source.getId())) {
                totalLost++;
            }
        }
    }

    public int getTotalLost() {
        return totalLost;
    }


}




