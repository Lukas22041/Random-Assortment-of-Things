package assortment_of_things.abyss.weapons;

import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.combat.CombatEngineAPI;
import com.fs.starfarer.api.combat.CombatEntityAPI;
import com.fs.starfarer.api.combat.DamagingProjectileAPI;
import com.fs.starfarer.api.combat.ProximityExplosionEffect;
import com.fs.starfarer.api.impl.combat.NegativeExplosionVisual;
import com.fs.starfarer.api.impl.combat.RiftCascadeEffect;
import com.fs.starfarer.api.loading.MissileSpecAPI;
import com.fs.starfarer.api.util.Misc;
import org.lwjgl.util.vector.Vector2f;

import java.awt.*;

public class GenesisFrigateDemMineExplosion implements ProximityExplosionEffect {

    public static String SIZE_MULT_KEY = "core_sizeMultKey";

    public void onExplosion(DamagingProjectileAPI explosion, DamagingProjectileAPI originalProjectile) {
        Float sizeMult = null;
        if (originalProjectile.getCustomData() != null) {
            sizeMult = (Float) originalProjectile.getCustomData().get(SIZE_MULT_KEY);
        }
        if (sizeMult == null) sizeMult = 1f;

        NegativeExplosionVisual.NEParams p = createStandardRiftParams("rat_genesis_frigate_dem_minelayer", 25f * sizeMult);
        p.fadeOut = 1f;
        //p.hitGlowSizeMult = 0.5f;
        spawnStandardRift(explosion, p);
    }

//	public static void spawnStandardRift(DamagingProjectileAPI explosion, String minelayerId, float baseRadius) {
//		NEParams p = createStandardRiftParams(minelayerId, baseRadius);
//		spawnStandardRift(explosion, p);
//	}

    public static void spawnStandardRift(DamagingProjectileAPI explosion, NegativeExplosionVisual.NEParams params) {
        CombatEngineAPI engine = Global.getCombatEngine();
        explosion.addDamagedAlready(explosion.getSource());

        CombatEntityAPI prev = null;
        for (int i = 0; i < 2; i++) {
            NegativeExplosionVisual.NEParams p = params;
            p.radius *= 0.75f + 0.5f * (float) Math.random();

            p.withHitGlow = prev == null;

            Vector2f loc = new Vector2f(explosion.getLocation());
            //loc = Misc.getPointWithinRadius(loc, p.radius * 1f);
            loc = Misc.getPointAtRadius(loc, p.radius * 0.4f);

            CombatEntityAPI e = engine.addLayeredRenderingPlugin(new NegativeExplosionVisual(p));
            e.getLocation().set(loc);

            if (prev != null) {
                float dist = Misc.getDistance(prev.getLocation(), loc);
                Vector2f vel = Misc.getUnitVectorAtDegreeAngle(Misc.getAngleInDegrees(loc, prev.getLocation()));
                vel.scale(dist / (p.fadeIn + p.fadeOut) * 0.7f);
                e.getVelocity().set(vel);
            }

            prev = e;
        }

    }


    public static NegativeExplosionVisual.NEParams createStandardRiftParams(String minelayerId, float baseRadius) {
        Color color = new Color(178, 36, 69,255);
        Object o = Global.getSettings().getWeaponSpec(minelayerId).getProjectileSpec();
        if (o instanceof MissileSpecAPI) {
            MissileSpecAPI spec = (MissileSpecAPI) o;
            color = spec.getGlowColor();
        }
        NegativeExplosionVisual.NEParams p = createStandardRiftParams(color, baseRadius);
        return p;
    }

    public static NegativeExplosionVisual.NEParams createStandardRiftParams(Color borderColor, float radius) {
        NegativeExplosionVisual.NEParams p = new NegativeExplosionVisual.NEParams();
        //p.radius = 50f;
        p.hitGlowSizeMult = .75f;
        //p.hitGlowSizeMult = .67f;
        p.spawnHitGlowAt = 0f;
        p.noiseMag = 1f;
        //p.fadeIn = 0f;
        //p.fadeOut = 0.25f;

        //p.color = new Color(175,100,255,255);

        //p.hitGlowSizeMult = .75f;
        p.fadeIn = 0.1f;
        //p.noisePeriod = 0.05f;
        p.underglow = RiftCascadeEffect.EXPLOSION_UNDERCOLOR;
        //p.withHitGlow = i == 0;
        p.withHitGlow = true;

        //p.radius = 20f;
        p.radius = radius;
        //p.radius *= 0.75f + 0.5f * (float) Math.random();

        p.color = borderColor;
        return p;
    }


}