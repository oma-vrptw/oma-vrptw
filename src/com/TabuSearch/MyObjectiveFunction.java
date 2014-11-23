package com.TabuSearch;

import org.coinor.opents.*;

import com.mdvrp.Cost;
import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;


@SuppressWarnings("serial")
public class MyObjectiveFunction implements ObjectiveFunction {
	private static Instance instance;
 	private double lambda;		// λ
    
    public MyObjectiveFunction( Instance instance ) 
    {   
    	MyObjectiveFunction.setInstance(instance);
     	lambda     = 0.5 * Math.sqrt(instance.getVehiclesNr() * instance.getCustomersNr());
    }   // end constructor

    /**
     * If the proposed move is null evaluate the solution from scratch.
     * Otherwise evaluate the best place to insert the customer in the route proposed,
     * operate the move, control if is subject to penalization and roll back the move
     * in order to preserve the solution for the others remaining moves in the vector
     * of moves.
     */
    public double[] evaluate(Solution solution, Move proposedMove) {
    	MySolution sol = (MySolution)solution;
    	double obj;
        // If move is null, calculate distance from scratch
        if( proposedMove == null ) {
        	evaluateAbsolutely(sol);
        	obj = sol.getCost().total;
        	     	
        	return new double[]{Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, sol.getCost().travelTime, sol.getCost().loadViol, sol.getCost().durationViol, sol.getCost().twViol};
        	
        }   // end if: move == null

        // Else calculate incrementally
        else {
        	MySwapMove move = ((MySwapMove)proposedMove);
        	Route insertRoute = sol.getRoute(move.getInsertDepotNr(), move.getInsertRouteNr());	// route on which is performed the insertion
        	Route deleteRoute = sol.getRoute(move.getDeleteDepotNr(), move.getDeleteRouteNr());
        	Cost varInsertCost = new Cost();
        	Cost varDeleteCost;
        	Cost solCost;
        	double penalization = 0;
        	
        	varInsertCost.total = Double.POSITIVE_INFINITY;
        	// evaluate each position of the route to find the best insertion of the getCustomer(); start from 0 and consider also the last position
        	for (int i = 0; i <= insertRoute.getCustomersLength(); ++i) {
        		// evaluate insertion of the customer in the list
        		Cost varCost = evaluateInsertRoute(insertRoute, move.getCustomer(), i);
        		varCost.calculateTotal(sol.getAlpha(), sol.getBeta(), sol.getGamma());
        		// this case covers the situation in which the parameters are too large and the cost will exceed the MAX_VALUE
        		if(varCost.total > Double.MAX_VALUE) {
        			varCost.total = Double.MAX_VALUE;
        		}
        		// if a better insertion is found, set the position to insert in the move and update the minimum cost found
        		if (varInsertCost.total > varCost.total) {
        			move.setInsertPositionIndex(i);
        			varInsertCost = new Cost(varCost);
        		}
        	} // end for
        	
        	varDeleteCost = evaluateDeleteRoute(deleteRoute, move.getCustomer(), move.getDeletePositionIndex());
        	solCost = getTotalCostVariation(sol, move, varInsertCost, varDeleteCost);
        	obj = solCost.total;
            //calculate the penalization
            if (sol.getObjectiveValue()[0] <= obj )
            	penalization = lambda * solCost.total * sol.getBs(move);
            
            double[] returnArray = new double[]{obj + penalization, obj, solCost.travelTime, solCost.loadViol, solCost.durationViol, solCost.twViol};
            
            return returnArray;
        }   // end else: calculate incremental
        
    }   // end evaluate
    
