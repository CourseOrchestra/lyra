const VueLoaderPlugin = require('vue-loader/lib/plugin');
const DojoWebpackPlugin = require('dojo-webpack-plugin');
const loaderConfig = require('./config/loaderConfig');

module.exports = {
  mode: 'development',

  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          'style-loader',
          'css-loader',
        ],
      }, {
        test: /\.vue$/,
        loader: 'vue-loader',
      },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.(png|jpg|gif|svg)$/,
        loader: 'url-loader',
      },
    ],
  },

  plugins: [
    new VueLoaderPlugin(),
    new DojoWebpackPlugin({
      loaderConfig,
      environment: { dojoRoot: '.' }, // used at run time for non-packed resources (e.g. blank.gif)
      buildEnvironment: { dojoRoot: 'node_modules' }, // used at build time
      locales: ['en', 'ru'],
    }),
  ],

  optimization: {
    splitChunks: false,
  },

};
