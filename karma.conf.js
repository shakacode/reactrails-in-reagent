module.exports = function(config) {
  var root = 'resources/public/js/compiled/test_out'// same as :output-dir

  config.set({
    frameworks: ['cljs-test'],

    files: [
      root + '/goog/base.js',
      root + '/cljs_deps.js',
      'resources/public/js/compiled/test.js',// same as :output-to
      {pattern: root + '/*.js', included: false},
      {pattern: root + '/**/*.js', included: false}
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