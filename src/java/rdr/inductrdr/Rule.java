package rdr.inductrdr;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import rdr.sqlite.inductSqliteOperation;

/**
 *
 * @author dherbert
 */
public class Rule {
	int ruleID;
	Clause clause;
	String class_name;
	Rule if_true;
	Rule if_false;
	
	// Number of the data of route true
	int true_no;
	
	// Number of the data of route false
	int false_no;
	
	// Number of true positive of route true
	int correct_no_t;
	
	// Number of true negative of route false
	int correct_no_f;
	
	// For validation
	int v_true_no;
	int v_false_no;
	int v_correct_no_t;
	int v_correct_no_f;
	
	// Record trained datasets at the leave nodes
	DataSet correct_t;
	DataSet correct_f;
	DataSet incorrect_t;
	DataSet incorrect_f;
	
	// Record tested datasets at the leave nodes
	DataSet test_correct_t;
	DataSet test_correct_f;
	DataSet test_incorrect_t;
	DataSet test_incorrect_f;
	
	int correct;
	int ep_incorrect[];
	int ep_sum[];
	double error_prune;
	/*double jm1;
	double jm2;*/
	
	Rule() {
		clause = new Clause();
		ruleID = -1;
		v_true_no = 0;
		v_false_no = 0;
		v_correct_no_t = 0;
		v_correct_no_f = 0;
	}
	
