package com.MyGeneticA;

public class Population {
	private Chromosome[] chromosomes;
	
	Population(int populationDim) { 
		chromosomes = new Chromosome[populationDim];
	}
	
	void setChromosome(int index, Chromosome c) { 
		chromosomes[index] = c;
	}
	
	Chromosome getChormosome(int index) { return chromosomes[index]; }
	
	double getFitness(int index) { return 0; }
	
	void printPopulation() { 
		for(int i = 0; i < chromosomes.length; i++){
			System.out.print("Chromosome["+i+"]: ");
			chromosomes[i].print();
			System.out.println();
		}
	}
	
	int getBestChromosome() { 
		double bestFitness = 0;
		int idBestChrom = 0;
		
		for(int i = 0; i < chromosomes.length; i++){
			if(getFitness(i) > bestFitness) {
				bestFitness = getFitness(i);
				idBestChrom = i;
			}
		}
		
		return idBestChrom;
	}
}
