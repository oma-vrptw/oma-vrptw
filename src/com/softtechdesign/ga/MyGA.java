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
        //addChromosomesToLog(0, populationDim);

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

        //addChromosomesToLog(iGen, 10); //display Chromosomes to system.out
        printPopulation();
        
        computeFitnessRankings();
        System.out.println("Best Chromosome Found: ");
        System.out.println(this.chromosomes[this.bestFitnessChromIndex] +
                           " Fitness= " + this.chromosomes[this.bestFitnessChromIndex].fitness);

        System.out.println("GA end time: " + new Date().toString());
        return (iGen);
	}
	
	@Override
	void doGeneticMating()
    {
        int iCnt, iRandom;
        int indexParent1 = -1, indexParent2 = -1;
        ChromInt Chrom1, Chrom2;

        iCnt = 0;
        
        //System.out.println("this.bestFitnessChromIndex: "+this.bestFitnessChromIndex);
        //System.out.println("Chromosome[0]: "+chromosomes[0]);
        //System.out.println("ChromNextGen[0]: "+chromNextGen[0]);
        //System.out.println("ChromNextGen[0]: "+chromNextGen[0]);
        
        //Elitism--fittest chromosome automatically go on to next gen (in 2 offspring)
        //this.chromNextGen[iCnt].copyChromGenes(this.chromosomes[this.bestFitnessChromIndex]);
        this.chromNextGen[iCnt] = this.chromosomes[this.bestFitnessChromIndex];
        this.chromosomes[this.bestFitnessChromIndex].fitness = 0.0;
        //System.out.println("Old this.bestFitnessChromIndex: "+this.bestFitnessChromIndex);
        printPopulation();
        computeFitnessRankings();
        printPopulation();
        //System.out.println("New this.bestFitnessChromIndex: "+this.bestFitnessChromIndex);
        iCnt++;
        //this.chromNextGen[iCnt].copyChromGenes(this.chromosomes[this.bestFitnessChromIndex]);
        this.chromNextGen[iCnt] = this.chromosomes[this.bestFitnessChromIndex];
        iCnt++;

        Chrom1 = new ChromInt(chromosomeDim);
        Chrom2 = new ChromInt(chromosomeDim);

        do
        {
            int indexes[] = { indexParent1, indexParent2 };
            selectTwoParents(indexes);
            indexParent1 = indexes[0];
            indexParent2 = indexes[1];

            Chrom1.copyChromGenes(this.chromosomes[indexParent1]);
            Chrom2.copyChromGenes(this.chromosomes[indexParent2]);

            if (getRandom(1.0) < crossoverProb) //do crossover
            {
                if (this.crossoverType == Crossover.ctOnePoint)
                    doOnePtCrossover(Chrom1, Chrom2);
                else if (this.crossoverType == Crossover.ctTwoPoint)
                    doTwoPtCrossover(Chrom1, Chrom2);
                else if (this.crossoverType == Crossover.ctUniform)
                    doUniformCrossover(Chrom1, Chrom2);
                else if (this.crossoverType == Crossover.ctRoulette)
                {
                    iRandom = getRandom(3);
                    if (iRandom < 1)
                        doOnePtCrossover(Chrom1, Chrom2);
                    else if (iRandom < 2)
                        doTwoPtCrossover(Chrom1, Chrom2);
                    else
                        doUniformCrossover(Chrom1, Chrom2);
                }
                //System.out.println("ChromNextGen["+iCnt+"]: "+chromNextGen[iCnt]);
                //System.out.println("Chrom1: "+Chrom1);
                //System.out.println("ChromNextGen["+iCnt+1+"]: "+chromNextGen[iCnt+1]);
                //System.out.println("Chrom2: "+Chrom2);
                
                //this.chromNextGen[iCnt].copyChromGenes(Chrom1);
                this.chromNextGen[iCnt] = Chrom1;
                iCnt++;
                //this.chromNextGen[iCnt].copyChromGenes(Chrom2);
                this.chromNextGen[iCnt] = Chrom2;
                iCnt++;
            }
            else //if no crossover, then copy this parent chromosome "as is" into the offspring
                {
                // CREATE OFFSPRING ONE
                //this.chromNextGen[iCnt].copyChromGenes(Chrom1);
            	this.chromNextGen[iCnt] = Chrom1;
                iCnt++;

                // CREATE OFFSPRING TWO
                //this.chromNextGen[iCnt].copyChromGenes(Chrom2);
                this.chromNextGen[iCnt] = Chrom2;
                iCnt++;
            }
        }
        while (iCnt < populationDim);
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
		
		return iChromIndex+10;
	}
	
	public void printPopulation()
	{
		for(int i=0; i<populationDim; i++)
		{
			System.out.println("Chom"+i+": "+chromosomes[i].toString()+" Fitness: "+chromosomes[i].fitness);
		}
	}
}
