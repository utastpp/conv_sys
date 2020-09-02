package rdr.inductrdr;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import rdr.apps.Main;
import rdr.cases.CaseLoader;
import rdr.cases.CornerstoneCaseSet;
import rdr.logger.Logger;
import rdr.sqlite.inductSqliteOperation;

import weka.core.Instances;

/**
 *
 * @author dherbert
 */
public class InductRDR {

	static Rule rule;
	
    /**
     *
     * @param clause
     * @param training_set
     * @param class_name
     * @return
     */
    public static double m_function(Clause clause, DataSet training_set, String class_name) {
            
		int p[] = new int[2];
                
		clause.countSelected(training_set, class_name, p);
		int z = p[0];
		int s = p[1];
		int k = training_set.countClass(class_name);
		int n = training_set.getSize();
		
		int min = k <= s? k : s;
		double sum = 0;
		double k_D_n = (double) k/n;
		double a1_k_D_n = 1 - k_D_n;
		double pre = m(s, z, Math.pow(k_D_n, z), Math.pow(a1_k_D_n, s - z));
                
                
		for (int i = z; i <= min; i++) {
			if(i == z)
				sum += pre;
			else {
				pre *= k_D_n / a1_k_D_n;
				sum += pre;
			}
			
			if(i < min - 1)
				pre *= (double) (min - i) / (i + 1);
			
		}

		return sum;
	}
	
    /**
     *
     * @param z
     * @param s
     * @param k
     * @param n
     * @return
     */
    public static double m_function(int z, int s, int k, int n) {
		
		int min = k <= s? k : s;
		double sum = 0;
		double k_D_n = (double) k/n;
		double a1_k_D_n = 1 - k_D_n;
		double pre = m(s, z, Math.pow(k_D_n, z), Math.pow(a1_k_D_n, s - z));
		//double pre = m(s, z, k_D_n, a1_k_D_n);
		//if("NaN".equals(pre))
		//	return Double.POSITIVE_INFINITY;
		
		for (int i = z; i <= min; i++) {
			if(i == z)
				sum += pre;
			else {
				pre *= k_D_n / a1_k_D_n;
				sum += pre;
			}
			
			if(i < min - 1)
				pre *= (double) (min - i) / (i + 1);
			
		}

		return sum;
	}
	
	// First element of m function
	private static double m(int s, int i, double k_D_n, double a1_k_D_n) {
		double r = k_D_n * a1_k_D_n;
		int j = 0;
		for(; j <= i - 1; j++) {
			r *= (double) (s - j) / (i - j);
		}
		return r;
	}
	
	static private double j_measure(int z, int s, int k, int n) {
		double px = (double) k / n;
		double py = (double) s / n;
		double pxy = (double) z / s;
		double j;
		
		return py * (pxy * Math.log(pxy / px)  + (1 - pxy) * Math.log((1 - pxy) / (1 - px))) / Math.log(2);
	}
	
	static private List<Object> best_clause(String class_name, String default_class, List<Integer> attrs, DataSet training_set, 
			int minNum, int parentcorrect, int parentincorrect/*, List<Double> lr1, List<Object> lr2, List<String> lr3*/) {
		Clause clause = null;
		Clause ct = null;
	
		double m1, m2, m3 = Double.POSITIVE_INFINITY;
		
		Term temp_term = null;
		Term t[] = new Term[1];
		int p[] = new int[2];
		int attr_no = 0;
		
		//List<Double> attrs0 = new ArrayList<Double>();
		List<Integer> attrs1 = new ArrayList<Integer>(attrs);
		List<Double> attrs2;
		List<Term> attrs3;
		
		//List<Double> list1 = new ArrayList<Double>();
		//List<Object> list2 = new ArrayList<Object>();
		List<Object> lr = new ArrayList<Object>();
		
		//int l = training_set.countClass(default_class);
		
		int n = training_set.getSize();
		int k = training_set.countClass(class_name);
		
		DataSet current;
		m2 = Double.POSITIVE_INFINITY;
		current = training_set;
		clause = new Clause();
		DataSet matched;
		//boolean flag;
		// Find the best clause with the smallest m values
		for(int i = 0; i < attrs.size(); i++) {

			attrs2 = new ArrayList<Double>();
			attrs3 = new ArrayList<Term>();
			
			CalNumericRange(attrs1, current, minNum, 1);
			
			for(int j = 0; j < attrs1.size(); j++) {
				attr_no = attrs1.get(j);
				t = new Term[1];
				m1 = current.findTerm(attr_no, class_name, minNum, t);
				if(t[0] != null) {
					attrs2.add(m1);
					attrs3.add(t[0]);	
				}
			}
			
			if(attrs3.size() == 0) {
				break;
			}
			QuickSort.sort(attrs2, attrs3);
			
			for(int j = 0; j < attrs3.size(); j++) {
				
                            for(int h = j + 1; h < attrs3.size(); h++) {
                                    if(attrs2.get(j).doubleValue() == attrs2.get(h).doubleValue()) {
                                        if(current.InfoGain(attrs3.get(j).attribute_no) <
                                                current.InfoGain(attrs3.get(h).attribute_no)) {
                                            j = h;
                                        }
                                    } else {
                                        break;
                                    }
                            }
                            clause.addTerm(attrs3.get(j));
                            clause.countSelected(training_set, class_name, p);

                            if(!(p[0] >= minNum && p[0] > 0) || (n - p[1]) < 1) {
                                    clause.deleteLastTerm();
                                    continue;
                            } else {

                                    m1 = m_function(p[0], p[1], k, n);
                                    if(m1 >= m2) {
                                            clause.deleteLastTerm();
                                            continue;
                                    } else {
                                            if(chi_square(p[1], n - p[1], p[0], (n - p[1]) - (k - p[0]), k, n - k) < 3.841) {
                                                    clause.deleteLastTerm();
                                                    continue;
                                            } else {
                                                    m2 = m1;

                                            }
                                    }
                            }
                            matched = new DataSet();
                            clause.copyCovered(current, matched);
                            current = matched;
                            attrs1.remove(attrs1.indexOf(attrs3.get(j).attribute_no));
                            break;
			}
			
			if(ct != null && ct.terms.size() == clause.terms.size())
				break;
			
			ct = clause;
			m3 = m2;
			
			if(p[0] == p[1]) {
				//flag = true;
				break;
			}	
		}
		
		if(ct != null) {
			
			if(m3 == 0 && ct.terms.size() == 1) {
				m3 = -training_set.InfoGain(ct.terms.get(0).attribute_no);
			}
			
			lr.add(ct);
			lr.add(m3);	
		}
		
		return lr;
	}
	
    /**
     *
     * @param s1
     * @param s2
     * @param c_o
     * @param d_o
     * @param c
     * @param d
     * @return
     */
    static public double chi_square(int s1, int s2, int c_o, int d_o, int c, int d) {
		double sp1 = (double) s1 / (s1 + s2);
		double sp2 = (double) s2 / (s1 + s2);
		
		double c_e = c * sp1;
		double d_e = d * sp2;
		
		double chi = Math.pow((double) c_o - c_e, 2) / c_e +
				Math.pow((double) d_o - d_e, 2) / d_e;
		
		return chi;
	}
	
