package org.coinor.opents;


/**
 * This version of the {@link TabuSearch} can create multiple threads to take advantage
 * of multiple processors. If more than one thread is requested, then the neighborhood
 * will be split up evenly after the {@link MoveManager#getAllMoves getAllMoves()} method is called
 * on your {@link MoveManager}.
 * <p/>
 * <em><b>This is the recommended {@link TabuSearch}</b></em> for nearly all well-threaded
 * applications because it does not block on the {@link #startSolving} method. When done
 * solving a given set of iterations, the thread will die, freeing up your computer's resources.
 * If you add iterations with {@link SingleThreadedTabuSearch#setIterationsToGo setIterationsToGo()} and call 
 * {@link #startSolving} again,
 * the thread(s) will restart.
 *
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
 * <p><em>Copyright � 2001 Robert Harder</em></p>
 *
 * @author  Robert Harder
 * @author  rharder@usa.net
 * @copyright 2000 Robert Harder
 * @version 1.0c
 * @since 1.0
 */
@SuppressWarnings("serial")
public class MultiThreadedTabuSearch extends SingleThreadedTabuSearch
{
    /**
     * Number of threads to use when searching the neighborhood.
     * On modern Java Virtual Machines, you can call
     * java.lang.Runtime.getRuntime().availableProcessors()
     * to determine the number of processors available.
     *
     * @since 1.0
     */
    private int threads = 1;
    
    /**
     * Priority to use for the threads that the tabu search creates.
     *
     * @since 1.0
     */
    private int threadPriority = Thread.NORM_PRIORITY;
    
    
    /**
     * Holds the helpers that will evaluate the neighborhood.
     */
    private NeighborhoodHelper[] helpers;
    
    
/* ********  C O N S T R U C T O R S  ******** */
    
    
    /**
     * Constructs a <tt>MultiThreadedTabuSearch</tt> with no tabu objects set.
     *
     * @since 1.0
     */
    public MultiThreadedTabuSearch()
    {
        super();
        
    }   // end constructor
    
    
    
    /**
     * Constructs a <tt>MultiThreadedTabuSearch</tt> with all tabu objects set.
     * The initial solution is evaluated with the objective function,
     * becomes the <tt>currentSolution</tt> 
     * and a copy becomes the <tt>bestSolution</tt>.
     *
     * @param initialSolution The initial <tt>currentSolution</tt>
     * @param moveManager The move manager
     * @param objectiveFunction The objective function
     * @param tabuList The tabu list
     * @param aspirationCriteria The aspiration criteria or <tt>null</tt> if none is to be used
     * @param maximizing Whether or not the tabu search should be maximizing the objective function
     *
     * @see Solution
     * @see ObjectiveFunction
     * @see MoveManager
     * @see TabuList
     * @see AspirationCriteria
     *
     * @since 1.0
     */
    public MultiThreadedTabuSearch(
    Solution initialSolution,
    MoveManager moveManager,
    ObjectiveFunction objectiveFunction,
    TabuList tabuList,
    AspirationCriteria aspirationCriteria,
    boolean maximizing)
    {
    	
        super( initialSolution, moveManager, objectiveFunction, tabuList, aspirationCriteria, maximizing);
    }   // end constructor
    
    
    /**
     * Set the number of threads to use when evaluating the tabu search neighborhood.
     * Generally you should set this to the number of processors that your computer has.
     * If <var>threads</var> is negative then there will be no change.
     *
     * @param threads The number of threads to use when evaluating the neighborhood
     * @since 1.0
     */
    public synchronized void setThreads( int threads )
    {   
        // Make sure they're not asking for a negative number of threads.
        if( threads <= 0 )
            return;
        
        // Set variable
        this.threads = threads;
        
        // Make sure we have an equal number of helper threads.
        if( helpers == null )
        {   helpers = new NeighborhoodHelper[ threads ];
            for( int i = 0; i < threads; i++ )
            {
                helpers[i] = new NeighborhoodHelper();
                helpers[i].start();
            }   // end for: each new thread
        }   // end if: helpers array is null
        
        
        // Are there too few helpers?
        else if( helpers.length < threads )
        {   
            // New array for helpers
            NeighborhoodHelper[] temp = new NeighborhoodHelper[ threads ];
            
            // Copy or create new
            for( int i = 0; i < threads; i++ )
                temp[i] = i < helpers.length ? helpers[i] : new NeighborhoodHelper();
            
            // Set new array
            helpers = temp;
        }   // end else if: not enough helpers

        
        // Are there too many helpers?
        else if( helpers.length > threads )
        {   
            // New array for helpers
            NeighborhoodHelper[] temp = new NeighborhoodHelper[ threads ];
            
            // Copy or dispose
            for( int i = 0; i < threads; i++ )
                if( i < threads )
                    temp[i] = helpers[i];
                else
                    helpers[i].dispose();
            
            // Set new array
            helpers = temp;
        }   // end else if: too many helpers
        notifyAll();
    }   // end setThreads
    
