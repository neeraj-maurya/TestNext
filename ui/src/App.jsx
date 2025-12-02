import React, { useEffect } from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Box, CssBaseline } from '@mui/material'
import Navbar from './components/Navbar'
import Sidebar from './components/Sidebar'
import Login from './pages/Login'
import ProjectsPage from './pages/ProjectsPage'
import StepDefinitionsPage from './pages/StepDefinitionsPage'
import SuiteEditor from './pages/SuiteEditor'
import SuiteDetails from './pages/SuiteDetails'
import ProjectExecutions from './pages/ProjectExecutions'
import ProjectTests from './pages/ProjectTests'
import ExecutionView from './pages/ExecutionView'

// Admin Pages
import AdminTenantManagement from './pages/admin/AdminTenantManagement'
import AdminUserManagement from './pages/admin/AdminUserManagement'
import AdminRoleManagement from './pages/admin/AdminRoleManagement'
import AdminTestSuites from './pages/admin/AdminTestSuites'
import AdminStepDefinitions from './pages/admin/AdminStepDefinitions'

// Tenant Pages
import TenantTestSuites from './pages/tenant/TenantTestSuites'
import TenantTestCases from './pages/tenant/TenantTestCases'
import TenantExecution from './pages/tenant/TenantExecution'
import TenantReports from './pages/tenant/TenantReports'

export default function App() {
  const [user, setUser] = React.useState(null)
  const [isLoading, setIsLoading] = React.useState(true)

  // Load user from localStorage on mount
  useEffect(() => {
    const savedUser = localStorage.getItem('currentUser')
    if (savedUser) {
      setUser(savedUser)
    }
    setIsLoading(false)
  }, [])

  const handleLogin = (username) => {
    setUser(username)
    localStorage.setItem('currentUser', username)
  }

  const handleLogout = () => {
    setUser(null)
    localStorage.removeItem('currentUser')
  }

  const isAdmin = user === 'admin'

  if (isLoading) return <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>Loading...</Box>
  if (!user) return <Login onLogin={handleLogin} />

  return (
    <>
      <CssBaseline />
      <BrowserRouter>
        <Navbar user={user} onLogout={handleLogout} onSwitchUser={handleLogin} />
        <Box sx={{ display: 'flex', minHeight: 'calc(100vh - 64px)' }}>
          <Sidebar isAdmin={isAdmin} />
          <Box component="main" sx={{ flexGrow: 1, p: 3, width: '100%', overflowX: 'auto' }}>
            <Routes>
              {/* Admin Routes */}
              {isAdmin && (
                <>
                  <Route path="/admin/tenants" element={<AdminTenantManagement />} />
                  <Route path="/admin/users" element={<AdminUserManagement />} />
                  <Route path="/admin/roles" element={<AdminRoleManagement />} />
                  <Route path="/admin/test-suites" element={<AdminTestSuites />} />
                  <Route path="/admin/step-definitions" element={<AdminStepDefinitions />} />
                </>
              )}

              {/* Tenant User Routes */}
              {!isAdmin && (
                <>
                  <Route path="/tenant/test-suites" element={<TenantTestSuites />} />
                  <Route path="/tenant/test-cases" element={<TenantTestCases />} />
                  <Route path="/tenant/execution" element={<TenantExecution />} />
                  <Route path="/tenant/reports" element={<TenantReports />} />
                </>
              )}

              {/* Shared Routes */}
              <Route path="/projects" element={<ProjectsPage />} />
              <Route path="/projects/:projectId/executions" element={<ProjectExecutions />} />
              <Route path="/projects/:projectId/tests" element={<ProjectTests />} />
              <Route path="/projects/:projectId/suites/:suiteId" element={<SuiteDetails />} />
              <Route path="/steps" element={<StepDefinitionsPage />} />
              <Route path="/suite-editor" element={<SuiteEditor />} />
              <Route path="/execution" element={<ExecutionView />} />

              {/* Default redirect */}
              <Route path="/" element={isAdmin ? <Navigate to="/admin/tenants" replace /> : <Navigate to="/tenant/test-suites" replace />} />
              <Route path="*" element={isAdmin ? <Navigate to="/admin/tenants" replace /> : <Navigate to="/tenant/test-suites" replace />} />
            </Routes>
          </Box>
        </Box>
      </BrowserRouter>
    </>
  )
}
