package com.MyGeneticA;

public class Chromosome implements Comparable<Chromosome>{
	private int[] genes;
	private int numberOfGenes;	//error! it's the length of the array, including delimiters, not genes number only.
	private final int delim;	//route delimiter in genes array 
	private MyGASolution solution;
	private double fitness;
	
	Chromosome(int n) { 
		delim = -1;	//set delimiter to -1
		this.numberOfGenes = n;
		genes = new int[n]; 
	}
	
	public int getNumberOfGenes() {
		return numberOfGenes;
	}
	
	void setGene(int index, int value) { genes[index] = value; }
	
	int getGene(int index) { return genes[index]; }
	
	public void print() {
		System.out.print("[");
		for(int i = 0; i < genes.length; i++){
			if(genes[i] == -1) System.out.print("] :::"+genes[i]+"::: [");
			else System.out.print(" "+genes[i]+" ");
		}
	}
	
	/**
	 * @return true if this element is a delimiter
	 */
	public boolean isDelimiter(int index){
		return genes[index] == delim;
	}

	public void setSolution(MyGASolution sol) {
		this.solution = sol;		
	}

	public double getFitness(){
		return this.fitness;
	}
	
	public void setFitness() {
		this.fitness = solution.getFitness();		
	}

	@Override
	public int compareTo(Chromosome c) {
		if(c == null)throw new NullPointerException();
		
		if(this.getFitness() > c.getFitness())
			return 1;
		else if(this.getFitness() < c.getFitness())
			return -1;
		
		return 0;
	}

	public boolean compareToGenes(Chromosome c){
		for(int i = 0; i < numberOfGenes; i++){
			if(this.genes[i] != c.genes[i]) return false;
		}
		return true;
	}
	
	public MyGASolution getSolution() {
		return solution;
	}
	
	public int getNumRoutes(){
		int count=0;
		for(int z = 0; z < numberOfGenes; z++){
			if(genes[z] == -1) count++;
		}
		return count;
	}
}
