export default {
    entry: {
        index: './src/index',
    },
    module: {
        rules: [
            {
                test: /\.tsx?$/,
                exclude: /node_modules/,
                loader: 'ts-loader',
            },
            {
                test: /\.module\.css$/,
                use: [
                    { loader: 'style-loader' },
                    {
                        loader: 'css-loader',
                        options: {
                            modules: {
                                namedExport: false,
                            },
                        },
                    },
                ],
            },
            {
                test: /\.css$/,
                exclude: /\.module\.css$/,
                use: [{ loader: 'style-loader' }, { loader: 'css-loader', options: { modules: false } }],
            },
            {
                test: /\.(?:png|gif|jpe?g|svg)$/,
                type: 'asset/inline',
            },
        ],
    },
    resolve: { extensions: ['.ts', '.tsx', '.js'] },
};