    /**
     * Returns the number of threads to use when evaluating the tabu search neighborhood.
     *
     * @return The number of threads to use when evaluating the tabu search neighborhood.
     * @since 1.0
     */
    public synchronized int getThreads()
    {   
        return this.threads;
    }   // end getThreads
    
    
    /**
     * Set the thread priority to use for all threads. There will always be at least two threads:
     * one for the main tabu search engine and one for the neighborhood-evaluation thread group.
     * If <var>threadPriority</var> is less than {@link java.lang.Thread#MIN_VALUE} or
     * greater than {@link java.lang.Thread#MAX_VALUE} then the value will actually be set to
     * {@link java.lang.Thread#MIN_VALUE} or {@link java.lang.Thread#MAX_VALUE}, whichever is closer.
     * <P>
     * <em>If the tabu search is already running, 
     * this will not take effect until the tabu search restarts.</em>
     *
     * @param threadPriority
     * @since 1.0
     */
    public synchronized void setThreadPriority( int threadPriority )
    {   
        if( threadPriority < Thread.MIN_PRIORITY )
            this.threadPriority = Thread.MIN_PRIORITY;
        
        else if( threadPriority > Thread.MAX_PRIORITY )
            this.threadPriority = Thread.MAX_PRIORITY;
        
        else this.threadPriority = threadPriority;
        notifyAll();
    }   // end setThreadPriority
    
    
    /**
     * Returns the thread priority setting for the threads used by the tabu search.
     *
     * @return The thread priority setting for the threads used by the tabu search.
     * @since 1.0
     */
    public synchronized int getThreadPriority()
    {   
        return this.threadPriority;
    }   // end getThreadPriority
    
    
    /**
     * Return the {@link NeighborhoodHelper}s being used to search the neighborhood.
     *
     * @since 1.0
     */
    private NeighborhoodHelper[] getHelpers()
    {   
        return this.helpers;
    }   // end getHelpers
    
    
    
    
    /**
     * Gets the best move--one that should be used for this iteration.
     * The work load is split up among as many threads as the user requests.
     * The default is 1.
     * By setting <var>chooseFirstImprovingMove</var> to <tt>true</tt>
     * you tell the tabu search to return the first move it encounters
     * that is improving and non-tabu rather than search through all of
     * the moves. This is actually implemented by having each thread
     * return the first improving move it encounters and then choosing
     * the best among these moves.
     *
     * @since 1.0
     */
    protected Object[] getBestMove( 
    final Solution soln, 
    final Move[] moves, 
    final ObjectiveFunction objectiveFunction, 
    final TabuList tabuList, 
    final AspirationCriteria aspirationCriteria, 
    final boolean maximizing, 
    final boolean chooseFirstImprovingMove )
    {      
        // If only one thread is requested, then make it this thread and call the
        // simple "getBestMove" in the SingleThreadedTabuSearch class.
        int threads = getThreads();
        if( threads == 1 )
        {   
            return SingleThreadedTabuSearch.getBestMove( soln, moves, objectiveFunction,
                tabuList, aspirationCriteria, maximizing, chooseFirstImprovingMove, getIterationsCompleted(), this );
        }   // end if: one thread requested
        
        // Else use as many threads as was requested.
        else
        {
            // Set up variables
            Move      bestMove     = null;
            double[]  bestMoveVal  = null;
            boolean   bestMoveTabu = false;
            NeighborhoodHelper[] helpers = getHelpers();
            
            // Divide up the moves in equal-sized groups.
            int numGroups = helpers.length;
            int nominalSize = moves.length / numGroups;
            Move[][] moveGroups= new Move[ numGroups ][];
            
            // Assign equal number to all but last group
            for( int i = 0; i < numGroups-1; i++ )
            {   
                moveGroups[i] = new Move[ nominalSize ];
                System.arraycopy( moves, i*nominalSize, 
                    moveGroups[i], 0, nominalSize );
            }   // end for: group except last
            
            // Last group gets at least as many as the first groups
            // but may also have some leftovers
            moveGroups[ numGroups-1 ] = new Move[ nominalSize + (moves.length % numGroups) ];
            System.arraycopy( moves, (numGroups-1)*nominalSize, 
                moveGroups[numGroups-1], 0, moveGroups[numGroups-1].length );
            
            // Hand out moves to the threads.
            for( int i = 0; i < numGroups; i++ )
                helpers[i].setWork( soln, moveGroups[i], objectiveFunction, 
                    tabuList, aspirationCriteria, maximizing, chooseFirstImprovingMove, this, getIterationsCompleted() );

            // Wait for threads to finish neighborhood when helpers[i].getBestMove
            // is called. That is, the method blocks until the work is done.
            // As a thread finishes, immediately compare it with the existing
            // best known move.
            //Object[][] bestMoves = new Object[ numGroups ][];
            for( int i = 0; i < numGroups; i++ )
            {
                // Get helper thread's best move data
                Object[] contender = helpers[i].getBestMove();
                
                // Make quick access variables to contender.
                Move newMove = (Move) contender[0];
                double[] newObjVal = (double[]) contender[1];
                boolean newMoveTabu = ((Boolean)contender[2]).booleanValue();
                
                // If we don't already have a best move, then this is the
                // best by default.
                if( bestMove == null )
                {
                    bestMove = newMove;
                    bestMoveVal = newObjVal;
                    bestMoveTabu = newMoveTabu;
                }   // end if: no existing best move
                
                // Else check to see if the contender is better than the existing best
                else
                {
                    if( isFirstBetterThanSecond( newObjVal, bestMoveVal, maximizing ) )
                    {   
                        // New one has a better objective value.

                        // Compare tabu status. The following, messy 'if' statement says
                        // Continue under the following conditions:
                        //  o  The new move is not tabu, or
                        //  o  The new move is tabu, but so is the old best move
                        if( !(!bestMoveTabu && newMoveTabu) )
                        {   bestMove = newMove;
                            bestMoveVal = newObjVal;
                            bestMoveTabu = newMoveTabu;
                        }   // end if: switch over
                    }   // end if: new one has better objective value
                    else
                    {   // New one does not have better objective value, but see if it
                        // has a better tabu status.
                        if( bestMoveTabu && newMoveTabu )
                        {   bestMove = newMove;
                            bestMoveVal = newObjVal;
                            bestMoveTabu = newMoveTabu; // <-- This should be false at this point.
                        }   // end if: old was tabu, new one isn't.
                    }   // end else: new one does not have better objective value
                    
                }   // end else
                
            }   // end for: each thread

            return new Object[]{ bestMove, bestMoveVal, new Boolean( bestMoveTabu) };
        }   // end else: multiple threads
    }   // end getBestMove
    
    
    
    
/* ********  T A B U S E A R C H   M E T H O D S  ******** */    
    
