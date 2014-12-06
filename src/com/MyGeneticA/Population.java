package com.MyGeneticA;

import com.mdvrp.Instance;

public class Population {
	private Chromosome[] chromosomes;
	private Instance instance;
	private int dim;
	
	Population(int populationDim, Instance instance) { 
		chromosomes = new Chromosome[populationDim];
		this.instance = instance;
		this.dim = populationDim;
	}
	
	void setChromosome(int index, Chromosome c) { 
		chromosomes[index] = c;
		MyGASolution sol = new MyGASolution(c, instance);
		c.setSolution(sol);
		c.setFitness();
	}
	
	Chromosome getChromosome(int index) { 
		return chromosomes[index]; 
	}
	
	double getFitness(int index) { 
		MyGASolution mgas = new MyGASolution(chromosomes[index], instance);
		return mgas.getFitness();
	}
	
	void printPopulation() { 
		for(int i = 0; i < chromosomes.length; i++){
			System.out.print("Chromosome["+i+"]: ");
			chromosomes[i].print();
			System.out.println();
		}
	}
	
	public Chromosome getBestChromosome() {
		Chromosome best, c;
		int j;
		
		for(j=0; getChromosome(j) == null; j++);
		
		best= getChromosome(j);
		
		for(int i = 1; i < dim; i++){
			c = getChromosome(i);
			if(c != null){
				if(c.compareTo(best) == -1)
					best = c;
			}
		}
		
		return best;
	}
	
	public int getBestChromosomeIndex() {
		Chromosome best, c;
		int bestIndex, j;
		
		for(j=0; getChromosome(j) == null; j++);
		
		bestIndex = j;
		best = getChromosome(j);
				
		for(int i = 1; i < dim; i++){
			c = getChromosome(i);
			if(c != null){
				if(c.compareTo(best) == -1){
					best = c;
					bestIndex = i;
				}
			}
		}
		
		return bestIndex;
	}

	public void removeChromosome(int iDbestChr) {
		// TODO Auto-generated method stub
		chromosomes[iDbestChr] = null;
	}
	
	public boolean chromosomeIsValid(int index){
		return chromosomes[index] != null;
	}
}
