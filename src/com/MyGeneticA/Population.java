package com.MyGeneticA;

import java.util.Arrays;

import com.mdvrp.Instance;

public class Population {
	private Chromosome[] chromosomes;
	private Instance instance;
	private int dim;
	private int currentDim;
	
	private Chromosome bestChromosome;
	private int indexBestChromosome;
	
	Population(int populationDim, Instance instance) { 
		chromosomes = new Chromosome[populationDim];
		this.instance = instance;
		this.dim = populationDim;
		this.currentDim = 0;
	}
	
	void setChromosome(int index, Chromosome c) { 
		chromosomes[index] = c;
		MyGASolution sol = new MyGASolution(c, instance);
		c.setSolution(sol);
		c.setFitness();
		
		if(currentDim == 0 || c.getFitness() < bestChromosome.getFitness()){
			bestChromosome = c;
			indexBestChromosome = index;
		}
		
		currentDim++;
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
		return bestChromosome;
	}
	
	public int getBestChromosomeIndex() {
		return indexBestChromosome;
	}
	
	/*
	private Chromosome getBestChromosomeInternal() {
		Chromosome best, c;
		int j;
		
		for(j=0; getChromosome(j) == null; j++);
		
		best= getChromosome(j);
		
		for(int i = j; i < dim; i++){
			c = getChromosome(i);
			if(c != null){
				if(c.compareTo(best) == -1)
					best = c;
			}
		}
		
		return best;
	}
	*/
	
	private int getBestChromosomeIndexInternal() {
		Chromosome best, c;
		int bestIndex, j;
		
		for(j=0; getChromosome(j) == null; j++);
		
		bestIndex = j;
		best = getChromosome(j);
				
		for(int i = j; i < dim; i++){
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

	public void removeChromosome(int index) {
		// TODO Auto-generated method stub
		chromosomes[index] = null;
		
		currentDim--;
		
		if(currentDim == 0){
			indexBestChromosome = -1;
			bestChromosome = null;
			return;
		}
		
		if(indexBestChromosome == index){
			indexBestChromosome = getBestChromosomeIndexInternal();
			bestChromosome = chromosomes[indexBestChromosome];
		}
	}
	
	public boolean chromosomeIsValid(int index){
		return chromosomes[index] != null;
	}

	public void sort() {
		// TODO Auto-generated method stub
		Arrays.sort(chromosomes);
	}

	public void swapChromosome(Chromosome c, int index) {
		removeChromosome(index);
		setChromosome(index, c);
	}

	public int getWorstChromosomeIndex() {
		this.sort();
		
		return dim-1;
	}
	
	
}
