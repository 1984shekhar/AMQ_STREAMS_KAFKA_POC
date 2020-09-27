### All steps mentioned in this doc are for deployment in minikube. All steps will remain same for Minishift too but only instead of loadbalancer service, OpenShift route can be used. Following yaml would expose a route also with scram-sha-512 authentication. Run it as 'oc apply -f kafka-ephemeral-single-scram-sha.yaml'.
```
apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: my-cluster
spec:
  kafka:
    version: 2.5.0
    replicas: 1
    listeners:
      plain: {}
      tls: {}
      external:
       type: route
       tls: true
       authentication:
          type: scram-sha-512
    authorizatiom:
       type: simple
    config:
      offsets.topic.replication.factor: 1
      transaction.state.log.replication.factor: 1
      transaction.state.log.min.isr: 1
      log.message.format.version: "2.5"
    storage:
      type: ephemeral
  zookeeper:
    replicas: 1
    storage:
      type: ephemeral
  entityOperator:
    topicOperator: {}
    userOperator: {}
```

### strimzi-2-kubectl folder: It is having yaml files for deployment in Strimzi
### Start minikube with strimzi-2 as profile.
```
minikube start --cpus 4 --memory 10240Mi -p strimzi-2

```
### Install Cluster Operator.
```
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl create ns strimzi-test
[chandrashekhar@localhost strimzi-2-kubectl]$ curl -L0 https://strimzi.io/install/latest | sed 's/namespace: .*/namespace: strimzi-test/' > strimzi.yaml
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl apply -f strimzi.yaml  -n strimzi-test
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl config set-context $(kubectl config current-context) --namespace=strimzi-test
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl get pods
NAME                                        READY   STATUS    RESTARTS   AGE
strimzi-cluster-operator-6c8d574d49-9gcld   1/1     Running   0          9h

[chandrashekhar@localhost strimzi-2-kubectl]$ curl -L0 https://strimzi.io/examples/latest/kafka/kafka-persistent.yaml > kafka-persistent.yaml
[chandrashekhar@localhost strimzi-2-kubectl]$ cat kafka-persistent.yaml
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl apply -f kafka-persistent.yaml
```
### Create tunnel for communication between external kafka client and strimzi cluster running in minikube. This should be run in a different terminal.
```
[chandrashekhar@localhost strimzi-2-kubectl]$  minikube tunnel -p strimzi-2
```

### Create topic
```
[chandrashekhar@localhost strimzi-2-kubectl]$ curl -L0 https://strimzi.io/examples/latest/topic/kafka-topic.yaml > kafka-topic.yaml
[chandrashekhar@localhost strimzi-2-kubectl]$ cat kafka-topic.yaml
apiVersion: kafka.strimzi.io/v1beta1
kind: KafkaTopic
metadata:
  name: my-topic
  labels:
    strimzi.io/cluster: my-cluster
spec:
  partitions: 2
  replicas: 2
  config:
    retention.ms: 7200000
    segment.bytes: 1073741824

[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl apply -f kafka-topic.yaml 
kafkatopic.kafka.strimzi.io/my-topic configured

[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl get pods
NAME                                          READY   STATUS    RESTARTS   AGE
my-cluster-entity-operator-768f5476f8-6fnwg   3/3     Running   0          29m
my-cluster-kafka-0                            2/2     Running   0          30m
my-cluster-kafka-1                            2/2     Running   0          30m
my-cluster-zookeeper-0                        2/2     Running   0          30m
strimzi-cluster-operator-6c8d574d49-9gcld     1/1     Running   0          14h
```
### Get truststore from secret
```
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl get secret my-cluster-cluster-ca-cert -o jsonpath='{.data.ca\.p12}'| base64 -d > ca.p12 
```
### Get truststore password from secret 
```
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl get secret my-cluster-cluster-ca-cert -o jsonpath='{.data.ca\.password}'| base64 -d
h8eL6UBAooMf

```
### Get loadbalancer service IP and port.
```
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl get svc|grep my-cluster-kafka-external-bootstrap
my-cluster-kafka-external-bootstrap   LoadBalancer   10.98.82.229     10.98.82.229     9094:32604/TCP               15m
[chandrashekhar@localhost strimzi-2-kubectl]$ 
```

### Get Topic statistics.
```
[chandrashekhar@localhost strimzi-2-kubectl]$  kubectl exec -it my-cluster-kafka-0 -c kafka -- /bin/bash
[kafka@my-cluster-kafka-0 bin]$ pwd
/opt/kafka/bin
```

### Check list of Topics
```
[kafka@my-cluster-kafka-0 bin]$ ./kafka-topics.sh --list --zookeeper localhost:2181
OpenJDK 64-Bit Server VM warning: If the number of processors is expected to increase from one, then you should configure the number of parallel GC threads appropriately using -XX:ParallelGCThreads=N
my-topic
cs-topic
```
### Code changes required for one way TLS communication with Strimzi from Java based Kafka Clients:

A. For both kafka.SampleProducer and kafka.ConsumerCreator:

