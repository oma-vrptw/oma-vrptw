package com.TabuSearch;

import org.coinor.opents.*;

import com.mdvrp.Cost;
import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;

@SuppressWarnings("serial")
public class MySwapMove implements ComplexMove {
	private Instance instance;
	private Customer customer;
    private int deleteDepotNr;
    private int deleteRouteNr;
    private int deletePositionIndex;
    private int insertDepotNr;
    private int insertRouteNr;
    private int insertPositionIndex;
    
    
    public MySwapMove( Instance instance, Customer customer, int deleteDepotNr, int deleteRouteNr, int deletePositionIndex, int insertDepotNr , int insertRouteNr)
    {   
        this.instance            = instance;
        this.customer            = customer;
        this.deleteDepotNr       = deleteDepotNr;
        this.deleteRouteNr       = deleteRouteNr;
        this.deletePositionIndex = deletePositionIndex;
        this.insertDepotNr       = insertDepotNr;
        this.insertRouteNr       = insertRouteNr;
    }   // end constructor
    
    /**
     * This function make the move on the solution and updates the originalPosition to be able to undo quickly
     * @param solution
     */
    public void operateOn( Solution solution )
    {
    	MySolution sol = (MySolution)solution;
    	Route insertRoute = sol.getRoute(insertDepotNr, insertRouteNr);
    	Route deleteRoute = sol.getRoute(deleteDepotNr, deleteRouteNr);
    	Cost initialInsertCost = new Cost(insertRoute.getCost());
    	Cost initialDeleteCost = new Cost(deleteRoute.getCost());
    	evaluateDeleteRoute(deleteRoute, customer, deletePositionIndex);
    	evaluateInsertRoute(insertRoute, customer, insertPositionIndex);
    	evaluateTotalCostVariation(sol, this, initialInsertCost, initialDeleteCost);
    	sol.incrementBs(this);
    }   // end operateOn
    
    /**
     * Set the insert position index of the move
     * (is done in objective function, for performance factor)
     * @param index
     */
    public void setInsertPositionIndex(int index) {
    	this.insertPositionIndex = index;
    } 
    

	@Override
	public int[] attributesDelete() {
		return new int[]{ deleteDepotNr, deleteRouteNr, customer.getNumber(), 0, 0};
	}
	
	@Override
	public int[] attributesInsert() {
		return new int[]{ insertDepotNr, insertRouteNr, customer.getNumber(), 0, 0};
	}
	
