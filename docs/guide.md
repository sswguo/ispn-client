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
