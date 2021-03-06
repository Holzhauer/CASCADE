/**
 * 
 */
package uk.ac.dmu.iesd.cascade.behaviour.psychological.cognitive;

import repast.simphony.random.RandomHelper;

/**
 * @author jsnape
 * 
 */
public class SCTModel
{

	double outcomeExpectation = 0;
	double perceptionOfOthers = 0;
	double selfEfficacy = 0;
	double socioStructural = 0;

	double goal = 0;
	double behaviour = 0;

	double outcome = 0;

	double weightSocioStructuralToOutcomeExp = 1;
	double weightSocioStructuralToPerceptionOfOthers = 1;
	double weightSelfEfficacyToOutcomeExp = 1;

	double weightOutcomeExpToGoal = 1;
	double weightPerceptionOfOthersToGoal = 1;
	double weightSelfEfficacyToGoal = 1;
	double weightSocioStructuralToGoal = 1;

	double weightOutcomeExpToBehaviour = 1;
	double weightPerceptionOfOthersToBehaviour = 1;
	double weightSelfEfficacyToBehaviour = 1;
	double weightSocioStructuralToBehaviour = 1;

	/*
	 * This is a special link - from the goal to behaviour This is given full
	 * weighting as it
	 */
	double weightGoalToBehaviour = 1;

	// Note - the behaviour to Outcome link is calculated by other
	// factors in the models and so outcome is populated directly

	/*
	 * These are the feedback links, affecting the construct values
	 */
	double weightOutcomeToSelfEfficacy = 0;
	double weightOutcomeToSocioStructural = 0;
	double weightOutcomeToOutcomeExp = 0;

	private void calculateFeedbacks()
	{
		this.outcomeExpectation = (this.outcomeExpectation + this.weightOutcomeToOutcomeExp * this.outcome
				+ this.weightSocioStructuralToOutcomeExp * this.socioStructural + this.weightSelfEfficacyToOutcomeExp * this.selfEfficacy)
				/ (1.0 + this.weightOutcomeToOutcomeExp + this.weightSocioStructuralToOutcomeExp + this.weightSelfEfficacyToOutcomeExp);
		this.perceptionOfOthers = (this.perceptionOfOthers + this.weightSocioStructuralToPerceptionOfOthers * this.socioStructural)
				/ (1.0 + this.weightSocioStructuralToPerceptionOfOthers);
		this.selfEfficacy = (this.selfEfficacy + this.weightOutcomeToSelfEfficacy * this.outcome)
				/ (1.0 + this.weightOutcomeToSelfEfficacy);
		this.socioStructural = (this.socioStructural + this.weightOutcomeToSocioStructural * this.outcome)
				/ (1.0 + this.weightOutcomeToSocioStructural);
	}

	/*
	 * Calculate the goal from the constructs
	 * 
	 * The formulae used are such that socio-structual factors of 0 does not
	 * render the holding of the goal impossible (note the difference with
	 * behaviour, where some socio structural factors can render the behaviour
	 * impossible
	 */
	private void calculateGoal()
	{
		// calculateFeedbacks(); // Need to think about this - ensure it doesn't
		// take contructs -ve (or at least that we deal with that properly)

		double sumOfWeightedConstructs = 0;
		sumOfWeightedConstructs += this.weightOutcomeExpToGoal * this.outcomeExpectation;
		sumOfWeightedConstructs += this.weightPerceptionOfOthersToGoal * this.perceptionOfOthers;
		sumOfWeightedConstructs += this.weightSelfEfficacyToGoal * this.selfEfficacy;
		sumOfWeightedConstructs += this.weightSocioStructuralToGoal * this.socioStructural;
		double sumOfWeights = 0;
		sumOfWeights += this.weightOutcomeExpToGoal;
		sumOfWeights += this.weightPerceptionOfOthersToGoal;
		sumOfWeights += this.weightSelfEfficacyToGoal;
		sumOfWeights += this.weightSocioStructuralToGoal;
		this.goal = sumOfWeightedConstructs / sumOfWeights;
	}