	void outputRule(String default_class, List<String> listAttr, String indent, FileWriter fileWriter) {
		try {
			fileWriter.write(indent + "All trained data:\t" + (this.true_no + this.false_no) + "\r\n");
			fileWriter.write(indent + "Default class:\t" + default_class + "\r\n");
			
			inductSqliteOperation.insert_tb_tree(-1, default_class);
			int ruleID = inductSqliteOperation.select_max_ruleId();
			
			outputRule(listAttr, indent, fileWriter, ruleID);
			
			inductSqliteOperation.update_exception(this.ruleID, ruleID);
			
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	void outputRule(List<String> listAttr, String indent, FileWriter fileWriter, int parent_id) {
		Term term;
		
		try {
			fileWriter.write(indent + "Class:\t" + this.class_name + "\r\n");
			fileWriter.write(indent + "True Cases: " + this.true_no +"\tFalse Cases: " + this.false_no + "\r\n");
			fileWriter.write(indent + "Terms:" + "\r\n");

			inductSqliteOperation.insert_tb_tree(parent_id, this.class_name);
			ruleID = inductSqliteOperation.select_max_ruleId();
			
			for(int i = 0; i < this.clause.terms.size(); i++) {
				term = this.clause.terms.get(i);
				fileWriter.write(String.format(indent + "%d.Attribute: %-18s"
						+ "Relation: %-10sValue: %-15s\r\n", (i + 1),
						listAttr.get(term.attribute_no), term.relation, term.value/*.replace(",", " to ")*/));
				
				inductSqliteOperation.insert_tb_cond(ruleID, term.attribute_no, term.value, term.relation);
			}
			
			if(this.if_true != null) {
				fileWriter.write(indent + "If_true: (" + this.correct_no_t + " true positive and "
						+ (this.true_no - this.correct_no_t) + " false positive.)" + "\r\n");
				this.if_true.outputRule(listAttr, indent + "  ", fileWriter, ruleID);
				fileWriter.write(indent + "End If_true" + "\r\n");
				
				inductSqliteOperation.update_exception(this.if_true.ruleID, ruleID);
			} else {
				fileWriter.write(indent + "If_true: null (" + this.correct_no_t + " correct"
						+ ((this.true_no - this.correct_no_t > 0)?
								(" and " + (this.true_no - this.correct_no_t) + " incorrect"):"")
								+ ".)\r\n");
				/*if(this.true_no - this.correct_no_t > 0) {
					
					fileWriter.write(indent + "Correct:\r\n");
					for(int i = 0; i < correct_t.getSize(); i++) {
						DataRecord record = correct_t.getData(i);
						fileWriter.write(indent);
						for(int j = 0; j < record.value.size(); j++) {
							fileWriter.write(record.value.get(j) + ",");
						}
						fileWriter.write(record.class_name + "\r\n");
					}
					
					fileWriter.write(indent + "Incorrect:\r\n");
					for(int i = 0; i < incorrect_t.getSize(); i++) {
						DataRecord record = incorrect_t.getData(i);
						fileWriter.write(indent);
						for(int j = 0; j < record.value.size(); j++) {
							fileWriter.write(record.value.get(j) + ",");
						}
						fileWriter.write(record.class_name + "\r\n");
					}
				}*/
				fileWriter.write(indent + "End If_true" + "\r\n");
			}
			
			if(this.if_false != null) {
				fileWriter.write(indent + "If_false: (" + this.correct_no_f + " true negative and "
						+ (this.false_no - this.correct_no_f) + " false negative.)" + "\r\n");
				this.if_false.outputRule(listAttr, indent + "  ", fileWriter, parent_id);
				fileWriter.write(indent + "End If_false" + "\r\n");
				
				inductSqliteOperation.update_alter(this.if_false.ruleID, ruleID);
			} else {
				fileWriter.write(indent + "If_false: null (" + this.correct_no_f + " correct"
						+ ((this.false_no - this.correct_no_f > 0)?
								(" and " + (this.false_no - this.correct_no_f) + " incorrect"):"")
								+ ".)\r\n");
				
				/*if(this.false_no - this.correct_no_f > 0) {
					
					fileWriter.write(indent + "Correct:\r\n");
					for(int i = 0; i < correct_f.getSize(); i++) {
						DataRecord record = correct_f.getData(i);
						fileWriter.write(indent);
						for(int j = 0; j < record.value.size(); j++) {
							fileWriter.write(record.value.get(j) + ",");
						}
						fileWriter.write(record.class_name + "\r\n");
					}
					
					fileWriter.write(indent + "Incorrect:\r\n");
					for(int i = 0; i < incorrect_f.getSize(); i++) {
						DataRecord record = incorrect_f.getData(i);
						fileWriter.write(indent);
						for(int j = 0; j < record.value.size(); j++) {
							fileWriter.write(record.value.get(j) + ",");
						}
						fileWriter.write(record.class_name + "\r\n");
					}
				}*/
				fileWriter.write(indent + "End If_false" + "\r\n");
			}
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	// Test the whole dataset
	void test(DataSet data, String default_class, FileWriter fileWriter1) {
		DataRecord dr;
		List<int[]> listCount = new ArrayList<int[]>();
		
		for(int i = 0; i < data.class_set.size(); i++) {
			listCount.add(new int[]{0, 0});
		}
		
		boolean testflag;
		for(int i = 0; i < data.getSize(); i++) {
			dr = data.getData(i);
			testflag = this.testRecord(dr, data.type, default_class, listCount, data.class_set);
			if(testflag == false) {
				int index = data.class_set.indexOf(default_class);
				listCount.get(index)[0]++;
				if(dr.class_name.equals(default_class)){
					listCount.get(index)[1]++;
				}
			}
		}
		
		int sum[] = {0, 0};
		for(int i = 0; i < data.class_set.size(); i++) {
			sum[0] += listCount.get(i)[0];
			sum[1] += listCount.get(i)[1];
		}
		
		try {
			fileWriter1.write("All data count:\t\t\t" + data.getSize() + "\r\n");
			fileWriter1.write("All selected data:\t\t" + sum[0] + "\r\n");
			fileWriter1.write("All correctly selected data:\t" + sum[1] + "\r\n");
			fileWriter1.write(String.format("Precision:\t\t\t%.2f%%\r\n", (float)sum[1]/sum[0]*100));
			
			String class_name;
			for(int i = 0; i < data.class_set.size(); i++) {
				class_name = data.class_set.get(i);
				int[] c = listCount.get(i);
				fileWriter1.write("Class: " + class_name + "\r\n");
				fileWriter1.write("Data count:\t\t\t" + data.countClass(class_name) + "\r\n");
				fileWriter1.write("Selected data:\t\t\t" + c[0] + "\r\n");
				fileWriter1.write("Correctly selected data:\t" + c[1] + "\r\n");
				fileWriter1.write(String.format("Recall:\t\t\t\t%.2f%%\r\n", (float)c[1]/data.countClass(class_name)*100));
				fileWriter1.write(String.format("Precision:\t\t\t%.2f%%\r\n", (c[0] == 0)?0:((float)c[1]/c[0]*100)));
			}
			
			
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	// Test each record
	boolean testRecord(DataRecord record, List<Integer> types, String class_name, 
			List<int[]> count, List<String> classSet) {
		boolean flag = this.clause.match(record, types);
		boolean testflag = false;
		
		if(flag == true) {
			if(this.if_true != null) {
				testflag = this.if_true.testRecord(record, types, this.class_name, count, classSet);
			} else {
				int index = classSet.indexOf(this.class_name);
				if(index != -1) {
					testflag = true;
					count.get(index)[0]++;
					if(this.class_name.equals(record.class_name)) {
						if(this.test_correct_t == null) {
							this.test_correct_t = new DataSet();
						}
						this.test_correct_t.addData(record);
						count.get(index)[1]++;
					} else {
						if(this.test_incorrect_t == null) {
							this.test_incorrect_t = new DataSet();
						}
						this.test_incorrect_t.addData(record);
					}
				}
			}
		} else {
			if(this.if_false != null) {
				testflag = this.if_false.testRecord(record, types, class_name, count, classSet);
			} else {
				int index = classSet.indexOf(class_name);
				if(index != -1) {
					testflag = true;
					count.get(index)[0]++;
					if(class_name.equals(record.class_name)) {
						if(this.test_correct_f == null) {
							this.test_correct_f = new DataSet();
						}
						this.test_correct_f.addData(record);
						count.get(index)[1]++;
					} else {
						if(this.test_incorrect_f == null) {
							this.test_incorrect_f = new DataSet();
						}
						this.test_incorrect_f.addData(record);
					}
				}
			}
		}
		return testflag;
	}
	
	void writeTestLog(String default_class, List<String> listAttr, String indent, FileWriter fileWriter) {
		Term term;
		
		try {
			if(default_class != null) {
				fileWriter.write(indent + "Default class:\t" + default_class + "\r\n");
			}
			
			fileWriter.write(indent + "Class:\t" + this.class_name + "\r\n");
			fileWriter.write(indent + "Terms:" + "\r\n");
			
			for(int i = 0; i < this.clause.terms.size(); i++) {
				term = this.clause.terms.get(i);
				fileWriter.write(String.format(indent + "%d.Attribute: %-18s"
						+ "Relation: %-10sValue: %-15s\r\n", (i + 1),
						listAttr.get(term.attribute_no), term.relation, term.value/*.replace(",", " to ")*/));
			}
			
			if(this.if_true != null) {
				fileWriter.write(indent + "If_true:\r\n");
				this.if_true.writeTestLog(null, listAttr, indent + "  ", fileWriter);
				fileWriter.write(indent + "End If_true" + "\r\n");
			} else {
				fileWriter.write(indent + "If_true: null\r\n");
				if(this.test_correct_t != null) {
					fileWriter.write(indent + "[" + this.test_correct_t.getSize() + "] correct.***********\r\n");
					for(int i = 0; i < this.test_correct_t.getSize(); i++) {
						DataRecord record = test_correct_t.getData(i);
						fileWriter.write(indent);
						for(int j = 0; j < record.value.size(); j++) {
							fileWriter.write(record.value.get(j) + ",");
						}
						fileWriter.write(record.class_name + "\r\n");
					}
				}
				if(this.test_incorrect_t != null) {
					fileWriter.write(indent + "[" + this.test_incorrect_t.getSize() + "] incorrect.***********\r\n");
					for(int i = 0; i < this.test_incorrect_t.getSize(); i++) {
						DataRecord record = test_incorrect_t.getData(i);
						fileWriter.write(indent);
						for(int j = 0; j < record.value.size(); j++) {
							fileWriter.write(record.value.get(j) + ",");
						}
						fileWriter.write(record.class_name + "\r\n");
					}
				}
				fileWriter.write(indent + "End If_true" + "\r\n");
			}
			
			if(this.if_false != null) {
				fileWriter.write(indent + "If_false:\r\n");
				this.if_false.writeTestLog(null, listAttr, indent + "  ", fileWriter);
				fileWriter.write(indent + "End If_false" + "\r\n");
			} else {
				fileWriter.write(indent + "If_false: null\r\n");
				if(this.test_correct_f != null) {
					fileWriter.write(indent + "[" + this.test_correct_f.getSize() + "] correct.***********\r\n");
					for(int i = 0; i < this.test_correct_f.getSize(); i++) {
						DataRecord record = test_correct_f.getData(i);
						fileWriter.write(indent);
						for(int j = 0; j < record.value.size(); j++) {
							fileWriter.write(record.value.get(j) + ",");
						}
						fileWriter.write(record.class_name + "\r\n");
					}
				}
				if(this.test_incorrect_f != null) {
					fileWriter.write(indent + "[" + this.test_incorrect_f.getSize() + "] incorrect.***********\r\n");
					for(int i = 0; i < this.test_incorrect_f.getSize(); i++) {
						DataRecord record = test_incorrect_f.getData(i);
						fileWriter.write(indent);
						for(int j = 0; j < record.value.size(); j++) {
							fileWriter.write(record.value.get(j) + ",");
						}
						fileWriter.write(record.class_name + "\r\n");
					}
				}
				fileWriter.write(indent + "End If_false" + "\r\n");
			}
		} catch(IOException e) {
			System.out.println(e.getMessage());
		}
	}
	
	void validate(DataSet data, String default_class) {
		DataRecord dr;
		for(int i = 0; i < data.getSize(); i++) {
			dr = data.getData(i);
			this.validateRecord(dr, data.type, default_class, data.class_set);
		}
		
		prune(this, 0, Integer.MAX_VALUE);
	}
	
	void validateRecord(DataRecord record, List<Integer> types, String class_name, List<String> classSet) {
		boolean flag = this.clause.match(record, types);
		if(flag == true) {
			this.v_true_no += 1;
			if(this.class_name.equals(record.class_name))
				this.v_correct_no_t += 1;
			if(this.if_true != null) {
				this.if_true.validateRecord(record, types, this.class_name, classSet);
			} /*else {
				int index = classSet.indexOf(this.class_name);
				if(index != -1) {
					if(this.class_name.equals(record.class_name)) {
						this.correct_t.addData(record);
					} else {
						this.incorrect_t.addData(record);
					}
				}
			}*/
		} else {
			this.v_false_no += 1;
			if(class_name.equals(record.class_name))
				this.v_correct_no_f += 1;
			if(this.if_false != null) {
				this.if_false.validateRecord(record, types, class_name, classSet);
			} /*else {
				int index = classSet.indexOf(class_name);
				if(index != -1) {
					if(class_name.equals(record.class_name)) {
						this.correct_f.addData(record);
					} else {
						this.incorrect_f.addData(record);
					}
				}
			}*/
		}
	}
	
	void prune(Rule r, int parentcorrect, int parentsum) {
		if(r.if_true != null)
			prune(r.if_true, r.v_correct_no_t, r.v_true_no);
		if(r.if_false != null)
			prune(r.if_false, r.v_correct_no_f, r.v_false_no);
		
		int nt_correct;
		int nf_correct;
		if(r.if_true != null)
			nt_correct = r.if_true.correct;
		else
			nt_correct = r.v_correct_no_t;
		
		if(r.if_false != null)
			nf_correct = r.if_false.correct;
		else
			nf_correct = r.v_correct_no_f;
		r.correct = nt_correct + nf_correct;
		
		double p1, p2, p3;
		if(r.if_true == null)
			p1 = InductRDR.confidence_interval(r.v_true_no - nt_correct, r.v_true_no);
		else
			p1 = r.if_true.error_prune;
		if(r.if_false == null)
			p2 = InductRDR.confidence_interval(r.v_false_no - nf_correct, r.v_false_no);
		else
			p2 = r.if_false.error_prune;
		
		p3 = InductRDR.confidence_interval(parentsum - parentcorrect, parentsum);
		
		if(p3 < (p1 + p2) / 2) 
			r = null;
		else
			r.error_prune = (p1 + p2) / 2;
	}
}