    /**
     *
     * @param c_o
     * @param d_o
     * @param c_e
     * @param d_e
     * @return
     */
    static public double chi_square(int c_o, int d_o, int c_e, int d_e) {
		double chi = Math.pow((double) c_o - c_e, 2) / c_e +
				Math.pow((double) d_o - d_e, 2) / d_e;
		
		return chi;
	}
	
	// Pruning method
	/*static public double chi_square(int p_t, int s_t, int p_f, int s_f) {
		
		int n_t = s_t - p_t;
		int s = s_t + s_f;
		int p = p_t + p_f;
		int n_f = s_f - p_f;
		int n = s - p;
		
		double p_t1 = s_t * (double) p/s;
		double n_t1 = s_t * (double) n/s;
		double p_f1 = s_f * (double) p/s;
		double n_f1 = s_f * (double) n/s;
		
		double chi = Math.pow((double) p_t - p_t1, 2) / p_t1 +
				Math.pow((double) p_f - p_f1, 2) / p_f1 + 
				Math.pow((double) n_t - n_t1, 2) / n_t1 +
				Math.pow((double) n_f - n_f1, 2) / n_f1;

		return chi;
	}
		
	// Pruning method
	static public double chi_square(String class_name, DataSet training_set, int p_t, int s_t) {
		int s = training_set.getSize();
		int p = training_set.countClass(class_name);
		int n = s - p;
		int n_t = s_t - p_t;
		int s_f = s - s_t;
		int p_f = p - p_t;
		int n_f = s_f - p_f;
		
		double p_t1 = s_t * (double) p/s;
		double n_t1 = s_t * (double) n/s;
		double p_f1 = s_f * (double) p/s;
		double n_f1 = s_f * (double) n/s;
		
		double chi = Math.pow((double) p_t - p_t1, 2) / p_t1 +
				Math.pow((double) p_f - p_f1, 2) / p_f1 + 
				Math.pow((double) n_t - n_t1, 2) / n_t1 +
				Math.pow((double) n_f - n_f1, 2) / n_f1;

		return chi;
	}
	
	static public boolean chi_square(DataSet training_set, Clause clause, int t, int f) {
		int s = t + f;
		List<String> lc = training_set.class_set; 
		int c[] = new int[lc.size()];
		int df = -1;
		
		for(int i = 0; i < c.length; i++) {
			c[i] = training_set.countClass(lc.get(i));
			if(c[i] != 0)
				df++;
		}
		
		DataSet matched = new DataSet();
		DataSet unmatched = new DataSet();
		clause.copyCoveredAndUncovered(training_set, matched, unmatched);
		
		double ob, ex;
		double sum = 0;
		double t_s = (double) t/s;
		double f_s = (double) f/s;
		
		for(int i = 0; i < c.length; i++) {
			if(c[i] != 0) {
				ob = matched.countClass(lc.get(i));
				ex = c[i] * t_s;
				sum += Math.pow(ob - ex, 2) / ex;
				
				ob = unmatched.countClass(lc.get(i));
				ex = c[i] * f_s;
				sum += Math.pow(ob - ex, 2) / ex;
			}
		}
		
		if(chi[df - 1] < sum)
			return true;
		else
			return false;
	}*/
	
	// Pruning method

    /**
     *
     * @param f1
     * @param n
     * @return
     */
	static public double confidence_interval(int f1, int n) {
		double f = (double) f1/n;
		double p = f + 0.67 * Math.sqrt(f * (1 - f) / n);
		return p;
	}

