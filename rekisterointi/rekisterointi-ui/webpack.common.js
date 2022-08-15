module.exports = {
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
                test: /\.css$/,
                use: [{ loader: 'style-loader' }, { loader: 'css-loader' }],
            },
            {
                test: /\.(?:png|gif|jpe?g|svg)$/,
                type: 'asset/inline',
            },
        ],
    },
    resolve: { extensions: ['.ts', '.tsx', '.js'] },
};