    private void evaluateTotalCostVariation(MySolution sol, MySwapMove move,
			Cost initialInsertCost, Cost initialDeleteCost) 
    {
    	Route insertRoute = sol.getRoute(move.getInsertDepotNr(), move.getInsertRouteNr());
    	Route deleteRoute = sol.getRoute(move.getDeleteDepotNr(), move.getDeleteRouteNr());
    	sol.addTravelTime( - initialInsertCost.travelTime - initialDeleteCost.travelTime
	                       +  deleteRoute.getCost().travelTime + insertRoute.getCost().travelTime);
    	sol.addServiceTime( - initialInsertCost.serviceTime - initialDeleteCost.serviceTime
                            + deleteRoute.getCost().serviceTime + insertRoute.getCost().serviceTime);
    	sol.addWaitingTime( - initialInsertCost.waitingTime - initialDeleteCost.waitingTime
                            +  deleteRoute.getCost().waitingTime + insertRoute.getCost().waitingTime);
    	
    	sol.getCost().loadViol += - initialInsertCost.loadViol - initialDeleteCost.loadViol
    			                      + deleteRoute.getCost().loadViol + insertRoute.getCost().loadViol;
                
    	
    	sol.getCost().durationViol += - initialInsertCost.durationViol - initialDeleteCost.durationViol
    	                              + deleteRoute.getCost().durationViol + insertRoute.getCost().durationViol;
    			                
    	sol.getCost().twViol += - initialInsertCost.twViol - initialDeleteCost.twViol
    			                + deleteRoute.getCost().twViol + insertRoute.getCost().twViol;
    	
    	sol.getCost().waitingTime = Math.abs(sol.getCost().waitingTime) < instance.getPrecision() ? 0 : sol.getCost().waitingTime;
    	sol.getCost().loadViol = Math.abs(sol.getCost().loadViol) < instance.getPrecision() ? 0 : sol.getCost().loadViol;
    	sol.getCost().durationViol = Math.abs(sol.getCost().durationViol) < instance.getPrecision() ? 0 : sol.getCost().durationViol;
    	sol.getCost().twViol = Math.abs(sol.getCost().twViol) < instance.getPrecision() ? 0 : sol.getCost().twViol;
    	
		sol.getCost().calculateTotal(sol.getAlpha(), sol.getBeta(), sol.getGamma());
	}
	
	
	/**
     * This function simulate the insertion of the customer in the given route on the given position.
     * Computes the new cost and return it.
     * It is an optimized version of the evaluate route. Calculates only for the customers affected
     * by the insertion. Starts from the given position and could finish before reaching the end of
     * the list if there is no modification in the arrive time at the customers.
     * Does not alter the route or the customer
     * @param route
     * @param customer
     * @param position
     * @return
     */
	private void evaluateInsertRoute(Route route, Customer customer, int position) {
    	Cost varCost = route.getCost();
    	double arriveCustomer = 0;
    	double arriveNextCustomer = 0;
    	double waitingTimeCustomer = 0;
    	double waitingTimeNextCustomer = 0;
    	double twViolCustomer = 0;
    	double twViolNextCustomer = 0;

    	// if route is empty insert: depot - customer - depot
    	if(route.isEmpty()) {
    		varCost.initialize();
    		// arrive time at the customer
    		arriveCustomer = route.getDepot().getStartTw()
    				             + instance.getTravelTime(route.getDepotNr(), customer.getNumber());
        	// waiting time for the customer if any
    		waitingTimeCustomer = Math.max(0, customer.getStartTw() - arriveCustomer);
    		// time window violation of the customer if any
    		twViolCustomer = Math.max(0, arriveCustomer - customer.getEndTw());
    		// arrive time at the depot
    		arriveNextCustomer = Math.max(customer.getStartTw(), arriveCustomer)
    				                 + customer.getServiceDuration()
    				                 + instance.getTravelTime(customer.getNumber(), route.getDepotNr());
    		// time window violation of the depot if any
    		twViolNextCustomer = Math.max(0, arriveNextCustomer - route.getDepot().getEndTw());
    		//variation of the travel time
    		varCost.travelTime = instance.getTravelTime(route.getDepotNr(), customer.getNumber())
    						   + instance.getTravelTime(customer.getNumber(), route.getDepotNr());
    		// variation of the capacity
    		varCost.load = customer.getCapacity();
    		// route service time
    		varCost.serviceTime = customer.getServiceDuration();
    		//variation of the waiting time
    		varCost.waitingTime = waitingTimeCustomer;
    		// variation of the time windows violation
    		varCost.twViol = twViolCustomer + twViolNextCustomer;
    		
    		customer.setArriveTime(arriveCustomer);
    		customer.setWaitingTime(waitingTimeCustomer);
    		customer.setTwViol(twViolCustomer);
    		varCost.returnToDepotTime = arriveNextCustomer;
    		varCost.depotTwViol = twViolNextCustomer;
    	}else{    	
    		// insertion at the end of the list: customer before - customer - depot
	    	if(position == route.getCustomersLength()){
	    		Customer customerBefore = route.getCustomer(position - 1);
	    		arriveCustomer = Math.max(customerBefore.getStartTw(), customerBefore.getArriveTime())
	    				             + customerBefore.getServiceDuration()
	    				             + instance.getTravelTime(customerBefore.getNumber(), customer.getNumber());
	        	// waiting time for the customer if any
	    		waitingTimeCustomer = Math.max(0, customer.getStartTw() - arriveCustomer);
	    		// time window violation of the customer if any
	    		twViolCustomer = Math.max(0, arriveCustomer - customer.getEndTw());
	    		// arrive time at the depot
	    		arriveNextCustomer = Math.max(customer.getStartTw(), arriveCustomer)
	    				                 + customer.getServiceDuration()
	    				                 + instance.getTravelTime(customer.getNumber(), route.getDepotNr());
	    		// time window violation of the depot if any
	    		twViolNextCustomer = Math.max(0, arriveNextCustomer - route.getDepot().getEndTw());
	    		//variation of the travel time
	    		varCost.travelTime += - instance.getTravelTime(customerBefore.getNumber(), route.getDepotNr())
	    				              + instance.getTravelTime(customerBefore.getNumber(), customer.getNumber())
	    							  + instance.getTravelTime(customer.getNumber(), route.getDepotNr());
	    		// variation of the capacity
	    		varCost.load += customer.getCapacity();
	    		// route service time
	    		varCost.serviceTime += customer.getServiceDuration();
	    		//variation of the waiting time
	    		varCost.waitingTime += waitingTimeCustomer;
	    		// variation of the time windows violation
	    		varCost.twViol += - varCost.depotTwViol + twViolCustomer + twViolNextCustomer;
	    		
	    		customer.setArriveTime(arriveCustomer);
	    		customer.setWaitingTime(waitingTimeCustomer);
	    		customer.setTwViol(twViolCustomer);
	    		varCost.returnToDepotTime = arriveNextCustomer;
	    		varCost.depotTwViol = twViolNextCustomer;
	    		
	    	}else{
	    		double variation = 0;
	    		Customer customerAfter = route.getCustomer(position);
	    		// insertion on the first position: depot - customer - customer after
	    		if(position == 0){
	    			// time before arrive at the customer
		    		arriveCustomer = route.getDepot().getStartTw()
		    				             + instance.getTravelTime(route.getDepotNr(), customer.getNumber());
		    		//variation of the travel time
		    		varCost.travelTime += - instance.getTravelTime(route.getDepotNr(), customerAfter.getNumber())
		    				              + instance.getTravelTime(route.getDepotNr(), customer.getNumber())
		    							  + instance.getTravelTime(customer.getNumber(), customerAfter.getNumber());
	    	
		    	// insertion in the middle of the list:  customer before - customer - customer after
		    	}else{
		    		Customer customerBefore = route.getCustomer(position - 1);
		    		// time before arrive at the customer
		    		arriveCustomer = Math.max(customerBefore.getStartTw(), customerBefore.getArriveTime())
		    				       + customerBefore.getServiceDuration()
		    				       + instance.getTravelTime(customerBefore.getNumber(), customer.getNumber());
		    		//variation of the travel time
		    		varCost.travelTime += - instance.getTravelTime(customerBefore.getNumber(), customerAfter.getNumber())
		    				              + instance.getTravelTime(customerBefore.getNumber(), customer.getNumber())
		    							  + instance.getTravelTime(customer.getNumber(), customerAfter.getNumber());
		    		} // end if else beginning or middle
	    		// this code runs when inserting at the beginning or in the middle
	    		
	        	// waiting time for the customer if any
	    		waitingTimeCustomer = Math.max(0, customer.getStartTw() - arriveCustomer);
	    		// time window violation of the customer if any
	    		twViolCustomer = Math.max(0, arriveCustomer - customer.getEndTw());
	    		// before arrive time at the customer after
	    		arriveNextCustomer = Math.max(customer.getStartTw(), arriveCustomer)
	    				           + customer.getServiceDuration()
	    				           + instance.getTravelTime(customer.getNumber(), customerAfter.getNumber());
	    		// waiting time for the customer after if any
	    		waitingTimeNextCustomer = Math.max(0, customerAfter.getStartTw() - arriveNextCustomer);
	    		// time window violation of the customer after if any
	    		twViolNextCustomer = Math.max(0, arriveNextCustomer - customerAfter.getEndTw());
	    		
	    		// variation of the capacity
	    		varCost.load += customer.getCapacity();
	    		// route service time
	    		varCost.serviceTime += customer.getServiceDuration();
	    		//variation of the waiting time
	    		varCost.waitingTime += - customerAfter.getWaitingTime() + waitingTimeCustomer + waitingTimeNextCustomer;
	    		// variation of the time windows violation
	    		varCost.twViol +=  - customerAfter.getTwViol() + twViolCustomer + twViolNextCustomer;
	    		
	    		variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getArriveTime() - customerAfter.getWaitingTime();
	    		variation = Math.abs(variation) < instance.getPrecision() ? 0 : variation;
	    		
	    		customer.setArriveTime(arriveCustomer);
	    		customer.setWaitingTime(waitingTimeCustomer);
	    		customer.setTwViol(twViolCustomer);
	    		customerAfter.setArriveTime(arriveNextCustomer);
	    		customerAfter.setWaitingTime(waitingTimeNextCustomer);
	    		customerAfter.setTwViol(twViolNextCustomer);

	    		// if there is a variation update the nodes after too
	    		int i = position + 1;
	    		while (variation != 0 && i < route.getCustomersLength()){	
	    				customerAfter = route.getCustomer(i);
	    				// arrive at the customer after
	    				arriveNextCustomer = customerAfter.getArriveTime() + variation;
	    			    waitingTimeNextCustomer = Math.max(0, customerAfter.getStartTw() - arriveNextCustomer);
	    			    twViolNextCustomer = Math.max(0, arriveNextCustomer - customerAfter.getEndTw());
	    			    //variation of the waiting time
	    	    		varCost.waitingTime += - customerAfter.getWaitingTime() + waitingTimeNextCustomer;
	    	    		// variation of the time windows violation
	    	    		varCost.twViol += - customerAfter.getTwViol() + twViolNextCustomer;
	    	    		
	    	    		variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getArriveTime() - customerAfter.getWaitingTime();
	    	    		variation = Math.abs(variation) < instance.getPrecision() ? 0 : variation;
	    	    		
	    	    		customerAfter.setArriveTime(arriveNextCustomer);
	    	    		customerAfter.setWaitingTime(waitingTimeNextCustomer);
	    	    		customerAfter.setTwViol(twViolNextCustomer);
	    	    		i++;
	    		}// end while
	    			
	    		if(i == route.getCustomersLength() && variation != 0 ){
	    			// update the return to the depot
	    			arriveNextCustomer = varCost.returnToDepotTime + variation;
	    			twViolNextCustomer = Math.max(0, arriveNextCustomer - route.getDepot().getEndTw());
	    			// variation of the time windows violation
    	    		varCost.twViol += - varCost.depotTwViol + twViolNextCustomer;
    	    		
    	    		varCost.returnToDepotTime = arriveNextCustomer;
    	    		varCost.depotTwViol = twViolNextCustomer;
	    		}// end if return to depot
	    		
	    		} // end if else of position cases
    	} // end if else route is empty
    	
    	route.addCustomer(customer, position);
//    	// be careful about precision; if there are subtraction
		varCost.waitingTime = Math.abs(varCost.waitingTime) < instance.getPrecision() ? 0 : varCost.waitingTime;
		varCost.twViol = Math.abs(varCost.twViol) < instance.getPrecision() ? 0 : varCost.twViol;
    	
		varCost.setLoadViol(Math.max(0, varCost.load - route.getLoadAdmited()));
		varCost.setDurationViol(Math.max(0, varCost.getDuration() - route.getDurationAdmited()));

		
    } // end method evaluate insert route
	