    private Cost getTotalCostVariation(MySolution sol, MySwapMove move,
			Cost varInsertCost, Cost varDeleteCost) 
    {
    	Cost varCost = new Cost(sol.getCost());
    	Route insertRoute = sol.getRoute(move.getInsertDepotNr(), move.getInsertRouteNr());
    	Route deleteRoute = sol.getRoute(move.getDeleteDepotNr(), move.getDeleteRouteNr());
    	varCost.travelTime += -  deleteRoute.getCost().travelTime - insertRoute.getCost().travelTime
    			              + varInsertCost.travelTime + varDeleteCost.travelTime;
    	varCost.loadViol += - deleteRoute.getCost().loadViol- insertRoute.getCost().loadViol
    			                + varInsertCost.loadViol + varDeleteCost.loadViol;             
    	varCost.durationViol += - deleteRoute.getCost().durationViol - insertRoute.getCost().durationViol
    			                + varInsertCost.durationViol + varDeleteCost.durationViol;
    	varCost.twViol += - deleteRoute.getCost().twViol - insertRoute.getCost().twViol
    			          + varInsertCost.twViol + varDeleteCost.twViol;
    	varCost.waitingTime = Math.abs(varCost.waitingTime) < instance.getPrecision() ? 0 : varCost.waitingTime;
    	varCost.loadViol = Math.abs(varCost.loadViol) < instance.getPrecision() ? 0 : varCost.loadViol;
    	varCost.durationViol = Math.abs(varCost.durationViol) < instance.getPrecision() ? 0 : varCost.durationViol;
    	varCost.twViol = Math.abs(varCost.twViol) < instance.getPrecision() ? 0 : varCost.twViol;
    	
		varCost.calculateTotal(sol.getAlpha(), sol.getBeta(), sol.getGamma());
		    	
    	return varCost;
	}
        
    
    /**
     * This function calculates costs for each route in the solution and each of these is added to the total cost of the solution
     * @param solution
     */
    private void evaluateAbsolutely(Solution solution){
    	MySolution sol = (MySolution)solution;
    	Route route;
    	
    	sol.getCost().initialize();
		for (int i = 0; i < sol.getDepotsNr(); ++i) {
			for(int j = 0; j < sol.getDepotVehiclesNr(i); ++j){
				route = sol.getRoute(i, j);
		    	// do the math only if the route is not empty
				if(!route.isEmpty()) {
					evaluateRoute(route);
					sol.getCost().travelTime += route.getCost().getTravel();
					sol.getCost().load += route.getCost().load;
					sol.getCost().serviceTime += route.getCost().serviceTime;
					sol.getCost().waitingTime += route.getCost().waitingTime;
					sol.getCost().addLoadViol(route.getCost().getLoadViol());
					sol.getCost().addDurationViol(route.getCost().getDurationViol());
					sol.getCost().addTWViol(route.getCost().getTwViol());
					
				} // end if route not empty
			}// end for vehicles
		}// end for depots
		sol.getCost().calculateTotalCostViol();
	}// end method evaluateAbsolutely
    
    /**
	 * this function calculates the cost of a route from scratch
	 * @param route
	 */
	private void evaluateRoute(Route route) {
    	double totalTime = 0;
    	double waitingTime = 0;
    	double twViol = 0;
    	Customer customerK;
    	route.initializeTimes();
    	// do the math only if the route is not empty
		if(!route.isEmpty()){
	    	// sum distances between each node in the route
			for (int k = 0; k < route.getCustomersLength(); ++k){
				// get the actual customer
				customerK = route.getCustomer(k);
				// add travel time to the route
				if(k == 0){
					route.getCost().travelTime += getInstance().getTravelTime(route.getDepotNr(), customerK.getNumber());
					totalTime += getInstance().getTravelTime(route.getDepotNr(), customerK.getNumber());
				}else{
					route.getCost().travelTime += getInstance().getTravelTime(route.getCustomerNr(k -1), customerK.getNumber());
					totalTime += getInstance().getTravelTime(route.getCustomerNr(k -1), customerK.getNumber());
				} // end if else
				
				customerK.setArriveTime(totalTime);
				// add waiting time if any
				waitingTime = Math.max(0, customerK.getStartTw() - totalTime);
				route.getCost().waitingTime += waitingTime;
				// update customer timings information
				customerK.setWaitingTime(waitingTime);
				
				totalTime = Math.max(customerK.getStartTw(), totalTime);

				// add time window violation if any
				twViol = Math.max(0, totalTime - customerK.getEndTw());
				route.getCost().addTWViol(twViol);
				customerK.setTwViol(twViol);
				// add the service time to the total
				totalTime += customerK.getServiceDuration();
				// add service time to the route
				route.getCost().serviceTime += customerK.getServiceDuration();
				// add capacity to the route
				route.getCost().load += customerK.getCapacity();
				
			} // end for customers
			
			// add the distance to return to depot: from last node to depot
			totalTime += getInstance().getTravelTime(route.getLastCustomerNr(), route.getDepotNr());
			route.getCost().travelTime += getInstance().getTravelTime(route.getLastCustomerNr(), route.getDepotNr());
			// add the depot time window violation if any
			twViol = Math.max(0, totalTime - route.getDepot().getEndTw());
			route.getCost().addTWViol(twViol);
			// update route with timings of the depot
			route.setDepotTwViol(twViol);
			route.setReturnToDepotTime(totalTime);
			route.getCost().setLoadViol(Math.max(0, route.getCost().load - route.getLoadAdmited()));
			route.getCost().setDurationViol(Math.max(0, route.getDuration() - route.getDurationAdmited()));
			
			route.getCost().setTravelTime(route.getCost().travelTime);
			// update total violation
			route.getCost().calculateTotalCostViol();
			
		} // end if route not empty
		
    } // end method evaluate route
    
    
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
    private Cost evaluateInsertRoute(Route route, Customer customer, int position) {
    	Cost varCost = new Cost(route.getCost());
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
	    	}else {
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
		    	}else {
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
	    		
//	    		variation = Math.max(customerAfter.getStartTw(), arriveNextCustomer) - Math.max(customerAfter.getStartTw(), customerAfter.getArriveTime());
	    		variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getArriveTime() - customerAfter.getWaitingTime();
	    		variation = Math.abs(variation) < instance.getPrecision() ? 0 : variation;

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
	    	    		
//	    	    		variation = Math.max(customerAfter.getStartTw(), arriveNextCustomer) - Math.max(customerAfter.getStartTw(), customerAfter.getArriveTime());
	    	    		variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getArriveTime() - customerAfter.getWaitingTime();
	    	    		variation = Math.abs(variation) < instance.getPrecision() ? 0 : variation;

	    	    		i++;
	    		}// end while
	    			
