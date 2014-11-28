package com.MyGeneticA;

import com.mdvrp.Instance;

public class MyGA {
	private Population population;
	
	MyGA(int chromosomeDim, int populationDim, Instance instance) { }
	void initPopulation() { }
	Chromosome[][] selectParents() { return null; }
	Chromosome[] crossover(Chromosome[][] parents) { return null; }
	void generateNewPopulation(Chromosome[] children) { }
	Chromosome selectBestChromosome() { return null; }
}
