const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

const common = require('./webpack.common.js');

module.exports = {
    ...common,
    mode: 'development',
    devServer: {
        port: 3000,
    },
    devtool: 'eval-source-map',
    output: {
        path: path.join(__dirname, 'public'),
        filename: '[name].js',
    },
    plugins: [
        new HtmlWebpackPlugin({
            template: './scripts/html-template.html',
        }),
        new ForkTsCheckerWebpackPlugin(),
    ],
};
