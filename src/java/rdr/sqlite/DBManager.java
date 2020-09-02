/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.sqlite;

import rdr.logger.Logger;

/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class DBManager {

    /**
     *
     * @param dbName
     */
    public static void initialise(String dbName, String path){
        Logger.info("KB DATABASES BEING CREATED FOR:" + dbName);
        Logger.info("KB DATABASES PATH:" + path);
        SqliteConnection.connect(dbName, path);        
        SqliteBasicOperation.domainDetailTableCreate();
        SqliteBasicOperation.ruleStructureTableCreate();
        SqliteBasicOperation.ruleConditionsTableCreate();
        SqliteBasicOperation.ruleConclusionTableCreate();
        SqliteBasicOperation.ruleCornerstonesTableCreate();
        SqliteBasicOperation.caseStructureTableCreate();
        SqliteBasicOperation.ruleCornerstoneInferenceResultTableCreate();
        SqliteBasicOperation.categoricalValuesTableCreate();

    }
    
}
    

