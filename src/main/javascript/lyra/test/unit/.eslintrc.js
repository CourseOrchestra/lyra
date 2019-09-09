module.exports = {
  env: {
    mocha: true,
  },
  globals: {
    sinon: true,
    expect: true
  },
  plugins: ['chai-friendly', 'chai-expect'],
  rules: {
    'no-unused-expressions': 'off',
    'chai-friendly/no-unused-expressions': 'error',
    'chai-expect/missing-assertion': 'error',
    'chai-expect/terminating-properties': 'error',
    'chai-expect/no-inner-compare': 'error',
    'no-underscore-dangle': [
      'error',
      {
        allow: [
          '__Rewire__',
          '_fetch',
          '_started',
          '_resizedColumns',
          '_updateColumns',
          '__vue__',
          '_stompHandler',
          '_subscriptions'
        ]
      }
    ]
  }
};
