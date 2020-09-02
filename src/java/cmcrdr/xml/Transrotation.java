/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.xml;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import cmcrdr.logger.Logger;

/**
 *
 * @author dherbert
 */
@XmlType( propOrder = { "name","command"} )
@XmlRootElement( name = "TRANSROTATION" )
public class Transrotation {


    private String name;
    private String command;

    public String getName()
    {
        return name;
    }

    @XmlElement( name = "TRANSROTATION_NAME" )
    public void setName( String name )
    {
        this.name = name;
    }

//    Boolean childrenAllowed;
//
//    public Boolean getChildrenAllowed() {
//        return childrenAllowed;
//    }

//    @XmlAttribute( name = "children_allowed", required = true )
//    public void setChildrenAllowed( Boolean childrenAllowed )
//    {
//        this.childrenAllowed = childrenAllowed;
//    }
    
    public String getCommand()
    {
        return command;
    }
        
    @XmlElement( name = "COMMAND" )
    public void setCommand( String command )
    {
        this.command = command;


    }



    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder( "Name: " + getName() + "\n" );
        str.append("Command: ").append(getCommand()).append("\n");

        return str.toString();
    }
}
