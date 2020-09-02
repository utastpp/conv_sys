package rdr.inductrdr;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import weka.core.Attribute;
import weka.core.Instance;
import weka.core.Instances;

// Store the whole dataset for training and test

/**
 *
 * @author dherbert
 */
public class DataSet {
	static int NOMINAL = 0;
	static int INTEGER = 1;
	static int DOUBLE = 2;
	List<DataRecord> records;
	List<String> attribute;
	List<Integer> type;
	List<String> class_set;
	String default_class;
	int default_class_count;
	NumericRange nr;
	List<Integer> infoAttr;
	List<Double> infoGain;
	//List<Double> infoSplit;
	
	// Constructors
	DataSet() {
		records = new ArrayList<DataRecord>();
		attribute = new ArrayList<String>();
		type = new ArrayList<Integer>();
		class_set = new ArrayList<String>();
		nr = new NumericRange();
		infoAttr = new ArrayList<Integer>();
		infoGain = new ArrayList<Double>();
	}
	
	DataSet(List<DataRecord> r, List<String> a, List<Integer> t) {
		records = r;
		attribute = a;
		type = t;
		class_set = new ArrayList<String>();
		nr = new NumericRange();
		infoAttr = new ArrayList<Integer>();
		infoGain = new ArrayList<Double>();
	}
	
	List<Integer> getAttrNoSet() {
		List<Integer> list = new ArrayList<Integer>();
		for(int i = 0; i < attribute.size(); i++) {
			list.add(i);
		}
		return list;
	}
	
	String getDefaultClass() {
		String default_class = null;
		List<Integer> class_count = new ArrayList<Integer>();
		
		for(int j = 0; j < class_set.size(); j++) {
			class_count.add(0);
		}
			
		for(int i = 0; i < records.size(); i++) {
			for(int j = 0; j < class_set.size(); j++)
				if(records.get(i).class_name == class_set.get(j))
					class_count.set(j, class_count.get(j) + 1);
		}
		
		int max = 0;
		for(int j = 0; j < class_set.size(); j++) {
			if(max < class_count.get(j))
			{
				max = class_count.get(j);
				default_class = class_set.get(j);
			}
		}
		
		default_class_count = max;
		this.default_class = default_class;
		
		return default_class;		
	}
	
	void addData(DataRecord d) {
		records.add(d);
	}
	
	DataRecord getData(int i) {
		return records.get(i);
	}
	
	int getSize() {
		return records.size();
	}
	
	int countClass(String class_name) {
		int sum = 0;
		for(int i = 0; i < records.size(); i++)
			if(class_name.equals(records.get(i).class_name))
				sum++;
		return sum;
	}
	
	boolean hasClassOtherThan(String class_name) {
		DataRecord record;
		boolean flag = false;
		for(int j = 0; j < getSize(); j++) {
			record = getData(j);
			if(!class_name.equals(record.class_name))
				flag = true;
			if(class_set.indexOf(record.class_name) == -1)
				class_set.add(record.class_name);
		}
		return flag;
	}
	
	void aggregateClass() {
		DataRecord record;
		class_set = new ArrayList<String>();
		for(int j = 0; j < getSize(); j++) {
			record = getData(j);
			if(class_set.indexOf(record.class_name) == -1)
				class_set.add(record.class_name);
		}
	}
	
