import React, { useState } from 'react'
import { AppBar, Toolbar, Typography, Button, Box, Dialog, DialogTitle, DialogContent, TextField, DialogActions } from '@mui/material'
import { LogoutOutlined, SwitchAccountOutlined } from '@mui/icons-material'

export default function Navbar({ user, onLogout, onSwitchUser }) {
  const [openSwitch, setOpenSwitch] = useState(false)
  const [switchUsername, setSwitchUsername] = useState('')
  const roleLabel = user === 'admin' ? '👤 (Admin)' : '👤 (User)'

  const handleSwitch = () => {
    if (switchUsername.trim()) {
      onSwitchUser(switchUsername.trim())
      setOpenSwitch(false)
      setSwitchUsername('')
    }
  }

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
              onClick={() => setOpenSwitch(true)}
              size="small"
              startIcon={<SwitchAccountOutlined />}
              sx={{ mr: 1 }}
            >
              Switch User
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

      <Dialog open={openSwitch} onClose={() => setOpenSwitch(false)}>
        <DialogTitle>Switch User</DialogTitle>
        <DialogContent sx={{ pt: 2, minWidth: 300 }}>
          <TextField
            autoFocus
            margin="dense"
            label="Username"
            fullWidth
            value={switchUsername}
            onChange={(e) => setSwitchUsername(e.target.value)}
            onKeyPress={(e) => e.key === 'Enter' && handleSwitch()}
            placeholder="Enter username to impersonate"
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenSwitch(false)}>Cancel</Button>
          <Button onClick={handleSwitch} variant="contained">Switch</Button>
        </DialogActions>
      </Dialog>
    </>
  )
}
