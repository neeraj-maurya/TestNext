import React from 'react'
import { Button, TextField, Box, Card, CardContent, Typography, Container } from '@mui/material'

export default function Login({ onLogin }) {
  const [user, setUser] = React.useState('neera')
  const [password, setPassword] = React.useState('')

  const handleLogin = () => {
    if (user.trim()) {
      onLogin(user)
    }
  }

  const handleKeyPress = (e) => {
    if (e.key === 'Enter') {
      handleLogin()
    }
  }

  return (
    <Container maxWidth="sm">
      <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', minHeight: '100vh' }}>
        <Card sx={{ width: '100%', maxWidth: 400 }}>
          <CardContent>
            <Typography variant="h4" component="h1" sx={{ mb: 3, textAlign: 'center', fontWeight: 'bold' }}>
              TestNext
            </Typography>
            <Typography variant="body2" sx={{ mb: 3, textAlign: 'center', color: '#666' }}>
              Test Automation Platform
            </Typography>

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <TextField
                fullWidth
                label="Username"
                placeholder="e.g., neera (admin) or other"
                value={user}
                onChange={(e) => setUser(e.target.value)}
                onKeyPress={handleKeyPress}
                autoFocus
              />
              <TextField
                fullWidth
                label="Password (Mock)"
                type="password"
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                onKeyPress={handleKeyPress}
              />
              <Button 
                fullWidth 
                variant="contained" 
                size="large"
                onClick={handleLogin}
              >
                Sign In
              </Button>
            </Box>

            <Box sx={{ mt: 3, p: 2, backgroundColor: '#f5f5f5', borderRadius: 1 }}>
              <Typography variant="caption" display="block" sx={{ fontWeight: 'bold', mb: 1 }}>
                Demo Credentials:
              </Typography>
              <Typography variant="caption" display="block">
                • <strong>Admin:</strong> neera
              </Typography>
              <Typography variant="caption" display="block">
                • <strong>Tenant User:</strong> any username
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Container>
  )
}
