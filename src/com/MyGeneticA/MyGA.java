package com.MyGeneticA;

import java.lang.reflect.Array;
import java.util.*;

import com.TabuSearch.MySolution;
import com.mdvrp.Cost;
import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;
import com.mdvrp.Vehicle;


public class MyGA {
	private Population population;
	private int populationDim;
	private int chromosomeDim;
	private Instance instance;
	private MyGASolution[] initialSolutions;
	private Properties prop;
	private MyPFIH pfihSol;
	private HashSet<Chromosome> optChrom;
	
	public MyGASolution getInitialSolutions(int index) {
		return initialSolutions[index];
	}

	public MyPFIH getPfihSol() {
		return pfihSol;
	}
	
	/** statistics--average deviation of current generation */
	double[] genAvgDeviation; 

	/** statistics--average fitness of current generation */
	double[] genAvgFitness;
	
	/** threshold to establish if to perform mutation */
	double threshold;
	
	private int maxGenerations;

	private int crossoverProb;
	
	private boolean computeStatistics; 

	/**
	 * Initializes the GA using given parameters
	 * @param chromosomeDim
	 * @param populationDim
	 * @param instance
	 * @param maxGenerations
	 * @param computeStatistics
	 * @param threshold
	 * @param properties
	 */
	public MyGA(int chromosomeDim, int populationDim, Instance instance, int maxGenerations, boolean computeStatistics, double threshold, Properties p) { 
		this.chromosomeDim = chromosomeDim;
		this.populationDim = populationDim;
		this.instance = instance;

		this.population = new Population(populationDim, instance);

		this.maxGenerations = maxGenerations;
		this.genAvgDeviation = new double[maxGenerations];
		this.genAvgFitness = new double[maxGenerations];
		this.computeStatistics = computeStatistics;
		this.threshold = threshold;
		//array stores solutions made by heuristics
		this.initialSolutions = new MyGASolution[3];
		this.prop = p;
		this.optChrom = new HashSet<Chromosome>();
	}

	public boolean isComputeStatistics() {
		return computeStatistics;
	}

	public void setComputeStatistics(boolean computeStatistics) {
		this.computeStatistics = computeStatistics;
	}

