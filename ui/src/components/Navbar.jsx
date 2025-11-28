import React from 'react'
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material'
import { LogoutOutlined } from '@mui/icons-material'

export default function Navbar({ user, onLogout }) {
  return (
    <AppBar position="static" sx={{ ml: { xs: 0, sm: '280px' } }}>
      <Toolbar>
        <Typography variant="h6" sx={{ flexGrow: 1 }}>
          TestNext — Test Automation Platform
        </Typography>
        <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Typography variant="body2" sx={{ opacity: 0.9 }}>
            {user} {user === 'neera' ? '👤 (Admin)' : '👤 (Tenant User)'}
          </Typography>
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
  )
}
