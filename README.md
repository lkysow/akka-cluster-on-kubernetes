# Akka Cluster on Kubernetes
Sample project for deploying Akka Cluster to Kubernetes.
Presented at Scala Up North on July 21, 2017.

To run the project yourself:

## Create a Kubernetes Cluster
Follow [https://cloud.google.com/container-engine/docs/quickstart](https://cloud.google.com/container-engine/docs/quickstart)
to create a Kubernetes cluster in minutes.

I recommend getting `kubectl` set up locally which means you need to install the
`gcloud` SDK: [https://cloud.google.com/sdk/docs/quickstarts](https://cloud.google.com/sdk/docs/quickstarts)

Then click the **Connect** button next to your cluster name here: [https://console.cloud.google.com/kubernetes/list](https://console.cloud.google.com/kubernetes/list) to set up `kubectl`

## Run Locally Without Etcd
The `master` branch is set up to run with etcd so if you want to run locally
then you'll need to uncomment the `seed-node` config in `src/main/resources/application.conf`

```hocon
// src/main/resources/application.conf
...
  cluster {
    roles = [frontend, backend]
    // uncomment this if running locally
    seed-nodes = [
      "akka.tcp://ClusterSystem@127.0.0.1:2551"
    ]
  }
```

Then you can run the project and curl the HTTP endpoint.

```
➜ sbt run
[info] Running com.hootsuite.akkak8s.SimpleClusterApp
[INFO] [07/21/2017 13:51:35.643] [run-main-0] [akka.remote.Remoting] Starting remoting
[INFO] [07/21/2017 13:51:36.052] [run-main-0] [akka.remote.Remoting] Remoting started; listening on addresses :[akka.tcp://ClusterSystem@127.0.0.1:2551]
[INFO] [07/21/2017 13:51:36.075] [run-main-0] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Starting up...
[INFO] [07/21/2017 13:51:36.269] [run-main-0] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Registered cluster JMX MBean [akka:type=Cluster]
[INFO] [07/21/2017 13:51:36.269] [run-main-0] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Started up successfully
[INFO] [07/21/2017 13:51:36.365] [ClusterSystem-akka.actor.default-dispatcher-14] [akka.tcp://ClusterSystem@127.0.0.1:2551/system/constructr] Stopping self, because seed-nodes defined
[INFO] [07/21/2017 13:51:36.411] [ClusterSystem-akka.actor.default-dispatcher-3] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Node [akka.tcp://ClusterSystem@127.0.0.1:2551] is JOINING, roles [frontend, backend]
[INFO] [07/21/2017 13:51:36.463] [ClusterSystem-akka.actor.default-dispatcher-3] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Leader is moving node [akka.tcp://ClusterSystem@127.0.0.1:2551] to [Up]
```

And now curl the app

```bash
➜ curl "http://localhost:8080?msg=about+a+hotdog"
Hot Dog! (from fe: xxxxxxxx be: xxxxxxxx)%
➜ curl "http://localhost:8080?msg=about+a+dog"
Not Hot Dog :( (from fe: xxxxxxxx be: xxxxxxxx)%
```

## Run Locally With Etcd
In Kubernetes, we need to use the [constructr](https://github.com/hseeberger/constructr) library to discover other seed nodes.

To test this out locally, comment out the `seed-nodes` config

```hocon
    roles = [frontend, backend]
    // uncomment this if running locally
//    seed-nodes = [
//      "akka.tcp://ClusterSystem@127.0.0.1:2551"
//    ]
```

And start etcd with [Docker for Mac](https://www.docker.com/docker-mac).

```bash
➜ docker run -d \
  --name etcd \
  --publish 2379:2379 \
  quay.io/coreos/etcd:v2.3.7 \
  --listen-client-urls http://0.0.0.0:2379 \
  --advertise-client-urls http://192.168.99.100:2379
```

Finally, restart the app

```
➜ sbt run

[info] Running com.hootsuite.akkak8s.SimpleClusterApp
[INFO] [07/21/2017 13:50:16.051] [run-main-0] [akka.remote.Remoting] Starting remoting
[INFO] [07/21/2017 13:50:16.503] [run-main-0] [akka.remote.Remoting] Remoting started; listening on addresses :[akka.tcp://ClusterSystem@127.0.0.1:2551]
[INFO] [07/21/2017 13:50:16.530] [run-main-0] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Starting up...
[INFO] [07/21/2017 13:50:16.870] [run-main-0] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Registered cluster JMX MBean [akka:type=Cluster]
[INFO] [07/21/2017 13:50:16.870] [run-main-0] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Started up successfully
[INFO] [07/21/2017 13:50:16.933] [ClusterSystem-akka.actor.default-dispatcher-6] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - No seed-nodes configured, manual cluster join required
[INFO] [07/21/2017 13:50:16.951] [ClusterSystem-akka.actor.default-dispatcher-2] [akka.tcp://ClusterSystem@127.0.0.1:2551/system/constructr] Creating constructr-machine, because no seed-nodes defined
[INFO] [07/21/2017 13:50:20.348] [ClusterSystem-akka.actor.default-dispatcher-6] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Node [akka.tcp://ClusterSystem@127.0.0.1:2551] is JOINING, roles [frontend, backend]
[INFO] [07/21/2017 13:50:20.370] [ClusterSystem-akka.actor.default-dispatcher-6] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Leader is moving node [akka.tcp://ClusterSystem@127.0.0.1:2551] to [Up]
```

And now curl the app again

```bash
➜ curl "http://localhost:8080?msg=about+a+hotdog"
Hot Dog! (from fe: xxxxxxxx be: xxxxxxxx)%
➜ curl "http://localhost:8080?msg=about+a+dog"
Not Hot Dog :( (from fe: xxxxxxxx be: xxxxxxxx)%
```

## Package into Docker
```bash
➜ sbt assembly
# outputs target/scala-2.12/akka-cluster-on-kubernetes-assembly-0.1.jar
# now build that into a docker image
➜ docker build -t {your namespace}/akka-cluster .
```

## Test Locally With Docker Compose
We still need etcd but we want it accessible from the same network as our app which
is now running on Docker, not on localhost. To do this we use [Docker Compose](https://docs.docker.com/compose/overview/).

```
➜ docker-compose up

Starting akkaclusteronkubernetes_etcd_1 ...
Starting akkaclusteronkubernetes_etcd_1 ... done
Starting akkaclusteronkubernetes_akka_1 ...
Starting akkaclusteronkubernetes_akka_1 ... done
Attaching to akkaclusteronkubernetes_etcd_1, akkaclusteronkubernetes_akka_1
etcd_1  | 2017-07-21 21:08:21.112937 I | etcdmain: etcd Version: 2.3.7
etcd_1  | 2017-07-21 21:08:21.113016 I | etcdmain: Git SHA: fd17c91
etcd_1  | 2017-07-21 21:08:21.113026 I | etcdmain: Go Version: go1.6.2
etcd_1  | 2017-07-21 21:08:21.113041 I | etcdmain: Go OS/Arch: linux/amd64
etcd_1  | 2017-07-21 21:08:21.113052 I | etcdmain: setting maximum number of CPUs to 2, total number of available CPUs is 2
etcd_1  | 2017-07-21 21:08:21.113058 W | etcdmain: no data-dir provided, using default data-dir ./default.etcd
etcd_1  | 2017-07-21 21:08:21.113834 N | etcdmain: the server is already initialized as member before, starting as etcd member...
etcd_1  | 2017-07-21 21:08:21.114360 I | etcdmain: listening for peers on http://localhost:2380
etcd_1  | 2017-07-21 21:08:21.114539 I | etcdmain: listening for peers on http://localhost:7001
etcd_1  | 2017-07-21 21:08:21.114584 I | etcdmain: listening for client requests on http://0.0.0.0:2379
etcd_1  | 2017-07-21 21:08:21.124405 I | etcdserver: name = default
etcd_1  | 2017-07-21 21:08:21.124445 I | etcdserver: data dir = default.etcd
etcd_1  | 2017-07-21 21:08:21.124453 I | etcdserver: member dir = default.etcd/member
etcd_1  | 2017-07-21 21:08:21.124461 I | etcdserver: heartbeat = 100ms
etcd_1  | 2017-07-21 21:08:21.124465 I | etcdserver: election = 1000ms
etcd_1  | 2017-07-21 21:08:21.124470 I | etcdserver: snapshot count = 10000
etcd_1  | 2017-07-21 21:08:21.124505 I | etcdserver: advertise client URLs = http://0.0.0.0:2379
etcd_1  | 2017-07-21 21:08:21.128006 I | etcdserver: restarting member ce2a822cea30bfca in cluster 7e27652122e8b2ae at commit index 424
etcd_1  | 2017-07-21 21:08:21.128122 I | raft: ce2a822cea30bfca became follower at term 10
etcd_1  | 2017-07-21 21:08:21.128165 I | raft: newRaft ce2a822cea30bfca [peers: [], term: 10, commit: 424, applied: 0, lastindex: 424, lastterm: 10]
etcd_1  | 2017-07-21 21:08:21.130984 I | etcdserver: starting server... [version: 2.3.7, cluster version: to_be_decided]
etcd_1  | 2017-07-21 21:08:21.134951 N | etcdserver: added local member ce2a822cea30bfca [http://localhost:2380 http://localhost:7001] to cluster 7e27652122e8b2ae
etcd_1  | 2017-07-21 21:08:21.135076 N | etcdserver: set the initial cluster version to 2.3
etcd_1  | 2017-07-21 21:08:22.431440 I | raft: ce2a822cea30bfca is starting a new election at term 10
etcd_1  | 2017-07-21 21:08:22.431735 I | raft: ce2a822cea30bfca became candidate at term 11
etcd_1  | 2017-07-21 21:08:22.431922 I | raft: ce2a822cea30bfca received vote from ce2a822cea30bfca at term 11
etcd_1  | 2017-07-21 21:08:22.432299 I | raft: ce2a822cea30bfca became leader at term 11
etcd_1  | 2017-07-21 21:08:22.432405 I | raft: raft.node: ce2a822cea30bfca elected leader ce2a822cea30bfca at term 11
etcd_1  | 2017-07-21 21:08:22.433347 I | etcdserver: published {Name:default ClientURLs:[http://0.0.0.0:2379]} to cluster 7e27652122e8b2ae
akka_1  | [INFO] [07/21/2017 21:08:24.309] [main] [akka.remote.Remoting] Starting remoting
akka_1  | [INFO] [07/21/2017 21:08:24.670] [main] [akka.remote.Remoting] Remoting started; listening on addresses :[akka.tcp://ClusterSystem@127.0.0.1:2551]
akka_1  | [INFO] [07/21/2017 21:08:24.696] [main] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Starting up...
akka_1  | [INFO] [07/21/2017 21:08:24.936] [main] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Registered cluster JMX MBean [akka:type=Cluster]
akka_1  | [INFO] [07/21/2017 21:08:24.936] [main] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Started up successfully
akka_1  | [INFO] [07/21/2017 21:08:25.005] [ClusterSystem-akka.actor.default-dispatcher-5] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - No seed-nodes configured, manual cluster join required
akka_1  | [INFO] [07/21/2017 21:08:25.006] [ClusterSystem-akka.actor.default-dispatcher-2] [akka.tcp://ClusterSystem@127.0.0.1:2551/system/constructr] Creating constructr-machine, because no seed-nodes defined
akka_1  | [INFO] [07/21/2017 21:08:27.112] [ClusterSystem-akka.actor.default-dispatcher-4] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Node [akka.tcp://ClusterSystem@127.0.0.1:2551] is JOINING, roles [frontend, backend]
akka_1  | [INFO] [07/21/2017 21:08:27.143] [ClusterSystem-akka.actor.default-dispatcher-4] [akka.cluster.Cluster(akka://ClusterSystem)] Cluster Node [akka.tcp://ClusterSystem@127.0.0.1:2551] - Leader is moving node [akka.tcp://ClusterSystem@127.0.0.1:2551] to [Up]
```
## Deploy to Kubernetes
Finally we're ready to deploy to Kubernetes!

First deploy etcd

```bash
➜ docker push {your namespace}/akka-cluster .
```

```bash
➜ kubectl apply -f kubernetes/etcd.yaml
```

Run a "bounce" pod so you can talk to the cluster easily.
I'm using my colleague's debug container which has a bunch of tools built in: [https://github.com/markeijsermans/docker-debug](https://github.com/markeijsermans/docker-debug).

```bash
➜ kubectl run bounce --image=markeijsermans/debug -it bash
If you don't see a command prompt, try pressing enter.
(21:15 bounce-2304503334-6dqpw:/) curl etcd:2379/health
{"health": "true"}
```

Now deploy the app! If you've been pushing your own Docker images, you'll need to edit the
`kubernetes/nothotdog.yaml` file to use your image. Specifically these lines

```yaml
...
        image: lkysow/akka-cluster
...
```

Push your docker image and then apply the app.

```bash
➜ docker push {your namespace}/akka-cluster
➜ kubectl apply -f kubernetes/nothotdog.yaml
```

From your bounce pod, you should be able to curl the app!

```bash
➜ curl nothotdog:8080?msg=about-a-hotdog
Hot Dog! (from fe: frontend-3857959296-5x885 be: backend-3899286914-6941z)
```

## Play Around With Kubernetes

`curl` the app in a loop from the bounce pod

```bash
➜ while true; do curl -sS -m 1.5 nothotdog:8080?msg=about-a-hotdog; echo ""; sleep 0.5; done
Hot Dog! (from fe: frontend-3857959296-5x885 be: backend-3899286914-6941z)
Hot Dog! (from fe: frontend-3857959296-5x885 be: backend-3899286914-6941z)
Hot Dog! (from fe: frontend-3857959296-5x885 be: backend-3899286914-6941z)
```

Scale the app

```bash
➜ kubectl scale deployment backend --replicas=3
➜ kubectl scale deployment frontend --replicas=3
```

Add autoscaling

```bash
➜ kubectl autoscale deploy backend --min=1 --max=3 --cpu-percent=5
➜ kubectl get hpa
```

Add some load from the bounce pod

```bash
➜ slow_cooker -concurrency 10 -qps 300 -interval 5s "http://nothotdog:8080?msg=hotdog"
```

And you're done! Welcome to Akka Cluster on Kubernetes :D
