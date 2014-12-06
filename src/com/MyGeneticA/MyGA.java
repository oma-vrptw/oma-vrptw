package com.MyGeneticA;

import java.util.Random;

import com.TabuSearch.MySolution;
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
		
		
		
	}
	Chromosome[][] selectParents() { 
		Chromosome[][] selection = new Chromosome[4][2];
		int cr = 0;
		for(int i = 0; i < 4; i++) {
			selection[i][0] = population.getChromosome(cr);
			selection[i][1] = population.getChromosome(cr+1);
			cr += 2;
		} 
		return selection;
	}
	
	Chromosome[] crossover(Chromosome[][] parents) { 
		
		int childrenNum = parents.length*2;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"
		
		//calcolo dei tagli
		int firstCut = (chromosomeDim/3);
		int secondCut = ((chromosomeDim*2)/3);
		System.out.println("chromosomeDim: "+chromosomeDim+" firstCut: "+firstCut+ " secondCut: "+secondCut);
		
		int k = 0; //variabile usata per riempire i figli (viene ogni volta incrementata di +2)
		
		//genero i figli
		for(int i = 0; i < parents.length; i++){ 
			for(int j = 0; j < 2; j++){ //j=0 genero primo figlio della "corrente" coppia, j=1 genero secondo figlio
				children[k+j] = new Chromosome(chromosomeDim);
				copyGenesInFrom(children[k+j], firstCut, secondCut, parents[i][j], firstCut, secondCut); //riempio la parte centrale del figlio1 con la parte centrale del genitore1
				
				 //optimization thing!!! faccio un cromosoma con soltanto la parte centrale del figlio così 
				//ogni volta che devo controllare se il gene del genitore è possibile inserirlo risparmio molto in termini di numero di iterazioni
				int centralPartDim = (secondCut-firstCut);
				Chromosome centralPart = new Chromosome(centralPartDim);
				copyGenesInFrom(centralPart, 0, centralPartDim, children[k+j], firstCut, secondCut);
				
				int nRoutes = children[k+j].getNumRoutes();
				int indexVal = secondCut; //indice relativo al figlio 
				int remainingVals = (chromosomeDim-centralPartDim); //valori rimanenti da inserire nel figlio
				int selectedParent = (j+1) % 2; //if j == 0 -> 1; if j == 1 -> 0
				int selectedGene = secondCut; //indice relativo al genitore
				//il cuore della generazione del figlio (sala parto :D)
				for(int z = 0; z < chromosomeDim; z++){
					//è possibile inserire il gene del genitore nel figlio?!?!?
					if(!geneIsPresent(parents[i][selectedParent].getGene(selectedGene), centralPart) || (parents[i][selectedParent].getGene(selectedGene) == -1 && nRoutes < instance.getVehiclesNr())){ 
						if(parents[i][selectedParent].getGene(selectedGene) == -1) nRoutes ++;
						children[k+j].setGene(indexVal, parents[i][selectedParent].getGene(selectedGene));
						indexVal = (indexVal+1) % chromosomeDim;
						remainingVals --;
						if(remainingVals == 0) break; //ho inserito l'ultimo gene nel figlio (è natoooooo :D)
					}
					selectedGene = (selectedGene+1) % chromosomeDim;
				}
			}
			k += 2;
		}
		
		return children; 
	}
	
	void copyGenesInFrom(Chromosome dest, int init_d, int end_d, Chromosome src, int init_s, int end_s){
		for(int i = init_d, j = init_s; i < end_d; i++, j++){
			dest.setGene(i, src.getGene(j));
		}
	}
	
	boolean geneIsPresent(int gene, Chromosome c){
		for(int i = 0; i < c.getNumberOfGenes(); i++){
			if(c.getGene(i) == gene) return true;
		}
		return false;
	}
	
	void generateNewPopulation(Chromosome[] children) { 

		Population p_new = new Population(populationDim, instance); //temporary next new population initially empty
		Population child = new  Population (children.length, instance); //population of children

		//set chromosomes into child population
		for(int h=0; h<children.length; h++){
			child.setChromosome(h, children[h]);}

		//define the percentage of the best chromosomes of the old population that will be reinsert in the next new population
		int percentageChoose = (int)(0.2*populationDim);
		//int percentageChoose = (int)((populationDim/10)*2);

		int c = 0;
		int counter1 = 1;

		/*creo un array contenente il totale dei cromosomi iniziali e dei figli generati*/

		//select the best "percentageChoose" of old population and child chromosomes and insert them into the new next population
		while(counter1 <= percentageChoose){
			int IDbestChr = population.getBestChromosomeIndex();
			int IDbestChi = child.getBestChromosomeIndex();

			p_new.setChromosome(c, population.getChromosome(IDbestChr));
			c++;
			p_new.setChromosome(c, child.getChromosome(IDbestChi));

			/*
			population.setChromosome(IDbestChr, null);
			child.setChromosome(IDbestChi, null);
			 */

			population.removeChromosome(IDbestChr);
			child.removeChromosome(IDbestChi);

			counter1++;
			c++;
		}

		//create a new population whose dimension is the total between population dimension and number of children create

		Population ArrayTotal = new Population (populationDim+children.length, instance);
		Chromosome tmp;
		//copy all the chromosomes into a temporary population --> all the chromosomes selected in the previous steps are equal to null
		int index;
		
		index = 0;
		for(int k=0; k < populationDim; k++){
			tmp = population.getChromosome(k);
			if(tmp != null){
				ArrayTotal.setChromosome(index, tmp);
				index++;
			}
		}
		
		for(int k = 0; k < children.length; k++ ){
			tmp = child.getChromosome(k);
			if(tmp != null){
				ArrayTotal.setChromosome(index, tmp);
				index++;
			}			
		}
		

		//create a temporary population that contain the chromosomes choose in a randomic way for the selection
		Population TempArray = new Population(3, instance);


		index = percentageChoose*2;
		int postiDisponibili = populationDim - index;	

		//selection of the remaining chromosomes that will define the next new population
		for(int l=0; l<postiDisponibili; l++){
			int cycle=0;

			Random rnd = new Random();
			//select 3 chromosomes from the total population and put the best into the next new population
			for(int h=0; cycle<=2; h++){

				int random = rnd.nextInt(populationDim+children.length-1);

				if(ArrayTotal.getChromosome(random) != null){
					TempArray.setChromosome(cycle, ArrayTotal.getChromosome(random));
					cycle++;}

			} //end inner "for"		

			int ID = TempArray.getBestChromosomeIndex();
			p_new.setChromosome(index, TempArray.getChromosome(ID));

			index++;
		} //end outer "for"

		//create the next new population
		for(int n=0; n<populationDim; n++){
			population.setChromosome(n, p_new.getChromosome(n));
		}
	}

	public MySolution getBestSolution(){
		Chromosome best;
		MyGASolution bestSolution;
		
		best = population.getBestChromosome();
		bestSolution = best.getSolution();
		
		System.out.println("Selected best chromosome. Its fitness is: " + best.getFitness());
		bestSolution.getChromosome().print();
		return (MySolution)bestSolution;
	}
	
	double getFitness(Chromosome c) { 		
		return c.getFitness();
	}


	public void evolve() {
		int count;
		int iteration = 100;

		initPopulation();

		count = 0;
		do{

			Chromosome[][] selection = selectParents();

			System.out.println("[[[CROSSOVER]]]");


			Chromosome[] result = crossover(selection);
			for(int i = 0; i < result.length; i++){
				System.out.print("Child["+i+"]: ");
				result[i].print();
				System.out.println();
			}

			generateNewPopulation(result);

			if((count % 20) == 0){
				population.printPopulation();
				for(int i = 0; i < populationDim; i++){
					System.out.println("fitness("+i+"): " + getFitness(population.getChromosome(i)));
				}
			}

			count++;
		}while(count < iteration);

	}
}
