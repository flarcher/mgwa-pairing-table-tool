#!/bin/bash
#
# Launch the project without any packaging required

cd main
mvn javafx:run
cd - > /dev/null