	double findTerm(int attr, String class_name, int minNum, Term rTerm[]) {
		DataRecord record;
		int type = this.type.get(attr);
		List<String> valueList = new ArrayList<String>();
		double min = 0, max = 0;
		
		if(type != NOMINAL) {
			max = Double.MIN_EXPONENT;
			min = Double.MAX_EXPONENT;
		} 
		
		for(int j = 0; j < getSize(); j++) {
			record = getData(j);
			if(class_name.equals(record.class_name)) {
				String value = record.value.get(attr);
				if(!"?".equals(value)) {
					if(type == NOMINAL) {
						
						if(valueList.indexOf(value) == -1) {
							valueList.add(value);
						}
					} else {
						double i = Double.valueOf(value);
						
						if(min > i) {
							min = i;
						}
						if(max < i) {
							max = i;
						}
					}

	
				}
			}
		}
		if(type == NOMINAL) {
			double mind = Double.POSITIVE_INFINITY;
			for(int i = 0; i < valueList.size(); i++) {
				Clause clause = new Clause();
				Term term = new Term(attr, "=", valueList.get(i));
				clause.addTerm(term);
				
				double m = InductRDR.m_function(clause, this, class_name);
				
				if(m == 0)
					m = -InductRDR.InfoGain(term, this, class_name);
				
				if(mind > m) {
					rTerm[0] = term;
					mind = m;
				}
				
			}
			if(mind <= 0)
				mind = -InfoGain(attr);

			return mind;
			/*double mind = Double.POSITIVE_INFINITY;
			List<Double> attrs2 = new ArrayList<Double>();
			List<String> attrs3 = new ArrayList<String>();
			for(int i = 0; i < valueList.size(); i++) {
				Clause clause = new Clause();
				Term term = new Term(attr, "=", valueList.get(i));
				clause.addTerm(term);
				
				double m = Rdr.m_function(clause, this, class_name);
				if(m == 0)
					m = -Rdr.InfoGain(term, this, class_name);
				
				attrs2.add(m);
				attrs3.add(valueList.get(i));
			}
			
			QuickSort.sort(attrs2, attrs3);
			
			Term term = new Term(attr, "Contains", "");
			String str = "";
			boolean flag = false;
			for(int i = 0; i < attrs3.size(); i++) {
				str = term.value;
				if(!term.value.equals(""))
					term.value += ",";
				term.value += attrs3.get(i);
				
				Clause clause = new Clause();
				clause.addTerm(term);
				
				double m = Rdr.m_function(clause, this, class_name);

				if(m >= mind) {
					flag = true;
					term.value = str;
				} else
					mind = m;
			}

			if(flag == false && valueList.size() > 1) {
				term.value = str;
				Clause clause = new Clause();
				clause.addTerm(term);
				mind = Rdr.m_function(clause, this, class_name);
			}
			
			if(!term.value.equals("")) {
				if(term.value.indexOf(",") == -1) {
					term.relation = "=";
				}
				
				rTerm[0] = term;	
			}

			if(mind == 0)
				mind = -InfoGain(attr);

			return mind;*/
		} else {
			if(min == max) {
				return 0;
				/*Clause clause = new Clause();
				Term term;
				if(type == INTEGER)
					term = new Term(attr, "=", String.valueOf((int)min));
				else
					term = new Term(attr, "=", String.valueOf(min));
				clause.addTerm(term);
				
				double m = Rdr.m_function(clause, this, class_name);
				
				if(m == 0) {
					m = -InfoGain(attr);
				}

				rTerm[0] = term;
				return m;*/
			} else {
				int index = nr.attrIndex.indexOf(attr);
				
				double mind = Double.POSITIVE_INFINITY;
				
				if(index != -1) {
					List<Object> lists = (List<Object>) nr.start.get(index);
					List<Object> liste = (List<Object>) nr.end.get(index);
					List<Object> listc = (List<Object>) nr.class_name.get(index);
					
					if(type == INTEGER) {
						int start, end;
						for(int i = 0; i < lists.size(); i++) {
							if(class_name.equals((String)listc.get(i))) {
								start = (int) lists.get(i);
								end = (int) liste.get(i);
								
								if(!(min > end || start > max)) {
									Clause clause = new Clause();
									Term term;
									if(start == Integer.MIN_VALUE && end == Integer.MAX_VALUE)
										term = new Term(attr, "All values", "");
									else if(start == Integer.MIN_VALUE && end != Integer.MAX_VALUE)
										term = new Term(attr, "<=", String.valueOf(end));
									else if(end == Integer.MAX_VALUE && start != Integer.MIN_VALUE)
										term = new Term(attr, ">", String.valueOf(start));
									else if(start != end) {
										term = new Term(attr, "Range", start + "," + end);}
									else
										term = new Term(attr, "=", String.valueOf(start));
									clause.addTerm(term);
									
									double m = InductRDR.m_function(clause, this, class_name);
									
									/*if(m == 0) {
										int p[] = new int[2];
										clause.countSelected(this, class_name, p);
										m = - (double) p[0] / p[1];
									}*/
									/*if(m == Double.POSITIVE_INFINITY)
										m = Integer.MAX_VALUE;*/
									if(m == 0) {
										//m = - ((List<Double>) nr.infoGain.get(index)).get(i);
										m = -InfoGain(attr);
										//m = -Rdr.InfoGain2(attr, this);
									}
									
									if(mind > m) {
										rTerm[0] = term;
										mind = m;
									}
								}
							}
						}
					} else {
						double start, end;
						
						for(int i = 0; i < lists.size(); i++) {
							if(class_name.equals((String)listc.get(i))) {
								start = (double) lists.get(i);
								end = (double) liste.get(i);
								
								if(!(min > end || start > max)) {
									Clause clause = new Clause();
									Term term;
									if(start == Double.NEGATIVE_INFINITY && end == Double.POSITIVE_INFINITY)
										term = new Term(attr, "All values", "");
									else if(start == Double.NEGATIVE_INFINITY && end != Double.POSITIVE_INFINITY)
										term = new Term(attr, "<=", String.valueOf(end));
									else if(end == Double.POSITIVE_INFINITY && start != Double.NEGATIVE_INFINITY)
										term = new Term(attr, ">", String.valueOf(start));
									else if(start != end)
										term = new Term(attr, "Range", start + "," + end);
									else
										term = new Term(attr, "=", String.valueOf(start));
									clause.addTerm(term);
									
									double m = InductRDR.m_function(clause, this, class_name);
									
									/*if(m == 0) {
										int p[] = new int[2];
										clause.countSelected(this, class_name, p);
										m = - (double) p[0] / p[1];
									}*/
									/*if(m == Double.POSITIVE_INFINITY)
										m = Integer.MAX_VALUE;*/
									if(m == 0) {
										//m = - ((List<Double>) nr.infoGain.get(index)).get(i);
										m = -InfoGain(attr);
										//m = -Rdr.InfoGain2(attr, this);
									}
									
									if(mind > m) {
										rTerm[0] = term;
										mind = m;
									}
								}
							}
						}	
					}
					
				}
				
				return mind;
				
			}
		}
	}
	
