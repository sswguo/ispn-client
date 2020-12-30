package org.commonjava.ispn.cache;

import org.apache.maven.artifact.repository.metadata.Metadata;
import org.apache.maven.artifact.repository.metadata.Versioning;
import org.infinispan.client.hotrod.RemoteCache;
import org.infinispan.client.hotrod.RemoteCacheManager;
import org.infinispan.client.hotrod.RemoteCounterManagerFactory;
import org.infinispan.client.hotrod.Search;
import org.infinispan.client.hotrod.configuration.ClientIntelligence;
import org.infinispan.client.hotrod.configuration.ConfigurationBuilder;
import org.infinispan.client.hotrod.configuration.SaslQop;
import org.infinispan.client.hotrod.marshall.MarshallerUtil;
import org.infinispan.commons.configuration.XMLStringConfiguration;
import org.infinispan.counter.api.CounterConfiguration;
import org.infinispan.counter.api.CounterManager;
import org.infinispan.counter.api.CounterType;
import org.infinispan.counter.api.Storage;
import org.infinispan.counter.api.StrongCounter;
import org.infinispan.protostream.FileDescriptorSource;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.annotations.ProtoSchemaBuilder;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static org.infinispan.query.remote.client.ProtobufMetadataManagerConstants.PROTOBUF_METADATA_CACHE_NAME;

