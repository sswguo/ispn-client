mvn clean install -Dmaven.test.skip=true

docker build . -t quay.io/wguo/ispn_test:latest

docker push quay.io/wguo/ispn_test:latest
