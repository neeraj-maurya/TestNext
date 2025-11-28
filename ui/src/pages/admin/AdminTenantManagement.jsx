import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell, 
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, IconButton, Chip
} from '@mui/material'
import { DeleteOutlined, EditOutlined } from '@mui/icons-material'

const MOCK_TENANTS = [
  { id: 1, name: 'TechCorp', schema: 'techcorp_tenant', email: 'admin@techcorp.com' },
  { id: 2, name: 'FinTrade Inc', schema: 'fintrade_tenant', email: 'admin@fintrade.com' },
]

export default function AdminTenantManagement() {
  const [tenants, setTenants] = useState(MOCK_TENANTS)
  const [formData, setFormData] = useState({ name: '', schema: '', email: '' })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)

  useEffect(() => {
    // Fetch tenants from API or use mock data
    fetchTenants()
  }, [])

  const fetchTenants = async () => {
    try {
      const response = await fetch('/api/tenants')
      const data = await response.json()
      setTenants(data)
    } catch (error) {
      console.error('Error fetching tenants, using mock data:', error)
      setTenants(MOCK_TENANTS)
    }
  }

  const handleOpenDialog = (tenant = null) => {
    if (tenant) {
      setFormData(tenant)
      setEditingId(tenant.id)
    } else {
      setFormData({ name: '', schema: '', email: '' })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ name: '', schema: '', email: '' })
    setEditingId(null)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSave = async () => {
    try {
      const method = editingId ? 'PUT' : 'POST'
      const url = editingId ? `/api/tenants/${editingId}` : '/api/tenants'
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      })
      if (response.ok) {
        handleCloseDialog()
        fetchTenants()
      }
    } catch (error) {
      console.error('Error saving tenant:', error)
      // Mock save - add to state
      if (editingId) {
        setTenants(tenants.map(t => t.id === editingId ? { ...formData, id: editingId } : t))
      } else {
        setTenants([...tenants, { ...formData, id: Math.max(...tenants.map(t => t.id), 0) + 1 }])
      }
      handleCloseDialog()
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this tenant?')) {
      try {
        const response = await fetch(`/api/tenants/${id}`, { method: 'DELETE' })
        if (response.ok) {
          fetchTenants()
        }
      } catch (error) {
        console.error('Error deleting tenant:', error)
        setTenants(tenants.filter(t => t.id !== id))
      }
    }
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
                <TableCell><strong>Email</strong></TableCell>
                <TableCell><strong>Status</strong></TableCell>
                <TableCell align="center"><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {tenants.map(tenant => (
                <TableRow key={tenant.id}>
                  <TableCell>{tenant.id}</TableCell>
                  <TableCell>{tenant.name}</TableCell>
                  <TableCell><code>{tenant.schema}</code></TableCell>
                  <TableCell>{tenant.email}</TableCell>
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
                      onClick={() => handleDelete(tenant.id)}
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
              name="schema"
              value={formData.schema}
              onChange={handleInputChange}
            />
            <TextField
              fullWidth
              label="Admin Email"
              name="email"
              type="email"
              value={formData.email}
              onChange={handleInputChange}
            />
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
