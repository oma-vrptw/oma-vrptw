package com.MyGeneticA;

import java.io.Console;
import java.util.Random;

import com.mdvrp.Cost;
import com.mdvrp.Instance;

public class MyGA {
	private Population population;
	private int populationDim;
	private int chromosomeDim;
	private Instance instance;
	
	public MyGA(int chromosomeDim, int populationDim, Instance instance) { 
		this.chromosomeDim = chromosomeDim;
		this.populationDim = populationDim;
		this.instance = instance;
		this.population = new Population(populationDim, instance);
	}
	
	public void initPopulation() {
		for (int i = 0; i < populationDim; i++)
        {
			Chromosome c = new Chromosome(chromosomeDim);
			
			boolean [] usedCustomer = new boolean[instance.getCustomersNr()];
			
			for(int j = 0; j < instance.getCustomersNr(); j++) usedCustomer[j] = false;
			
			int routeCapacity = 0;
			int usedRoutes = 0;
			boolean endOfRoute;
			int customerChosen;
			
			/*
			 * while we have genes to insert and vehicles into depote, try to build a feasible
			 * (ignore time window constraints, so it's actually infeasible)
			 *  chromosome (solution)
			 */
			int iGene;
			
            for (iGene = 0; iGene < chromosomeDim && usedRoutes < instance.getVehiclesNr(); )
            {
            	//start building new route
            	endOfRoute = false;
            	
            	Random random = new Random();
            	//retrieve a number between (0 .. CustomersNr-1)
            	int startCustomer = random.nextInt(instance.getCustomersNr());
            	int assignedCustomersNr = instance.getCustomersNr();
            	//try to fill a new route
            	//for(int j = 0; j < instance.getCustomersNr() && !endOfRoute; j++){
            	for(int j = startCustomer; j < assignedCustomersNr + startCustomer; ++j){
            		customerChosen = j % assignedCustomersNr;
            		
            		if(usedCustomer[customerChosen] == true || routeCapacity+(int)instance.getCapacity(customerChosen) > 200){
            			//skip to next customer, this was considered yet	
            			continue;
            		}
	            	
	            	//insert the selected customer into the route
	            	c.setGene(iGene, customerChosen);
	            	routeCapacity = routeCapacity + (int)instance.getCapacity(customerChosen);
	            	usedCustomer[customerChosen] = true;
	            	iGene++;
            	}
            	
            	//end of route
            	usedRoutes++;
            	
            	//se non è l'ultima rotta
            	if(usedRoutes < instance.getVehiclesNr()){
	            	c.setGene(iGene, -1);
	        		routeCapacity = 0;
	        		iGene++;
            	}
            }
            
       
            if(iGene < chromosomeDim){
            	for(int j = 0; j < instance.getCustomersNr(); j++){
            		if(usedCustomer[j] == false){
            			//insert the selected customer into the route
    	            	c.setGene(iGene, j);
    	            	routeCapacity = routeCapacity + (int)instance.getCapacity(j);
    	            	usedCustomer[j] = true;
    	            	//avanzo nel cromosoma
    	            	iGene++;
            		}
            	}
            	
        		
        	}
            
            c.setGene(iGene, -1);
            //lasciato per compatibilità, cit. roberto
    		usedRoutes++;
    		routeCapacity = 0;
            
            population.setChromosome(i, c);

        }
		
		//test code (stub)
		System.out.println("[[[INIT_POPULATION]]]");
		population.printPopulation();
		Chromosome[][] selection = new Chromosome[4][2];
		int cr = 0;
		for(int i = 0; i < 4; i++) {
			selection[i][0] = population.getChormosome(cr);
			selection[i][1] = population.getChormosome(cr+1);
			cr += 2;
		}
		
		System.out.println("[[[CROSSOVER]]]");
		
		Chromosome[] result = crossover(selection);
		for(int i = 0; i < result.length; i++){
			System.out.print("Child["+i+"]: ");
			result[i].print();
			System.out.println();
		}
		
		population.printPopulation();
		for(int i = 0; i < populationDim; i++){
			System.out.println("fitness("+i+"): " + getFitness(i));
		}
		
		
		/*
		System.out.println("[[[GENERATE_NEW_POPULATION]]]");
		generateNewPopulation(result);
		population.printPopulation();
		*/
	}
	Chromosome[][] selectParents() { return null; }
	
