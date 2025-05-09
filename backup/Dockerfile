FROM amazonlinux:2023@sha256:cb5b4c509d62ae388f674c139ae5e8281fc160c217d474445e912043e1941988

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
