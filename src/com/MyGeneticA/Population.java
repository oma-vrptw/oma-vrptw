package com.MyGeneticA;

public class Population {
	private Chromosome[] chromosomes;
	
	Population(int populationDim) { }
	void setChromosome(int index, Chromosome c) { }
	Chromosome getChormosome(int index) { return chromosomes[index]; }
	double getFitness(int index) { return 0; }
	void printPopulation() { }
	int getBestChromosome() { return 0; }
}
