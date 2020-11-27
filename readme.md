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
