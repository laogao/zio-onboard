var PROTO_PATH = __dirname + '/src/main/protobuf/bidi.proto';

var async = require('async');
var fs = require('fs');
var parseArgs = require('minimist');
var path = require('path');
var _ = require('lodash');
var grpc = require('@grpc/grpc-js');
var protoLoader = require('@grpc/proto-loader');
var packageDefinition = protoLoader.loadSync(
    PROTO_PATH,
    {keepCase: true,
     longs: String,
     enums: String,
     defaults: true,
     oneofs: true
    });
var bidi = grpc.loadPackageDefinition(packageDefinition).laogao.bidi;
var client = new bidi.Pipe('localhost:9000', grpc.credentials.createInsecure());

var COORD_FACTOR = 1e7;

function runSmoke(callback) {
  var call = client.smoke();
  call.on('data', function(res) {
    console.log('Got message "' + res.message);
  });

  call.on('end', callback);

  for (var i = 0; i < 10000; i++) {
    call.write({"message": "" + i});
  }
  call.end();
}

/**
 * Run all of the demos in order
 */
function main() {
  async.series([
    runSmoke
  ]);
}

if (require.main === module) {
  main();
}

exports.runSmoke = runSmoke;