	/*void findTerm(int attr, String class_name, int minNum, List<Term> termlist) {
		DataRecord record;
		int type = this.type.get(attr);
		List<String> valueList = new ArrayList<String>();
		double min = 0, max = 0;
		
		if(type != STRING) {
			max = Double.MIN_EXPONENT;
			min = Double.MAX_EXPONENT;
		} 
		
		for(int j = 0; j < getSize(); j++) {
			record = getData(j);
			if(class_name.equals(record.class_name)) {
				if(type == STRING) {
					String value = record.value.get(attr);
					if(valueList.indexOf(value) == -1) {
						valueList.add(value);
					}
				} else {
					double i = Double.valueOf(record.value.get(attr));
					
					if(min > i) {
						min = i;
					}
					if(max < i) {
						max = i;
					}
				}

			}
		}
		if(type == STRING) {
			for(int i = 0; i < valueList.size(); i++) {
				Term term = new Term(attr, "=", valueList.get(i));
				termlist.add(term);
			}
			if(termlist.size() == 1)
				termlist.remove(0);
			return;
		} else {
			if(min == max) {
				return;
			} else {
				int index = nr.attrIndex.indexOf(attr);
				List<Object> lists = (List<Object>) nr.start.get(index);
				List<Object> liste = (List<Object>) nr.end.get(index);
				List<Object> listc = (List<Object>) nr.class_name.get(index);
				
				if(type == INTEGER) {
					int start, end;
					for(int i = 0; i < lists.size(); i++) {
						if(class_name.equals((String)listc.get(i))) {
							start = (int) lists.get(i);
							end = (int) liste.get(i);
							
							if(!(min > end || start > max)) {
								Term term;
								if(start == Integer.MIN_VALUE && end == Integer.MAX_VALUE)
									return;
								else if(start == Integer.MIN_VALUE && end != Integer.MAX_VALUE)
									term = new Term(attr, "<=", String.valueOf(end));
								else if(end == Integer.MAX_VALUE && start != Integer.MIN_VALUE)
									term = new Term(attr, ">", String.valueOf(start));
								else if(start != end) {
									term = new Term(attr, "Range", start + "," + end);}
								else
									term = new Term(attr, "=", String.valueOf(start));
								
								termlist.add(term);
							}
						}
					}
				} else {
					double start, end;
					
					for(int i = 0; i < lists.size(); i++) {
						if(class_name.equals((String)listc.get(i))) {
							start = (double) lists.get(i);
							end = (double) liste.get(i);
							
							if(!(min > end || start > max)) {
								Term term;
								if(start == Double.NEGATIVE_INFINITY && end == Double.POSITIVE_INFINITY)
									return;
								else if(start == Double.NEGATIVE_INFINITY && end != Double.POSITIVE_INFINITY)
									term = new Term(attr, "<=", String.valueOf(end));
								else if(end == Double.POSITIVE_INFINITY && start != Double.NEGATIVE_INFINITY)
									term = new Term(attr, ">", String.valueOf(start));
								else if(start != end)
									term = new Term(attr, "Range", start + "," + end);
								else
									term = new Term(attr, "=", String.valueOf(start));

								termlist.add(term);
							}
						}
					}	
				}
				return;
			}
		}
	}*/
	
