package rdr.inductrdr;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dherbert
 */
public class NumericRange {
	List<Integer> attrIndex;
	List<Integer> type;
	List<Object> start;
	List<Object> end;
	List<Object> class_name;
	List<Object> infoGain;
	NumericRange() {
		attrIndex = new ArrayList<Integer>();
		type = new ArrayList<Integer>();
		start = new ArrayList<Object>();
		end = new ArrayList<Object>();
		class_name = new ArrayList<Object>();
		infoGain = new ArrayList<Object>();
	}
}
