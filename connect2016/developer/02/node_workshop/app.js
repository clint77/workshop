'use strict';

var couchbase = require('couchbase');
var uuid = require('uuid');

// Connect to Couchbase
// TODO: connect to the couchbase cluster with IP address
var cluster = new couchbase.Cluster('TODO');

// TODO: get a bucket object using the bucket name
var bucket = cluster.openBucket('TODO');

// Insert some user documents
// TODO: use a new UUID as the key
bucket.upsert("TODO", {
  'first_name': 'Laura',
  'last_name': 'Franecki',
  'city': 'Lake Ollie',
  'country': 'Palau'
}, function(err, res) {
  if (err) throw err;

  // Query for all our users
  // TODO: write a N1QL query to select all the documents from the default bucket
  var qs = 'TODO';
  var q = couchbase.N1qlQuery.fromString(qs);
  bucket.query(q, function(err, rows, meta) {
    if (err) throw err;

    for (var i in rows) {
      console.log(rows[i]);
    }

    process.exit(0);
  });
});
