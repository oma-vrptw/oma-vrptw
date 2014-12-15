package com.MyGeneticA;


import java.util.*;

import com.mdvrp.*;

/**
 * Classe che esegue il Clarke-Wright Savings Algorithm (versione sequenziale)
 * senza considerare il vincolo della time window.
 * 
 * @author Alessio
 *
 */
public class MyCW {
	
	private class Saving implements Comparable<Saving>
	{
		
		private double val;
		private int from, to;
		
		public Saving(double val, int from, int to)
		{
			this.val = val;
			this.from = from;
			this.to = to;
		}

		@Override
		public int compareTo(Saving o) 
		{
			if(this.val > o.val) return 1;
			else return -1;
		}
		
		@Override
		public String toString()
		{
			return "S("+from+","+to+") = " + (float)val;
		}
		
	}
	
	private Instance instance;
	private int numberOfCustomers, numberOfVehicles, numberOfUsedVehicles, numberOfGenes;
	private double capacity;
	
	private double[][] distances;
	private ArrayList<Saving> savings;
	
	private Map<Integer, Integer> assignedRoute;
	private Map<Integer, Route> routes;
	private Map<Integer, Boolean> from, to;
	
	public MyCW(int numberOfGenes, Instance instance) 
	{
		numberOfUsedVehicles = 0;
		this.numberOfGenes = numberOfGenes;
		this.instance = instance;
		numberOfCustomers = instance.getCustomersNr();
		numberOfVehicles = instance.getVehiclesNr();
		capacity = instance.getCapacity(0, 0);
		distances = instance.getDistances();
		savings = new ArrayList<>();
		
		routes = new HashMap<>();		
		assignedRoute = new HashMap<>();
		from = new HashMap<>();
		to = new HashMap<>();
	}
	
	private boolean IsUsable(Saving s)
	{
		boolean esito = from.get(s.from) && to.get(s.to);
		return esito;
	}

	
	private void InitSavings()
	{	
		double value;
		
		for(int i=0; i<distances.length; i++)
		{
			for(int j=i+1; j<distances.length; j++)
			{
				// Se sono entrambi customer
				if(i < numberOfCustomers && j < numberOfCustomers)
				{
					value = distances[i][numberOfCustomers] + distances[numberOfCustomers][j] - distances[i][j];
					
					if(value > 0) 
					{
						Saving s = new Saving(value, i, j);
						savings.add(s);
						
						if(!from.containsKey(i)) from.put(i, true);
						if(!to.containsKey(j)) to.put(j, true);
					}
				}
			}
		}
		Collections.sort(savings, Collections.reverseOrder());

	}
	
	private boolean Test(int s, int d)
	{
		boolean test1=false, test2=false, test3=false;
		
		Integer sourceIndex = assignedRoute.get(s);
		Integer destIndex = assignedRoute.get(d);
		
		test1 = sourceIndex.equals(destIndex);
		// if(!test1) System.out.println("ERRORE IN " + s + "-" + d + ": sono ancora assegnati a route diverse.");
		
		Route source = routes.get(sourceIndex);
		Route dest = routes.get(destIndex);
		
		test2 = source != null && dest != null;
		// if(!test2) System.out.println("ERRORE IN " + s + "-" + d + ": una delle route è inesistente.");
		
		try
		{
			test3 = source.equals(dest);
		}
		catch(Exception e)
		{
			// System.out.println("ERRORE IN " + s + "-" + d + ": la source route è null.");
		}
		
		// if(!test3) System.out.println("ERRORE IN " + s + "-" + d + ": source e dest hanno route diverse.");
		
		return(test1 && test2 && test3);
	}

	private boolean MergeRoutes(int s, int d)
	{
		Integer sourceIndex = assignedRoute.get(s);
		Integer destIndex = assignedRoute.get(d);
		
		Route source = routes.get(sourceIndex);
		Route dest = routes.get(destIndex);
		
		// Unisce due route già esistenti
		if(source != null && dest != null)
		{
			// Rischio cammino non hamiltoniano
			if(source.equals(dest)) return false;
					
			List<Customer> sourceCustomers = source.getCustomers();
			List<Customer> destCustomers = dest.getCustomers();
					
			for(Customer customer : destCustomers)
			{
				
				assignedRoute.replace(customer.getNumber(), sourceIndex);
				
				// Deep copy
				sourceCustomers.add(new Customer(customer));
			}
			
			Cost sourceCost = source.getCost();
			Cost destCost = dest.getCost();
					
			sourceCost.total += destCost.total;
			source.setCost(sourceCost);
			
			routes.remove(destIndex);
			
			numberOfUsedVehicles--;
			
			return Test(s,d);	
		}
					
		
		// Nuova route
		else if(source==null && dest==null)
		{
			Route r = new Route();
			r.setIndex(s);
			
			Cost c = new Cost();
			c.load = instance.getCapacity(s) + instance.getCapacity(d);
			r.setCost(c);
			
			Customer c1 = new Customer();
			c1.setNumber(s);
			
			Customer c2 = new Customer();
			c2.setNumber(d);
			
			r.addCustomer(c1);
			r.addCustomer(c2);
			
			assignedRoute.put(s, s);
			assignedRoute.put(d, s);
			
			routes.put(s, r);
			
			numberOfUsedVehicles++;
			
			return Test(s,d);
		}
		
		else if(source!=null && dest==null)
		{	
			Customer c = new Customer();
			c.setNumber(d);
			source.addCustomer(c);
			
			Cost cost = source.getCost();
			cost.load += instance.getCapacity(d);
			source.setCost(cost);
			
			assignedRoute.put(d, sourceIndex);
			
			return Test(s,d);
		}
		
		//else if(dest!=null && source==null)
		//{	
			Customer c = new Customer();
			c.setNumber(s);
			dest.addCustomer(c);
			
			Cost cost = dest.getCost();
			cost.load += instance.getCapacity(s);
			dest.setCost(cost);
			
			assignedRoute.put(s, destIndex);
			
			return Test(s,d);
		//}
				
	}
	
