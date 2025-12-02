import React from 'react'
import { Button, TextField, Box, Card, CardContent, Typography, Container, Alert } from '@mui/material'

const VALID_USERS = {
  'admin': 'admin',
  'user': 'user'
}

export default function Login({ onLogin }) {
  const [username, setUsername] = React.useState('')
  const [password, setPassword] = React.useState('')
  const [error, setError] = React.useState('')

  const handleLogin = () => {
    setError('')
    
    if (!username.trim() || !password.trim()) {
      setError('Please enter both username and password')
      return
    }
    
    if (VALID_USERS[username] === password) {
      onLogin(username)
    } else {
      setError('Invalid credentials. Use admin:admin or user:user')
      setPassword('')
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

            {error && (
              <Alert severity="error" sx={{ mb: 2 }}>
                {error}
              </Alert>
            )}

            <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
              <TextField
                fullWidth
                label="Username"
                value={username}
                onChange={(e) => setUsername(e.target.value)}
                onKeyPress={handleKeyPress}
                autoFocus
              />
              <TextField
                fullWidth
                label="Password"
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
                • <strong>Admin:</strong> admin / admin
              </Typography>
              <Typography variant="caption" display="block">
                • <strong>User:</strong> user / user
              </Typography>
            </Box>
          </CardContent>
        </Card>
      </Box>
    </Container>
  )
}