	    		if(i == route.getCustomersLength() && variation != 0 ){
	    			// update the return to the depot
	    			arriveNextCustomer = varCost.returnToDepotTime + variation;
	    			twViolNextCustomer = Math.max(0, arriveNextCustomer - route.getDepot().getEndTw());
	    			// variation of the time windows violation
    	    		varCost.twViol += - varCost.depotTwViol + twViolNextCustomer;
    	    		
	    		}// end if return to depot
	    		
	    		} // end if else of position cases
    	} // end if else route is empty
    	
		varCost.waitingTime = Math.abs(varCost.waitingTime) < instance.getPrecision() ? 0 : varCost.waitingTime;
		varCost.twViol = Math.abs(varCost.twViol) < instance.getPrecision() ? 0 : varCost.twViol;
    	
		varCost.setLoadViol(Math.max(0, varCost.load - route.getLoadAdmited()));
		varCost.setDurationViol(Math.max(0, varCost.getDuration() - route.getDurationAdmited()));

		return varCost;
    } // end method evaluate insert route
	
	
	/**
	 * This function simulate the deletion of a customer in the given route on the given position.
     * Computes the new cost and return it.
     * It is an optimized version of the evaluate route. Calculates only for the customers affected
     * by the deletion. Starts from the given position and could finish before reaching the end of
     * the list if there is no modification in the arrive time at the customers.
     * Does not alter the route.
	 * @param route
	 * @param position
	 * @return
	 */
    private Cost evaluateDeleteRoute(Route route, Customer customer, int position) {
    	Cost varCost = new Cost(route.getCost());
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
	    	
		    	// insertion in the middle of the list
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
//	    		variation = arriveNextCustomer -customerAfter.getArriveTime();
	    		variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getArriveTime() - customerAfter.getWaitingTime();
	    		variation = Math.abs(variation) < instance.getPrecision() ? 0 : variation; 		

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
//	    	    		variation = arriveNextCustomer -customerAfter.getArriveTime();
	    	    		variation = arriveNextCustomer + waitingTimeNextCustomer - customerAfter.getArriveTime() - customerAfter.getWaitingTime();
	    	    		variation = Math.abs(variation) < instance.getPrecision() ? 0 : variation; 
	    	    			    	    		
	    	    		i++;
	    		}// end while
	    			
	    		// update depot violation too if any
	    		if(i == route.getCustomersLength() && variation != 0 ){
	    			// update the return to the depot
	    			arriveNextCustomer = route.getReturnToDepotTime() + variation;
	    			twViolNextCustomer = Math.max(0, arriveNextCustomer - route.getDepot().getEndTw());
	    			// variation of the time windows violation
    	    		varCost.twViol += - route.getDepotTwViol() + twViolNextCustomer;
    	    		    	    		
	    		}// end if return to depot
	    		
	    		
	    		
	    		
	    		} // end if else of position cases
    	} // end if else route is empty

//    	route.removeCustomer(position);
    	// be careful about precision; if there are subtraction
		varCost.waitingTime = Math.abs(varCost.waitingTime) < instance.getPrecision() ? 0 : varCost.waitingTime;
		varCost.twViol = Math.abs(varCost.twViol) < instance.getPrecision() ? 0 : varCost.twViol;
		
		varCost.setLoadViol(Math.max(0, varCost.load - route.getLoadAdmited()));
		varCost.setDurationViol(Math.max(0, varCost.getDuration() - route.getDurationAdmited()));
		
		return varCost;
    } // end method evaluate delete route
	
	

	/**
	 * @return the instance
	 */
	public static Instance getInstance() {
		return instance;
	}

	/**
	 * @param instance the instance to set
	 */
	public static void setInstance(Instance instance) {
		MyObjectiveFunction.instance = instance;
	}
}   // end class MyObjectiveFunction
