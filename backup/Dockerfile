FROM amazonlinux:2023@sha256:5642f8e1796bb7e486021647c4914299c5b6c9102ee00cde8b47c962b1bf9e02

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
