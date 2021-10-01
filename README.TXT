
Storage Client Service


Minio S3 Object Store
https://hub.docker.com/r/minio/minio/

----------------------------------------------------------------------
Run as docker container

 First create a docker network

docker network create minio-net

 Now use one of the following to run the Minio image

docker run --name minio --network minio-net -v /mnt/data:/data --rm -p 9000:9000 -p 9001:9001  -e MINIO_ROOT_USER=minioadmin -e MINIO_ROOT_PASSWORD=minioadmin minio/minio server /data --console-address ":9001"

or

docker run --name minio --network minio-net -v /mnt/data:/data --rm -p 9000:9000 -p 9001:9001   minio/minio server /data --console-address ":9001"


----------------------------------------------------------------------
Build jar

mvn clean package

----------------------------------------------------------------------
Run jar

java -jar ./target/storage-client-0.0.1-SNAPSHOT.jar 

----------------------------------------------------------------------
Manually build docker container image

docker build -t storage-client:latest .
----------------------------------------------------------------------
Run docker image

docker run --network minio-net -p 8080:8080 storage-client:latest  --minio.host.url=http://minio:9000


----------------------------------------------------------------------

Get a listing of buckets

curl http://localhost:8080/buckets

----------------------------------------------------------------------
Create new bucket

curl -X POST http://localhost:8080/buckets/new-bucket

where new-bucket is the name of the bucket
----------------------------------------------------------------------
Delete bucket

curl -X DELETE  http://localhost:8080/buckets/new-bucket

----------------------------------------------------------------------
List bucket contents

curl http://localhost:8080/new-bucket

----------------------------------------------------------------------
Create new bucket object

curl -F 'filename=@./testfile.txt' http://localhost:8080/new-bucket

----------------------------------------------------------------------

*****************************************************************
SPRING NATIVE IS CURRENTLY NOT SUPPORTED FOR THIS SERVICE

For details please refer to:
https://github.com/oracle/graal/issues/3067

Ignore the following section until this notice has been removed

******************************************************************
build Spring Native GraalVM native image with container image
 

mvn spring-boot:build-image


run Spring Native GraalVM native image container
docker run -p 8080:8080 storage-client:0.0.1-SNAPSHOT
docker run --network minio-net -p 8080:8080 storage-client:latest --minio.host.url=http://minio:9000 
docker run --network minio-net -p 8080:8080 storage-client:0.0.1-SNAPSHOT -minio.host.url=http://minio:9000 
CURL test