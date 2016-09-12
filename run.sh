#!/bin/bash
sudo ifconfig lo 127.0.0.1 netmask 255.0.0.0 up
sudo kill -s 9 $(pidof arm2)
cd  website
sudo java -jar ../RoboticArrrmLauncher/target/RoboticArrrmLauncher-1.0-SNAPSHOT.jar

