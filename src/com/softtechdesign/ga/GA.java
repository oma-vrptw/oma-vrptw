package com.softtechdesign.ga;
import java.util.*;

/**
 * <pre>
 * Package ga
 * --------------------------------------------------------------------------------------------
 * The GAFloat, GAString, and GASequenceList classes all extend the GA class and can be used
 * to model different populations of candidate solutions. You will generally have to extend
 * one of these classes every time you create a new GA. In the simplest cases, you will subclass
 * one of these classes and then just override and implement your own GetFitness() function. The
 * three main subclasses of GA are:
 *   GAString (chromosomes are stored as strings)
 *   GAFloat (chromosomes are stored as floating point numbers)
 *   GASequenceList (chromosomes are stored as strings, additional methods in this class handle
 *                   sorting sequences. For example, the GASalesman class extends GASequenceList)
 *
 * For example:
 *   If your chromosomes are floating point numbers, you should subclass TGAFloat and override
 *   the getFitness() function with your own.
 *
 *   If your chromosomes are strings, you should subclass TGAString and override the
 *   getFitness() function with your own.
 *
 *   If your chromosomes are characters in a sequence (or list) that needs to be rearranged, you
 *   should use TGASequenceList and override the getFitness() function with your own.
 * ---------------------------------------------------------------------------------------------
 * 
 *  This main abstract class is extended by the 3 main GA subclasses:
 *  GAString, GAFloat, GASequenceList
 *  It (obviously) contains the methods common to all GA subclasses
 * </pre>
 * @author Jeff Smith jeff@SoftTechDesign.com
 */
public abstract class GA implements Runnable
{
    /** probability of a mutation occuring during genetic mating. For example, 0.03 means 3% chance */
    double mutationProb; 

    /** maximum generations to evolve */
    int maxGenerations; 

    /** number of prelim generations to evolve. Set to zero to disable */
    int numPrelimRuns; 

    /** prelim generations. Prelim runs are useful for building fitter "starting" chromosome stock before the main evolution run. */
    int maxPrelimGenerations; //maximum prelim generations to evolve

    /** 1-100 (e.g. 10 = 10% chance of random selection--not based on fitness). 
     * Setting nonzero randomSelectionChance helps maintain genetic diversity during evolution
     */ 
    int randomSelectionChance; 

    /** probability that a crossover will occur during genetic mating */
    double crossoverProb; 
    
    /** dimension of chromosome (number of genes) */
    protected int chromosomeDim; 
    
    /** number of chromosomes to evolve. A larger population dim will result in a better evolution but will slow the process down */
    protected int populationDim; 
    
    /** storage for pool of chromosomes for current generation */
    Chromosome[] chromosomes; 
    
    /** storage for temporary holding pool for next generation chromosomes */
    Chromosome[] chromNextGen; 
    
    /** storage for pool of prelim generation chromosomes */
    Chromosome[] prelimChrom; 
    
    /** index of fittest chromosome in current generation */
    int bestFitnessChromIndex; 
    
    /** index of least fit chromosome in current generation */
    int worstFitnessChromIndex; 
    
    /** type of crossover to be employed during genetic mating */
    protected int crossoverType; 
    
    /** statistics--average deviation of current generation */
    double[] genAvgDeviation; 
    
    /** statistics--average fitness of current generation */
    double[] genAvgFitness; 
    
    /** compute statistics for each generation during evolution? */ 
    boolean computeStatistics; 

    /** initialize the population (chromosomes) to random values */
    abstract protected void initPopulation();
    
    /** do a random mutation on given chromosome */
    abstract protected void doRandomMutation(int iChromIndex);
    
    /** do one point crossover between the two given chromosomes */
    abstract protected void doOnePtCrossover(Chromosome Chrom1, Chromosome Chrom2);
    
    /** do two point crossover between the two given chromosomes */
    abstract protected void doTwoPtCrossover(Chromosome Chrom1, Chromosome Chrom2);
    
    /** do uniform crossover between the two given chromosomes */
    abstract protected void doUniformCrossover(Chromosome Chrom1, Chromosome Chrom2);
    
    /** get the fitness value for the given chromosome */
    abstract protected double getFitness(int iChromIndex);