1. Un-comment following kafka properties:
```
properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
```
2. Set Trustore location and password.
```
properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,"pA3F9qkI5aF0");
properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,"/home/chandrashekhar/Development/Streams_RH/strimzi-2-kubectl/ca.p12");
```      
3. Comment following kafka properties, These are only required for SASL(SCRAM-SHA-512) login:
```
// properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");
// properties.put(SaslConfigs.SASL_MECHANISM,"SCRAM-SHA-512");
//  properties.put(SaslConfigs.SASL_JAAS_CONFIG,"org.apache.kafka.common.security.scram.ScramLoginModule required username=\"my-user\" password=\"gMBqy2GEV0kZ\";");
```
### How to run Producer and Consumer :
```
- Using any Java IDE, these examples can be executed. I used Eclipse IDE.
- Proucer: kafka.KafkaProducerDemo.java
- Consumer: kafka.KafkaClient.java
```

### Check list of groups
```
[kafka@my-cluster-kafka-0 bin]$ ./kafka-consumer-groups.sh  --list --bootstrap-server localhost:9092
OpenJDK 64-Bit Server VM warning: If the number of processors is expected to increase from one, then you should configure the number of parallel GC threads appropriately using -XX:ParallelGCThreads=N
consumerGroup1
```

### Check offset and consumer stats
```
[kafka@my-cluster-kafka-0 bin]$ ./kafka-consumer-groups.sh --bootstrap-server localhost:9092 --group consumerGroup1 --describe

OpenJDK 64-Bit Server VM warning: If the number of processors is expected to increase from one, then you should configure the number of parallel GC threads appropriately using -XX:ParallelGCThreads=N
Consumer group 'consumerGroup1' has no active members.

GROUP           TOPIC           PARTITION  CURRENT-OFFSET  LOG-END-OFFSET  LAG             CONSUMER-ID     HOST            CLIENT-ID
consumerGroup1  cs-topic        0          88665           88665           0               -               -               -
consumerGroup1  cs-topic        1          84387           87025           2638            -               -               -
```

### Authentication and Authorization:
```
[chandrashekhar@localhost strimzi-2-kubectl]$ cp kafka-persistent.yaml kafka-persistent-authorization-authentication.yaml
[chandrashekhar@localhost strimzi-2-kubectl]$ vi kafka-persistent-authorization-authentication.yaml
apiVersion: kafka.strimzi.io/v1beta1
kind: Kafka
metadata:
  name: my-cluster
spec:
  kafka:
    version: 2.4.0
    replicas: 2
    listeners:
      plain: {}
      tls: {}
      external:
        type: loadbalancer
        tls: true
        authentication:
          type: scram-sha-512
    authorization:
      type: simple
----

[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl apply -f kafka-persistent-authorization-authentication.yaml
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl get pods -w
NAME                                          READY   STATUS    RESTARTS   AGE
my-cluster-entity-operator-768f5476f8-6fnwg   3/3     Running   0          178m
my-cluster-kafka-0                            2/2     Running   0          179m
my-cluster-zookeeper-0                        2/2     Running   0          3h
strimzi-cluster-operator-6c8d574d49-9gcld     1/1     Running   0          16h
my-cluster-kafka-1                            2/2     Terminating   0          179m
my-cluster-kafka-1                            0/2     Terminating   0          179m
my-cluster-kafka-1                            0/2     Terminating   0          179m
my-cluster-kafka-1                            0/2     Terminating   0          179m
```
### Create Kafka Topic User
```
[chandrashekhar@localhost strimzi-2-kubectl]$ curl -L0 https://strimzi.io/examples/latest/user/kafka-user.yaml > kafka-user.yaml
# In kafka-user.yaml replace KafkaUser.spec.authentication.type with scram-sha-512 in place of tls.
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl apply -f kafka-user.yaml
```

### Get password for the User
```
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl get secret|grep my-user
my-user                                  Opaque                                1      56s
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl get secret my-user -o jsonpath='{.data.password}' | base64 -d
gMBqy2GEV0kZ
[chandrashekhar@localhost strimzi-2-kubectl]$ kubectl get KafkaUser
NAME      AUTHENTICATION   AUTHORIZATION
my-user   scram-sha-512    simple
```

### Code changes required for SCRAM-SHA-512 authentication with TLS communication using JAVA based Kafka Clients:

A. For both kafka.SampleProducer and kafka.ConsumerCreator:

1. Comment following kafka properties:
```
// properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SSL");
```
2. Set Trustore location and password.
```
properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG,"pA3F9qkI5aF0");
properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,"/home/chandrashekhar/Development/Streams_RH/strimzi-2-kubectl/ca.p12");
```
3. Un-Comment following kafka properties:
```
properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_SSL");     
properties.put(SaslConfigs.SASL_MECHANISM,"SCRAM-SHA-512");
properties.put(SaslConfigs.SASL_JAAS_CONFIG,"org.apache.kafka.common.security.scram.ScramLoginModule required username=\"my-user\" password=\"gMBqy2GEV0kZ\";");
```

### How to run Producer and Consumer :
```
- Using any Java IDE, these examples can be executed. I used Eclipse IDE.
- Proucer: kafka.KafkaProducerDemo.java
- Consumer: kafka.KafkaClient.java
```

        



 
