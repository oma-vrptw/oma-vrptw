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
import com.mdvrp.Depot;
import com.mdvrp.Instance;
import com.TabuSearch.MySolution;

@SuppressWarnings("serial")
public class MyGASolution extends MySolution{
	Chromosome chromosome;
	
	public MyGASolution(Chromosome ch, Instance instance) {
		// TODO Auto-generated constructor stub
		super(instance);
		
		this.chromosome = ch;
	}
	
	/**
	 * create routes according to chromosome 
	 * note: private method, called from evaluateRoute
	 */
	private void buildRoutes() {
		int ng;
		ng = chromosome.getNumberOfGenes();
		
		//for each deposit
		for (int i = 0; i < instance.getDepotsNr(); ++i){
			int k = 0;	//index in chromosome array
			for(int j=0; j < ng; j++){
				//until there are genes in chromosome
				//at each iteration create a new route (new vehicle)
				for( ;!chromosome.isDelimiter(k) ;k++){
					//fill a route according to chromosome
					//get the customer pointed by chromosome[k]
					Customer cu = instance.getDepot(i).getAssignedCustomer(chromosome.getGene(k)-1);
					routes[i][j].addCustomer(cu);
				}
				//if(chromosome.isDelimiter(k)) k++;
			}
		}
	}

	private void evaluateChromosome() {
		buildRoutes();	//create route object from chromosome
		//for each deposit
		for (int i = 0; i < instance.getDepotsNr(); ++i){
			for(int j=0; j < routes[i].length; j++){
				evaluateRoute(routes[0][j]);	//calculate cost for a given route
			}
		}
		
		Cost c = getCost();	//object storing total cost of the routes
		
		//calculate total in order to give it available to getFitness method
		c.calculateTotal(alpha, beta, gamma);
		
		return;
	}

	/**
	 * calculate objective function for a given chromosome. 
	 * infeasible solutions are allowed but penalized according to
	 * instance alpha, beta, gamma parameters.
	 * @return fitness value = objective function cost
	 */
	public double getFitness() {
		//this function build and evaluate routes 
		evaluateChromosome();
		
		return cost.getTotal();
	}
	
}
