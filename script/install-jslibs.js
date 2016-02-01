var fs   = require('fs-extra'),
    libs = require('./frontend.js');

function copyLib(spec) {
  var source = spec.source,
      dest   = spec.dest;

  console.log("installing: " + source);
  console.log("        to: " + dest);

  try {
    fs.copySync(source, dest);
  } catch(e) {
    console.error(e)
  }
}

for (var propname in libs) {
  copyLib(libs[propname])
}
