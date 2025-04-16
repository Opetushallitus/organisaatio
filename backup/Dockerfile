FROM amazonlinux:2023@sha256:fc7c82b2ba834045bdf454ef0f9e73d6fdf01166e08671037c8ffdaa9de2cac4

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
