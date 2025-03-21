#!/bin/bash

# Start GlucoPi on Raspberry Pi Zero 2 W with HyperPixel 2.1 Round display

export DISPLAY=:0
unclutter -display :0 -idle 1 &
screen -dmS jar1 bash -c "java -Dglass.platform=gtk -Dprism.verbose=false -Djavafx.verbose=false -Dmonocle.platform.traceConfig=false -jar /home/hansolo/shared/GlucoPi/build/libs/glucopi-21.0.0.jar"
