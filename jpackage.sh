jpackage --type app-image \
         --input target/scala-2.13 \
         --main-jar pingrpc-assembly-0.0.1.jar \
         --type dmg \
         --icon src/main/resources/icon.icns \
         --name PingRPC