	/*
	 * Return a number between 0 1nad 1 which can be thought of as a likelihood
	 * of behaviour.
	 * 
	 * The formulae implemented are such that certaion socio-structural factors
	 * can render the socio-structural construct value of 0 can render the
	 * behaviour impossible.
	 */
	public void calculateBehaviour()
	{
		this.calculateGoal();
		double sumOfWeightedConstructs = 0;
		sumOfWeightedConstructs += this.weightOutcomeExpToBehaviour * this.outcomeExpectation;
		sumOfWeightedConstructs += this.weightPerceptionOfOthersToBehaviour * this.perceptionOfOthers;
		sumOfWeightedConstructs += this.weightSelfEfficacyToBehaviour * this.selfEfficacy;
		sumOfWeightedConstructs += this.weightSocioStructuralToBehaviour * this.socioStructural;
		sumOfWeightedConstructs += this.weightGoalToBehaviour * this.goal;
		double sumOfWeights = 0;
		sumOfWeights += this.weightOutcomeExpToBehaviour;
		sumOfWeights += this.weightPerceptionOfOthersToBehaviour;
		sumOfWeights += this.weightSelfEfficacyToBehaviour;
		sumOfWeights += this.weightSocioStructuralToBehaviour;
		sumOfWeights += this.weightGoalToBehaviour;
		this.behaviour = sumOfWeightedConstructs / sumOfWeights;
		this.behaviour *= this.socioStructural;
	}

	double lowestBehaviourTrueThreshold = 0;
	double absoluteBehaviourThreshold = 1;

	/*
	 * get a binary behaviour variable from the internal behaviour likelihood.
	 * 
	 * This is done in a stochastic fashion, with hard limits if required. The
	 * scheme is as follows
	 * 
	 * if behaviour likelihood < lowestBehaviourThreshold return false if
	 * behaviour likelihood > absoluteBehaviourThreshold retunr true for other
	 * values, draw from a uniform distribution between the thresholds. If
	 * likelihood > random draw return true else false
	 * 
	 * By default, the thresholds are 0 and 1, meaning that the "inbetween"
	 * regime applies to all values
	 */
	public boolean getBinaryBehaviourDecisionStochastic()
	{
		if (RandomHelper.nextDoubleFromTo(this.lowestBehaviourTrueThreshold, this.absoluteBehaviourThreshold) < this.behaviour)
		{
			return true;
		}
		return false;
	}

	/*
	 * return true if behaviour likelihood > absoluteBehaviourThreshold, false
	 * otherwise
	 */
	public boolean getBinaryBehaviourDecisionHardThreshold()
	{
		return (this.behaviour > this.absoluteBehaviourThreshold);
	}

	public void setLowestTrueBehaviourThreshold(double d)
	{
		this.lowestBehaviourTrueThreshold = d;
	}

	public void setAbsoluteBehaviourThreshold(double d)
	{
		this.absoluteBehaviourThreshold = d;
	}

	/**
	 * @return the outcomeExpectation
	 */
	public double getOutcomeExpectation()
	{
		return this.outcomeExpectation;
	}

	/**
	 * @return the perceptionOfOthers
	 */
	public double getPerceptionOfOthers()
	{
		return this.perceptionOfOthers;
	}

	/**
	 * @return the selfEfficacy
	 */
	public double getSelfEfficacy()
	{
		return this.selfEfficacy;
	}

	/**
	 * @return the socioStructural
	 */
	public double getSocioStructural()
	{
		return this.socioStructural;
	}

	/**
	 * @return the goal
	 */
	public double getGoal()
	{
		return this.goal;
	}

	/**
	 * @return the behaviour
	 */
	public double getBehaviour()
	{
		return this.behaviour;
	}

	/**
	 * @return the outcome
	 */
	public double getOutcome()
	{
		return this.outcome;
	}

	/**
	 * @return the weightSocioStructuralToOutcomeExp
	 */
	public double getWeightSocioStructuralToOutcomeExp()
	{
		return this.weightSocioStructuralToOutcomeExp;
	}

	/**
	 * @return the weightSocioStructuralToPerceptionOfOthers
	 */
	public double getWeightSocioStructuralToPerceptionOfOthers()
	{
		return this.weightSocioStructuralToPerceptionOfOthers;
	}

	/**
	 * @return the weightSelfEfficacyToOutcomeExp
	 */
	public double getWeightSelfEfficacyToOutcomeExp()
	{
		return this.weightSelfEfficacyToOutcomeExp;
	}

	/**
	 * @return the weightOutcomeExpToGoal
	 */
	public double getWeightOutcomeExpToGoal()
	{
		return this.weightOutcomeExpToGoal;
	}

	/**
	 * @return the weightPerceptionOfOthersToGoal
	 */
	public double getWeightPerceptionOfOthersToGoal()
	{
		return this.weightPerceptionOfOthersToGoal;
	}

	/**
	 * @return the weightSelfEfficacyToGoal
	 */
	public double getWeightSelfEfficacyToGoal()
	{
		return this.weightSelfEfficacyToGoal;
	}

