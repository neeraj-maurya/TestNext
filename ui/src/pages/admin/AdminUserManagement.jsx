import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, IconButton, Chip, MenuItem
} from '@mui/material'
import { DeleteOutlined, EditOutlined } from '@mui/icons-material'
import { useApi } from '../../hooks/useApi'

export default function AdminUserManagement() {
  const [users, setUsers] = useState([])
  const [tenants, setTenants] = useState([])
  const [projects, setProjects] = useState([])
  const [formData, setFormData] = useState({
    username: '', email: '', password: '', displayName: '', role: 'ROLE_VIEWER', status: 'active', tenantId: '', projectId: ''
  })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const api = useApi()

  useEffect(() => {
    fetchUsers()
    fetchTenantsAndProjects()
  }, [])

  const fetchUsers = async () => {
    try {
      const data = await api.get('/api/system/users')
      const list = Array.isArray(data) ? data : (data?.value || data)
      setUsers(list)
    } catch (error) {
      console.error('Error fetching users:', error)
    }
  }

  const fetchTenantsAndProjects = async () => {
    try {
      const data = await api.get('/api/tenants')
      const list = Array.isArray(data) ? data : (data?.value || data)
      setTenants(list)

      const allProjects = []
      for (const t of list) {
        try {
          const pResponse = await api.get(`/api/tenants/${t.id}/projects`)
          const pList = Array.isArray(pResponse) ? pResponse : []
          pList.forEach(p => allProjects.push({ ...p, tenantId: t.id }))
        } catch (e) {
          console.error(`Failed to load projects for tenant ${t.id}`, e)
        }
      }
      setProjects(allProjects)
    } catch (error) {
      console.error('Error fetching tenants/projects:', error)
    }
  }

  const handleOpenDialog = (user = null) => {
    if (user) {
      setFormData({
        username: user.username || '',
        email: user.email || '',
        password: '',
        displayName: user.display_name || user.displayName || '',
        role: user.role || 'ROLE_VIEWER',
        status: user.active ? 'active' : 'inactive',
        tenantId: user.tenantId || '',
        projectId: '' // Cannot easily extract projectId backwards since it's on project side
      })
      setEditingId(user.id)
    } else {
      setFormData({
        username: '', email: '', password: '', displayName: '', role: 'ROLE_VIEWER', status: 'active', tenantId: '', projectId: ''
      })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ username: '', email: '', password: '', displayName: '', role: 'ROLE_VIEWER', status: 'active', tenantId: '', projectId: '' })
    setEditingId(null)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSave = async () => {
    // Validation
    if (!formData.username.trim()) {
      alert('Username is required')
      return
    }
    if (!formData.email.trim() || !formData.email.includes('@')) {
      alert('Valid email is required')
      return
    }

    // Client-side Tenant and Project Validation
    const isProjectRole = ['ROLE_PROJECT_MANAGER', 'ROLE_TEST_ENGINEER', 'ROLE_VIEWER'].includes(formData.role)
    const isTenantRole = formData.role === 'ROLE_TENANT_MANAGER'

    if ((isTenantRole || isProjectRole) && !formData.tenantId) {
      alert('Tenant is required for this role.')
      return
    }

    if (isProjectRole && !formData.projectId) {
      alert('Project is required for this role.')
      return
    }

    if (formData.role === 'ROLE_PROJECT_MANAGER' && formData.projectId) {
      const proj = projects.find(p => p.id === formData.projectId)
      if (proj && proj.projectManagerId && proj.projectManagerId !== editingId) {
        alert('This project already has a Project Manager assigned. You must demote the existing one first.')
        return
      }
    }

    try {
      const payload = {
        ...formData,
        active: formData.status === 'active',
        tenantId: formData.tenantId ? parseInt(formData.tenantId) : null
      }

      let userId = editingId;
      if (editingId) {
        await api.put(`/api/system/users/${editingId}`, payload)
      } else {
        const res = await api.post('/api/system/users', payload)
        userId = res.id
      }

      // Handle project assignments if applicable
      if (isProjectRole && formData.projectId && userId) {
        const proj = projects.find(p => p.id === formData.projectId)
        if (proj) {
          if (formData.role === 'ROLE_PROJECT_MANAGER') {
            await api.put(`/api/tenants/${formData.tenantId}/projects/${formData.projectId}/manager`, `"${userId}"`, {
              headers: { 'Content-Type': 'application/json' }
            })
          } else {
            // Assign test engineer or viewer
            const existingUsers = proj.assignedUserIds || []
            if (!existingUsers.includes(userId)) {
              await api.put(`/api/tenants/${formData.tenantId}/projects/${formData.projectId}/assignments`, [...existingUsers, userId])
            }
          }
        }
      }

      handleCloseDialog()
      fetchUsers()
      fetchTenantsAndProjects()
    } catch (error) {
      console.error('Error saving user:', error)
      alert('Failed to save user: ' + error.message)
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this user?')) {
      try {
        await api.delete(`/api/system/users/${id}`)
        fetchUsers()
      } catch (error) {
        console.error('Error deleting user:', error)
      }
    }
  }

  return (
    <Card>
      <CardHeader
        title="User Management"
        action={<Button variant="contained" onClick={() => handleOpenDialog()}>Add User</Button>}
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>#</strong></TableCell>
                <TableCell><strong>Username</strong></TableCell>
                <TableCell><strong>Display Name</strong></TableCell>
                <TableCell><strong>Role</strong></TableCell>
                <TableCell><strong>Status</strong></TableCell>
                <TableCell><strong>Tenant</strong></TableCell>
                <TableCell align="center"><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map((user, index) => (
                <TableRow key={user.id}>
                  <TableCell>{index + 1}</TableCell>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>{user.display_name || user.displayName}</TableCell>
                  <TableCell>
                    <Chip label={user.role} size="small" color={user.role === 'SYSTEM_ADMIN' ? 'primary' : 'default'} />
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={user.active ? 'Active' : 'Inactive'}
                      size="small"
                      color={user.active ? 'success' : 'error'}
                      variant={user.active ? 'filled' : 'outlined'}
                    />
                  </TableCell>
                  <TableCell>{tenants.find(t => t.id === user.tenantId)?.name || '-'}</TableCell>
                  <TableCell align="center">
                    <IconButton
                      size="small"
                      onClick={() => handleOpenDialog(user)}
                      title="Edit"
                    >
                      <EditOutlined fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() => handleDelete(user.id)}
                      title={user.username === localStorage.getItem('currentUser') ? "Cannot delete yourself" : "Delete"}
                      disabled={user.username === localStorage.getItem('currentUser')}
                    >
                      <DeleteOutlined fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit User' : 'Add New User'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              label="Username"
              name="username"
              value={formData.username}
              onChange={handleInputChange}
            />
            <TextField
              fullWidth
              label="Display Name"
              name="displayName"
              value={formData.displayName}
              onChange={handleInputChange}
            />
            <TextField
              fullWidth
              label="Email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleInputChange}
            />
            {!editingId && (
              <TextField
                fullWidth
                label="Password"
                name="password"
                type="password"
                value={formData.password}
                onChange={handleInputChange}
              />
            )}
            <TextField
              fullWidth
              select
              label="Role"
              name="role"
              value={formData.role}
              onChange={handleInputChange}
            >
              <MenuItem value="ROLE_SYSTEM_ADMIN">System Admin</MenuItem>
              <MenuItem value="ROLE_TENANT_MANAGER">Tenant Manager</MenuItem>
              <MenuItem value="ROLE_PROJECT_MANAGER">Project Manager</MenuItem>
              <MenuItem value="ROLE_TEST_ENGINEER">Test Engineer</MenuItem>
              <MenuItem value="ROLE_VIEWER">Viewer</MenuItem>
            </TextField>

            <TextField
              fullWidth
              select
              label="Tenant"
              name="tenantId"
              value={formData.tenantId}
              onChange={handleInputChange}
              required={formData.role !== 'ROLE_SYSTEM_ADMIN'}
              disabled={formData.role === 'ROLE_SYSTEM_ADMIN'}
              helperText={formData.role !== 'ROLE_SYSTEM_ADMIN' ? 'Required for non-admin users' : 'Disabled for System Admin'}
            >
              <MenuItem value=""><em>None (Global)</em></MenuItem>
              {tenants.map(t => (
                <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>
              ))}
            </TextField>

            <TextField
              fullWidth
              select
              label="Project"
              name="projectId"
              value={formData.projectId}
              onChange={handleInputChange}
              required={['ROLE_PROJECT_MANAGER', 'ROLE_TEST_ENGINEER', 'ROLE_VIEWER'].includes(formData.role)}
              disabled={!['ROLE_PROJECT_MANAGER', 'ROLE_TEST_ENGINEER', 'ROLE_VIEWER'].includes(formData.role) || !formData.tenantId}
              helperText={['ROLE_PROJECT_MANAGER', 'ROLE_TEST_ENGINEER', 'ROLE_VIEWER'].includes(formData.role) ? 'Required for project-scoped roles' : 'Only applicable for project roles'}
            >
              <MenuItem value=""><em>None</em></MenuItem>
              {projects.filter(p => p.tenantId === formData.tenantId).map(p => (
                <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>
              ))}
            </TextField>

            <TextField
              fullWidth
              select
              label="Status"
              name="status"
              value={formData.status}
              onChange={handleInputChange}
            >
              <MenuItem value="active">Active</MenuItem>
              <MenuItem value="inactive">Inactive</MenuItem>
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>
    </Card>
  )
}
