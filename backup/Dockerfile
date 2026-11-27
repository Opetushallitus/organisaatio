FROM amazonlinux:2023@sha256:edd3297df7d444ab9a7896b763d5305d4bab57d2fb3943e831c1713b97d32d9d

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
