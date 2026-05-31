import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import fs from 'fs'
import path from 'path'
import { fileURLToPath } from 'url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

// Custom plugin to redirect individual 'es-toolkit/compat/*' CJS imports
// to their ESM equivalents in 'es-toolkit/dist/compat/*' wrapped as default exports.
const esToolkitCompatPlugin = () => {
  const virtualModules = new Map();

  return {
    name: 'es-toolkit-compat-esm',
    resolveId(source) {
      const normalizedSource = source.replace(/\\/g, '/');
      if (normalizedSource.includes('es-toolkit/compat/')) {
        const matchFunc = normalizedSource.match(/es-toolkit\/compat\/([^./?#]+)/);
        if (matchFunc) {
          const funcName = matchFunc[1];
          const compatJsPath = path.resolve(__dirname, 'node_modules/es-toolkit/compat', `${funcName}.js`);
          if (fs.existsSync(compatJsPath)) {
            const content = fs.readFileSync(compatJsPath, 'utf-8');
            const match = content.match(/require\(['"]([^'"]+)['"]\)\.([a-zA-Z0-9_$]+);/);
            if (match) {
              const relPath = match[1].replace(/\.js$/, '.mjs');
              const propName = match[2];
              const resolvedPath = path.resolve(path.dirname(compatJsPath), relPath);
              if (fs.existsSync(resolvedPath)) {
                const virtualId = `\0es-toolkit-compat:${funcName}`;
                virtualModules.set(virtualId, {
                  resolvedPath,
                  propName
                });
                return virtualId;
              }
            }
          }
        }
      }
      return null;
    },
    load(id) {
      if (id.startsWith('\0es-toolkit-compat:')) {
        const meta = virtualModules.get(id);
        if (meta) {
          const { resolvedPath, propName } = meta;
          const normalizedPath = resolvedPath.replace(/\\/g, '/');
          return `import { ${propName} } from "${normalizedPath}"; export default ${propName};`;
        }
      }
      return null;
    }
  }
}

// https://vite.dev/config/
export default defineConfig({
  plugins: [react(), esToolkitCompatPlugin()],
  optimizeDeps: {
    include: ['recharts'],
    exclude: ['es-toolkit']
  },
  server: {
    host: true,   // équivalent de --host 0.0.0.0 → nécessaire dans Docker
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
