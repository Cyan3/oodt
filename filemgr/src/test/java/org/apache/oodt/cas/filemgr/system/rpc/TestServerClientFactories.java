package org.apache.oodt.cas.filemgr.system.rpc;

import junit.framework.TestCase;
import org.apache.oodt.cas.filemgr.system.FileManagerServer;
import org.apache.oodt.cas.filemgr.util.RpcCommunicationFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.Properties;

/**
 * Created by radu on 7/10/15.
 */
public class TestServerClientFactories extends TestCase {

    FileManagerServer fm;


    public void setProprieties(){

        //get all properties
        Properties properties = new Properties(System.getProperties());

        // first load the example configuration
        try {
            URL filemgrPropertiesUrl = this.getClass().getResource(
                    "/filemgr.properties");

            properties.load(new FileInputStream(new File(filemgrPropertiesUrl.getFile())));
            //set Properties so when the server will be instantiated from the right class.
            System.setProperties(properties);

        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    public void setUp(){
        setProprieties();
    }

    public void testServerInitialization(){
        try {
            fm = RpcCommunicationFactory.createServer(60001);
            assertNotNull(fm);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }





}
