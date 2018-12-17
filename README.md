# http4s-sangria-mongo

A reference project intended to be a starting point (reference) to use Sangria, a Scala implementation of the [GraphQL](https://graphql.org/) specification.

Note: If you're upgrading the http4s version, the [changelog](https://github.com/http4s/http4s/blob/master/website/src/hugo/content/changelog.md) is invaluable.

# Running the service
### Directly on your host
In the root of the project, invoke the following command:
```bash
sbt service/run
```

### Using docker (assuming it's set up on your host)
In the root of the project, invoke the following:
```bash
sbt dockerize
```
If you'd like to run a MongoDB instance, run the following:
```bash
docker run --name mongo -p 27017:27017 -d mongo
```
This should pull and run the latest mongo image.

Add the required DB and collections - have a look at reference.conf in model/src/main/resources.

To start the http4s-mongo service container, run the following:
```bash
docker run --name http4s -d -p 8080:8080 --link mongo:mongo deontaljaard.github.io/service
```
This will link the mongo container to the http4s container. Note that the connection string changes in this case as a result of the --link flag. The connection string to mongo will change from:
```scala
mongodb://[user:pass@]localhost:27017
```
to this:
```scala
mongodb://[user:pass@]mongo:27017
```

# Navigate to graphiql
In your favourite browser, navigate to the [GraphiQL](http://localhost:8085/api) interface.

Some sample queries
```json
query A {
  person(id:"") {
    id
    firstName
    registrationData {
      deviceUID
    }
    created
  }
}

query B {
  device(id:"1") {
    id
    deviceUID
    personId
    created
  }
}

query C {
  devices(id: "1") {
    id
    deviceUID
  }
}
```


