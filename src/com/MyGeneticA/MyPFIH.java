package com.MyGeneticA;

import java.util.*;

import com.TabuSearch.MySolution;
import com.mdvrp.*;

/**
 * Classe che realizza il Push-Forward Insertion Heuristic (PFIH).
 * 
 * @author Alessio
 *
 */
public class MyPFIH 
{
	private class CostAwareCustomer extends Customer implements Comparable<CostAwareCustomer>
	{
		private double cost, minInsertionCost;
		private int minCostPosition;
		boolean routed;
		
		public CostAwareCustomer(Customer c)
		{
			super(c);
			
			cost = 0;	
			minInsertionCost = Double.MAX_VALUE;
			minCostPosition = -1;
			routed = false;
		}
		
		
		public boolean isRouted() {
			return routed;
		}


		public void setRouted(boolean routed) {
			this.routed = routed;
		}

		public void setCost(double cost)
		{
			this.cost = cost;
		}
		
		public void ComputeInsertionMinCost(Route currentRoute)
		{
			int actualCustomersLength = currentRoute.getCustomersLength();
			List<Double> costs = new ArrayList<>();
			
			for(int i=0; i<actualCustomersLength; i++)
			{
				currentRoute.addCustomer(this, i);
				evaluateRoute(currentRoute);
				
				costs.add(new Double(currentRoute.getCost().total));
				
				currentRoute.removeCustomer(i);
				evaluateRoute(currentRoute);
			}
			
			minInsertionCost = Collections.min(costs);
			minCostPosition = costs.indexOf(minInsertionCost);
		}
		
		@Override
		public int compareTo(CostAwareCustomer o) 
		{
			if(this.cost > o.cost) return 1;
			else return -1;
		}
	}
	
	private Instance instance;
	private int numberOfCustomers, numberOfVehicles, numberOfGenes;
	private double alpha, beta, gamma, capacityOfVehicle;
	
	private List<CostAwareCustomer> customers;
	
	private Route[][] routes;

	public Route[][] getRoutes() 
	{
		return routes;
	}

	public MyPFIH(int numberOfGenes, Instance instance)
	{
		alpha = 0.7;
		beta = 0.1;
		gamma = 0.2;
		
		this.numberOfGenes = numberOfGenes;
		this.instance = instance;
		numberOfCustomers = instance.getCustomersNr();
		numberOfVehicles = instance.getVehiclesNr();
		capacityOfVehicle = instance.getCapacity(0, 0);

		routes = new Route[instance.getDepotsNr()][numberOfVehicles];
		
		customers = new ArrayList<>();
		
		PerformPFIH();
	}
	
	private void PerformPFIH()
	{
		int i;
		Route route;
		
		InitCustomers();
		
		for(i=0; !customers.isEmpty() && i<numberOfVehicles; i++)
		{
			route = new Route();
			
			setupRoute(i, instance, route, new Vehicle(), new Cost());
			
			InsertFirstCustomer(route);
			InsertOtherCustomers(route);
			
			routes[0][i] = route;
		}
		
		// Uscita dal ciclo precedente solo per (i<numberOfVehicles)
		
		if(!customers.isEmpty())
		{
			route = routes[0][i-1];
			
			int k = route.getCustomersLength();
			
			for(CostAwareCustomer c : customers)
			{
				route.addCustomer(c, k);
				k++;
			}
			
			evaluateRoute(route);
			
			customers.clear();//per completezza svuoto la collezione
		}
		
		// Gestione uscita dal ciclo solo per !customers.isEmpty()
		for(int j=i;  j<numberOfVehicles; j++)
		{
			route = new Route();
	
			setupRoute(j, instance, route, new Vehicle(), new Cost());
			
			routes[0][j] = route;
		}	
		
		for(i=1; i<instance.getDepotsNr(); i++)
		{
			for(int j=0; j<numberOfVehicles; j++)
			{
				route = new Route();
	
				setupRoute(i*numberOfVehicles + j, instance, route, new Vehicle(), new Cost());

				routes[i][j] = route;
			}
		}
	}
	
	private void InitCustomers()
	{
		double[][] distances = instance.getDistances();
		
		double cost = 0, di0, pi, li;
		CostAwareCustomer c = null;
		
		for(int i=0; i<numberOfCustomers; i++)
		{
			c = new CostAwareCustomer(instance.getCustomer(i));
			
			di0 = distances[i][numberOfCustomers];
			pi = c.getAngleToDepot(0);
			li = c.getEndTw();
			
			//System.out.println("Polar(" + i + ")=" + pi);
			
			cost = -alpha*di0 + beta*li + gamma*pi*di0;
			
			c.setCost(cost);
			c.setNumber(i);
			customers.add(c);
		}
		
		Collections.sort(customers);
		/*
		System.out.println("COSTI");
		
		for(CostAwareCustomer cu : customers)
			System.out.println(cu.cost);
			*/
	}

