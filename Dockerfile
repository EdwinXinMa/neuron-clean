# 使用腾讯云镜像加速器地址（已经在你的服务器上验证成功）
FROM mirror.ccs.tencentyun.com/library/eclipse-temurin:21-jre

WORKDIR /app

COPY neuron-system-start.jar app.jar

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

RUN mkdir -p /opt/upFiles /opt/webapp

EXPOSE 8080 9001

ENTRYPOINT ["java", \
  "-Xmx512m", \
  "-Xms256m", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dfile.encoding=UTF-8", \
  "-Dspring.profiles.active=prod", \
  "-jar", "app.jar"]