package com.mdvrp;

/**
 * Customer class stores information about one customer which implements the Vertex interface.
 * Stores the number of the customer, coordinates, service duration, capacity,
 */
public class Customer {
	private int number;
	private double xCoordinate;
	private double yCoordinate;
	private double serviceDuration;     // duration that takes to dispatch the delivery    
	private double load;                // capacity of the pack that is expecting
	private int frequency;              // frequency of visit
	private int combinationsVisitsNr;   // number of possible visits combinations
	private int[][] combinationsList;   // combinationslist[i][j] where i = visit combinations nr and j = frequency
	private int startTw;                // beginning of time window (earliest time for start of service),if any
	private int endTw;                  // end of time window (latest time for start of service), if any
	private int patternUsed;            // the combination of 
	private Depot assignedDepot;        // the depot from which the customer will be served
	private double arriveTime;          // time at which the car arrives to the customer
	private double waitingTime;         // time to wait until arriveTime equal start time window
	private double twViol;              // value of time window violation, 0 if none
	private double[] anglesToDepots;
	
	public Customer() {
		xCoordinate          = 0;
		yCoordinate          = 0;
		serviceDuration      = 0;
		load                 = 0;
		frequency            = 0;
		combinationsVisitsNr = 0;
		startTw              = 0;
		endTw                = 0;
		arriveTime           = 0;
		waitingTime          = 0;
		twViol               = 0;
		
	}
	
	public Customer(Customer customer) {
		this.number 			= customer.number;
		this.xCoordinate 		= customer.xCoordinate;
		this.yCoordinate 		= customer.yCoordinate;
		this.serviceDuration 	= customer.serviceDuration;
		this.load 				= customer.load;
		this.frequency 			= customer.frequency;
		this.combinationsVisitsNr = customer.combinationsVisitsNr;
		this.combinationsList 	= customer.combinationsList;
		this.startTw 			= customer.startTw;
		this.endTw 				= customer.endTw;
		this.patternUsed 		= customer.patternUsed;
		this.assignedDepot 		= customer.assignedDepot;
		this.arriveTime 		= new Double(customer.arriveTime);
		this.waitingTime 		= new Double(customer.waitingTime);
		this.twViol 			= new Double(customer.twViol);
		this.anglesToDepots 	= customer.anglesToDepots;
	}

	/**
	 * This return a string with formated customer data
	 * @return
	 */
	public String print() {
		StringBuffer print = new StringBuffer();
		print.append("\n");
		print.append("\n" + "--- Customer " + number + " -----------------------------------");
		print.append("\n" + "| x=" + xCoordinate + " y=" + yCoordinate);
		print.append("\n" + "| ServiceDuration=" + serviceDuration + " Demand=" + load);
		print.append("\n" + "| frequency=" + frequency + " visitcombinationsnr=" + combinationsVisitsNr);
		print.append("\n" + "| AssignedDepot=" + assignedDepot.getNumber());
		print.append("\n" + "| StartTimeWindow=" + startTw + " EndTimeWindow=" + endTw);
		print.append("\n" + "| AnglesToDepots: ");
		for (int i = 0; i < anglesToDepots.length; ++i) {
			print.append(anglesToDepots[i] + " ");
		}
		print.append("\n" + "--------------------------------------------------");
		return print.toString();
		
	}
	
	/**
	 * get the time at which the car arrives to the customer
	 * @return dispatchtime
	 */
	public double getArriveTime() {
		return arriveTime;
	}
	
	/**
	 * set the time at which the car arrives to the customer
	 * @param dispatchtime
	 */
	public void setArriveTime(double dispatchtime) {
		this.arriveTime = dispatchtime;
	}
	
	/**
	 * @return the customernumber
	 */
	public int getNumber() {
		return this.number;
	}


	/**
	 * @param customernumber the customernumber to set
	 */
	public void setNumber(int customernumber) {
		this.number = customernumber;
	}


