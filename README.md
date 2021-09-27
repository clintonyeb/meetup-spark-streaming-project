# Most Sentimental News API

#### Demo Video Link (on Microsoft Stream && Youtube)

Microsoft Stream 'requires login' (https://web.microsoftstream.com/video/443fa996-d46c-4d19-a166-d094a5dd0b02)

Youtube Link 'no login required'  (https://youtu.be/ZgsEqUEfFNk)



#### Definitions

>Definition of *sentimental*: marked or governed by feeling, sensibility, or emotional idealism                                                                      



The aim of this project is to provide an API that provides the most sentimental NEWS in the world (mostly us).



The architecture of the project is shown in the following diagram.



![Project Infrastructure Diagram](news-infrasture.png)

1. News Collection Service

   The News Collection Service fetches NEWS from an endpoints parses them and sends them to a Kafka data store.

2. Sentimental Analysis Service

   The Sentimental Analysis Service receives the News articles from Kafka and uses a Machine Leaning endpoint to compute their sentimental information. It then pushes this information to another Kafka topic.

3. Hot Topic Analysis Service

   The Hot Topic Analysis Service is a Spark Stream services that uses a window to compute the most sentimental news in a time window. It stores this information into an HBase database. The streaming services uses HDFS for check-pointing and recovery. For efficient use of resources, I create a pool of Hbase connection object for each partition.

4. Sentimental News API Service

   The Sentimental News API Service is a REST API service that responds to client requests to fetch various sentiments. It uses a cache to store time based sentiments so future requests are processed faster.

5. Kafka

   Apache Kafka is an open-source distributed event streaming platform used by thousands of companies for high-performance data pipelines, streaming analytics, data integration, and mission-critical applications. Kafka provides High throughput, high scalability, permanent storage, and high availability. It also offers built-in stream processing system, connectors to almost all existing data sources, extensive client libraries, and a large ecosystem of Open Source Tools. It can be used in Mission Critical applications, is trusted by thousands of organizations, has vast user community, and rich online resources.

Kafka combines three key capabilities so you can implement your use cases for event streaming end-to-end with a single battle-tested solution:

    1. To publish (write) and subscribe to (read) streams of events, including continuous import/export of your data from other systems.
    2. To store streams of events durably and reliably for as long as you want.
    3. To process streams of events as they occur or retrospectively.

And all this functionality is provided in a distributed, highly scalable, elastic, fault-tolerant, and secure manner. Kafka can be deployed on bare-metal hardware, virtual machines, and containers, and on-premises as well as in the cloud. You can choose between self-managing your Kafka environments and using fully managed services offered by a variety of vendors.

Kafka is a distributed system consisting of servers and clients that communicate via a high-performance TCP network protocol. It can be deployed on bare-metal hardware, virtual machines, and containers in on-premise as well as cloud environments.

Servers: Kafka is run as a cluster of one or more servers that can span multiple datacenters or cloud regions. Some of these servers form the storage layer, called the brokers. Other servers run Kafka Connect to continuously import and export data as event streams to integrate Kafka with your existing systems such as relational databases as well as other Kafka clusters. To let you implement mission-critical use cases, a Kafka cluster is highly scalable and fault-tolerant: if any of its servers fails, the other servers will take over their work to ensure continuous operations without any data loss.

Clients: They allow you to write distributed applications and microservices that read, write, and process streams of events in parallel, at scale, and in a fault-tolerant manner even in the case of network problems or machine failures. Kafka ships with some such clients included, which are augmented by dozens of clients provided by the Kafka community: clients are available for Java and Scala including the higher-level Kafka Streams library, for Go, Python, C/C++, and many other programming languages as well as REST APIs.

![Kafka Processing](streams-and-tables-p1_p4.png)

Kafka APIs

In addition to command line tooling for management and administration tasks, Kafka has five core APIs for Java and Scala:

    * The Admin API to manage and inspect topics, brokers, and other Kafka objects.
    * The Producer API to publish (write) a stream of events to one or more Kafka topics.
    * The Consumer API to subscribe to (read) one or more topics and to process the stream of events produced to them.
    * The Kafka Streams API to implement stream processing applications and microservices. It provides higher-level functions to process event streams, including transformations, stateful operations like aggregations and joins, windowing, processing based on event-time, and more. Input is read from one or more topics in order to generate output to one or more topics, effectively transforming the input streams to output streams.
    * The Kafka Connect API to build and run reusable data import/export connectors that consume (read) or produce (write) streams of events from and to external systems and applications so they can integrate with Kafka. For example, a connector to a relational database like PostgreSQL might capture every change to a set of tables. However, in practice, you typically don't need to implement your own connectors because the Kafka community already provides hundreds of ready-to-use connectors.



### Building and Running Application

The application comes with a `docker-compose.yml` file that lists all the services it needs.

It also includes a `Makefile` with some utility commands for building and running the application.

This means you should have `cmake` and `Docker`, and `Docker Compose` installed.



1. Building components: 

   You can easily build all the components by running: `make build`. For the details of the commands that are run, you can reference the `Makefile`.

2. Running background services:

   For the application to work properly, you need to start the background services that the services rely upon. Example Kafka, HBase. You can start these by running the command: `make start`.

3. Running app services:

   After the background services are initialized, you can run the main component of the application by running the command: `make app` or `make restart-app`.

4. Stopping app services:

   To stop the main components of the pipeline, run the command: `make clean`.

5. Stopping all services:

   To stop all services including the background services, run the command: `make clean-all`.

Sample Datasets:

The application is built to handle live streaming data from the news api. However due to API rating limitations, sample data has been provided. Sample data for the news api is in `sample-news.json` file, and sample data for sentimental api is in `sample-sentiment.json` in the root directory of the project. When the `DEBUG` mode is set to true, the streaming pipeline uses data from the sample files. There is also a `sample-output.json` which has the expected data when you conenct to the REST endpoint.

Users looking to test the application in real life can get tokens and provide them as environment variables to the services.


Results:

An example of getting data from the REST is:

```shell
$ curl 'app.news-api:8085'
```

and the expected sample results are:

```json
[
  {
    "id": "\"ImRlMzBkZmJjLTMyYTYtNDU0Ny1hMzc2LWFlZjRjOTZiMGIwZSI=\"",
    "articleSentiment": {
      "article": {
        "authors": [
          "Snopes"
        ],
        "title": "Indiana Jones Was Originally Indiana Smith",
        "description": "Indiana Jones Was Originally Indiana Smith  Snopes.com",
        "url": "https://www.snopes.com/articles/348039/indiana-jones-indiana-smith/",
        "imageUrl": "https://www.snopes.com/tachyon/2021/06/indiana-jones-1.jpg",
        "content": "Years before audiences were first introduced to Indiana Jones, the titular character of a movie franchise now going on its 40th year, the character was just an idea inside “Star Wars” director George Lucas’ head. In 1978, Lucas got together with his  ... [+3164 chars]",
        "source": "snopes.com",
        "pubDate": "2021-06-11T16:27:09+00:00",
        "country": "us",
        "language": "en"
      },
      "sentimentResponse": {
        "keywords": [
          {
            "score": 0.192192596,
            "word": "grip"
          },
          {
            "score": 0.13424713,
            "word": "day"
          },
          {
            "score": 0.117922934,
            "word": "street"
          },
          {
            "score": -0.992633864,
            "word": "worst"
          },
          {
            "score": -0.815093905,
            "word": "fear"
          },
          {
            "score": -0.205555683,
            "word": "wall"
          },
          {
            "score": -0.126476981,
            "word": "variant"
          }
        ],
        "ratio": 0,
        "score": -0.24219968185714288,
        "type": "negative"
      }
    }
  }
]

```

#### Acknowledgements:

1. Mrudula Mukadam (Associate Chair and Assistant Professor at Maharishi International University). Comprehensive introduction to Big Data Technologies course.
2. All my Team at Telnyx LLC. for the first hand experience.



#### References:

1. Designing Data-Intensive Applications: The Big Ideas Behind Reliable, Scalable, and Maintainable Systems (https://www.google.com/url?sa=t&rct=j&q=&esrc=s&source=web&cd=&cad=rja&uact=8&ved=2ahUKEwjrlebDg5nzAhVGFFkFHTdvDrIQFnoECAIQAQ&url=https%3A%2F%2Fwww.amazon.com%2FDesigning-Data-Intensive-Applications-Reliable-Maintainable%2Fdp%2F1449373321&usg=AOvVaw0T6a9Z2m4rHbndFXgGcWry)
2. https://docs.rapidapi.com/
3. https://datanews.io/docs/headlines
4. https://www.big-data-europe.eu/
5. https://spark.apache.org/docs/latest/streaming-programming-guide.html
6. https://github.com/DanielYWoo/fast-object-pool
7. https://kafka.apache.org/intro
