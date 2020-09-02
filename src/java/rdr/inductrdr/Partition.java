package rdr.inductrdr;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author dherbert
 */
public class Partition {
	/*static public Rule partition(String default_class, List<Integer> attrs, DataSet training_set, boolean falseFlag, 
			int minNum, int method, int parentcorrect, int parentincorrect) {
		List<DataSet> dsl = new ArrayList<DataSet>();
		DataSet ds = null;
		
		int size;
		if(training_set.getSize() <= 10000)
			size = (training_set.getSize() + 1) / 2;
		else
			size = 5000;
		
		for(int i = 0, j = 0; i < training_set.getSize(); i++, j++) {
			if(j == size) {
				j = 0;
				if(ds != null)
					dsl.add(ds);
			}
			if(j == 0)
				ds = new DataSet();
			
			ds.addData(training_set.getData(i));
		}
		if(ds != null)
			dsl.add(ds);
	
		List<List<Object>> listall = new ArrayList<List<Object>>();
		
		for(int j = 0; j < dsl.size(); j++) {
			ds = dsl.get(j);
			ds.attribute = training_set.attribute;
			ds.type = training_set.type;
			ds.class_set = training_set.class_set;
			
			List<Object> list;
			List<Object> list1 = new ArrayList<Object>();
			String class_name;
			List<String> class_set = ds.class_set;
			
			Rdr.CalNumericRange(attrs, ds, minNum, method);
			for(int i = 0; i < class_set.size(); i++) {
				class_name = class_set.get(i);
				if(!default_class.equals(class_name)) {
					//list = find_terms(class_name, attrs, ds, minNum);
					//if(list.size() > 0) {
					//	list1.addAll(list);
					}
				}
			}
			QuickSort.sort((List<Double>) list1.get(0), (Object) list1.get(1), (Object) list1.get(2));
			List<Double> l = (List<Double>) list1.get(0);
			for(int i = 0; i < l.size(); i++) {
				l.set(i, (double) i);
			}
			listall.add(list1);
		}
		
		Term term = null;
		String cl = null;
		List<Object> list1;
		List<Term> lt;
		List<String> lc;
		String str;
		List<String> lstr = new ArrayList<String>();
		List<Double> lcount = new ArrayList<Double>();
		List<Term> ltm = new ArrayList<Term>();
		int max = Integer.MIN_VALUE;
		boolean flag = false;
		
		for(int j = 0; j < listall.size(); j++) {
			list1 = listall.get(j);
			if(list1.size() > max)
				max = list1.size();
		}
		

		for(int i = 0; i < max; i++) {
			for(int j = 0; j < listall.size(); j++) {
				list1 = listall.get(j);
				lt = (List<Term>) list1.get(1);
				lc = (List<String>) list1.get(2);
				
				if(i < lt.size()) {

					str = String.valueOf(lt.get(i).attribute_no) + "," +
						lc.get(i);
					if(lstr.contains(str)) {
						int index = lstr.indexOf(str);
						lcount.set(index, lcount.get(index) + 1);
					} else {
						lstr.add(str);
						lcount.add(1.0);
						ltm.add(lt.get(i));
					}
				}
			}
			QuickSort.sort(lcount, lstr, ltm);
			for(int j = lcount.size() - 1; j >= 0; j--) {
				if(lcount.get(j) > lcount.size() * 0.7) {
					flag = true;
					term = ltm.get(j);
					cl = lstr.get(j);
					cl = cl.substring(cl.indexOf(",") + 1, cl.length());
					break;
				}
			}
			if(flag == true)
				break;
		}
		
		if(term == null)
			return null;
		
		List<Integer> attrs1 = new ArrayList<Integer>();
		attrs1.add(term.attribute_no);
		Rdr.CalNumericRange(attrs1, training_set, minNum, method);
		Term tm[] = new Term[1];
		training_set.findTerm(term.attribute_no, cl, minNum, tm);
		
		Rule t = new Rule();
		Clause clause = new Clause();
		clause.addTerm(tm[0]);
		t.clause = clause;
		t.class_name = cl;
		
		DataSet covered = new DataSet();
		DataSet uncovered = new DataSet();
		
		t.clause.copyCoveredAndUncovered(training_set, covered, uncovered);
		t.true_no = covered.getSize();
		t.false_no = uncovered.getSize();
		t.correct_no_t = t.true_no;
		t.correct_no_f = t.false_no;
		
		if(covered.hasClassOtherThan(t.class_name)) {
			List<Integer> a = t.clause.removeUsedAttr(attrs);
			t.correct_no_t = covered.countClass(t.class_name);
			if(attrs.size() != 0) {
				t.if_true = Rdr.make_rdr(t.class_name, attrs, covered, false, minNum, method, 
						t.correct_no_t, t.true_no - t.correct_no_t);
			} 
			attrs.addAll(a);		
		}
		
		if(uncovered.hasClassOtherThan(default_class)) {
			t.correct_no_f = uncovered.countClass(default_class);
			if(!(falseFlag == true && t.true_no == 0)) {
				t.if_false = Rdr.make_rdr(default_class, attrs, uncovered, true, minNum, method, 
						t.correct_no_f, t.false_no - t.correct_no_f);
			} 

		}

		int nt_correct;
		int nf_correct;
		if(t.if_true != null) {
			nt_correct = t.if_true.correct;
		} else {
			nt_correct = t.correct_no_t;
		}
		
		if(t.if_false != null) {
			nf_correct = t.if_false.correct;
		} else {
			nf_correct = t.correct_no_f;
		}
		t.correct = nt_correct + nf_correct;

		double p1, p2, p3;
		if(t.if_true == null) {
			if(t.true_no != 0)
				p1 = Rdr.reduced_error(t.true_no - nt_correct, t.true_no);
			else
				p1 = 0;
		} else
			p1 = t.if_true.error_prune;
		if(t.if_false == null) {
			if(t.false_no != 0)
				p2 = Rdr.reduced_error(t.false_no - nf_correct, t.false_no);
			else
				p2 = 0;
		} else
			p2 = t.if_false.error_prune;
		
		p3 = Rdr.reduced_error(parentincorrect, parentcorrect + parentincorrect);
		
		if((t.true_no == 0 || t.false_no == 0)) {
			if(p3 < (p1 + p2))
				return null;
			else
				t.error_prune = (p1 + p2);
		} else {
			if(p3 < (p1 + p2) / 2)
				return null;
			else
				t.error_prune = (p1 + p2) / 2;
		}
		
		if(t.if_true == null && t.correct_no_t != t.true_no) {
			t.correct_t = new DataSet();
			t.incorrect_t = new DataSet();
			for(int i = 0; i < covered.getSize(); i++) {
				if(covered.getData(i).class_name.equals(t.class_name)) {
					t.correct_t.addData(covered.getData(i));
				} else {
					t.incorrect_t.addData(covered.getData(i));
				}
			}
		}
		if(t.if_false == null && t.correct_no_f != t.false_no) {
			t.correct_f = new DataSet();
			t.incorrect_f = new DataSet();
			for(int i = 0; i < uncovered.getSize(); i++) {
				if(uncovered.getData(i).class_name.equals(default_class)) {
					t.correct_f.addData(uncovered.getData(i));
				} else {
					t.incorrect_f.addData(uncovered.getData(i));
				}
			}
		}
	
		return t;
	}
	
	/*static private List<Object> find_terms(String class_name, List<Integer> attrs, DataSet training_set, 
			int minNum) {
		double m1;
		Term t[] = new Term[1];
		int attr_no = 0;
		
		List<String> attrs1 = new ArrayList<String>();
		List<Double> attrs2 = new ArrayList<Double>();
		List<Term> attrs3 = new ArrayList<Term>();
		List<Object> lr = new ArrayList<Object>();
		
		for(int j = 0; j < attrs.size(); j++) {
			attr_no = attrs.get(j);
			t[0] = null;
			m1 = training_set.findTerm(attr_no, class_name, minNum, t);
			if(t[0] != null) {
				attrs2.add(m1);
				attrs3.add(t[0]);
				attrs1.add(class_name);
			}
		}
		if(attrs3.size() == 0) {
			return lr;
		}
		lr.add(attrs2);
		lr.add(attrs3);
		lr.add(attrs1);
		return lr;
	}*/
}
