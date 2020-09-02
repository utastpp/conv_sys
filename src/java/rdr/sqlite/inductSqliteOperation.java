/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import rdr.apps.Main;
import rdr.logger.Logger;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class inductSqliteOperation {
    
	static Connection connect = SqliteConnection.connection;
	
	static String cr_tb_cond = "CREATE TABLE IF NOT EXISTS `tb_rule_conditions` (" +
								"`condition_id` integer PRIMARY KEY AUTOINCREMENT," +
								"`rule_id` integer NOT NULL," +
								"`attribute_id` integer NOT NULL," +
								"`operator_id` integer NOT NULL," +
								"`condition_value` varchar(255) DEFAULT NULL," +
//								"`condition_type` varchar(255) DEFAULT NULL," +
//								"`case_id` integer DEFAULT NULL," +
								"`creation_date` timestamp DEFAULT CURRENT_TIMESTAMP" +
								");" ;
	
	static String cr_tb_tree1 = "CREATE TABLE IF NOT EXISTS `tb_rule_structure1` (" +
							"`rule_id` integer PRIMARY KEY AUTOINCREMENT," +
							"`alter_id` integer DEFAULT NULL," +
							"`exception_id` integer DEFAULT NULL," +
							"`conclusion_id` integer NOT NULL," +
							"`creation_date` timestamp DEFAULT CURRENT_TIMESTAMP" +
							");";
	
	static String cr_tb_tree = "CREATE TABLE IF NOT EXISTS `tb_rule_structure` (" +
							"`rule_id` integer PRIMARY KEY AUTOINCREMENT," +
							"`parentrule_id` integer NOT NULL," +
							"`conclusion_id` integer NOT NULL," +
							"`creation_date` timestamp DEFAULT CURRENT_TIMESTAMP" +
							");";
	
	static String cr_tb_conclusion = "CREATE TABLE IF NOT EXISTS `tb_rule_conclusion` (" +
							"`conclusion_id` integer PRIMARY KEY AUTOINCREMENT," +
							"`value_type_id` INTEGER NOT NULL, " +
							"`conclusion_name` varchar(255) DEFAULT NULL," +
							"`creation_date` timestamp DEFAULT CURRENT_TIMESTAMP" +
							");";
	
	static String cr_tb_attribute = "CREATE TABLE IF NOT EXISTS `tb_case_structure` (" +
			 						"`attribute_id` INTEGER NOT NULL,"  +
			 						"`value_type_id` INTEGER NOT NULL," +
			 						"`attribute_name` VARCHAR(255) NULL " +
			 						");";
			
	static String cr_tb_nominal = "CREATE TABLE IF NOT EXISTS `tb_categorical_value` (" +
									"`categorical_value_id` INTEGER PRIMARY KEY   AUTOINCREMENT, " +
									"`attribute_id` INTEGER NOT NULL, " +
									"`value_name` VARCHAR(255) NULL " +
									")";
	
	static String dl_tb_cond = "DELETE from `tb_rule_conditions`;";
	static String dl_tb_tree1 = "DELETE from `tb_rule_structure1`;";
	static String dl_tb_tree = "DELETE from `tb_rule_structure`;";
	static String dl_tb_conclusion = "DELETE from `tb_rule_conclusion`;";
	static String dl_tb_attribute = "DELETE from `tb_case_structure`;";
	static String dl_tb_nominal = "DELETE from `tb_categorical_value`;";
	static String dl_seq = "DELETE from `sqlite_sequence`;";
	
	static String is_tb_cond = "INSERT INTO `tb_rule_conditions`" +
			//"(`condition_id`,`rule_id`,`attribute_name`,`attribute_value`,`operator_id`,`condition_type`) " +
			"(`condition_id`,`rule_id`,`attribute_id`,`operator_id`,`condition_value`) " +
			"VALUES($next_id,?, ?, ?, ?);";
	
	static String is_tb_tree1 = "INSERT INTO `tb_rule_structure1`" +
			"(`rule_id`,`alter_id`,`exception_id`,`conclusion_id`) " +
			"VALUES($next_id,?, ?, ?);";
	
	static String is_tb_tree2 = "INSERT INTO `tb_rule_structure1`" +
			"(`rule_id`,`conclusion_id`) VALUES($next_id,?);";
	
	static String is_tb_tree = "INSERT INTO `tb_rule_structure`" +
			"(`rule_id`,`parentrule_id`,`conclusion_id`) VALUES($next_id,?,?);";
	
	static String is_tb_conclusion = "INSERT INTO `tb_rule_conclusion`" +
			"(`conclusion_id`,`value_type_id`,`conclusion_name`) VALUES($next_id,1,?);";
	
	static String is_tb_attribute = "INSERT INTO `tb_case_structure`" +
			"(`attribute_id`,`value_type_id`,`attribute_name`) VALUES(?,?,?);";
	
	static String is_tb_nominal = "INSERT INTO `tb_categorical_value`" +
			"(`categorical_value_id`,`attribute_id`,`value_name`) VALUES($next_id,?,?);";
	
	static String ud_tb_tree1 = "UPDATE `tb_rule_structure1`" +
			"set `alter_id` = ? where `rule_id` = ?;";

	static String ud_tb_tree2 = "UPDATE `tb_rule_structure1`" +
			"set `exception_id` = ? where `rule_id` = ?;";
	
	static String q_max_id = "SELECT ifnull(MAX(`rule_id`), 0) AS max_id FROM `tb_rule_structure1`";
	
	
	
	static List<String> class_set;
	static List<String> attributes;
	static List<Integer> types;
	
	static List<Integer> attributes_id;
	static List<String> operator;
	static List<Integer> attr_type;
	
	static Statement stmt;
	static PreparedStatement pstmt;
	
    /**
     *
     */
    public static void init() {
		operator = new ArrayList<String>();
		String[] op = {"", "=", "!=", "<", ">", "<=", ">="};
		for(int i = 0; i < 7; i++) {
			operator.add(op[i]);
		}
		
		attr_type = new ArrayList<Integer>();
		int[] tp = {1, 0, 0};
		for(int i = 0; i < 3; i++) {
			attr_type.add(tp[i]);
		}
		
		try {
			Class.forName("org.sqlite.JDBC");
                        connect = (Connection) DriverManager.getConnection("jdbc:sqlite:" + System.getProperty("user.dir") + "/domain/"+ Main.domain.getDomainName() +".db" );
	    } catch ( Exception e ) {
		    Logger.error( e.getClass().getName() + ": " + e.getMessage() );
		    System.exit(0);
	    }
	    Logger.info("Opened database successfully");
	}
	
    /**
     *
     */
    public static void create_table() {
	    try {
	    	stmt = connect.createStatement();
	    	stmt.executeUpdate(cr_tb_cond);
	    	stmt.executeUpdate(cr_tb_tree1);
	    	stmt.executeUpdate(cr_tb_tree);
	    	stmt.executeUpdate(cr_tb_conclusion);
	    	stmt.executeUpdate(cr_tb_attribute);
	    	stmt.executeUpdate(cr_tb_nominal);
	    } catch ( Exception e ) {
	    	Logger.error( e.getClass().getName() + ": " + e.getMessage() );
	    	System.exit(0);
	    }
	    Logger.info("Table created successfully");
	}

    /**
     *
     */
    public static void delete_table() {
	    try {
	    	stmt = connect.createStatement();
	    	stmt.executeUpdate(dl_tb_cond);
	    	stmt.executeUpdate(dl_tb_tree1);
	    	stmt.executeUpdate(dl_tb_tree);
	    	stmt.executeUpdate(dl_tb_conclusion);
	    	stmt.executeUpdate(dl_tb_attribute);
	    	stmt.executeUpdate(dl_tb_nominal);
	    	stmt.executeUpdate(dl_seq);
	    } catch ( Exception e ) {
	    	Logger.error( e.getClass().getName() + ": " + e.getMessage() );
	    	System.exit(0);
	    }
	    Logger.info("Table deleted successfully");
	}
	
    /**
     *
     * @param rule_id
     * @param attribute_id
     * @param attribute_value
     * @param operator_id
     */
    public static void insert_tb_cond(int rule_id, int attribute_id, String attribute_value,
			String operator_id) {
		try {
			pstmt = connect.prepareStatement(is_tb_cond);
			pstmt.setInt(2, rule_id);
			pstmt.setInt(3, attributes_id.indexOf(attribute_id));
			pstmt.setInt(4, operator.indexOf(operator_id));
			pstmt.setString(5, attribute_value);
			pstmt.executeUpdate();
		} catch ( Exception e ) {
			Logger.error( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		Logger.info("Table tb_cond inserted successfully");
	}
	
    /**
     *
     * @param alter_id
     * @param exception_id
     * @param conclusion_id
     */
    public static void insert_tb_tree(int alter_id, int exception_id, int conclusion_id) {
		try {
			pstmt = connect.prepareStatement(is_tb_tree1);
			pstmt.setInt(2, alter_id);
			pstmt.setInt(3, exception_id);
			pstmt.setInt(4, conclusion_id);
			pstmt.executeUpdate();
		} catch ( Exception e ) {
			Logger.error( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		Logger.info("Table tb_tree inserted successfully");
	}
	
    /**
     *
     * @param parent_id
     * @param conclusion_id
     */
    public static void insert_tb_tree(int parent_id, int conclusion_id) {
		try {
			pstmt = connect.prepareStatement(is_tb_tree2);
			pstmt.setInt(2, conclusion_id);
			pstmt.executeUpdate();
			pstmt = connect.prepareStatement(is_tb_tree);
			pstmt.setInt(2, parent_id);
			pstmt.setInt(3, conclusion_id);
			pstmt.executeUpdate();
		} catch ( Exception e ) {
			Logger.error( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		Logger.info("Table tb_tree inserted successfully");
	}
	
    /**
     *
     * @param conclusion_id
     */
    public static void insert_tb_tree_root(int conclusion_id) {
		try {
			pstmt = connect.prepareStatement(is_tb_tree2);
			pstmt.setInt(1, 0);
			pstmt.setInt(2, conclusion_id);
			pstmt.executeUpdate();
			pstmt = connect.prepareStatement(is_tb_tree);
			pstmt.setInt(1, 0);
			pstmt.setInt(2, -1);
                        
                        /**
                         * modified code...to add default conclusion_id as 0.
                         * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
                         */
                        /** BEGIN **/
//			pstmt.setInt(3, conclusion_id);
                                         
                            pstmt.setInt(3, 0);
                            pstmt.executeUpdate();

                            // update tb_rule_structure of default conclusion_id
                            pstmt = connect.prepareStatement("UPDATE `tb_rule_structure` SET `conclusion_id` = 0 WHERE `conclusion_id` = ?");
                            pstmt.setInt(1, conclusion_id);
                            pstmt.executeUpdate();

                            // update tb_rule_structure of default conclusion_id
                            pstmt = connect.prepareStatement("UPDATE `tb_rule_conclusion` SET `conclusion_id` = 0 WHERE `conclusion_id` = ?");
                            pstmt.setInt(1, conclusion_id);
                            pstmt.executeUpdate();
                        /** END **/
		} catch ( Exception e ) {
			Logger.error( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		Logger.info("Table tb_tree inserted successfully");
	}
	
    /**
     *
     * @param alter_id
     * @param exception_id
     * @param conclusion
     */
    public static void insert_tb_tree(int alter_id, int exception_id, String conclusion) {
		insert_tb_tree(alter_id, exception_id, class_set.indexOf(conclusion));
	}
	
    /**
     *
     * @param parent_id
     * @param conclusion
     */
    public static void insert_tb_tree(int parent_id, String conclusion) {
		if(parent_id == -1)
			insert_tb_tree_root(class_set.indexOf(conclusion));
		else
			insert_tb_tree(parent_id, class_set.indexOf(conclusion));
	}
	
    /**
     *
     * @param alter_id
     * @param rule_id
     */
    public static void update_alter(int alter_id, int rule_id) {
		try {
			pstmt = connect.prepareStatement(ud_tb_tree1);
			pstmt.setInt(1, alter_id);
			pstmt.setInt(2, rule_id);
			pstmt.executeUpdate();
		} catch ( Exception e ) {
			Logger.error( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		Logger.info("Table alter_id updated successfully");
	}
	
    /**
     *
     * @param exception_id
     * @param rule_id
     */
    public static void update_exception(int exception_id, int rule_id) {
		try {
			pstmt = connect.prepareStatement(ud_tb_tree2);
			pstmt.setInt(1, exception_id);
			pstmt.setInt(2, rule_id);
			pstmt.executeUpdate();
		} catch ( Exception e ) {
			Logger.error( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		Logger.info("Table exception_id updated successfully");
	}
	
    /**
     *
     * @return
     */
    public static int select_max_ruleId() {
		int id = 0;
		try {
			stmt = connect.createStatement();
			ResultSet rs = stmt.executeQuery(q_max_id);
			id = rs.getInt("max_id");
			rs.close();
	    
		} catch ( Exception e ) {
			Logger.error( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		Logger.info("next ruleId selected successfully");
		return id;
	}
	
    /**
     *
     */
    public static void close() {
		try {
			stmt.close();
			pstmt.close();
		    connect.close();
	    } catch ( Exception e ) {
		    Logger.error( e.getClass().getName() + ": " + e.getMessage() );
		    System.exit(0);
	    }
	    Logger.info("Closed database successfully");
	}

    /**
     *
     * @param attributes1
     * @param types1
     * @param class_index
     * @param class_name
     * @param class_set1
     */
    public static void set_Attributes(List<String> attributes1, List<Integer> types1, 
			int class_index, String class_name, List<String> class_set1) {
		attributes = new ArrayList<String>();
		attributes.addAll(attributes1);
		types = new ArrayList<Integer>();
		types.addAll(types1);
		
		attributes_id = new ArrayList<Integer>();
		for(int i = 0; i < attributes.size(); i++) {
			attributes_id.add(i);
		}
		
		int size = attributes.size() + 1;
		if(class_index == -1)
			class_index = size - 1;
		
		try {
			for(int i = 0; i < size; i++) {
//                                System.out.println("Setting Attributes...");
				pstmt = connect.prepareStatement(is_tb_attribute);
				pstmt.setInt(1, i);
				if(i != class_index) {
//                                        System.out.println("Setting Attributes...class_index");
					pstmt.setInt(2, attr_type.get(types.get(i)));
					pstmt.setString(3, attributes.get(i));
					
					if(attr_type.get(types.get(i)) == 1) {
//                                                System.out.println("Setting Attributes...attr_type");
//						pstmt.executeUpdate();
						
//						pstmt = connect.prepareStatement(is_tb_nominal);
//						pstmt.setInt(2, i);
//						pstmt.setString(3, attributes.get(i));
						
					}
					
				} else {
					types.add(i, 1);
					attributes.add(i, class_name);
					attributes_id.add(i, class_index);
					pstmt.setInt(2, 1);
					pstmt.setString(3, class_name);
//					pstmt.executeUpdate();
					
//					pstmt = connect.prepareStatement(is_tb_nominal);
//					pstmt.setInt(2, class_index);
//					pstmt.setString(3, attributes.get(i));
					
				}
//				pstmt.executeUpdate();
			}
		} catch ( Exception e ) {
			Logger.error( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}

		Logger.info("Table tb_case_structure inserted successfully");
        }
        
    /**
     *
     * @param attributes1
     * @param types1
     * @param class_index
     * @param class_name
     * @param class_set1
     */
    public static void set_Conclusions(List<String> attributes1, List<Integer> types1, 
			int class_index, String class_name, List<String> class_set1) {
		
		class_set = new ArrayList<String>();
		class_set.addAll(class_set1);
		
		try {
			for(int i = 0; i < class_set.size(); i++) {
				pstmt = connect.prepareStatement(is_tb_conclusion);
				pstmt.setString(2, class_set.get(i));
				pstmt.executeUpdate();
			}
		} catch ( Exception e ) {
			Logger.error( e.getClass().getName() + ": " + e.getMessage() );
			System.exit(0);
		}
		class_set.add(0, "");
		Logger.info("Table tb_rule_conclusion inserted successfully");
	}

    /**
     *
     * @param fileName
     */
    public static void read(String fileName) {
		operator = new ArrayList<String>();
		String[] op = {"", "=", "!=", "<", ">", "<=", ">="};
		for(int i = 0; i < 7; i++) {
			operator.add(op[i]);
		}
		
		attr_type = new ArrayList<Integer>();
		int[] tp = {1, 0, 0};
		for(int i = 0; i < 3; i++) {
			attr_type.add(tp[i]);
		}
		
		try {
			Class.forName("org.sqlite.JDBC");
		    connect = DriverManager.getConnection("jdbc:sqlite:" + fileName);
	    } catch ( Exception e ) {
		    Logger.error( e.getClass().getName() + ": " + e.getMessage() );
		    System.exit(0);
	    }
	    Logger.info("Opened database successfully");
	}
}
