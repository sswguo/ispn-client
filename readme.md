https://infinispan.org/infinispan-operator/1.1.x/operator.html
https://infinispan.org/infinispan-operator/master/operator.html
https://infinispan.org/docs/9.4.x/server_guide/server_guide.html

How to access the infinispan cluster from application via Hot Rod client

### SSL Error
```
05:52:12,134 WARN  (SINGLE_PORT-ServerIO-4-2) [io.netty.handler.ssl.ApplicationProtocolNegotiationHandler] [id: 0xb4a9f17d, L:/172.21.11.33:11222 ! R:/172.21.10.21:49048] TLS handshake failed: io.netty.handler.ssl.NotSslRecordException: not an SSL/TLS record: a0031d17000003ffffffff0f0000
```
```
oc get secret rmt-infinispan-cert-secret -o jsonpath='{.data.tls\.crt}' | base64 -d > tls.crt
```
### Check the dnsPing in infinispan.yaml
```
oc rsh rmt-infinispan-0
/etc/config/infinispan.yaml
```
```
clusterName: rmt-infinispan
jgroups:
  transport: tcp
  dnsPing:
    query: rmt-infinispan-ping.spmm-automation.svc.cluster.local
  diagnostics: false
keystore:
  path: /opt/infinispan/server/conf/keystore
  password: password
  alias: server
  crtPath: /etc/encrypt
xsite:
  address: ""
  name: ""
  port: 0
  backups: []
logging:
  categories: {}
```

/opt/infinispan/server/conf/infinispan.xml

### Issue: 
```
org.infinispan.client.hotrod.exceptions.TransportException: org.infinispan.client.hotrod.exceptions.HotRodClientException:Request for messageId=72 returned server error (status=0x84): javax.security.sasl.SaslException: ELY05088: digest-uri "hotrod/rmt-infinispan" not accepted
```
the above issue is caused by the wrong cluerName set in code `.serverName(clusterName)`, which in my case it should be `infinispan` not `rmt-infinispan`

```
org.infinispan.client.hotrod.exceptions.TransportException: org.infinispan.client.hotrod.exceptions.HotRodClientException:Request for messageId=72 returned server error (status=0x84): javax.security.sasl.SaslException: ELY05087: Client selected realm not offered by server (ApplicationRealm)
```
the above issue is caused by the wrong realm name set or missing in code `.realm( "default" )`

### Create user/pass 
```
oc create secret generic --from-file=credentials/identities.yaml remote-ispn-secret
```

### Ref the secret in CR
```
...
security:
  endpointSecretName: remote-ispn-secret
...
```

