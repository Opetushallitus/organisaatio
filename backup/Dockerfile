FROM amazonlinux:2023@sha256:e62b7b67cc1321b3a36930f30d915cf93f725fa47043a27e7956c52e3f4038b1

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
