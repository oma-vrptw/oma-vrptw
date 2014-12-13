package com.mdvrp;

import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;

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
			int populationDim = instance.getCustomersNr();
			int NBestSolution, countBestSolution;
			ArrayList<MySolution> BestGASolutions;
			
			// Init data for Genetic Algorithm
			myGA = new MyGA(chromosomeDim, 
					populationDim,
					instance, 
					3, 
					true,
					10);

			System.out.println("Hi there, we are going to start the job! Are u ready?");
			System.out.println("Init population");
			myGA.initPopulation();
			System.out.println("done.");
			
			double bestSolutionFound = Double.MAX_VALUE;
			bestRoutesNr = 0;
			
			for(int k=0; k <= 2; k++){
				System.out.println("start TABU with initial solution number "+(k+1));
				
				switch(k)
				{
					case 0:
						// Tesista
						initialSol = new MySolution(instance);
						break;
					
					case 1:
						// CW
						initialSol = new MyGASolution(instance, myGA.getCwSol().getRoutes());
						break;
					case 2:
						// PFIH
						initialSol = new MyGASolution(instance, myGA.getPfihSol().getRoutes());
						break;
						
					default:
						// Impossibile che si verifichi ma chi se ne frega
						initialSol = new MySolution(instance);
				}
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
				System.out.println("end of TABU.");
				
				System.out.println("insert this solution into population");
				myGA.insertBestTabuSolutionIntoInitPopulation(search.feasibleRoutes);
				System.out.println("done.");
			}
			
			iter = 3;
			NBestSolution = 3;
			count = 0;
			Boolean doMutation;
		
			doMutation = false;
			
			System.out.println("starting to evolve the population. We hope to reach the optimum if we haven't already find it.");
			while(count < iter){
				System.out.println("iteration "+(count+1));
				myGA.evolve2(doMutation);
				//myGA.evolve();
				//population.printPopulation();
				System.out.println("select best chromosomes from population");
				BestGASolutions = myGA.getNDifferentBestSolutions(NBestSolution);
				NBestSolution = BestGASolutions.size();
				
				countBestSolution = 0;
				while(countBestSolution < NBestSolution){	
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