	private void GenerateRandomChromosome(int i)
	{
		Chromosome c = new Chromosome(chromosomeDim);		

		boolean [] usedCustomer = new boolean[instance.getCustomersNr()];

		for(int j = 0; j < instance.getCustomersNr(); j++) usedCustomer[j] = false;

		int routeCapacity = 0;
		int usedRoutes = 0;
		double totalCapacity = instance.getCapacity(0, 0);

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
			for(int j = startCustomer; j < assignedCustomersNr + startCustomer; ++j)
			{
				customerChosen = j % assignedCustomersNr;

				if(usedCustomer[customerChosen] == true || routeCapacity + (int)instance.getCapacity(customerChosen) > totalCapacity)
				{
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

			//se non � l'ultima rotta
			if(usedRoutes < instance.getVehiclesNr()){
				//c.setGene(iGene, -1);
				routeCapacity = 0;
				//iGene++;
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

		//c.setGene(iGene, -1);
		//lasciato per compatibilit�, cit. roberto
		usedRoutes++;
		routeCapacity = 0;

		population.setChromosome(i, c);
	}

	public void initPopulation() 
	{

		MyCW generator = new MyCW(chromosomeDim, instance);
		MySolution initialSol = new MySolution(instance);
		this.pfihSol = new MyPFIH(chromosomeDim, instance);
		
		Chromosome tesista = new Chromosome(initialSol.getRoutes(), chromosomeDim);
		Chromosome cw = generator.GenerateChromosome();
		Chromosome pfih = new Chromosome(pfihSol.getRoutes(), chromosomeDim);
		
		// CW
		population.setChromosome(0, cw);
		// InitialSol del package
		population.setChromosome(1, tesista);
		// PFIH
		population.setChromosome(2, pfih);
		
		initialSolutions[0] = tesista.getSolution();
		initialSolutions[1] = cw.getSolution();
		initialSolutions[2] = pfih.getSolution();
		
		System.out.println("1. Tesista solution, fitness: " + Math.round(population.getChromosome(1).getFitness()));
		System.out.println("2. CW solution, fitness: " + Math.round(population.getChromosome(0).getFitness()));
		System.out.println("3. PFIH solution, fitness: " + Math.round(population.getChromosome(2).getFitness()));
		
		// Tutti gli altri sono randomici
		for(int i = 3; i < populationDim; i++)
			GenerateRandomChromosome(i);
		
		//System.out.println("fitness heuristic first solution by tesista: "+c.getFitness());		

		//test code (stub)

		//System.out.println("[[[INIT_POPULATION]]]");
		//population.printPopulation();


	}
	/*
	Chromosome[][] selectParents() { 
		Chromosome[][] selection = new Chromosome[4][2];
		int cr = 0;

		population.sort();

		for(int i = 0; i < 4; i++) {
			selection[i][0] = population.getChromosome(cr);
			selection[i][1] = population.getChromosome(cr+1);
			cr += 2;
		} 
		return selection;
	}*/

	private Chromosome[][] selectParents() {
		
		int numberOfParents = populationDim/4, R1 = 0, R2 = 0;;
		int diversityRate;
		int maxIteration ;
        int iterationDone;
	    Chromosome[][] parents= new Chromosome[numberOfParents][2];
		boolean[] map = new boolean[populationDim];
		boolean flag1, flag2, found;

		Random R= new Random();	
		
		Chromosome c1, c2;
    	
	    //riempimento iniziale mappa a true
    	Arrays.fill(map, true);
    	
    	population.sort();
    	
    	diversityRate =  populationDim/100*20;
    	
        for(int i=0; i < numberOfParents/2; i++)
        {	   	
        	c1 = population.getChromosome(i);
        	parents[i][0] = c1;
        	map[i] = false; //mark chromosome as already taken
        	
        	//search a good partner for c1
        	found = false;
        	for(int j = i+1; j < populationDim; j++){
        		c2 = population.getChromosome(j);
        		if(c1.differentGenesAmongTwoChroms(c2) > diversityRate){
        			//a good partner found, good in terms of fitness but different enough
        			found = true; 
        			parents[i][1] = c2; 
        			map[j] = false; //mark chromosome as already taken
        			break;
        		}
        	}
        	
        	if(!found){
        		//if we haven't found any good partner choose the nearest in terms of fitness
        		parents[i][1] = population.getChromosome(i+1);
        		map[i+1] = false;
        	}
        }
        
        
        for(int i=numberOfParents/2; i < numberOfParents; i++)
        {
        	flag1=false;
        	//keep one parent not already taken randomly
        	iterationDone = 0; //deterministic loop exit condition 
        	while(!flag1 && iterationDone < chromosomeDim)
            {
            	R1=R.nextInt(populationDim);
            	
            	if(map[R1]) flag1 = true;
            	iterationDone++;
            }
        	
        	c1 = population.getChromosome(R1);
            parents[i][0] = c1 ;
            map[R1] = false; //mark chromosome as already taken
            
            //search a good partner for c1
            flag2=false;
            c2 = null;
            iterationDone = 0;//deterministic loop exit condition 
			while(!flag2 && iterationDone < chromosomeDim)
            {
            	R2=R.nextInt(populationDim);
            		
            	if(map[R2] && R2!=R1){
            		c2 = population.getChromosome(R2);
            		if(c1.differentGenesAmongTwoChroms(c2) > diversityRate){
            			//a good partner found, good in terms of fitness but different enough
            			c2 = population.getChromosome(R2);
                    	parents[i][1] = c2;
            			flag2 = true;
            		}
            	}
            	iterationDone++;
            }
            	
            if(c2 == null){
            	//if we haven't found any good partner choose the best
            	R2=0;
            	c2 = population.getChromosome(R2);
            	parents[i][1] = c2;
            }
            
            map[R2] = false;
        }             
        
        /*
        for(int i=0; i<parents.length; i++){
        	System.out.println("coppia: "+parents[i][0] +" - "+parents[i][1]);
        	System.out.println("fitness: "+parents[i][0].getFitness() + " - "+parents[i][1].getFitness());
        }
        
        System.out.println();
        */
        return parents;
	}
		


	Chromosome[] crossover(Chromosome[][] parents, boolean tempHigh) { 
		Chromosome[] children;
		
		if(tempHigh){
			children = crossoverSpecial(parents);
		}else{
		Random rnd = new Random();

		int selectedCrossover = rnd.nextInt(3);

		switch(selectedCrossover){
			case 0: children =  crossover1pt(parents);
	
			case 1: children =  crossover2pt(parents);
	
			case 2: children =  crossoverUniform(parents);
	
			default: children =  crossoverUniform(parents);
			}
		}
		
		return children;
	}

	void initializeRoute(Route r){		
		
		// add the depot as the first node to the route
		r.setDepot(instance.getDepot(0));
		
		// set the cost of the route
		Cost cost = new Cost();
		r.setCost(cost);
		
		// assign vehicle
		Vehicle vehicle = new Vehicle();
		vehicle.setCapacity(instance.getCapacity(0, 0));
		vehicle.setDuration(instance.getDuration(0, 0));
		r.setAssignedVehicle(vehicle);
	}
	
	Chromosome[] crossoverSpecial(Chromosome[][] partners) {
		int childrenNum = partners.length*2;
		Chromosome[] children = new Chromosome[childrenNum];
		int cuttingPoint;
		int customerChosen;
		Customer customerChosenPtr;
		boolean[] taken = new boolean[chromosomeDim];
		boolean endRoute;
		
		Chromosome child, mom, dad;
		System.out.println("partners");
//		for(int i=0; i < partners.length; i++){
//			
//			System.out.println(partners[i][0]);
//			System.out.println(partners[i][1]);
//			System.out.println("----");
//		}
		
		for(int i=0; i < partners.length; i++){	
			cuttingPoint = getRandom(chromosomeDim);
			
			for(int j=1; j <= 2; j++){
				dad = partners[i][j / 2];
				mom = partners[i][j % 2];
				for(int m=0; m<taken.length; m++)taken[m]=false;
//				System.out.println("mom: "+mom);
//				System.out.println("dad: "+dad);
//				System.out.println("---");
				child = new Chromosome(chromosomeDim);
				
				Route route = new Route();
				initializeRoute(route);
				int k=0;
				child.setGene(k, dad.getGene(k));
				customerChosen = dad.getGene(k);
				customerChosenPtr = instance.getCustomer(customerChosen);
				route.addCustomer(customerChosenPtr);
				MySolution.evaluateRoute(route);
				
				
				//for(k = 1; (k < cuttingPoint || !endRoute) && k < chromosomeDim; k++){
				for(k = 1; k < cuttingPoint ; k++){
					
					child.setGene(k, dad.getGene(k));
					taken[dad.getGene(k)] = true;
					if(customerChosenPtr.getCapacity() + route.getCost().load <= route.getLoadAdmited()	
							&& Math.max(route.getCost().getTotal(), customerChosenPtr.getStartTw()) + instance.getTravelTime(child.getGene(k-1), child.getGene(k))+customerChosenPtr.getServiceDuration() <= route.getDepot().getEndTw()
							//&& Math.max(route.getCost().getTotal(), customerChosenPtr.getStartTw()) + instance.getTravelTime(child.getGene(k-1), child.getGene(k))+customerChosenPtr.getServiceDuration() - instance.getTravelTime(route.getLastCustomerNr(), route.getDepotNr()) <= customerChosenPtr.getEndTw()
							){
						route.addCustomer(customerChosenPtr);
						MySolution.evaluateRoute(route);
					}else{
						
						route = new Route();
						initializeRoute(route);
						route.addCustomer(customerChosenPtr);
						MySolution.evaluateRoute(route);
					}
				}
				
				//calculate how many genes to complete the route starting to cutting point
				int y = k;
				do{
					customerChosen = dad.getGene(y);
					customerChosenPtr = instance.getCustomer(customerChosen);
					route.addCustomer(customerChosenPtr);
					MySolution.evaluateRoute(route);
					y++;
				}while(customerChosenPtr.getCapacity() + route.getCost().load <= route.getLoadAdmited()	
						&& Math.max(route.getCost().getTotal(), customerChosenPtr.getStartTw()) + instance.getTravelTime(dad.getGene(y-1), dad.getGene(y))+customerChosenPtr.getServiceDuration() <= route.getDepot().getEndTw()
						);
				
				//System.out.println("child before cutting point"+child);
				
				
				
					double min = Double.MAX_VALUE;
					int w = -1;

					for(int z = 0; z < chromosomeDim ; z++){
						if(taken[mom.getGene(z)])continue;
						customerChosen = mom.getGene(z);
						customerChosenPtr = instance.getCustomer(customerChosen);
						if(min > instance.getTravelTime(child.getGene(k-1), mom.getGene(z)) && 
								customerChosenPtr.getCapacity() + route.getCost().load <= route.getLoadAdmited()	
								&& Math.max(route.getCost().getTotal(), customerChosenPtr.getStartTw()) + instance.getTravelTime(child.getGene(k-1), customerChosen)+customerChosenPtr.getServiceDuration() <= route.getDepot().getEndTw()
								//&& Math.max(route.getCost().getTotal(), customerChosenPtr.getStartTw()) + instance.getTravelTime(child.getGene(k-1), customerChosen)+customerChosenPtr.getServiceDuration() - instance.getTravelTime(route.getLastCustomerNr(), route.getDepotNr()) <= customerChosenPtr.getEndTw()
								){
							min = instance.getTravelTime(child.getGene(k-1), mom.getGene(z));
							w = z;
						}
					}
					if(min == Double.MAX_VALUE){
						w = k;
						route = new Route();
						initializeRoute(route);
					}
					
					child.setGene(k, mom.getGene(w));
					taken[mom.getGene(w)] = true;
					
					k++;
					w++;
				for(; k < y; k++){
					if(taken[mom.getGene(w)])continue;
					customerChosen = mom.getGene(w);
					customerChosenPtr = instance.getCustomer(customerChosen);
				
					if(		customerChosenPtr.getCapacity() + route.getCost().load <= route.getLoadAdmited()	
							&& Math.max(route.getCost().getTotal(), customerChosenPtr.getStartTw()) + instance.getTravelTime(child.getGene(k-1), customerChosen)+customerChosenPtr.getServiceDuration() <= route.getDepot().getEndTw()
							//&& Math.max(route.getCost().getTotal(), customerChosenPtr.getStartTw()) + instance.getTravelTime(child.getGene(k-1), customerChosen)+customerChosenPtr.getServiceDuration() - instance.getTravelTime(route.getLastCustomerNr(), route.getDepotNr()) <= customerChosenPtr.getEndTw()
							){
						
					route.addCustomer(instance.getCustomer(mom.getGene(w)));
					MySolution.evaluateRoute(route);
					
					child.setGene(k, mom.getGene(w));
					taken[mom.getGene(w)] = true;
					}
				}
				
				
				while(k < chromosomeDim){
					if(taken[dad.getGene(y)]){
						y++;
						continue;
					}
					child.setGene(k, dad.getGene(y));
				}
				
//				System.out.println("child after: cutting point"+child);
//				System.out.println("---");
				children[(j-1) + (2*i)] = child;
				
			}		
			
		}
				
		return children;
	}

	Chromosome[] crossover1pt(Chromosome[][] parents) { 
		int childrenNum = parents.length*2;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"

		//calcolo del taglio
		Random rnd = new Random();
		int cut = rnd.nextInt(chromosomeDim);

		int k = 0; //variabile usata per riempire i figli (viene ogni volta incrementata di +2)

		//genero i figli
		for(int i = 0; i < parents.length; i++){ 
			for(int j = 0; j < 2; j++){ //j=0 genero primo figlio della "corrente" coppia, j=1 genero secondo figlio
				children[k+j] = new Chromosome(chromosomeDim);
				copyGenesInFrom(children[k+j], 0, cut, parents[i][j], 0, cut); //riempio la parte iniziale del figlio1 con la parte iniziale del genitore1

				//optimization thing!!! faccio un cromosoma con soltanto la parte iniziale del figlio cos� 
				//ogni volta che devo controllare se il gene del genitore � possibile inserirlo risparmio molto in termini di numero di iterazioni
				Chromosome initialPart = new Chromosome(cut);
				copyGenesInFrom(initialPart, 0, cut, children[k+j], 0, cut);

				int indexVal = cut; //indice relativo al figlio 
				int remainingVals = (chromosomeDim-cut); //valori rimanenti da inserire nel figlio
				int selectedParent = (j+1) % 2; //if j == 0 -> 1; if j == 1 -> 0
				int selectedGene = cut; //indice relativo al genitore
				//il cuore della generazione del figlio (sala parto :D)
				for(int z = 0; z < chromosomeDim; z++){
					//� possibile inserire il gene del genitore nel figlio?!?!?
					if(!geneIsPresent(parents[i][selectedParent].getGene(selectedGene), initialPart, cut)){ 
						children[k+j].setGene(indexVal, parents[i][selectedParent].getGene(selectedGene));
						indexVal = (indexVal+1) % chromosomeDim;
						remainingVals --;
						if(remainingVals == 0) break; //ho inserito l'ultimo gene nel figlio (� natoooooo :D)
					}
					selectedGene = (selectedGene+1) % chromosomeDim;
				}
			}
			k += 2;
		}

		int newDim = deleteDuplicates(children);
		if(childrenNum == newDim) return children;
		else {
			//System.out.println("Duplicates found!!!");
			Chromosome[] childrenWithoutDuplicates = new Chromosome[newDim];
			int j = 0;
			for(int i = 0; i < childrenNum; i++){
				if(children[i] != null) {
					childrenWithoutDuplicates[j] = children[i];
					j++;
				}
			}
			return childrenWithoutDuplicates;
		}
	}

	Chromosome[] crossover2pt(Chromosome[][] parents) { 

		int childrenNum = parents.length*2;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"

		//calcolo dei tagli
		Random rnd = new Random();
		int firstCut = rnd.nextInt(chromosomeDim/2);
		int secondCut = rnd.nextInt(chromosomeDim/2) + (chromosomeDim/2);

		//System.out.println("chromosomeDim: "+chromosomeDim+" firstCut: "+firstCut+ " secondCut: "+secondCut);

		int k = 0; //variabile usata per riempire i figli (viene ogni volta incrementata di +2)

		//genero i figli
		for(int i = 0; i < parents.length; i++){ 
			for(int j = 0; j < 2; j++){ //j=0 genero primo figlio della "corrente" coppia, j=1 genero secondo figlio
				children[k+j] = new Chromosome(chromosomeDim);
				copyGenesInFrom(children[k+j], firstCut, secondCut, parents[i][j], firstCut, secondCut); //riempio la parte centrale del figlio1 con la parte centrale del genitore1

				//optimization thing!!! faccio un cromosoma con soltanto la parte centrale del figlio cos� 
				//ogni volta che devo controllare se il gene del genitore � possibile inserirlo risparmio molto in termini di numero di iterazioni
				int centralPartDim = (secondCut-firstCut);
				Chromosome centralPart = new Chromosome(centralPartDim);
				copyGenesInFrom(centralPart, 0, centralPartDim, children[k+j], firstCut, secondCut);

				int indexVal = secondCut; //indice relativo al figlio 
				int remainingVals = (chromosomeDim-centralPartDim); //valori rimanenti da inserire nel figlio
				int selectedParent = (j+1) % 2; //if j == 0 -> 1; if j == 1 -> 0
				int selectedGene = secondCut; //indice relativo al genitore
				//il cuore della generazione del figlio (sala parto :D)
				for(int z = 0; z < chromosomeDim; z++){
					//� possibile inserire il gene del genitore nel figlio?!?!?
					if(!geneIsPresent(parents[i][selectedParent].getGene(selectedGene), centralPart, centralPartDim)){ 
						children[k+j].setGene(indexVal, parents[i][selectedParent].getGene(selectedGene));
						indexVal = (indexVal+1) % chromosomeDim;
						remainingVals --;
						if(remainingVals == 0) break; //ho inserito l'ultimo gene nel figlio (� natoooooo :D)
					}
					selectedGene = (selectedGene+1) % chromosomeDim;
				}
			}
			k += 2;
		}

		int newDim = deleteDuplicates(children);
		if(childrenNum == newDim) return children;
		else {
			//System.out.println("Duplicates found!!!");
			Chromosome[] childrenWithoutDuplicates = new Chromosome[newDim];
			int j = 0;
			for(int i = 0; i < childrenNum; i++){
				if(children[i] != null) {
					childrenWithoutDuplicates[j] = children[i];
					j++;
				}
			}
			return childrenWithoutDuplicates;
		}
	}

	Chromosome[] crossoverUniform(Chromosome[][] parents) { 

		int childrenNum = parents.length*2;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"

		Random rnd = new Random();

		int k = 0; //variabile usata per riempire i figli (viene ogni volta incrementata di +2)

		for(int i = 0; i < parents.length; i++){
			children[k] = new Chromosome(chromosomeDim);
			children[k+1] = new Chromosome(chromosomeDim);
			for(int j = 0; j < chromosomeDim; j++){
				int tmp = rnd.nextInt(2); //0 or 1

				if(!geneIsPresent(parents[i][tmp].getGene(j), children[k], j) && !geneIsPresent(parents[i][(tmp+1)%2].getGene(j), children[k+1], j)){ 
					children[k].setGene(j, parents[i][tmp].getGene(j));
					children[k+1].setGene(j, parents[i][(tmp+1)%2].getGene(j)); 
				}else{
					children[k].setGene(j, parents[i][(tmp+1)%2].getGene(j));
					children[k+1].setGene(j, parents[i][tmp].getGene(j)); 
				}
			}
			k += 2;
		}

		int newDim = deleteDuplicates(children);
		if(childrenNum == newDim) return children;
		else {
			//System.out.println("Duplicates found!!!");
			Chromosome[] childrenWithoutDuplicates = new Chromosome[newDim];
			int j = 0;
			for(int i = 0; i < childrenNum; i++){
				if(children[i] != null) {
					childrenWithoutDuplicates[j] = children[i];
					j++;
				}
			}
			return childrenWithoutDuplicates;
		}
	}

	void copyGenesInFrom(Chromosome dest, int init_d, int end_d, Chromosome src, int init_s, int end_s){
		for(int i = init_d, j = init_s; i < end_d; i++, j++){
			dest.setGene(i, src.getGene(j));
		}
	}

	boolean geneIsPresent(int gene, Chromosome c, int cDim){
		for(int i = 0; i < cDim; i++){
			if(c.getGene(i) == gene) return true;
		}
		return false;
	}

	int deleteDuplicates(Chromosome[] children){
		int newDim = children.length;
		for(int i = 0; i < children.length; i++){
			if(children[i] != null){
				for(int j = 0; j < children.length; j++){
					if(children[j] != null){
						if(i != j && children[i].compareToGenes(children[j])){
							children[j] = null;
							newDim--;
						}
					}
				}
			}
		}
		return newDim;
	}

	void generateNewPopulation(Chromosome[] children) { 

		Population p_new = new Population(populationDim, instance); //temporary next new population initially empty
		Population child = new  Population (children.length, instance); //population of children

		//set chromosomes into child population
		for(int h=0; h<children.length; h++){
			child.setChromosome(h, children[h]);}

		//define the percentage of the best chromosomes of the old population that will be reinsert in the next new population
		int precentageChoose = (populationDim/10)*2;

		int c = 0;
		int counter1 = 1;
		int IDbestChr, IDbestChi;

		while(counter1 <= precentageChoose ){
			IDbestChr = population.getBestChromosomeIndex();

			p_new.setChromosome(c, population.getChromosome(IDbestChr));
			population.removeChromosome(IDbestChr);

			counter1++;
			c++;
		}

		int counter2 = 1;
		int min = Math.min(children.length, precentageChoose);

		if(min!=0){

			while(counter2 <= min){
				IDbestChi = child.getBestChromosomeIndex();

				p_new.setChromosome(c, child.getChromosome(IDbestChi));

				child.removeChromosome(IDbestChi);

				counter2++;
				c++;} }
		//create a new population whose dimension is the total between population dimension and number of children create

		Population ArrayTotal = new Population (populationDim+children.length, instance);
		Chromosome tmp;
		//copy all the chromosomes into a temporary population --> all the chromosomes selected in the previous steps are equal to null
		int index=0;

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

		//int index1 =  precentageChoose ;
		int postiDisponibili = populationDim - precentageChoose - min ;	



		double tmpFitness, bestFitness;
		int ID = 0, random;
		//selection of the remaining chromosomes that will define the next new population
		for(int l=0; l<postiDisponibili; l++){

			Random rnd = new Random();
			//select 3 chromosomes from the total population and put the best into the next new population

			bestFitness = 0;
			for(int cycle=0; cycle<=2; ){

				random = rnd.nextInt(populationDim+children.length-1);

				if(ArrayTotal.getChromosome(random) != null){
					tmpFitness = ArrayTotal.getChromosome(random).getFitness();
					if(bestFitness <  tmpFitness){
						bestFitness = tmpFitness;
						ID = random;
					}

					cycle++;
				}

			} //end inner "for"		

			p_new.setChromosome(c, ArrayTotal.getChromosome(ID));
			ArrayTotal.removeChromosome(ID);

			c++;
		} //end outer "for"

		//create the next new population
		for(int n=0; n<populationDim; n++){
			population.setChromosome(n, p_new.getChromosome(n));
		}
	}

	void swapMutation(Population P) {

		int gene_tmp;
		Random rnd1 = new Random();
		Random rnd2 = new Random();
		int i = 0;
		int k = 0;
		boolean matrix[][];

		int numSwap = (int) (chromosomeDim*Double.parseDouble(prop.getProperty("numSwap"))); //FACCIO UN NUMERO DI SWAP PARI AL 3% DEL NUMERO DI CUSTOMER, QUINDI SE HO 100 CUSTOMER FACCIO 3 SWAP ALL'INTERNO DEL CROMOSOMA i-esimo

		while(i < ((int) (populationDim*Double.parseDouble(prop.getProperty("mutationChromosomeN"))))){ //faccio la mutation solo sul 5% dei cromosomi quindi se ho 100 cromosomi applico la mutation su 5 di questi

			matrix = new boolean [instance.getCustomersNr()][instance.getCustomersNr()];

			for(int h=0; h<instance.getCustomersNr(); h++){
				matrix[h][h]=false;} 
			
			while(k<numSwap){ //faccio 3(%) swap di geni sui primi 5(%) cromosomi della popolazione

				int sw1=rnd1.nextInt(instance.getCustomersNr()-1);
				int sw2=rnd2.nextInt(instance.getCustomersNr()-1);

				if(matrix[sw1][sw2]==false){
					gene_tmp = P.getChromosome(i).getGene(sw1);
					P.getChromosome(i).setGene(sw1, P.getChromosome(i).getGene(sw2));
					P.getChromosome(i).setGene(sw2, gene_tmp);
					k++;
					matrix[sw1][sw2]=true;}

			}

			i++;}

	}

	public MySolution getBestSolution(){
		Chromosome best;
		MyGASolution bestSolution;

		best = population.getBestChromosome();
		bestSolution = best.getSolution();

		//System.out.println("Selected best chromosome. Its fitness is: " + best.getFitness());
		bestSolution.getChromosome().print();
		return (MySolution)bestSolution;
	}

	double getFitness(Chromosome c) { 		
		return c.getFitness();
	}


	public void evolve() {

		int count;
		int iteration = 1;

		count = 0;
		do{

			Chromosome[][] selection = selectParents();

			/*population.printPopulation();
			System.out.println("[[[CROSSOVER]]]");
			 */

			Chromosome[] result = crossover(selection, true);

			/*System.out.println("result.length: "+result.length);

			for(int i = 0; i < result.length; i++){
				System.out.print("Child["+i+"]: ");
				result[i].print();
				System.out.println();
			}*/

			generateNewPopulation(result);

			/*if((count % 20) == 0){



				for(int i = 0; i < result.length; i++){
					System.out.print("Child["+i+"]: ");
					result[i].print();
					System.out.println();
				}

				population.printPopulation();
				for(int i = 0; i < populationDim; i++){
					System.out.println("fitness("+i+"): " + getFitness(population.getChromosome(i)));
				}
			}*/

			count++;
		}while(count < iteration);

	}


	public void evolve2(Boolean doMutation) {
		int iGen;
		int windowSize = (int) (populationDim*Double.parseDouble(prop.getProperty("windowSize")));
		iGen = 0;
		int mutationDone=0;
		boolean temp;
		
		temp = true;
		do{
			if(iGen > (maxGenerations / 2))
				temp = false;
			
			if(temp)
				doGeneticMating(temp);
			
			if (doMutation)
			{
				this.genAvgDeviation[iGen] = getAvgDeviationAmongChroms();
				this.genAvgFitness[iGen] = getAvgFitness();

					//windowSize 5  % popolazione
					//if(iGen > windowSize){
						double windowAvgFitness = getWindowAvgFitness(iGen, windowSize);
						
						if( getAvgFitness(iGen) >= windowAvgFitness - windowAvgFitness/100*threshold
								&& getAvgFitness(iGen) <= windowAvgFitness+windowAvgFitness/100*threshold
								){
							//System.out.println("mutation done!");
							//System.out.println("media finestra: "+windowAvgFitness+ " media questa popolazione: "+getAvgFitness(iGen));
							swapMutation(population);
							mutationDone++;
						}			
					//}
						//System.out.println("deviation in respect of the best: "+this.genAvgDeviation[iGen]);
			}
			
			iGen++;
		}while(iGen < maxGenerations);
System.out.println("mutation done "+mutationDone+" times.");
	}

	public void insertBestTabuSolutionIntoInitPopulation(Route[][] feasibleRoutes) {
		Chromosome c;
		//build a chromosome from a route 
		c = new Chromosome(feasibleRoutes, chromosomeDim);


		population.swapChromosome(c, population.getWorstChromosomeIndex());
		//System.out.println("Fitness del nuovo inserito = "+c.getFitness()+" route number: "+c.getRoutesNumber());			
	}

	public ArrayList<MySolution> getNDifferentBestSolutions(int nMax) {
		Chromosome c;
		
		ArrayList<MyGASolution> solution;
		boolean toInsert;
		int x;
		
		solution = new ArrayList<MyGASolution>();

		population.sort();
		int i;
		for(i = 0; i < populationDim; i++){
			c = population.getChromosome(i);
			if(!optChrom.contains(c)){
				solution.add(0, c.getSolution());
				optChrom.add(c);
				System.out.println("1. Its fitness is: " + Math.round(c.getFitness()));
				break;
			}
		}
		
		for(int j=i, nSelected = 1; nSelected < (nMax) && j < populationDim; j++){
			c = population.getChromosome(j);
			
			if(!optChrom.contains(c)
					//Math.round(c.getFitness()) != Math.round(solution.get(nSelected-1).getChromosome().getFitness()) ||
					 ){
				toInsert = true;
				for(Chromosome chrom : optChrom){
					x = c.differentGenesAmongTwoChroms(chrom);
					if(x < 10 || c.getFitness() > 5*solution.get(0).getChromosome().getFitness())
						toInsert = false;
				}
				if(toInsert){
					solution.add(nSelected, c.getSolution());
					optChrom.add(c);
					nSelected++;
					System.out.println(nSelected+". Its fitness is: " + Math.round(c.getFitness()));
			}
		}
		}
/*
		c = population.getChromosome(getRandom(populationDim/2));
		System.out.println((nMax)+". Its fitness is: " + Math.round(c.getFitness()));
		solution.add(nMax-1, c.getSolution());
		*/
		return new ArrayList<MySolution>(solution);
	}

	/**
	 * Go through all chromosomes and calculate the average fitness (of this generation)
	 * @return double
	 */
	public double getAvgFitness()
	{
		double rSumFitness = 0.0;

		for (int i = 0; i < populationDim; i++)
			rSumFitness += this.population.getChromosome(i).getFitness();
		return (rSumFitness / populationDim);
	}

	/**
	 * Get the average deviation from the current population of chromosomes. The smaller this
	 * deviation, the higher the convergence is to a particular (but not necessarily optimal)
	 * solution. It calculates this deviation by determining how many genes in the populuation
	 * are different than the bestFitGenes. The more genes which are "different", the higher
	 * the deviation.
	 * @return
	 */
	protected double getAvgDeviationAmongChroms()
	{
		int devCnt = 0;
		for (int iGene = 0; iGene < this.chromosomeDim; iGene++)
		{
			int bestFitGene =
					this.population.getBestChromosome().getGene(iGene);
			for (int i = 0; i < populationDim; i++)
			{
				double thisGene = this.population.getChromosome(i).getGene(iGene);
				if (thisGene != bestFitGene)
					devCnt++;
			}

		}

		return ((double)devCnt);
	}


	
	/**
	 * Gets the average deviation of the given generation of chromosomes
	 * @param iGeneration
	 * @return
	 */
	public double getAvgDeviation(int iGeneration)
	{
		return (this.genAvgDeviation[iGeneration]);
	}

	/**
	 * Gets the average fitness of the given generation of chromosomes
	 * @param iGeneration
	 * @return
	 */
	public double getAvgFitness(int iGeneration)
	{
		return (this.genAvgFitness[iGeneration]);
	}

	void doGeneticMating(boolean tempHigh)
	{
		Chromosome[][] parents = selectParents();
		
Chromosome[] children;
//for(int i=0; i < parents.length; i++){
//	
//	System.out.println(parents[i][0]);
//	System.out.println(parents[i][1]);
//	System.out.println("----");
//}

//		if(tempHigh){
//			children = crossoverSpecial(parents);
//		}else{

		Random rnd = new Random();

		int selectedCrossover = rnd.nextInt(3);

		switch(selectedCrossover){
			case 0: children =  crossover1pt(parents);
	
			case 1: children =  crossover2pt(parents);
	
			case 2: children =  crossoverUniform(parents);
	
			default: children =  crossoverUniform(parents);
			}
//		}
		
		generateNewPopulation(children);
	}

	int getRandom(int upperBound)
	{
		int iRandom = (int) (Math.random() * upperBound);
		return (iRandom);
	}
	/*
	 * takes boundary and size of a generation window
	 * and compute avg of avgFitness into this window
	 */
	
	double getWindowAvgFitness(int boundary, int size){
		double avg, count;
		
		//i=window lower bound
		int i = Math.max(0, boundary-size);
		
		avg= count = 0;
		
		for(; i<boundary; i++){
			avg+=getAvgFitness(i);
			count++;
		}
		
		return count > 0 ? avg/count : getAvgFitness(boundary);
	}

	public Population getPopulation() {
		// TODO Auto-generated method stub
		return population;
	}

}
