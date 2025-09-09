FROM openjdk:17-slim

WORKDIR /app

RUN apt-get update && \
    apt-get install -y curl gnupg && \
    echo "deb https://repo.scala-sbt.org/scalasbt/debian all main" | tee /etc/apt/sources.list.d/sbt.list && \
    curl -sL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x99E82A75642AC823" | apt-key add - && \
    apt-get update && \
    apt-get install -y sbt && \
    apt-get clean

COPY build.sbt .
COPY project ./project
COPY . .


RUN sbt stage

EXPOSE 9000
EXPOSE 50051

CMD ["./target/universal/stage/bin/library-manager"]
# CMD ./start.sh
