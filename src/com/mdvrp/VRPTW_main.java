package com.mdvrp;

import java.io.FileWriter;
import java.io.PrintStream;

import org.coinor.opents.TabuList;

import com.TabuSearch.*;
import com.MyGeneticA.MyGA;

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
		Duration            duration 		= new Duration(); 		// used to calculate the elapsed time
		PrintStream         outPrintSream 	= null;					// used to redirect the output
		int count, iter;
		int bestRoutesNr;

		try {			
			// check to see if an input file was specified
			parameters.updateParameters(args);

			if(parameters.getInputFileName() == null){
				System.out.println("You must specify an input file name");
				return;
			}

			duration.start();

			// get the instance from the file			
			instance = new Instance(parameters); 
			instance.populateFromHombergFile(parameters.getInputFileName());
			objFunc 		= new MyObjectiveFunction(instance);
			moveManager 	= new MyMoveManager(instance);
			moveManager.setMovesType(parameters.getMovesType());

			// Tabu list
			int dimension[] = {instance.getDepotsNr(), instance.getVehiclesNr(), instance.getCustomersNr(), 1, 1};
			tabuList 		= new MyTabuList(parameters.getTabuTenure(), dimension);

			// Create Tabu Search object

			/*
			 * allocate space for all the customers 
			 * and routes delimiters (=vehicles number)
			 * Note: a chromosome is always terminated with a delimiter
			 */
			int chromosomeDim = instance.getCustomersNr();
			int populationDim = 100;
			int NBestSolution, countBestSolution;
			
			MySolution BestGASolutions[];
			
			// Init data for Genetic Algorithm
			myGA = new MyGA(chromosomeDim, 
					populationDim,
					instance, 
					50, 
					true,
					25);


			myGA.initPopulation();
			
			
			iter = 3;
			NBestSolution = 3;
			
			double bestSolutionFound = Double.MAX_VALUE;
			bestRoutesNr = 0;
			count = 0;
			while(count < iter){
				myGA.evolve2();

				//population.printPopulation();

				// Init memory for Tabu Search
				
				BestGASolutions = myGA.getNBestSolution(NBestSolution);
				
				countBestSolution = 0;
				while(countBestSolution < NBestSolution){

					initialSol 		= BestGASolutions[countBestSolution];
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
					duration.stop();
					/* Print results
					String outSol = String.format(
							"\nInstance file: %s\n"
									+ "Total cost: %5.2f\n"
									+ "Execution time: %d sec\n"
									+ "Number of routes: %4d\n",
									instance.getParameters().getInputFileName(), search.feasibleCost.total,
									duration.getSeconds(), routesNr);
					System.out.println(outSol);
*/
					
					if(bestSolutionFound > search.feasibleCost.total){
						bestSolutionFound = search.feasibleCost.total;
						bestRoutesNr = routesNr;
					}

					
					myGA.insertBestTabuSolutionIntoInitPopulation(search.feasibleRoutes);
					countBestSolution++;
				}

				count++;
			}
			
			System.out.println("best solution found:");
			 String outSol = String.format(
		        		"Instance file: %s\n"
		        		+ "Total cost: %5.2f\n"
		        		+ "Execution time: %d sec\n"
		        		+ "Number of routes: %4d\n",
		        		instance.getParameters().getInputFileName(), bestSolutionFound,
		            	duration.getSeconds(), bestRoutesNr);
		        System.out.println(outSol);
		        FileWriter fw = new FileWriter(parameters.getOutputFileName(),true);
		        fw.write(outSol);
		        fw.close();
			
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
