FROM openjdk:8u131
ADD target/scala-2.12/akka-cluster-on-kubernetes-assembly-1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", "-XX:MaxRAMFraction=1", "-XshowSettings:vm"]
