FROM amazonlinux:2023@sha256:b0737d724480361f137d428136721fff45f56792d650f4675d1e5732bb1104e4

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