	/**
	 * @return the xcoordinate
	 */
	public double getXCoordinate() {
		return xCoordinate;
	}


	/**
	 * @param xcoordinate the xcoordinate to set
	 */
	public void setXCoordinate(double xcoordinate) {
		this.xCoordinate = xcoordinate;
	}


	/**
	 * @return the ycoordinate
	 */
	public double getYCoordinate() {
		return yCoordinate;
	}


	/**
	 * @param ycoordinate the ycoordinate to set
	 */
	public void setYCoordinate(double ycoordinate) {
		this.yCoordinate = ycoordinate;
	}


	/**
	 * @return the serviceduration
	 */
	public double getServiceDuration() {
		return serviceDuration;
	}


	/**
	 * @param serviceduration the serviceduration to set
	 */
	public void setServiceDuration(double serviceduration) {
		this.serviceDuration = serviceduration;
	}


	/**
	 * @return the demand
	 */
	public double getCapacity() {
		return load;
	}


	/**
	 * @param demand the demand to set
	 */
	public void setCapacity(double demand) {
		this.load = demand;
	}


	/**
	 * @return the startTW
	 */
	public int getStartTw() {
		return startTw;
	}


	/**
	 * @param startTW the startTW to set
	 */
	public void setStartTw(int startTW) {
		this.startTw = startTW;
	}


	/**
	 * @return the endTW
	 */
	public int getEndTw() {
		return endTw;
	}


	/**
	 * @param endTW the endTW to set
	 */
	public void setEndTw(int endTW) {
		this.endTw = endTW;
	}


	/**
	 * @return the frequency
	 */
	public int getFrequency() {
		return frequency;
	}


	/**
	 * @param frequency the frequency to set
	 */
	public void setFrequency(int frequency) {
		this.frequency = frequency;
	}


	/**
	 * @return the patternsNr
	 */
	public int getCombinationsVisitsNr() {
		return combinationsVisitsNr;
	}


	/**
	 * @param patternsNr the patternsNr to set
	 */
	public void setCombinationsVisitsNr(int patternsNr) {
		this.combinationsVisitsNr = patternsNr;
	}


	/**
	 * @return the combinationslist
	 */
	public int[][] getCombinationsList() {
		return combinationsList;
	}


	/**
	 * @param combinationslist the combinationslist to set
	 */
	public void setCombinationsList(int[][] combinationslist) {
		this.combinationsList = combinationslist;
	}


	/**
	 * @return the assigneddepot
	 */
	public Depot getAssignedDepot() {
		return assignedDepot;
	}


	/**
	 * @param assigneddepot the assigneddepot to set
	 */
	public void setAssignedDepot(Depot assigneddepot) {
		this.assignedDepot = assigneddepot;
	}

	/**
	 * Get the angle of the customer with the depot passed as parameter
	 */
	public double getAngleToDepot(int depotnr) {
		return anglesToDepots[depotnr];
	}
	
	/**
	 * @return the anglestodepots
	 */
	public double[] getAnglesToDepots() {
		return anglesToDepots;
	}


	/**
	 * @param anglestodepots the anglestodepots to set
	 */
	public void setAnglesToDepots(double[] anglestodepots) {
		this.anglesToDepots = anglestodepots;
	}


	/**
	 * @return the patternused
	 */
	public int getPatternUsed() {
		return patternUsed;
	}


	/**
	 * @param patternused the patternused to set
	 */
	public void setPatternUsed(int patternused) {
		this.patternUsed = patternused;
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
	 * @return the twViol
	 */
	public double getTwViol() {
		return twViol;
	}

	/**
	 * @param twViol the twViol to set
	 */
	public void setTwViol(double twViol) {
		this.twViol = twViol;
	}



	/*
	// get depot i from depot list
	public Depot getDepot(int index){
		return depotlist.get(index);
	}
	*/

}
