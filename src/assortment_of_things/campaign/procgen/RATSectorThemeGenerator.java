package assortment_of_things.campaign.procgen;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import assortment_of_things.campaign.procgen.vannilaThemes.RATDerelictThemeGenerator;
import assortment_of_things.campaign.procgen.vannilaThemes.RATMiscellaneousThemeGenerator;
import assortment_of_things.campaign.procgen.vannilaThemes.RATRuinsThemeGenerator;
import assortment_of_things.misc.RATSettings;
import assortment_of_things.campaign.procgen.vannilaThemes.RATRemnantThemeGenerator;
import com.fs.starfarer.api.Global;
import com.fs.starfarer.api.impl.campaign.procgen.StarSystemGenerator;
import com.fs.starfarer.api.impl.campaign.procgen.themes.*;

public class RATSectorThemeGenerator {

	public static List<ThemeGenerator> generators = new ArrayList<ThemeGenerator>();
	
/*	static {
		generators.add(new DerelictThemeGenerator());
		generators.add(new RemnantThemeGenerator());
		generators.add(new RuinsThemeGenerator());
		generators.add(new MiscellaneousThemeGenerator());
	}*/
	
	public static void generate(ThemeGenContext context) {

		//This section is used to get all the generators mods add and replace vannila ones with mine.
		generators.clear();
		for (ThemeGenerator gen : SectorThemeGenerator.generators)
		{
			if (gen instanceof RemnantThemeGenerator || gen instanceof RuinsThemeGenerator || gen instanceof MiscellaneousThemeGenerator || gen instanceof DerelictThemeGenerator) continue;
			generators.add(gen);
		}
		generators.add(new RATDerelictThemeGenerator());
		generators.add(new RATRemnantThemeGenerator());
		generators.add(new RATRuinsThemeGenerator());
		generators.add(new RATMiscellaneousThemeGenerator());


		Collections.sort(generators, new Comparator<ThemeGenerator>() {
			public int compare(ThemeGenerator o1, ThemeGenerator o2) {
				int result = o1.getOrder() - o2.getOrder();
				if (result == 0) return o1.getThemeId().compareTo(o2.getThemeId());
				return result;
			}
		});
		
		float totalWeight = 0f;
		for (ThemeGenerator g : generators) {
			totalWeight += g.getWeight();
			g.setRandom(StarSystemGenerator.random);
		}
		
		for (ThemeGenerator g : generators) {
			float w = g.getWeight();
			
			float f = 0f;
			if (totalWeight > 0) {
				f = w / totalWeight; 
			} else {
				if (w > 0) f = 1f;
			}
			//g.setRandom(StarSystemGenerator.random);
			g.generateForSector(context, f);
			
			//float used = context.majorThemes.size();
			totalWeight -= w;

		}
	}
	
	
	
	
}
