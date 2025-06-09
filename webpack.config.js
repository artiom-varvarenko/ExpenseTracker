import MiniCssExtractPlugin from 'mini-css-extract-plugin';
import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

// Read all JS files from src/main/js as entries
const jsPath = path.resolve(__dirname, 'src/main/js');
const entries = {};

if (fs.existsSync(jsPath)) {
    fs.readdirSync(jsPath)
        .filter(file => file.endsWith('.js'))
        .forEach(file => {
            const name = file.replace('.js', '');
            entries[name] = path.join(jsPath, file);
        });
}

export default {
    mode: 'development',
    entry: entries,
    output: {
        path: path.resolve(__dirname, 'src/main/resources/static'),
        filename: 'js/bundle-[name].js',
        clean: false // Don't clean the output directory
    },
    module: {
        rules: [
            {
                test: /\.s?css$/i,
                use: [
                    MiniCssExtractPlugin.loader,
                    'css-loader',
                    'sass-loader'
                ]
            }
        ]
    },
    plugins: [
        new MiniCssExtractPlugin({
            filename: 'css/bundle-[name].css'
        })
    ],
    devtool: 'source-map'
};