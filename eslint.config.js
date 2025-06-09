import js from '@eslint/js'
import globals from 'globals'

export default [
    js.configs.recommended,
    {
        files: ['src/main/js/**/*.js'],
        languageOptions: {
            ecmaVersion: 2022,
            sourceType: 'module',
            globals: {
                ...globals.browser,
                bootstrap: 'readonly',
                Chart: 'readonly'
            }
        },
        rules: {
            'no-unused-vars': 'warn',
            'no-console': ['warn', { allow: ['warn', 'error'] }],
            'semi': ['error', 'never'],
            'quotes': ['error', 'single'],
            'indent': ['error', 4],
            'no-trailing-spaces': 'error',
            'comma-dangle': ['error', 'never'],
            'object-curly-spacing': ['error', 'always'],
            'array-bracket-spacing': ['error', 'never']
        }
    }
]