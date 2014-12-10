package com.MyGeneticA;

import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;


public class MyGAObjectiveFunction{
	private static Instance instance;
 	//private double lambda;		// λ
    
    public MyGAObjectiveFunction( Instance instance ) 
    {   
    	MyGAObjectiveFunction.setInstance(instance);
     	//lambda     = 0.5 * Math.sqrt(instance.getVehiclesNr() * instance.getCustomersNr());
    }   // end constructor

    
    
    /**
     * This function calculates costs for each route in the solution and each of these is added to the total cost of the solution
     * @param solution
     */
    public double evaluateAbsolutely(MyGASolution sol){
    	Route route;
    	
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
		
		return sol.getCost().total;
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
	 * @return the instance
	 */
	public static Instance getInstance() {
		return instance;
	}

	/**
	 * @param instance the instance to set
	 */
	public static void setInstance(Instance instance) {
		MyGAObjectiveFunction.instance = instance;
	}
}   // end class MyObjectiveFunction
