package com.softtechdesign.ga;

/**
 * <pre>
 * The GASequenceList class models chromosomes as strings and extends GAString 
 * while adding one main capability:
 * the ability to prevent duplicate genes from appearing in a chromosome.
 *
 * For example, if you extend this class to optimize some sort of list (set), you don't
 * want chromosomes to evolve that violate the rule that there be no duplicates in
 * the list.
 * 
 * If you just extended GAString, you could wind up with duplicate genes. For example,
 * with GAString, if your gene space is "ABCDEF", you may wind up with chromosomes like
 * "ADDCBE".
 *
 * The GASequenceList methods prevent duplicate gene entries in your chromosomes. So a gene
 * space like "ABCDEF" can result in chromosomes like "FABCDE" and "EFABCD" but NEVER
 * "ADDCBE".
 * @author Jeff Smith jeff@SoftTechDesign.com
 */
public abstract class GASequenceList extends GAString
{
    /** sequence of genes */
    protected double[] sequence;

    /**
     * Take the given chromosome which may have duplicated genes and eliminate any duplicated genes
     * by replacing duplicates with genes which were left out of the chromosome (that is they exist
     * in the gene space but not in the given chromosome).
     * For example if gene space = 'ABCDEFGH' and sChrom1 = 'ABCDDEFG', then the
     * gene 'D' is duplicated and 'H' has been left out. Swap the 2nd 'D' with 'H' and
     * the chromosome is fixed.
     * @param sChromosome
     * @return Chromosome string without any duplicate genes.
     */
    protected String getChromWithoutDuplicates(String sChromosome)
    {
        int iPos;
        int iRandomGeneLeftOut;
        String sGene, sGenesLeftOut, sRestOfChrom;

        //first get a string (which acts as a list) of all genes left OUT of this chrom
        sGenesLeftOut = "";
        for (int i = 0; i < this.possGeneValues.length(); i++)
        {
            sGene = "" + this.possGeneValues.charAt(i);
            iPos = sChromosome.indexOf(sGene);
            if (iPos < 0) //this gene not found in chromosome
                sGenesLeftOut += sGene;
        }

        if (sGenesLeftOut.length() == 0) //no duplicate genes, so exit
            return (sChromosome);

        StringBuffer sbChromosome = new StringBuffer(sChromosome);
        StringBuffer sbGenesLeftOut = new StringBuffer(sGenesLeftOut);

        for (int i = 0; i < chromosomeDim; i++)
        {
            sGene = "" + sbChromosome.charAt(i);
            sRestOfChrom = sbChromosome.substring(i + 1, chromosomeDim);

            iPos = sRestOfChrom.indexOf(sGene);
            if (iPos > -1) //gene also found in a later part of the chromosome, it is duplicated!
            {
                //assign this duplicate gene a random value from the list of genes left out
                iRandomGeneLeftOut = getRandom(sbGenesLeftOut.length());
                sbChromosome.setCharAt(iPos + i + 1, sbGenesLeftOut.charAt(iRandomGeneLeftOut));

                //now take this "gene left out" out of the list (string) of available genes
                sbGenesLeftOut.deleteCharAt(iRandomGeneLeftOut);
            }
        }

        return (sbChromosome.toString());
    }

    /**
     * Create random chromosomes from the given gene space.
     */
    protected void initPopulation()
    {
        String sGene, sChromosome;

        for (int i = 0; i < populationDim; i++)
        {
            sChromosome = "";
            for (int iGene = 0; iGene < chromosomeDim; iGene++)
            {
                //search until we find a gene not yet in the string
                do
                {
                    sGene = "" + this.possGeneValues.charAt(+getRandom(this.possGeneValues.length()));
                }
                while (sChromosome.indexOf(sGene) >= 0);

                sChromosome += sGene;
            }
            ((ChromChars)this.chromosomes[i]).setGenesFromStr(sChromosome);
        }
    }

