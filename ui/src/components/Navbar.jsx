import React from 'react'
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material'
import { LogoutOutlined, AccountCircleOutlined } from '@mui/icons-material'
import { useNavigate } from 'react-router-dom'

export default function Navbar({ user, onLogout }) {
  const navigate = useNavigate()
  const roleLabel = user === 'admin' ? '👤 (Admin)' : '👤 (User)'

  return (
    <>
      <AppBar position="static" sx={{ ml: { xs: 0, sm: '280px' } }}>
        <Toolbar>
          <Typography variant="h6" sx={{ flexGrow: 1 }}>
            TestNext — Test Automation Platform
          </Typography>
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
            <Typography variant="body2" sx={{ opacity: 0.9 }}>
              {user} {roleLabel}
            </Typography>
            <Button
              color="inherit"
              onClick={() => navigate('/profile')}
              size="small"
              startIcon={<AccountCircleOutlined />}
              sx={{ mr: 1 }}
            >
              Profile
            </Button>
            <Button
              color="inherit"
              onClick={onLogout}
              startIcon={<LogoutOutlined />}
              size="small"
            >
              Logout
            </Button>
          </Box>
        </Toolbar>
      </AppBar>
    </>
  )
}
