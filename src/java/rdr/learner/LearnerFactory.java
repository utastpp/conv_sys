/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rdr.learner;

/**
 * This class is used to define knowledge acquisition process
 * 
 * @author Hyunsuk (David) Chung (DavidChung89@gmail.com)
 */
public class LearnerFactory {

    /**
     *
     */
    protected static String classPath = "rdr.learner.";

    /**
     *
     * @param name
     * @return
     */
    public static ILearner createLearner(String name) {
        name = name.toUpperCase();
        ILearner learner = null;
        try {
            learner = (ILearner) Class.forName(classPath + name + "Learner").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            
        }
        return learner;
    }
    
}
