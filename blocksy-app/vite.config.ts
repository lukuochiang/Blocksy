import path from "path";
import { defineConfig } from "vite";
import { createRequire } from "module";

process.env.UNI_INPUT_DIR = path.resolve(__dirname, "src");
process.env.UNI_OUTPUT_DIR = path.resolve(__dirname, "dist");

const require = createRequire(import.meta.url);
const uniPlugin = require("@dcloudio/vite-plugin-uni").default;

export default defineConfig({
  plugins: [uniPlugin()],
  logLevel: "info",
  clearScreen: false,
  server: {
    host: "0.0.0.0",
    port: 5174,
    strictPort: true,
    allowedHosts: true
  },
  // 使用 public 目录作为静态资源目录
  publicDir: 'public'
});
