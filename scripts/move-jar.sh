#!/usr/bin/bash

JAR_PATH=build/libs/
CLIENT_PATH=/games/Minecraft/instances/outlanders-debug/minecraft/mods/
SERVER_PATH=/games/Minecraft/Servers/outlanders-server-debug/mods/

rm $JAR_PATH/outlanders*-sources.jar
rm $CLIENT_PATH/outlanders*.jar
rm $SERVER_PATH/outlanders*.jar

cp $JAR_PATH/outlanders*.jar $CLIENT_PATH
cp $JAR_PATH/outlanders*.jar $SERVER_PATH