FROM registry.cn-shenzhen.aliyuncs.com/deversun/jdk:17-alpine

LABEL maintainer="deversun <https://unihh.com>"
LABEL description="UNIHH-IOT Generic Protocol Driver"

WORKDIR /app

# 可通过 build-arg 传入具体驱动模块目录 (unihh-driver-modbus / unihh-driver-s7 / unihh-driver-opcua)
ARG DRIVER_NAME=unihh-driver-modbus

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone 2>/dev/null || true

COPY ${DRIVER_NAME}/target/*.jar app.jar

ENV JAVA_OPTS="-Xms64m -Xmx128m -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