    /**
     *
     * @param parent_rule
     * @param default_class
     * @param attrs
     * @param training_set
     * @param falseFlag
     * @param minNum
     * @param method
     * @param parentcorrect
     * @param parentincorrect
     * @return
     */
    static public Rule make_rdr(Rule parent_rule, String default_class, List<Integer> attrs, DataSet training_set, 
			boolean falseFlag, int minNum, int method, int parentcorrect, int parentincorrect) {
		
		Rule t = new Rule();
		/*List<Double> list1 = new ArrayList<Double>();
		List<Object> list2 = new ArrayList<Object>();
		List<String> list3 = new ArrayList<String>();*/
		List<Object> list = new ArrayList<Object>();
		Clause temp_clause;
		String class_name = "";
		List<String> class_set = training_set.class_set;
		double m1, m2 = Double.POSITIVE_INFINITY;
		//boolean flag = false;
		//double b1, b2 = Double.POSITIVE_INFINITY;
		
		//CalNumericRange(attrs, training_set, minNum, method);
		
		// Find the best clause
		for(int i = 0; i < class_set.size(); i++) {
                    class_name = class_set.get(i);
                    if(!default_class.equals(class_name)) {
                        list = best_clause(class_name, default_class, attrs, training_set, minNum, parentcorrect, parentincorrect/*, list1, list2, list3*/);
                        if(list.size() != 0) {
                            temp_clause = (Clause) list.get(0);
                            m1 = (double) list.get(1);

                                if(m1 < m2){
                                        t.clause = temp_clause;
                                        t.class_name = class_name;
                                        m2 = m1;
                                }

                        }
                    }
		}

		if(t.clause.terms.size() == 0) {
			//t = FindRule(default_class, attrs, training_set, minNum);
			//if(t.clause.terms.size() == 0)
				return null;
		}
		
		//QuickSort.sort(list1, list2);
		
		//for(int id = 0; id < 2 && id < list2.size(); id++) {
		//	t = new Rule();
		//	t.clause = (Clause) list2.get(id);
		//	t.class_name = list3.get(id);

			DataSet covered = new DataSet();
			DataSet uncovered = new DataSet();
			
			t.clause.copyCoveredAndUncovered(training_set, covered, uncovered);
			t.true_no = covered.getSize();
			t.false_no = uncovered.getSize();
			t.correct_no_t = t.true_no;
			t.correct_no_f = t.false_no;
			
			// Recursively run the first subset
			if(covered.hasClassOtherThan(t.class_name)) {
				//List<Integer> a = t.clause.removeUsedAttr(attrs, training_set);
				t.correct_no_t = covered.countClass(t.class_name);
				/*if(attrs.size() != 0)*/ {
					t.if_true = make_rdr(t, t.class_name, attrs, covered, false, minNum, method, 
							t.correct_no_t, t.true_no - t.correct_no_t);
				} 
				//attrs.addAll(a);		
			}
			
			// Pruning
			if(t.if_true != null) {
				if(t.true_no - t.if_true.correct >= parentincorrect) 
					t.if_true = null;
			}
			
			// Sum up the number of correctly classified data 
			int nt_correct;
			if(t.if_true != null) {
				if(t.if_true.correct <= t.correct_no_t) {
					t.if_true = null;
					nt_correct = t.correct_no_t;
				} else {
					nt_correct = t.if_true.correct;
				}
			} else {
				nt_correct = t.correct_no_t;
			}
			
			// Recursively run the second subset
			if(uncovered.hasClassOtherThan(default_class)) {
				t.correct_no_f = uncovered.countClass(default_class);
				/*if(!(falseFlag == true && t.true_no == 0))*/ {
					t.if_false = make_rdr(t, default_class, attrs, uncovered, true, minNum, method, 
							t.correct_no_f, t.false_no - t.correct_no_f);
				} 

			}

			// Pruning
			if(t.if_false != null) {
				if(t.false_no - t.if_false.correct >= parentincorrect) 
					t.if_false = null;
			}
			
			// Sum up the number of correctly classified data 
			int nf_correct;
			if(t.if_false != null) {
				if(t.if_false.correct <= t.correct_no_f) {
					t.if_false = null;
					nf_correct = t.correct_no_f;
				} else {
					nf_correct = t.if_false.correct;
				}
			} else {
				nf_correct = t.correct_no_f;
			}
			
			t.correct = nt_correct + nf_correct;

			if(parent_rule != null) {
				String max_class = training_set.getDefaultClass();
				int max_count = training_set.default_class_count;
			
				if(max_count > t.correct) {
				
					if(falseFlag == false) {
						t.clause = new Clause();
						t.class_name = max_class;
						t.if_true = null;
						t.if_false = null;
						t.correct = max_count;
					} else {
						t.clause = new Clause();
						t.class_name = max_class;
						t.if_true = null;
						t.if_false = null;
						t.correct = max_count;						
					}
					
				}	
			}
			
			/*if(t.if_true == null && t.if_false == null) {
				int p[] = new int[2];
				int q[] = new int[2];
				t.clause.countSelected(training_set, t.class_name, p);
				t.clause.countUnselected(training_set, default_class, q);
				if(chi_square(p[1], q[1], p[0], q[0], 
						training_set.countClass(t.class_name), training_set.countClass(default_class)) < 3.841)
					return null;
			}*/
			
			/*t.ep_incorrect = new int[class_set.size()];
			t.ep_sum = new int[class_set.size()];
			
			for(int i = 0; i < class_set.size(); i++) {
				t.ep_incorrect[i] = 0;
				t.ep_sum[i] = 0;
			}
			

			if(t.if_true != null) {
				for(int i = 0; i < class_set.size(); i++) {
					t.ep_incorrect[i] += t.if_true.ep_incorrect[i];
					t.ep_sum[i] += t.if_true.ep_sum[i];
				}
			} else {
				t.ep_incorrect[class_set.indexOf(t.class_name)] += t.true_no - t.correct_no_t;
				t.ep_sum[class_set.indexOf(t.class_name)] += t.true_no;
			}
			
			if(t.if_false != null) {
				for(int i = 0; i < class_set.size(); i++) {
					t.ep_incorrect[i] += t.if_false.ep_incorrect[i];
					t.ep_sum[i] += t.if_false.ep_sum[i];
				}
			} else {
				t.ep_incorrect[class_set.indexOf(default_class)] += t.false_no - t.correct_no_f;
				t.ep_sum[class_set.indexOf(default_class)] += t.false_no;
			}

			double p1 = 0;
			//int d = 0;
			for(int i = 0; i < class_set.size(); i++) {
				if(t.ep_sum[i] != 0) {
					p1 += confidence_interval(t.ep_incorrect[i], t.ep_sum[i]) * t.ep_sum[i];
					//d++;
				}
			}
			//p1 = p1 / d;
			
			double p2 = confidence_interval(parentincorrect, parentcorrect + parentincorrect) * (parentcorrect + parentincorrect);
			
			if(p2 < p1) {
				return null;
			}*/
			
			/*if(t.if_true == null && t.if_false == null)
			{
				double p1, p2, p3;
				p1 = confidence_interval(t.true_no - nt_correct, t.true_no) * t.true_no;
				p2 = confidence_interval(t.false_no - nf_correct, t.false_no) * t.false_no;
				p3 = confidence_interval(parentincorrect, parentcorrect + parentincorrect) * (parentcorrect + parentincorrect);
				
				if(p3 < (p1 + p2) / 2)
					return null;
			}*/
			
			/*double p1, p2, p3;
			if(t.if_true == null)
				p1 = confidence_interval(t.true_no - nt_correct, t.true_no) * t.true_no;
			else
				p1 = t.if_true.error_prune;
			
			if(t.if_false == null)
				p2 = confidence_interval(t.false_no - nf_correct, t.false_no) * t.false_no;
			else
				p2 = t.if_false.error_prune;
			
			p3 = confidence_interval(parentincorrect, parentcorrect + parentincorrect) * (parentcorrect + parentincorrect);
			
			if(p3 < (p1 + p2) / 2)
				return null;
			else
				t.error_prune = (p1 + p2) / 2;*/
			

			// Record used training data at the leave nodes
			/*if(t.if_true == null) {
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
			if(t.if_false == null) {
				t.correct_f = new DataSet();
				t.incorrect_f = new DataSet();
				for(int i = 0; i < uncovered.getSize(); i++) {
					if(uncovered.getData(i).class_name.equals(default_class)) {
						t.correct_f.addData(uncovered.getData(i));
					} else {
						t.incorrect_f.addData(uncovered.getData(i));
					}
				}
			}*/
		//	break;
		//}
		
		return t;
	}
	
    /**
     *
     * @param fileName
     */
    public static void createRules(String fileName) {
		createRules(fileName, -1);
	}
	
    /**
     *
     * @param fileName
     * @param classIndex
     */
    public static void createRules(String fileName, int classIndex) {
                Logger.info("Creating Rules based on dataset...");
		fileName = fileName.replace("/", "\\");
		Path p = Paths.get(fileName);
		String path = p.toString();
		String file2 = path.substring(0, path.lastIndexOf("\\") + 1) + "rule.txt";
                
		try {
			FileReader frData = new FileReader(fileName);
			Instances data = new Instances(frData);
		
			DataSet ds = new DataSet();
		
			String class_name = ds.createDataSet(data, classIndex);
			
			inductSqliteOperation.create_table();
			inductSqliteOperation.delete_table();
                        
			Rule r = make_rdr(null, ds.getDefaultClass(), ds.getAttrNoSet(), ds, false, 2, 1,
					0, Integer.MAX_VALUE);
                        
                        Logger.info("Inserting attributes...");
                        
//			inductSqliteOperation.set_Attributes(ds.attribute, ds.type, classIndex, class_name, ds.class_set);
                        CaseLoader.inserCaseStructure(Main.domain.getCaseStructure());
                        inductSqliteOperation.set_Attributes(ds.attribute, ds.type, classIndex, class_name, ds.class_set);
                        inductSqliteOperation.set_Conclusions(ds.attribute, ds.type, classIndex, class_name, ds.class_set);
                        
                        Logger.info("Writing results into text file...");
                        
			FileWriter writer = new FileWriter(file2);
			r.outputRule(ds.getDefaultClass(), ds.attribute, "", writer);
			writer.close();
			
			inductSqliteOperation.close();
			frData.close();
			
			rule = new Rule();
			rule.clause = new Clause();
			rule.class_name = ds.getDefaultClass();
			rule.if_true = r;
                        
                        getCornerstonCases();
                        
		} catch(IOException e) {
			System.err.println(e.getMessage());
		} catch (Exception ex) {
                java.util.logging.Logger.getLogger(InductRDR.class.getName()).log(Level.SEVERE, null, ex);
            }

	}
        
