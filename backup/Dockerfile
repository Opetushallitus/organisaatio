FROM amazonlinux:2023@sha256:eb5359d566df2b34cb58be63c2d0fa1476e7654833d4d3af307b930a8fac6446

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
