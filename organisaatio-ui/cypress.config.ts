import { defineConfig } from 'cypress';

export default defineConfig({
    defaultCommandTimeout: 10000,
    pageLoadTimeout: 15000,
    video: false,
    e2e: {
        // We've imported your old cypress plugins here.
        // You may want to clean this up later by importing these.
        setupNodeEvents(on, config) {
            return require('./cypress/plugins/index.js')(on, config);
        },
        baseUrl: 'http://devaaja:devaaja@localhost:3003/',
    },
});