    /**
     *
     */
    public static void getCornerstonCases(){
            
        }
	
    /**
     *
     * @param fileName
     */
    public static void predict(String fileName) {
		predict(fileName, -1);
	}
	
    /**
     *
     * @param fileName
     * @param classIndex
     */
    public static void predict(String fileName, int classIndex) {
		
		Path p = Paths.get(fileName);
		String path = p.toString();
                                
		String file3 = System.getProperty("user.dir") + "/domain/inductResult/" + Main.domain.getDomainName() + "_result.txt";
		
		try {
			FileReader frData = new FileReader(fileName);
			Instances data = new Instances(frData);
			
			DataSet ds1 = new DataSet();
			ds1.createDataSet(data, classIndex);
			
			FileWriter writer = new FileWriter(file3);
			Rule r = rule.if_true;
			r.test(ds1, rule.class_name, writer);
			writer.close();
			
			frData.close();
			
		} catch(IOException e) {
			System.err.println(e.getMessage());
		}
		
	}

    /**
     *
     * @param fileName
     */
    public static void readDB(String fileName) {
		
	}
	
    /**
     *
     * @param trainingFileName
     * @param testingFileName
     */
    public static void executeInductRDR(String trainingFileName, String testingFileName) {
		createRules(trainingFileName, -1);
		predict(testingFileName, -1);
	}
	
    /**
     *
     * @param args
     */
    public static void main(String [] args) {
		createRules(args[0], -1);
		predict(args[1], -1);
	}
	
	// main function

    /**
     *
     * @param args
     */
	public static void main2(String [] args)
	{
		if(args.length < 2) {
			System.err.println("Insufficient parameters.");
			System.exit(1);
		}
		
		String file = args[0];
		String file1 = args[1];
		
		file.replace("/", "\\");
		file1.replace("/", "\\");
		Path p = Paths.get(file);
		//String path = p.toAbsolutePath().toString();
		String path = p.toString();
		
		String file2 = path.substring(0, path.lastIndexOf("\\") + 1) + "rule.txt";
		String file3 = path.substring(0, path.lastIndexOf("\\") + 1) + "result.txt";
		String file4 = path.substring(0, path.lastIndexOf("\\") + 1) + "testlog.txt";
		
		int classIndex = -1;
		int minNum = -1;
		int method = 1;
		
		try {
			if(args.length > 2) {
				if("last".equals(args[2]))
					classIndex = -1;
				else
					classIndex = Integer.valueOf(args[2]);
			}
			if(args.length > 3)
				method = Integer.valueOf(args[3]);
			if(args.length > 4)
				minNum = Integer.valueOf(args[4]);
			/*if(args.length > 4)
				ratio1 = Double.valueOf(args[4]);
			if(args.length > 5)
				ratio2 = Double.valueOf(args[5]);*/
		} catch(Exception e) {
			System.err.println("Parameter 3 and 4 and 5 must be integers"
					/*+ " and Parameter 5 and 6 must be float numbers."*/);
			System.err.println(e.getMessage());
			System.exit(1);
		}

		try {
			FileReader frData = new FileReader(file);
			Instances data = new Instances(frData);
			
			DataSet ds = new DataSet();
			String class_name = ds.createDataSet(data, classIndex);
			//DataSet vd = ds.splitValidationData();
			
			if(minNum == -1) {
				/*minNum = Math.round((float) ds.getSize() / (float) 500 / ds.class_set.size());
				if(minNum < 1)
					minNum = 1;*/
				minNum = 2;
			}
			
			/*for(int i = 0; i < ds.getAttrNoSet().size(); i++) {
				ds.InfoGain(ds.getAttrNoSet().get(i));
			}*/
			
			
			Rule r = make_rdr(null, ds.getDefaultClass(), ds.getAttrNoSet(), ds, false, minNum, method,
					0, Integer.MAX_VALUE);
			
			// default class
			while (r == null) {
				r = new Rule();
				r.class_name = ds.getDefaultClass();
			}
			
			//r.validate(vd, ds.getDefaultClass());
			
			inductSqliteOperation.create_table();
			inductSqliteOperation.delete_table();
                        
			CaseLoader.caseStructureImport();
                        CaseLoader.inserCaseStructure(Main.domain.getCaseStructure());
			inductSqliteOperation.set_Attributes(ds.attribute, ds.type, classIndex, class_name, ds.class_set);
			
			FileWriter writer = new FileWriter(file2);
			r.outputRule(ds.getDefaultClass(), ds.attribute, "", writer);
			writer.close();
			
			inductSqliteOperation.close();
			
			
			frData.close();
			frData = new FileReader(file1);
			data = new Instances(frData);
			
			DataSet ds1 = new DataSet();
			ds1.createDataSet(data, classIndex);
			
			writer = new FileWriter(file3);
			r.test(ds1, ds.getDefaultClass(), writer);
			writer.close();
			
			writer = new FileWriter(file4);
			r.writeTestLog(ds.getDefaultClass(), ds.attribute, "", writer);
			writer.close();
			frData.close();
			
		} catch(IOException e) {
			System.err.println(e.getMessage());
		} catch (Exception ex) {
                java.util.logging.Logger.getLogger(InductRDR.class.getName()).log(Level.SEVERE, null, ex);
            }

	}
	
