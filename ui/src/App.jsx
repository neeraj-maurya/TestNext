import React from 'react'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import Login from './pages/Login'
import ProjectsPage from './pages/ProjectsPage'
import StepDefinitionsPage from './pages/StepDefinitionsPage'
import SuiteEditor from './pages/SuiteEditor'
import ExecutionView from './pages/ExecutionView'

export default function App(){
  const [user, setUser] = React.useState(null)
  if (!user) return <Login onLogin={u=>setUser(u)} />
  return (
    <BrowserRouter>
      <Navbar onLogout={()=>setUser(null)} />
      <Routes>
        <Route path="/" element={<Navigate to="/projects" replace />} />
        <Route path="/projects" element={<ProjectsPage />} />
        <Route path="/steps" element={<StepDefinitionsPage />} />
        <Route path="/suite-editor" element={<SuiteEditor />} />
        <Route path="/execution" element={<ExecutionView />} />
      </Routes>
    </BrowserRouter>
  )
}
