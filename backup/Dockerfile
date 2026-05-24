FROM amazonlinux:2023@sha256:f03d3254192161691b72be58219022adc2036bd0933aa2b6f3b744f20b2bbe8c

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
