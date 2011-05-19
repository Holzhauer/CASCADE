/**
 * 
 */
package uk.ac.dmu.iesd.cascade.context;

import javax.accessibility.AccessibleContext;

import repast.simphony.context.Context;
import repast.simphony.parameter.Parameters;
import repast.simphony.random.RandomHelper;
import uk.ac.dmu.iesd.cascade.Consts;
import uk.ac.dmu.iesd.cascade.Consts.*;

/**
 * This class is a factory for creating instances of <tt>ProsumerAgent</tt> concrete subclasses
 * Its public creator method's signatures are defined by {@link IProsumerFactory} interface.
 *   
 * @author Babak Mahdavi
 * @version $Revision: 1.0 $ $Date: 2011/05/16 14:00:00 $
 */
public class ProsumerFactory implements IProsumerFactory {
	//Parameters params;
	CascadeContext cascadeMainContext;
	
	
	public ProsumerFactory(CascadeContext context) {
		//this.params = par;
		this.cascadeMainContext= context;
		
	}
	
	/*
	 * Creates a household prosumer with a basic consumption profile as supplied
	 * with or without added noise
	 * 
	 * @param baseProfile - an array of the basic consumption profile for this prosumer (kWh per tick)
	 * @param addNoise - boolean specifying whether or not to add noise to the profile
	 */
	
	public HouseholdProsumer createHouseholdProsumer(float[] baseProfileArray, boolean addNoise) {
		HouseholdProsumer pAgent;
		int ticksPerDay = cascadeMainContext.getTickPerDay();
		if (baseProfileArray.length % ticksPerDay != 0)
		{
			System.err.println("Household base demand array not a whole number of days");
			System.err.println("May cause unexpected behaviour");
		}
		
		if (addNoise) 
		{
			pAgent = new HouseholdProsumer(cascadeMainContext, createRandomHouseholdDemand(baseProfileArray));
		}
		else
		{
			pAgent = new HouseholdProsumer(cascadeMainContext, baseProfileArray);
		}
		//thisAgent.exercisesBehaviourChange = (RandomHelper.nextDouble() > 0.5);
		pAgent.hasSmartControl = (RandomHelper.nextDouble() > 0.9);
		pAgent.exercisesBehaviourChange = true;
		pAgent.hasSmartMeter = true;
		pAgent.costThreshold = 125;  //Threshold in price signal at which behaviour change is prompted (if agent is willing)
		pAgent.minSetPoint = 18;
		pAgent.maxSetPoint = 21;
		pAgent.currentInternalTemp = 19;
		
		pAgent.transmitPropensitySmartControl = (float) RandomHelper.nextDouble();
		
		return pAgent;
	}
	
	
	/*
	 * This method simply adds a random element to the base profile to create a household demand
	 * It should be over-ridden in the future to use something better - for instance melody's model
	 * or something which time-shifts demand somewhat, or select one of a number of typical profiles
	 * based on occupancy.
	 */
	private float[] createRandomHouseholdDemand(float[] baseProfileArray){
		float[] newProfile = new float[baseProfileArray.length];
		
		//add amplitude randomisation
		for (int i = 0; i < newProfile.length; i++)
		{
			newProfile[i] = baseProfileArray[i] * (float)(1 + 0.3*(RandomHelper.nextDouble() - 0.5));
		}
		
		//add time jitter
		float jitterFactor = (float) RandomHelper.nextDouble() - 0.5f;
		//System.out.println("Applying jitter" + jitterFactor);
		newProfile[0] = (jitterFactor * newProfile[0]) + ((1 - jitterFactor) * newProfile[newProfile.length - 1]);
		for (int i = 1; i < (newProfile.length - 1); i++)
		{
			newProfile[i] = (jitterFactor * newProfile[i]) + ((1 - jitterFactor) * newProfile[i+1]);
		}
		newProfile[newProfile.length - 1] = (jitterFactor * newProfile[newProfile.length - 1]) + ((1 - jitterFactor) * newProfile[0]);
		
		return newProfile;
	}
	
	
	/*
	 * Creates a prosumer to represent a pure generator.  Therefore zero
	 * base demand, set the generator type and capacity.
	 * 
	 * TODO: should the base demand be zero or null???
	 * 
	 * @param Capacity - the generation capacity of this agent (kWh)
	 * @param type - the type of this generator (from an enumerator of all types)
	 */
	
	public ProsumerAgent createPureGenerator(float capacity, Consts.GENERATOR_TYPE genType) {
		GeneratorProsumer thisAgent = null;
		
		// Create a prosumer with zero base demand
		// TODO: Should this be null?  or zero as implemented?
		float[] nilDemand = new float[1];
		nilDemand[0] = 0;
		switch (genType){
		case WIND:
			thisAgent = new WindGeneratorProsumer(cascadeMainContext, nilDemand, capacity);
			break;			
		}

		return thisAgent;
	} 
	
	

}