    /**
     * Perform one point crossover on the two given chromosomes 
     * @param Chrom1
     * @param Chrom2
     */
    protected void doOnePtCrossover(Chromosome Chrom1, Chromosome Chrom2)
    {
        super.doOnePtCrossover(Chrom1, Chrom2); //do normal crossover

        //  Now eliminate any duplicated genes by replacing duplicates with genes which
        //were left out of the chromosome.
        //  For example if gene space = 'ABCDEFGH' and sChrom1 = 'ABCDDEFG', then the
        //gene 'D' is duplicated and 'H' has been left out. Swap the 2nd 'D' with 'H' and
        //the problem is fixed.

        String sChrom1 = getChromWithoutDuplicates(Chrom1.getGenesAsStr());
        String sChrom2 = getChromWithoutDuplicates(Chrom2.getGenesAsStr());
        ((ChromChars)Chrom1).setGenesFromStr(sChrom1);
        ((ChromChars)Chrom2).setGenesFromStr(sChrom2);
    }

    /**
     * Perform two point crossover on the two given chromosomes 
     * @param Chrom1
     * @param Chrom2
     */
    protected void doTwoPtCrossover(Chromosome Chrom1, Chromosome Chrom2)
    {
        super.doTwoPtCrossover(Chrom1, Chrom2); //do normal crossover

        //  Now eliminate any duplicated genes by replacing duplicates with genes which
        //were left out of the chromosome.
        //  For example if gene space = 'ABCDEFGH' and sChrom1 = 'ABCDDEFG', then the
        //gene 'D' is duplicated and 'H' has been left out. Swap the 2nd 'D' with 'H' and
        //the problem is fixed.

        String sChrom1 = getChromWithoutDuplicates(Chrom1.getGenesAsStr());
        String sChrom2 = getChromWithoutDuplicates(Chrom2.getGenesAsStr());
        ((ChromChars)Chrom1).setGenesFromStr(sChrom1);
        ((ChromChars)Chrom2).setGenesFromStr(sChrom2);
    }

    /**
     * Perform uniform crossover on the given two chromosomes 
     * @param Chrom1
     * @param Chrom2
     */
    protected void doUniformCrossover(Chromosome Chrom1, Chromosome Chrom2)
    {
        super.doUniformCrossover(Chrom1, Chrom2); //do normal crossover

        //  Now eliminate any duplicated genes by replacing duplicates with genes which
        //were left out of the chromosome.
        //  For example if gene space = 'ABCDEFGH' and sChrom1 = 'ABCDDEFG', then the
        //gene 'D' is duplicated and 'H' has been left out. Swap the 2nd 'D' with 'H' and
        //the problem is fixed.

        String sChrom1 = getChromWithoutDuplicates(Chrom1.getGenesAsStr());
        String sChrom2 = getChromWithoutDuplicates(Chrom2.getGenesAsStr());
        ((ChromChars)Chrom1).setGenesFromStr(sChrom1);
        ((ChromChars)Chrom2).setGenesFromStr(sChrom2);
    }

    /**
     * Initialize the GASequenceList
     * @param pChromosomeDim
     * @param pPopulationDim
     * @param pCrossoverProb
     * @param pRandomSelectionChance
     * @param pMaxGenerations
     * @param pNumPrelimRuns
     * @param pMaxPrelimGenerations
     * @param pMutationProb
     * @param pChromDecPts
     * @param pPossGeneValues
     * @param pCrossoverType
     * @param computeStatistics
     * @throws GAException
     */
    public GASequenceList(int pChromosomeDim,
                          int pPopulationDim,
                          double pCrossoverProb,
                          int pRandomSelectionChance,
                          int pMaxGenerations,
                          int pNumPrelimRuns,
                          int pMaxPrelimGenerations,
                          double pMutationProb,
                          int pChromDecPts,
                          String pPossGeneValues,
                          int pCrossoverType,
                          boolean computeStatistics) throws GAException
    {
        super(pChromosomeDim,
              pPopulationDim,
              pCrossoverProb,
              pRandomSelectionChance,
              pMaxGenerations,
              pNumPrelimRuns,
              pMaxPrelimGenerations,
              pMutationProb,
              pChromDecPts,
              pPossGeneValues,
              pCrossoverType,
              computeStatistics);

        if (pPossGeneValues.length() != pChromosomeDim)
            throw new GAException("Number of Possible gene values must equal Chromosome Dimension");
        sequence = new double[pChromosomeDim];
    }
}