## Magneto DB

### Run

Spin up docker containers of 5 nodes and 1 client

```
 docker-compose up --scale node=5 --build
```
Master Node
```
 java -jar magneto-db.jar master 5670
```

Node
```
 java -jar magneto-db.jar node 5671
 java -jar magneto-db.jar node 5672
 java -jar magneto-db.jar node 5673
 java -jar magneto-db.jar node 5674

```


Client connect to master node
```
 java -jar magneto-client.jar localhost 5670
```