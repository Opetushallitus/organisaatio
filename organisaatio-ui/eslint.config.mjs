import { defineConfig } from 'eslint/config';
import eslint from '@eslint/js';
import tseslint from 'typescript-eslint';
import react from 'eslint-plugin-react';
import jsxA11y from 'eslint-plugin-jsx-a11y';
import eslintPluginPrettierRecommended from 'eslint-plugin-prettier/recommended';

export default defineConfig([
    eslint.configs.recommended,
    tseslint.configs.recommended,
    react.configs.flat.recommended,
    jsxA11y.flatConfigs.recommended,
    eslintPluginPrettierRecommended,
    {
        settings: {
            react: {
                version: 'detect',
            },
        },

        rules: {
            'jsx-a11y/click-events-have-key-events': 1,
            'jsx-a11y/no-autofocus': 1,
            'jsx-a11y/no-noninteractive-element-interactions': 1,
            'jsx-a11y/no-static-element-interactions': 1,
            '@typescript-eslint/consistent-type-definitions': ['error', 'type'],
            '@typescript-eslint/no-explicit-any': 1,
            '@typescript-eslint/no-invalid-void-type': 1, // does not recognise rtk generic type arguments
            '@typescript-eslint/no-non-null-assertion': 1,
            '@typescript-eslint/no-unused-vars': [
                'error',
                {
                    argsIgnorePattern: '^_',
                    varsIgnorePattern: '^_',
                    caughtErrorsIgnorePattern: '^_',
                },
            ],
        },
    },
    {
        // Note: there should be no other properties in this object
        ignores: ['build/*', 'src/declarations.d.ts', 'node_modules/*', 'webpack.config.js', 'public/dev-raamit.js'],
    },
]);
