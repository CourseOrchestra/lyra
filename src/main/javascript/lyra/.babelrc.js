module.exports = {
  presets: [
    [
      '@babel/env',
      {
        modules: false
      }
    ]
  ],
  plugins: [
    'rewire',
    'istanbul'
  ]
};
