package com.softtechdesign.ga;

/**
 * Chromosome class where genes are stored as floating point numbers 
 * 
 * @author Jeff Smith jeff@SoftTechDesign.com
 */
public class ChromFloat extends Chromosome
{
    /** array of genes that comprise the chromosome */
    protected double[] genes;

    /**
     * Creates an double array which stores the genes 
     * @param iGenesDim
     */
    public ChromFloat(int iGenesDim)
    {
        genes = new double[iGenesDim];
    }

    /**
     * Getter for the genes array
     * @return double[]
     */
    public double[] getGenes()
    {
        return(genes);
    }
    
    /**
     * Return the genes as a string
     * @return String
     */
    public String toString()
    {
        return (getGenesAsString());
    }

    /**
     * Get the number of genes in common
     * @param chromosome
     * @return int
     */
    public int getNumGenesInCommon(Chromosome chromosome)
    {
        //@@ need to write some code to see how many float genes are within 10% of each other
        //if they are within 10%, they are considered to be the "same" gene
        return (genes.length);
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
     * Copy the genes from the given chromosome over the existing genes
     * @param Chromosome 
     */
    public void copyChromGenes(Chromosome chromosome)
    {
        ChromFloat chromFloat = (ChromFloat)chromosome;

        for (int iGene = 0; iGene < genes.length; iGene++)
            this.genes[iGene] = chromFloat.genes[iGene];
    }

    /**
     * return the gene indexed by iGene as a double
     * @param int iGene gene to get
     * @return double gene value 
     */
    public double getGene(int iGene)
    {
        return (this.genes[iGene]);
    }

    /**
     * Set the gene indexed by iGene to the given double value
     * @param int gene to set
     * @param double value to set gene
     */
    public void setGene(int iGene, double value)
    {
        genes[iGene] = value;
    }

    /**
     * return the array of genes as a string
     * @return String
     */
    public String getGenesAsStr()
    {
        String sGenes = "";
        for (int i = 0; i < genes.length; i++)
            sGenes += this.genes[i] + ",";
        return (sGenes);
    }
}
