/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cmcrdr.xml;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import java.io.File;
import cmcrdr.logger.Logger;
/**
 *
 * @author dherbert
 */


public class TransrotationsBuilder {
    
    public static Transrotations getTransrotationCapabilities(String path)
    {
        Transrotations capabilities = null;
        
        try
        {

            File file = new File( path + "/domain/transrotations.xml" );
            JAXBContext jaxbContext = JAXBContext.newInstance( Transrotations.class );


            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();

            capabilities = (Transrotations)jaxbUnmarshaller.unmarshal( file );

            System.out.println( capabilities );

        }
        catch( JAXBException e )
        {
            e.printStackTrace();
        }
        catch (Exception e2) {
            e2.printStackTrace();
        }
        
        return capabilities;

    }

}

