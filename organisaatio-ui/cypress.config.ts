import { defineConfig } from 'cypress';

export default defineConfig({
    defaultCommandTimeout: 10000,
    pageLoadTimeout: 15000,
    video: false,
    e2e: {
        baseUrl: 'http://devaaja:devaaja@localhost:3003/',
    },
});