    /**
     * Starts the tabu search solving in another thread and immediately
     * returns control to the calling thread.
     *
     * @since 1.0c
     */
    public synchronized void startSolving()
    {   
        // v1.0c: Clear internal flag that might otherwise say "stop"
        setKeepSolving( true );
        
        // Are we already solving?
        if( !solving && iterationsToGo > 0 ) 
        {
            // Turn on our 'Go' flags
            setSolving( true );
            setKeepSolving( true );
            // This line was moved from below the 'if' statement
            // in v1.0-exp2 so that if an object responding
            // to the tabuSearchStarted event inquired, the
            // isSolving() method would correctly return true.
            
            
            fireTabuSearchStarted();  // Inform listeners
            
        
            // Get a final version of 'this' so we can lock on it.
            final TabuSearch This = this;

            Thread t = new Thread( new Runnable()
            {   public void run()
                {
                    // While there's work left to do
                    while( keepSolving && iterationsToGo > 0 )
                    {   
                        // Lock on the Tabu Search object
                        synchronized( This )
                        {
                            iterationsToGo--;

                            try
                            {   performOneIteration();
                            }   // end try
                            catch( NoMovesGeneratedException e )
                            {   
                                if( err != null )
                                    err.println( e );
                                stopSolving();
                            }   // end catch
                            catch( NoCurrentSolutionException e )
                            {   
                                if( err != null )
                                    err.println( e );
                                stopSolving();
                            }   // end catch
                            incrementIterationsCompleted();
                            This.notifyAll();
                        }   // end sync: this
                    }   // end while: iters left
                    
                    // Lock and close up shop
                    synchronized( This )
                    {
                        // Turn off our 'Go' flags, although the keepSolving may
                        // already be turned off.
                        setSolving( false );
                        setKeepSolving( false );

                        // Clear the helper threads.
                        if( helpers != null )
                            for( int i = 0; i < helpers.length; i++ )
                                helpers[i].dispose();
                        helpers = null;

                        // Let listeners know that we stopped solving
                        fireTabuSearchStopped();
                        This.notifyAll();
                    }   // end sync
                    
                    // Okay, main thread is dying now. It will be restarted if
                    // more work is requested of the tabu search engine.
                    
                }   // end run
            }, "MultiThreadedTabuSearch-Master"); // end runnable, thread
            t.setPriority( threadPriority );
            t.start();
        }   // end if: not already solving
        notifyAll();
    }   // end startSolving
    
    
    /**
     * Stops the tabu search and preserves the number of
     * iterations remaining.
     *
     * @since 1.0
     */
    public synchronized void stopSolving()
    {   
        setKeepSolving( false );
        notifyAll();
    }   // end stopSolving
    
