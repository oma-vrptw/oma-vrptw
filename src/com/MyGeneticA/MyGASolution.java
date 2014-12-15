package com.MyGeneticA;

/** 
 * MyGASolution class where it will generated a solution using com.mdvrp objects 
 * (e.g. route, cost, vehicle, depot). In particular, this makes possible a 
 * conversion between genetic and tabu way of represent a solution, that is 
 * respectively chromosome and a route array.
 *  
 * @author phil heartbreakid91@gmail.com
 */

import com.mdvrp.Cost;
import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;
import com.TabuSearch.MyObjectiveFunction;
import com.TabuSearch.MySolution;

@SuppressWarnings("serial")
public class MyGASolution extends MySolution{
	Chromosome chromosome;
	MyGAObjectiveFunction objectiveFunction;
	
	public MyGASolution(Chromosome ch, Instance instance) {
		// TODO Auto-generated constructor stub
		objectiveFunction = new MyGAObjectiveFunction(instance);
		MySolution.setInstance(instance);
		cost = new Cost();
		initializeRoutes(instance);
		alpha 	= 1;
    	beta 	= 1;
    	gamma	= 1;
    	delta	= 0.005;
    	upLimit = 10000000;
    	resetValue = 0.1;
    	feasibleIndex = 0;
    	MySolution.setIterationsDone(0);
    	Bs = new int[instance.getCustomersNr()][instance.getVehiclesNr()][instance.getDepotsNr()];	
    	
		this.chromosome = ch;
	}
	
	public MyGASolution(Instance instance, Route[][] routes)
	{
		objectiveFunction = new MyGAObjectiveFunction(instance);
		MySolution.setInstance(instance);
		cost = new Cost();
		
		alpha 	= 1;
    	beta 	= 1;
    	gamma	= 1;
    	delta	= 0.005;
    	upLimit = 10000000;
    	resetValue = 0.1;
    	feasibleIndex = 0;
    	MySolution.setIterationsDone(0);
    	Bs = new int[instance.getCustomersNr()][instance.getVehiclesNr()][instance.getDepotsNr()];
    	this.routes = routes;
    	this.objectiveFunction.evaluateAbsolutely(this);
	}
	
	/**
	 * create routes according to chromosome 
	 * note: private method, called from evaluateRoute
	 */
	private void buildRoutes() {
		int ng;

		int customerChosen;
		Customer customerChosenPtr;
		Route route;
		int k;
		
		ng = chromosome.getNumberOfGenes();

		//for each deposit
		for (int i = 0; i < instance.getDepotsNr(); ++i){
			int j=0;	//routes index
				//until there are genes in chromosome
				//at each iteration create a new route (new vehicle)
			route = routes[i][j]; //first route
			//fin quando ci sono geni e veicoli disponibili
			
			//inserisci il primo nella prima rotta
			k=0;
			customerChosen = chromosome.getGene(k);
			customerChosenPtr = instance.getCustomer(customerChosen);
			route.addCustomer(customerChosenPtr);
			evaluateRoute(route);
			
				for(k = 1; k < ng && j < instance.getVehiclesNr()-1; k++){
					//fill a route according to chromosome
					//get the customer pointed by chromosome[k]
					customerChosen = chromosome.getGene(k);
					customerChosenPtr = instance.getCustomer(customerChosen);
					if(customerChosenPtr.getCapacity() + route.getCost().load <= route.getLoadAdmited()	
						&& Math.max(route.getCost().getTotal(), customerChosenPtr.getStartTw()) + instance.getTravelTime(chromosome.getGene(k-1), chromosome.getGene(k))+customerChosenPtr.getServiceDuration() <= route.getDepot().getEndTw()
						&& Math.max(route.getCost().getTotal(), customerChosenPtr.getStartTw()) + instance.getTravelTime(chromosome.getGene(k-1), chromosome.getGene(k))+customerChosenPtr.getServiceDuration() - getInstance().getTravelTime(route.getLastCustomerNr(), route.getDepotNr()) <= customerChosenPtr.getEndTw()
						){
						route.addCustomer(customerChosenPtr);
						evaluateRoute(route);
					}else{
						j++;
						route = routes[i][j];
						route.addCustomer(customerChosenPtr);
						evaluateRoute(route);
					}
				}
				
				//inserisci tutto nella ultima rotta
				if(k<ng){
					for(; k < ng ; k++){
						customerChosen = chromosome.getGene(k);
						customerChosenPtr = instance.getCustomer(customerChosen);
						route.addCustomer(customerChosenPtr);
						evaluateRoute(route);
					}
					
				}
				chromosome.setRoutesNumber(j+1);
			}
		
		
		}

	/**
	 * calculate objective function for a given chromosome. 
	 * infeasible solutions are allowed but penalized according to
	 * instance alpha, beta, gamma parameters.
	 * @return fitness value = objective function cost
	 */
	//WARNING -> questa funzione è pericolosa ti sballa mezzo mondo, usare con cautela
	public double getFitness() {
		//this function build and evaluate routes 
		//initializeRoutes(instance);
		
		buildRoutes();
		
		return objectiveFunction.evaluateAbsolutely(this);
	}
	
	public Object clone()
    {   
        MyGASolution copy = (MyGASolution)super.clone();
       
        copy.chromosome = this.chromosome;
        
        return copy;
    }   // end clone

	public Chromosome getChromosome() {
		return chromosome;
	}
	
}
