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
	
	double[] labelV;
	int[] labelP;
	
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
	
	void setAlphaBetaGamma(double alpha, double beta, double gamma){
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
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
	
	private void buildRoutes2() {
		Route route;
		int customerChosen;
		Customer customerChosenPtr;
		int ng = chromosome.getNumberOfGenes();
		
		labelling2();
		
		for (int i = 0; i < instance.getDepotsNr(); ++i){
			int j=0;
			route = routes[i][j];
			int k = 0;
			customerChosen = chromosome.getGene(k);
			customerChosenPtr = instance.getCustomer(customerChosen);
			route.addCustomer(customerChosenPtr);
			evaluateRoute(route);
			
			for(k = 1; k < ng; k++ ){
				if(labelP[k] != labelP[k-1] && j < instance.getVehiclesNr()-1){
					j++;
					route = routes[i][j];
				}
				customerChosen = chromosome.getGene(k);
				customerChosenPtr = instance.getCustomer(customerChosen);
				route.addCustomer(customerChosenPtr);
				evaluateRoute(route);
			}	
			
			if( k < ng ){
				System.out.println("hi there!");
				for( ; k < ng ; k++ ){
					customerChosen = chromosome.getGene(k);
					customerChosenPtr = instance.getCustomer(customerChosen);
					route.addCustomer(customerChosenPtr);
					evaluateRoute(route);
				}	
			}
			
			chromosome.setRoutesNumber(j+1);
		}
			
	}
		
	void printConvertedRoutes(){
		System.out.println("total cost from labelling: "+labelV[chromosome.getNumberOfGenes()-1]);
				
				System.out.print( chromosome.getGene(0) + ", ");
				for(int k = 1; k < chromosome.getNumberOfGenes(); k++){
					if(labelP[k] != labelP[k-1])
						System.out.println();
					System.out.print( chromosome.getGene(k) + ", ");
				}
				
	}
	
	public void labelling(){
		int ng = chromosome.getNumberOfGenes();
		double load, cost;
		int customerChosen;
		Customer customerChosenPtr;
		int depotNr = routes[0][1].getDepotNr();
		double L = routes[0][1].getDepot().getEndTw();
		double Q = routes[0][1].getLoadAdmited();
		double costViol, loadViol, TWViol, totalCost, totalViol;

		labelV = new double[ng]; 
		labelP = new int[ng]; 
		//double []labelViol = new double[ng]; 
		
		labelV[0] = 0;
		
		for( int i = 1; i < ng; i++ ) labelV[i] = Double.MAX_VALUE;

		for( int i = 1; i < ng; i++ ){	
			
			totalCost = cost = load = 0;
			totalViol = 0;
			costViol = loadViol = TWViol = 0;
			int j = i;
			do{
				customerChosen = chromosome.getGene(j);
				customerChosenPtr = instance.getCustomer(customerChosen);
				load += customerChosenPtr.getCapacity();
				
				//check for load violation
				if(load > Q){
					loadViol += load - Q;
				}
				
				if( i == j ){
					cost =  Math.max(instance.getTravelTime(depotNr, chromosome.getGene(j)), customerChosenPtr.getStartTw());
							//+ customerChosenPtr.getServiceDuration()
							//+ instance.getTravelTime(chromosome.getGene(j), depotNr);
				}else{
					cost = Math.max(
							cost 
						- instance.getTravelTime(chromosome.getGene(j-1), depotNr)
						+ instance.getTravelTime(chromosome.getGene(j-1), chromosome.getGene(j))
						,
						customerChosenPtr.getStartTw()
						);
				
					//cost += customerChosenPtr.getServiceDuration();
					//check TW violation
					if(cost > customerChosenPtr.getEndTw()){
						//relaxed EndTimeWindowConstraint
						TWViol += cost - customerChosenPtr.getEndTw();
						//System.out.println("hi there: "+cost);
					}
					
					
				}
				
				cost += customerChosenPtr.getServiceDuration();
				cost += instance.getTravelTime(chromosome.getGene(j), depotNr);
				
				//check for cost Violation
				if(cost > L){
					costViol += cost - L;
				}
				
				totalCost = cost + alpha * loadViol + beta * costViol + gamma * TWViol;
				totalViol = loadViol + costViol + TWViol;
				//totalCost = cost + beta * costViol;
					if( labelV[i-1] + totalCost < labelV[j]){// || (labelV[i-1] + totalCost >= labelV[j] && (totalCost - totalViol) < labelViol[j])){
						labelV[j] = labelV[i-1] + totalCost;
						//labelViol[j] = totalViol;
						labelP[j] = i-1;
					}
					
				j++;
			}while( j < ng );
		}

	}
		
		public void labelling2(){
			int ng = chromosome.getNumberOfGenes();
			double load, cost;
			int customerChosen;
			Customer customerChosenPtr;
			int depotNr = routes[0][1].getDepotNr();
			double L = routes[0][1].getDepot().getEndTw();
			double Q = routes[0][1].getLoadAdmited();
			
		//	double alpha, beta, gamma;
		//	double alpha * loadViol + beta * durationViol + gamma * twViol;
			labelV = new double[ng]; 
			labelP = new int[ng]; 
			
			labelV[0] = 0;
			
			for( int i = 1; i < ng; i++ ) labelV[i] = Double.MAX_VALUE;
			
			for( int i = 1; i < ng; i++ ){	
				
				cost = load = 0;
				int j = i;
				do{
					customerChosen = chromosome.getGene(j);
					customerChosenPtr = instance.getCustomer(customerChosen);
					load += customerChosenPtr.getCapacity();
					
					if( i == j ){
						cost =  Math.max(instance.getTravelTime(depotNr, chromosome.getGene(j)), customerChosenPtr.getStartTw());
					}else{
						cost = Math.max(
								cost 
							- instance.getTravelTime(chromosome.getGene(j-1), depotNr)
							+ instance.getTravelTime(chromosome.getGene(j-1), chromosome.getGene(j))
							,
							customerChosenPtr.getStartTw()
							);
					}
					
					if(cost > customerChosenPtr.getEndTw()){
						//relaxed EndTimeWindowConstraint
						cost = Double.MAX_VALUE;
						//System.out.println("hi there: "+cost);
					}else{
						cost += customerChosenPtr.getServiceDuration() + instance.getTravelTime(chromosome.getGene(j), depotNr);
					}
					
					if(cost <= L && load <= Q){
						if( labelV[i-1] + cost < labelV[j]){
							labelV[j] = labelV[i-1] + cost;
							labelP[j] = i-1;
						}
					}
					j++;
				}while( j < ng );
			}

		//System.out.println("total cost from labelling: "+labelV[ng-1]);
		/*
		System.out.print( chromosome.getGene(0) + ", ");
		for(int k = 1; k < ng; k++){
			if(labelP[k] != labelP[k-1])
				System.out.println();
			System.out.print( chromosome.getGene(k) + ", ");
		}
		*/
		
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
		
		buildRoutes2();
		
		double x = objectiveFunction.evaluateAbsolutely(this);
		//System.out.println("fitness = "+x);
		return x;
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
