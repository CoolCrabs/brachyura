# javacompilelib

Simple library to compile Java programs. Supports Java 6+

## Compiling

```
mvn -Dmaven.compiler.fork=true -Dmaven.compiler.executable=/yadaya/zulu6.22.0.3-jdk6.0.119-linux_x64/bin/javac -Djvm=/yadaya/zulu6.22.0.3-jdk6.0.119-linux_x64/bin/java install -e
./cleanup.sh
```