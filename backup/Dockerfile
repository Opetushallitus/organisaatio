FROM amazonlinux:2023@sha256:c6f12c603e1abd267ca09c9c604a8b4eb421deddb83a90323cb835bdf8f1dce5

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
