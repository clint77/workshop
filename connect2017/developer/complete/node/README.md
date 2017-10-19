# Installation and Configuration Instructions

To run the project, a few things must be done prior, such as installing developer dependencies, configuring FTS and N1QL indexes, among other things.

## Installing Development Dependencies

Execute the following from the Terminal (Mac and Linux) or Command Prompt (Windows):

```
npm install
```

The above command will install all dependencies as outlined in the project's **package.json** file.

## Configuring the Project

Open the project's **app.js** file and look for the following three lines:

```
const cluster = new Couchbase.Cluster("couchbase://localhost");
cluster.authenticate("demo", "123456");
const bucket = cluster.openBucket("default");
```

Be sure to update them to reflect your cluster, bucket, and user account with permission to use the necessary services such as N1QL, Data, and FTS.

## Running the Project

The project can be run by executing the following from the command line:

```
npm start app.js
```

The project will be ran using Nodemon which offers hot-reload every time the project is saved.