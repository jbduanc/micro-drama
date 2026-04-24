import { createBrowserRouter, Navigate } from 'react-router-dom'
import { MainLayout } from '@/components/layout/MainLayout'
import { lazy, Suspense } from 'react'
import { RequireAuth } from './RequireAuth'

// 懒加载页面
const Drama = lazy(() => import('@/pages/drama/List'))
const DramaEdit = lazy(() => import('@/pages/drama/Edit'))
const User = lazy(() => import('@/pages/user/List'))
const Plan = lazy(() => import('@/pages/plan/List'))
const Admin = lazy(() => import('@/pages/admin/List'))
const Login = lazy(() => import('@/pages/login'))
const OAuthCallback = lazy(() => import('@/pages/oauth/Callback'))

// 加载占位
const Loading = () => <div className="flex h-full items-center justify-center">页面加载中...</div>

export const router = createBrowserRouter([
  {
    path: '/login',
    element: (
      <Suspense fallback={<Loading />}>
        <Login />
      </Suspense>
    ),
  },
  {
    path: '/oauth/callback',
    element: (
      <Suspense fallback={<Loading />}>
        <OAuthCallback />
      </Suspense>
    ),
  },
  {
    path: '/',
    element: (
      <RequireAuth>
        <MainLayout />
      </RequireAuth>
    ), // 父路由：只放布局
    children: [
      // ✅ 索引路由（单独写，负责重定向）
      { index: true, element: <Navigate to="/users" replace /> },
      // ✅ 子路由页面
      {
        path: 'dramas',
        element: (
          <Suspense fallback={<Loading />}>
            <Drama />
          </Suspense>
        ),
      },
      {
        path: 'dramas/new',
        element: (
          <Suspense fallback={<Loading />}>
            <DramaEdit />
          </Suspense>
        ),
      },
      {
        path: 'dramas/:dramaId',
        element: (
          <Suspense fallback={<Loading />}>
            <DramaEdit />
          </Suspense>
        ),
      },
      {
        path: 'users',
        element: (
          <Suspense fallback={<Loading />}>
            <User />
          </Suspense>
        ),
      },
      {
        path: 'plans',
        element: (
          <Suspense fallback={<Loading />}>
            <Plan />
          </Suspense>
        ),
      },
      {
        path: 'admins',
        element: (
          <Suspense fallback={<Loading />}>
            <Admin />
          </Suspense>
        ),
      },
    ],
  },
])
