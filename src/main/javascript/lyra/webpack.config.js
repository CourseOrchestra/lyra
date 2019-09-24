const VueLoaderPlugin = require('vue-loader/lib/plugin');
const DojoWebpackPlugin = require('dojo-webpack-plugin');
const loaderConfig = require('./config/loaderConfig');


module.exports = {
  mode: 'production',

  module: {
    rules: [
      {
        test: /\.css$/,
        use: [
          'vue-style-loader',
          'css-loader',
        ],
      }, {
        test: /\.vue$/,
        loader: 'vue-loader',
        options: {
          loaders: {},
        },
      },
      {
        test: /\.js$/,
        loader: 'babel-loader',
        exclude: /node_modules/,
      },
      {
        test: /\.(png|jpg|gif|svg)$/,
        loader: 'file-loader',
        options: {
          name: '[name].[ext]?[hash]',
        },
      },
    ],
  },

  resolve: {
    alias: {
      vue$: 'vue/dist/vue.esm.js',
    },
    extensions: ['*', '.js', '.vue', '.json'],
  },

  performance: {
    hints: false,
  },

  devtool: '#eval-source-map',

  plugins: [
    new VueLoaderPlugin(),
    new DojoWebpackPlugin({
      loaderConfig,
      environment: { dojoRoot: '.' }, // used at run time for non-packed resources (e.g. blank.gif)
      buildEnvironment: { dojoRoot: 'node_modules' }, // used at build time
      locales: ['en', 'ru'],
    }),
  ],

};
