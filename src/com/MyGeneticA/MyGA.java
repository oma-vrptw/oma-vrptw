package com.MyGeneticA;

import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

import org.coinor.opents.MoveManager;
import org.coinor.opents.ObjectiveFunction;
import org.coinor.opents.TabuList;

import com.TabuSearch.MySearchProgram;
import com.TabuSearch.MySolution;
import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Parameters;
import com.mdvrp.Route;


public class MyGA {
	private Population population;
	private int populationDim;
	private int chromosomeDim;
	private Instance instance;
	private Parameters parameters;
	private MyGASolution[] initialSolutions;
	private Properties prop;
	private MyPFIH pfihSol;
	private HashSet<Long> optChrom;
	
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
	public MyGA(int chromosomeDim, int populationDim, Instance instance, Parameters parameters, int maxGenerations, boolean computeStatistics, double threshold, Properties p) { 
		this.chromosomeDim = chromosomeDim;
		this.populationDim = populationDim;
		this.instance = instance;
		this.parameters = parameters;

		this.population = new Population(populationDim, instance);

		this.maxGenerations = maxGenerations;
		this.genAvgDeviation = new double[maxGenerations];
		this.genAvgFitness = new double[maxGenerations];
		this.computeStatistics = computeStatistics;
		this.threshold = threshold;
		//array stores solutions made by heuristics
		this.initialSolutions = new MyGASolution[3];
		this.prop = p;
		this.optChrom = new HashSet<>();
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
			//retrieve a number between (0 .. CustomersNr-1)
			int startCustomer = instance.getRandom().nextInt(instance.getCustomersNr());
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

			//se non è l'ultima rotta
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
		//lasciato per compatibilità, cit. roberto
		usedRoutes++;
		routeCapacity = 0;

		c.evaluate(instance);
		population.setChromosome(i, c);
	}

	boolean DeclonerSwapMutation(Chromosome c, int worstCaseIterations)
	{
		
		int sw1, sw2, gene_tmp, k;
		double epsilon = 0.001, initialFitness = c.getFitness();


		
		
		//System.out.println("Inizio mutazioni");
		for(int i=0; Math.abs(initialFitness - c.getFitness())<epsilon && i<worstCaseIterations;i++)
		{
			sw1 = instance.getRandom().nextInt(instance.getCustomersNr());

			k=0;
			do
			{
				sw2 = instance.getRandom().nextInt(instance.getCustomersNr());
				
				k++;
			}
			while(sw1==sw2 && k<c.getNumberOfGenes()+1);
			
			if(sw1==sw2)
			{
				if(sw2>0 && sw2<c.getNumberOfGenes())
					sw2--;
			
				if(sw2==0) sw2++;
				
			}
			
			//System.out.println(sw1 + " " + sw2);
			
			gene_tmp = c.getGene(sw1);
			c.setGene(sw1, c.getGene(sw2));
			c.setGene(sw2, gene_tmp);
			
			//Ricalcola la fitness
			c.evaluate(instance);
			
		}
		//System.out.println("Fine mutazioni");
		
		
		
		if(Math.abs(initialFitness - c.getFitness())<epsilon)
		{
			//System.out.println("NON DIVERSIFICATO: " + initialFitness + " vs " + c.getFitness());
			return false;
		}
		else
		{
			//System.out.println("DIVERSIFICATO");
			return true;
		}
	}
	
	private void deleteDuplicatesFromInitialPopulation()
	{
		int clones = 0, maxMutations = 100, maxIt = 1000;
		
		Set<Double> set = new HashSet<>();
		Set<Integer> clonesIndexes = new HashSet<>();
		Set<Integer> notAnymoreClonesIndexes = new HashSet<>();
		
		// Ottengo gli indici dei cloni
		for(int i=0; i<populationDim; i++)
		{	
			if(set.add(population.getChromosome(i).getFitness()) == false)
				clonesIndexes.add(i);
		}
		
		clones = populationDim-set.size();
		set.clear();
		
		System.out.println("NUMERO CLONI DA INIT POPULATION: " + clones);
		
		for(int i=0; i<maxIt && !clonesIndexes.isEmpty(); i++)
		{
			for(Integer index : clonesIndexes)
			{
				if(DeclonerSwapMutation(population.getChromosome(index), maxMutations))
					notAnymoreClonesIndexes.add(index);
			}
			
			for(Integer index : notAnymoreClonesIndexes)
			{
				clonesIndexes.remove(index);
			}
			
			population.detectClones();
			
			notAnymoreClonesIndexes.clear();
		}
	}
	
