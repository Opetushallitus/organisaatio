FROM amazonlinux:2023@sha256:d6b42a987b6699493e34cccb263fa5d6bb5a19308e781384c786a384a71bea04

RUN yum update && yum install -y postgresql16 awscli jq

COPY backup.sh /backup.sh

CMD ["/backup.sh"]