	/*static Rule FindRule(String default_class, List<Integer> attrs, DataSet training_set, 
			int minNum, double ratio1, double ratio2) {
		Rule t = new Rule();
		Clause temp_clause;
		String class_name;
		List<String> class_set = training_set.class_set;
		double m1, m2 = Double.MAX_VALUE;
		
		int type;
		DataRecord record;
		NumericRange nr = new NumericRange();
		List<Integer> attrs1 = new ArrayList<Integer>();

		int nclass[] = new int[training_set.class_set.size()];
		int fclass[] = new int[training_set.class_set.size()];
		int gclass[] = new int[training_set.class_set.size()];

		for(int i = 0; i < attrs.size(); i++) {
			type = training_set.type.get(attrs.get(i));
			
			if(type == DataSet.INTEGER || type == DataSet.DOUBLE) {
				
				attrs1.add(attrs.get(i));
				List<Double> listd = new ArrayList<Double>();
				List<String> listClass = new ArrayList<String>();
				for(int j = 0; j < training_set.getSize(); j++) {
					record = training_set.getData(j);
					listd.add(Double.valueOf(record.value.get(attrs.get(i))));
					listClass.add(record.class_name);
				}
				
				QuickSort.sort(listd, listClass);

				String pclass = null;
				List<Object> lists = new ArrayList<Object>();
				List<Object> liste = new ArrayList<Object>();
				List<String> listc = new ArrayList<String>();
				
				for(int k = 0; k < class_set.size(); k++) {
					nclass[k] = 0;
					fclass[k] = 1;
					gclass[k] = 0;
				}
				
				if(type == DataSet.INTEGER) {
					
					int start = Integer.MIN_VALUE, end, current = Integer.MIN_VALUE;
					int previous = Integer.MIN_VALUE;
					for(int j = 0; j < listd.size(); j++) {
						current = (int) listd.get(j).doubleValue();
						if(start == Integer.MIN_VALUE)
							start = current;
						class_name = listClass.get(j);
						if(previous == Integer.MIN_VALUE || previous == current) {
							nclass[class_set.indexOf(class_name)]++;
						} else {
							int max = 0;
							for(int k = 0; k < class_set.size(); k++) {
								max += nclass[k];
							}
							
							if(max >= minNum) {
								max = -1;
								for(int k = 0; k < class_set.size(); k++) {
									if(nclass[k] == max) {
										gclass[k] = 1;
									} else if(nclass[k] > max) {
										for(int l = 0; l < class_set.size(); l++) {
											gclass[l] = 0;
										}
										gclass[k] = 1;
										max = nclass[k];
									}
									nclass[k] = 0;
								}
								max = 0;
								for(int l = 0; l < class_set.size(); l++) {
									if(fclass[l] == 1 && gclass[l] == 1) {
										fclass[l] = 1;
										pclass = class_set.get(l);
										max++;
									} else {
										fclass[l] = 0;
									}
								}
								if(max >= 1) {
									if(lists.size() == 0) {
										lists.add(Integer.MIN_VALUE);
										liste.add(current);
										listc.add(pclass);
									} else {
										liste.set(liste.size() - 1, current);
										listc.set(listc.size() - 1, pclass);
									}
								} else {
									if(start != Integer.MIN_VALUE)
										end = ((int) liste.get(liste.size() - 1) + start) / 2;
									else
										end = ((int) liste.get(liste.size() - 1) + current) / 2;
									liste.set(liste.size() - 1, end);
									lists.add(end + 1);
									liste.add(current);
									
									for(int l = 0; l < class_set.size(); l++) {
										if(gclass[l] == 1) {
											pclass = class_set.get(l);
										}
										fclass[l] = gclass[l];
									}
									listc.add(pclass);
								}
								for(int l = 0; l < class_set.size(); l++) {
									gclass[l] = 0;
								}
								start = Integer.MIN_VALUE;
							} else {
								start = current;
							}
						}
						previous = current;
					}
					if(lists.size() == 0)
						lists.add(Integer.MIN_VALUE);
					else {
						if(start != Integer.MIN_VALUE)
							end = ((int) liste.get(liste.size() - 1) + start) / 2;
						else
							end = ((int) liste.get(liste.size() - 1) + current) / 2;
						liste.set(liste.size() - 1, end);
						lists.add(end + 1);
					}
					liste.add(Integer.MAX_VALUE);
					int max = -1;
					for(int k = 0; k < class_set.size(); k++) {
						if(nclass[k] > max) {
							pclass = class_set.get(k);
							max = nclass[k];
						} 
					}
					listc.add(pclass);
				} else {
					
					double start = Double.NEGATIVE_INFINITY, end, current = Double.NEGATIVE_INFINITY;
					double previous = Double.NEGATIVE_INFINITY;
					for(int j = 0; j < listd.size(); j++) {
						current = listd.get(j).doubleValue();
						if(start == Double.NEGATIVE_INFINITY)
							start = current;
						class_name = listClass.get(j);
						if(previous == Double.NEGATIVE_INFINITY || previous == current) {
							nclass[class_set.indexOf(class_name)]++;
						} else {
							int max = 0;
							for(int k = 0; k < class_set.size(); k++) {
								max += nclass[k];
							}
							
							if(max >= minNum) {
								max = -1;
								for(int k = 0; k < class_set.size(); k++) {
									if(nclass[k] == max) {
										gclass[k] = 1;
									} else if(nclass[k] > max) {
										for(int l = 0; l < class_set.size(); l++) {
											gclass[l] = 0;
										}
										gclass[k] = 1;
										max = nclass[k];
									}
									nclass[k] = 0;
								}
								max = 0;
								for(int l = 0; l < class_set.size(); l++) {
									if(fclass[l] == 1 && gclass[l] == 1) {
										fclass[l] = 1;
										pclass = class_set.get(l);
										max++;
									} else {
										fclass[l] = 0;
									}
								}
								if(max >= 1) {
									if(lists.size() == 0) {
										lists.add(Double.NEGATIVE_INFINITY);
										liste.add(current);
										listc.add(pclass);
									} else {
										liste.set(liste.size() - 1, current);
										listc.set(listc.size() - 1, pclass);
									}
								} else {
									if(start != Double.NEGATIVE_INFINITY)
										end = ((double) liste.get(liste.size() - 1) + start) / 2;
									else
										end = ((double) liste.get(liste.size() - 1) + current) / 2;
									liste.set(liste.size() - 1, end);
									lists.add(end);
									liste.add(current);
									
									for(int l = 0; l < class_set.size(); l++) {
										if(gclass[l] == 1) {
											pclass = class_set.get(l);
										}
										fclass[l] = gclass[l];
									}
									listc.add(pclass);
								}
								for(int l = 0; l < class_set.size(); l++) {
									gclass[l] = 0;
								}
								start = Double.NEGATIVE_INFINITY;
							} else {
								start = current;
							}
						}
						previous = current;
					}
					if(lists.size() == 0)
						lists.add(Double.NEGATIVE_INFINITY);
					else {
						if(start != Double.NEGATIVE_INFINITY)
							end = ((double) liste.get(liste.size() - 1) + start) / 2;
						else
							end = ((double) liste.get(liste.size() - 1) + current) / 2;
						liste.set(liste.size() - 1, end);
						lists.add(end);
					}
					liste.add(Double.POSITIVE_INFINITY);
					int max = -1;
					for(int k = 0; k < class_set.size(); k++) {
						if(nclass[k] > max) {
							pclass = class_set.get(k);
							max = nclass[k];
						} 
					}
					listc.add(pclass);
				}		
								
				nr.start.add(lists);
				nr.end.add(liste);
				nr.attrIndex.add(attrs.get(i));
				nr.type.add(type);
				nr.class_name.add(listc);
			}
		}
		
		if(attrs.size() != 0) {
			//NumericRange nr1 = training_set.nr;
			training_set.nr = nr;
			for(int i = 0; i < class_set.size(); i++) {
				class_name = class_set.get(i);
				if(!default_class.equals(class_name)) {
					temp_clause = best_clause(class_name, attrs, training_set, minNum, ratio1, ratio2);
					if(temp_clause != null && temp_clause.terms.size() != 0) {
						m1 = m_function(temp_clause, training_set, class_name);
						if(t.clause.terms.size() == 0 || m1 < m2){
							t.clause = temp_clause;
							t.class_name = class_name;
							m2 = m1;
						}
					}
				}
			}
			//training_set.nr = nr1;
		}
		return t;
	}*/
	
