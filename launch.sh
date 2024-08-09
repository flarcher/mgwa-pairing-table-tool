#!/bin/bash
#
# Launch the project without any packaging required
export API_SERVER_DEBUG=true
cd main
mvn javafx:run
cd - > /dev/null
