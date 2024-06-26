package assortment_of_things;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.MarketAPI;
import com.fs.starfarer.api.characters.OfficerDataAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.AICoreAdminPluginImpl;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Factions;

import java.util.Random;

public class Runcodes {

    public void Example() {

        SectorEntityToken gateHauler = null;


        for (StarSystemAPI system : Global.getSector().getStarSystems()) {
            for (CustomCampaignEntityAPI entity : system.getCustomEntities()) {
                if (entity.getCustomEntityType().equals("derelict_gatehauler")) {
                    gateHauler = entity;
                    break;
                }
            }
        }

        if (gateHauler != null) {

        }



    }


}
