var webpack = require('webpack');

module.exports = {
    output: {
        path: './organisaatio-ui/target/organisaatio-ui/jslib',
        filename: 'vendors.js'
    },
    entry: {
        vendors: ["jquery", "angular", "angular-cookies", "angular-mocks", "angular-resource", "angular-route",
            "angular-sanitize", "tinymce", "angular-ui-tinymce", "angular-ui-bootstrap", "angular-idle", "ui-select",
            "moment", "jquery.ui.widget", "jquery.iframe-transport", "jquery.fileupload"
        ]
    },
    resolve: {
        modulesDirectories: ['node_modules', 'web_modules'],
        alias: {
            "tinymce": 'tinymce/tinymce.min',
            "angular-ui-bootstrap": 'angular-ui-bootstrap/ui-bootstrap-tpls',
            "angular-idle": 'ng-idle/angular-idle',
            "moment": 'moment/moment',
            "angular-ui-tinymce": 'angular-ui-tinymce/src/tinymce',
            "ui-select": 'ui-select/dist/select',
            "jquery.ui.widget": "blueimp-file-upload/js/vendor/jquery.ui.widget",
            "jquery.iframe-transport": "blueimp-file-upload/js/jquery.iframe-transport",
            "jquery.fileupload": "blueimp-file-upload/js/jquery.fileupload"
        }
    },
    plugins: [
        new webpack.optimize.CommonsChunkPlugin({
            name: "vendors",
            minChunks: Infinity
        })
    ],
    module: {
        loaders: [
            { test: /jquery\.js$/, loader: 'expose?$' },
            { test: /jquery\.js$/, loader: 'expose?jQuery' },
            { test: /jquery\.js$/, loader: 'expose?window.jQuery' },
            { test: /moment\.js$/, loader: 'expose?moment' },
            { test: /jquery\.ui\.widget\.js$/, loader: 'imports?define=>false&exports=>false' },
            { test: /jquery\.iframe\-transport\.js$/, loader: 'imports?define=>false&exports=>false' },
            { test: /jquery\.fileupload\.js$/, loader: 'imports?define=>false&exports=>false' }
        ]
    }
};