//https://github.com/infinispan/infinispan-simple-tutorials/tree/master/remote
//https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.0/html-single/running_data_grid_on_openshift/index
//https://infinispan.org/infinispan-operator/1.1.x/operator.html
//https://infinispan.org/infinispan-operator/master/operator.html#creating_caches_hotrod-caches
//https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html/hot_rod_java_client_guide/configuring_hotrod
//https://github.com/redhat-developer/redhat-datagrid-tutorials/tree/RHDG_8.1.0/remote-query/src/main/java/org/infinispan/tutorial/simple/remote/query
//https://infinispan.org/blog/2020/05/30/hotrod-percache-configuration/
//https://infinispan.org/docs/dev/titles/hotrod_java/hotrod_java.html
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

    private RemoteCacheManager startRemoteCacheManager() throws Exception
    {
        ConfigurationBuilder builder = new ConfigurationBuilder();
        Properties p = new Properties();
        try(Reader r = new FileReader( "/var/lib/ispn/hotrod-client.properties")) {
            p.load(r);
            builder.withProperties(p);
        }
        RemoteCacheManager rcm = new RemoteCacheManager(builder.build());
        rcm.start();
        return rcm;
    }

    public void setupWithProps() throws Exception
    {
        RemoteCacheManager rcm = startRemoteCacheManager();

        //Test the cluster counter
        registerCounter( rcm );

        //register .proto into remoteCacheManager
        registerMarshallerAndProto( rcm );

        // Obtain the default cache
        RemoteCache<Object, Object> nfcCache = getOrCreateCache( rcm, "notfound" );

        nfcCache.addClientListener( new MetadataListener() );

        nfcCache.put( "key1", buildMetadata() );

        RemoteCache<Object, Object> metadataCache = getOrCreateCache( rcm, "metadata" );
        metadataCache.put( new CacheKey( "0001", "cache0001", new CacheItem( "item001" ) ), "test_metadata" );

        RemoteCache<Object, Object> expirationCache = getOrCreateCache( rcm, "expiration" );
        // Test the client listener
        expirationCache.addClientListener( new MetadataListener() );
        expirationCache.put( "key1", buildMetadata() );

        System.out.println("Put value success.");

        expirationCache = rcm.getCache( "expiration" );
        System.out.println("Query the value:" + expirationCache.get( "key1" ));

        metadataCache = rcm.getCache("metadata");
        System.out.println("Query the value of cacheKey: " + metadataCache.get( new CacheKey( "0001", "cache0001", new CacheItem( "item001" ) ) ));

        // test enum marshaller and query
        nfcCache.put( "key2", new StoreKey( "001", StoreType.group ) );
        checkCache( nfcCache );

        //https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html-single/data_grid_developer_guide/index#query_library
        QueryFactory queryFactory = Search.getQueryFactory( expirationCache );
        Query query = queryFactory.create( "FROM maven.Metadata m where m.groupId = 'org.jboss'" );

        checkQuery( query );

        Thread.sleep(70000);

        checkQuery( query );

        // Try to trigger the remove event
        expirationCache.remove( "key1" );

        checkQuery( query );

        expirationCache.put( "key2", buildMetadata(), 5, TimeUnit.SECONDS );
        Thread.sleep(70000);
        // Stop the cache manager and release all resources
        rcm.stop();
    }

    private Object buildMetadata()
    {
        Metadata metadata = new Metadata();
        metadata.setGroupId( "org.jboss" );
        metadata.setArtifactId( "infinispan-parent" );
        metadata.setVersion( "11.4.5.Final" );
        Versioning versioning = new Versioning();
        versioning.setRelease( "11.4.5.Final" );
        metadata.setVersioning( versioning );
        return metadata;
    }

    private void checkCache( RemoteCache<Object, Object> nfcCache )
    {
        QueryFactory queryFactory = Search.getQueryFactory( nfcCache );
        Query query = queryFactory.create( "FROM maven.StoreKey sk where sk.type = 'hosted'" );
        List<StoreKey> keys = query.list();
        System.out.println("StoreKey size of hosted:" + keys.size());
        for ( StoreKey key : keys )
        {
            System.out.println("Query storeKey by type hosted:" + key.getKeyId());
        }

        query = queryFactory.create( "FROM maven.StoreKey sk where sk.type = 'group'" );
        keys = query.list();
        System.out.println("StoreKey size of group:" + keys.size());
        for ( StoreKey key : keys )
        {
            System.out.println("Query storeKey by type group:" + key.getKeyId());
        }
    }

    private void checkQuery( Query query )
    {
        List<Metadata> metadatas = query.list();

        System.out.println("Metadata size:" + metadatas.size());
        for ( Metadata md : metadatas )
        {
            System.out.println("Metadata:" + md.getArtifactId());
        }
    }

    // for infinispan 9.x
    @Deprecated
    public RemoteCache<Object, Object> getOrCreateCache( final RemoteCacheManager manager, final String cacheName )
    {
        // For test, let's remove the cache first everytime
        manager.administration().removeCache( cacheName );
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

    private void registerMarshallerAndProto( RemoteCacheManager cacheManager) throws IOException, URISyntaxException
    {
        // Get the serialization context of the client
        SerializationContext ctx = MarshallerUtil.getSerializationContext( cacheManager);

        // 1. register schema via .proto and marshaller per entity
        ctx.registerProtoFiles( FileDescriptorSource.fromResources( "metadata.proto" ));
        ctx.registerMarshaller( new MetadataMarshaller() );
        ctx.registerMarshaller( new VersionMarshaller() );
        ctx.registerMarshaller( new StoreKeyMarshaller() );
        ctx.registerMarshaller( new StoreTypeMarshaller() );

        // 2. java annotated way, use ProtoSchemaBuilder to define a Protobuf schema on the client
        ProtoSchemaBuilder protoSchemaBuilder = new ProtoSchemaBuilder();
        String fileName = "cache_auto.proto";
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
        metadataCache.put( "metadata.proto", FileDescriptorSource.getResourceAsString( getClass(), "/metadata.proto" ));


    }

    private void registerCounter( RemoteCacheManager rcm )
    {
        String counter = "remote-counter";
        CounterManager cm = RemoteCounterManagerFactory.asCounterManager( rcm );

        // To test , let's remove first
        cm.remove( counter );

        cm.defineCounter( counter, CounterConfiguration.builder( CounterType.BOUNDED_STRONG).initialValue( 1 ).lowerBound( 0).storage(
                        Storage.VOLATILE).build() );

        StrongCounter sc = cm.getStrongCounter( counter );
        CompletableFuture<Long> value = sc.incrementAndGet();
        try
        {
            System.out.println(" The value of the counter is: " + value.get());
            System.out.println("CompareAndSwap 2|1: " + sc.compareAndSwap( 2, 3 ).get());
            System.out.println("CompareAndSwap 3|1: " + sc.compareAndSwap( 3, 4 ).get());
        }
        catch ( InterruptedException | ExecutionException e )
        {
            e.printStackTrace();
        }

    }

}
