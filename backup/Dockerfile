FROM amazonlinux:2023@sha256:c1872fb69ff9ed9581c999509dd0dcb4288235087f1df4999b866affdac0278d

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
