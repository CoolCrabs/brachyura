# Contributing

## Commands
```bash
git remote add -f cfr https://github.com/leibnitz27/cfr.git #add cfr remote
```

## Useful Links

https://www.atlassian.com/git/tutorials/git-subtree

## How To Compile CFR

Get java 6 https://www.azul.com/downloads/?version=java-6-lts&package=jdk

mvn -Dmaven.compiler.fork=true -Dmaven.compiler.executable=~/whereyouputit/zulu6.22.0.3-jdk6.0.119-linux_x64/bin/javac -DjavadocExecutable=/usr/bin/javadoc install -e
