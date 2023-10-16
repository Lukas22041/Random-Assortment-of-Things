package assortment_of_things;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.impl.campaign.rulecmd.AddRemoveCommodity;
import com.fs.starfarer.api.impl.campaign.rulecmd.BaseCommandPlugin;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireAll;
import com.fs.starfarer.api.impl.campaign.rulecmd.FireBest;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.util.*;

// Thank you Lukas for all your support!

public class sfckweenturnin extends BaseCommandPlugin {

    public boolean turnin;

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected PersonAPI person;
    protected FactionAPI faction;

    protected float valueMult;
    protected float repMult;

    public static class ItemTradeData {

        String id;
        Float credits;
        Float rep;

        public ItemTradeData(String id, Float credits, Float rep) {
            this.id = id;
            this.credits = credits;
            this.rep = rep;
        }
    }

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);


        if (command == null) return false;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        person = dialog.getInteractionTarget().getActivePerson();
        faction = person.getFaction();

        //buysAICores = faction.getCustomBoolean("buysAICores");
        valueMult = 2f;
        repMult = 2f;
        switch (command) {
            case "selectItems":
                selectItems();
                break;
            case "playerHasAllowedItem":
                return playerHasAllowedItem();
            case "contributedItem":
                String id = (String) params.get(1).getObject(memoryMap);
                Float amount = (Float) params.get(2).getObject(memoryMap);
                return playerContributedItem(id, amount);
            default:
                break;
        }

        return true;
    }

    public static List<ItemTradeData> getAllowedItems() {
        boolean allowOmega = Global.getSector().getMemoryWithoutUpdate().getBoolean("$showedOmegaCore");
        boolean allowDaemon = Global.getSector().getMemoryWithoutUpdate().getBoolean("$sfcDaemonCore_completed");
        boolean allowArchDaemon = Global.getSector().getMemoryWithoutUpdate().getBoolean("$sfc_gaveKweenArchDaemon");
        boolean allowAbyssCore = Global.getSector().getMemoryWithoutUpdate().getBoolean("$knowsAbyssData");

        List<ItemTradeData> allowedItems = new ArrayList<>();

        //ID, Price, Rep
        allowedItems.add(new ItemTradeData(Commodities.GAMMA_CORE, 10000f, 0.25f));
        allowedItems.add(new ItemTradeData(Commodities.BETA_CORE, 30000f, 0.5f));
        allowedItems.add(new ItemTradeData(Commodities.ALPHA_CORE, 100000f, 1f));

        if (allowOmega) {
            allowedItems.add(new ItemTradeData(Commodities.OMEGA_CORE, 500000f, 5f));
        }
        if (allowAbyssCore) {
            allowedItems.add(new ItemTradeData("rat_chronos_core", 50000f, 0.75f));
            allowedItems.add(new ItemTradeData("rat_cosmos_core", 50000f, 0.75f));
            allowedItems.add(new ItemTradeData("rat_seraph_core", 100000f, 1f));
            //allowedItems.add(new ItemTradeData("rat_neuro_core", 200000f, 2f));
        }
        if (allowDaemon) {
            allowedItems.add(new ItemTradeData("tahlan_daemoncore", 50000f, 0.5f));
        }
        if (allowArchDaemon) {
            allowedItems.add(new ItemTradeData("tahlan_archdaemoncore", 100000f, 1f));
        }
        return allowedItems;
    }

    public static boolean playerContributedItem(String id, Float amount) {
        if (getContributedItem(id) >= amount) {
            return true;
        }
        return false;
    }

    public static boolean isAllowedItem(CargoStackAPI stack) {

        CommoditySpecAPI commoditySpec = stack.getResourceIfResource();
        if (commoditySpec != null) {
            for (ItemTradeData item : getAllowedItems()) {
                if (item.id.equals(commoditySpec.getId())) {
                    return true;
                }
            }
        }

        SpecialItemSpecAPI specialSpec = stack.getSpecialItemSpecIfSpecial();
        if (specialSpec != null) {
            for (ItemTradeData item : getAllowedItems()) {
                if (item.id.equals(specialSpec.getId())) {
                    return true;
                }
            }
        }
        return false;

    }

    protected boolean playerHasAllowedItem() {
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            if (isAllowedItem(stack)) {
                return true;
            }
        }
        return false;
    }

    protected void selectItems() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (isAllowedItem(stack)) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select AI Cores to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
            @Override
            public void pickedCargo(CargoAPI cargo) {
                cargo.sort();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
                    if (stack.isCommodityStack()) { // should be always, but just in case
                        AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
                        addToContributedItems(stack.getCommodityId(), stack.getSize());
                    }
                    if (stack.isSpecialStack()) {
                        AddRemoveCommodity.addItemLossText(stack.getSpecialDataIfSpecial(), (int) stack.getSize(), text);
                        addToContributedItems(stack.getSpecialItemSpecIfSpecial().getId(), stack.getSize());
                    }
                }

                float bounty = computeCoreCreditValue(cargo);
                float repChange = computeCoreReputationValue(cargo);

                if (bounty > 0) {
                    playerCargo.getCredits().add(bounty);
                    AddRemoveCommodity.addCreditsGainText((int)bounty, text);
                }

                if (repChange >= 1f) {
                    CustomRepImpact impact = new CustomRepImpact();
                    impact.delta = repChange * 0.01f;
                    if (impact.delta >= 0.01f) {
                        Global.getSector().adjustPlayerReputation(
                                new RepActionEnvelope(RepActions.CUSTOM, impact,
                                        null, text, true),
                                person);
                        impact.delta *= 0.25f;
                    }
                }
                if (bounty == 0 || repChange < 1f) {
                    FireAll.fire(null, dialog, memoryMap, "AICoresTurnedInKweenLow");
                } else {
                    FireBest.fire(null, dialog, memoryMap, "AICoresTurnedInKween");}
            }
            @Override
            public void cancelledCargoSelection() {
            }
            @Override
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

                float bounty = computeCoreCreditValue(combined);
                float repChange = computeCoreReputationValue(combined);

                float pad = 3f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst("Sindrian Fuel?"), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();
                panel.addImage(faction.getLogo(), width * 1f, 3f);
                panel.addPara("Compared to dealing with others, turning AI Cores into Yunris Kween will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                panel.addToGrid(0, 0, "Bounty value", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Relation gain", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected AI Cores, you will receive a %s bounty reward " +
                                "and your relations with Yunris Kween will improve by %s points. ",
                        opad * 1f, Misc.getHighlightColor(),
                        Misc.getWithDGS(bounty) + Strings.C,
                        "" + (int) repChange);
            }
        });
    }


    public static void addToContributedItems(String id, Float amount) {
        HashMap<String, Float> contributions = getContributedItems();
        if (contributions.get(id) == null) {
            contributions.put(id, 0f);
        }
        contributions.put(id, contributions.get(id) + amount);
    }

    public static Float getContributedItem(String id) {
        HashMap<String, Float> contributions = getContributedItems();
        if (contributions.get(id) == null) {
            return 0f;
        }
        return contributions.get(id);
    }

    public static HashMap<String, Float> getContributedItems() {
        String key = "$kweenTurnedInItems";
        HashMap<String, Float> contributions = (HashMap<String, Float>) Global.getSector().getMemoryWithoutUpdate().get(key);
        if (contributions == null) {
            contributions = new HashMap<>();
            Global.getSector().getMemoryWithoutUpdate().set(key, contributions);
        }
        return contributions;
    }

    protected float computeCoreCreditValue(CargoAPI cargo) {
        float bounty = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {

            String id = null;
            CommoditySpecAPI commoditySpec = stack.getResourceIfResource();
            if (commoditySpec != null) id = commoditySpec.getId();

            SpecialItemSpecAPI specialSpec = stack.getSpecialItemSpecIfSpecial();
            if (specialSpec != null) id = specialSpec.getId();

            if (id != null) {
                bounty += getBaseCreditValue(id) * stack.getSize();
            }
        }
        bounty *= valueMult;
        return bounty;
    }

    public static float getBaseCreditValue(String coreType) {

        Float value = 1f;

        for (ItemTradeData item : getAllowedItems()) {
            if (item.id.equals(coreType)) {
                value = item.credits;
            }
        }
        return value;
    }

    protected float computeCoreReputationValue(CargoAPI cargo) {
        float rep = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI commoditySpec = stack.getResourceIfResource();
            if (commoditySpec != null) {
                for (ItemTradeData item : getAllowedItems()) {
                    if (item.id.equals(commoditySpec.getId())) {
                        rep += item.rep * stack.getSize();
                    }
                }
            }

            SpecialItemSpecAPI specialSpec = stack.getSpecialItemSpecIfSpecial();
            if (specialSpec != null) {
                for (ItemTradeData item : getAllowedItems()) {
                    if (item.id.equals(specialSpec.getId())) {
                        rep += item.rep * stack.getSize();
                    }
                }
            }

        }
        rep *= repMult;
        return rep;
    }
}

