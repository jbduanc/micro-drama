/// <reference types="vite/client" />

// 让 TypeScript 识别 CSS 文件
declare module '*.css' {
  const content: string
  export default content
}