	// Split a numeric attribute into different ranges. 0: multi split 1: binary split(default)
	static void CalNumericRange(List<Integer> attrs, DataSet training_set, int minNum, int method) {
		
		int type;
		DataRecord record;
		List<String> class_set = training_set.class_set;
		
		int nclass[] = new int[training_set.class_set.size()];
		
		String class_name = "";
		NumericRange nr = new NumericRange();
		
		// multi split
		if(method == 0) {
			for(int i = 0; i < attrs.size(); i++) {
				type = training_set.type.get(attrs.get(i));
				if(type == DataSet.INTEGER || type == DataSet.DOUBLE) {
				
					List<Double> listd = new ArrayList<Double>();
					List<String> listClass = new ArrayList<String>();
					for(int j = 0; j < training_set.getSize(); j++) {
						record = training_set.getData(j);
						listd.add(Double.valueOf(record.value.get(attrs.get(i))));
						listClass.add(record.class_name);
					}
				
					QuickSort.sort(listd, listClass);
		
				
					List<Object> lists = new ArrayList<Object>();
					List<Object> liste = new ArrayList<Object>();
					List<String> listc = new ArrayList<String>();
				
				
					String pclass = null;
					for(int k = 0; k < class_set.size(); k++) {
						nclass[k] = 0;
					}
					
					double start = Double.NEGATIVE_INFINITY , end, current = Double.NEGATIVE_INFINITY;
					double previous = Double.NEGATIVE_INFINITY;
					int num = 0;
					int maxid;
					int maxct;
					int flag = 0;
					int min;
					
					if(type == DataSet.INTEGER)
						min = minNum;
					else
						min = minNum;
					
					for(int j = 0; j <= listd.size(); j++) {
						if(j < listd.size()) {
							current = listd.get(j).doubleValue();
							if(start == Double.NEGATIVE_INFINITY)
								start = current;
							class_name = listClass.get(j);
						}
						
						if(flag == 2 || j == listd.size() || previous != Double.NEGATIVE_INFINITY && previous != current
								&& num >= min) {
							maxid = maxct = -1;
							for(int k = 0; k < class_set.size(); k++) {
								if(nclass[k] > maxct) {
									maxct = nclass[k];
									maxid = k;
								} else if (nclass[k] == maxct) {
									maxid = -1;
								}
							}
							
							if(maxid != -1) {
								pclass = class_set.get(maxid);
								if(listc.size() == 0) {
									if(type == DataSet.INTEGER)
										lists.add(Integer.MIN_VALUE);
									else
										lists.add(Double.NEGATIVE_INFINITY);
									if(j == listd.size()) {
										if(type == DataSet.INTEGER)
											liste.add(Integer.MAX_VALUE);
										else
											liste.add(Double.POSITIVE_INFINITY);
									}
									else {
										if(type == DataSet.INTEGER)
											liste.add((int) current);
										else
											liste.add(current);
									}
									listc.add(pclass);
								} else if(listc.get(listc.size() - 1).equals(pclass)) {
									if(j == listd.size()) {
										if(type == DataSet.INTEGER)
											liste.set(liste.size() - 1, Integer.MAX_VALUE);
										else
											liste.set(liste.size() - 1, Double.POSITIVE_INFINITY);
									} else {
										if(type == DataSet.INTEGER)
											liste.set(liste.size() - 1, (int) current);
										else
											liste.set(liste.size() - 1, current);
									}
								} else {
									if(type == DataSet.INTEGER) {
										end = ((int) liste.get(liste.size() - 1) + (int) start) / 2;
										liste.set(liste.size() - 1, (int) end);
									} else {
										end = ((double) liste.get(liste.size() - 1) + start) / 2;
										liste.set(liste.size() - 1, end);
									}
									
									if(j == listd.size()) {
										break;
									} else {
										if(type == DataSet.INTEGER) {
											lists.add((int) end);
											liste.add((int) current);
										} else {
											lists.add(end);
											liste.add(current);
										}
									}
									listc.add(pclass);
								}
								start = current;
								num = 0;
								for(int k = 0; k < class_set.size(); k++) {
									nclass[k] = 0;
								}
							}
							
						}
						nclass[class_set.indexOf(class_name)]++;
						previous = current;
						num++;
					}

					nr.start.add(lists);
					nr.end.add(liste);
					nr.attrIndex.add(attrs.get(i));
					nr.type.add(type);
					nr.class_name.add(listc);
				}
			}
			
			//Infogain (Binary split)
		} else {
			
			for(int i = 0; i < attrs.size(); i++) {
				
				type = training_set.type.get(attrs.get(i));
				
				if(type == DataSet.INTEGER || type == DataSet.DOUBLE) {
					
					/*List<Object> lists = new ArrayList<Object>();
					List<Object> liste = new ArrayList<Object>();
					List<String> listc = new ArrayList<String>();
					
					double split = training_set.infoSplit.get(attrs.get(i));
					List<Integer> list1 = (List<Integer>) (training_set.infoPart.get(attrs.get(i)));
					
					for(int j = 0; j < class_set.size(); j++) {
						
						int part = list1.get(j);
						class_name = class_set.get(j);
						
						if(type == DataSet.INTEGER) {
							if(part == 0) {
								lists.add(Integer.MIN_VALUE);
								liste.add((int) split);
								listc.add(class_name);
							} else {
								lists.add((int) split);
								liste.add(Integer.MAX_VALUE);
								listc.add(class_name);
							}
						} else {
							if(part == 0) {
								lists.add(Double.NEGATIVE_INFINITY);
								liste.add(split);
								listc.add(class_name);
							} else {
								lists.add(split);
								liste.add(Double.POSITIVE_INFINITY);
								listc.add(class_name);
							}
						}
					}*/
					
					List<Double> listd = new ArrayList<Double>();
					List<String> listClass = new ArrayList<String>();
					String strValue;
					
					for(int j = 0; j < training_set.getSize(); j++) {
						record = training_set.getData(j);
						strValue = record.value.get(attrs.get(i));
						if(!"?".equals(strValue)) {
							listd.add(Double.valueOf(strValue));
							listClass.add(record.class_name);
						}
					}	
					
					if(listd.size() == 0)
						continue;
					
					QuickSort.sort(listd, listClass);

					List<Object> lists = new ArrayList<Object>();
					List<Object> liste = new ArrayList<Object>();
					List<String> listc = new ArrayList<String>();
					List<Double> listi = new ArrayList<Double>();
			
					
					
					/*double current, next;
					double value, value1, value2, value3, max;
					double ct = 0, nt = 0, md;
					
					int ctleft[] = new int[class_set.size()];
					int ctright[] = new int[class_set.size()];
					int ctlefta[] = new int[class_set.size()];
					int ctrighta[] = new int[class_set.size()];
					
					for(int j = 0; j < class_set.size(); j++) {
						ctleft[j] = ctright[j] = 0;
					}
					
					for(int j = 0; j < listClass.size() - 1; j++) {
						ctright[class_set.indexOf(listClass.get(j))]++;
					}
					
					value = 0;
					for(int j = 0; j < class_set.size(); j++) {
						value1 = (double) ctright[j] / listClass.size();
						if(value1 == 0) value1 = 1;
						value -= value1 * Math.log(value1) / Math.log(2);
						
					}
					
					max = Double.NEGATIVE_INFINITY;

					for(int j = 0; j < listd.size() - 1; j++) {
						
						int k1 = class_set.indexOf(listClass.get(j));
						ctleft[k1]++;
						ctright[k1]--;
						
						if(j + 1 >= 1 && listd.size() - j - 1 >= 1) {
							current = listd.get(j).doubleValue();
							next = listd.get(j + 1).doubleValue();

							if(current == next)
								continue;
							
							value3 = value;
							value2 = 0;
							for(int k = 0; k < class_set.size(); k++) {
								value1 = (double) ctleft[k] / (j + 1);
								if(value1 == 0) value1 = 1;
								value2 += value1 * Math.log(value1) / Math.log(2);
							}
							
							value2 *= - (double) (j + 1) / listd.size();
							value3 -= value2;
							
							value2 = 0;
							for(int k = 0; k < class_set.size(); k++) {
								value1 = (double) ctright[k] / (listd.size() - j - 1);
								if(value1 == 0) value1 = 1;
								value2 += value1 * Math.log(value1) / Math.log(2);
							}
							
							value2 *= - (double) (listd.size() - j - 1) / listd.size();
							value3 -= value2;
							
							if(value3 > max) {
								max = value3;
								ct = current;
								nt = next;
								
								for(int k = 0; k < class_set.size(); k++) {
									ctlefta[k] = ctleft[k];
									ctrighta[k] = ctright[k];
								}
								
							}
						}
					}
					
					if(max != Double.NEGATIVE_INFINITY) {
						
						md = (ct + nt) / 2;
						
						int lefta = 0;
						int righta = 0;
						for(int j = 0; j < class_set.size(); j++) {
							lefta += ctlefta[j];
							righta += ctrighta[j];
						}
						
						for(int j = 0; j < class_set.size(); j++) {
							class_name = class_set.get(j);
							if(type == DataSet.INTEGER) {
								if((double) ctlefta[j] / lefta > (double) ctrighta[j] / righta) {
									lists.add(Integer.MIN_VALUE);
									liste.add((int) md);
									listc.add(class_name);
									listi.add(max);
								} else {
									lists.add((int) md);
									liste.add(Integer.MAX_VALUE);
									listc.add(class_name);
									listi.add(max);	
								}
							} else {
								if((double) ctlefta[j] / lefta > (double) ctrighta[j] / righta) {
									lists.add(Double.NEGATIVE_INFINITY);
									liste.add(md);
									listc.add(class_name);
									listi.add(max);	
								} else {
									lists.add(md);
									liste.add(Double.POSITIVE_INFINITY);
									listc.add(class_name);
									listi.add(max);	
								}
							}
						}
					}*/
					
					for(int c = 0; c < class_set.size(); c++) {
						class_name = class_set.get(c);
						
						double current, next;
						double value, value1, value2, valuel, valuer, valuefi, max;
						double ct = 0, nt = 0, md;
						
						int ct1eft[] = new int[2];
						int ctright[] = new int[2];
						int ct1efta[] = new int[2];
						int ctrighta[] = new int[2];
						
						for(int j = 0; j < 2; j++) {
							ct1eft[j] = ctright[j] = 0;
						}
						
						for(int j = 0; j < listClass.size() - 1; j++) {
							if(listClass.get(j).equals(class_name)) {
								ctright[0]++;
							} else {
								ctright[1]++;
							}
						}
						
						value1 = (double) ctright[0] / (double) listClass.size();
						value2 = (double) ctright[1] / (double) listClass.size();
						if(value1 == 0) value1 = 1;
						if(value2 == 0) value2 = 1;
						value = - (value1 * Math.log(value1) / Math.log(2) + value2 * Math.log(value2) / Math.log(2));
						max = Double.NEGATIVE_INFINITY;

						for(int j = 0; j < listd.size() - 1; j++) {
							
							if(listClass.get(j).equals(class_name)) {
								ct1eft[0]++;
								ctright[0]--;
							} else {
								ct1eft[1]++;
								ctright[1]--;
							}
							
							if(j + 1 >= 1 && listd.size() - j - 1 >= 1) {
								current = listd.get(j).doubleValue();
								next = listd.get(j + 1).doubleValue();

								if(current == next)
									continue;
								
								
								/*if(listClass.get(j).equals(listClass.get(j + 1))) {
									boolean flag = true;
									int k = j + 2;
									for(; k < listd.size(); k++) {
										if(listd.get(j + 1).doubleValue() != listd.get(k).doubleValue())
											break;
										if(!listClass.get(j).equals(listClass.get(k))) {
											flag = false;
											break;
										}
									}
									if(flag == true) {
										if(listClass.get(j).equals(class_name)) {
											ct1eft[0] += k - j - 2;
											ctright[0] -= k - j - 2;
										} else {
											ct1eft[1] += k - j - 2;
											ctright[1] -= k - j - 2;
										}
										j = k - 2;
										continue;
									}
								}*/
								
								
								value1 = (double) ct1eft[0] / (double) (j + 1);
								value2 = (double) ct1eft[1] / (double) (j + 1);
								if(value1 == 0) value1 = 1;
								if(value2 == 0) value2 = 1;
								valuel = (double) (j + 1) / (double) listd.size() * 
										(-(value1 * Math.log(value1) / Math.log(2) + value2 * Math.log(value2) / Math.log(2)));
								
								value1 = (double) ctright[0] / (double) (listd.size() - j - 1);
								value2 = (double) ctright[1] / (double) (listd.size() - j - 1);
								if(value1 == 0) value1 = 1;
								if(value2 == 0) value2 = 1;
								
								valuer = (double) (listd.size() - j - 1) / (double) listd.size() * 
										(-(value1 * Math.log(value1) / Math.log(2) + value2 * Math.log(value2) / Math.log(2)));
								
								valuefi = value - (valuel + valuer);
								
								if(valuefi > max) {
									max = valuefi;
									ct = current;
									nt = next;
									
									ct1efta[0] = ct1eft[0];
									ct1efta[1] = ct1eft[1];
									ctrighta[0] = ctright[0];
									ctrighta[1] = ctright[1];
									
								} 
							}
							
						}
						
						if(max != Double.NEGATIVE_INFINITY) {
							
							int s_t = ct1efta[0] + ct1efta[1];
							int p_t = ct1efta[0];
							int s_f = ctrighta[0] + ctrighta[1];
							int p_f = ctrighta[0];

							md = (ct + nt) / 2;
							
							if((double) p_t / s_t > (double) p_f / s_f) {
								{
									if(type == DataSet.INTEGER) {
										lists.add(Integer.MIN_VALUE);
										liste.add((int) md);
										listc.add(class_name);
										listi.add(max);
									} else {
										lists.add(Double.NEGATIVE_INFINITY);
										liste.add(md);
										listc.add(class_name);
										listi.add(max);
									}	
								}
							} else {
								 {
									if(type == DataSet.INTEGER) {
										lists.add((int) md);
										liste.add(Integer.MAX_VALUE);
										listc.add(class_name);
										listi.add(max);
									} else {
										lists.add(md);
										liste.add(Double.POSITIVE_INFINITY);
										listc.add(class_name);
										listi.add(max);
									}
								}
							}
							
						}

					}
					
					nr.start.add(lists);
					nr.end.add(liste);
					nr.attrIndex.add(attrs.get(i));
					nr.type.add(type);
					nr.class_name.add(listc);
					nr.infoGain.add(listi);
				}
			}
		}
		training_set.nr = nr;
	}

