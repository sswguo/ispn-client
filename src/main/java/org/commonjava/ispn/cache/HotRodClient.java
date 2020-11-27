package org.commonjava.ispn.cache;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.SaslQop;
import org.infinispan.commons.configuration.XMLStringConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Properties;

//https://github.com/infinispan/infinispan-simple-tutorials/tree/master/remote
//https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.0/html-single/running_data_grid_on_openshift/index
//https://infinispan.org/infinispan-operator/1.1.x/operator.html
//https://infinispan.org/infinispan-operator/master/operator.html#creating_caches_hotrod-caches
//https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html/hot_rod_java_client_guide/configuring_hotrod
public class HotRodClient
{
    public void setup() throws Exception
    {
        String ispnHost = "rmt-infinispan.spmm-automation.svc.cluster.local";
        String clusterName = "infinispan";
        ConfigurationBuilder builder = new ConfigurationBuilder();
        builder.addServer()
          //Connection
          .host(ispnHost).port( 11222 )
          //Client intelligence
          //External clients can use `BASIC` intelligence only.
          .clientIntelligence( ClientIntelligence.BASIC)
          .security()
              //Authentication
              .authentication().enable()
              //Application user credentials.
                 .username("testuser")
                 .password("testpassword")
                 .serverName(clusterName)
                 .realm( "default" )
                 .saslQop(SaslQop.AUTH)
                 .saslMechanism("DIGEST-MD5")
              //Encryption
              .ssl()
                  .sniHostName(ispnHost)
                  //Path to the TLS certificate.
                  //Clients automatically generate trust stores from certificates.
                  .trustStorePath("/var/lib/ispn/tls.crt");
        RemoteCacheManager rmc = new RemoteCacheManager(builder.build());
        rmc.start();

        // Obtain the default cache
        RemoteCache<String, String> cache = getOrCreateCache( rmc, "notfound" );

        cache.put( "key1", "v1" );
        System.out.println("Put value success.");

        // Stop the cache manager and release all resources
        rmc.stop();

    }

    public void setupWithProps() throws Exception
    {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        Properties p = new Properties();
        try(Reader r = new FileReader( "/var/lib/ispn/hotrod-client.properties")) {
            p.load(r);
            builder.withProperties(p);
        }
        RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
        rcm.start();

        // Obtain the default cache
        RemoteCache<String, String> cache = getOrCreateCache( rcm, "notfound" );

        cache.put( "key1", "v1" );
        System.out.println("Put value success.");

        // Stop the cache manager and release all resources
        rcm.stop();
    }

    public RemoteCache<String, String> getOrCreateCache( final RemoteCacheManager manager, final String cacheName )
    {
        //String xml = "<infinispan><cache-container><distributed-cache name=\"" + cacheName + "\"><expiration interval=\"10000\" lifespan=\"10\" max-idle=\"10\"/></distributed-cache></cache-container></infinispan>";
        String xml = loadXMLConfiguration( cacheName );
        // Obtain the default cache
        return manager.administration().getOrCreateCache( cacheName, new XMLStringConfiguration( xml ) );
    }

    // Manually to load xml for infinispan 9.x
    private String loadXMLConfiguration( final String cacheName )
    {
        BufferedReader bufReader = null;
        try
        {
            // our XML file for this example
            File xmlFile = new File( "/var/lib/ispn/cache-" + cacheName + ".xml" );

            // Let's get XML file as String using BufferedReader
            // FileReader uses platform's default character encoding
            // if you need to specify a different encoding, use InputStreamReader
            Reader fileReader = new FileReader( xmlFile );
            bufReader = new BufferedReader( fileReader );

            StringBuilder sb = new StringBuilder();
            String line = bufReader.readLine();
            while ( line != null )
            {
                sb.append( line.trim() );
                line = bufReader.readLine();
            }
            String xml2String = sb.toString();
            System.out.println( "XML to String using BufferedReader : " );
            System.out.println( xml2String );

            return xml2String;
        }
        catch ( Exception e )
        {
            System.out.println("Load configuration error: " + cacheName );
        }
        finally
        {
            if ( bufReader != null )
            {
                try
                {
                    bufReader.close();
                }
                catch ( IOException e )
                {
                    e.printStackTrace();
                }
            }
        }
        return "";
    }

}
