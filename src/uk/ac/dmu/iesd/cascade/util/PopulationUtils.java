/**
 * 
 */
package uk.ac.dmu.iesd.cascade.util;

import repast.simphony.query.PropertyEquals;
import repast.simphony.query.Query;
import repast.simphony.util.collections.IndexedIterable;
import uk.ac.dmu.iesd.cascade.agents.prosumers.HouseholdProsumer;
import uk.ac.dmu.iesd.cascade.base.Consts;
import uk.ac.dmu.iesd.cascade.context.CascadeContext;

/**
 * Provides utilities to perform operations across a population in a simulation
 * 
 * @author jsnape
 * 
 */
public class PopulationUtils
{

	/**
	 * 
	 */
	public static void testAndPrintHouseholdApplianceProportions(CascadeContext thisContext)
	{
		// TODO Auto-generated method stub
		IndexedIterable<HouseholdProsumer> householdProsumers = thisContext.getObjects(HouseholdProsumer.class);
		int totalPopulation = IterableUtils.count(householdProsumers);

		if (thisContext.logger.isTraceEnabled())
		{
			thisContext.logger.trace("Population proportions");
		}
		if (thisContext.logger.isTraceEnabled())
		{
			thisContext.logger.trace("======================");
		}
		if (thisContext.logger.isTraceEnabled())
		{
			thisContext.logger.trace("There are " + totalPopulation + "agents");
		}
		for (int i = 1; i <= Consts.OCCUPANCY_PROBABILITY_ARRAY.length; i++)
		{
			Query<HouseholdProsumer> occ1Query = new PropertyEquals(thisContext, "numOccupants", i);
			thisContext.logger
					.trace(((IterableUtils.count(occ1Query.query()) * 100) / totalPopulation) + "% of agents with occupancy " + i);
		}
		String[] coldAppliances =
		{ "hasFridgeFreezer", "hasRefrigerator", "hasUprightFreezer", "hasChestFreezer" };
		String[] wetAppliances =
		{ "hasWashingMachine", "hasWasherDryer", "hasTumbleDryer", "hasDishWasher" };

		for (String coldAppliance : coldAppliances)
		{
			Query<HouseholdProsumer> occ1Query = new PropertyEquals(thisContext, coldAppliance, true);
if (			thisContext.logger.isTraceEnabled()) {
			thisContext.logger.trace(((IterableUtils.count(occ1Query.query()) * 100) / totalPopulation) + "% of agents with appliance "
					+ coldAppliance);
}
		}

		for (String wetAppliance : wetAppliances)
		{

			Query<HouseholdProsumer> occ1Query = new PropertyEquals(thisContext, wetAppliance, true);
if (			thisContext.logger.isTraceEnabled()) {
			thisContext.logger.trace(((IterableUtils.count(occ1Query.query()) * 100) / totalPopulation) + "% of agents with appliance "
					+ wetAppliance);
}
		}

	}

}
