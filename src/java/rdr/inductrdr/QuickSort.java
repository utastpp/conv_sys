package rdr.inductrdr;

import java.math.BigDecimal;
import java.util.List;

/**
 *
 * @author dherbert
 */
public class QuickSort {
	static private List<Double> list1;
	static private List<Object> list1big;
	static private Object list2;
	static private Object list3;
	static private int length;
 
	// Sort the first arguments and arrange the same order for the second

    /**
     *
     * @param l1
     * @param l2
     */
    static public void sort(List<Double> l1, Object l2) {
         
        list1 = l1;
        list2 = l2;
        list3 = null;
        length = list1.size();
        quickSort(0, length - 1);
    }

    // Sort the first arguments and arrange the same order for the second and the third

    /**
     *
     * @param l1
     * @param l2
     * @param l3
     */
    static public void sort(List<Double> l1, Object l2, Object l3) {
        
        list1 = l1;
        list2 = l2;
        list3 = l3;
        length = list1.size();
        quickSort(0, length - 1);
    }

    static private void quickSort(int lowerIndex, int higherIndex) {
         
        int i = lowerIndex;
        int j = higherIndex;
        // calculate pivot number, I am taking pivot as middle index number
        double pivot = list1.get(lowerIndex+(higherIndex-lowerIndex)/2);
        // Divide into two arrays
        while (i <= j) {

            while (list1.get(i) < pivot) {
                i++;
            }
            while (list1.get(j) > pivot) {
                j--;
            }
            if (i <= j) {
                exchangeNumbers(i, j);
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
            quickSort(lowerIndex, j);
        if (i < higherIndex)
            quickSort(i, higherIndex);
    }
 
    static private void exchangeNumbers(int i, int j) {
        double temp1 = list1.get(i);
        list1.set(i, list1.get(j));
        list1.set(j, temp1);
        if(list2 != null) {
        	Object temp2 = ((List)list2).get(i);
        	((List)list2).set(i, ((List)list2).get(j));
        	((List)list2).set(j, temp2);
        }
        if(list3 != null) {
        	Object temp3 = ((List)list3).get(i);
        	((List)list3).set(i, ((List)list3).get(j));
        	((List)list3).set(j, temp3);
        }
    }
    
    /*
    static public void sortBigDecimal(List<Object> l1, Object l2) {
        
    	list1big = l1;
        list2 = l2;
        length = list1big.size();
        quickSortBigDecimal(0, length - 1);
    }
    
    static private int compareTo(Object a, Object b) {
    	List<Object> a1 = (List<Object>) a;
    	List<Object> b1 = (List<Object>) b;
    	
    	if((int) a1.get(1) > (int) b1.get(1) || 
    			((int) a1.get(1) == (int) b1.get(1) && (double) a1.get(0) > (double) b1.get(0))) {
    		return 1;
    	} else if((int) a1.get(1) == (int) b1.get(1)) {
    		return 0;
    	} else
    		return 1;
    }
    
    static private void quickSortBigDecimal(int lowerIndex, int higherIndex) {
        
        int i = lowerIndex;
        int j = higherIndex;
        // calculate pivot number, I am taking pivot as middle index number
        Object pivot = list1big.get(lowerIndex+(higherIndex-lowerIndex)/2);
        // Divide into two arrays
        while (i <= j) {
            while (compareTo(list1big.get(i), pivot) == -1) { // less than
                i++;
            }
            while (compareTo(list1big.get(j), pivot) == 1) { // greater than
                j--;
            }
            if (i <= j) {
                exchangeNumbersBigDecimal(i, j);
                //move index to next position on both sides
                i++;
                j--;
            }
        }
        // call quickSort() method recursively
        if (lowerIndex < j)
        	quickSortBigDecimal(lowerIndex, j);
        if (i < higherIndex)
        	quickSortBigDecimal(i, higherIndex);
    }
    
    static private void exchangeNumbersBigDecimal(int i, int j) {
    	Object temp1 = list1big.get(i);
        list1big.set(i, list1big.get(j));
        list1big.set(j, temp1);
        if(list2 != null) {
        	Object temp2 = ((List)list2).get(i);
        	((List)list2).set(i, ((List)list2).get(j));
        	((List)list2).set(j, temp2);
        }
    }*/
}
