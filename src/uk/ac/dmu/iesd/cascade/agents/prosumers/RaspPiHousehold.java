/**
 * 
 */
package uk.ac.dmu.iesd.cascade.agents.prosumers;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import repast.simphony.engine.environment.RunState;
import repast.simphony.engine.schedule.ScheduledMethod;
import repast.simphony.random.RandomHelper;
import repast.simphony.visualization.IDisplay;
import repast.simphony.visualizationOGL2D.DisplayOGL2D;
import uk.ac.dmu.iesd.cascade.base.Consts;
import uk.ac.dmu.iesd.cascade.context.CascadeContext;

/**
 * @author richard
 * 
 */
public class RaspPiHousehold extends HouseholdProsumer
{
	String piURL = null;

	private boolean fridgeOn = true;
	
	WeakHashMap<String, Boolean> deviceMap = new WeakHashMap<String, Boolean>();

	private boolean heaterOn;

	private boolean spaceHeaterOn;

	private boolean upHeaterOn;

	private boolean downHeaterOn;

	@ScheduledMethod(start = 100, interval = 1)
	public void controlThePi()
	{
		/*
		 * if (this.getContext().getTimeslotOfDay() == 0) { if (fridgeOn) {
		 * switchFridgeOff(); } else { switchFridgeOn(); } }
		 */

		this.setOptions();

		if (timeOfDay >= 38 && timeOfDay < 43)
		{
			//switch on kids bedroom light
			switchOnDevice("Bed2Light");
			switchOnDevice("Bed3Light");
		}
		else
		{
			switchOffDevice("Bed2Light");
			switchOffDevice("Bed3Light");
		}

		if (timeOfDay >= 43)
		{
			switchOnDevice("Bed1Light");
		}
		else
		{
			switchOffDevice("Bed1Light");
		}

		if (timeOfDay == 36 || timeOfDay == 37 || timeOfDay ==38 || timeOfDay == 43)
		{
			switchOnDevice("BathroomLight");
		}
		else
		{
			switchOffDevice("BathroomLight");
		}

		if (timeOfDay >= 34)
		{
			switchOnDevice("LivingLight");
		}
		else
		{
			switchOffDevice("LivingLight");
		}

		if ((timeOfDay >= 34 && timeOfDay < 37) || (timeOfDay >= 14 && timeOfDay < 17))
		{
			switchOnDevice("KitchenLight");
			switchOnDevice("DiningLight");
		}
		else
		{
			switchOffDevice("KitchenLight");
			switchOffDevice("DiningLight");
		}


		if (this.getWaterHeatProfile()[timeOfDay] > 0.1) {
			if (!heaterOn) {
				switchOnWaterHeat();
			}
		}
		else {
			if (heaterOn)
			{
				switchOffWaterHeat();
			}
		}


		if (this.getHistoricalSpaceHeatDemand()[timeOfDay] > 0.1){
			if (timeOfDay > 32) {
				if (!upHeaterOn) {
					switchOnUpHeat();
				}
			}
			else {
				if (upHeaterOn)
				{
					switchOffUpHeat();
				}
			}

			if (timeOfDay <46) {
				if (!downHeaterOn) {
					switchOnDownHeat();
				}
			}
			else {
				if (downHeaterOn)
				{
					switchOffDownHeat();
				}
			}	
		}

		if (this.isHasColdAppliances() && this.coldApplianceProfile != null)
		{
			double fridgeLoad = 0;

			if (this.isHasFridgeFreezer()) {
				fridgeLoad = ((double[]) coldApplianceProfiles
						.get(Consts.COLD_APP_FRIDGEFREEZER))[time
						                                     % coldApplianceProfile.length];

			} else {
				fridgeLoad = ((double[]) coldApplianceProfiles
						.get(Consts.COLD_APP_FRIDGE))[time
						                              % coldApplianceProfile.length];

			}

			// System.err.print(fridgeLoad + ",");

			if (this.fridgeOn)
			{
				if (fridgeLoad == 0)
				{
					this.switchFridgeOff();
				}
			}
			else
			{
				if (fridgeLoad > 0)
				{
					this.switchFridgeOn();
				}
			}

		}
	}

	/**
	 * 
	 */
	private void switchOnWaterHeat() {
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", "HotWater");
		requestVars.put("value", "1");
		this.sendPiRequest(requestVars);
		this.heaterOn = true;		
	}

	@Override
	@ScheduledMethod(start = 0, interval = 0, priority = Consts.PROSUMER_PRIORITY_FIFTH)
	public void probeSpecificAgent()
	{
		//	if (this.getAgentID() == 1001)

		{
			ArrayList probed = new ArrayList();
			probed.add(this);
			List<IDisplay> listOfDisplays = RunState.getInstance().getGUIRegistry().getDisplays();
			for (IDisplay display : listOfDisplays)
			{

				if (display instanceof DisplayOGL2D)
				{
					((DisplayOGL2D) display).getProbeSupport().fireProbeEvent(this, probed);
				}
			}
		}
	}

