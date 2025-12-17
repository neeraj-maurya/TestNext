import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Dialog, DialogTitle,
  DialogContent, DialogActions, FormControlLabel, Checkbox, 
  List, ListItem, ListItemText, ListItemIcon, Chip
} from '@mui/material'
import { AssignmentInd } from '@mui/icons-material'
import { useApi } from '../../hooks/useApi'

export default function TenantProjectsPage() {
  const [currentUser, setCurrentUser] = useState(null)
  const [projects, setProjects] = useState([])
  const [tenantUsers, setTenantUsers] = useState([])
  const [openAssignDialog, setOpenAssignDialog] = useState(false)
  const [selectedProject, setSelectedProject] = useState(null)
  const [selectedUserIds, setSelectedUserIds] = useState([])
  
  const api = useApi()

  useEffect(() => {
    fetchMe()
  }, [])

  const fetchMe = async () => {
    try {
      const me = await api.get('/api/system/users/me')
      if (me) {
        setCurrentUser(me)
        fetchProjects(me.tenantId)
      }
    } catch (error) {
      console.error('Error fetching current user:', error)
    }
  }

  const fetchProjects = async (tenantId) => {
    try {
      const response = await api.get(`/api/tenants/${tenantId}/projects`)
      setProjects(Array.isArray(response) ? response : [])
    } catch (error) {
      console.error('Error fetching projects:', error)
    }
  }

  const handleOpenAssign = async (project) => {
    setSelectedProject(project)
    
    // Load users if not loaded
    if (tenantUsers.length === 0) {
      try {
        const users = await api.get(`/api/tenants/${currentUser.tenantId}/users`)
        setTenantUsers(Array.isArray(users) ? users : [])
      } catch (error) {
        console.error('Error loading users:', error)
        setTenantUsers([])
      }
    }

    // Reset selection for now (blind assignment as backend DTO doesn't return existing IDs yet)
    // In a full implementation, we would fetch existing assignments first.
    setSelectedUserIds([]) 
    setOpenAssignDialog(true)
  }

  const handleAssignSave = async () => {
    if (!selectedProject || !currentUser) return
    try {
      await api.put(`/api/tenants/${currentUser.tenantId}/projects/${selectedProject.id}/assignments`, selectedUserIds)
      setOpenAssignDialog(false)
      fetchProjects(currentUser.tenantId) // Refresh list
    } catch (error) {
      console.error('Error assigning users:', error)
    }
  }

  const toggleUserSelection = (userId) => {
    setSelectedUserIds(prev => 
      prev.includes(userId) ? prev.filter(id => id !== userId) : [...prev, userId]
    )
  }

  const isManager = currentUser && (currentUser.role === 'ROLE_TEST_MANAGER' || currentUser.role === 'ROLE_SYSTEM_ADMIN')

  return (
    <div style={{ padding: 20 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
        <h2>My Projects</h2>
        {currentUser && !isManager && <p style={{fontSize: '0.8em', color: '#666'}}>Showing projects assigned to you</p>}
      </div>

      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow style={{ backgroundColor: '#f5f5f5' }}>
              <TableCell><strong>Project Name</strong></TableCell>
              <TableCell><strong>Description</strong></TableCell>
              <TableCell><strong>Actions</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {projects.length === 0 ? (
              <TableRow>
                <TableCell colSpan={3} align="center">
                  {isManager ? 'No projects found in tenant.' : 'No projects assigned to you.'}
                </TableCell>
              </TableRow>
            ) : (
              projects.map(p => (
                <TableRow key={p.id}>
                  <TableCell>{p.name}</TableCell>
                  <TableCell>{p.description || '-'}</TableCell>
                  <TableCell>
                    {isManager && (
                      <Button 
                        size="small" 
                        variant="outlined" 
                        startIcon={<AssignmentInd />}
                        onClick={() => handleOpenAssign(p)}
                      >
                        Manage Access
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))
            )}
          </TableBody>
        </Table>
      </TableContainer>

      {/* Assignment Dialog */}
      <Dialog open={openAssignDialog} onClose={() => setOpenAssignDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Assign Users to {selectedProject?.name}</DialogTitle>
        <DialogContent>
          <p style={{fontSize: '0.9em', color: '#666', marginBottom: 10}}>Select users who can access this project.</p>
          <List style={{maxHeight: 300, overflow: 'auto', border: '1px solid #eee'}}>
            {tenantUsers.map(u => (
               <ListItem key={u.id} dense button onClick={() => toggleUserSelection(u.id)}>
                 <ListItemIcon>
                   <Checkbox
                     edge="start"
                     checked={selectedUserIds.indexOf(u.id) !== -1}
                     tabIndex={-1}
                     disableRipple
                   />
                 </ListItemIcon>
                 <ListItemText 
                    primary={u.username} 
                    secondary={u.displayName ? `${u.displayName} (${u.role.replace('ROLE_', '')})` : u.role.replace('ROLE_', '')} 
                  />
               </ListItem>
            ))}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenAssignDialog(false)}>Cancel</Button>
          <Button onClick={handleAssignSave} color="primary" variant="contained">
            Save
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  )
}
