package com.MyGeneticA;

import com.mdvrp.Route;

public class Chromosome implements Comparable<Chromosome>{
	private int[] genes;
	private int numberOfGenes;
	private MyGASolution solution;
	private double fitness;
	private int routesNumber;
	
	Chromosome(int chromosomeDim) { 
		this.numberOfGenes = chromosomeDim;
		genes = new int[chromosomeDim]; 
	}
	
	public Chromosome(Route[][] feasibleRoutes, int chromosomeDim) {
		this(chromosomeDim);
		
		int k;

		k = 0;
		for(int i =0; i < feasibleRoutes.length; i++){
			for(int j=0; j < feasibleRoutes[i].length; j++){
				for(int z=0; z < feasibleRoutes[i][j].getCustomersLength(); z++, k++){
					setGene(k, feasibleRoutes[i][j].getCustomerNr(z));
				}
			}
		}
		
		//System.out.println("chromosome from TABU: "+this.toString());
	}

	public int getNumberOfGenes() {
		return numberOfGenes;
	}
	
	void setGene(int index, int value) { genes[index] = value; }
	
	public int getGene(int index) { return genes[index]; }
	
	public void print() {
		System.out.print("[");
		for(int i = 0; i < genes.length; i++){
			if(genes[i] == -1) System.out.print("] :::"+genes[i]+"::: [");
			else System.out.print(" "+genes[i]+" ");
		}
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
	
	 public String getGenesAsString()
	    {
	        String sGenes = "";

	        for (int i = 0; i < genes.length; i++)
	            sGenes += " " + genes[i] + ",";
	        return (sGenes);
	    }
	 
	 public String toString()
	    {
	        return (getGenesAsString());
	    }

	public boolean compareToGenes(Chromosome c){
		for(int i = 0; i < numberOfGenes; i++){
			if(this.genes[i] != c.genes[i]) return false;
		}
		return true;
	}

	public void setRoutesNumber(int k) {
		this.routesNumber = k;
	}
	
	public int getRoutesNumber(){
		return routesNumber;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Chromosome)) return false;
		
		Chromosome c = (Chromosome)obj;

		//sono uguali se hanno i geni nelle stesse posizioni oppure hanno la stessa fitness (probabile cambio di posizione per intere rotte)
		return this.differentGenesAmongTwoChroms(c) == 0 || this.getFitness() == c.getFitness();
	}
	
	protected int differentGenesAmongTwoChroms(Chromosome c2)
	{
		int devCnt = 0;
		for (int iGene = 0; iGene < this.numberOfGenes; iGene++)
		{
			if (this.getGene(iGene) != c2.getGene(iGene))
				devCnt++;
		}

		return devCnt;
	}
}
