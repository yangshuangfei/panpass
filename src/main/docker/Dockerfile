FROM williamyeh/java8:latest
VOLUME /tmp
ADD *.jar app.jar
#RUN bash -c 'touch /app.jar'
ENV JAVA_OPTS="\
-Dfile.encoding=UTF8 \
-Duser.timezone=GMT+08 \
-server \
-Xmx512m \
-Xms512m \
-Xmn256m \
-XX:SurvivorRatio=8 \
-XX:MetaspaceSize=256m \
-XX:MaxMetaspaceSize=256m \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:+UseParNewGC \
-XX:HeapDumpPath=/log/panpass-gateway-heap-dump.hprof "
ENTRYPOINT java ${JAVA_OPTS} -Djava.security.egd=file:/dev/./urandom -jar /app.jar
EXPOSE 8089