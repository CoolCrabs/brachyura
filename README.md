# brachyura

\[WIP\] Java build tool focused on Minecraft modding. Uses simple buildscripts written in Java with easy to understand and debug logic.

## Goals

Predictable
Simple
Flexible

## Discord

[![Join The Discord](https://discordapp.com/api/guilds/844335788384452619/widget.png?style=banner2)](https://discord.gg/ZfNH3BUVth)

## File Structure

```
.
├── brachyura - Source for the build tool itself
│
├── cfr - CFR decompiler with brachyura changes (javadocs)
│
├── fabricmerge - Merge utilites from FabricMC Stitch seperated out and slightly improved
│
├── javacompilelib - Simple library to compile sources using javax.tools.JavaCompiler. Supports running in a separate process and can compile with JDK 6+.
│
└── testmod - A simple test mod compiled in brachyura's junit tests
```
