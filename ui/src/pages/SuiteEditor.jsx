import React from 'react'
import axios from 'axios'
import DynamicForm from '../components/DynamicForm'
import { Button, TextField, MenuItem, Select, InputLabel, FormControl } from '@mui/material'

export default function SuiteEditor() {
  const [tenants, setTenants] = React.useState([])
  const [projects, setProjects] = React.useState([])

  const [selectedTenantId, setSelectedTenantId] = React.useState('')
  const [selectedProjectId, setSelectedProjectId] = React.useState('')

  const [suiteName, setSuiteName] = React.useState('My Suite')
  const [description, setDescription] = React.useState('')

  const [testName, setTestName] = React.useState('My Test')
  const [steps, setSteps] = React.useState([])
  const [defs, setDefs] = React.useState([])

  // Fetch tenants on mount
  React.useEffect(() => {
    axios.get('/api/tenants').then(r => setTenants(r.data))
    // Also fetch step definitions (assuming global or we might need to fetch per tenant later)
    // For now, fetching from tenant 1 as placeholder or we should update this API to be tenant-agnostic or context-aware
    axios.get('/api/tenants/1/step-definitions').then(r => setDefs(r.data)).catch(e => console.error(e))
  }, [])

  // Fetch projects when tenant changes
  React.useEffect(() => {
    if (selectedTenantId) {
      axios.get(`/api/tenants/${selectedTenantId}/projects`)
        .then(r => setProjects(r.data))
        .catch(e => setProjects([]))
    } else {
      setProjects([])
    }
  }, [selectedTenantId])

  const addStep = (defId) => setSteps(prev => [...prev, { stepDefinitionId: defId, parameters: {} }])

  const createSuite = async () => {
    if (!selectedProjectId) {
      alert('Please select a project')
      return
    }
    try {
      const s = await axios.post(`/api/projects/${selectedProjectId}/suites`, { name: suiteName, description })
      const test = await axios.post(`/api/test-suites/${s.data.id}/tests`, { name: testName, steps })
      alert('Created suite ' + s.data.id + ' and test ' + test.data.id)
    } catch (e) {
      alert('Error creating suite: ' + (e.response?.data?.message || e.message))
    }
  }

  return (
    <div style={{ padding: 16 }}>
      <h2>Test Suite Editor</h2>

      <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
        <FormControl fullWidth>
          <InputLabel>Tenant</InputLabel>
          <Select value={selectedTenantId} label="Tenant" onChange={e => setSelectedTenantId(e.target.value)}>
            {tenants.map(t => <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>)}
          </Select>
        </FormControl>

        <FormControl fullWidth disabled={!selectedTenantId}>
          <InputLabel>Project</InputLabel>
          <Select value={selectedProjectId} label="Project" onChange={e => setSelectedProjectId(e.target.value)}>
            {projects.map(p => <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>)}
          </Select>
        </FormControl>
      </div>

      <div style={{ display: 'flex', gap: 16, marginBottom: 16 }}>
        <TextField fullWidth label="Suite Name" value={suiteName} onChange={e => setSuiteName(e.target.value)} />
        <TextField fullWidth label="Description" value={description} onChange={e => setDescription(e.target.value)} />
      </div>

      <div style={{ marginTop: 12 }}>
        <TextField fullWidth label="Test Name (Initial Test)" value={testName} onChange={e => setTestName(e.target.value)} />
      </div>

      <div style={{ marginTop: 12 }}>
        <h4>Available Steps</h4>
        {defs.map(d => <Button key={d.id} onClick={() => addStep(d.id)} variant="outlined" style={{ margin: 4 }}>{d.name}</Button>)}
      </div>

      <div style={{ marginTop: 12 }}>
        <h4>Configured Steps</h4>
        {steps.map((s, idx) => (
          <div key={idx} style={{ border: '1px solid #ddd', padding: 8, margin: 8 }}>
            <div>Step: {s.stepDefinitionId}</div>
            <DynamicForm schema={{ fields: [{ name: 'param', type: 'string', label: 'Param' }] }} onSubmit={(data) => { s.parameters = data; setSteps([...steps]) }} />
          </div>
        ))}
      </div>

      <div style={{ marginTop: 12 }}>
        <Button variant="contained" onClick={createSuite} disabled={!selectedProjectId}>Create Suite & Test</Button>
      </div>
    </div>
  )
}
