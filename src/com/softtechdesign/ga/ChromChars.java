package com.softtechdesign.ga;

/** 
 * Chromosome class where genes are stored as an array of chars
 * @author Jeff Smith jeff@SoftTechDesign.com
 */
public class ChromChars extends Chromosome
{
    /** array of genes that comprise this Chromosome */
    protected char[] genes;

    /**
     * Constructor creates new array of genes given the iGenesDim
     * @param iGenesDim
     */
    public ChromChars(int iGenesDim)
    {
        genes = new char[iGenesDim];
    }

    /**
     * Returns the genes in this chromosome as a string
     * @return String
     */
    public String toString()
    {
        return (getGenesAsStr());
    }

    /**
     * Determines how many genes are in common betwen this Chromosome and the given Chromosome
     * @param chromosome
     * @return int
     */
    public int getNumGenesInCommon(Chromosome chromosome)
    {
        int numGenesInCommon = 0;
        String chromGenes = chromosome.getGenesAsStr();

        for (int i = 0; i < genes.length; i++)
            if (this.genes[i] == chromGenes.charAt(i))
                numGenesInCommon++;
        return (numGenesInCommon);
    }

    /**
     * Getter for the genes array
     * @return char[]
     */
    public char[] getGenes()
    {
        return(genes);
    }
    
    /**
     * return the array of genes as a string 
     */
    public String getGenesAsStr()
    {
        String sGenes = "";

        for (int i = 0; i < genes.length; i++)
            sGenes += genes[i];
        return (sGenes);
    }

    /** return the gene indexed by iGene as a char
     *  @return char gene  
     */
    public char getGene(int iGene)
    {
        return (this.genes[iGene]);
    }

    /**
     * Set this chromosomes genes (array) to the given chromosome string
     * @param String 
     */
    public void setGenesFromStr(String sChromosome)
    {
        for (int i = 0; i < genes.length; i++)
            this.genes[i] = sChromosome.charAt(i);
    }

    /**
     * Copy the genes from the given chromosome over the existing genes
     * @param Chromosome  
    */
    public void copyChromGenes(Chromosome chromosome)
    {
        int iGene;

        ChromChars ChromChars = (ChromChars)chromosome;
        for (iGene = 0; iGene < genes.length; iGene++)
            this.genes[iGene] = ChromChars.genes[iGene];
    }
}
