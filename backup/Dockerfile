FROM amazonlinux:2023@sha256:383f900729263a3c63b8e7e3410f94282f02e246fa68bba066a61f82bece8330

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