	/**
	 * This function delete the customer in the given route on the given position and updates
	 * the cost.
     * It is an optimized version of the evaluate route. Calculates only for the customers affected
     * by the deletion. Starts from the given position and could finish before reaching the end of
     * the list if there is no modification in the arrive time at the customers.
     * Does alter the route.
	 * @param route
	 * @param position
	 * @return
	 */
	private void evaluateDeleteRoute(Route route, Customer customer, int position) {
    	Cost varCost = route.getCost();
    	double arriveNextCustomer = 0;
    	double waitingTimeNextCustomer = 0;
    	double twViolNextCustomer = 0;

    	// if route has only the customer that will be deleted
    	if(route.getCustomersLength() - 1 == 0) {
    		varCost.initialize();    		
    	
    	}else{    	
    		// case when customer is the last one: customer before - depot
	    	if(position == route.getCustomersLength() - 1){
	    		Customer customerBefore = route.getCustomer(position - 1);
	    		//arrive time at the depot
	    		arriveNextCustomer = Math.max(customerBefore.getStartTw(), customerBefore.getArriveTime())
	    				           + customerBefore.getServiceDuration()
	    				           + instance.getTravelTime(customerBefore.getNumber(), route.getDepotNr());
	    		// time window violation of the depot if any
	    		twViolNextCustomer = Math.max(0, arriveNextCustomer - route.getDepot().getEndTw());
	    		//variation of the travel time
	    		varCost.travelTime += - instance.getTravelTime(customerBefore.getNumber(), customer.getNumber())
	    							  - instance.getTravelTime(customer.getNumber(), route.getDepotNr())
	    						 	  + instance.getTravelTime(customerBefore.getNumber(), route.getDepotNr());
	    				            
	    		// variation of the capacity
	    		varCost.load -= customer.getCapacity();
	    		// route service time
	    		varCost.serviceTime -= customer.getServiceDuration();
	    		//variation of the waiting time
	    		varCost.waitingTime -= customer.getWaitingTime();
	    		// variation of the time windows violation
	    		varCost.twViol += - customer.getTwViol() - route.getDepotTwViol() + twViolNextCustomer;
	    		
	    		varCost.returnToDepotTime = arriveNextCustomer;
	    		varCost.depotTwViol = twViolNextCustomer;
	    		    		
	    	}else{
	    		double variation = 0;
	    		Customer customerAfter = route.getCustomer(position + 1);
	    		// delete on the first position
	    		if(position == 0){
	    			// time before arrive at customer after
		    		arriveNextCustomer = route.getDepot().getStartTw()
		    				                 + instance.getTravelTime(route.getDepotNr(), customerAfter.getNumber());
		    		//variation of the travel time
		    		varCost.travelTime  += - instance.getTravelTime(route.getDepotNr(), customer.getNumber())
		    							   - instance.getTravelTime(customer.getNumber(), customerAfter.getNumber())
		    				               + instance.getTravelTime(route.getDepotNr(), customerAfter.getNumber());
	    	
		    	// delete in the middle of the list
		    	}else{
		    		Customer customerBefore = route.getCustomer(position - 1);
		    		// time before arrive at customer after
		    		arriveNextCustomer = Math.max(customerBefore.getStartTw(), customerBefore.getArriveTime())
		    				           + customerBefore.getServiceDuration()
		    				           + instance.getTravelTime(customerBefore.getNumber(), customerAfter.getNumber());
		    		//variation of the travel time
		    		varCost.travelTime += - instance.getTravelTime(customerBefore.getNumber(), customer.getNumber())
		    							  - instance.getTravelTime(customer.getNumber(), customerAfter.getNumber())
		    				              + instance.getTravelTime(customerBefore.getNumber(), customerAfter.getNumber());
		    		} // end if else beginning or middle
	    		// this code runs when inserting at the beginning or in the middle
	    		
	        	// waiting time for the customer after if any
	    		waitingTimeNextCustomer = Math.max(0, customerAfter.getStartTw() - arriveNextCustomer);
	    		// time window violation of the customer after if any
	    		twViolNextCustomer = Math.max(0, arriveNextCustomer - customerAfter.getEndTw());
	    		
	    		// variation of the capacity
	    		varCost.load -= customer.getCapacity();
	    		// route service time
	    		varCost.serviceTime -= customer.getServiceDuration();
	    		//variation of the waiting time
	    		varCost.waitingTime += - customer.getWaitingTime() - customerAfter.getWaitingTime() + waitingTimeNextCustomer;
	    		// variation of the time windows violation
	    		varCost.twViol += - customer.getTwViol() - customerAfter.getTwViol() +  twViolNextCustomer;
	    		
//	    		variation = Math.max(customerAfter.getStartTw(), arriveNextCustomer) - Math.max(customerAfter.getStartTw(), customerAfter.getArriveTime());
	    		variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getArriveTime() - customerAfter.getWaitingTime();
	    		variation = Math.abs(variation) < instance.getPrecision() ? 0 : variation;
	    		
	    		customerAfter.setArriveTime(arriveNextCustomer);
	    		customerAfter.setWaitingTime(waitingTimeNextCustomer);
	    		customerAfter.setTwViol(twViolNextCustomer);
	    		

	    		// if there is a variation update the nodes after too
	    		// the node after the customer is already updated
	    		int i = position + 2;
	    		while (variation != 0 && i < route.getCustomersLength()){	
	    				customerAfter = route.getCustomer(i);
	    				// arrive at the customer after
	    				arriveNextCustomer = customerAfter.getArriveTime() + variation;
	    			    waitingTimeNextCustomer = Math.max(0, customerAfter.getStartTw() - arriveNextCustomer);
	    			    twViolNextCustomer = Math.max(0, arriveNextCustomer - customerAfter.getEndTw());
	    			    //variation of the waiting time
	    	    		varCost.waitingTime += -customerAfter.getWaitingTime() + waitingTimeNextCustomer;
	    	    		// variation of the time windows violation
	    	    		varCost.twViol += -customerAfter.getTwViol() + twViolNextCustomer;
	    	    		
//	    	    		variation = Math.max(customerAfter.getStartTw(), arriveNextCustomer) - Math.max(customerAfter.getStartTw(), customerAfter.getArriveTime());
	    	    		variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getArriveTime() - customerAfter.getWaitingTime();
	    	    		variation = Math.abs(variation) < instance.getPrecision() ? 0 : variation; 
	    	    		
	    	    		customerAfter.setArriveTime(arriveNextCustomer);
	    	    		customerAfter.setWaitingTime(waitingTimeNextCustomer);
	    	    		customerAfter.setTwViol(twViolNextCustomer);
	    	    		
	    	    		i++;
	    		}// end while
	    			
	    		// update depot violation too if any
	    		if(i == route.getCustomersLength() && variation != 0 ){
	    			// update the return to the depot
	    			arriveNextCustomer = route.getReturnToDepotTime() + variation;
	    			twViolNextCustomer = Math.max(0, arriveNextCustomer - route.getDepot().getEndTw());
	    			// variation of the time windows violation
    	    		varCost.twViol += - route.getDepotTwViol() + twViolNextCustomer;
    	    		
    	    		varCost.returnToDepotTime = arriveNextCustomer;
    	    		varCost.depotTwViol = twViolNextCustomer;
    	    		
	    		}// end if return to depot
    		} // end if else of position cases
    	} // end if else route is empty

    	route.removeCustomer(position);
    	// be careful about precision; if there are subtraction
		varCost.waitingTime = Math.abs(varCost.waitingTime) < instance.getPrecision() ? 0 : varCost.waitingTime;
		varCost.twViol = Math.abs(varCost.twViol) < instance.getPrecision() ? 0 : varCost.twViol;
		
		varCost.setLoadViol(Math.max(0, varCost.load - route.getLoadAdmited()));
		varCost.setDurationViol(Math.max(0, varCost.getDuration() - route.getDurationAdmited()));
		
    } // end method evaluate delete route

	
	/**
	 * This function returns a string containing the move information in readable format
	 */
	public String toString() {
		StringBuffer print = new StringBuffer();
		print.append("--- Move Customer " + customer.getNumber() + "-------------------------------------");
		print.append("\n" + "| DeleteDepot=" + deleteDepotNr + " DeleteRoute=" + deleteRouteNr + " DeletePosition=" + deletePositionIndex);
		print.append("\n" + "| InsertDepot=" + insertDepotNr + " InsertRoute=" + insertRouteNr + " InsertPosition=" + insertPositionIndex);
		print.append("\n" + "--------------------------------------------------");
		return print.toString();
	}

