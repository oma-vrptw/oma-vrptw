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
		if(!test1) System.out.println("ERRORE IN " + s + "-" + d + ": sono ancora assegnati a route diverse.");
		
		Route source = routes.get(sourceIndex);
		Route dest = routes.get(destIndex);
		
		test2 = source != null && dest != null;
		if(!test2) System.out.println("ERRORE IN " + s + "-" + d + ": una delle route è inesistente.");
		
		try
		{
			test3 = source.equals(dest);
		}
		catch(Exception e)
		{
			System.out.println("ERRORE IN " + s + "-" + d + ": la source route è null.");
		}
		
		if(!test3) System.out.println("ERRORE IN " + s + "-" + d + ": source e dest hanno route diverse.");
		
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
	
	private Chromosome ConvertRoutesArrayToChromosome()
	{
		Chromosome chromosome = new Chromosome(numberOfGenes);
		
		for(int i=0; i<chromosome.getNumberOfGenes(); i++)
			chromosome.setGene(i, -1);
		
		Iterator<Route> it = routes.values().iterator();
		
		for(int i=0; it.hasNext();)
		{
			Route r = it.next();
			List<Customer> cList = r.getCustomers();
			
			for(Customer c : cList)
			{
				chromosome.setGene(i, c.getNumber());
				i++;
			}

			chromosome.setGene(i,-1);
			i++;
		}
		
		System.out.println("CROMOSOMA");
		
		for(int i=0; i<chromosome.getNumberOfGenes(); i++)
			System.out.print(chromosome.getGene(i) + " ");
		
		int gene = 0, count = 0;
		int[] array = new int[numberOfCustomers];
		Arrays.fill(array, -1);
		
		for(int i=0; i<chromosome.getNumberOfGenes(); i++)
		{
			gene = chromosome.getGene(i);
			if(gene!=-1)
			{
				array[count] = gene;
				count++;
			}
		}
		Arrays.sort(array);
		
		int n=0;
		System.out.println("\n");
		for(int i=0; i<array.length; i++)
		{
			//System.out.print(array[i] + " ");
			if(array[i]>-1) n++;
		}
		System.out.println();
		System.out.println("Ho usato " + n + " customer su " + numberOfCustomers);
		System.out.println("Ho usato " + numberOfUsedVehicles + " veicoli su " + numberOfVehicles);
		
		return chromosome;
		
	}
	
	public Chromosome GenerateChromosome()
	{	
		InitSavings();
		PerformSequencialCW();
		Chromosome result = ConvertRoutesArrayToChromosome();
		
		return result;
		
	}

}
