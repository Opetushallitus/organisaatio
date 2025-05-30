FROM amazonlinux:2023@sha256:c3381e594bead0d6e859ae15b587854e3afc634e13a1ebdeef26a66ccdad46cd

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
