package assortment_of_things;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.SectorEntityToken;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.impl.campaign.terrain.NebulaTerrainPlugin;
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI;
import com.fs.starfarer.api.loading.Description;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import kotlin.jvm.JvmSerializableLambda;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class Runcodes  {

    public void Example() {

    }

    public static Vector2f getTileCenter(NebulaTerrainPlugin terrain, SectorEntityToken target) {
        //Get location of the terrain
        float terrainX = terrain.getEntity().getLocation().x;
        float terrainY = terrain.getEntity().getLocation().y;

        //Get how large each individual cell is
        float size = terrain.getTileSize();

        //Get the width and height of the terrain in woorld coordinates
        float w = terrain.getTiles().length * size;
        float h = terrain.getTiles()[0].length * size;

        float offsetX = terrainX - w/2f;
        float offsetY = terrainY - h/2f;

        //Offset the targets coordinates space with the world coordinates of the grid, then divide by cell-size to get the index of the cell.
        int cellX = (int) ((target.getLocation().x - offsetX) / size);
        int cellY = (int) ((target.getLocation().y - offsetY) / size);

        float[] coords = terrain.getTileCenter(cellX, cellY);
        return new Vector2f(coords[0], coords[1]);
    }

}
