package com.MyGeneticA;

public class Chromosome {
	private int[] genes;
	
	Chromosome(int n) { genes = new int[n]; }
	void setGene(int index, int value) { genes[index] = value; }
	int getGene(int index) { return genes[index]; }
	double calculateFitness() { return 0.0; }
}
