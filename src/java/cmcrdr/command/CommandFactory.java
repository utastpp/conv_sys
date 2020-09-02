/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.command;

/**
 *
 * @author hchung
 */
public class CommandFactory {

    /**
     *
     */
    protected static String classPath = "cmcrdr.command.";

    /**
     *
     * @param name
     * @return
     */
    public static ICommandInstance createCommandInstance(String name) {
        if(name.equals("")){
            name = "";
        } else if(name.equals("KA")){
            name = "KA";
        } 
        
        ICommandInstance reasoner = null;
        try {
            reasoner = (ICommandInstance) Class.forName(classPath + name + "CommandInstance").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            
        }
        return reasoner;
    }
        
}