    /**
     * Runs the evolution by calling evolve() routine
     */
    public void run()
    {
        evolve();
    }

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
     */
    public GA(int chromosomeDim,
              int populationDim,
              double crossoverProb,
              int randomSelectionChance,
              int maxGenerations,
              int numPrelimRuns,
              int maxPrelimGenerations,
              double mutationProb,
              int crossoverType,
              boolean computeStatistics)
    {
        this.randomSelectionChance = randomSelectionChance;
        this.crossoverType = crossoverType;
        this.chromosomeDim = chromosomeDim;
        this.populationDim = populationDim;
        this.computeStatistics = computeStatistics;

        this.chromosomes = new Chromosome[populationDim];
        this.chromNextGen = new Chromosome[populationDim];
        this.prelimChrom = new Chromosome[populationDim];
        this.genAvgDeviation = new double[maxGenerations];
        this.genAvgFitness = new double[maxGenerations];

        this.crossoverProb = crossoverProb;
        this.maxGenerations = maxGenerations;
        this.numPrelimRuns = numPrelimRuns;
        this.maxPrelimGenerations = maxPrelimGenerations;
        this.mutationProb = mutationProb;
    }

    GA(){}
    
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

    /**
     * Returns the mutation probability
     * @return double
     */
    public double getMutationProb()
    {
        return mutationProb;
    }

    /**
     * Gets the maximum number of generations this evolution will evolve 
     * @return int
     */
    public int getMaxGenerations()
    {
        return maxGenerations;
    }

    /**
     * Gets the number of preliminary runs that will be performed before the main
     * evolution begins
     * @return int
     */
    public int getNumPrelimRuns()
    {
        return numPrelimRuns;
    }

    /**
     * Gets the maximum number of preliminary generations to evolve
     * @return int
     */
    public int getMaxPrelimGenerations()
    {
        return maxPrelimGenerations;
    }

    /**
     * Gets the random selection probability
     * @return int
     */
    public int getRandomSelectionChance()
    {
        return randomSelectionChance;
    }

    /**
     * Gets the crossover probability 
     * @return double
     */
    public double getCrossoverProb()
    {
        return crossoverProb;
    }

    /**
     * Gets the dimension (size or number) of genes per chromosome
     * @return int
     */
    public int getChromosomeDim()
    {
        return chromosomeDim;
    }

    /**
     * Gets the dimension (size or number) of chromosomes in the population
     * @return int
     */
    public int getPopulationDim()
    {
        return populationDim;
    }

    /**
     * Gets the crossover type (e.g. one point, two point, uniform, roulette)
     * @return
     */
    public int getCrossoverType()
    {
        return crossoverType;
    }

    /**
     * Returns whether statistics will be computed for this evolution run
     * @return boolean
     */
    public boolean getComputeStatistics()
    {
        return computeStatistics;
    }

    /**
     * Returns the fittest chromosome in the population
     * @return Chromosome
     */
    public Chromosome getFittestChromosome()
    {
        return (this.chromosomes[bestFitnessChromIndex]);
    }

    /**
     * Gets the fitness value of the fittest chromosome in the population 
     * @return double
     */
    public double getFittestChromosomesFitness()
    {
        return (this.chromosomes[bestFitnessChromIndex].fitness);
    }

    /**
     * return a integer random number between 0 and upperBound
     * @param upperBound
     * @return int
     */
    int getRandom(int upperBound)
    {
        int iRandom = (int) (Math.random() * upperBound);
        return (iRandom);
    }

    /**
     * return a double random number between 0 and upperBound
     * @param upperBound
     * @return double
     */
    double getRandom(double upperBound)
    {
        double dRandom = (Math.random() * upperBound);
        return (dRandom);
    }

