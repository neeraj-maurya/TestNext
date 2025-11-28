import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { Box, CssBaseline } from '@mui/material'
import Navbar from './components/Navbar'
import Sidebar from './components/Sidebar'
import Login from './pages/Login'
import ProjectsPage from './pages/ProjectsPage'
import StepDefinitionsPage from './pages/StepDefinitionsPage'
import SuiteEditor from './pages/SuiteEditor'
import ExecutionView from './pages/ExecutionView'

// Admin Pages (placeholder)
import AdminTenantManagement from './pages/admin/AdminTenantManagement'
import AdminUserManagement from './pages/admin/AdminUserManagement'
import AdminRoleManagement from './pages/admin/AdminRoleManagement'
import AdminTestSuites from './pages/admin/AdminTestSuites'
import AdminStepDefinitions from './pages/admin/AdminStepDefinitions'

// Tenant Pages (placeholder)
import TenantTestSuites from './pages/tenant/TenantTestSuites'
import TenantTestCases from './pages/tenant/TenantTestCases'
import TenantExecution from './pages/tenant/TenantExecution'
import TenantReports from './pages/tenant/TenantReports'

export default function App(){
  const [user, setUser] = React.useState(null)
  const isAdmin = user === 'neera'
  
  if (!user) return <Login onLogin={u=>setUser(u)} />
  
  return (
    <>
      <CssBaseline />
      <BrowserRouter>
        <Navbar user={user} onLogout={()=>setUser(null)} />
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
