const path = require('path');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const MiniCssExtractPlugin = require('mini-css-extract-plugin');
const { WebpackManifestPlugin } = require('webpack-manifest-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const { createHash } = require('crypto');

const shouldUseSourceMap = process.env.GENERATE_SOURCEMAP !== 'false';
const imageInlineSizeLimit = parseInt(process.env.IMAGE_INLINE_SIZE_LIMIT || '10000');

const isEnvDevelopment = process.env.NODE_ENV === 'development';
const isEnvProduction = process.env.NODE_ENV === 'production';
const isEnvProductionProfile = isEnvProduction && process.argv.includes('--profile');

const createEnvironmentHash = () => {
    const hash = createHash('md5');
    hash.update(JSON.stringify({ NODE_ENV: process.env.NODE_ENV }));
    return hash.digest('hex');
};

module.exports = function () {
    return {
        target: ['browserslist'],
        stats: 'errors-warnings',
        mode: isEnvProduction ? 'production' : 'development',
        bail: isEnvProduction,
        devtool: isEnvProduction ? (shouldUseSourceMap ? 'source-map' : false) : 'cheap-module-source-map',
        devServer: {
            allowedHosts: 'auto',
            headers: {
                'Access-Control-Allow-Origin': '*',
                'Access-Control-Allow-Methods': '*',
                'Access-Control-Allow-Headers': '*',
            },
            compress: true,
            static: {
                directory: path.resolve(__dirname, 'public'),
                publicPath: ['/organisaatio-service/'],
                watch: {
                    ignored: {},
                },
            },
            client: {
                overlay: {
                    errors: true,
                    warnings: false,
                },
            },
            devMiddleware: {
                publicPath: '/organisaatio-service',
            },
            historyApiFallback: {
                disableDotRule: true,
                index: '/organisaatio-service/',
            },
            host: '127.0.0.1',
            hot: true,
            port: 3003,
            proxy: [
                {
                    target: 'http://localhost:8080',
                    context: [
                        '/organisaatio-service/api',
                        '/organisaatio-service/rest',
                        '/organisaatio-service/internal',
                        '/organisaatio-service/mock',
                    ],
                },
            ],
        },
        entry: {
            main: path.resolve(__dirname, 'src', 'index.tsx'),
        },
        output: {
            path: path.resolve(__dirname, 'build'),
            pathinfo: isEnvDevelopment,
            filename: isEnvProduction
                ? 'static/js/[name].[contenthash:8].js'
                : isEnvDevelopment && 'static/js/[name].bundle.js',
            chunkFilename: isEnvProduction
                ? 'static/js/[name].[contenthash:8].chunk.js'
                : isEnvDevelopment && 'static/js/[name].chunk.js',
            assetModuleFilename: 'static/media/[name].[hash][ext]',
            publicPath: '/organisaatio-service/',
            devtoolModuleFilenameTemplate: isEnvProduction
                ? (info) => path.relative(path.resolve(__dirname, 'src'), info.absoluteResourcePath).replace(/\\/g, '/')
                : isEnvDevelopment && ((info) => path.resolve(info.absoluteResourcePath).replace(/\\/g, '/')),
        },
        cache: {
            type: 'filesystem',
            version: createEnvironmentHash(),
            cacheDirectory: path.resolve(__dirname, 'node_modules', '.cache'),
            store: 'pack',
            buildDependencies: {
                defaultWebpack: ['webpack/lib/'],
                config: [__filename],
                tsconfig: [path.resolve(__dirname, 'tsconfig.json')],
            },
        },
        resolve: {
            extensions: [
                '.web.mjs',
                '.mjs',
                '.web.js',
                '.js',
                '.web.ts',
                '.ts',
                '.web.tsx',
                '.tsx',
                '.json',
                '.web.jsx',
                '.jsx',
            ],
            alias: {
                ...(isEnvProductionProfile && {
                    'react-dom$': 'react-dom/profiling',
                    'scheduler/tracing': 'scheduler/tracing-profiling',
                }),
            },
        },
        module: {
            strictExportPresence: true,
            rules: [
                shouldUseSourceMap && {
                    enforce: 'pre',
                    test: /\.(js|mjs|jsx|ts|tsx|css)$/,
                    loader: 'source-map-loader',
                    exclude: /virkailija-ui-components/,
                },
                {
                    oneOf: [
                        {
                            test: [/\.bmp$/, /\.gif$/, /\.jpe?g$/, /\.png$/],
                            type: 'asset',
                            parser: {
                                dataUrlCondition: {
                                    maxSize: imageInlineSizeLimit,
                                },
                            },
                        },
                        { test: /\.svg/, type: 'asset/inline' },
                        {
                            test: /\.(js|mjs|jsx|ts|tsx)?$/,
                            exclude: /node_modules/,
                            use: [
                                {
                                    loader: 'ts-loader',
                                    options: {
                                        transpileOnly: isEnvDevelopment,
                                    },
                                },
                            ],
                        },
                        {
                            test: /\.css$/,
                            exclude: /\.module\.css$/,
                            use: ['style-loader', 'css-loader'],
                            sideEffects: true,
                        },
                        {
                            test: /\.module\.css$/,
                            use: ['style-loader', 'css-loader'],
                        },
                        {
                            exclude: [/^$/, /\.(js|mjs|jsx|ts|tsx)$/, /\.html$/, /\.json$/],
                            type: 'asset/resource',
                        },
                    ],
                },
            ].filter(Boolean),
        },
        plugins: [
            new HtmlWebpackPlugin({
                filename: 'index.html',
                template: path.resolve(__dirname, 'public', 'index.html'),
                chunks: ['main'],
            }),
            new webpack.DefinePlugin({
                'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV),
            }),
            isEnvProduction &&
                new MiniCssExtractPlugin({
                    filename: 'static/css/[name].[contenthash:8].css',
                    chunkFilename: 'static/css/[name].[contenthash:8].chunk.css',
                }),
            new CopyWebpackPlugin({
                patterns: [{ from: path.resolve(__dirname, 'public', 'favicon.ico') }],
            }),
            new WebpackManifestPlugin({
                fileName: 'asset-manifest.json',
                publicPath: '/organisaatio-service/',
                generate: (seed, files, entrypoints) => {
                    const manifestFiles = files.reduce((manifest, file) => {
                        manifest[file.name] = file.path;
                        return manifest;
                    }, seed);
                    const entrypointFiles = entrypoints.main.filter((fileName) => !fileName.endsWith('.map'));

                    return {
                        files: manifestFiles,
                        entrypoints: entrypointFiles,
                    };
                },
            }),
            new webpack.IgnorePlugin({
                resourceRegExp: /^\.\/locale$/,
                contextRegExp: /moment$/,
            }),
            new ForkTsCheckerWebpackPlugin({
                typescript: {
                    configOverwrite: {
                        compilerOptions: {
                            sourceMap: isEnvProduction ? shouldUseSourceMap : isEnvDevelopment,
                            noEmit: true,
                            incremental: true,
                            tsBuildInfoFile: path.resolve(__dirname, 'node_modules', '.cache', 'tsconfig.tsbuildinfo'),
                        },
                    },
                    diagnosticOptions: {
                        syntactic: true,
                    },
                    mode: 'write-references',
                },
            }),
        ].filter(Boolean),
    };
};
