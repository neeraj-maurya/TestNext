import React from 'react'
import axios from 'axios'
import { Button, TextField } from '@mui/material'

export default function StepDefinitionsPage(){
  const [defs, setDefs] = React.useState([])
  const [name, setName] = React.useState('')
  React.useEffect(()=> axios.get('/api/tenants/1/step-definitions').then(r=>setDefs(r.data)),[])
  const create = ()=> axios.post('/api/tenants/1/step-definitions', { name, input_schema: { fields: [] } }).then(r=>setDefs(prev=>[...prev,r.data]));
  return (
    <div style={{ padding: 16 }}>
      <h2>Step Definitions</h2>
      <div>
        <TextField label="Name" value={name} onChange={e=>setName(e.target.value)} />
        <Button onClick={create} variant="contained" style={{ marginLeft: 8 }}>Create</Button>
      </div>
      <ul>
        {defs.map(d=> <li key={d.id}>{d.name}</li>)}
      </ul>
    </div>
  )
}
