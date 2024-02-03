package assortment_of_things;

import com.fs.starfarer.api.EveryFrameScript;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.impl.campaign.fleets.PersonalFleetHoracioCaden;
import com.fs.starfarer.api.impl.campaign.fleets.PersonalFleetScript;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;
import com.fs.starfarer.api.impl.campaign.procgen.NameGenData;
import com.fs.starfarer.api.impl.campaign.procgen.ProcgenUsedNames;
import lunalib.lunaSettings.LunaSettings;
import lunalib.lunaUtil.LunaCommons;
import org.lwjgl.util.vector.Vector2f;

import java.util.Iterator;
import java.util.List;

public class Runcodes {

    public void Example() {

        SectorAPI sector = Global.getSector();


        ProcgenUsedNames.pickName(NameGenData.TAG_STAR, null, null);

        if (sector.hasScript(PersonalFleetHoracioCaden.class)) {
            PersonalFleetScript fleetScript = null;

            for (EveryFrameScript script : Global.getSector().getScripts()) {
                if (script instanceof PersonalFleetHoracioCaden) {
                    fleetScript = (PersonalFleetHoracioCaden) script;
                }
            }

            if (fleetScript != null) {
                fleetScript.getFleet().despawn();
                sector.removeScript(fleetScript);
            }
        }
    }

    public static JumpPointAPI findNearestGravityWell(CampaignFleetAPI var1, LocationAPI var2) {
        List var3 = var2.getEntities(JumpPointAPI.class);
        float var4 = Float.MAX_VALUE;
        JumpPointAPI var5 = null;
        Iterator var7 = var3.iterator();

        while (true) {
            JumpPointAPI var6;
            StarSystemAPI var8;
            do {
                do {
                    if (!var7.hasNext()) {
                        return var5;
                    }

                    var6 = (JumpPointAPI) var7.next();
                    var8 = var6.getDestinationStarSystem();
                } while (var8.hasTag("system_abyssal"));
            } while (!var6.isStarAnchor() && !var6.isGasGiantAnchor());

            if (!var6.getDestinations().isEmpty()) {
                float var9 = Vector2f.sub(var1.getLocation(), var6.getLocation(), new Vector2f()).length();
                if (var9 < var4) {
                    var5 = var6;
                    var4 = var9;
                }
            }
        }
    }
}
