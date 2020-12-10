### Prerequisites
- Install Infinispan Operator.
- Have an `oc` or a `kubectl` client.

### Running the client app 
- Login OpenShift and select the right namespace/project 
- Running `bin/run.sh` to exec maven build, docker build and push image into quay.io
- Running `bin/oc.sh` to new app in OpenShift 
- Checking logs
```
oc get pod|grep ispntest
ispntest-1-deploy                     0/1       Completed           0          57s
ispntest-1-tkpp4                      1/1       Running             0          53s
```
```
oc logs ispntest-1-tkpp4
```

### Doc
- [Infinispan Configuration Schemas](https://docs.jboss.org/infinispan/11.0/configdocs/)  
- [DataGrid 8.1](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html-single/)  
- [DateGrid 7.x](https://access.redhat.com/documentation/en-us/red_hat_data_grid/7.1/html/administration_and_configuration_guide/set_up_and_configure_the_infinispan_query_api)  
- [infinispan-simple-tutorials](https://github.com/infinispan/infinispan-simple-tutorials/tree/master/remote)  
- [infinispan-operator-1.1.x](https://infinispan.org/infinispan-operator/1.1.x/operator.html)    
- [infinispan-operator-master](https://infinispan.org/infinispan-operator/master/operator.html#creating_caches_hotrod-caches)  
- [configuring hotrod](https://access.redhat.com/documentation/en-us/red_hat_data_grid/8.1/html/hot_rod_java_client_guide/configuring_hotrod)  
- [redhat-datagrid-tutorials](https://github.com/redhat-developer/redhat-datagrid-tutorials/tree/RHDG_8.1.0/remote-query/src/main/java/org/infinispan/tutorial/simple/remote/query)  
- [hotrod-percache-configuration](https://infinispan.org/blog/2020/05/30/hotrod-percache-configuration/)  
- [hotrod_java](https://infinispan.org/docs/dev/titles/hotrod_java/hotrod_java.html)  
