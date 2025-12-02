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
  const [formData, setFormData] = useState({
    username: '', email: '', password: '', displayName: '', role: 'ROLE_VIEWER', status: 'active'
  })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const api = useApi()

  useEffect(() => {
    fetchUsers()
  }, [])

  const fetchUsers = async () => {
    try {
      const data = await api.get('/api/system/users')
      // backend may return {value: []} or []
      const list = Array.isArray(data) ? data : (data?.value || data)
      setUsers(list)
    } catch (error) {
      console.error('Error fetching users:', error)
    }
  }

  const handleOpenDialog = (user = null) => {
    if (user) {
      setFormData({
        username: user.username || '',
        email: user.email || '',
        password: '',
        displayName: user.display_name || '', // Note: backend returns display_name
        role: user.role || 'ROLE_VIEWER',
        status: user.status || 'active'
      })
      setEditingId(user.id)
    } else {
      setFormData({
        username: '', email: '', password: '', displayName: '', role: 'ROLE_VIEWER', status: 'active'
      })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ username: '', email: '', password: '', displayName: '', role: 'ROLE_VIEWER', status: 'active' })
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

    try {
      if (editingId) {
        await api.put(`/api/system/users/${editingId}`, formData)
      } else {
        await api.post('/api/system/users', formData)
      }
      handleCloseDialog()
      fetchUsers()
    } catch (error) {
      console.error('Error saving user:', error)
      alert('Failed to save user: ' + (error.response?.data?.error || error.message))
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
                <TableCell><strong>ID</strong></TableCell>
                <TableCell><strong>Username</strong></TableCell>
                <TableCell><strong>Display Name</strong></TableCell>
                <TableCell><strong>Role</strong></TableCell>
                <TableCell align="center"><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map(user => (
                <TableRow key={user.id}>
                  <TableCell>{user.id}</TableCell>
                  <TableCell>{user.username}</TableCell>
                  <TableCell>{user.display_name || user.displayName}</TableCell>
                  <TableCell>
                    <Chip label={user.role} size="small" color={user.role === 'SYSTEM_ADMIN' ? 'primary' : 'default'} />
                  </TableCell>
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
              <MenuItem value="ROLE_TEST_MANAGER">Test Manager</MenuItem>
              <MenuItem value="ROLE_TEST_ENGINEER">Test Engineer</MenuItem>
              <MenuItem value="ROLE_VIEWER">Viewer</MenuItem>
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
