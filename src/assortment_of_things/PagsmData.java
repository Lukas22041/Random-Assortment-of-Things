package assortment_of_things;

import com.fs.starfarer.api.Global;

public class PagsmData {

    public static String pagsmDataMemoryKey = "$pagsmDataClass";

    public static PagsmData getData() {
        PagsmData data = (PagsmData) Global.getSector().getMemoryWithoutUpdate().get(pagsmDataMemoryKey);
        if (data == null) {
            data = new PagsmData();
            Global.getSector().getMemoryWithoutUpdate().set(pagsmDataMemoryKey, data);
        }
        return data;
    }

    boolean allowOmega = false;
    boolean allowDaemon = false;
    boolean allowArchDaemon = false;
    boolean allowAbyssCore = false;

}
