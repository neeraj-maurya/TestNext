import React from 'react'
import axios from 'axios'
import { Button, TextField } from '@mui/material'

export default function ProjectsPage(){
  const [tenants, setTenants] = React.useState([])
  const [name, setName] = React.useState('')
  React.useEffect(()=>{ axios.get('/api/tenants').then(r=>setTenants(r.data)) },[])
  const create = ()=>{ axios.post('/api/tenants', { name }).then(r=> setTenants(prev=>[...prev, r.data])); setName('') }
  return (
    <div style={{ padding: 16 }}>
      <h2>Tenants / Projects (top-level)</h2>
      <div>
        <TextField label="Tenant name" value={name} onChange={e=>setName(e.target.value)} />
        <Button onClick={create} variant="contained" style={{ marginLeft: 8 }}>Create</Button>
      </div>
      <ul>
        {tenants.map(t=> <li key={t.id}>{t.name} ({t.schemaName})</li>)}
      </ul>
    </div>
  )
}
