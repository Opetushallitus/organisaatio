FROM amazonlinux:2023@sha256:8217eec04f21ec88f5fd0ce57641a9fa9a586af5c4d2a68c7fcd50f2146ddb03

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
