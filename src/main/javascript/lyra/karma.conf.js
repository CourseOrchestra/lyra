process.env.CHROME_BIN = require('puppeteer')
  .executablePath();
const webpackConfig = require('./webpack.config.js');

module.exports = function setConfig(config) {
  config.set({
    frameworks: ['mocha', 'sinon-chai'],

    preprocessors: {
      '**/*.spec.js': ['webpack', 'sourcemap'],
    },

    files: [
      'test/unit/*.spec.js',
      {
        pattern: 'node_modules/dojo/resources/*.gif',
        watched: false,
        included: false,
        served: true,
        nocache: false,
      },
    ],

    proxies: {
      '/dojo/resources/': '/base/node_modules/dojo/resources/',
    },


    webpack: webpackConfig,

    reporters: ['spec', 'coverage'],

    coverageReporter: {
      dir: '../../../../target/client-coverage',

      reporters: [
        {
          type: 'lcov',
          subdir: '.',
        },
        { type: 'text-summary' },
        { type: 'text' },
        {
          type: 'cobertura',
          subdir: '.',
        },
      ],
    },

    singleRun: true,

    browserNoActivityTimeout: 900000,

    browsers: ['ChromeHeadless'],
    // browsers: ['Chrome'],

  });
};
