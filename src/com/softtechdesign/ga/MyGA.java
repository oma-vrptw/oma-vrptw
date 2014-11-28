package com.softtechdesign.ga;

import java.util.Date;
import java.util.Random;

import com.mdvrp.*;

public class MyGA extends GA{
	
	private Instance instance;
	private int numOfRoutes;
	private int numOfCustomer;
	
	/**
     * Initializes the GA using given parameters
     * @param chromosomeDim
     * @param populationDim
     * @param crossoverProb
     * @param randomSelectionChance
     * @param maxGenerations
     * @param numPrelimRuns
     * @param maxPrelimGenerations
     * @param mutationProb
     * @param crossoverType
     * @param computeStatistics
	 * @param instance 
     */
    public MyGA(int chromosomeDim,
              int populationDim,
              double crossoverProb,
              int randomSelectionChance,
              int maxGenerations,
              int numPrelimRuns,
              int maxPrelimGenerations,
              double mutationProb,
              int crossoverType,
              boolean computeStatistics, 
              Instance instance)
    {
    	super(chromosomeDim, 
				populationDim, 
				crossoverProb, 
				randomSelectionChance,
				maxGenerations, 
				numPrelimRuns, 
				maxPrelimGenerations, 
				mutationProb,
				crossoverType, 
				computeStatistics);
		
		this.instance = instance;
		this.numOfRoutes = instance.getVehiclesNr();
		this.numOfCustomer = instance.getCustomersNr();
		
		initPopulation();
    }

	@Override
	protected void initPopulation() {
		for (int i = 0; i < populationDim; i++)
        {
			ChromInt c = new ChromInt(chromosomeDim);
			
			boolean [] usedCustomer = new boolean[numOfCustomer+1];
			
			for(int j = 0; j < numOfCustomer+1; j++) usedCustomer[j] = false;
			
			int routeCapacity = 0;
			int usedRoutes = 0;
			
            for (int iGene = 0; iGene < chromosomeDim; iGene++)
            {
            	boolean endOfRoute = false;
            	
            	for(int j = 0; j < numOfCustomer; j++){
            		Random random = new Random();
	            	int customerRandom = random.nextInt(numOfCustomer);
	            	if((usedCustomer[customerRandom] == false) && (usedRoutes >= (numOfRoutes-1) || routeCapacity+(int)instance.getCapacity(customerRandom) <= 200)){
	            		c.setGene(iGene, customerRandom+1);
	            		endOfRoute = false;
	            		routeCapacity = routeCapacity + (int)instance.getCapacity(customerRandom);
	            		usedCustomer[customerRandom] = true;
	            		break;
	            	}
	            	endOfRoute = true;
            	}
            	
            	if(endOfRoute == true){
            		c.setGene(iGene, -1);
            		usedRoutes++;
            		routeCapacity = 0;
            	}
            	
            }
            
            chromosomes[i] = c;
            
            //TODO: definire una fitness
            chromosomes[i].fitness = getFitness(i);
        }
		
	}

	@Override
	public int evolve(){
		int iGen;
        int iPrelimChrom, iPrelimChromToUsePerRun;

        System.out.println("GA start time: " + new Date().toString());

        if (numPrelimRuns > 0)
        {
            iPrelimChrom = 0;
            //number of fittest prelim chromosomes to use with final run
            iPrelimChromToUsePerRun = populationDim / numPrelimRuns;

            for (int iPrelimRuns = 1; iPrelimRuns <= numPrelimRuns; iPrelimRuns++)
            {
                iGen = 0;
                initPopulation();

                //create a somewhat fit chromosome population for this prelim run
                while (iGen < maxPrelimGenerations)
                {
                    System.out.println(iPrelimRuns + " of " + numPrelimRuns + " prelim runs --> " +
                                       (iGen + 1) + " of " + maxPrelimGenerations + " generations");

                    computeFitnessRankings();
                    doGeneticMating();
                    copyNextGenToThisGen();

                    if (computeStatistics == true)
                    {
                        this.genAvgDeviation[iGen] = getAvgDeviationAmongChroms();
                        this.genAvgFitness[iGen] = getAvgFitness();
                    }
                    iGen++;
                }

                computeFitnessRankings();

                //copy these somewhat fit chromosomes to the main chromosome pool
                int iNumPrelimSaved = 0;
                for (int i = 0; i < populationDim && iNumPrelimSaved < iPrelimChromToUsePerRun; i++)
                    if (this.chromosomes[i].fitnessRank >= populationDim - iPrelimChromToUsePerRun)
                    {
                        this.prelimChrom[iPrelimChrom + iNumPrelimSaved].copyChromGenes(this.chromosomes[i]);
                        //store (remember) these fit chroms
                        iNumPrelimSaved++;
                    }
                iPrelimChrom += iNumPrelimSaved;
            }
            for (int i = 0; i < iPrelimChrom; i++)
                this.chromosomes[i].copyChromGenes(this.prelimChrom[i]);
            System.out.println("INITIAL POPULATION AFTER PRELIM RUNS:");
        }
        else
            System.out.println("INITIAL POPULATION (NO PRELIM RUNS):");

        //Add Preliminary Chromosomes to list box
        addChromosomesToLog(0, populationDim);

        iGen = 0;
        while (iGen < maxGenerations)
        {
            computeFitnessRankings();
            doGeneticMating();
            copyNextGenToThisGen();

            if (computeStatistics == true)
            {
                this.genAvgDeviation[iGen] = getAvgDeviationAmongChroms();
                this.genAvgFitness[iGen] = getAvgFitness();
            }

            iGen++;
        }

        System.out.println("GEN " + (iGen + 1) + " AVG FITNESS = " + this.genAvgFitness[iGen-1] +
                           " AVG DEV = " + this.genAvgDeviation[iGen-1]);

        addChromosomesToLog(iGen, 10); //display Chromosomes to system.out

        computeFitnessRankings();
        System.out.println("Best Chromosome Found: ");
        System.out.println(this.chromosomes[this.bestFitnessChromIndex].getGenesAsStr() +
                           " Fitness= " + this.chromosomes[this.bestFitnessChromIndex].fitness);

        System.out.println("GA end time: " + new Date().toString());
        return (iGen);
	}
	
	@Override
	protected void doRandomMutation(int iChromIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doOnePtCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doTwoPtCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void doUniformCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected double getFitness(int iChromIndex) {
		// TODO Auto-generated method stub
		
		return getRandom(100);
	}
	
	public void printPopulation()
	{
		for(int i=0; i<populationDim; i++)
		{
			System.out.println(chromosomes[i].toString());
		}
	}
}
