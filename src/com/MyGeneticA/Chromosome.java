package com.MyGeneticA;

import com.mdvrp.Instance;
import com.mdvrp.Route;

public class Chromosome implements Comparable<Chromosome>{
	private int[] genes;
	private int numberOfGenes;
	public MyGASolution solution;
	private double fitness;
	private int routesNumber;
	private boolean tabuImproved;	//true if chromosome already passed to TABU
	
	Chromosome(int chromosomeDim) { 
		this.numberOfGenes = chromosomeDim;
		this.genes = new int[chromosomeDim]; 
		/*
		this.fitness = Double.MAX_VALUE;
		this.routesNumber = 0;
		*/
		this.solution = null;
		this.tabuImproved = false;
	}
	
	public Chromosome(Route[][] feasibleRoutes, int chromosomeDim, Instance instance, double alpha, double beta, double gamma) {
		this(chromosomeDim);
		
		int k;

		k = 0;
		for(int i =0; i < feasibleRoutes.length; i++){
			for(int j=0; j < feasibleRoutes[i].length; j++){
				for(int z=0; z < feasibleRoutes[i][j].getCustomersLength(); z++, k++){
					setGene(k, feasibleRoutes[i][j].getCustomerNr(z));
					//System.out.print(feasibleRoutes[i][j].getCustomerNr(z)+ ", ");
				}
				//if(feasibleRoutes[i][j].getCustomersLength() > 0)System.out.println();
			}
		}
		this.solution =  new MyGASolution(this, instance);
		this.solution.setAlphaBetaGamma(alpha, beta, gamma);
		this.fitness = solution.calculateFitness();
		//System.out.println("chromosome from TABU: "+this.toString());
	}

	// constructor which clone the cost passed as parameter
	public Chromosome(Chromosome c){		
		this.numberOfGenes = new Integer(c.numberOfGenes);
		this.genes = new int[this.numberOfGenes];
		for(int i=0; i < this.numberOfGenes; i++)
			this.genes = c.genes;		
	}

	public int getNumberOfGenes() {
		return numberOfGenes;
	}
	
	void setGene(int index, int value) { genes[index] = value; }
	
	public int getGene(int index) { return genes[index]; }
	
	public void print() {
		System.out.print("genes: [ ");
		for(int i=0; i< numberOfGenes; i++)System.out.print(genes[i]+" ");
		System.out.println("]");
		System.out.println("fitness: "+fitness);
	}

	public void setSolution(MyGASolution sol) {
		this.solution = sol;		
	}

	public double getFitness(){
		return this.fitness;
	}
	
//	public void setFitness() {
//		this.fitness = solution.getFitness();		
//	}

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
			if(this.genes[i] != c.genes[i]) 
				// non è clone
				return false;
		}
		// è clone
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
	
	public int differentGenesAmongTwoChroms(Chromosome c2)
	{
		int devCnt = 0;
		for (int iGene = 0; iGene < this.numberOfGenes; iGene++)
		{
			if (this.getGene(iGene) != c2.getGene(iGene))
				devCnt++;
		}

		return devCnt;
	}

	public boolean isAlreadyTabuImproved() {
		return tabuImproved;
	}

	public void setTabuImproved(boolean b) {
		tabuImproved = b;		
	}

	public void evaluate(Instance instance) {
		if(this.solution == null)
			this.solution = new MyGASolution(this, instance);
		
		this.fitness = solution.calculateFitness();
	}

	public void swapGenes(int position, int customer) {	
		int i, tmp;
		
		//search customer
		for(i = 0; i < numberOfGenes && genes[i] != customer; i++);
		
		//swap
		if(i >= numberOfGenes){
			System.out.println("error");
			System.out.println("we search "+customer);
			print();
		}
		tmp = genes[position];
		genes[position] = genes[i];
		genes[i] = tmp;
	}
}
