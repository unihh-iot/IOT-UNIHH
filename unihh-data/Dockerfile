FROM registry.cn-shenzhen.aliyuncs.com/deversun/jdk:17-alpine

LABEL maintainer="deversun <https://unihh.com>"
LABEL description="UNIHH-IOT Data Service"

WORKDIR /app

ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone 2>/dev/null || true

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 28002 29002

ENV JAVA_OPTS="-Xms64m -Xmx256m -Dfile.encoding=UTF-8"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