	private void PerformSequencialCW()
	{	
		
		ListIterator<Saving> it = savings.listIterator(0);
		
		while(it.hasNext() && numberOfUsedVehicles < numberOfVehicles)
		{
			Saving s = it.next();
			
			if(IsUsable(s) && CapacityConstraintIsMet(s.from, s.to) && HamiltonialityConstraintIsMet(s.from, s.to))
			{
				boolean success = MergeRoutes(s.from, s.to);
				
				if(success)
				{
					from.replace(s.from, false);
					to.replace(s.to, false);
				}
			}
		}
		

		
		for(int i=0; i<numberOfCustomers && numberOfUsedVehicles < numberOfVehicles; i++)
		{
			Integer n = assignedRoute.get(i);
			
			if(n==null)
			{
				Route r = new Route();
				Customer c = new Customer();
				Cost cost = new Cost();
				
				c.setNumber(i);
				r.addCustomer(c);
				
				cost.load = instance.getCapacity(i);
				r.setCost(cost);
				r.setIndex(i);
				
				routes.put(i, r);
				assignedRoute.put(i, i);
				
				numberOfUsedVehicles++;
			}
		}
	}
	
	private boolean CapacityConstraintIsMet(int x, int y)
	{
		double totalDemandRouteOfX = 0, totalDemandRouteOfY = 0;
		Integer iX, iY;
		iX = assignedRoute.get(x);
		iY = assignedRoute.get(y);
		
		if(assignedRoute.containsKey(x))
		{			
			Route u = routes.get(iX);
			if(u==null) System.out.println("rut nulla");
			try
			{
			Cost c = u.getCost();
			totalDemandRouteOfX = c.load;
			} catch(Exception e)
			{
				if(u == null) System.out.println("la route non c'è");
				if(iX == null) System.out.println("x non c'è");
				if(iY == null) System.out.println("y non c'è");
			}
			
		}
		else totalDemandRouteOfX = instance.getCapacity(x);
		
		if(assignedRoute.containsKey(y))
		{
			
			
				Route u = routes.get(iY);
				Cost c = u.getCost();
				totalDemandRouteOfY = c.load;
			/*}
			catch(Exception e)
			{
				System.out.println("y: " + y);
				System.out.println("iY: " + iY);
				System.out.println("totalDemandRouteOfY: " + totalDemandRouteOfY);
			}*/
		}
		else totalDemandRouteOfY = instance.getCapacity(y);
		
		return(totalDemandRouteOfX + totalDemandRouteOfY <= capacity);
	}
	
	private boolean HamiltonialityConstraintIsMet(int s, int d)
	{
		if(!assignedRoute.containsKey(s) || !assignedRoute.containsKey(d))
			return true;
		
		int source = assignedRoute.get(s);
		int dest = assignedRoute.get(d);
		
		Route r1 = routes.get(source);
		Route r2 = routes.get(dest);
		
		return(!r1.equals(r2));
		
	}

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
					route.getCost().travelTime += instance.getTravelTime(route.getDepotNr(), customerK.getNumber());
					totalTime += instance.getTravelTime(route.getDepotNr(), customerK.getNumber());
				}else{
					route.getCost().travelTime += instance.getTravelTime(route.getCustomerNr(k -1), customerK.getNumber());
					totalTime += instance.getTravelTime(route.getCustomerNr(k -1), customerK.getNumber());
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
			totalTime += instance.getTravelTime(route.getLastCustomerNr(), route.getDepotNr());
			route.getCost().travelTime += instance.getTravelTime(route.getLastCustomerNr(), route.getDepotNr());
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
	 * Imposta parametri per la route (depot, veicolo, costi)
	 * @param i
	 * @param route
	 * @param vehicle
	 * @param cost
	 */
	private void setupRoute(int i, Instance instance, Route route, Vehicle vehicle, Cost cost)
	{
		vehicle.setCapacity(instance.getCapacity(0, 0));
		vehicle.setDuration(instance.getDuration(0, 0));
		
		route.setAssignedVehicle(vehicle);
		route.setDepot(instance.getDepot(0));
		route.setReturnToDepotTime(instance.getDepot(0).getEndTw());
		route.setCapacity(vehicle.getCapacity());

		route.setCost(cost);
		route.setIndex(i+1);
		
		evaluateRoute(route);
	}
	
	private Chromosome ConvertRoutesToChromosome()
	{
		
		Chromosome chromosome = new Chromosome(numberOfGenes);
		
		Iterator<Route> it = routes.values().iterator();
		
		
		for(int i=0, j=0; it.hasNext(); i++)
		{
			Route route = it.next();
			
			setupRoute(i, instance, route, new Vehicle(), new Cost());
			
			List<Customer> cList = route.getCustomers();
			
			for(Customer c : cList)
			{
				chromosome.setGene(j, c.getNumber());
				j++;
			}
		}
		
		return chromosome;
		
	}
	
	public Chromosome GenerateChromosome()
	{	
		InitSavings();
		PerformSequencialCW();
		Chromosome result = ConvertRoutesToChromosome();
		
		return result;
		
	}

}
