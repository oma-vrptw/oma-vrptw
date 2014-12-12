package com.mdvrp;

import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;

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
					false,
					10);

			myGA.initPopulation();
			
			double bestSolutionFound = Double.MAX_VALUE;
			bestRoutesNr = 0;
			
			for(int k=0; k <= 2; k++){
				if(k==0){
					initialSol 		= new MySolution(instance);
				}else{
					initialSol 		= myGA.getInitialSolutions(k);
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
					
				}

				myGA.insertBestTabuSolutionIntoInitPopulation(search.feasibleRoutes);
			}
			
			iter = 3;
			NBestSolution = 3;
			count = 0;
			Boolean doMutation;
		
			doMutation = false;
			
			while(count < iter){
				
				myGA.evolve2(doMutation);
				//myGA.evolve();
				//population.printPopulation();

				BestGASolutions = myGA.getNDifferentBestSolutions(NBestSolution);
				NBestSolution = BestGASolutions.size();
				
				countBestSolution = 0;
				while(countBestSolution < NBestSolution){					
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
					}

					myGA.insertBestTabuSolutionIntoInitPopulation(search.feasibleRoutes);
					countBestSolution++;
				}

				if(myGA.isComputeStatistics())
					doMutation = true;
				count++;
			}
			
			System.out.println("best solution found:");
			 String outSol = String.format(
		        		"Instance file: %s\n"
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
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
