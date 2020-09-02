package rdr.inductrdr;

import java.util.List;

/**
 *
 * @author dherbert
 */
public class DataRecord {
	List<String> value;
	String class_name;
	DataRecord(List<String> l, String c) {
		value = l;
		class_name = c;
	}
}
