import React from 'react'
import axios from 'axios'

export default function ExecutionView(){
  const [exec, setExec] = React.useState(null)
  const [id, setId] = React.useState('')
  const load = ()=> axios.get(`/api/executions/${id}`).then(r=>setExec(r.data)).catch(e=>alert('not found'))
  return (
    <div style={{ padding: 16 }}>
      <h2>Execution Viewer</h2>
      <div>
        <input value={id} onChange={e=>setId(e.target.value)} placeholder="execution id" />
        <button onClick={load}>Load</button>
      </div>
      {exec && (
        <div>
          <h3>Status: {exec.status}</h3>
          <ul>
            {exec.steps.map(s=> <li key={s.id}>{s.step_definition_id}: {s.status} - {JSON.stringify(s.result)}</li>)}
          </ul>
        </div>
      )}
    </div>
  )
}
