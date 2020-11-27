oc delete dc/ispntest
oc delete svc/ispntest
oc delete imagestream/ispntest
oc new-app quay.io/wguo/ispn_test:latest
