var path = require('path');

var root        = path.join(__dirname, "..");
var nodeModules = path.join(root, "node_modules");
var jsDest      = path.join(root, "resources", "public", "js");


var marked     = path.join(nodeModules, "marked", "marked.min.js");
var markedDest = path.join(jsDest, "marked", "marked.min.js");




var libs = {"marked": {"source": marked, "dest": markedDest}};



module.exports = libs;

