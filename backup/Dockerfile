FROM amazonlinux:2023@sha256:99c2f938acd0d2a154a005b8dd30c90718727736c60cc93e718f68020948a41d

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
