package assortment_of_things;


import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.ShipAPI;
import com.fs.starfarer.api.combat.WeaponAPI;
import com.fs.starfarer.api.combat.listeners.DamageListener;
import com.fs.starfarer.api.loading.BeamWeaponSpecAPI;
import com.fs.starfarer.api.loading.WeaponSpecAPI;
import kotlin.jvm.JvmSerializableLambda;
import org.lwjgl.util.vector.Vector2f;

import java.util.List;

public class Runcodes  {

    public void Example() {
        ShipAPI ship = null;

        for (WeaponAPI weapon : ship.getAllWeapons()) {
            weapon.ensureClonedSpec();
            WeaponSpecAPI spec = weapon.getSpec();

            if (spec instanceof BeamWeaponSpecAPI) { //Only execute if the weapons spec is that of a beam
                //Cast the spec variable to a BeamWeaponSpecAPI, which has more methods related to beams.
                BeamWeaponSpecAPI beamSpec = (BeamWeaponSpecAPI) spec;
                beamSpec.setBeamSpeed(100000f);
            }
        }
    }

}
