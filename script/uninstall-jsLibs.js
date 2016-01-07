var fs   = require('fs-extra'),
    path = require('path'),
    libs = require('./frontend.js');

function removeLib(spec) {
  var dest = spec.dest,
      dir  = path.dirname(dest);

  console.log("deleting: " + dir);

  try {
    fs.removeSync(dir);
  } catch(e) {
    console.error(e)
  }
}

for (var propname in libs) {
  removeLib(libs[propname])
}
