const path = require('path');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

const common = require('./webpack.common.js');

module.exports = {
    ...common,
    mode: 'development',
    output: {
        filename: '[name].[contenthash].js',
        path: path.resolve(__dirname, '../target/classes/public/'),
        clean: true,
    },
    devtool: 'source-map',
    plugins: [
        new ForkTsCheckerWebpackPlugin(),
        new HtmlWebpackPlugin({
            template: './scripts/html-template.html',
            publicPath: '/',
        }),
        new CopyWebpackPlugin({
            patterns: [{ from: 'public' }],
        }),
    ],
    optimization: {
        splitChunks: {
            cacheGroups: {
                vendor: {
                    test: /[\\/]node_modules[\\/]/,
                    name: 'vendors',
                    chunks: 'all',
                },
            },
        },
    },
};
