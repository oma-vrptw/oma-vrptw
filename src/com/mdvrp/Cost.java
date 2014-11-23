package com.mdvrp;

/**
 * This class stores information about cost of a route or a group of routes.
 * It has total which is the sum of travel, capacityViol, durationViol, twViol.
 */
public class Cost {
	public double total;			 // sum of all the costs
	public double travelTime;		 // sum of all distances travel time
	public double load;			     // sum of all quantities
	public double serviceTime;		 // sum of all service time;	 
	public double waitingTime;		 // sum of all waiting times when arrives before start TW
	public double loadViol;		     // violation of the load
	public double durationViol;      // violation of the duration waiting time + service time
	public double twViol;            // violation of the time window
	public double returnToDepotTime; // stores time to return to the depot
	public double depotTwViol;       // stores the time window violation of the depot
	
	
	// default constructor
	public Cost(){
		total             = 0;
		travelTime        = 0;
		load		      = 0;
		serviceTime		  = 0;
		waitingTime		  = 0;
		
		loadViol          = 0;
		durationViol      = 0;
		twViol            = 0;
		
		returnToDepotTime = 0;
		depotTwViol       = 0;
		
	}
	
	// constructor which clone the cost passed as parameter
	public Cost(Cost cost) {
		this.total             = new Double(cost.total);
		this.travelTime        = new Double(cost.travelTime);
		this.load              = new Double(cost.load);
		this.serviceTime       = new Double(cost.serviceTime);
		this.waitingTime       = new Double(cost.waitingTime);
		
		this.loadViol          = new Double(cost.loadViol);
		this.durationViol      = new Double(cost.durationViol);
		this.twViol            = new Double(cost.twViol);
		
		this.returnToDepotTime = new Double(cost.returnToDepotTime);
		this.depotTwViol       = new Double(cost.depotTwViol);		
	}

	
	public String toString() {
		StringBuffer print = new StringBuffer();
		print.append("--- Cost -------------------------------------");
		print.append("\n" + "| TotalTravelCost=" + travelTime + " TotalCostViol=" + total);
		print.append("\n" + "| LoadViol=" + loadViol + " DurationViol=" + durationViol + " TWViol=" + twViol);
		print.append("\n" + "--------------------------------------------------" + "\n");
		return print.toString();
	}
	
	public void calculateTotalCostViol() {
		total = travelTime + loadViol + durationViol + twViol;
	}
	
	/**
	 * Set the total cost based on alpha, beta, gamma
	 * @param alpha
	 * @param beta
	 * @param gamma
	 */
	public void calculateTotal(double alpha, double beta, double gamma) {
		total = travelTime + alpha * loadViol + beta * durationViol + gamma * twViol;
	}
	
	public void setLoadViol(double capacityviol) {
		this.loadViol = capacityviol;
	}
	
	public void setDurationViol(double durationviol) {
		this.durationViol = durationviol;
	}
	
	public void addLoadViol(double capacityviol) {
		this.loadViol += capacityviol;
	}
	
	public void addDurationViol(double durationviol) {
		this.durationViol += durationviol;
	}
	
	public void addTWViol(double TWviol) {
		this.twViol += TWviol;
	}
	
	/**
	 * Add cost to the total cost
	 * @param cost
	 */
	public void addTravel(double cost) {
		travelTime +=cost;
	}
	
	public void setTravelTime(double travelTime) {
		this.travelTime = travelTime;
	}
	
	/**
	 * @return the totalcost
	 */
	public double getTravel() {
		return travelTime;
	}

	/**
	 * @return the totalcostviol
	 */
	public double getTotal() {
		return total;
	}
	/**
	 * @return the capacityviol
	 */
	public double getLoadViol() {
		return loadViol;
	}

	/**
	 * @return the durationviol
	 */
	public double getDurationViol() {
		return durationViol;
	}

	/**
	 * @return the tWviol
	 */
	public double getTwViol() {
		return twViol;
	}

	public void initialize() {
		total             	= 0;
		travelTime        	= 0;
		load		      	= 0;
		serviceTime		  	= 0;
		waitingTime			= 0;
		
		loadViol			= 0;
		durationViol 		= 0;
		twViol       		= 0;
		
		returnToDepotTime 	= 0;
		depotTwViol       	= 0;
		
	}
	
	// check if a cost has violations
    public boolean checkFeasible() {
    	if (this.loadViol == 0 && this.durationViol == 0 && this.twViol == 0) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    public double getDuration(){
    	return this.serviceTime + this.waitingTime;
    }

	/**
	 * @return the load
	 */
	public double getLoad() {
		return load;
	}

	/**
	 * @param load the load to set
	 */
	public void setLoad(double load) {
		this.load = load;
	}

	/**
	 * @return the serviceTime
	 */
	public double getServiceTime() {
		return serviceTime;
	}

	/**
	 * @param serviceTime the serviceTime to set
	 */
	public void setServiceTime(double serviceTime) {
		this.serviceTime = serviceTime;
	}

	/**
	 * @return the waitingTime
	 */
	public double getWaitingTime() {
		return waitingTime;
	}

	/**
	 * @param waitingTime the waitingTime to set
	 */
	public void setWaitingTime(double waitingTime) {
		this.waitingTime = waitingTime;
	}

	/**
	 * @return the returnToDepotTime
	 */
	public double getReturnToDepotTime() {
		return returnToDepotTime;
	}

	/**
	 * @param returnToDepotTime the returnToDepotTime to set
	 */
	public void setReturnToDepotTime(double returnToDepotTime) {
		this.returnToDepotTime = returnToDepotTime;
	}

	/**
	 * @return the depotTwViol
	 */
	public double getDepotTwViol() {
		return depotTwViol;
	}

	/**
	 * @param depotTwViol the depotTwViol to set
	 */
	public void setDepotTwViol(double depotTwViol) {
		this.depotTwViol = depotTwViol;
	}

	/**
	 * @return the travelTime
	 */
	public double getTravelTime() {
		return travelTime;
	}

	/**
	 * @param total the total to set
	 */
	public void setTotal(double total) {
		this.total = total;
	}

	/**
	 * @param twViol the twViol to set
	 */
	public void setTwViol(double twViol) {
		this.twViol = twViol;
	}
}