	Chromosome[] crossover(Chromosome[][] parents) { 
		
		Chromosome[] children = new Chromosome[parents.length*2];
		
		int firstCut = (chromosomeDim/3);
		int secondCut = ((chromosomeDim*2)/3);
		System.out.println("chromosomeDim: "+chromosomeDim+" firstCut: "+firstCut+ " secondCut: "+secondCut);
		
		int numChildren = 0;
		
		for(int i = 0; i < parents.length; i++){
			children[numChildren] = new Chromosome(chromosomeDim);
			children[numChildren+1] = new Chromosome(chromosomeDim);
			
			for(int j = 0; j < firstCut; j++){
				children[numChildren].setGene(j, parents[i][0].getGene(j));
				children[numChildren+1].setGene(j, parents[i][1].getGene(j));
			}
			
			for(int j = firstCut; j < secondCut; j++){
				children[numChildren].setGene(j, parents[i][1].getGene(j));
				children[numChildren+1].setGene(j, parents[i][0].getGene(j));
			}
			
			for(int j = secondCut; j < chromosomeDim; j++){
				children[numChildren].setGene(j, parents[i][0].getGene(j));
				children[numChildren+1].setGene(j, parents[i][1].getGene(j));
			}
			
			numChildren += 2;
		}
		
		return children; 
	}
	
	void generateNewPopulation(Chromosome[] children) { 

		Population p_new = new Population(populationDim, instance);
		Population child = new  Population (children.length, instance);
		for(int h=0; h<children.length; h++){
			child.setChromosome(h, children[h]);
		}
		
		int percentageChoose = (int)(0.2*populationDim);
		
		int c = 0;
		int counter1 = 1;
		
		while(counter1 < percentageChoose){
			System.out.println("CIAO");
			int IDbestChr = population.getBestChromosome();
			System.out.println("BestChromosome is: "+IDbestChr);
			p_new.setChromosome(c, population.getChormosome(IDbestChr));
			population.setChromosome(IDbestChr, null);
			counter1++;
			c++;
		}
											
		int cnt = percentageChoose;									
		int counter2 = 1;	
		while(counter2 < percentageChoose){
			int IDbestChi = child.getBestChromosome();
			System.out.println("BestChromosome is: "+IDbestChi);
			p_new.setChromosome(cnt, child.getChormosome(IDbestChi));
			children[IDbestChi] = null;
			counter2++;
			cnt++;	
		}
		
		/*creo un array contenente il totale dei cromosomi iniziali e dei figli generati*/
		Population ArrayTotal = new Population (populationDim+children.length, instance);
		
		int counter3 = 0;
		for(int k=0; k < populationDim+children.length; k++){
			if(k<populationDim){	
				ArrayTotal.setChromosome(k, population.getChormosome(k));}
					else{ 
				 		 ArrayTotal.setChromosome(k, child.getChormosome(counter3));
				 		 counter3++;}
		}
		
		/*genero un array temporaneo che utilizzo per prendere blocchi randomici di 4 cromosomi e selezionare il best*/
		Population TempArray = new Population(4, instance);
		
		int index = (int)(0.4*percentageChoose);
		System.out.println("Index: "+index);
		int postiDisponibili = populationDim - index;	
		int counter4 = 1;
		
		while(counter4 < postiDisponibili){	
		int cycle=0;
		
			Random rnd = new Random();
			while(cycle<4){
				int random = rnd.nextInt(populationDim+children.length-1);
					if(ArrayTotal.getChormosome(random) == null){break;}
						else{
							TempArray.setChromosome(cycle, ArrayTotal.getChormosome(random));
							cycle++;}
						  }
		int ID = TempArray.getBestChromosome();
		
		p_new.setChromosome(index, TempArray.getChormosome(ID));
		index++;
		}
		
		/*;creo la nuova popolazione andando a rimpiazzare l'array iniziale con quello che ho creato ArrayTotal*/
		for(int n=0; n<populationDim; n++){
		population.setChromosome(n, p_new.getChormosome(n));
		}
	}
	
	Chromosome selectBestChromosome() { return null; }
	
	double getFitness(int index) { 
		MyGASolution sol = new MyGASolution(population.getChormosome(index), instance);
		
		return sol.getFitness();
	}
}
