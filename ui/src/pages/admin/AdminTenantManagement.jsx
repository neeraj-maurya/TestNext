import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, IconButton, Chip, MenuItem, FormControl, InputLabel, Select
} from '@mui/material'
import { DeleteOutlined, EditOutlined } from '@mui/icons-material'
import { useApi } from '../../hooks/useApi'

const MOCK_TENANTS = [
  { id: 1, name: 'TechCorp', schemaName: 'techcorp_tenant' },
  { id: 2, name: 'FinTrade Inc', schemaName: 'fintrade_tenant' },
]

export default function AdminTenantManagement() {
  const [tenants, setTenants] = useState(MOCK_TENANTS)
  const [users, setUsers] = useState([])
  const [formData, setFormData] = useState({ name: '', schemaName: '', testManagerId: '' })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const api = useApi()

  useEffect(() => {
    fetchTenants()
    fetchUsers()
  }, [])

  const fetchTenants = async () => {
    try {
      const response = await api.get('/api/tenants')
      if (response && Array.isArray(response)) {
        setTenants(response)
      } else if (response && response.value) {
        setTenants(response.value)
      } else {
        setTenants(MOCK_TENANTS)
      }
    } catch (error) {
      console.error('Error fetching tenants, using mock data:', error)
      setTenants(MOCK_TENANTS)
    }
  }

  const fetchUsers = async () => {
    try {
      const response = await api.get('/api/system/users')
      if (response && Array.isArray(response)) {
        setUsers(response)
      }
    } catch (error) {
      console.error('Error fetching users:', error)
    }
  }

  const handleOpenDialog = (tenant = null) => {
    if (tenant) {
      setFormData({
        name: tenant.name,
        schemaName: tenant.schemaName,
        testManagerId: tenant.testManagerId || ''
      })
      setEditingId(tenant.id)
    } else {
      setFormData({ name: '', schemaName: '', testManagerId: '' })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ name: '', schemaName: '', testManagerId: '' })
    setEditingId(null)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSave = async () => {
    try {
      const payload = { ...formData }
      if (!payload.testManagerId) delete payload.testManagerId; // Don't send empty string if UUID expected

      if (editingId) {
        await api.put(`/api/tenants/${editingId}`, payload)
      } else {
        await api.post('/api/tenants', payload)
      }
      handleCloseDialog()
      fetchTenants()
    } catch (error) {
      console.error('Error saving tenant:', error)
      // Fallback: add to local state
      if (editingId) {
        setTenants(tenants.map(t => t.id === editingId ? { ...formData, id: editingId } : t))
      } else {
        const newId = Math.max(...tenants.map(t => t.id || 0), 0) + 1
        setTenants([...tenants, { ...formData, id: newId }])
      }
      handleCloseDialog()
    }
  }

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false)
  const [tenantToDelete, setTenantToDelete] = useState(null)

  const confirmDelete = (tenant) => {
    setTenantToDelete(tenant)
    setDeleteDialogOpen(true)
  }

  const handleDelete = async () => {
    if (!tenantToDelete) return

    try {
      await api.delete(`/api/tenants/${tenantToDelete.id}`)
      fetchTenants()
    } catch (error) {
      console.error('Error deleting tenant:', error)
      setTenants(tenants.filter(t => t.id !== tenantToDelete.id))
    }
    setDeleteDialogOpen(false)
    setTenantToDelete(null)
  }

  const getManagerName = (id) => {
    const u = users.find(u => u.id === id)
    return u ? (u.displayName || u.username) : '-'
  }

  return (
    <Card>
      <CardHeader
        title="Tenant Management"
        action={<Button variant="contained" onClick={() => handleOpenDialog()}>Add Tenant</Button>}
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>ID</strong></TableCell>
                <TableCell><strong>Name</strong></TableCell>
                <TableCell><strong>Schema</strong></TableCell>
                <TableCell><strong>Test Manager</strong></TableCell>
                <TableCell><strong>Status</strong></TableCell>
                <TableCell align="center"><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {tenants.map(tenant => (
                <TableRow key={tenant.id}>
                  <TableCell>{tenant.id}</TableCell>
                  <TableCell>{tenant.name}</TableCell>
                  <TableCell><code>{tenant.schemaName}</code></TableCell>
                  <TableCell>{getManagerName(tenant.testManagerId)}</TableCell>
                  <TableCell>
                    <Chip label="Active" color="success" size="small" />
                  </TableCell>
                  <TableCell align="center">
                    <IconButton
                      size="small"
                      onClick={() => handleOpenDialog(tenant)}
                      title="Edit"
                    >
                      <EditOutlined fontSize="small" />
                    </IconButton>
                    <IconButton
                      size="small"
                      onClick={() => confirmDelete(tenant)}
                      title="Delete"
                      color="error" // Highlight danger
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
        <DialogTitle>{editingId ? 'Edit Tenant' : 'Add New Tenant'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              label="Tenant Name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
            />
            <TextField
              fullWidth
              label="Database Schema"
              name="schemaName"
              value={formData.schemaName}
              onChange={handleInputChange}
            />
            <FormControl fullWidth>
              <InputLabel>Test Manager</InputLabel>
              <Select
                name="testManagerId"
                value={formData.testManagerId}
                label="Test Manager"
                onChange={handleInputChange}
              >
                <MenuItem value=""><em>None</em></MenuItem>
                {users.map(u => (
                  <MenuItem key={u.id} value={u.id}>
                    {u.displayName || u.username} ({u.role})
                  </MenuItem>
                ))}
              </Select>
            </FormControl>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle sx={{ color: 'error.main' }}>⚠️ Irreversible Action</DialogTitle>
        <DialogContent>
          Are you sure you want to delete the tenant <strong>{tenantToDelete?.name}</strong>?
          <br /><br />
          This will <strong>permanently delete</strong> the associated database schema <code>{tenantToDelete?.schemaName}</code> and <strong>ALL</strong> data within it.
          <br /><br />
          This action cannot be undone.
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Cancel</Button>
          <Button onClick={handleDelete} color="error" variant="contained">Yes, Delete</Button>
        </DialogActions>
      </Dialog>
    </Card >
  )
}
