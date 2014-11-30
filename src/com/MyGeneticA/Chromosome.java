package com.MyGeneticA;

public class Chromosome {
	private int[] genes;
	private int numberOfGenes;
	private final int delim;	//route delimiter in genes array 
	
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
}
