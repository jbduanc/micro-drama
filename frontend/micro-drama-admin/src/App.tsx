import { TooltipProvider } from '@/components/ui/tooltip'
import { Toaster } from 'sonner'
import { RouterProvider } from 'react-router-dom'
import { Suspense } from 'react'
import { router } from './routes'

function App() {
  return (
    <TooltipProvider>
      <Suspense
        fallback={<div className="flex h-screen items-center justify-center">Loading...</div>}
      >
        <RouterProvider router={router} />
      </Suspense>
      <Toaster />
    </TooltipProvider>
  )
}

export default App
