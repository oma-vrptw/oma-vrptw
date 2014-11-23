package com.TabuSearch;

import org.coinor.opents.*;

import com.mdvrp.Customer;
import com.mdvrp.Instance;
import com.mdvrp.Route;

@SuppressWarnings("serial")
public class MyMoveManager implements MoveManager {
	private static Instance instance;
    private MovesType movesType;

	public MyMoveManager(Instance instance) {
    	MyMoveManager.setInstance(instance);
    }
    
    public Move[] getAllMoves( Solution solution ) { 
    	MySolution sol = ((MySolution)solution);
     
    	switch (movesType) {
		case SWAP:
			return getSwapMoves(sol);

		default:
			return getSwapMoves(sol);
		}
    }   // end getAllMoves
    
    
    /**
     * Generate moves that move each customer from one route to all routes that are different
     * @param solution
     * @return
     */
    public Move[] getSwapMoves(MySolution solution){
    	 Route[][] routes = solution.getRoutes();
         Move[] buffer = new Move[ getInstance().getCustomersNr() * getInstance().getVehiclesNr() * getInstance().getDepotsNr()];
         int nextBufferPos = 0;
         
         // iterates depots
         for (int i = 0; i < routes.length; ++i) {
         	// iterates routes
         	for (int j = 0; j < routes[i].length; ++j) {
         		// iterates customers in the route
         		for (int k = 0; k < routes[i][j].getCustomersLength(); ++k) {
         			for(int l = 0; l < routes.length; ++l){
	         			// iterate each route for that deposit and generate move to it if is different from the actual route
	         			for (int r = 0; r < routes[l].length; ++r) { 
	         				if (!(r == j && i == l)) {
	         					Customer customer = routes[i][j].getCustomer(k);
	         					buffer[nextBufferPos++] = new MySwapMove(getInstance(), customer, i, j, k, l, r);
	         				}
	         			}
         			}
         		}
         	}
         }
          
         // Trim buffer
         Move[] moves = new Move[ nextBufferPos];
         System.arraycopy( buffer, 0, moves, 0, nextBufferPos );
         
         return moves;
    }
    
	/**
	 * @return the movesType
	 */
	public MovesType getMovesType() {
		return movesType;
	}

	/**
	 * @param movesType the movesType to set
	 */
	public void setMovesType(MovesType movesType) {
		this.movesType = movesType;
	}

	/**
	 * @return the instance
	 */
	public static Instance getInstance() {
		return instance;
	}

	/**
	 * @param instance the instance to set
	 */
	public static void setInstance(Instance instance) {
		MyMoveManager.instance = instance;
	}
    
}   // end class MyMoveManager
