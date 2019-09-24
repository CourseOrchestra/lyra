module.exports = {
  env: {
    browser: true,
    es6: true,
    node: true
  },
  parserOptions: {
    parser: 'babel-eslint'
  },
  extends: [
    'airbnb-base',
    'plugin:vue/recommended'
  ],
  rules: {
    'import/no-extraneous-dependencies': [
      'error',
      {
        devDependencies: [
          '**/*.spec.js',
          'webpack.config.js',
          'karma.conf.js'
        ]
      }
    ],
    'no-underscore-dangle': [
      'error',
      {
        allow: [
          '_restore',
          '_total',
          '__vue__',
          '_updateColumns',
          '_started',
          '_resizedColumns',
          '_getResizedColumnWidths',
          '_focusedNode',
          '_columns'
        ]
      }
    ]
  }
};
