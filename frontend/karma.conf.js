// Karma configuration for running validation tests

module.exports = function (config) {
    config.set({
        // base path that will be used to resolve all patterns (eg. files, exclude)
        basePath: '.',

        // frameworks to use
        // available frameworks: https://npmjs.org/browse/keyword/karma-adapter
        frameworks: ['jasmine'],

        // list of files / patterns to load in the browser
        files: [
            // Program files
            '_public/frontend/js/vendor.other.min.js',
            '_public/frontend/js/vendor.angular.min.js',
            '_public/frontend/js/app.min.js',
            '_public/frontend/js/templates.js',
            'test-metadata.js',

            // Load mocks for angular
            '../node_modules/angular-mocks/angular-mocks.js',

            // Specs
            'app/module/**/*.spec.js'
        ],

        // list of files to exclude
        exclude: [],

        // preprocess matching files before serving them to the browser
        // available preprocessors: https://npmjs.org/browse/keyword/karma-preprocessor
        preprocessors: {},

        // test results reporter to use
        // possible values: 'dots', 'progress'
        // available reporters: https://npmjs.org/browse/keyword/karma-reporter
        reporters: ['spec'],

        // web server port
        port: 9876,

        // cli runner port
        runnerPort: 9100,

        // enable / disable colors in the output (reporters and logs)
        colors: true,

        // level of logging
        // possible values: config.LOG_DISABLE || config.LOG_ERROR || config.LOG_WARN || config.LOG_INFO || config.LOG_DEBUG
        logLevel: config.LOG_INFO,

        // enable / disable watching file and executing tests whenever any file changes
        autoWatch: true,

        // start these browsers
        // available browser launchers: https://npmjs.org/browse/keyword/karma-launcher
        // browsers: ['Chrome'],
        browsers: ['PhantomJS'],

        // Plugins to load
        plugins: [
            'karma-junit-reporter',
            'karma-spec-reporter',
            'karma-jasmine',
            // 'karma-chrome-launcher'
            'karma-phantomjs-launcher'
        ],

        // JUnit reporter
        junitReporter: {
            outputDir: '../target/jasmine'
        },

        specReporter: {
            suppressErrorSummary: true,
            suppressFailed: false,
            suppressPassed: true,
            suppressSkipped: true,
            showSpecTiming: false
        },

        // Continuous Integration mode
        // if true, Karma captures browsers, runs the tests and exits
        singleRun: false
    });
};
