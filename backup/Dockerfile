FROM amazonlinux:2023@sha256:04ff8139d7f2d9f026a5593f47e494ebe9f9819349f1b1ccc3be2bbbcdc317bf

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
