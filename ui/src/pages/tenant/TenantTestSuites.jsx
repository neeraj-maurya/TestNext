import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, IconButton, Chip
} from '@mui/material'
import { DeleteOutlined, EditOutlined, PlayArrowOutlined, ExpandMoreOutlined } from '@mui/icons-material'

const MOCK_TENANT_SUITES = [
  { id: 1, name: 'Smoke Tests', description: 'Basic functionality tests', testCount: 10, lastRun: '2025-11-27 14:30' },
  { id: 2, name: 'E2E Tests', description: 'End-to-end user workflows', testCount: 25, lastRun: '2025-11-26 10:15' },
]

export default function TenantTestSuites() {
  const [suites, setSuites] = useState(MOCK_TENANT_SUITES)
  const [formData, setFormData] = useState({ name: '', description: '' })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const [expandedId, setExpandedId] = useState(null)

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
      setSuites(MOCK_TENANT_SUITES)
    }
  }

  const handleOpenDialog = (suite = null) => {
    if (suite) {
      setFormData({ name: suite.name, description: suite.description })
      setEditingId(suite.id)
    } else {
      setFormData({ name: '', description: '' })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ name: '', description: '' })
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
        setSuites(suites.map(s => s.id === editingId ? { ...s, ...formData } : s))
      } else {
        setSuites([...suites, { ...formData, id: Math.max(...suites.map(s => s.id), 0) + 1, testCount: 0, lastRun: '' }])
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
        title="My Test Suites"
        subheader="Create and manage test suites for your tenant"
        action={<Button variant="contained" onClick={() => handleOpenDialog()}>Create Suite</Button>}
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>Name</strong></TableCell>
                <TableCell><strong>Description</strong></TableCell>
                <TableCell><strong>Test Count</strong></TableCell>
                <TableCell><strong>Last Run</strong></TableCell>
                <TableCell align="center"><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {suites.map(suite => (
                <TableRow key={suite.id}>
                  <TableCell><strong>{suite.name}</strong></TableCell>
                  <TableCell>{suite.description}</TableCell>
                  <TableCell>
                    <Chip label={suite.testCount || 0} size="small" />
                  </TableCell>
                  <TableCell>{suite.lastRun || '—'}</TableCell>
                  <TableCell align="center">
                    <IconButton 
                      size="small" 
                      title="Run Suite"
                    >
                      <PlayArrowOutlined fontSize="small" />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      title="View Tests"
                      onClick={() => setExpandedId(expandedId === suite.id ? null : suite.id)}
                    >
                      <ExpandMoreOutlined fontSize="small" />
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
                  <TableCell colSpan={5} align="center" sx={{ py: 3, color: '#999' }}>
                    No test suites. Click "Create Suite" to get started.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Test Suite' : 'Create New Test Suite'}</DialogTitle>
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
