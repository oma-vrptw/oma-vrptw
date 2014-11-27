/**
 * 
 */
package com.softtechdesign.ga;
import com.mdvrp.*;
/**
 * @author Alessio Viola
 *
 */
public class Population extends GA {

	private Instance instance;
	
	public Population(int chromosomeDim, 
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

		initPopulation();
	}

	// Itialize all the chromosomes
	@Override
	protected void initPopulation() {

		for (int i = 0; i < populationDim; i++)
        {
			
			ChromInt c = new ChromInt(chromosomeDim);
			
            for (int iGene = 0; iGene < chromosomeDim; iGene++)
            {
            	/*
            	 *  Ad ogni gene viene assegnato un valore randomico
            	 *  compreso tra -1 e CustomerNr-1, dove
            	 *  -1 è il delimitatore delle route
            	 *  l'intervallo [0,CustomerNr-1] include la posizione di ciascun
            	 *  customer nel suo vettore/lista
            	 */
            	c.setGene(iGene, getRandom(instance.getCustomersNr()+1) - 1);
            }
            
            chromosomes[i] = c;
            
            //TODO: definire una fitness
            chromosomes[i].fitness = getFitness(i);
        }

	}

	/* (non-Javadoc)
	 * @see com.softtechdesign.ga.GA#doRandomMutation(int)
	 */
	@Override
	protected void doRandomMutation(int iChromIndex) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.softtechdesign.ga.GA#doOnePtCrossover(com.softtechdesign.ga.Chromosome, com.softtechdesign.ga.Chromosome)
	 */
	@Override
	protected void doOnePtCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.softtechdesign.ga.GA#doTwoPtCrossover(com.softtechdesign.ga.Chromosome, com.softtechdesign.ga.Chromosome)
	 */
	@Override
	protected void doTwoPtCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.softtechdesign.ga.GA#doUniformCrossover(com.softtechdesign.ga.Chromosome, com.softtechdesign.ga.Chromosome)
	 */
	@Override
	protected void doUniformCrossover(Chromosome Chrom1, Chromosome Chrom2) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see com.softtechdesign.ga.GA#getFitness(int)
	 */
	@Override
	protected double getFitness(int iChromIndex) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	public void printPopulation()
	{
		for(int i=0; i<populationDim; i++)
		{
			System.out.println(chromosomes[i].toString());
		}
	}

}