	/**
	 * 
	 */
	private void switchOffDevice(String deviceName) {
		boolean currState = true;
		if (deviceMap.containsKey(deviceName))
		{
			currState = deviceMap.get(deviceName);
		}
		
		if (currState)
		{
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", deviceName);
		requestVars.put("value", "0");
		sendPiRequest(requestVars);
		}
		
		deviceMap.put(deviceName, false);

	}

	/**
	 * 
	 */
	private void switchOnDevice(String deviceName) {
		boolean currState = false;
		if (deviceMap.containsKey(deviceName))
		{
			currState = deviceMap.get(deviceName);
		}
		
		if (!currState)
		{

		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", deviceName);
		requestVars.put("value", "1");
		sendPiRequest(requestVars);
		}
		
		deviceMap.put(deviceName, true);
	}

	/**
	 * 
	 */
	private void switchOffWaterHeat() {
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", "HotWater");
		requestVars.put("value", "0");
		this.sendPiRequest(requestVars);
		this.heaterOn = false;

	}

	/**
	 * 
	 */

	private void switchOnHeat()
	{
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", "HotWater");
		requestVars.put("value", "1");
		this.sendPiRequest(requestVars);
		this.heaterOn = true;

	}
	
	/**
	 * 
	 */
	private void switchOffUpHeat() {
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", "UpHeat");
		requestVars.put("value", "0");
		sendPiRequest(requestVars);
		upHeaterOn = false;

	}

	/**
	 * 
	 */
	private void switchOnUpHeat() {
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", "UpHeat");
		requestVars.put("value", "1");
		sendPiRequest(requestVars);
		upHeaterOn = true;

	}
	
	/**
	 * 
	 */
	private void switchOffDownHeat() {
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", "DownHeat");
		requestVars.put("value", "0");
		sendPiRequest(requestVars);
		downHeaterOn = false;

	}

	/**
	 * 
	 */
	private void switchOnDownHeat() {
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", "DownHeat");
		requestVars.put("value", "1");
		sendPiRequest(requestVars);
		downHeaterOn = true;

	}

	/**
	 * 
	 */
	public void setOptions()
	{
		this.costThreshold = Consts.HOUSEHOLD_COST_THRESHOLD;
		this.setPredictedCostSignal(Consts.ZERO_COST_SIGNAL);

		this.transmitPropensitySmartControl = RandomHelper.nextDouble();

		this.initializeRandomlyDailyElasticityArray(0, 0.1);

		// this.initializeSimilarlyDailyElasticityArray(0.1d);
		this.setRandomlyPercentageMoveableDemand(0, Consts.MAX_DOMESTIC_MOVEABLE_LOAD_FRACTION);

		this.exercisesBehaviourChange = true;
		// pAgent.exercisesBehaviourChange = (RandomHelper.nextDouble() > (1 -
		// Consts.HOUSEHOLDS_WILLING_TO_CHANGE_BEHAVIOUR));

		// TODO: We just set smart meter true here - need more sophisticated way
		// to set for different scenarios
		this.hasSmartMeter = true;

		// pAgent.hasSmartControl = (RandomHelper.nextDouble() > (1 -
		// Consts.HOUSEHOLDS_WITH_SMART_CONTROL));
		this.hasSmartControl = true;

		// TODO: we need to set up wattbox after appliances added. This is all a
		// bit
		// non-object oriented. Could do with a proper design methodology here.
		if (this.hasSmartControl)
		{
			this.setWattboxController();


		}

		this.setNumOccupants(2);

	}

	/**
	 * 
	 */
	private void switchFridgeOff()
	{
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", "0");
		requestVars.put("value", "1");
		this.sendPiRequest(requestVars);
		this.fridgeOn = false;

	}

	/**
	 * 
	 */
	private void switchFridgeOn()
	{
		WeakHashMap<String, Object> requestVars = new WeakHashMap<String, Object>();
		requestVars.put("device", "0");
		requestVars.put("value", "0");
		this.sendPiRequest(requestVars);
		this.fridgeOn = true;

	}

	private void sendPiRequest(WeakHashMap<String, Object> map)
	{
		try
		{
			StringBuffer getURL = new StringBuffer(this.piURL);
			getURL.append("?");

			for (String k : map.keySet())
			{
				getURL.append(k);
				getURL.append("=");
				getURL.append(map.get(k).toString());
				getURL.append("&");
			}
			System.err.println(this.getContext().getTickCount() + " : Trying to send URL " + getURL.toString());
			HttpURLConnection thisConn = (HttpURLConnection) (new URL(getURL.toString())).openConnection();
			System.err.println("Request sent, response code : " + thisConn.getResponseCode() + " - " + thisConn.getResponseMessage());
			// thisConn.setRequestMethod("GET");

		}
		catch (IOException e)
		{
			System.err.println("Couldn't connect to Pi when attempted");
			e.printStackTrace();
		}

	}

	/**
	 * @param context
	 * @param otherDemandProfile
	 */
	public RaspPiHousehold(CascadeContext context, double[] otherDemandProfile)
	{
		super(context, otherDemandProfile);

		piURL = "http://192.168.1.100/action";

	}

}
