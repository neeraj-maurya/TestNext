import React from 'react'
import axios from 'axios'
import DynamicForm from '../components/DynamicForm'
import { Button, TextField } from '@mui/material'

export default function SuiteEditor(){
  const [suiteName, setSuiteName] = React.useState('My Suite')
  const [testName, setTestName] = React.useState('My Test')
  const [steps, setSteps] = React.useState([])
  const [defs, setDefs] = React.useState([])
  React.useEffect(()=> axios.get('/api/tenants/1/step-definitions').then(r=>setDefs(r.data)),[])
  const addStep = (defId)=> setSteps(prev=>[...prev,{ stepDefinitionId: defId, parameters: {} }])
  const createSuite = async ()=>{
    const s = await axios.post('/api/projects/1/test-suites', { name: suiteName })
    const test = await axios.post(`/api/test-suites/${s.data.id}/tests`, { name: testName, steps })
    alert('Created test id '+test.data.id)
  }
  return (
    <div style={{ padding: 16 }}>
      <h2>Test Suite Editor</h2>
      <div>
        <TextField label="Suite name" value={suiteName} onChange={e=>setSuiteName(e.target.value)} />
      </div>
      <div style={{ marginTop: 12 }}>
        <TextField label="Test name" value={testName} onChange={e=>setTestName(e.target.value)} />
      </div>
      <div style={{ marginTop: 12 }}>
        <h4>Available Steps</h4>
        {defs.map(d=> <Button key={d.id} onClick={()=>addStep(d.id)} variant="outlined" style={{ margin: 4 }}>{d.name}</Button>)}
      </div>
      <div style={{ marginTop: 12 }}>
        <h4>Configured Steps</h4>
        {steps.map((s, idx)=> (
          <div key={idx} style={{ border: '1px solid #ddd', padding: 8, margin: 8 }}>
            <div>Step: {s.stepDefinitionId}</div>
            <DynamicForm schema={{ fields: [{ name: 'param', type: 'string', label: 'Param' }] }} onSubmit={(data)=>{ s.parameters = data; setSteps([...steps]) }} />
          </div>
        ))}
      </div>
      <div style={{ marginTop: 12 }}>
        <Button variant="contained" onClick={createSuite}>Create Suite & Test</Button>
      </div>
    </div>
  )
}
