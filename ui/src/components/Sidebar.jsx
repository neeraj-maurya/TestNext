import React from 'react'
import {
  Drawer,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Collapse,
  Divider,
  Box
} from '@mui/material'
import {
  ExpandLess,
  ExpandMore,
  Dashboard,
  Layers
} from '@mui/icons-material'
import { useNavigate } from 'react-router-dom'

export default function Sidebar({ isAdmin }) {
  const navigate = useNavigate()
  const [expandedMenu, setExpandedMenu] = React.useState(isAdmin ? 'admin' : 'tenant')

  const toggleMenu = (menu) => {
    setExpandedMenu(expandedMenu === menu ? null : menu)
  }

  const handleNavigate = (path) => {
    navigate(path)
  }

  return (
    <Drawer
      variant="permanent"
      sx={{
        width: 280,
        flexShrink: 0,
        '& .MuiDrawer-paper': {
          width: 280,
          boxSizing: 'border-box',
          mt: '64px',
          height: 'calc(100vh - 64px)'
        }
      }}
    >
      <Box sx={{ width: 280, overflowY: 'auto', pt: 2 }}>
        <List>
          {/* Admin Menu */}
          {isAdmin && (
            <>
              <ListItemButton onClick={() => toggleMenu('admin')}>
                <ListItemIcon><Dashboard /></ListItemIcon>
                <ListItemText primary="Admin" />
                {expandedMenu === 'admin' ? <ExpandLess /> : <ExpandMore />}
              </ListItemButton>
              <Collapse in={expandedMenu === 'admin'} timeout="auto" unmountOnExit>
                <List component="div" disablePadding>
                  <ListItemButton
                    sx={{ pl: 4 }}
                    onClick={() => handleNavigate('/admin/tenants')}
                  >
                    <ListItemText primary="Tenant Management" />
                  </ListItemButton>
                  <ListItemButton
                    sx={{ pl: 4 }}
                    onClick={() => handleNavigate('/admin/users')}
                  >
                    <ListItemText primary="User Management" />
                  </ListItemButton>

                  <ListItemButton
                    sx={{ pl: 4 }}
                    onClick={() => handleNavigate('/admin/test-suites')}
                  >
                    <ListItemText primary="Test Suites" />
                  </ListItemButton>
                  <ListItemButton
                    sx={{ pl: 4 }}
                    onClick={() => handleNavigate('/admin/test-steps-library')}
                  >
                    <ListItemText primary="Test Steps Library" />
                  </ListItemButton>
                </List>
              </Collapse>
              <Divider sx={{ my: 1 }} />
            </>
          )}

          {/* Tenant User Menu */}
          {!isAdmin && (
            <>
              <ListItemButton onClick={() => toggleMenu('tenant')}>
                <ListItemIcon><Layers /></ListItemIcon>
                <ListItemText primary="Test Management" />
                {expandedMenu === 'tenant' ? <ExpandLess /> : <ExpandMore />}
              </ListItemButton>
              <Collapse in={expandedMenu === 'tenant'} timeout="auto" unmountOnExit>
                <List component="div" disablePadding>
                  <ListItemButton
                    sx={{ pl: 4 }}
                    onClick={() => handleNavigate('/tenant/test-suites')}
                  >
                    <ListItemText primary="Test Suites" />
                  </ListItemButton>
                  <ListItemButton
                    sx={{ pl: 4 }}
                    onClick={() => handleNavigate('/tenant/test-cases')}
                  >
                    <ListItemText primary="Test Cases" />
                  </ListItemButton>
                  <ListItemButton
                    sx={{ pl: 4 }}
                    onClick={() => handleNavigate('/tenant/execution')}
                  >
                    <ListItemText primary="Execution" />
                  </ListItemButton>
                  <ListItemButton
                    sx={{ pl: 4 }}
                    onClick={() => handleNavigate('/tenant/reports')}
                  >
                    <ListItemText primary="Reports" />
                  </ListItemButton>
                </List>
              </Collapse>
              <Divider sx={{ my: 1 }} />
            </>
          )}
        </List>
      </Box>
    </Drawer>
  )
}
