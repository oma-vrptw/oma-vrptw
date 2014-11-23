package org.coinor.opents;


/**
 * <p>
 *  This implementation of a tabu list uses the {@link ComplexMove}'s <code>attributes()</code>
 *  method to determine the move's identity.
 *  <strong>It is imperative that you add the <code>attributes()</code>
 * and implement {@link ComplexMove} rather than just {@link Move}</strong>.
 * </p>
 * <p>
 *  A double <tt>int</tt> array (<tt>int[][]</tt>) is used to store
 *  the attributes values, and a double
 *  <code>for</code> loop checks for a move's presence
 *  when {@link #isTabu isTabu(...)}
 *  is called.
 * </p>
 * <p>
 *  You can resize the tabu list dynamically by calling
 *  {@link #setTenure setTenure(...)}. The data structure
 *  being used to record the tabu list grows if the requested
 *  tenure is larger than the array being used, but stays the 
 *  same size if the tenure is reduced. This is for performance
 *  reasons and insures that you can change the size of the
 *  tenure often without a performance degredation.
 * </p>
 *
 * <p><em>This code is licensed for public use under the Common Public License version 0.5.</em><br/>
 * The Common Public License, developed by IBM and modeled after their industry-friendly IBM Public License,
 * differs from other common open source licenses in several important ways:
 * <ul>
 *  <li>You may include this software with other software that uses a different (even non-open source) license.</li>
 *  <li>You may use this software to make for-profit software.</li>
 *  <li>Your patent rights, should you generate patents, are protected.</li>
 * </ul>
 * </p>
 * <p><em>Copyright © 2001 Robert Harder</em></p>
 *
 *
 * @author Robert Harder
 * @author rharder@usa.net
 * @see Move
 * @see Solution
 * @version 1.0-exp9
 * @since 1.0-exp9
 */
@SuppressWarnings("serial")
public class ComplexTabuList implements TabuList
{
    /**
     * The value 10 will be used as the tenure if
     * the null constructor is used.
     *
     * @since 1.0-exp3
     */
    public final static int DEFAULT_TENURE   = 10;
    
    /**
     * The value will be used as the maximum number of
     * the arguments that the tabu list can contain.
     *
     * @since 1.0-exp3
     */
    public final static int MAX_NUM_ATTR   = 5;


    /**
    * The value 2 will be used as the number of attributes if
     * the null constructor is used.
     *
     * @since 1.0-exp9
     */
    public final static int DEFAULT_NUM_ATTR = 2;

    
    private int           tenure;        // Tabu list tenure
    private int[][][][][] tabuList;      // Data structure used to store list
    private int           lowPos;        // Used when tenure increase in order to take only those in the list
    private int           numAttr;       // Number of attributes to track
    private int           lastIteration; // Last iteration number used so far in the list
    private int[]         tabuDim = {1, 1, 1, 1, 1}; // Used to create the tabu
    
    
    /**
     * Constructs a <code>ComplexTabuList</code> with a given tenure
     * and number of attributes
     *
     * @param tenure the tabu list's tenure
     * @param numAttr the number of attributes in each move to store
     * @since 1.0-exp3
     */
    public ComplexTabuList( int tenure, int[] attrDim )
    {
        super();
        
        this.tenure     = tenure;
        this.numAttr    = attrDim.length;
        if(numAttr > MAX_NUM_ATTR) 
        	throw new IllegalArgumentException( "Wrong number of attributes (" +
                    							numAttr + "). Should be " + 
                    							MAX_NUM_ATTR + "." );
        for(int i = 0; i < numAttr; ++i){
        	tabuDim[i] = attrDim[i];
        }
        this.tabuList   = new int[ tabuDim[0] ][ tabuDim[1] ][ tabuDim[2] ][ tabuDim[3] ][ tabuDim[4] ];
        this.lowPos = 0;
    }   // end SimpleTabuList
    
    
    