	static double InfoGain(Term term, DataSet training_set, String class_name) {
		Clause clause = new Clause();
		clause.addTerm(term);
		
		DataSet matched = new DataSet();
		DataSet unmatched = new DataSet();
		clause.copyCoveredAndUncovered(training_set, matched, unmatched);
		
		double value, value1, value2, valuel, valuer;
		
		int ct1eft[] = new int[2];
		int ctright[] = new int[2];

		ct1eft[0] = matched.countClass(class_name);
		ct1eft[1] = matched.getSize() - ct1eft[0];
		ctright[0] = unmatched.countClass(class_name);
		ctright[1] = unmatched.getSize() - ctright[0];
		
		value1 = training_set.countClass(class_name);
		value2 = training_set.getSize() - training_set.countClass(class_name);
		
		value1 = value1 / training_set.getSize();
		value2 = value2 / training_set.getSize();
		if(value1 == 0) value1 = 1;
		if(value2 == 0) value2 = 1;
		value = - (value1 * Math.log(value1) / Math.log(2) + value2 * Math.log(value2) / Math.log(2));

		value1 = (double) ct1eft[0] / matched.getSize();
		value2 = (double) ct1eft[1] / matched.getSize();
		if(value1 == 0) value1 = 1;
		if(value2 == 0) value2 = 1;
		valuel = (double) matched.getSize() / training_set.getSize() * 
				(-(value1 * Math.log(value1) / Math.log(2) + value2 * Math.log(value2) / Math.log(2)));
				
		value1 = (double) ctright[0] / unmatched.getSize();
		value2 = (double) ctright[1] / unmatched.getSize();
		if(value1 == 0) value1 = 1;
		if(value2 == 0) value2 = 1;
				
		valuer = (double) unmatched.getSize() / training_set.getSize() * 
				(-(value1 * Math.log(value1) / Math.log(2) + value2 * Math.log(value2) / Math.log(2)));
				
		return value - (valuel + valuer);
				
	}
	
