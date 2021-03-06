process.env.CHROME_BIN = require('puppeteer').executablePath();
const webpackConfig = require('./webpack.config');

module.exports = function setConfig(config) {
  config.set({
    frameworks: ['mocha', 'sinon-chai', 'webpack'],

    preprocessors: {
      '**/*.spec.js': ['webpack'],
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

    browserNoActivityTimeout: 900000,

    singleRun: true,
    browsers: ['ChromeHeadless'],
    // browsers: ['Chrome'],

  });
};
