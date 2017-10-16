This service has two endpoints

1. /experiment
Task : Accepts json requests to create an Experiment
Request method : POST
Sample JSON :
{"experimentId":"E001","projectId":"P001","gatewayId":"G001"}

2. /exit
Task : Kills the container that holds this application
Request methos : GET