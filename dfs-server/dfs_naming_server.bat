@echo off
rem Runs standalone RSO APP
java -XX:+UseParallelGC -cp "conf;lib/*" rso.dfs.server.DFSServer M