	private void InsertFirstCustomer(Route currentRoute)
	{
		CostAwareCustomer cStar = null;
		boolean FirstCustomerInserted = false;
		
		Iterator<CostAwareCustomer> it = customers.iterator();
		
		while(!FirstCustomerInserted && it.hasNext())
		{
			cStar = it.next();
			
			currentRoute.addCustomer(cStar);
			evaluateRoute(currentRoute);
			
			if(!currentRoute.getCost().checkFeasible())
			{
				currentRoute.getCustomers().remove(cStar);
				evaluateRoute(currentRoute);
			}
			else
			{
				FirstCustomerInserted = true;
				cStar.setRouted(true);
				cStar.minInsertionCost = Double.MAX_VALUE;
			}
		}
		
		// Se il ciclo è terminato per it.hasNext()
		if(!FirstCustomerInserted)
		{
			int index, i=0;
			CostAwareCustomer randCustomer;
			do
			{
				index = instance.getRandom().nextInt(customers.size());
				randCustomer = customers.get(index);
				i++;
			}
			while(randCustomer.isRouted() && i<customers.size());
			
			currentRoute.addCustomer(randCustomer);
			randCustomer.setRouted(true);
			randCustomer.minInsertionCost = Double.MAX_VALUE;
			
			evaluateRoute(currentRoute);
			
			customers.remove(randCustomer);
		}
		
		// Se il ciclo è terminato per !FirstCustomerInserted
		if(FirstCustomerInserted && cStar!=null) customers.remove(cStar);
			

	}
	
	private void InsertOtherCustomers(Route currentRoute)
	{

		List<CostAwareCustomer> customersToBeRemoved = new ArrayList<>();
		
		for(int i=0; i<customers.size(); i++)
		{
			UpdateInsertionMinCostsForRoute(currentRoute);
			
			CostAwareCustomer customer = MinInsertionCostCustomer();
			
			currentRoute.addCustomer(customer, customer.minCostPosition);
			evaluateRoute(currentRoute);
			
			if(!currentRoute.getCost().checkFeasible())
			{
				currentRoute.removeCustomer(customer.minCostPosition);
				evaluateRoute(currentRoute);
				break;
			}
			else
			{
				customer.setRouted(true);
				customer.minInsertionCost = Double.MAX_VALUE;
				
				customersToBeRemoved.add(customer);
			}
		}
		
		for(CostAwareCustomer c : customersToBeRemoved)
		{
			customers.remove(c);
		}
		
		customersToBeRemoved.clear();
	}
	
	// >-------------------- METODI AUSILIARI --------------------------<
	private CostAwareCustomer MinInsertionCostCustomer()
	{
		CostAwareCustomer mincustomer = null;
		double mincost = Double.MAX_VALUE;
		
		for(CostAwareCustomer c : customers)
		{
			if(!c.isRouted() && c.minInsertionCost < mincost)
			{
				mincost = c.minInsertionCost;
				mincustomer = c;
			}
		}
		
		return mincustomer;
	}
	
	private void UpdateInsertionMinCostsForRoute(Route currentRoute)
	{
		for(CostAwareCustomer c : customers)
			if(!c.isRouted())
				c.ComputeInsertionMinCost(currentRoute);
	}
	
    /**
	 * this function calculates the cost of a route from scratch
	 * @param route
	 */
	protected void evaluateRoute(Route route) 
	{	
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
				waitingTime = Math.max(0, customerK.getStartTw() - totalTime); //ritorna zero se il customer è pronto a ricevere il pacco altrimenti ritorna il tempo di attesa quindi potrei andare da un altro customer
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
			
			// update total violation
			route.getCost().calculateTotalCostViol();
			
		} // end if route not empty
		
    } // end method evaluate route

	/*
	private Chromosome ConvertRoutesToChromosome()
	{
		Chromosome chromosome = new Chromosome(numberOfGenes);
		
		Iterator<Route> it = routes.iterator();
		
		for(int i=0; it.hasNext();)
		{
			Route r = it.next();
			List<Customer> cList = r.getCustomers();
			
			for(Customer c : cList)
			{
				chromosome.setGene(i, c.getNumber());
				i++;
			}

		}

		return chromosome;		
	}*/
	
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

	
}