	public void initPopulation() 
	{

		MyCW generator = new MyCW(chromosomeDim, instance);
		MySolution initialSol = new MySolution(instance);
		this.pfihSol = new MyPFIH(chromosomeDim, instance);
		
		Chromosome tesista = new Chromosome(initialSol.getRoutes(), chromosomeDim, instance, initialSol.alpha, initialSol.beta, initialSol.gamma);
		Chromosome cw = generator.GenerateChromosome();
		cw.evaluate(instance);
		Chromosome pfih = new Chromosome(pfihSol.getRoutes(), chromosomeDim, instance, 1, 1, 1);
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
		
		// Muto eventuali cloni
		deleteDuplicatesFromInitialPopulation();
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
        int iterationDone;
	    Chromosome[][] parents= new Chromosome[numberOfParents][2];
		boolean[] map = new boolean[populationDim];
		boolean flag1, flag2, found;
		
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
            	R1 = instance.getRandom().nextInt(populationDim);
            	
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
            	R2 = instance.getRandom().nextInt(populationDim);
            		
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
	
        return parents;
	}

	Chromosome[] heuristicCrossover(Chromosome[][] parents) { 
		int childrenNum = parents.length*2;
		Chromosome[] children = new Chromosome[childrenNum];
		
		for(int i=0; i<children.length; i++)children[i]=new Chromosome(chromosomeDim);
		
		int cut = instance.getRandom().nextInt(chromosomeDim);
		
		for(int i = 0, p = 0; p < parents.length; p++, i+=2){
			Chromosome dad = new Chromosome(parents[p][0]);
			Chromosome mom = new Chromosome(parents[p][1]);
			Chromosome brother = children[i];
			
			brother.setGene(0, dad.getGene(cut));
			
			mom.swapGenes(cut, dad.getGene(cut));
			
			for(int j = 1, k = (cut+1)%chromosomeDim; j < chromosomeDim; j++, k = (k+1)%chromosomeDim){
				double distDad = instance.getTravelTime(brother.getGene(j-1), dad.getGene(k));
				double distMom = instance.getTravelTime(brother.getGene(j-1), mom.getGene(k));
				int gene;
				
				if(distDad > distMom){
					gene = mom.getGene(k);
					dad.swapGenes(k, mom.getGene(k));
				}else{
					gene = dad.getGene(k);
					mom.swapGenes(k, dad.getGene(k));
				}
				
				brother.setGene(j, gene);
			}
			//child.print();
			
			cut = instance.getRandom().nextInt(chromosomeDim);
			
			Chromosome sister= children[i+1];
			sister.setGene(0, dad.getGene(cut));
			
			mom.swapGenes(cut, dad.getGene(cut));
			
			for(int j = 1, k = (cut+1)%chromosomeDim; j < chromosomeDim; j++, k = (k+1)%chromosomeDim){
				Customer customerDadPtr = instance.getCustomer(dad.getGene(k));
				Customer customerMomPtr = instance.getCustomer(mom.getGene(k));
				Customer customerSisterPtr = instance.getCustomer(sister.getGene(j-1));
				
				double startTWDad = customerDadPtr.getStartTw();
				double startTWMom = customerMomPtr.getStartTw();
				double startTWSister = customerSisterPtr.getStartTw();
				int gene;
				
				if(Math.abs(startTWDad - startTWSister) > Math.abs(startTWMom - startTWSister)){
					gene = mom.getGene(k);
					dad.swapGenes(k, mom.getGene(k));
				}else{
					gene = dad.getGene(k);
					mom.swapGenes(k, dad.getGene(k));
				}
				
				sister.setGene(j, gene);
			}
			//child.print();
		}
		
		return children;
	}
	
	Chromosome[] heuristicCrossover(Chromosome[] parents) { 
		int childrenNum = parents.length*2;
		Chromosome[] children = new Chromosome[childrenNum];
		
		for(int i=0; i<children.length; i++)children[i]=new Chromosome(chromosomeDim);
		
		int cut = instance.getRandom().nextInt(chromosomeDim);
		
		for(int i = 0, p = 0; p < parents.length; p++, i+=2){
			Chromosome dad = new Chromosome(parents[0]);
			Chromosome mom = new Chromosome(parents[1]);
			Chromosome brother = children[i];
			
			brother.setGene(0, dad.getGene(cut));
			
			mom.swapGenes(cut, dad.getGene(cut));
			
			for(int j = 1, k = (cut+1)%chromosomeDim; j < chromosomeDim; j++, k = (k+1)%chromosomeDim){
				double distDad = instance.getTravelTime(brother.getGene(j-1), dad.getGene(k));
				double distMom = instance.getTravelTime(brother.getGene(j-1), mom.getGene(k));
				int gene;
				
				if(distDad > distMom){
					gene = mom.getGene(k);
					dad.swapGenes(k, mom.getGene(k));
				}else{
					gene = dad.getGene(k);
					mom.swapGenes(k, dad.getGene(k));
				}
				
				brother.setGene(j, gene);
			}
			//child.print();
			
			cut = instance.getRandom().nextInt(chromosomeDim);
			
			Chromosome sister= children[i+1];
			sister.setGene(0, dad.getGene(cut));
			
			mom.swapGenes(cut, dad.getGene(cut));
			
			for(int j = 1, k = (cut+1)%chromosomeDim; j < chromosomeDim; j++, k = (k+1)%chromosomeDim){
				Customer customerDadPtr = instance.getCustomer(dad.getGene(k));
				Customer customerMomPtr = instance.getCustomer(mom.getGene(k));
				Customer customerSisterPtr = instance.getCustomer(sister.getGene(j-1));
				
				double startTWDad = customerDadPtr.getStartTw();
				double startTWMom = customerMomPtr.getStartTw();
				double startTWSister = customerSisterPtr.getStartTw();
				int gene;
				
				if(Math.abs(startTWDad - startTWSister) > Math.abs(startTWMom - startTWSister)){
					gene = mom.getGene(k);
					dad.swapGenes(k, mom.getGene(k));
				}else{
					gene = dad.getGene(k);
					mom.swapGenes(k, dad.getGene(k));
				}
				
				sister.setGene(j, gene);
			}
			//child.print();
		}
		
		return children;
	}
	
