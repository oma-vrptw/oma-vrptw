package com.MyGeneticA;

import java.util.Arrays;

import com.TabuSearch.MySolution;
import com.mdvrp.Instance;

public class Population {
	private Chromosome[] chromosomes;
	private Instance instance;
	private int populationDim;
	private int currentDim;
	
	private Chromosome bestChromosome;
	private int indexBestChromosome;
	
	Population(int populationDim, Instance instance) { 
		chromosomes = new Chromosome[populationDim];
		this.instance = instance;
		this.populationDim = populationDim;
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
	
	public Chromosome getChromosome(int index) { 
		return chromosomes[index]; 
	}
	
	double getFitness(int index) { 
		MyGASolution mgas = new MyGASolution(chromosomes[index], instance);
		return mgas.getFitness();
	}
	
	public void printPopulation() {
		sort();
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
				
		for(int i = j; i < populationDim; i++){
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
		//c.solution.labelling();
	}

	public int getWorstChromosomeIndex() {
		this.sort();
		
		return populationDim-1;
	}

	public Chromosome getWorstChromosome() {
		// TODO Auto-generated method stub
		return chromosomes[getWorstChromosomeIndex()];
	}

	public void swapChromosome(Chromosome c, int index, double alpha, double beta,
			double gamma) {
		removeChromosome(index);
		setChromosome(index, c, alpha, beta, gamma);
		
	}

	private void setChromosome(int index, Chromosome c, double alpha,
			double beta, double gamma) {
		chromosomes[index] = c;
		MyGASolution sol = new MyGASolution(c, instance);
		c.setSolution(sol);
		c.getSolution().setAlphaBetaGamma(alpha, beta, gamma);
		c.setFitness();
	

		if(currentDim == 0 || c.getFitness() < bestChromosome.getFitness()){
			bestChromosome = c;
			indexBestChromosome = index;
		}
		
		currentDim++;
	}

	public boolean isClone(Chromosome c) {
		for(Chromosome tmp : chromosomes)
			if(!c.compareToGenes(tmp))
				return false;
		
		return true;
	}
	
	public void detectClones(){
		int clones =0;
		sort();
		for(int i=0; i < populationDim-1; i++){
			if(chromosomes[i].compareToGenes(chromosomes[i+1])){
				System.out.println("clones! fitness1: "+chromosomes[i].getFitness()+" fitness2: "+chromosomes[i].getFitness());
				clones++;
			}
		}
		System.out.println("num of clones: "+clones);
	}
}
