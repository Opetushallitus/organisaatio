FROM amazonlinux:2023@sha256:f8ca68a32b41290acdc8db52f950650f020ec7e42585e7b139a869822767152b

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
