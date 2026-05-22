FROM amazon/aws-cli:2.34.52@sha256:d12f97286c70c50dafa3313fe6b6dc3aefd5dd71e0a73f3f893ea181aad2bdf8

RUN curl "https://s3.amazonaws.com/session-manager-downloads/plugin/latest/linux_arm64/session-manager-plugin.rpm" -o "session-manager-plugin.rpm"
RUN yum install -y session-manager-plugin.rpm openssh-clients nc jq socat

COPY port-forward.sh .

EXPOSE 1111
HEALTHCHECK --interval=1s --start-period=30s CMD ["nc", "-z", "localhost", "1111"]
ENTRYPOINT ["./port-forward.sh"]