    /**
     * Determines if the {@link ComplexMove} is on the tabu list and ignores the
     * {@link Solution} that is passed to it. The move's identity is determined
     * by its <code>attributes()</code> method.
     *
     * @param move A move
     * @param solution The solution before the move operation - ignored.
     * @return whether or not the tabu list permits the move.
     * @throw IllegalArgumentException if move is not of type {@link ComplexMove}
     * @throw IllegalArgumentException if move's <tt>attributes()</tt> method
     *        returns the wrong number of elements in the array.
     * @see Move
     * @see ComplexMove
     * @see Solution
     * @since 1.0-exp3
     */
    public boolean isTabu(Solution fromSolution, Move move, int iteration) 
    {
        // Make sure it's a "ComplexMove"
        if( ! (move instanceof ComplexMove) )
            throw new IllegalArgumentException( "Move is not of type ComplexMove" );
        ComplexMove cMove = (ComplexMove)move;

        // Get attributes and check length
        int[] attrs = cMove.attributesInsert();
        if( attrs.length != this.numAttr )
            throw new IllegalArgumentException( "Wrong number of attributes (" +
                                                attrs.length + "). Should be " +
                                                this.numAttr + "." );


        // See if the move is tabu
        if ( tabuList[ attrs[0] ][ attrs[1] ][ attrs[2] ][ attrs[3] ][ attrs[4] ] + tenure  > iteration
        		&& tabuList[ attrs[0] ][ attrs[1] ][ attrs[2] ][ attrs[3] ][ attrs[4] ] > lowPos) {
//        	System.out.print(" Is tabu\n");
        	return true;
        }
        else{
//        	System.out.print(" Not tabu\n");
            return false;
        }
    }   // end isTabu 
    
    /**
     * This method accepts a {@link ComplexMove} and {@link Solution} as
     * arguments and updates the tabu list as necessary.
     * <P>
     * Although the tabu list may not use both of the passed
     * arguments, both must be included in the definition.
     *
     * Records a {@link ComplexMove} on the tabu list by calling the move's
     * <code>attributes()</code> method.
     *
     * @param move The {@link ComplexMove} to register
     * @param solution The {@link Solution} before the move operation - ignored.
     * @throw IllegalArgumentException if move is not of type {@link ComplexMove}
     * @throw IllegalArgumentException if move's <tt>attributes()</tt> method
     *        returns the wrong number of elements in the array.
     * @see Move
     * @see ComplexMove
     * @see Solution
     * @since 1.0-exp3
     */
    public synchronized void setTabu(Solution fromSolution, Move move, int iteration)
    {
        // Make sure it's a "ComplexMove"
        if( ! (move instanceof ComplexMove) )
            throw new IllegalArgumentException( "Move is not of type ComplexMove" );
        ComplexMove cMove = (ComplexMove)move;

        // Get attributes and check length
        int[] attrs = cMove.attributesDelete();
        if( attrs.length != this.numAttr )
            throw new IllegalArgumentException( "Wrong number of attributes (" +
                                                attrs.length + "). Should be " +
                                                this.numAttr + "." );
//        lastIteration = iteration; // update the last iteration used in the list
        // Record tabu
        tabuList[ attrs[0] ][ attrs[1] ][ attrs[2] ][ attrs[3] ][ attrs[4] ] = iteration;
    }   // end setTabu


    /**
     * Returns the number of attributes in each move
     * being tracked by this tabu list.
     *
     * @return number of attributes
     * @since 1.0-exp9
     */
    public int getNumberOfAttributes()
    {   return numAttr;
    }   // end getNumberOfAttributes
    
    /**
     * Reset the tabu search list, all the moves that are currently
     * tabu will set as non tabu
     */
    public void reset()
    {
    	lowPos = lastIteration;
    }

    
    /**
     * Returns the tenure being used by this tabu list.
     *
     * @return tabu list's tenure
     * @since 1.0-exp3
     */
    public int getTenure()
    {   return tenure;
    }   // end getTenure
    
    
    /**
     * Sets the tenure used by the tabu list. 
     * tenure often without a performance degredation.
     * A negative value will be ignored.
     *
     * @param tenure the tabu list's new tenure
     * @since 1.0-exp3
     */
    public void setTenure( int tenure )
    {
        if( tenure < 0 )
            return;
        if(tenure > this.tenure )
        	this.lowPos = Math.max(0, this.lastIteration - this.tenure);
        this.tenure = tenure;
    }   // end setTenure
    
    /**
     * This function returns a string which contains the tabu list values
     */
    public String toString(){
		StringBuffer print = new StringBuffer();
		// Cycle the tabu list
		for( int i = 0; i < tabuList.length; ++i ){
			print.append("D"+ i + "\n" );
			for( int j = 0; j < tabuList[i].length; ++j ){
				for( int k = 0; k < tabuList[i][j].length; ++k ){
					print.append(tabuList[i][j][k][0][0] + " ");
				}
				print.append("\n");
			}
	        
        }		
		return print.toString();
    }
    
    
}   // end class SimpleTabuList
