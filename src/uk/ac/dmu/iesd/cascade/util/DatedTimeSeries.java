/**
 * 
 */
package uk.ac.dmu.iesd.cascade.util;

import java.util.Date;

/**
 * @author Richard
 * 
 */
public class DatedTimeSeries<T> extends TimeSeries<Date, T>
{
	private Date lastValidDate = new Date(Long.MAX_VALUE);

	public Date getFirstDate()
	{
		if (this.datapoints.size() == 0)
		{
			return null;
		}
		return this.datapoints.firstKey();
	}

	/**
	 * @return the lastValidDate
	 */
	public Date getLastValidDate()
	{
		return this.lastValidDate;
	}

	/**
	 * @param lastValidDate
	 *            the lastValidDate to set
	 */
	public void setLastValidDate(Date lastValidDate)
	{
		this.lastValidDate = lastValidDate;
	}

}
