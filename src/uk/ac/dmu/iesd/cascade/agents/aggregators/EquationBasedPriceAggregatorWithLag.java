/**
 * 
 */
package uk.ac.dmu.iesd.cascade.agents.aggregators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import repast.simphony.essentials.RepastEssentials;
import repast.simphony.random.RandomHelper;
import repast.simphony.space.graph.Network;
import repast.simphony.space.graph.RepastEdge;
import uk.ac.dmu.iesd.cascade.agents.prosumers.ProsumerAgent;
import uk.ac.dmu.iesd.cascade.base.Consts;
import uk.ac.dmu.iesd.cascade.context.CascadeContext;
import uk.ac.dmu.iesd.cascade.util.WrongCustomerTypeException;

/**
 * Very simple aggregator which passes a one value price signal to its prosumers
 * (i.e. "real time") This value is simply calculated from the demand of the
 * timestep before in this very simple model
 * 
 * @author jsnape
 * 
 */
public class EquationBasedPriceAggregatorWithLag extends AggregatorAgent
{

	/**
	 * Parameters characterising this aggregator's behaviour
	 */
	double[] laggedPrice;
	boolean addNoise;
	double A = 0.0006;
	double B = 12.0;
	double C = 40.0;
	double noiseRatio = 0.25;

	/**
	 * @param context
	 */
	public EquationBasedPriceAggregatorWithLag(CascadeContext context)
	{
		this(context, 0);
	}

	public EquationBasedPriceAggregatorWithLag(CascadeContext context, int lag)
	{
		this(context, lag, false);
	}

	/**
	 * @param context
	 */
	public EquationBasedPriceAggregatorWithLag(CascadeContext context, int lag, boolean noise)
	{
		super(context);
		this.laggedPrice = new double[lag + 1];
		Arrays.fill(this.laggedPrice, 125);
		this.addNoise = noise;
		this.category = Consts.BMU_CATEGORY.DEM_S;
		this.type = Consts.BMU_TYPE.DEM_SMALL;
		context.add(this);
	}

	public double getCurrPrice()
	{
		return this.laggedPrice[0];
	}

	public String priceTitle()
	{
		return this.getAgentName() + " current price";
	}

	public String demandTitle()
	{
		return this.getAgentName() + " current demand";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.dmu.iesd.cascade.agents.aggregators.AggregatorAgent#paramStringReport
	 * ()
	 */
	@Override
	protected String paramStringReport()
	{
		return this.getAgentName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * uk.ac.dmu.iesd.cascade.agents.aggregators.AggregatorAgent#bizPreStep()
	 */
	@Override
	public void bizPreStep()
	{
		if (this.mainContext.logger.isDebugEnabled())
		{
			this.mainContext.logger.debug("At tick " + RepastEssentials.GetTickCount() + " demand = " + this.getNetDemand());
		}
		ArrayList<ProsumerAgent> customers = this.getCustomersList();
		this.broadcastSignalToCustomers(this.getCurrPrice(), customers);
		for (int i = 0; i < this.laggedPrice.length - 1; i++)
		{
			this.laggedPrice[i] = this.laggedPrice[i + 1];

		}
		this.laggedPrice[this.laggedPrice.length - 1] = this.calculatePrice(this.getNetDemand());
	}

	private boolean broadcastSignalToCustomers(double val, List<ProsumerAgent> customerList)
	{
		double[] tmp = new double[]
		{ val };
		return this.broadcastSignalToCustomers(tmp, customerList);
	}

	/**
	 * @param netDemand
	 * @return
	 */
	private double calculatePrice(double netDemand)
	{
		// From Roscoe and Ault
		// Co-efficients estimated from Figure 4 in Roscoe and Ault
		// Note this was used in Cascade version checked in on
		// commit commit aef4743a1c085b17ce14559f21066cf7ce6de643

		double supplyCap = this.mainContext.getObjects(ProsumerAgent.class).size() * 0.75;
		double biggestGen = supplyCap / 100;
		double x = (netDemand / (supplyCap - biggestGen));
		double calcPrice = (this.A * Math.exp(this.B * x) + this.C);

		if (this.addNoise)
		{
			calcPrice = calcPrice * ((RandomHelper.nextDouble() - 0.5) * this.noiseRatio + 1);
		}

		if (calcPrice > 1000)
		{
			calcPrice = 1000;
		}
		return calcPrice + 20;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.ac.dmu.iesd.cascade.agents.aggregators.AggregatorAgent#bizStep()
	 */
	@Override
	public void bizStep()
	{
		ArrayList<? extends ProsumerAgent> customers = this.getCustomersList();
		float totalDemand = 0;
		for (ProsumerAgent c : customers)
		{
			totalDemand += c.getNetDemand();
		}
		if (this.mainContext.logger.isDebugEnabled())
		{
			this.mainContext.logger.debug(this.getAgentName() + " has total demand " + totalDemand);
		}
		this.setNetDemand(totalDemand);
	}

	/**
	 * @return ArrayList of ProsumerAgents that are this aggregators' customers
	 */
	private ArrayList<ProsumerAgent> getCustomersList()
	{
		ArrayList<ProsumerAgent> c = new ArrayList<ProsumerAgent>();
		Network economicNet = this.mainContext.getEconomicNetwork();

		Iterable<RepastEdge> iter = economicNet.getEdges();
		if (this.mainContext.logger.isTraceEnabled())
		{
			this.mainContext.logger.trace(this.getAgentName() + " has " + economicNet.size() + " links in economic network");
		}

		for (RepastEdge edge : iter)
		{
			Object linkSource = edge.getTarget();
			if (this.mainContext.logger.isTraceEnabled())
			{
				this.mainContext.logger.trace("RECO linkSource " + linkSource);
			}
			if (linkSource instanceof ProsumerAgent)
			{
				c.add((ProsumerAgent) linkSource);
			}
			else
			{
				throw (new WrongCustomerTypeException(linkSource));
			}
		}
		return c;
	}

}
