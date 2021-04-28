FROM openjdk:8-jre-alpine

LABEL maintainer="remylavergne@icloud.com"

ARG version=0.0.1

WORKDIR /usr/src/ktoggl

# Update alpine && Install wget
RUN apk update && \
    apk add --no-cache bash && \
    apk --no-cache add unzip && \
    apk --no-cache add wget && \
    wget -O ktoggl-cli.zip https://github.com/remylavergne/Ktoggl-CLI/releases/download/${version}/ktoggl-cli-${version}.zip && \
    unzip ktoggl-cli.zip && \
    rm ktoggl-cli.zip && \
    mkdir ktoggl-cli-output

ENV PATH /usr/src/ktoggl/ktoggl-cli-shadow-${version}/bin:$PATH

CMD ["bash"]