	String createDataSet(Instances data, int classIndex) {
		Enumeration<Attribute> enumAttr = data.enumerateAttributes();
		List<Attribute> list = Collections.list(enumAttr);
		List<String> listAttr = new ArrayList<String>();
		List<Integer> listType = new ArrayList<Integer>();
		
		Enumeration<Instance> enumInst = data.enumerateInstances();
		List<Instance> list1 = Collections.list(enumInst);
		List<DataRecord> listRecord = new ArrayList<DataRecord>();
		
		int type;
		boolean flag;
		
		if(classIndex == -1)
			classIndex = list.size() - 1;
		
		List<String> listClass = new ArrayList<String>();
		for(int j = 0; j < list1.size(); j++) {
			listClass.add(list1.get(j).stringValue(classIndex));
		}
		
		for(int i = 0; i < list.size(); i++) {
		
			listAttr.add(list.get(i).name());
			type = list.get(i).type();
			if(type == Attribute.NUMERIC) {
				flag = false;
				//List<Double> listd = new ArrayList<Double>();
				for(int j = 0; j < list1.size(); j++) {
					if(flag == false) {
						if("NaN".equals(String.valueOf(list1.get(j).value(i))))
							continue;
						if(list1.get(j).value(i) != (double)(int)list1.get(j).value(i)) {
							flag = true; 
							break;
						}
					}
					//listd.add(list1.get(j).value(i));
				}
				
				/*List<String> listClass1 = new ArrayList<String>(listClass);
				QuickSort.sort(listd, listClass1);

				String pclass = null;
				String class_name;
				List<Object> lists = new ArrayList<Object>();
				List<Object> liste = new ArrayList<Object>();
				List<String> listc = new ArrayList<String>();*/
				
				if(flag == false) {
					listType.add(INTEGER);
					
					/*int start = 0, end = 0, current;
					int previous = 0;
					for(int j = 0; j < listd.size(); j++) {
						current = (int) listd.get(j).doubleValue();
						class_name = listClass1.get(j);
						if(pclass == null || previous != current && !class_name.equals(pclass)) {
							if(pclass != null) {
								lists.add(start);
								liste.add(end);
								listc.add(pclass);
							}
							
							start = current;
							end = current;
							pclass = class_name;
						} else {
							end = current;
						}
						previous = current;
					}
					lists.add(start);
					liste.add(end);
					listc.add(pclass);*/

				}
				else {
					listType.add(DOUBLE);
					
					/*double start = 0, end = 0, current;
					double previous = 0;
					for(int j = 0; j < listd.size(); j++) {
						current = listd.get(j).doubleValue();
						class_name = listClass1.get(j);
						if(pclass == null || previous != current && !class_name.equals(pclass)) {
							if(pclass != null) {
								lists.add(start);
								liste.add(end);
								listc.add(pclass);
							}
							
							start = current;
							end = current;
							pclass = class_name;
						} else {
							end = current;
						}
						previous = current;
					}
					lists.add(start);
					liste.add(end);
					listc.add(pclass);*/
				}
				
				/*nr.start.add(lists);
				nr.end.add(liste);
				nr.attrIndex.add(i);
				nr.type.add(listType.get(listType.size() - 1));
				nr.class_name.add(listc);*/
				
			} else if(type == Attribute.NOMINAL || type == Attribute.STRING) {
				listType.add(NOMINAL);
			} 
		}
		
		int i, j;
		for(i = 0; i < list1.size(); i++) {
			List<String> listInst = new ArrayList<String>();
			flag = true;
			for(j = 0; j < listType.size(); j++) {
				if(j == classIndex)
					continue;
				if(listType.get(j).equals(NOMINAL)) {
					if("?".equals(list1.get(i).stringValue(j))) {
						listInst.add("?");
					} else {
						listInst.add(list1.get(i).stringValue(j));	
					}
					
				}
				else if(listType.get(j).equals(DOUBLE)) {
					if("NaN".equals(String.valueOf(list1.get(i).value(j)))) {
						listInst.add("?");
					} else {
						listInst.add(String.valueOf(list1.get(i).value(j)));	
					}
					
				} else {
					if("NaN".equals(String.valueOf(list1.get(i).value(j)))) {
						listInst.add("?");
					} else {
						listInst.add(String.valueOf((int)list1.get(i).value(j)));	
					}
				}
				//System.out.println(listInst.get(listInst.size() - 1));
			}
			if(flag == true) {
				DataRecord d = new DataRecord(listInst, listClass.get(i));
				listRecord.add(d);
			}
		}
		
		String class_name = listAttr.get(classIndex);
		listAttr.remove(classIndex);
		listType.remove(classIndex);

		this.records = listRecord;
		this.attribute = listAttr;
		this.type = listType;
		aggregateClass();
		return class_name;
	}
	
