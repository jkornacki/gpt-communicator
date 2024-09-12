import {fileURLToPath, URL} from 'node:url'

import {build, defineConfig} from 'vite'
import {resolve} from 'path';
import fs from 'fs-extra';
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vitejs.dev/config/
export default defineConfig({

    build: {
        outDir: '../src/main/resources/static/dist',
        sourcemap: true,
        emptyOutDir: true,
        minify: false,
        cssCodeSplit: false,
        rollupOptions: {
            output: {
                manualChunks: () => 'everything.js',
                entryFileNames: '[name].js',
                chunkFileNames: '[name].js',
                assetFileNames: '[name].[ext]'
            }
        }
    },
    plugins: [
        vue(),
        vueDevTools(),
    ],
    optimizeDeps: {
        entries: [],
    },
    resolve: {
        alias: {
            '@': fileURLToPath(new URL('./src', import.meta.url))
        }
    }
})
