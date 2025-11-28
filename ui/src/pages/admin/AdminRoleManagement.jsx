import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, IconButton, Chip, MenuItem
} from '@mui/material'
import { DeleteOutlined, EditOutlined } from '@mui/icons-material'

export default function AdminRoleManagement() {
  const [assignments, setAssignments] = useState([])
  const [users, setUsers] = useState([])
  const [tenants, setTenants] = useState([])
  const [formData, setFormData] = useState({ userId: '', tenantId: '', role: 'viewer' })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)

  useEffect(() => {
    fetchAssignments()
    fetchUsers()
    fetchTenants()
  }, [])

  const fetchAssignments = async () => {
    try {
      const response = await fetch('/api/role-assignments')
      const data = await response.json()
      setAssignments(data || [])
    } catch (error) {
      console.error('Error fetching role assignments:', error)
      setAssignments([])
    }
  }

  const fetchUsers = async () => {
    try {
      const response = await fetch('/api/system-users')
      const data = await response.json()
      setUsers(data || [])
    } catch (error) {
      console.error('Error fetching users:', error)
    }
  }

  const fetchTenants = async () => {
    try {
      const response = await fetch('/api/tenants')
      const data = await response.json()
      setTenants(data || [])
    } catch (error) {
      console.error('Error fetching tenants:', error)
    }
  }

  const handleOpenDialog = (assignment = null) => {
    if (assignment) {
      setFormData({ userId: assignment.userId, tenantId: assignment.tenantId, role: assignment.role })
      setEditingId(assignment.id)
    } else {
      setFormData({ userId: '', tenantId: '', role: 'viewer' })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ userId: '', tenantId: '', role: 'viewer' })
    setEditingId(null)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSave = async () => {
    try {
      const method = editingId ? 'PUT' : 'POST'
      const url = editingId ? `/api/role-assignments/${editingId}` : '/api/role-assignments'
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      })
      if (response.ok) {
        handleCloseDialog()
        fetchAssignments()
      }
    } catch (error) {
      console.error('Error saving role assignment:', error)
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this role assignment?')) {
      try {
        const response = await fetch(`/api/role-assignments/${id}`, { method: 'DELETE' })
        if (response.ok) {
          fetchAssignments()
        }
      } catch (error) {
        console.error('Error deleting role assignment:', error)
      }
    }
  }

  const getUserName = (userId) => {
    const user = users.find(u => u.id === parseInt(userId))
    return user ? user.email : `User #${userId}`
  }

  const getTenantName = (tenantId) => {
    const tenant = tenants.find(t => t.id === parseInt(tenantId))
    return tenant ? tenant.name : `Tenant #${tenantId}`
  }

  return (
    <Card>
      <CardHeader 
        title="Role Management"
        subheader="Assign users to tenant roles"
        action={<Button variant="contained" onClick={() => handleOpenDialog()}>Add Assignment</Button>}
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>ID</strong></TableCell>
                <TableCell><strong>User</strong></TableCell>
                <TableCell><strong>Tenant</strong></TableCell>
                <TableCell><strong>Role</strong></TableCell>
                <TableCell align="center"><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {assignments.map(assignment => (
                <TableRow key={assignment.id}>
                  <TableCell>{assignment.id}</TableCell>
                  <TableCell>{getUserName(assignment.userId)}</TableCell>
                  <TableCell>{getTenantName(assignment.tenantId)}</TableCell>
                  <TableCell>
                    <Chip 
                      label={assignment.role}
                      size="small"
                      color={assignment.role === 'admin' ? 'error' : 'default'}
                    />
                  </TableCell>
                  <TableCell align="center">
                    <IconButton 
                      size="small" 
                      onClick={() => handleOpenDialog(assignment)}
                      title="Edit"
                    >
                      <EditOutlined fontSize="small" />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      onClick={() => handleDelete(assignment.id)}
                      title="Delete"
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
        <DialogTitle>{editingId ? 'Edit Role Assignment' : 'Add New Role Assignment'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              select
              label="User"
              name="userId"
              value={formData.userId}
              onChange={handleInputChange}
            >
              {users.map(user => (
                <MenuItem key={user.id} value={user.id}>
                  {user.email}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              fullWidth
              select
              label="Tenant"
              name="tenantId"
              value={formData.tenantId}
              onChange={handleInputChange}
            >
              {tenants.map(tenant => (
                <MenuItem key={tenant.id} value={tenant.id}>
                  {tenant.name}
                </MenuItem>
              ))}
            </TextField>
            <TextField
              fullWidth
              select
              label="Role"
              name="role"
              value={formData.role}
              onChange={handleInputChange}
            >
              <MenuItem value="viewer">Viewer</MenuItem>
              <MenuItem value="editor">Editor</MenuItem>
              <MenuItem value="admin">Admin</MenuItem>
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
