package com.mdvrp;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Properties;

import org.coinor.opents.TabuList;

import com.TabuSearch.*;
import com.MyGeneticA.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

public class VRPTW_main {

	public static void main(String[] args) {

		MyGA				myGA;
		MySearchProgram     search;
		MySolution          initialSol;
		MyObjectiveFunction objFunc;
		MyMoveManager       moveManager;
		TabuList            tabuList;
		Parameters          parameters 		= new Parameters(); 	// holds all the parameters passed from the input line
		Instance            instance; 								// holds all the problem data extracted from the input file
		PrintStream         outPrintSream 	= null;					// used to redirect the output
		int count, iter;
		int bestRoutesNr;

		Instant previous, current;
		long gap = 0, totalTime = 0;

		previous = Instant.now();
		
		try {			
			// check to see if an input file was specified
			parameters.updateParameters(args);

			if(parameters.getInputFileName() == null){
				System.out.println("You must specify an input file name");
				return;
			}

			/*load configuration file*/
			Properties prop = new Properties();
			String propFileName = "./config.properties";
			
			InputStream inputStream = VRPTW_main.class.getClassLoader().getResourceAsStream(propFileName);

			if(inputStream != null){
				prop.load(inputStream);
			}else{
				throw new FileNotFoundException("property file '"+propFileName+"' not found on the classpath");
			}
			
			parameters.setIterations(Integer.parseInt(prop.getProperty("tsIterationN")));
			
			// get the instance from the file			
			instance = new Instance(parameters); 
			instance.populateFromHombergFile(parameters.getInputFileName());
			objFunc 		= new MyObjectiveFunction(instance);
			moveManager 	= new MyMoveManager(instance);
			moveManager.setMovesType(parameters.getMovesType());

			// Tabu list
			int dimension[] = {instance.getDepotsNr(), instance.getVehiclesNr(), instance.getCustomersNr(), 1, 1};
			tabuList 		= new MyTabuList(parameters.getTabuTenure(), dimension);
			
			/*
			 * allocate space for all the customers 
			 * and routes delimiters (=vehicles number)
			 * Note: a chromosome is always terminated with a delimiter
			 */
			int chromosomeDim = instance.getCustomersNr();
			int populationDim = (int) (instance.getCustomersNr()*Double.parseDouble(prop.getProperty("populationDim")));
			int NBestSolution, countBestSolution;
			ArrayList<MySolution> BestGASolutions;
			
			System.out.println("populationDim: "+populationDim+" TS It: "+parameters.getIterations());
			
			// Init data for Genetic Algorithm
			myGA = new MyGA(chromosomeDim, 
					populationDim,
					instance, 
					Integer.parseInt(prop.getProperty("gaIterationN")), 
					Boolean.parseBoolean(prop.getProperty("enableMutation")),
					Integer.parseInt(prop.getProperty("threshold")),
					prop);


			System.out.println("Hi there, we are going to start the job! Are u ready?");
			System.out.println("Init population");
			myGA.initPopulation();
			System.out.println("done.");
			
			
			Population pop =  myGA.getPopulation();
			Chromosome c;
			
			for(int i = 0; i < populationDim/4; i++){
				c = pop.getChromosome(i);
				initialSol 		= c.getSolution();
				
				// Start solving  
				search 			= new MySearchProgram(instance, initialSol, moveManager,
						objFunc, tabuList, false,  outPrintSream);

				search.tabuSearch.setIterationsToGo(100);	// Set number of iterations
				search.tabuSearch.startSolving();
				myGA.insertBestTabuSolutionIntoInitPopulation(search.bestRoutes);
			}
			
			double bestSolutionFound = Double.MAX_VALUE;
			bestRoutesNr = 0;
		
			for(int k=0; k <= 2; k++){
				System.out.println("start TABU with initial solution number "+(k+1));
				if(k==0){
					initialSol 		= new MySolution(instance);
					//System.out.println("----TESISTA----");
					//System.out.println(initialSol);
				}else if(k==2){
					initialSol 		= new MyGASolution(instance, myGA.getPfihSol().getRoutes());
					//initialSol 		= myGA.getInitialSolutions(k);
					//System.out.println("----PFIH----");
					//System.out.println(initialSol);
					
				}else{
					initialSol 		= myGA.getInitialSolutions(k);
//					System.out.println("----CW----");
//					System.out.println(initialSol);

				}
				// Start solving  
				search 			= new MySearchProgram(instance, initialSol, moveManager,
						objFunc, tabuList, false,  outPrintSream);

				search.tabuSearch.setIterationsToGo(parameters.getIterations());	// Set number of iterations
				
				current = Instant.now();
				
				search.tabuSearch.startSolving();	        
				int numberOfCustomers = 0;
				int routesNr = 0;
				for(int i =0; i < search.feasibleRoutes.length; ++i)
					for(int j=0; j < search.feasibleRoutes[i].length; ++j)
						if(search.feasibleRoutes[i][j].getCustomersLength() > 0){
							numberOfCustomers += search.feasibleRoutes[i][j].getCustomersLength();
							routesNr++;	
						}
	
				System.out.println("number of customers: "+numberOfCustomers);
				
				if(bestSolutionFound > search.feasibleCost.total){
					bestSolutionFound = search.feasibleCost.total;
					
					bestRoutesNr = routesNr;
					
					if (previous != null) {
					    gap = ChronoUnit.SECONDS.between(previous,current);
					}
					System.out.println("current solution changed:");
					 String outSol = String.format(
				        		"Instance file: %s\n"
				        		+ "Total cost: %5.2f\n"
				        		+ "Execution time: %d sec\n"
				        		+ "Number of routes: %4d\n",
				        		instance.getParameters().getInputFileName(), bestSolutionFound,
				        		gap, bestRoutesNr);
				        System.out.println(outSol);
				}
				System.out.println("solution from TABU = "+Math.round(search.feasibleCost.total));
				System.out.println("end of TABU.");
				
				System.out.println("insert this solution into population");
				myGA.insertBestTabuSolutionIntoInitPopulation(search.feasibleRoutes);
				System.out.println("done.");
			}
		
			
			iter = Integer.parseInt(prop.getProperty("totalIteration"));
			NBestSolution = Integer.parseInt(prop.getProperty("nBestSolution"));
			count = 1;
			Boolean doMutation;
		
			doMutation = false;
			
			System.out.println("starting to evolve the population. We hope to reach the optimum if we haven't already find it.");
			while(count <= iter){
				System.out.println("iteration "+count);
				myGA.evolve2(doMutation);
				//myGA.evolve();
				//population.printPopulation();
				System.out.println("select best chromosomes from population: "+NBestSolution);
				BestGASolutions = myGA.getNDifferentBestSolutions(NBestSolution);
				
					
				int NBestSolutionSelected = BestGASolutions.size();
				
				
				countBestSolution = 0;
				while(countBestSolution < NBestSolutionSelected){	
					System.out.println("start TABU with solution "+(countBestSolution+1)+ " as input");
					initialSol 		= BestGASolutions.get(countBestSolution);
					
					// Start solving  
					search 			= new MySearchProgram(instance, initialSol, moveManager,
							objFunc, tabuList, false,  outPrintSream);

					search.tabuSearch.setIterationsToGo(parameters.getIterations());	// Set number of iterations
					search.tabuSearch.startSolving();	        

					int routesNr = 0;
					for(int i =0; i < search.feasibleRoutes.length; ++i)
						for(int j=0; j < search.feasibleRoutes[i].length; ++j)
							if(search.feasibleRoutes[i][j].getCustomersLength() > 0)
								routesNr++;
					
					if(bestSolutionFound > search.feasibleCost.total){
						bestSolutionFound = search.feasibleCost.total;
						bestRoutesNr = routesNr;
						current = Instant.now();
						if (previous != null) {
						    gap = ChronoUnit.SECONDS.between(previous,current);
						}
						
						System.out.println("current solution changed:");
						 String outSol = String.format(
					        		"Instance file: %s\n"
					        		+ "Total cost: %5.2f\n"
					        		+ "Execution time: %d sec\n"
					        		+ "Number of routes: %4d\n",
					        		instance.getParameters().getInputFileName(), bestSolutionFound,
					        		gap, bestRoutesNr);
					        System.out.println(outSol);
					}

					System.out.println("solution from TABU = "+Math.round(search.feasibleCost.total));
					System.out.println("done.");
					
					System.out.println("insert this solution into population");
					myGA.insertBestTabuSolutionIntoInitPopulation(search.feasibleRoutes);
					System.out.println("done.");
					
					countBestSolution++;
				}

				if(myGA.isComputeStatistics())
					doMutation = true;
				
				count++;
				System.out.println("end of iteration "+count);
			}
			
			System.out.println("the game is over.");

			 String outSol = String.format(
					 	"\nThis one the best solution found\n"
		        		+ "Instance file: %s\n"
		        		+ "Total cost: %5.2f\n"
		        		+ "Execution time: %d sec\n"
		        		+ "Number of routes: %4d\n",
		        		instance.getParameters().getInputFileName(), bestSolutionFound,
		        		gap, bestRoutesNr);
		        System.out.println(outSol);
		        
		        FileWriter fw = new FileWriter(parameters.getOutputFileName(),true);
		        fw.write(outSol);
		        fw.close();
			
		        current = Instant.now();
		        if (previous != null) {
				    totalTime = ChronoUnit.SECONDS.between(previous,current);
				    System.out.println("time to compute all of the stuff= "+totalTime+" secondssssss");
				}
		        System.out.println("see u soon.");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