	/**
	 * @return the weightSocioStructuralToGoal
	 */
	public double getWeightSocioStructuralToGoal()
	{
		return this.weightSocioStructuralToGoal;
	}

	/**
	 * @return the weightOutcomeExpToBehaviour
	 */
	public double getWeightOutcomeExpToBehaviour()
	{
		return this.weightOutcomeExpToBehaviour;
	}

	/**
	 * @return the weightPerceptionOfOthersToBehaviour
	 */
	public double getWeightPerceptionOfOthersToBehaviour()
	{
		return this.weightPerceptionOfOthersToBehaviour;
	}

	/**
	 * @return the weightSelfEfficacyToBehaviour
	 */
	public double getWeightSelfEfficacyToBehaviour()
	{
		return this.weightSelfEfficacyToBehaviour;
	}

	/**
	 * @return the weightSocioStructuralToBehaviour
	 */
	public double getWeightSocioStructuralToBehaviour()
	{
		return this.weightSocioStructuralToBehaviour;
	}

	/**
	 * @return the weightGoalToBehaviour
	 */
	public double getWeightGoalToBehaviour()
	{
		return this.weightGoalToBehaviour;
	}

	/**
	 * @return the weightOutcomeToSelfEfficacy
	 */
	public double getWeightOutcomeToSelfEfficacy()
	{
		return this.weightOutcomeToSelfEfficacy;
	}

	/**
	 * @return the weightOutcomeToSocioStructural
	 */
	public double getWeightOutcomeToSocioStructural()
	{
		return this.weightOutcomeToSocioStructural;
	}

	/**
	 * @return the weightOutcomeToOutcomeExp
	 */
	public double getWeightOutcomeToOutcomeExp()
	{
		return this.weightOutcomeToOutcomeExp;
	}

	/**
	 * @return the lowestBehaviourTrueThreshold
	 */
	public double getLowestBehaviourTrueThreshold()
	{
		return this.lowestBehaviourTrueThreshold;
	}

	/**
	 * @return the absoluteBehaviourThreshold
	 */
	public double getAbsoluteBehaviourThreshold()
	{
		return this.absoluteBehaviourThreshold;
	}

	/**
	 * @param outcomeExpectation
	 *            the outcomeExpectation to set
	 */
	public void setOutcomeExpectation(double outcomeExpectation)
	{
		this.outcomeExpectation = outcomeExpectation;
	}

	/**
	 * @param perceptionOfOthers
	 *            the perceptionOfOthers to set
	 */
	public void setPerceptionOfOthers(double perceptionOfOthers)
	{
		this.perceptionOfOthers = perceptionOfOthers;
	}

	/**
	 * @param selfEfficacy
	 *            the selfEfficacy to set
	 */
	public void setSelfEfficacy(double selfEfficacy)
	{
		this.selfEfficacy = selfEfficacy;
	}

	/**
	 * @param socioStructural
	 *            the socioStructural to set
	 */
	public void setSocioStructural(double socioStructural)
	{
		this.socioStructural = socioStructural;
	}

	/**
	 * @param goal
	 *            the goal to set
	 */
	public void setGoal(double goal)
	{
		this.goal = goal;
	}

	/**
	 * @param behaviour
	 *            the behaviour to set
	 */
	public void setBehaviour(double behaviour)
	{
		this.behaviour = behaviour;
	}

	/**
	 * @param outcome
	 *            the outcome to set
	 */
	public void setOutcome(double outcome)
	{
		this.outcome = outcome;
	}

	/**
	 * @param weightSocioStructuralToOutcomeExp
	 *            the weightSocioStructuralToOutcomeExp to set
	 */
	public void setWeightSocioStructuralToOutcomeExp(double weightSocioStructuralToOutcomeExp)
	{
		this.weightSocioStructuralToOutcomeExp = weightSocioStructuralToOutcomeExp;
	}

	/**
	 * @param weightSocioStructuralToPerceptionOfOthers
	 *            the weightSocioStructuralToPerceptionOfOthers to set
	 */
	public void setWeightSocioStructuralToPerceptionOfOthers(double weightSocioStructuralToPerceptionOfOthers)
	{
		this.weightSocioStructuralToPerceptionOfOthers = weightSocioStructuralToPerceptionOfOthers;
	}

