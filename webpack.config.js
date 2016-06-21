var webpack = require('webpack');
var copyWebpackPlugin = require('copy-webpack-plugin');

module.exports = {
    output: {
        path: './organisaatio-ui/target/organisaatio-ui/jslib',
        filename: 'vendors.js'
    },
    entry: {
        vendors: ["jquery", "angular", "angular-cookies", "angular-mocks", "angular-resource", "angular-route",
            "angular-sanitize", "script!./node_modules/tinymce/tinymce.js", "angular-ui-tinymce",
            "angular-ui-bootstrap", "angular-idle", "ui-select", "moment", "jquery.ui.widget", "jquery.iframe-transport",
            "jquery.fileupload"
        ]
    },
    resolve: {
        modulesDirectories: ['node_modules', 'web_modules'],
        alias: {
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
    module: {
        loaders: [
            { test: /jquery\.js$/, loader: 'expose?$!expose?jQuery' },
            { test: /moment\.js$/, loader: 'expose?moment' },
            { test: /jquery\.ui\.widget\.js$/, loader: 'imports?define=>false' },
            { test: /jquery\.iframe\-transport\.js$/, loader: 'imports?define=>false' },
            { test: /jquery\.fileupload\.js$/, loader: 'imports?define=>false' }
        ]
    },
    plugins: [
        new copyWebpackPlugin([
            { from: './node_modules/tinymce/plugins', to: './plugins' },
            { from: './node_modules/tinymce/themes', to: './themes' },
            { from: './node_modules/tinymce/skins', to: './skins' }
        ])
    ]
};
