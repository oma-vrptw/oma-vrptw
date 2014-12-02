package com.MyGeneticA;

import com.mdvrp.Instance;

public class Population {
	private Chromosome[] chromosomes;
	private Instance instance;
	
	Population(int populationDim, Instance instance) { 
		chromosomes = new Chromosome[populationDim];
		this.instance = instance;
	}
	
	void setChromosome(int index, Chromosome c) { 
		chromosomes[index] = c;
	}
	
	Chromosome getChormosome(int index) { return chromosomes[index]; }
	
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
	
	int getBestChromosome() { 
		double bestFitness = Integer.MAX_VALUE;
		int idBestChrom = 0;
		
		for(int i = 0; i < chromosomes.length; i++){
			System.out.println("Fitness[i]: "+getFitness(i));
			if(getFitness(i) < bestFitness) {
				bestFitness = getFitness(i);
				idBestChrom = i;
			}
		}
		
		return idBestChrom;
	}
}
