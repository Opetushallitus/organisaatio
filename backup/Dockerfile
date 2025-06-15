FROM amazonlinux:2023@sha256:54064c8f8487b81679fa8b8ddfc75a8eff98d85bdd07b6b2ce9ed7cc18754fb2

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
