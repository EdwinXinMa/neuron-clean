FROM registry.cn-hangzhou.aliyuncs.com/dockerhub_mirror/java:17-anolis

MAINTAINER dev@alwayscontrol.net

RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime

WORKDIR /neuron-system

EXPOSE 8080

ADD ./target/neuron-system-start-1.0.0.jar ./

RUN mkdir -p /neuron-system/config

CMD sleep 60;java -Dfile.encoding=UTF-8 -Djava.security.egd=file:/dev/./urandom -jar neuron-system-start-1.0.0.jar
