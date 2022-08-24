FROM bitnami/zookeeper:3.6.1

COPY . . 

CMD zkCli.sh -server registry:2181 < ./init