    /**
     * Main routine that runs the evolution simulation for this population of chromosomes.  
     * @return number of generations
     */
    public int evolve()
    {
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
        addChromosomesToLog(0, 10);

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

    /**
     * Go through all chromosomes and calculate the average fitness (of this generation)
     * @return double
     */
    public double getAvgFitness()
    {
        double rSumFitness = 0.0;

        for (int i = 0; i < populationDim; i++)
            rSumFitness += this.chromosomes[i].fitness;
        return (rSumFitness / populationDim);
    }

    /**
     * Select two parents from population, giving highly fit individuals a greater chance of 
     * being selected.
     * @param indexParents
     */
    public void selectTwoParents(int[] indexParents)
    {
        int indexParent1 = indexParents[0];
        int indexParent2 = indexParents[1];
        boolean bFound = false;
        int index;

        while (bFound == false)
        {
            index = getRandom(populationDim); //get random member of population

            if (randomSelectionChance > getRandom(100))
            {
                indexParent1 = index;
                bFound = true;
            }
            else
            {
                //the greater a chromosome's fitness rank, the higher prob that it will be
                //selected to reproduce
                if (this.chromosomes[index].fitnessRank + 1 > getRandom(populationDim))
                {
                    indexParent1 = index;
                    bFound = true;
                }
            }
        }

        bFound = false;
        while (bFound == false)
        {
            index = getRandom(populationDim); //get random member of population

            if (randomSelectionChance > getRandom(100))
            {
                if (index != indexParent1)
                {
                    indexParent2 = index;
                    bFound = true;
                }
            }
            else
            {
                //the greater a chromosome's fitness rank, the higher prob that it will be
                //selected to reproduce
                if ((index != indexParent1)
                    && (this.chromosomes[index].fitnessRank + 1 > getRandom(populationDim)))
                {
                    //          if (this.chromosomes[index].getNumGenesInCommon(this.chromosomes[indexParent1])+1 > getRandom(chromosomeDim))
                    //          {
                    //            indexParent2 = index;
                    //            bFound = true;
                    //          }
                    indexParent2 = index;
                    bFound = true;
                }
            }
        }

        indexParents[0] = indexParent1;
        indexParents[1] = indexParent2;
    }

    /**
     * Calculate the ranking of the parameter "fitness" with respect to the current generation.
     * If the fitness is high, the corresponding fitness ranking will be high, too.
     * For example, if the fitness passed in is higher than any fitness value for any chromosome in the
     * current generation, the fitnessRank will equal the populationDim. And if the fitness is lower than
     * any fitness value for any chromosome in the current generation, the fitnessRank will equal zero.
     * @param fitness
     * @return int the fitness ranking
     */
    int getFitnessRank(double fitness)
    {
        int fitnessRank = -1;
        for (int i = 0; i < populationDim; i++)
        {
            if (fitness >= this.chromosomes[i].fitness)
                fitnessRank++;
        }

        return (fitnessRank);
    }

    /**
     * Calculate rankings for all chromosomes. High ranking numbers denote very fit chromosomes.
     */
    void computeFitnessRankings()
    {
        double rValue;

        // recalc the fitness of each chromosome
        /*for (int i = 0; i < populationDim; i++)
            this.chromosomes[i].fitness = getFitness(i);*/

        for (int i = 0; i < populationDim; i++)
            this.chromosomes[i].fitnessRank = getFitnessRank(this.chromosomes[i].fitness);

        double rBestFitnessVal;
        double rWorstFitnessVal;
        for (int i = 0; i < populationDim; i++)
        {
            if (this.chromosomes[i].fitnessRank == populationDim - 1)
            {
                rBestFitnessVal = this.chromosomes[i].fitness;
                this.bestFitnessChromIndex = i;
            }
            if (this.chromosomes[i].fitnessRank == 0)
            {
                rWorstFitnessVal = this.chromosomes[i].fitness;
                this.worstFitnessChromIndex = i;
            }
        }
    }

    /**
     * Create the next generation of chromosomes by genetically mating fitter individuals of the
     * current generation.
     * Also employ elitism (so the fittest 2 chromosomes always survive to the next generation). 
     * This way an extremely fit chromosome is never lost from our chromosome pool.
     */
    void doGeneticMating()
    {
        int iCnt, iRandom;
        int indexParent1 = -1, indexParent2 = -1;
        Chromosome Chrom1, Chrom2;

        iCnt = 0;

        //Elitism--fittest chromosome automatically go on to next gen (in 2 offspring)
        this.chromNextGen[iCnt].copyChromGenes(this.chromosomes[this.bestFitnessChromIndex]);
        iCnt++;
        this.chromNextGen[iCnt].copyChromGenes(this.chromosomes[this.bestFitnessChromIndex]);
        iCnt++;

        if (this instanceof GAString)
        {
            Chrom1 = new ChromChars(chromosomeDim);
            Chrom2 = new ChromChars(chromosomeDim);
        }
        else if (this instanceof GAFloat)
        {
            Chrom1 = new ChromFloat(chromosomeDim);
            Chrom2 = new ChromFloat(chromosomeDim);
        }
        else //must be GASeq
            {
            Chrom1 = new ChromStrings(chromosomeDim);
            Chrom2 = new ChromStrings(chromosomeDim);
        }

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

                this.chromNextGen[iCnt].copyChromGenes(Chrom1);
                iCnt++;
                this.chromNextGen[iCnt].copyChromGenes(Chrom2);
                iCnt++;
            }
            else //if no crossover, then copy this parent chromosome "as is" into the offspring
                {
                // CREATE OFFSPRING ONE
                this.chromNextGen[iCnt].copyChromGenes(Chrom1);
                iCnt++;

                // CREATE OFFSPRING TWO
                this.chromNextGen[iCnt].copyChromGenes(Chrom2);
                iCnt++;
            }
        }
        while (iCnt < populationDim);
    }

    /**
     * Copy the chromosomes previously created and stored in the "next" generation into the main
     * chromsosome memory pool. Perform random mutations where appropriate.
     */
    void copyNextGenToThisGen()
    {
        for (int i = 0; i < populationDim; i++)
        {
            //this.chromosomes[i].copyChromGenes(this.chromNextGen[i]);
        	this.chromosomes[i] = this.chromNextGen[i];

            //only mutate chromosomes if it is NOT the best
            if (i != this.bestFitnessChromIndex)
            {
                //always mutate the chromosome with the lowest fitness
                if ((i == this.worstFitnessChromIndex) || (getRandom(1.0) < mutationProb))
                    doRandomMutation(i);
            }
        }
    }

    /**
     * Display chromosome information to System.out
     * @param iGeneration
     * @param iNumChromosomesToDisplay
     */
    void addChromosomesToLog(int iGeneration, int iNumChromosomesToDisplay)
    {
        String sGen, sChrom;

        if (iNumChromosomesToDisplay > this.populationDim)
            iNumChromosomesToDisplay = this.chromosomeDim;

        //Display Chromosomes
        for (int i = 0; i < iNumChromosomesToDisplay; i++)
        {
            this.chromosomes[i].fitness = getFitness(i);
            sGen = "" + iGeneration;
            if (sGen.length() < 2)
                sGen = sGen + " ";
            sChrom = "" + i;
            if (sChrom.length() < 2)
                sChrom = sChrom + " ";
            System.out.println("Gen " + sGen + ": Chrom" + sChrom + " = " + 
                               this.chromosomes[i].getGenesAsStr() + ", fitness = " + 
                               this.chromosomes[i].fitness);
        }
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
            if (this instanceof GAString)
            {
                char bestFitGene = ((ChromChars)this.chromosomes[this.bestFitnessChromIndex]).getGene(iGene);
                for (int i = 0; i < this.populationDim; i++)
                {
                    char thisGene = ((ChromChars)this.chromosomes[i]).getGene(iGene);
                    if (thisGene != bestFitGene)
                        devCnt++;
                }
            }
            else if (this instanceof GAFloat)
            {
                double bestFitGene =
                    ((ChromFloat)this.chromosomes[this.bestFitnessChromIndex]).getGene(iGene);
                for (int i = 0; i < populationDim; i++)
                {
                    double thisGene = ((ChromFloat)this.chromosomes[i]).getGene(iGene);
                    if (thisGene != bestFitGene)
                        devCnt++;
                }
            }
            else //GAStringsSeq
                {
                String bestFitGene =
                    ((ChromStrings)this.chromosomes[this.bestFitnessChromIndex]).getGene(iGene);
                for (int i = 0; i < this.populationDim; i++)
                {
                    String thisGene = ((ChromStrings)this.chromosomes[i]).getGene(iGene);
                    if (thisGene.equals(bestFitGene) == false)
                        devCnt++;
                }
            }
        }

        return ((double)devCnt);
    }

    /**
     * Take a binary string and convert it to the long integer. For example, '1101' --> 13
     * @param sBinary
     * @return long
     */
    long binaryStrToInt(String sBinary)
    {
        long digit, iResult = 0;

        int iLen = sBinary.length();
        for (int i = iLen - 1; i >= 0; i--)
        {
            if (sBinary.charAt(i) == '1')
                digit = 1;
            else
                digit = 0;
            iResult += (digit << (iLen - i - 1));
        }
        return (iResult);
    }
}
