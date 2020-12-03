package org.commonjava.ispn.cache;

import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.SaslQop;
import org.infinispan.client.hotrod.marshall.MarshallerUtil;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Properties;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

//https://github.com/infinispan/infinispan-simple-tutorials/tree/master/remote
//https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.0/html-single/running_data_grid_on_openshift/index
//https://infinispan.org/infinispan-operator/1.1.x/operator.html
//https://infinispan.org/infinispan-operator/master/operator.html#creating_caches_hotrod-caches
//https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html/hot_rod_java_client_guide/configuring_hotrod
//https://github.com/redhat-developer/redhat-datagrid-tutorials/tree/RHDG_8.1.0/remote-query/src/main/java/org/infinispan/tutorial/simple/remote/query
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
        RemoteCache<Object, Object> cache = getOrCreateCache( rmc, "notfound" );

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

        //register .proto into remoteCacheManager
        addCacheKeySchema( rcm );

        // Obtain the default cache
        RemoteCache<Object, Object> cache = getOrCreateCache( rcm, "notfound" );

        cache.put( "key1", "v1" );

        RemoteCache<Object, Object> metadataCache = getOrCreateCache( rcm, "metadata" );
        metadataCache.put( new CacheKey( "0001", "cache0001", new CacheItem( "item001" ) ), "test_metadata" );

        System.out.println("Put value success.");

        cache = rcm.getCache( "notfound" );
        System.out.println("Query the value:" + cache.get( "key1" ));

        metadataCache = rcm.getCache("metadata");
        System.out.println("Query the value of cacheKey: " + metadataCache.get( new CacheKey( "0001", "cache0001", new CacheItem( "item001" ) ) ));

        //https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html-single/data_grid_developer_guide/index#query_library
        QueryFactory queryFactory = Search.getQueryFactory( metadataCache );
        Query query = queryFactory.create( "FROM cache.CacheKey c where c.key = :key" );
        query.setParameter( "key", "0001" );

        List<CacheKey> cacheKeyList = query.list();

        System.out.println(cacheKeyList);

        for( CacheKey cacheKey : cacheKeyList )
        {
            System.out.println("CacheKey with key: " + cacheKey);
        }


        // Stop the cache manager and release all resources
        rcm.stop();
    }

    // for infinispan 9.x
    @Deprecated
    public RemoteCache<Object, Object> getOrCreateCache( final RemoteCacheManager manager, final String cacheName )
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

    private void addCacheKeySchema(RemoteCacheManager cacheManager) throws IOException
    {
        // Get the serialization context of the client
        SerializationContext ctx = MarshallerUtil.getSerializationContext( cacheManager);
        // Use ProtoSchemaBuilder to define a Protobuf schema on the client
        ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
        String fileName = "cache.proto";
        String protoFile = protoSchemaBuilder
                        .fileName(fileName)
                        .addClass(CacheKey.class)
                        .packageName("cache")
                        .build(ctx);

        // Retrieve metadata cache
        RemoteCache<String, String> metadataCache =
                        cacheManager.getCache(PROTOBUF_METADATA_CACHE_NAME);

        // Define the new schema on the server too
        metadataCache.put(fileName, protoFile);
    }

}
