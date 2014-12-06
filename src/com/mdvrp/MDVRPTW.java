package com.mdvrp;

import java.io.FileWriter;
import java.io.PrintStream;

import org.coinor.opents.TabuList;

import com.TabuSearch.MyMoveManager;
import com.TabuSearch.MyObjectiveFunction;
import com.TabuSearch.MySearchProgram;
import com.TabuSearch.MySolution;
import com.TabuSearch.MyTabuList;

public class MDVRPTW {
		
	public static void main(String[] args) {
		MySearchProgram     search;
		MySolution          initialSol;
		MyObjectiveFunction objFunc;
		MyMoveManager       moveManager;
		TabuList            tabuList;
		Parameters          parameters 		= new Parameters(); 	// holds all the parameters passed from the input line
		Instance            instance; 								// holds all the problem data extracted from the input file
		Duration            duration 		= new Duration(); 		// used to calculate the elapsed time
		PrintStream         outPrintSream 	= null;					// used to redirect the output
		
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
						
			// Init memory for Tabu Search
			initialSol 		= new MySolution(instance);
			objFunc 		= new MyObjectiveFunction(instance);
	        moveManager 	= new MyMoveManager(instance);
	        moveManager.setMovesType(parameters.getMovesType());
	        
	        // Tabu list
	        int dimension[] = {instance.getDepotsNr(), instance.getVehiclesNr(), instance.getCustomersNr(), 1, 1};
	        tabuList 		= new MyTabuList(parameters.getTabuTenure(), dimension);
	        
	        System.out.println("initial solution cost: "+objFunc.evaluate(initialSol, null)[2]);
	        // Create Tabu Search object
	        search 			= new MySearchProgram(instance, initialSol, moveManager,
							            objFunc, tabuList, false,  outPrintSream);
	        // Start solving        
	        search.tabuSearch.setIterationsToGo(parameters.getIterations());
	        search.tabuSearch.startSolving();
	        
	        // wait for the search thread to finish
	        /*try {
	        	// in order to apply wait on an object synchronization must be done
	        	synchronized(instance){
	        		instance.wait();
	        	}
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}*/
	        
	        duration.stop();
	        
	        // Count routes
	        int routesNr = 0;
	        for(int i =0; i < search.feasibleRoutes.length; ++i)
	        	for(int j=0; j < search.feasibleRoutes[i].length; ++j)
	        		if(search.feasibleRoutes[i][j].getCustomersLength() > 0)
	        			routesNr++;
	        duration.stop();
	        // Print results
	        String outSol = String.format(
	        		"\nInstance file: %s\n"
	        		+ "Total cost: %5.2f\n"
	        		+ "Execution time: %d sec\n"
	        		+ "Number of routes: %4d\n",
	        		instance.getParameters().getInputFileName(), search.feasibleCost.total,
	            	duration.getSeconds(), routesNr);
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
