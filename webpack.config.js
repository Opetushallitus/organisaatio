var webpack = require('webpack');

module.exports = {
    output: {
        path: './organisaatio-ui/target/organisaatio-ui/jslib',
        filename: 'bundle.js'
    },
    entry: {
        library: './index'
    },
    resolve: {
        modulesDirectories: ['node_modules', 'web_modules'],
        alias: {
            "tinymce": 'tinymce/tinymce.min',
            "angular-ui-bootstrap": 'angular-ui-bootstrap/ui-bootstrap-tpls',
            "angular-idle": 'ng-idle/angular-idle',
            "moment": 'moment/moment',
            "angular-ui-tinymce": 'angular-ui-tinymce/src/tinymce',
            "ui-select": 'ui-select/dist/select'
        }
    },
    // plugins: [
    //     new webpack.ProvidePlugin({
    //         $: "jquery",
    //         jQuery: "jquery",
    //         "window.jQuery": "jquery"
    //     })
    // ],
    module: {
        loaders: [
            { test: /jquery\.js$/, loader: 'expose?$' },
            { test: /jquery\.js$/, loader: 'expose?jQuery' },
            { test: /jquery\.js$/, loader: 'expose?window.jQuery' },
            { test: /moment\.js$/, loader: 'expose?moment' }
        ]
    }
};