	static double InfoGain(Clause clause, DataSet training_set, String class_name) {
		
		DataSet matched = new DataSet();
		DataSet unmatched = new DataSet();
		clause.copyCoveredAndUncovered(training_set, matched, unmatched);
		
		double value, value1, value2, valuel, valuer;
		
		int ct1eft[] = new int[2];
		int ctright[] = new int[2];

		ct1eft[0] = matched.countClass(class_name);
		ct1eft[1] = matched.getSize() - ct1eft[0];
		ctright[0] = unmatched.countClass(class_name);
		ctright[1] = unmatched.getSize() - ctright[0];
		
		value1 = training_set.countClass(class_name);
		value2 = training_set.getSize() - training_set.countClass(class_name);
		
		value1 = value1 / training_set.getSize();
		value2 = value2 / training_set.getSize();
		if(value1 == 0) value1 = 1;
		if(value2 == 0) value2 = 1;
		value = - (value1 * Math.log(value1) / Math.log(2) + value2 * Math.log(value2) / Math.log(2));

		value1 = (double) ct1eft[0] / matched.getSize();
		value2 = (double) ct1eft[1] / matched.getSize();
		if(value1 == 0) value1 = 1;
		if(value2 == 0) value2 = 1;
		valuel = (double) matched.getSize() / training_set.getSize() * 
				(-(value1 * Math.log(value1) / Math.log(2) + value2 * Math.log(value2) / Math.log(2)));
				
		value1 = (double) ctright[0] / unmatched.getSize();
		value2 = (double) ctright[1] / unmatched.getSize();
		if(value1 == 0) value1 = 1;
		if(value2 == 0) value2 = 1;
				
		valuer = (double) unmatched.getSize() / training_set.getSize() * 
				(-(value1 * Math.log(value1) / Math.log(2) + value2 * Math.log(value2) / Math.log(2)));
				
		return value - (valuel + valuer);
				
	}
	
	static double InfoGain(int attr, DataSet training_set) {
		DataRecord record;
		List<String> class_set = training_set.class_set;
		

		List<String> listValue = new ArrayList<String>();
		List<Integer> listNumber = new ArrayList<Integer>();
		List<Object> listNumberClass = new ArrayList<Object>();
		int classCount[];
		int allClassNo = class_set.size();
		String str;
		for(int j = 0; j < training_set.getSize(); j++) {
			record = training_set.getData(j);
			str = record.value.get(attr);
			if(!"?".equals(str)) {
				if(listValue.indexOf(str) == -1) {
					listValue.add(str);
					listNumber.add(1);
					classCount = new int[allClassNo];
					for(int k = 0; k < allClassNo; k++) {
						classCount[k] = 0;
					}
					classCount[class_set.indexOf(record.class_name)] = 1;
					listNumberClass.add(classCount);
				} else {
					int k = listValue.indexOf(str);
					listNumber.set(k, listNumber.get(k) + 1);
					classCount = (int[]) listNumberClass.get(k);
					classCount[class_set.indexOf(record.class_name)]++;
				}
			}
			
		}
		
		double value = 0;
		double value1, value2;
		
		for(int j = 0; j < class_set.size(); j++) {
			value1 = (double) training_set.countClass(class_set.get(j)) / training_set.getSize();
			if(value1 == 0) value1 = 1;
			value -= value1 * Math.log(value1) / Math.log(2); 
		}
		
		for(int j = 0; j < listValue.size(); j++) {
			classCount = (int[]) listNumberClass.get(j);
			value2 = 0;
			for(int k = 0; k < classCount.length; k++) {
				value1 = (double) classCount[k] / listNumber.get(j);
				if(value1 == 0) value1 = 1;
				value2 += value1 * Math.log(value1) / Math.log(2);
			}
			value2 *= - (double) listNumber.get(j) / training_set.getSize();
			value -= value2;
		}
		
		return value;
	}
}