// Previous Version
/*
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.campaign.*;
import com.fs.starfarer.api.campaign.econ.CommoditySpecAPI;
import com.fs.starfarer.api.campaign.rules.MemoryAPI;
import com.fs.starfarer.api.characters.PersonAPI;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.CustomRepImpact;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActionEnvelope;
import com.fs.starfarer.api.impl.campaign.CoreReputationPlugin.RepActions;
import com.fs.starfarer.api.impl.campaign.ids.Commodities;
import com.fs.starfarer.api.impl.campaign.ids.Strings;
import com.fs.starfarer.api.ui.TooltipMakerAPI;
import com.fs.starfarer.api.util.Misc;
import com.fs.starfarer.api.util.Misc.Token;

import java.util.*;

// more things borrowed from smarter modders

public class sfckweenturnin extends BaseCommandPlugin {

    public boolean turnin;

    protected CampaignFleetAPI playerFleet;
    protected SectorEntityToken entity;
    protected FactionAPI playerFaction;
    protected FactionAPI entityFaction;
    protected TextPanelAPI text;
    protected OptionPanelAPI options;
    protected CargoAPI playerCargo;
    protected MemoryAPI memory;
    protected InteractionDialogAPI dialog;
    protected Map<String, MemoryAPI> memoryMap;
    protected PersonAPI person;
    protected FactionAPI faction;

    protected float valueMult;
    protected float repMult;

    public static class ItemTradeData {

        String id;
        Float credits;
        Float rep;

        public ItemTradeData(String id, Float credits, Float rep) {
            this.id = id;
            this.credits = credits;
            this.rep = rep;
        }
    }

    public boolean execute(String ruleId, InteractionDialogAPI dialog, List<Token> params, Map<String, MemoryAPI> memoryMap) {

        this.dialog = dialog;
        this.memoryMap = memoryMap;

        String command = params.get(0).getString(memoryMap);
        if (command == null) return false;

        memory = getEntityMemory(memoryMap);

        entity = dialog.getInteractionTarget();
        text = dialog.getTextPanel();
        options = dialog.getOptionPanel();

        playerFleet = Global.getSector().getPlayerFleet();
        playerCargo = playerFleet.getCargo();

        playerFaction = Global.getSector().getPlayerFaction();
        entityFaction = entity.getFaction();

        person = dialog.getInteractionTarget().getActivePerson();
        faction = person.getFaction();



        //buysAICores = faction.getCustomBoolean("buysAICores");
        valueMult = 2f;
        repMult = 2f;
        switch (command) {
            case "selectItems":
                selectItems();
                break;
            case "playerHasAllowedItem":
                return playerHasAllowedItem();
            default:
                break;
        }

        return true;
    }

    public static List<ItemTradeData> getAllowedItems() {
        boolean allowOmega = Global.getSector().getMemoryWithoutUpdate().getBoolean("$showedOmegaCore");
        boolean allowDaemon = Global.getSector().getMemoryWithoutUpdate().getBoolean("$sfcDaemonCore_completed");
        boolean allowArchDaemon = Global.getSector().getMemoryWithoutUpdate().getBoolean("$sfc_gaveKweenArchDaemon");
        boolean allowAbyssCore = Global.getSector().getMemoryWithoutUpdate().getBoolean("$knowsAbyssData");

        List<ItemTradeData> allowedItems = new ArrayList<>();

        //ID, Price, Rep
        allowedItems.add(new ItemTradeData(Commodities.GAMMA_CORE, 10000f, 0.25f));
        allowedItems.add(new ItemTradeData(Commodities.BETA_CORE, 30000f, 0.5f));
        allowedItems.add(new ItemTradeData(Commodities.ALPHA_CORE, 100000f, 1f));

        if (allowOmega) {
            allowedItems.add(new ItemTradeData(Commodities.OMEGA_CORE, 500000f, 5f));
        }
        if (allowAbyssCore) {
            allowedItems.add(new ItemTradeData("rat_chronos_core", 50000f, 0.75f));
            allowedItems.add(new ItemTradeData("rat_cosmos_core", 50000f, 0.75f));
            allowedItems.add(new ItemTradeData("rat_primordial_core", 100000f, 1f));
            //allowedItems.add(new ItemTradeData("rat_neuro_core", 200000f, 2f));
        }
        if (allowDaemon) {
            allowedItems.add(new ItemTradeData("tahlan_daemoncore", 50000f, 0.5f));
        }
        if (allowArchDaemon) {
            allowedItems.add(new ItemTradeData("tahlan_archdaemoncore", 100000f, 1f));
        }
        return allowedItems;
    }

    public static boolean isAllowedItem(CargoStackAPI stack) {

        CommoditySpecAPI commoditySpec = stack.getResourceIfResource();
        if (commoditySpec != null) {
            for (ItemTradeData item : getAllowedItems()) {
                if (item.id.equals(commoditySpec.getId())) {
                    return true;
                }
            }
        }

        SpecialItemSpecAPI specialSpec = stack.getSpecialItemSpecIfSpecial();
        if (specialSpec != null) {
            for (ItemTradeData item : getAllowedItems()) {
                if (item.id.equals(specialSpec.getId())) {
                    return true;
                }
            }
        }

        return false;

    }

    protected boolean playerHasAllowedItem() {
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            if (isAllowedItem(stack)) {
                return true;
            }
        }
        return false;
    }

    protected void selectItems() {
        CargoAPI copy = Global.getFactory().createCargo(false);
        for (CargoStackAPI stack : playerCargo.getStacksCopy()) {
            CommoditySpecAPI spec = stack.getResourceIfResource();
            if (isAllowedItem(stack)) {
                copy.addFromStack(stack);
            }
        }
        copy.sort();

        final float width = 310f;
        dialog.showCargoPickerDialog("Select AI Cores to turn in", "Confirm", "Cancel", true, width, copy, new CargoPickerListener() {
            @Override
            public void pickedCargo(CargoAPI cargo) {
                cargo.sort();
                for (CargoStackAPI stack : cargo.getStacksCopy()) {
                    playerCargo.removeItems(stack.getType(), stack.getData(), stack.getSize());
                    if (stack.isCommodityStack()) { // should be always, but just in case
                        AddRemoveCommodity.addCommodityLossText(stack.getCommodityId(), (int) stack.getSize(), text);
                    }
                    if (stack.isSpecialStack()) {
                        AddRemoveCommodity.addItemLossText(stack.getSpecialDataIfSpecial(), (int) stack.getSize(), text);
                    }
                }

                float bounty = computeCoreCreditValue(cargo);
                float repChange = computeCoreReputationValue(cargo);

                if (bounty > 0) {
                    playerCargo.getCredits().add(bounty);
                    AddRemoveCommodity.addCreditsGainText((int)bounty, text);
                }

                if (repChange >= 1f) {
                    CustomRepImpact impact = new CustomRepImpact();
                    impact.delta = repChange * 0.01f;
                    if (impact.delta >= 0.01f) {
                        Global.getSector().adjustPlayerReputation(
                                new RepActionEnvelope(RepActions.CUSTOM, impact,
                                        null, text, true),
                                person);
                        impact.delta *= 0.25f;
                    }
                }
                if (bounty == 0 || repChange < 1f) {
                    FireAll.fire(null, dialog, memoryMap, "AICoresTurnedInKweenLow");
                } else {FireBest.fire(null, dialog, memoryMap, "AICoresTurnedInKween");}
            }
            @Override
            public void cancelledCargoSelection() {
            }
            @Override
            public void recreateTextPanel(TooltipMakerAPI panel, CargoAPI cargo, CargoStackAPI pickedUp, boolean pickedUpFromSource, CargoAPI combined) {

                float bounty = computeCoreCreditValue(combined);
                float repChange = computeCoreReputationValue(combined);

                float pad = 3f;
                float opad = 10f;

                panel.setParaFontOrbitron();
                panel.addPara(Misc.ucFirst("Sindrian Fuel?"), faction.getBaseUIColor(), 1f);
                panel.setParaFontDefault();
                panel.addImage(faction.getLogo(), width * 1f, 3f);
                panel.addPara("Compared to dealing with others, turning AI Cores into Yunris Kween will result in: ", opad);
                panel.beginGridFlipped(width, 1, 40f, 10f);
                panel.addToGrid(0, 0, "Bounty value", "" + (int)(valueMult * 100f) + "%");
                panel.addToGrid(0, 1, "Relation gain", "" + (int)(repMult * 100f) + "%");
                panel.addGrid(pad);

                panel.addPara("If you turn in the selected AI Cores, you will receive a %s bounty reward " +
                                "and your relations with Yunris Kween will improve by %s points. ",
                        opad * 1f, Misc.getHighlightColor(),
                        Misc.getWithDGS(bounty) + Strings.C,
                        "" + (int) repChange);
            }
        });
    }

    protected float computeCoreCreditValue(CargoAPI cargo) {
        float bounty = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {

            String id = null;
            CommoditySpecAPI commoditySpec = stack.getResourceIfResource();
            if (commoditySpec != null) id = commoditySpec.getId();

            SpecialItemSpecAPI specialSpec = stack.getSpecialItemSpecIfSpecial();
            if (specialSpec != null) id = specialSpec.getId();

            if (id != null) {
                bounty += getBaseCreditValue(id) * stack.getSize();
            }
        }
        bounty *= valueMult;
        return bounty;
    }

    public static float getBaseCreditValue(String coreType) {

        Float value = 1f;

        for (ItemTradeData item : getAllowedItems()) {
            if (item.id.equals(coreType)) {
                value = item.credits;
            }
        }

        return value;
    }

    protected float computeCoreReputationValue(CargoAPI cargo) {
        float rep = 0;
        for (CargoStackAPI stack : cargo.getStacksCopy()) {
            CommoditySpecAPI commoditySpec = stack.getResourceIfResource();
            if (commoditySpec != null) {
                for (ItemTradeData item : getAllowedItems()) {
                    if (item.id.equals(commoditySpec.getId())) {
                        rep += item.rep * stack.getSize();
                    }
                }
            }

            SpecialItemSpecAPI specialSpec = stack.getSpecialItemSpecIfSpecial();
            if (specialSpec != null) {
                for (ItemTradeData item : getAllowedItems()) {
                    if (item.id.equals(specialSpec.getId())) {
                        rep += item.rep * stack.getSize();
                    }
                }
            }

        }
        rep *= repMult;
        return rep;
    }
}
*/