	/**
	 * @param weightSelfEfficacyToOutcomeExp
	 *            the weightSelfEfficacyToOutcomeExp to set
	 */
	public void setWeightSelfEfficacyToOutcomeExp(double weightSelfEfficacyToOutcomeExp)
	{
		this.weightSelfEfficacyToOutcomeExp = weightSelfEfficacyToOutcomeExp;
	}

	/**
	 * @param weightOutcomeExpToGoal
	 *            the weightOutcomeExpToGoal to set
	 */
	public void setWeightOutcomeExpToGoal(double weightOutcomeExpToGoal)
	{
		this.weightOutcomeExpToGoal = weightOutcomeExpToGoal;
	}

	/**
	 * @param weightPerceptionOfOthersToGoal
	 *            the weightPerceptionOfOthersToGoal to set
	 */
	public void setWeightPerceptionOfOthersToGoal(double weightPerceptionOfOthersToGoal)
	{
		this.weightPerceptionOfOthersToGoal = weightPerceptionOfOthersToGoal;
	}

	/**
	 * @param weightSelfEfficacyToGoal
	 *            the weightSelfEfficacyToGoal to set
	 */
	public void setWeightSelfEfficacyToGoal(double weightSelfEfficacyToGoal)
	{
		this.weightSelfEfficacyToGoal = weightSelfEfficacyToGoal;
	}

	/**
	 * @param weightSocioStructuralToGoal
	 *            the weightSocioStructuralToGoal to set
	 */
	public void setWeightSocioStructuralToGoal(double weightSocioStructuralToGoal)
	{
		this.weightSocioStructuralToGoal = weightSocioStructuralToGoal;
	}

	/**
	 * @param weightOutcomeExpToBehaviour
	 *            the weightOutcomeExpToBehaviour to set
	 */
	public void setWeightOutcomeExpToBehaviour(double weightOutcomeExpToBehaviour)
	{
		this.weightOutcomeExpToBehaviour = weightOutcomeExpToBehaviour;
	}

	/**
	 * @param weightPerceptionOfOthersToBehaviour
	 *            the weightPerceptionOfOthersToBehaviour to set
	 */
	public void setWeightPerceptionOfOthersToBehaviour(double weightPerceptionOfOthersToBehaviour)
	{
		this.weightPerceptionOfOthersToBehaviour = weightPerceptionOfOthersToBehaviour;
	}

	/**
	 * @param weightSelfEfficacyToBehaviour
	 *            the weightSelfEfficacyToBehaviour to set
	 */
	public void setWeightSelfEfficacyToBehaviour(double weightSelfEfficacyToBehaviour)
	{
		this.weightSelfEfficacyToBehaviour = weightSelfEfficacyToBehaviour;
	}

	/**
	 * @param weightSocioStructuralToBehaviour
	 *            the weightSocioStructuralToBehaviour to set
	 */
	public void setWeightSocioStructuralToBehaviour(double weightSocioStructuralToBehaviour)
	{
		this.weightSocioStructuralToBehaviour = weightSocioStructuralToBehaviour;
	}

	/**
	 * @param weightGoalToBehaviour
	 *            the weightGoalToBehaviour to set
	 */
	public void setWeightGoalToBehaviour(double weightGoalToBehaviour)
	{
		this.weightGoalToBehaviour = weightGoalToBehaviour;
	}

	/**
	 * @param weightOutcomeToSelfEfficacy
	 *            the weightOutcomeToSelfEfficacy to set
	 */
	public void setWeightOutcomeToSelfEfficacy(double weightOutcomeToSelfEfficacy)
	{
		this.weightOutcomeToSelfEfficacy = weightOutcomeToSelfEfficacy;
	}

	/**
	 * @param weightOutcomeToSocioStructural
	 *            the weightOutcomeToSocioStructural to set
	 */
	public void setWeightOutcomeToSocioStructural(double weightOutcomeToSocioStructural)
	{
		this.weightOutcomeToSocioStructural = weightOutcomeToSocioStructural;
	}

	/**
	 * @param weightOutcomeToOutcomeExp
	 *            the weightOutcomeToOutcomeExp to set
	 */
	public void setWeightOutcomeToOutcomeExp(double weightOutcomeToOutcomeExp)
	{
		this.weightOutcomeToOutcomeExp = weightOutcomeToOutcomeExp;
	}

	/**
	 * @param lowestBehaviourTrueThreshold
	 *            the lowestBehaviourTrueThreshold to set
	 */
	public void setLowestBehaviourTrueThreshold(double lowestBehaviourTrueThreshold)
	{
		this.lowestBehaviourTrueThreshold = lowestBehaviourTrueThreshold;
	}

}
