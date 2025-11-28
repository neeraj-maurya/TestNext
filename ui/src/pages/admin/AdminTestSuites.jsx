import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, IconButton, Chip
} from '@mui/material'
import { DeleteOutlined, EditOutlined, PlayArrowOutlined } from '@mui/icons-material'

const MOCK_SUITES = [
  { id: 1, name: 'Login Tests', description: 'Test user authentication flows', tenantId: 1, testCount: 5 },
  { id: 2, name: 'Payment Processing', description: 'Test payment gateway integration', tenantId: 1, testCount: 8 },
  { id: 3, name: 'Trade Execution', description: 'Test trade execution workflow', tenantId: 2, testCount: 12 },
]

export default function AdminTestSuites() {
  const [suites, setSuites] = useState(MOCK_SUITES)
  const [formData, setFormData] = useState({ name: '', description: '', tenantId: '' })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)

  useEffect(() => {
    fetchSuites()
  }, [])

  const fetchSuites = async () => {
    try {
      const response = await fetch('/api/test-suites')
      const data = await response.json()
      setSuites(data || [])
    } catch (error) {
      console.error('Error fetching test suites, using mock data:', error)
      setSuites(MOCK_SUITES)
    }
  }

  const handleOpenDialog = (suite = null) => {
    if (suite) {
      setFormData({ name: suite.name, description: suite.description, tenantId: suite.tenantId })
      setEditingId(suite.id)
    } else {
      setFormData({ name: '', description: '', tenantId: '' })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ name: '', description: '', tenantId: '' })
    setEditingId(null)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSave = async () => {
    try {
      const method = editingId ? 'PUT' : 'POST'
      const url = editingId ? `/api/test-suites/${editingId}` : '/api/test-suites'
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      })
      if (response.ok) {
        handleCloseDialog()
        fetchSuites()
      }
    } catch (error) {
      console.error('Error saving test suite:', error)
      if (editingId) {
        setSuites(suites.map(s => s.id === editingId ? { ...formData, id: editingId, testCount: 0 } : s))
      } else {
        setSuites([...suites, { ...formData, id: Math.max(...suites.map(s => s.id), 0) + 1, testCount: 0 }])
      }
      handleCloseDialog()
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this test suite?')) {
      try {
        const response = await fetch(`/api/test-suites/${id}`, { method: 'DELETE' })
        if (response.ok) {
          fetchSuites()
        }
      } catch (error) {
        console.error('Error deleting test suite:', error)
        setSuites(suites.filter(s => s.id !== id))
      }
    }
  }

  return (
    <Card>
      <CardHeader 
        title="Test Suites (Admin View)"
        subheader="Manage all test suites across tenants"
        action={<Button variant="contained" onClick={() => handleOpenDialog()}>Add Suite</Button>}
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>ID</strong></TableCell>
                <TableCell><strong>Name</strong></TableCell>
                <TableCell><strong>Description</strong></TableCell>
                <TableCell><strong>Test Count</strong></TableCell>
                <TableCell><strong>Status</strong></TableCell>
                <TableCell align="center"><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {suites.map(suite => (
                <TableRow key={suite.id}>
                  <TableCell>{suite.id}</TableCell>
                  <TableCell><strong>{suite.name}</strong></TableCell>
                  <TableCell>{suite.description}</TableCell>
                  <TableCell>
                    <Chip label={suite.testCount || 0} size="small" />
                  </TableCell>
                  <TableCell>
                    <Chip label="Ready" color="success" size="small" />
                  </TableCell>
                  <TableCell align="center">
                    <IconButton 
                      size="small" 
                      title="Run"
                    >
                      <PlayArrowOutlined fontSize="small" />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      onClick={() => handleOpenDialog(suite)}
                      title="Edit"
                    >
                      <EditOutlined fontSize="small" />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      onClick={() => handleDelete(suite.id)}
                      title="Delete"
                    >
                      <DeleteOutlined fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {suites.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 3, color: '#999' }}>
                    No test suites found. Click "Add Suite" to create one.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Test Suite' : 'Add New Test Suite'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              label="Suite Name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
            />
            <TextField
              fullWidth
              label="Description"
              name="description"
              multiline
              rows={3}
              value={formData.description}
              onChange={handleInputChange}
            />
            <TextField
              fullWidth
              label="Tenant ID"
              name="tenantId"
              value={formData.tenantId}
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
