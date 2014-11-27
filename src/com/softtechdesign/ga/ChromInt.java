package com.softtechdesign.ga;
import com.mdvrp.*;

/**
 * Chromosome class where genes are stored as integer numbers 
 * 
 * @author Alessio Viola
 */

public class ChromInt extends Chromosome {
    
	/** array of genes that comprise the chromosome */
    protected int[] genes;
    
    /**
     * Creates an integer array which stores the genes 
     * @param iGenesDim
     */
    public ChromInt(int iGenesDim)
    {
    	
        genes = new int[iGenesDim];
        
    }
    
    /**
     * Getter for the genes array
     * @return int[]
     */
    public int[] getGenes()
    {
        return(genes);
    }
    
    /**
     * Return the array of genes as a string
     * @return String 
     */
    public String getGenesAsString()
    {
        String sGenes = "";

        for (int i = 0; i < genes.length; i++)
            sGenes += " " + genes[i] + ",";
        return (sGenes);
    }
    
    /**
     * Return the genes as a string
     * @return String
     */
    public String toString()
    {
        return (getGenesAsString());
    }
    
	@Override
	String getGenesAsStr() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
     * Copy the genes from the given chromosome over the existing genes
     * @param Chromosome 
     */
    public void copyChromGenes(Chromosome chromosome)
    {
        ChromInt chromInt = (ChromInt)chromosome;

        for (int iGene = 0; iGene < genes.length; iGene++)
            this.genes[iGene] = chromInt.genes[iGene];
    }
    
    /**
     * return the gene indexed by iGene as an integer
     * @param int iGene gene to get
     * @return int gene value 
     */
    public int getGene(int iGene)
    {
        return (this.genes[iGene]);
    }
    
    /**
     * Set the gene indexed by iGene to the given integer value
     * @param int gene to set
     * @param int value to set gene
     */
    public void setGene(int iGene, int value)
    {
        genes[iGene] = value;
    }

	@Override
	int getNumGenesInCommon(Chromosome chromosome) {
		// TODO Auto-generated method stub
		return 0;
	}

}
