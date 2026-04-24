// eslint.config.js
import js from "@eslint/js";
import tseslint from "typescript-eslint";
import reactPlugin from "eslint-plugin-react";
import reactHooksPlugin from "eslint-plugin-react-hooks";
import reactRefreshPlugin from "eslint-plugin-react-refresh";
import prettierConfig from "eslint-config-prettier";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

export default tseslint.config(
  js.configs.recommended,
  ...tseslint.configs.recommended,
  reactPlugin.configs.flat.recommended,
  reactPlugin.configs.flat["jsx-runtime"],
  prettierConfig,
  {
    ignores: [
      "dist/**",
      "node_modules/**",
      // Shadcn CLI artifacts accidentally generated at repo root
      "@/**",
      // Build/tooling config files (outside TS project includes)
      "eslint.config.js",
      "postcss.config.js",
      "tailwind.config.js",
      "vite.config.ts",
    ],
  },
  {
    files: ["**/*.{ts,tsx}"],
    languageOptions: {
      parserOptions: {
        // Use the actual TS projects that include source files.
        project: ["./tsconfig.app.json", "./tsconfig.node.json"],
        tsconfigRootDir: __dirname, // 👈 加上这一行
      },
    },
    plugins: {
      "react-hooks": reactHooksPlugin,
      "react-refresh": reactRefreshPlugin,
    },
    rules: {
      ...reactHooksPlugin.configs.recommended.rules,
      // This rule is not part of the mainstream React Hooks ESLint set and
      // incorrectly flags common data-fetch patterns.
      "react-hooks/set-state-in-effect": "off",
      "react-refresh/only-export-components": [
        "warn",
        { allowConstantExport: true },
      ],
      "@typescript-eslint/no-unused-vars": [
        "error",
        { argsIgnorePattern: "^_" },
      ],
      "@typescript-eslint/no-explicit-any": "warn",
    },
    settings: {
      react: { version: "detect" },
    },
  },
);
