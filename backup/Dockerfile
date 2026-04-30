FROM amazonlinux:2023@sha256:ceeab7e010ed03ea155cfbbfd7140672eba5a49e1110b8b4ed35342312c3f21a

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