    /**
     * Blocks until the tabu search reaches zero iterations to go.
     *
     * @since 1.0-exp8
     **/
    public void waitToFinish(){
        synchronized( this ){
            while( getIterationsToGo() > 0 ){
                try{
                    this.wait();
                }   // end try
                catch( InterruptedException e ){
                    e.printStackTrace();
                }   // end catch
            }   // end while: not done
        }   // end sync
    }   // end waitToFinish
    
    
    
    
/* ********  N E I G H B O R H O O D   H E L P E R   I N N E R   C L A S S  ******** */    
    
    
    protected static class NeighborhoodHelper extends java.lang.Thread
    {
        /** Kills thread when the thread is no longer needed. */
        private boolean dispose = false;
        
        /** Tells helper that there's work waiting to be done. */
        private boolean workToDo = false;
        
        /** Stores the results of the last work that was done. */
        private Object[] bestMove;
        
        private static int instanceNum = 0;
        
        
        // The tabu search objects that the helper uses.
        private Solution            soln;
        private Move[]              moves;
        private ObjectiveFunction   objectiveFunction;
        private TabuList            tabuList;
        private AspirationCriteria  aspirationCriteria;
        private boolean             maximizing; 
        private boolean             chooseFirstImprovingMove;
        private TabuSearch          tabuSearch;
        private int                 iterationsCompleted;
        
        
        /**
         * Constructs a <tt>NeighborhoodHelper</tt> and starts its thread.
         * 
         * @since 1.0
         */
        private NeighborhoodHelper()
        {   super( "Neighborhood-Helper-" + (instanceNum++));
            //start();
        }   // end constructor
        
        
        
        /** 
         * Keeps running until the tabu search determines it's time to quit
         * and free up resources.
         */
        public void run()
        {   
            while( !dispose )
            {
                // If there's nothing to do, just wait.
                if( !workToDo )
                {
                    synchronized( this )
                    {   try{ this.wait(); }
                        catch( java.lang.InterruptedException e )
                        {   e.printStackTrace( err );
                        }   // end catch
                    }   // end sync
                }   // end if: no work to do
                
                // Else there's work to do
                else
                {   
                    synchronized( this )
                    {   
                        // Use the "getBestMove" method in the simpler SingleThreadedTabuSearch
                        // class and pretend like we're just finding a "smalller" neighborhood.
                        bestMove = SingleThreadedTabuSearch.getBestMove( 
                            soln, 
                            moves, 
                            objectiveFunction, 
                            tabuList, 
                            aspirationCriteria, 
                            maximizing, 
                            chooseFirstImprovingMove,
                            iterationsCompleted,
                            tabuSearch );
                        workToDo = false;
                        
                        this.notifyAll();
                    }   // end sync
                }   // end else: work to do
            }   // end while
            // Okay, we're dying now.
        }   // end run
    
        
        
        /**
         * Returns the results of the last work that was done. 
         * If work is still happening, then block until the work is done.
         */
        private synchronized Object[] getBestMove()
        {   
            while( workToDo )
                synchronized( this )
                {   try{ this.wait(); }
                    catch( java.lang.InterruptedException e )
                    {   e.printStackTrace( err );
                    }   // end catch
                }   // end sync
                
            return bestMove;
        }   // end getBestMove
        
        
        /** Set the tabu objects that are to do work. */
        private synchronized void setWork(
        final Solution soln, 
        final Move[] moves, 
        final ObjectiveFunction objectiveFunction, 
        final TabuList tabuList, 
        final AspirationCriteria aspirationCriteria, 
        final boolean maximizing, 
        final boolean chooseFirstImprovingMove,
        final TabuSearch tabuSearch,
        final int iterationsCompleted)
        {         
            // Store the tabu objects
            this.soln                       = soln;
            this.moves                      = moves;
            this.objectiveFunction          = objectiveFunction;
            this.tabuList                   = tabuList;
            this.aspirationCriteria         = aspirationCriteria;
            this.maximizing                 = maximizing;
            this.chooseFirstImprovingMove   = chooseFirstImprovingMove;
            this.tabuSearch                 = tabuSearch;
            this.iterationsCompleted        = iterationsCompleted;
            
            // Turn on the work flag
            workToDo = true;
            bestMove = null;
            
            // Wake up self.
            this.notifyAll();
            
        }   // end setWork
        
        
        /** Mark helper for destruction. Thread will complete and die gracefully. */
        private synchronized void dispose()
        {   this.dispose = true;
            this.notifyAll();
        }   // end dispose
        
    }   // end inner class NeighborhoodHelper
    
}   // end class MultiThreadedTabuSearch
