FROM amazonlinux:2023@sha256:57f00a32ff213e126c59e03f8a7ecb662b1887c1ef31c709865e6fd5c72cac07

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
