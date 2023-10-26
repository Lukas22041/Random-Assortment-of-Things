package assortment_of_things.misc;

import com.fs.starfarer.api.campaign.CampaignTerrainAPI;
import com.fs.starfarer.api.campaign.RingBandAPI;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.campaign.StarSystemAPI;
import com.fs.starfarer.api.impl.campaign.ids.Tags;
import com.fs.starfarer.api.impl.campaign.ids.Terrain;
import com.fs.starfarer.api.impl.campaign.procgen.AccretionDiskGenPlugin;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.TerrainGenDataSpec;
import com.fs.starfarer.api.impl.campaign.terrain.BaseRingTerrain;
import com.fs.starfarer.api.impl.campaign.terrain.RingSystemTerrainPlugin;
import com.fs.starfarer.api.util.WeightedRandomPicker;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class RATAccretionDiskGen {
    public static class TexAndIndex {
        public String tex;
        public int index;
    }

    protected AccretionDiskGenPlugin.TexAndIndex getTexAndIndex() {
        AccretionDiskGenPlugin.TexAndIndex result = new AccretionDiskGenPlugin.TexAndIndex();
        WeightedRandomPicker<Integer> indexPicker = new WeightedRandomPicker<Integer>(StarSystemGenerator.random);

        WeightedRandomPicker<String> ringSet = new WeightedRandomPicker<String>(StarSystemGenerator.random);
        ringSet.add("ring_ice", 10f);
        ringSet.add("ring_dust", 10f);
        //ringSet.add("ring_special", 1f);

        String set = ringSet.pick();

        if (set.equals("ring_ice")) {
            result.tex = "rings_ice0";
            indexPicker.add(0);
            indexPicker.add(1);
        } else if (set.equals("ring_dust")) {
            result.tex = "rings_dust0";
            indexPicker.add(0);
            indexPicker.add(1);
        }

        result.index = indexPicker.pick();

        return result;
    }


    public StarSystemGenerator.GenResult generate(SectorEntityToken parent, Float orbitRadius) {
        StarSystemAPI system = parent.getStarSystem();

        //float orbitRadius = context.currentRadius * (2f + 2f * StarSystemGenerator.random.nextFloat());
        ///float orbitRadius = context.currentRadius * (2f + 2f * StarSystemGenerator.random.nextFloat());

        float bandWidth = 256f;

        //int numBands = (int) (2f + StarSystemGenerator.random.nextFloat() * 5f);
        int numBands = 8;

        float spiralFactor = 3f + StarSystemGenerator.random.nextFloat() * 2f;
        numBands += (int) spiralFactor;

        numBands = 12;
//		boolean leaveRoomInMiddle = context.system.getStar() != null &&
//									parent == context.system.getCenter() &&
//									Misc.getDistance(context.system.getStar().getLocation(), parent.getLocation()) > 100;
        for (float i = 0; i < numBands; i++) {
//			float radiusMult = 0.25f + 0.75f * (i + 1f) / (numBands);
//			radiusMult = 1f;
            //float radius = orbitRadius * radiusMult;
            float radius = orbitRadius - i * bandWidth * 0.25f - i * bandWidth * 0.1f;
            //float radius = orbitRadius - i * bandWidth / 2;

            AccretionDiskGenPlugin.TexAndIndex tex = getTexAndIndex();
            float orbitDays = radius / (30f + 10f * StarSystemGenerator.random.nextFloat());
            Color color = StarSystemGenerator.getColor(system.getLightColor().darker(), system.getLightColor());
            //color = Color.white;
            RingBandAPI visual = system.addRingBand(parent, "misc", tex.tex, 256f, tex.index, color, bandWidth,
                    radius + bandWidth / 2f, -orbitDays);

            spiralFactor = 2f + StarSystemGenerator.random.nextFloat() * 5f;
            visual.setSpiral(true);
            visual.setMinSpiralRadius(0);
            visual.setSpiralFactor(spiralFactor);
        }


        List<SectorEntityToken> rings = new ArrayList<SectorEntityToken>();
        SectorEntityToken ring = system.addTerrain(Terrain.RING, new BaseRingTerrain.RingParams(orbitRadius, orbitRadius / 2f, parent, null));
        ring.addTag(Tags.ACCRETION_DISK);
        if (((CampaignTerrainAPI)ring).getPlugin() instanceof RingSystemTerrainPlugin) {
            ((RingSystemTerrainPlugin)((CampaignTerrainAPI)ring).getPlugin()).setNameForTooltip("Accretion Disk");
        }

        ring.setCircularOrbit(parent, 0, 0, -100);

        rings.add(ring);

        StarSystemGenerator.GenResult result = new StarSystemGenerator.GenResult();
        result.onlyIncrementByWidth = false;
        result.orbitalWidth = orbitRadius;
        result.entities.addAll(rings);
        return result;
    }



}
