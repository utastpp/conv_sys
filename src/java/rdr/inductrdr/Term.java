package rdr.inductrdr;

/**
 *
 * @author dherbert
 */
public class Term {
	int attribute_no;
	String relation;
	String value;
	Term() {
		
	}
	Term(Term t) {
		attribute_no = t.attribute_no;
		relation = t.relation;
		value = t.value;
	}
	Term(int attribute_no, String relation, String value) {
		this.attribute_no = attribute_no;
		this.relation = relation;
		this.value = value;
	}
	
	boolean matchTerm(DataRecord record, int type) {
		if(relation.equals("All values"))
			return true;
		String dataValue = record.value.get(attribute_no);
		if("?".equals(dataValue))
			return false;
		if(type == DataSet.NOMINAL) {
			if(relation.equals("=") && (dataValue.equals(value))) {
				return true;
			} else if(relation.equals("Contains")){
				String str[] = value.split(",");
				for(int i = 0; i < str.length; i++) {
					if(dataValue.equals(str[i]))
						return true;
				}
			} else if(relation.equals("!=") && !dataValue.equals(value)){
					return true;
			}
		} else if(type == DataSet.DOUBLE) {
			double dataValueD = Double.parseDouble(dataValue);
			
			if(relation.equals("=")) {
				double value1D = Double.parseDouble(value);
				if(dataValueD == value1D) {
					return true;
				}
			} else if(relation.equals(">")) {
				double value1D = Double.parseDouble(value);
				if(dataValueD > value1D) {
					return true;
				}
			} else if(relation.equals("<=")) {
				double value1D = Double.parseDouble(value);
				if(dataValueD <= value1D) {
					return true;
				}
			} else if(relation.equals("Range")) {
				String str[] = value.split(",");
				double value1D = Double.parseDouble(str[0]);
				double value2D = Double.parseDouble(str[1]);
				if(dataValueD > value1D &&	dataValueD <= value2D) {
					return true;
				}
				/*String str1[] = value.split(";");
				for(int i = 0; i < str1.length; i++) {
					String str2[] = str1[i].split(",");
					double value1D = Double.parseDouble(str2[0]);
					double value2D = Double.parseDouble(str2[1]);
					if(dataValueD >= value1D &&	dataValueD <= value2D) {
						return true;
					}
				}*/
			}
		} else if(type == DataSet.INTEGER) {
			int dataValueI = Integer.parseInt(dataValue);
			
			if(relation.equals("=")) {
				int value1I = Integer.parseInt(value);
				if(dataValueI == value1I) {
					return true;
				}
			} else if(relation.equals(">")) {
				int value1I = Integer.parseInt(value);
				if(dataValueI > value1I) {
					return true;
				}
			} else if(relation.equals("<=")) {
				int value1I = Integer.parseInt(value);
				if(dataValueI <= value1I) {
					return true;
				}
			} else if(relation.equals("Range")) {
				String str[] = value.split(",");
				int value1I = Integer.parseInt(str[0]);
				int value2I = Integer.parseInt(str[1]);
				if(dataValueI > value1I && dataValueI <= value2I) {
					return true;
				}
				/*String str1[] = value.split(";");
				for(int i = 0; i < str1.length; i++) {
					String str2[] = str1[i].split(",");
					int value1I = Integer.parseInt(str2[0]);
					int value2I = Integer.parseInt(str2[1]);
					if(dataValueI >= value1I &&	dataValueI <= value2I) {
						return true;
					}
				}*/
			}
		} 
		return false;
	}
}
