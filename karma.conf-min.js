module.exports = function(config) {
  var root = 'resources/public/js/compiled/test_out'// same as :output-dir

  config.set({
    frameworks: ['cljs-test'],

    files: [
      'resources/public/js/compiled/test-min.js',// same as :output-to
    ],

    client: {
      // main function
      args: ['test.runner.run']
    },

    browsers: ['Chrome', 'Firefox', 'Safari'],

    // singleRun set to false does not work!
    singleRun: true
  })
}