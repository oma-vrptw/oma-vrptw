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
import com.mdvrp.Route;
import com.TabuSearch.MySolution;

@SuppressWarnings("serial")
public class MyGASolution extends MySolution{
	Chromosome chromosome;
	
	public MyGASolution(Chromosome ch, Instance instance) {
		// TODO Auto-generated constructor stub
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
	
	/**
	 * create routes according to chromosome 
	 * note: private method, called from evaluateRoute
	 */
	private void buildRoutes() {
		int ng;
		ng = chromosome.getNumberOfGenes();
		
		//for each deposit
		for (int i = 0; i < instance.getDepotsNr(); ++i){
			int j=0;	//routes index
				//until there are genes in chromosome
				//at each iteration create a new route (new vehicle)
				for(int k = 0; k < ng;k++){
					//fill a route according to chromosome
					//get the customer pointed by chromosome[k]
					if(chromosome.isDelimiter(k)){
						j++;	//end of a route
						continue;
					}
					
					Customer cu = instance.getCustomer(chromosome.getGene(k));

					routes[i][j].addCustomer(cu);
					
				}
			}
		}

	/**
	 * calculate objective function for a given chromosome. 
	 * infeasible solutions are allowed but penalized according to
	 * instance alpha, beta, gamma parameters.
	 * @return fitness value = objective function cost
	 */
	public double getFitness() {
		//this function build and evaluate routes 
		buildRoutes();
		MyGAObjectiveFunction objectiveFunction = new MyGAObjectiveFunction(instance);
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
