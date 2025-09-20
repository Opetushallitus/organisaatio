FROM amazonlinux:2023@sha256:4f02d5d4500105878af64950f9c46cde3392b907907cb0d23a885c721a26f67d

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