	/**
	 * @return the customer
	 */
	public Customer getCustomer() {
		return customer;
	}
	
	/**
	 * @return the customer number
	 */
	public int getCustomerNr() {
		return customer.getNumber();
	}

	/**
	 * @param customer the customer to set
	 */
	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	/**
	 * @return the deleteDepotNr
	 */
	public int getDeleteDepotNr() {
		return deleteDepotNr;
	}

	/**
	 * @param deleteDepotNr the deleteDepotNr to set
	 */
	public void setDeleteDepotNr(int deleteDepotNr) {
		this.deleteDepotNr = deleteDepotNr;
	}

	/**
	 * @return the deleteRouteNr
	 */
	public int getDeleteRouteNr() {
		return deleteRouteNr;
	}

	/**
	 * @param deleteRouteNr the deleteRouteNr to set
	 */
	public void setDeleteRouteNr(int deleteRouteNr) {
		this.deleteRouteNr = deleteRouteNr;
	}

	/**
	 * @return the deletePositionIndex
	 */
	public int getDeletePositionIndex() {
		return deletePositionIndex;
	}

	/**
	 * @param deletePositionIndex the deletePositionIndex to set
	 */
	public void setDeletePositionIndex(int deletePositionIndex) {
		this.deletePositionIndex = deletePositionIndex;
	}

	/**
	 * @return the insertDepotNr
	 */
	public int getInsertDepotNr() {
		return insertDepotNr;
	}

	/**
	 * @param insertDepotNr the insertDepotNr to set
	 */
	public void setInsertDepotNr(int insertDepotNr) {
		this.insertDepotNr = insertDepotNr;
	}

	/**
	 * @return the insertRouteNr
	 */
	public int getInsertRouteNr() {
		return insertRouteNr;
	}

	/**
	 * @param insertRouteNr the insertRouteNr to set
	 */
	public void setInsertRouteNr(int insertRouteNr) {
		this.insertRouteNr = insertRouteNr;
	}

	/**
	 * @return the insertPositionIndex
	 */
	public int getInsertPositionIndex() {
		return insertPositionIndex;
	}
}   // end class MySwapMove