	DataSet splitValidationData() {
		DataSet dataset = new DataSet();
		int size = getSize() / 3;
		for(int i = 0; i < size; i++) {
			dataset.addData(this.getData(i));
			this.records.remove(i);
		}
		dataset.attribute = this.attribute;
		dataset.type = this.type;
		dataset.class_set = this.class_set;
		return dataset;
	}
	
    /**
     *
     * @param attr
     * @return
     */
    public double InfoGain(int attr) {
		if(infoAttr.indexOf(attr) != -1) {
			return infoGain.get(infoAttr.indexOf(attr));
		}
		
		double info;
		
		if(type.get(attr).equals(DataSet.NOMINAL)) {
			DataRecord record;
			List<String> class_set = this.class_set;

			List<String> listValue = new ArrayList<String>();
			List<Integer> listNumber = new ArrayList<Integer>();
			List<Object> listNumberClass = new ArrayList<Object>();
			int classCount[];
			int allClassNo = class_set.size();
			String str;
			for(int j = 0; j < this.getSize(); j++) {
				record = this.getData(j);
				str = record.value.get(attr);
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
			
			double value = 0;
			double value1, value2;
			
			for(int j = 0; j < class_set.size(); j++) {
				value1 = (double) this.countClass(class_set.get(j)) / this.getSize();
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
				value2 *= - (double) listNumber.get(j) / this.getSize();
				value -= value2;
			}
			
			info = value;
			
		} else {
						
			DataRecord record;
			List<Double> listd = new ArrayList<Double>();
			List<String> listClass = new ArrayList<String>();
			String strValue;
			for(int j = 0; j < this.getSize(); j++) {
				record = this.getData(j);
				strValue = record.value.get(attr);
				if(!"?".equals(strValue)) {
					listd.add(Double.valueOf(strValue));
					listClass.add(record.class_name);
				}
			}	
			
			QuickSort.sort(listd, listClass);
			
			double current, next;
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
			
			max = 0;

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
			
			info = max;
		}
		
		
		infoAttr.add(attr);
		infoGain.add(info);
		return info;
	}
	
    /**
     *
     * @param term
     * @return
     */
    public double InfoGain(Term term) {
		Clause clause = new Clause();
		clause.addTerm(term);
		
		DataSet matched = new DataSet();
		DataSet unmatched = new DataSet();
		clause.copyCoveredAndUncovered(this, matched, unmatched);
		
		double value, value1, value2;
		
		int ctleft[] = new int[class_set.size()];
		int ctright[] = new int[class_set.size()];

		for(int i = 0; i < class_set.size(); i++) {
			ctleft[i] = matched.countClass(class_set.get(i));
			ctright[i] = unmatched.countClass(class_set.get(i));
		}
		
		value = 0;
		for(int j = 0; j < class_set.size(); j++) {
			value1 = (double) (ctleft[j] + ctright[j]) / getSize();
			if(value1 == 0) value1 = 1;
			value -= value1 * Math.log(value1) / Math.log(2);
			
		}

		value2 = 0;
		for(int k = 0; k < class_set.size(); k++) {
			value1 = (double) ctleft[k] / matched.getSize();
			if(value1 == 0) value1 = 1;
			value2 += value1 * Math.log(value1) / Math.log(2);
		}
		
		value2 *= - (double) matched.getSize() / getSize();
		value -= value2;
		
		value2 = 0;
		for(int k = 0; k < class_set.size(); k++) {
			value1 = (double) ctright[k] / unmatched.getSize();
			if(value1 == 0) value1 = 1;
			value2 += value1 * Math.log(value1) / Math.log(2);
		}
		
		value2 *= - (double) unmatched.getSize() / getSize();
		value -= value2;

		return value;
	}
}
