const path = require('path');
const webpack = require('webpack');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ForkTsCheckerWebpackPlugin = require('fork-ts-checker-webpack-plugin');

const shouldUseSourceMap = process.env.GENERATE_SOURCEMAP !== 'false';
const imageInlineSizeLimit = parseInt(process.env.IMAGE_INLINE_SIZE_LIMIT || '10000');

const isEnvDevelopment = process.env.NODE_ENV === 'development';
const isEnvProduction = process.env.NODE_ENV === 'production';
const isEnvProductionProfile = isEnvProduction && process.argv.includes('--profile');

module.exports = function () {
    return {
        stats: 'errors-warnings',
        mode: isEnvProduction ? 'production' : 'development',
        bail: isEnvProduction,
        devtool: isEnvProduction ? (shouldUseSourceMap ? 'source-map' : false) : 'cheap-module-source-map',
        devServer: {
            allowedHosts: ['localhost'],
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
                    changeOrigin: true,
                },
            ],
        },
        entry: {
            main: path.resolve(__dirname, 'src', 'index.tsx'),
        },
        output: {
            path: path.resolve(__dirname, 'build'),
            clean: true,
            filename: isEnvProduction ? 'static/js/[name].[contenthash:8].js' : 'static/js/[name].bundle.js',
            chunkFilename: isEnvProduction ? 'static/js/[name].[contenthash:8].chunk.js' : 'static/js/[name].chunk.js',
            assetModuleFilename: '[name].[hash][ext]',
            publicPath: '/organisaatio-service/',
        },
        cache: {
            type: 'filesystem',
            buildDependencies: {
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
                '@opetushallitus/virkailija-ui-components': path.resolve(__dirname, 'src', 'virkailija-ui-components'),
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
                    test: /\.(js|mjs|jsx|ts|tsx)$/,
                    loader: 'source-map-loader',
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
                        { test: /\.svg$/, type: 'asset/inline' },
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
                            use: [
                                'style-loader',
                                {
                                    loader: 'css-loader',
                                    options: {
                                        esModule: false,
                                        modules: {
                                            exportLocalsConvention: 'as-is',
                                        },
                                    },
                                },
                            ],
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
                favicon: path.resolve(__dirname, 'public', 'favicon.ico'),
                chunks: ['main'],
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
        ],
    };
};
