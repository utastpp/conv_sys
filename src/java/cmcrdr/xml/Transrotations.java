/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.xml;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author dherbert
 */
@XmlRootElement( name = "TRANSROTATIONS" )
public class Transrotations {

    List<Transrotation> transrotations;

    public List<Transrotation> getTransrotations()
    {
        return transrotations;
    }

    /**
     * element that is going to be marshaled in the xml
     * @param transrotations
     */
    @XmlElement( name = "TRANSROTATION" )
    public void setTransrotations( List<Transrotation> transrotations )
    {
        this.transrotations = transrotations;
    }

    /**
     * This method is not used by jaxb, just used for business reasons. 
     * @param transrotation
     */
    public void add( Transrotation transrotation )
    {
        if( this.transrotations == null )
        {
            this.transrotations = new ArrayList<Transrotation>();
        }
        this.transrotations.add( transrotation );
 }

    @Override
    public String toString()
    {
        StringBuilder str = new StringBuilder();
        str.append("Robot XML capabilities read from file:\n");
        for( Transrotation transrotation : this.transrotations )
        {
            str.append( transrotation.toString() );
        }
        return str.toString();
    }

}

