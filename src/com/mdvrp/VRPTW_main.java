package com.mdvrp;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.PrintStream;

import org.coinor.opents.MoveManager;
import org.coinor.opents.ObjectiveFunction;
import org.coinor.opents.TabuList;

import com.TabuSearch.*;
import com.MyGeneticA.*;

import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class VRPTW_main {

	static Instant previous;
	static double timeLimit = 0;	
	
	public static int countClones(int populationDim, Population p)
	{
		int clones = 0;
		Set<Double> set = new HashSet<>();
		
		for(int i=0; i<populationDim; i++)
		{	
			set.add(p.getChromosome(i).getFitness());
		}	
		clones = populationDim-set.size();
		
		set.clear();
		
		return clones;
	}
	
	public static boolean TimeExpired(){
		if(previous == null || timeLimit == 0) System.out.println("azz, da gestire con un eccezione");
		
		return UpdateGap(previous) > timeLimit;
	}
	
	private static double UpdateGap(Instant previous)
	{
		Instant current = Instant.now();
		
		return ChronoUnit.SECONDS.between(previous, current)/*+(double)ChronoUnit.MILLIS.between(previous, current)%1000/1000*/;
	}
	
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
		
		double gap = 0, totalTime = 0;
		double epsilon = 0.001;
		double norm; //norm = fattore di normalizzazione
	
		previous = Instant.now();
		
		try 
		{			
			// check to see if an input file was specified
			parameters.updateParameters(args);
			
			if(parameters.getInputFileName() == null)
			{
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
			
			parameters.setIterations(Integer.parseInt(prop.getProperty("tsIterationNr")));
			parameters.setTabuTenure(Integer.parseInt(prop.getProperty("tabuTenure")));
			
			//set time limit to 5 minutes normalized in respect of Perboli's performance
			norm = Double.parseDouble(prop.getProperty("fattore_di_normalizzazione"));
			timeLimit = ((Double.parseDouble(prop.getProperty("timeLimit")) * norm)-5);
			
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
			int populationDim = Integer.parseInt(prop.getProperty("populationDim"));
			int NBestSolution, countBestSolution;
			ArrayList<MySolution> BestGASolutions;
			
			System.out.println("populationDim: "+populationDim+" TS It: "+parameters.getIterations());
			
			// Init data for Genetic Algorithm
			myGA = new MyGA(chromosomeDim, 
					populationDim,
					instance, 
					parameters,
					Integer.parseInt(prop.getProperty("gaIterationN")), 
					prop);

			System.out.println("Hi there, we are going to start the job! Are u ready?");
			System.out.println("It's "+Date.from(previous));
			
			System.out.println("Init population");
			myGA.initPopulation();
			System.out.println("done.");

			double bestSolutionFound = Double.MAX_VALUE;
			bestRoutesNr = 0;
			
			//sort once only, not for each TABU solution inserting in initPopulation
			myGA.getPopulation().sort();
			
			for(int k=0, index = populationDim-1; k <= 2; k++, index-=2){
				
				
				System.out.println("start TABU with initial solution number "+(k+1));
				if(k==0){
					initialSol 		= new MySolution(instance);
				}else if(k==2){
					initialSol 		= new MyGASolution(instance, myGA.getPfihSol().getRoutes());					
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
						if(search.feasibleRoutes[i][j].getCustomersLength() > 0){
							routesNr++;	
						}
				
				double diff = Math.abs(bestSolutionFound - search.feasibleCost.total);
				
				if(bestSolutionFound > search.feasibleCost.total && diff>epsilon){
					
	
					
					bestSolutionFound = search.feasibleCost.total;	
					bestRoutesNr = routesNr;
					
					if (previous != null) {
						
						gap = 	UpdateGap(previous);
					}
					
					System.out.println("current solution changed:");
					 String outSol = String.format(
				        		"Instance file: %s\n"
				        		+ "Total cost: %5.2f\n"
				        		+ "Execution time: %5.3f sec\n"
				        		+ "Number of routes: %4d\n",
				        		instance.getParameters().getInputFileName(), bestSolutionFound,
				        		gap, bestRoutesNr);
				        System.out.println(outSol);
				}
				System.out.println("solution from TABU = "+ search.feasibleCost.total);
				System.out.println("end of TABU.");
				
				//System.out.println("alpha, beta, gamma: "+search.getSol().alpha+", "+search.getSol().beta+", "+search.getSol().gamma);
				if(search.feasibleCost.total != Double.POSITIVE_INFINITY)
					myGA.insertBestTabuSolutionIntoInitPopulation(search.feasibleRoutes, index, search.getSol().alpha, search.getSol().beta, search.getSol().gamma);
				else
					myGA.insertBestTabuSolutionIntoInitPopulation(search.bestRoutes, index, search.getSol().alpha, search.getSol().beta, search.getSol().gamma);
							
				System.out.println("done.");
			}
						
			//a fast optimization to worst population chromosomes -> only 20 iterations
			tabuPensaciTu(myGA, populationDim, instance, moveManager, objFunc, tabuList, false, outPrintSream, 50, prop);			
			
			NBestSolution = Integer.parseInt(prop.getProperty("nBestSolution"));
			count = 0;
			iter = Integer.parseInt(prop.getProperty("totalIteration"));
			
			System.out.println("starting to evolve the population. We hope to reach the optimum if we haven't already find it.");
			
			while(count<iter && !TimeExpired()){
				System.out.println("iteration "+(count+1));
				
				myGA.evolve2(moveManager, objFunc, tabuList, outPrintSream, 20, prop, count);

				System.out.println("select best chromosomes from population");
				
				BestGASolutions = myGA.getNDifferentBestSolutions(NBestSolution);
				NBestSolution = BestGASolutions.size();
				
				countBestSolution = 0;
				while(countBestSolution < BestGASolutions.size() && !TimeExpired()){	
					System.out.println("start TABU with solution "+(countBestSolution+1)+ " as input at "+UpdateGap(previous));
					initialSol 		= BestGASolutions.get(countBestSolution);
					
					// Start solving  
					search 			= new MySearchProgram(instance, initialSol, moveManager,
							objFunc, tabuList, false,  outPrintSream, prop);

					search.tabuSearch.setIterationsToGo(parameters.getIterations());	// Set number of iterations
					search.tabuSearch.startSolving();	        
					System.out.println("solution from TABU = "+ search.feasibleCost.total + " at "+UpdateGap(previous));
					int routesNr = 0;
					for(int i =0; i < search.feasibleRoutes.length; ++i)
						for(int j=0; j < search.feasibleRoutes[i].length; ++j)
							if(search.feasibleRoutes[i][j].getCustomersLength() > 0)
								routesNr++;
					
					double diff = Math.abs(bestSolutionFound - search.feasibleCost.total);
					
					if(bestSolutionFound > search.feasibleCost.total && diff>epsilon)
					{
						//incremento num iterazioni se miglioro, baloon-like ;)
						iter++;
						bestSolutionFound = search.feasibleCost.total;
						bestRoutesNr = routesNr;
						
						if (previous != null) {
						    gap = 	UpdateGap(previous);
						}
						
						System.out.println("current solution changed:");						 
					     System.out.println("Instance file: " + instance.getParameters().getInputFileName()
					        				 + "\nTotal cost: " + bestSolutionFound 
					        				 + "\nExecution time: "+ gap +" sec"
					        				 + "\nNumber of routes: " + bestRoutesNr);

					}

					
					System.out.println("done.");
					
					System.out.println("insert this solution into population");
					
					if (search.feasibleCost.total != Double.POSITIVE_INFINITY)
						myGA.insertBestTabuSolutionIntoInitPopulation2(search.feasibleRoutes, search.feasibleCost.total, search.getSol().alpha, search.getSol().beta, search.getSol().gamma );
					else
						myGA.insertBestTabuSolutionIntoInitPopulation2(search.bestRoutes, search.bestCost.total, search.getSol().alpha, search.getSol().beta, search.getSol().gamma );

					System.out.println("done.");
					
					countBestSolution++;
				}
				
				count++;
				System.out.println("end of iteration "+count+" at " + UpdateGap(previous));
			}
			
			System.out.println("the game is over.");
			

	        if (previous != null) {
			    totalTime = UpdateGap(previous);
			    //System.out.println("time to compute all of the stuff= "+totalTime+" secondssssss");
			}
	        
	        String outSol = String.format("%s; %5.2f; %5.3f; %5.3f; %4d\r\n" ,
	        		instance.getParameters().getInputFileName(), bestSolutionFound,
	        		gap, totalTime, bestRoutesNr);
	        /*
			 String outSol = String.format(
					 	"\nThis one the best solution found\n"
		        		+ "Instance file: %s\n"
		        		+ "Total cost: %5.2f\n"
		        		+ "Time to compute best solution: %5.3f sec\n"
		        		+ "Total time: %5.3f sec\n"
		        		+ "Number of routes: %4d\n",
		        		instance.getParameters().getInputFileName(), bestSolutionFound,
		        		gap, totalTime, bestRoutesNr);
		        		*/
		        System.out.println(outSol);
		        
		        FileWriter fw = new FileWriter(parameters.getOutputFileName(),true);
		        fw.write(outSol);
		        fw.close();
			
		        System.out.println("Finish at "+Date.from(Instant.now()));
		        System.out.println("see u soon.");
		} catch (Exception e) {
			e.printStackTrace();
		}

		
	}
	
	static void tabuPensaciTu(
	MyGA myGA,
	int populationDim,
	Instance instance, 
	MoveManager moveManager, 
	ObjectiveFunction objFunc, 
	TabuList tabuList, 
	boolean minmax, 
	PrintStream outPrintSream,
	int tabuIteration, 
	Properties prop
	){
		MySolution initialSol;
		MySearchProgram search;
		System.out.println("improving bad init Population chromosomes");
		prop.setProperty("enableCheckImprovement", "false");
		
		try{
		myGA.getPopulation().sort();
		for(int i =populationDim/2; i< populationDim; i++){
			//System.out.println("start TABU with solution "+i);
			initialSol 		= myGA.getPopulation().getChromosome(i).getSolution();
			
			// Start solving  
			search 			= new MySearchProgram(instance, initialSol, moveManager,
					objFunc, tabuList, false,  outPrintSream, prop);

			search.tabuSearch.setIterationsToGo(tabuIteration);	// Set number of iterations
			search.tabuSearch.startSolving();	
			if (search.feasibleCost.total != Double.POSITIVE_INFINITY)
				myGA.insertBestTabuSolutionIntoInitPopulation(search.feasibleRoutes, i, search.getSol().alpha, search.getSol().beta, search.getSol().gamma);
			else
				myGA.insertBestTabuSolutionIntoInitPopulation(search.bestRoutes, i, search.getSol().alpha, search.getSol().beta, search.getSol().gamma);
			
		}
		prop.setProperty("enableCheckImprovement", "true");
		System.out.println("done");

		//END TRY BLOCK
	} catch (Exception e) {
		e.printStackTrace();
	}
	}
	
	
}
