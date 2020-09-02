/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.reasoner;


/**
 *
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class ReasonerFactory {

    /**
     *
     */
    protected static String classPath = "rdr.reasoner.";

    /**
     *
     * @param name
     * @return
     */
    public static IReasoner createReasoner(String name) {
        name = name.toUpperCase();
        IReasoner reasoner = null;
        try {
            reasoner = (IReasoner) Class.forName(classPath + name + "Reasoner").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            
        }
        return reasoner;
    }
        
}