	Chromosome[] crossover1pt(Chromosome[][] parents) { 
		int childrenNum = parents.length*2;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"

		//calcolo del taglio
		int cut = instance.getRandom().nextInt(chromosomeDim);

		int k = 0; //variabile usata per riempire i figli (viene ogni volta incrementata di +2)

		//genero i figli
		for(int i = 0; i < parents.length; i++){ 
			for(int j = 0; j < 2; j++){ //j=0 genero primo figlio della "corrente" coppia, j=1 genero secondo figlio
				children[k+j] = new Chromosome(chromosomeDim);
				copyGenesInFrom(children[k+j], 0, cut, parents[i][j], 0, cut); //riempio la parte iniziale del figlio1 con la parte iniziale del genitore1

				//optimization thing!!! faccio un cromosoma con soltanto la parte iniziale del figlio così 
				//ogni volta che devo controllare se il gene del genitore è possibile inserirlo risparmio molto in termini di numero di iterazioni
				Chromosome initialPart = new Chromosome(cut);
				copyGenesInFrom(initialPart, 0, cut, children[k+j], 0, cut);

				int indexVal = cut; //indice relativo al figlio 
				int remainingVals = (chromosomeDim-cut); //valori rimanenti da inserire nel figlio
				int selectedParent = (j+1) % 2; //if j == 0 -> 1; if j == 1 -> 0
				int selectedGene = cut; //indice relativo al genitore
				//il cuore della generazione del figlio (sala parto :D)
				for(int z = 0; z < chromosomeDim; z++){
					//è possibile inserire il gene del genitore nel figlio?!?!?
					if(!geneIsPresent(parents[i][selectedParent].getGene(selectedGene), initialPart, cut)){ 
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

	Chromosome[] crossover2pt(Chromosome[][] parents) { 

		int childrenNum = parents.length*2;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"

		//calcolo dei tagli
		int firstCut = instance.getRandom().nextInt(chromosomeDim/2);
		int secondCut = instance.getRandom().nextInt(chromosomeDim/2) + (chromosomeDim/2);

		//System.out.println("chromosomeDim: "+chromosomeDim+" firstCut: "+firstCut+ " secondCut: "+secondCut);

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

				int indexVal = secondCut; //indice relativo al figlio 
				int remainingVals = (chromosomeDim-centralPartDim); //valori rimanenti da inserire nel figlio
				int selectedParent = (j+1) % 2; //if j == 0 -> 1; if j == 1 -> 0
				int selectedGene = secondCut; //indice relativo al genitore
				//il cuore della generazione del figlio (sala parto :D)
				for(int z = 0; z < chromosomeDim; z++){
					//è possibile inserire il gene del genitore nel figlio?!?!?
					if(!geneIsPresent(parents[i][selectedParent].getGene(selectedGene), centralPart, centralPartDim)){ 
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

	Chromosome[] pmxCrossover(Chromosome[][] parents) {
		int childrenNum = parents.length*2;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"

		int k = 0; //variabile usata per riempire i figli (viene ogni volta incrementata di +2)

		for(int i = 0; i < parents.length; i++){
			children[k] = new Chromosome(chromosomeDim);
			children[k+1] = new Chromosome(chromosomeDim);
			
			for(int j = 0; j < chromosomeDim; j++){
				children[k].setGene(j, parents[i][0].getGene(j));
				children[k+1].setGene(j, parents[i][1].getGene(j));
			}
			
			boolean[][] matrix = new boolean[chromosomeDim][chromosomeDim];
			int numSwap = (int) (chromosomeDim*0.03);
			
			int z = 0;
			while(z < numSwap){
				int sw1 = instance.getRandom().nextInt(chromosomeDim);
				int sw2 = instance.getRandom().nextInt(chromosomeDim);
				
				if(matrix[sw1][sw2] == false && sw1 != sw2){
					int tmp = children[k].getGene(sw1);
					children[k].setGene(sw1, children[k].getGene(sw2));
					children[k].setGene(sw2, tmp);
					
					tmp = children[k+1].getGene(sw1);
					children[k+1].setGene(sw1, children[k+1].getGene(sw2));
					children[k+1].setGene(sw2, tmp);
					
					matrix[sw1][sw2] = true;
					matrix[sw2][sw1] = true;
					z++;
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

	boolean geneIsPresent(int gene, Chromosome c, int cDim){
		for(int i = 0; i < cDim; i++){
			if(c.getGene(i) == gene) return true;
		}
		return false;
	}

	private boolean areClones(Chromosome c1, Chromosome c2)
	{
		double epsilon = 0.001;
		
		if(c1.compareToGenes(c2)) 
		{
			//System.out.println("SGAMATO CLONE PER I GENI");
			return true;
		}
		if(Math.abs(c1.getFitness() - c2.getFitness()) < epsilon)
		{
			//System.out.println("SGAMATO CLONE PER FITNESS");
			return true;
		}
		
		return false;
	}
	
	Chromosome[] deleteDuplicates(Chromosome[] children)
	{
		
		int newDim = children.length;
		
		for(int i = 0; i < children.length; i++)
		{
			if(children[i] != null)
			{
				for(int j = 0; j < children.length; j++)
				{
					if(i==j) continue;
					
					if(children[j] != null)
					{
						// Se sono cloni
						if(areClones(children[i],children[j]))
						{
							children[j] = null;
							//System.out.println("**************************************DUPLICATE FOUND!!!**********************************");
							newDim--;
							break;
						}
					}
				}
				
				for(int k = 0; k < populationDim; k++)
				{
					if(areClones(children[i],population.getChromosome(k)))
					{
						children[i] = null;
						newDim--;
						//System.out.println("**************************************DUPLICATE FOUND!!!**********************************");
						break;
					}
				}
			}
		}
		
		Chromosome[] childrenWithoutDuplicates = new Chromosome[newDim];
		int j = 0;
		for(int i = 0; i < children.length; i++){
			if(children[i] != null) {
				childrenWithoutDuplicates[j] = children[i];
				j++;
			}
		}
		
		return childrenWithoutDuplicates;
	}

	void generateNewPopulation(Chromosome[] children) { 

		Population p_new = new Population(populationDim, instance); //temporary next new population initially empty
		Population child = new  Population (children.length, instance); //population of children
		
		//set chromosomes into child population
		for(int h=0; h<children.length; h++){
			child.setChromosome(h, children[h]);
		}
		/*
		System.out.println("children, "+children.length);
		child.printPopulation();
		*/
		
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

			//select 3 chromosomes from the total population and put the best into the next new population

			bestFitness = 0;
			for(int cycle=0; cycle<=2; ){

				random = instance.getRandom().nextInt(populationDim+children.length-1);

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


		/*
		 * System.out.println("new population");
		p_new.printPopulation();
		*/

		//create the next new population
		for(int n=0; n<populationDim; n++){
			
			population.setChromosome(n, p_new.getChromosome(n));
		}
	}

//	void swapMutation(Population P) {
//
//		int gene_tmp;
//		Random rnd1 = new Random();
//		Random rnd2 = new Random();
//		int i = 0;
//		int k = 0;
//		boolean matrix[][];
//
//		int numSwap = (int) (chromosomeDim*Double.parseDouble(prop.getProperty("numSwap"))); //FACCIO UN NUMERO DI SWAP PARI AL 3% DEL NUMERO DI CUSTOMER, QUINDI SE HO 100 CUSTOMER FACCIO 3 SWAP ALL'INTERNO DEL CROMOSOMA i-esimo
//
//		while(i < ((int) (populationDim*Double.parseDouble(prop.getProperty("mutationChromosomeN"))))){ //faccio la mutation solo sul 5% dei cromosomi quindi se ho 100 cromosomi applico la mutation su 5 di questi
//
//			matrix = new boolean [instance.getCustomersNr()][instance.getCustomersNr()];
//
//			/*for(int h=0; h<instance.getCustomersNr(); h++){ //ERR
//				matrix[h][h]=false;} */
//			
//			while(k<numSwap){ //faccio 3(%) swap di geni sui primi 5(%) cromosomi della popolazione
//
//				int sw1=rnd1.nextInt(instance.getCustomersNr()-1);
//				int sw2=rnd2.nextInt(instance.getCustomersNr()-1);
//
//				if(matrix[sw1][sw2]==false){
//					gene_tmp = P.getChromosome(i).getGene(sw1);
//					P.getChromosome(i).setGene(sw1, P.getChromosome(i).getGene(sw2));
//					P.getChromosome(i).setGene(sw2, gene_tmp);
//					k++;
//					matrix[sw1][sw2]=true;}
//
//			}
//
//			i++;}
//
//	}

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

//	public void evolve() {
//		int iGen = 0, clones = 0;
//		
//		do{
//			
//			clones = VRPTW_main.countClones(populationDim, population);
//			if(clones>0)
//				System.out.println("<<<<<< CLONI PRIMA DELLA MATING: " + clones + ">>>>>>");
//			
//			// genetic mating
//			doGeneticMating(iGen);
//			
//			clones = VRPTW_main.countClones(populationDim, population);
//			if(clones>0)
//				System.out.println("<<<<<< CLONI DOPO LA MATING: " + clones + ">>>>>>");
//			
//			
//			iGen++;
//		}while(iGen < maxGenerations);
//	}
	
	public void evolve3() {
		int iGen = 0;
		Chromosome[] result;

		do{
			result = doGeneticMating4(iGen);
			
			result = deleteDuplicates(result);
			
			generateNewPopulation(result);
			
			population.detectClones();
			
			iGen++;
		}while(iGen < maxGenerations);
	}
	
    
//	public void insertBestTabuSolutionIntoInitPopulation(Route[][] feasibleRoutes) {
//		Chromosome c;
//		//build a chromosome from a route 
//		c = new Chromosome(feasibleRoutes, chromosomeDim, instance);
//
//
//		population.swapChromosome(c, population.getWorstChromosomeIndex());
//		//System.out.println("Fitness del nuovo inserito = "+c.getFitness()+" route number: "+c.getRoutesNumber());			
//	}
	
//	public void insertBestTabuSolutionIntoInitPopulation2(Route[][] feasibleRoutes, double cost) {
//		
//		if( population.getWorstChromosome().getFitness() <= cost ) 
//			return;
//
//		Chromosome c;
//		//build a chromosome from a route 
//		c = new Chromosome(feasibleRoutes, chromosomeDim, instance);
//
//		population.swapChromosome(c, population.getWorstChromosomeIndex());
//		//System.out.println("Fitness del nuovo inserito = "+c.getFitness()+" route number: "+c.getRoutesNumber());			
//	}
	
	public void insertBestTabuSolutionIntoInitPopulation2(Route[][] feasibleRoutes, double cost, double alpha, double beta, double gamma) {
		
		if( population.getWorstChromosome().getFitness() <= cost ) 
			return;

		Chromosome c;
		//build a chromosome from a route 
		c = new Chromosome(feasibleRoutes, chromosomeDim, instance, alpha, beta, gamma);
		
//		MyGASolution sol = new MyGASolution(c, instance);
//		c.setSolution(sol);
//		c.getSolution().setAlphaBetaGamma(alpha, beta, gamma);
//		c.setFitness();
		
		if(!population.isClone(c)){
//			population.swapChromosome(c, population.getWorstChromosomeIndex(), alpha, beta, gamma);
			population.swapChromosome(c, population.getWorstChromosomeIndex());
			System.out.println("fitness chromosome inserito: "+c.getFitness());
		}else{
			System.out.println("clone detected. skip it.");
		}
		//System.out.println("Fitness del nuovo inserito = "+c.getFitness()+" route number: "+c.getRoutesNumber());			
	}

	public ArrayList<MySolution> getNDifferentBestSolutions(int nMax) {
		Chromosome c;
		
		ArrayList<MyGASolution> solution;
		solution = new ArrayList<MyGASolution>();

		population.sort();
		//population.printPopulation();
		int i;
		for(i = 0; i < populationDim; i++){
			c = population.getChromosome(i);
			if(!optChrom.contains(Math.round(c.getFitness()))){
				solution.add(0, c.getSolution());
				optChrom.add(Math.round(c.getFitness()));
				String msg = "1. Its fitness is: " + Math.round(c.getFitness());

				System.out.println(msg);	        

				break;
			}
		}
		
		for(int j=i, nSelected = 1; nSelected < nMax && j < populationDim; j++){
			c = population.getChromosome(j);
			int x = c.differentGenesAmongTwoChroms(solution.get(nSelected-1).getChromosome());
			if(!optChrom.contains(Math.round(c.getFitness())) &&
					//Math.round(c.getFitness()) != Math.round(solution.get(nSelected-1).getChromosome().getFitness()) ||
					 x >= 5){
					solution.add(nSelected, c.getSolution());
					optChrom.add(Math.round(c.getFitness()));
					nSelected++;
					String msg = nSelected+". Its fitness is: " + Math.round(c.getFitness())+" it has "+x+" genes in different position compared with "+(nSelected-1);
					System.out.println(msg);
			}
		}

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
	public double getAvgDeviationAmongChroms()
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

	void doGeneticMating(int iGen)
	{
		Chromosome[] result;
		Chromosome[][] selection = selectParents();
		
		int selectedCrossover = instance.getRandom().nextInt(3);
			
		switch(selectedCrossover){
			case 0: 
				result = crossover1pt(selection);
				break;

			case 1: 
				result = crossover2pt(selection);
				break;

			default: 
				result = pmxCrossover(selection);
		}
			
/////////////////////////////////
		/*
		for(int i = 0; i < result.length; i++){
			for(int j = 1; j <= chromosomeDim; j++){
				int count = 0;
				for(int k = 0; k < chromosomeDim; k++){
					if(result[i].getGene(k) == j) count++;
					if(count >= 2) {
						System.out.println("**************************************BUG FOUND!!!**********************************"+j);
						break;
					}
				}
				if(count >= 2) break;
			}
		}	
		*/
/////////////////////////////////
		
		generateNewPopulation(result);

	}

//	int getRandom(int upperBound)
//	{
//		int iRandom = (int) (Math.random() * upperBound);
//		return (iRandom);
//	}
	
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

	public void insertBestTabuSolutionIntoInitPopulation(Route[][] routes,
			int i, double alpha, double beta, double gamma) {
		
		Chromosome c;
		
		//build a chromosome from a route 
		c = new Chromosome(routes, chromosomeDim, instance, alpha, beta, gamma);
	
//		MyGASolution sol = new MyGASolution(c, instance);
//		c.setSolution(sol);
//		c.setFitness();
//		
		if(!population.isClone(c))
		{
			population.swapChromosome(c, i);
			//System.out.println("fitness chromosome inserito: " + c.getFitness());	
		}
		else
		{
			System.out.println("clone detected. skip it.");
		}
		/*
		for(Route r:routes[0])
			if(r.getCustomersLength() > 0)
				System.out.println(r.printRoute());
		
		c.getSolution().printConvertedRoutes();
		*/
	}


	public ArrayList<MySolution> getNDifferentBestSolutions2(int nBestSolution) {
		Chromosome c;
		ArrayList<Chromosome> pickedUpChrom;
		ArrayList<MyGASolution> solution;
		boolean tooSimilar;
		
		pickedUpChrom = new ArrayList<Chromosome>();
		solution = new ArrayList<MyGASolution>();

		population.sort();

		for ( int i = 0, pickedUp = 0; i < populationDim && pickedUp < nBestSolution; i++ ){
			c = population.getChromosome(i);
			if(!c.isAlreadyTabuImproved()){
				tooSimilar = false;
				for(Chromosome tmp : pickedUpChrom){

					if(c.differentGenesAmongTwoChroms(tmp) < 20){
						
						tooSimilar = true;
						break;
					}
				}
				if(!tooSimilar){
					String msg = (pickedUp+1)+". Its fitness is: " + Math.round(c.getFitness()) ;
					System.out.println(msg);
					c.setTabuImproved(true);
					solution.add(pickedUp, c.getSolution());
					pickedUpChrom.add(c);
					pickedUp++;
				}
			}
		}
		
		return new ArrayList<MySolution>(solution);
	}

	public void evolve2(MoveManager moveManager, 
			ObjectiveFunction objFunc, 
			TabuList tabuList, PrintStream outPrintSream,
			int tabuIteration, 
			Properties prop) {
		int iGen = 0;
		Chromosome[] result;

		do{
			result = doGeneticMating4(iGen);
			
			result = deleteDuplicates(result);
			
			for(int i = 0; i < result.length; i++){
				Chromosome c = result[i];
				MySearchProgram search;
				try {
					search = new MySearchProgram(instance, c.getSolution(), moveManager,
							objFunc, tabuList, false,  outPrintSream, prop);
					
					prop.setProperty("enableCheckImprovement", "false");
					search.tabuSearch.setIterationsToGo(tabuIteration);	// Set number of iterations
					search.tabuSearch.startSolving();
					
					if (search.feasibleCost.total != Double.POSITIVE_INFINITY)
						result[i] = new Chromosome(search.feasibleRoutes, chromosomeDim, instance, search.getSol().alpha, search.getSol().beta, search.getSol().gamma);
					
					prop.setProperty("enableCheckImprovement", "true");
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			result = deleteDuplicates(result);
			System.out.println("children after tabu: "+result.length);
			generateNewPopulation(result);
			population.detectClones();
			
			iGen++;
		}while(iGen < maxGenerations);
		
	}
	
	Chromosome[] sequencedBasedMutation(Chromosome[] children)
	{
		Chromosome[] result = new Chromosome[children.length];
		
		//do mutation		
		for(int i = 0; i < children.length; i++){
			Chromosome child1 = new Chromosome(children[i]);
			//Chromosome child2 = new Chromosome(children[i+1]);
			child1.evaluate(instance);
			//child2.evaluate(instance);
			
			int cuttingPoint1 = instance.getRandom().nextInt(chromosomeDim);
			
			int cuttingPoint2 = instance.getRandom().nextInt(chromosomeDim);

			int j = cuttingPoint1, k = cuttingPoint2;
			//copy customer untile we are on the same route
			do{
				int tmp = child1.getGene(j);
				child1.setGene(j, child1.getGene(k));	
				child1.setGene(k, tmp);
				k++;
				j++;
				
				if(k >= chromosomeDim || j >= chromosomeDim) break;
			}while( 
					child1.solution.labelP[k] == child1.solution.labelP[k-1]
					&& child1.solution.labelP[j] == child1.solution.labelP[j-1]);
			
			result[i] = child1;				
		}

		return result;
	}
	
	Chromosome[] doGeneticMating2(int iGen)
	{
		Chromosome[] result;
		Chromosome[][] selection = selectParents();
		
		
		int selectedCrossover = instance.getRandom().nextInt(4);
			
		switch(selectedCrossover){
			case 0: 
				result = crossover1pt(selection);
				break;

			case 1: 
				result = crossover2pt(selection);
				break;

			case 2:
				result = pmxCrossover(selection);
				break;
				
			case 3:
				System.out.println("heuristic crossover");
				result = heuristicCrossover(selection);
				break;
				
			default: 
				result = pmxCrossover(selection);
		}
		
		
		for(int i = 0; i < result.length; i++){
			Chromosome c = result[i];
			c.evaluate(instance);
			//System.out.println("fitness figlio: "+c.getFitness());
		}
		
		return result;
	}
	
	Chromosome[] doGeneticMating3(int iGen)
	{
		Chromosome[] result;
		Chromosome[][] selection = selectParents();
		
		result = new Chromosome[selection.length*2];
		
		for(int i = 0, k = 0; k < selection.length*2; k+=2, i++){
			Chromosome[] children;
			
			if(instance.getRandom().nextDouble() <= 0.8){
				//do crossover
				children = heuristicCrossover(selection[i]);
			}else{
				//do mutation
				children = sequencedBasedMutation(selection[i]);
			}
			result[k] = children[0];
			result[k+1] = children[1];
		}
			
		for(int i = 0; i < result.length; i++){
			Chromosome c = result[i];
			c.evaluate(instance);
			//System.out.println("fitness figlio: "+c.getFitness());
		}
		
		return result;
	}
	
	Chromosome[] doGeneticMating4(int iGen)
	{
		Chromosome[] result;
		Chromosome[][] selection = selectParents();
		
		result = new Chromosome[selection.length*2];
		
		for(int i = 0, k = 0; k < selection.length*2; k+=2, i++){
			Chromosome[] children;
			
			if(instance.getRandom().nextDouble() <= 0.8){
				//do crossover
				int selectedCrossover = instance.getRandom().nextInt(4);
				
				switch(selectedCrossover){
					case 0: 
						children = crossover1pt(selection[i]);
						break;

					case 1: 
						children = crossover2pt(selection[i]);
						break;

					case 2:
						children = pmxCrossover(selection[i]);
						break;
						
					case 3:
						children = heuristicCrossover(selection[i]);
						break;
						
					default: 
						children = pmxCrossover(selection[i]);
				}
			}else{
				//do mutation
				children = sequencedBasedMutation(selection[i]);
			}
			result[k] = children[0];
			result[k+1] = children[1];
		}
			
		for(int i = 0; i < result.length; i++){
			Chromosome c = result[i];
			c.evaluate(instance);
			//System.out.println("fitness figlio: "+c.getFitness());
		}
		
		return result;
	}

	private Chromosome[] pmxCrossover(Chromosome[] parents) {
		int childrenNum = parents.length;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"

		//calcolo dei tagli
		int firstCut = instance.getRandom().nextInt(chromosomeDim/2);
		int secondCut = instance.getRandom().nextInt(chromosomeDim/2);

		//System.out.println("chromosomeDim: "+chromosomeDim+" firstCut: "+firstCut+ " secondCut: "+secondCut);
		
		children[0] = new Chromosome(chromosomeDim);
		children[1] = new Chromosome(chromosomeDim);
		copyGenesInFrom(children[0], 0, chromosomeDim, parents[0], 0, chromosomeDim);
		copyGenesInFrom(children[1], 0, chromosomeDim, parents[1], 0, chromosomeDim);
		
		//children[0]//1 5 | 9 7 3 | 2 6 8
		//children[1]//9 8 | 7 1 2 | 3 6 5
		Chromosome tmp1 = new Chromosome(chromosomeDim);
		Chromosome tmp2 = new Chromosome(chromosomeDim);
		
		copyGenesInFrom(tmp1, firstCut, secondCut, children[0], firstCut, secondCut);
		copyGenesInFrom(tmp2, firstCut, secondCut, children[1], firstCut, secondCut);
		
		//tmp1//0 0 | 9 7 3 | 0 0 0
		//tmp2//0 0 | 7 1 2 | 0 0 0
		
		for(int i = firstCut; i < secondCut; i++){
			if(children[0].getGene(i) == tmp2.getGene(i)){
				tmp2.setGene(i, -1);
			}
			if(children[1].getGene(i) == tmp1.getGene(i)){
				tmp1.setGene(i, -1);
			}
		}
		
		//generate children[0]//9 5 7 1 2 3 6 8
		for(int i = firstCut; i < secondCut; i++){
			if(tmp2.getGene(i) == -1) continue;
			for(int j = 0; j < chromosomeDim; j++){
				if(tmp2.getGene(i) == children[0].getGene(j)){
					children[0].setGene(j, children[0].getGene(i));
					children[0].setGene(i, tmp2.getGene(i));
					tmp2.setGene(i, -1);
				}
			}
		}

		//generate children[1]//1 8 9 7 3 2 6 5
		for(int i = firstCut; i < secondCut; i++){
			if(tmp1.getGene(i) == -1) continue;
			for(int j = 0; j < chromosomeDim; j++){
				if(tmp1.getGene(i) == children[1].getGene(j)){
					children[1].setGene(j, children[1].getGene(i));
					children[1].setGene(i, tmp1.getGene(i));
					tmp1.setGene(i, -1);
				}
			}
		}
		
		return children;
	}

	private Chromosome[] crossover2pt(Chromosome[] parents) {
		int childrenNum = parents.length;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"

		//calcolo dei tagli
		int firstCut = instance.getRandom().nextInt(chromosomeDim/2);
		int secondCut = instance.getRandom().nextInt(chromosomeDim/2) + (chromosomeDim/2);

		//System.out.println("chromosomeDim: "+chromosomeDim+" firstCut: "+firstCut+ " secondCut: "+secondCut);


			for(int j = 0; j < 2; j++){ //j=0 genero primo figlio della "corrente" coppia, j=1 genero secondo figlio
				children[j] = new Chromosome(chromosomeDim);
				copyGenesInFrom(children[j], firstCut, secondCut, parents[j], firstCut, secondCut); //riempio la parte centrale del figlio1 con la parte centrale del genitore1

				//optimization thing!!! faccio un cromosoma con soltanto la parte centrale del figlio così 
				//ogni volta che devo controllare se il gene del genitore è possibile inserirlo risparmio molto in termini di numero di iterazioni
				int centralPartDim = (secondCut-firstCut);
				Chromosome centralPart = new Chromosome(centralPartDim);
				copyGenesInFrom(centralPart, 0, centralPartDim, children[j], firstCut, secondCut);

				int indexVal = secondCut; //indice relativo al figlio 
				int remainingVals = (chromosomeDim-centralPartDim); //valori rimanenti da inserire nel figlio
				int selectedParent = (j+1) % 2; //if j == 0 -> 1; if j == 1 -> 0
				int selectedGene = secondCut; //indice relativo al genitore
				//il cuore della generazione del figlio (sala parto :D)
				for(int z = 0; z < chromosomeDim; z++){
					//è possibile inserire il gene del genitore nel figlio?!?!?
					if(!geneIsPresent(parents[selectedParent].getGene(selectedGene), centralPart, centralPartDim)){ 
						children[j].setGene(indexVal, parents[selectedParent].getGene(selectedGene));
						indexVal = (indexVal+1) % chromosomeDim;
						remainingVals --;
						if(remainingVals == 0) break; //ho inserito l'ultimo gene nel figlio (è natoooooo :D)
					}
					selectedGene = (selectedGene+1) % chromosomeDim;
				}
			}


		return children;
	}

	private Chromosome[] crossover1pt(Chromosome[] parents) {
		int childrenNum = parents.length;
		Chromosome[] children = new Chromosome[childrenNum]; //creo un array di cromosomi di dimensione al max il doppio dei "genitori"

		//calcolo del taglio
		int cut = instance.getRandom().nextInt(chromosomeDim);



			for(int j = 0; j < 2; j++){ //j=0 genero primo figlio della "corrente" coppia, j=1 genero secondo figlio
				children[j] = new Chromosome(chromosomeDim);
				copyGenesInFrom(children[j], 0, cut, parents[j], 0, cut); //riempio la parte iniziale del figlio1 con la parte iniziale del genitore1

				//optimization thing!!! faccio un cromosoma con soltanto la parte iniziale del figlio così 
				//ogni volta che devo controllare se il gene del genitore è possibile inserirlo risparmio molto in termini di numero di iterazioni
				Chromosome initialPart = new Chromosome(cut);
				copyGenesInFrom(initialPart, 0, cut, children[j], 0, cut);

				int indexVal = cut; //indice relativo al figlio 
				int remainingVals = (chromosomeDim-cut); //valori rimanenti da inserire nel figlio
				int selectedParent = (j+1) % 2; //if j == 0 -> 1; if j == 1 -> 0
				int selectedGene = cut; //indice relativo al genitore
				//il cuore della generazione del figlio (sala parto :D)
				for(int z = 0; z < chromosomeDim; z++){
					//è possibile inserire il gene del genitore nel figlio?!?!?
					if(!geneIsPresent(parents[selectedParent].getGene(selectedGene), initialPart, cut)){ 
						children[j].setGene(indexVal, parents[selectedParent].getGene(selectedGene));
						indexVal = (indexVal+1) % chromosomeDim;
						remainingVals --;
						if(remainingVals == 0) break; //ho inserito l'ultimo gene nel figlio (è natoooooo :D)
					}
					selectedGene = (selectedGene+1) % chromosomeDim;
				}
			}
		

		return children;
	}
}
