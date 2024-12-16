FROM amazonlinux:2023

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
