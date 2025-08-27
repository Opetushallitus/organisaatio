FROM amazonlinux:2023@sha256:f5077958231a41decbd60c59c48cdb30519b77fdd326e829893470e3a8aa2e55

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
