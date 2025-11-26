import React from 'react'
import { Button, TextField } from '@mui/material'

export default function Login({ onLogin }) {
  const [user, setUser] = React.useState('admin')
  return (
    <div style={{ maxWidth: 400, margin: 'auto' }}>
      <h2>Login (mock)</h2>
      <TextField label="Username" value={user} onChange={e=>setUser(e.target.value)} fullWidth />
      <div style={{ marginTop: 12 }}>
        <Button variant="contained" onClick={()=>onLogin(user)}>Sign in</Button>
      </div>
    </div>
  )
}
