package com.mdvrp;

import java.util.ArrayList;

public class Quick {

    /**
     * Rearranges the array in ascending order, using the angle with the depot
     * @param a the array to be sorted
     */
    public static void sort(ArrayList<Customer>  a, int index) {
        sort(a, 0, a.size() - 1, index);
        //show(a, index);
    }

    // quicksort the subarray from a[lo] to a[hi]
    private static void sort(ArrayList<Customer> a, int lo, int hi, int index) { 
        if (hi <= lo) return;
        int j = partition(a, lo, hi, index);
        sort(a, lo, j-1, index);
        sort(a, j+1, hi, index);
    }

    // partition the subarray a[lo..hi] so that a[lo..j-1] <= a[j] <= a[j+1..hi]
    // and return the index j.
    private static int partition(ArrayList<Customer> a, int lo, int hi, int index) {
        int i = lo;
        int j = hi + 1;
        Customer v = a.get(lo);
        while (true) { 

            // find item on lo to swap
            while (less(a.get(++i), v, index))
                if (i == hi) break;

            // find item on hi to swap
            while (less(v, a.get(--j), index))
                if (j == lo) break;      // redundant since a[lo] acts as sentinel

            // check if pointers cross
            if (i >= j) break;

            exch(a, i, j);
        }

        // put partitioning item v at a[j]
        exch(a, lo, j);

        // now, a[lo .. j-1] <= a[j] <= a[j+1 .. hi]
        return j;
    }

   /***********************************************************************
    *  Helper sorting functions
    ***********************************************************************/
    // is v < w ?
    private static boolean less(Customer v, Customer w, int index) {
    	if( v.getAngleToDepot(index) <  w.getAngleToDepot(index)) {
			return true;
		}
		else 
			return false;
    }
        
    // exchange a[i] and a[j]
    private static void exch(ArrayList<Customer> a, int i, int j) {
        Customer swap = a.get(i);
        a.set(i, a.get(j));
        a.set(j, swap);
    }
}