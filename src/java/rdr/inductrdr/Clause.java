package rdr.inductrdr;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dherbert
 */
public class Clause {
	List<Term> terms;
	Clause() {
		terms = new ArrayList<Term>();
	}
	
	void addTerm(Term term) {
		terms.add(term);
	}
	
	Term GetLastTerm() {
		return new Term(terms.get(terms.size() - 1));
	}
	
	void deleteLastTerm() {
		terms.remove(terms.size() - 1);
	}

	void countSelected(DataSet data, String class_name, int p[]) {
		
		DataRecord record;
		
		p[0] = p[1] = 0;
		boolean flag;
		for(int j = 0; j < data.getSize(); j++) {
			record = data.getData(j);
			flag = match(record, data.type);
			
			if(flag == true) {
				p[1]++;
				if(record.class_name.equals(class_name)) {
					p[0]++;
				}
			}
		}
	}
	
	void countUnselected(DataSet data, String class_name, int p[]) {
		
		DataRecord record;
		
		p[0] = p[1] = 0;
		boolean flag;
		for(int j = 0; j < data.getSize(); j++) {
			record = data.getData(j);
			flag = match(record, data.type);
			
			if(flag == false) {
				p[1]++;
				if(record.class_name.equals(class_name)) {
					p[0]++;
				}
			}
		}
	}

	boolean match(DataRecord record, List<Integer> types) {
		Term term;
		boolean flag = true;
		for(int i = 0; i < terms.size(); i++) {
			term = terms.get(i);
			int type = types.get(term.attribute_no);
			
			if(!term.matchTerm(record, type)) {
				flag = false;
				break;
			}
		}
		return flag;
	}
	
	// Copy selected data records of the first argument to the second argument and the left to the third argument
	void copyCoveredAndUncovered(DataSet original, DataSet matched, DataSet unmatched) {
		DataRecord record;
		boolean flag;
		
		for(int j = 0; j < original.getSize(); j++) {
			record = original.getData(j);
			
			flag = match(record, original.type);
			
			if(flag == true) {
				matched.addData(record);
			} else {
				unmatched.addData(record);
			}
		}
		unmatched.attribute = matched.attribute = original.attribute;
		unmatched.type = matched.type = original.type;
		//unmatched.infoAttr = matched.infoAttr = original.infoAttr;
		//unmatched.infoGain = matched.infoGain = original.infoGain;
		unmatched.aggregateClass();
		matched.aggregateClass();
	}
	
	// Copy selected data records of the first argument to the second argument
	void copyCovered(DataSet original, DataSet matched) {
		DataRecord record;
		boolean flag;
		
		for(int j = 0; j < original.getSize(); j++) {
			record = original.getData(j);
			
			flag = match(record, original.type);
			
			if(flag == true) {
				matched.addData(record);
			} 
		}
		matched.attribute = original.attribute;
		matched.type = original.type;
		//matched.infoAttr = original.infoAttr;
		//matched.infoGain = original.infoGain;
		matched.aggregateClass();
	}
	
	List<Integer> removeUsedAttr(List<Integer> attrs, DataSet training_set) {
		Term term;
		List<Integer> removed = new ArrayList<Integer>();
		List<Integer> type = training_set.type;
		for(int i = 0; i < terms.size(); i++) {
			term = terms.get(i);
			/*if(type.get(term.attribute_no) == DataSet.NOMINAL)*/ {
				removed.add(term.attribute_no);
				attrs.remove(attrs.indexOf(term.attribute_no));
			}
		}
		return removed;
	}
}
