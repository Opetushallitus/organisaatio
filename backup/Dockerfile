FROM amazonlinux:2023@sha256:a72ad02c10cc535f7b34da08c2a06b191a0c5824f1454531dd06db1236478102

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
