## Running Infinispan on OpenShift with Infinispan operator
### Installing Infinispan Operator (TODO)
### Getting Started with Infinispan Operator
- Adding custom credentials from file credentials/identities.yaml
```
credentials:
- username: testuser
  password: testpassword
```
```
oc create secret generic --from-file=credentials/identities.yaml remote-ispn-secret
```
- Create Infinispan Cluster based on the Custom Resource(CR)
```
apiVersion: infinispan.org/v1
kind: Infinispan
metadata:
  name: rmt-infinispan
spec:
  replicas: 2
  service:
    type: Cache
  security:
    endpointSecretName: remote-ispn-secret
```
```
oc create -f templates/cr_minimal.yaml
```
### Connectiong to Infinispan Clusters via Hot Rod Client
- Grab the crt
```
oc get secret rmt-infinispan-cert-secret -o jsonpath='{.data.tls\.crt}' | base64 -d > tls.crt
```
Hot Rod Client: `ConfigurationBuilder`
```
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
```
Hot Rod Client: properties
```
# Connection
infinispan.client.hotrod.server_list=rmt-infinispan.spmm-automation.svc.cluster.local:11222

# Client intelligence
# External clients can use `BASIC` intelligence only.
infinispan.client.hotrod.client_intelligence=BASIC

# Authentication
infinispan.client.hotrod.use_auth=true
# Application user credentials.
# The default username is developer.
infinispan.client.hotrod.auth_username=testuser
infinispan.client.hotrod.auth_password=testpassword
infinispan.client.hotrod.auth_server_name=infinispan
infinispan.client.hotrod.auth_realm=default
infinispan.client.hotrod.sasl_properties.javax.security.sasl.qop=auth
infinispan.client.hotrod.sasl_mechanism=DIGEST-MD5

# Encryption
infinispan.client.hotrod.sni_host_name=rmt-infinispan.spmm-automation.svc.cluster.local
# Path to the TLS certificate.
# Clients automatically generate trust stores from certificates.
infinispan.client.hotrod.trust_store_path=/var/lib/ispn/tls.crt
```

### Developer -- [Developing for Infinispan 11.0](https://infinispan.org/docs/stable/titles/developing/developing.html#marshalling_user_types)

> How do I make my Java objects queryable by remote clients? 
https://infinispan.org/blog/2018/06/27/making-java-objects-queryable-by/

## Using Protobuf with Hot Rod
* Configure the client to use a dedicated marshaller, in this case, the ProtoStreamMarshaller
```
infinispan.client.hotrod.marshaller=org.infinispan.client.hotrod.marshall.ProtoStreamMarshaller
```
* In some cases we might need to manually define Protobuf schemas and implement ProtoStream marshallers. For example, if you cannot modify Java object classes to add annotations.
  * Create a Protobuf schema, .proto file, that provides a structured representations of the Java objects to marshall.
```
package maven;
message Metadata {
    required string artifactId = 1;
    required string groupId = 2;
    required string version = 3;

    required Versioning versioning = 4;
}

message Versioning {
    required string release = 1;
}
```
  * The `SerializationContext.registerProtofile` method receives the name of a .proto classpath resource file that contains the message type definitions.
```
// Get the serialization context of the client
 SerializationContext ctx = MarshallerUtil.getSerializationContext( cacheManager);

// 1. register schema via .proto and marshaller per entity
ctx.registerProtoFiles( FileDescriptorSource.fromResources( "metadata.proto" ));
```

  * Use the `org.infinispan.protostream.MessageMarshaller` interface to implement marshallers for our classes.
```
public class MetadataMarshaller
                implements MessageMarshaller<Metadata>
{
    @Override
    public Metadata readFrom( ProtoStreamReader reader ) throws IOException
    {
        String artifactId = reader.readString("artifactId");
        String groupId = reader.readString( "groupId" );
        String version = reader.readString( "version" );
        Versioning versioning = reader.readObject( "versioning", Versioning.class );

        Metadata metadata = new Metadata();
        metadata.setArtifactId( artifactId );
        metadata.setGroupId( groupId );
        metadata.setVersion( version );
        metadata.setVersioning( versioning );

        return metadata;
    }

    @Override
    public void writeTo( ProtoStreamWriter writer, Metadata metadata ) throws IOException
    {
        writer.writeString("artifactId", metadata.getArtifactId());
        writer.writeString( "groupId", metadata.getGroupId() );
        writer.writeString( "version", metadata.getVersion() );
        writer.writeObject( "versioning", metadata.getVersioning(), Versioning.class );
    }

    @Override
    public Class<? extends Metadata> getJavaClass()
    {
        return Metadata.class;
    }

    @Override
    public String getTypeName()
    {
        return "maven.Metadata";
    }
}
```
- Registering Per Entity Marshallers
```
ctx.registerMarshaller( new MetadataMarshaller() );
ctx.registerMarshaller( new VersionMarshaller() );

// Define the new schema on the server too
metadataCache.put( "metadata.proto", FileDescriptorSource.getResourceAsString( getClass(), "/metadata.proto" ));
```
### Defining Protocol Buffers Schemas With Java Annotations
- You can declare Protobuf metadata using Java annotations. Instead of providing a MessageMarshaller implementation and a .proto schema file, you can add minimal annotations to a Java class and its fields.
```
@ProtoDoc("@Indexed")
public class CacheKey implements Serializable
{

    //@Field(index = Index.YES, analyze = Analyze.NO)
    @ProtoDoc("@Field(index=Index.YES, analyze = Analyze.NO, store = Store.NO)")
    @ProtoField(number = 1)
    public String key;

    //@Field(index = Index.NO, analyze = Analyze.NO)
    @ProtoDoc("@Field(index=Index.NO, analyze = Analyze.NO, store = Store.NO)")
    @ProtoField(number = 2)
    public String value;

    @ProtoField( number = 3, javaType = CacheItem.class )
    public CacheItem item;

    @ProtoFactory
    public CacheKey( String key, String value, CacheItem item )
    {
        this.key = key;
        this.value = value;
        this.item = item;
    }

    @Override
    public String toString()
    {
        return "CacheKey{" + "key='" + key + '\'' + ", value='" + value + '\'' + '}';
    }
}
```
- Registering the schema both in client and server
```
// Get the serialization context of the client
  SerializationContext ctx = MarshallerUtil.getSerializationContext( cacheManager);

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
```
### Query 
```
QueryFactory queryFactory = Search.getQueryFactory( nfcCache );
Query query = queryFactory.create( "FROM maven.Metadata m where m.groupId = 'org.jboss'" );
List<Metadata> metadatas = query.list();
```
### Indexing and Searching `TODO` [Performance Tuning](https://infinispan.org/docs/stable/titles/developing/developing.html#query_performance)
