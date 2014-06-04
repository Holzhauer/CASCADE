/**
 * 
 */
package uk.ac.dmu.iesd.cascade.util;

import java.util.Date;


/**
 * @author Richard
 *
 */
public class DatedDoubleTimeSeries extends DatedTimeSeries<Double> 
{
	private boolean interpolate = true;

	/**
	 * @return the interpolate
	 */
	public boolean isInterpolate() {
		return interpolate;
	}

	/**
	 * @param interpolate the interpolate to set
	 */
	public void setInterpolate(boolean interpolate) {
		this.interpolate = interpolate;
	}
	
	@Override
	public Double getValue(Date d)
	{
		if (!interpolate)
		{
			super.getValue(d);
		}
		
		Date d1 = d;
		if (!datapoints.containsKey(d))
		{
			d1 = datapoints.headMap(d).lastKey();
		}
		
		double v1 = datapoints.get(d1);
		Date d2 = datapoints.tailMap(d).firstKey();
		double v2 = datapoints.get(d2);
		
		double fraction = ((double) d.getTime() - d1.getTime()) / ((double) d2.getTime() - d1.getTime());
		double retVal = v1 + (v2-v1)*fraction;
		return retVal;
	}
	
}