import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, IconButton, Chip, MenuItem
} from '@mui/material'
import { DeleteOutlined, EditOutlined, PlayArrowOutlined, BookmarkOutlined } from '@mui/icons-material'

export default function TenantTestCases() {
  const [testCases, setTestCases] = useState([])
  const [suites, setSuites] = useState([])
  const [formData, setFormData] = useState({ 
    name: '', 
    description: '', 
    suiteId: '',
    status: 'draft'
  })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)

  useEffect(() => {
    fetchTestCases()
    fetchSuites()
  }, [])

  const fetchTestCases = async () => {
    try {
      const response = await fetch('/api/test-cases')
      const data = await response.json()
      setTestCases(data || [])
    } catch (error) {
      console.error('Error fetching test cases:', error)
    }
  }

  const fetchSuites = async () => {
    try {
      const response = await fetch('/api/test-suites')
      const data = await response.json()
      setSuites(data || [])
    } catch (error) {
      console.error('Error fetching test suites:', error)
    }
  }

  const handleOpenDialog = (testCase = null) => {
    if (testCase) {
      setFormData({
        name: testCase.name,
        description: testCase.description,
        suiteId: testCase.suiteId,
        status: testCase.status
      })
      setEditingId(testCase.id)
    } else {
      setFormData({ name: '', description: '', suiteId: '', status: 'draft' })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ name: '', description: '', suiteId: '', status: 'draft' })
    setEditingId(null)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSave = async () => {
    try {
      const method = editingId ? 'PUT' : 'POST'
      const url = editingId ? `/api/test-cases/${editingId}` : '/api/test-cases'
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      })
      if (response.ok) {
        handleCloseDialog()
        fetchTestCases()
      }
    } catch (error) {
      console.error('Error saving test case:', error)
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this test case?')) {
      try {
        const response = await fetch(`/api/test-cases/${id}`, { method: 'DELETE' })
        if (response.ok) {
          fetchTestCases()
        }
      } catch (error) {
        console.error('Error deleting test case:', error)
      }
    }
  }

  const getSuiteName = (suiteId) => {
    const suite = suites.find(s => s.id === suiteId)
    return suite ? suite.name : `Suite #${suiteId}`
  }

  return (
    <Card>
      <CardHeader 
        title="Test Cases"
        subheader="Create and manage test cases within your test suites"
        action={<Button variant="contained" onClick={() => handleOpenDialog()}>Create Test Case</Button>}
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>Name</strong></TableCell>
                <TableCell><strong>Test Suite</strong></TableCell>
                <TableCell><strong>Description</strong></TableCell>
                <TableCell><strong>Status</strong></TableCell>
                <TableCell align="center"><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {testCases.map(testCase => (
                <TableRow key={testCase.id}>
                  <TableCell><strong>{testCase.name}</strong></TableCell>
                  <TableCell>{getSuiteName(testCase.suiteId)}</TableCell>
                  <TableCell>{testCase.description}</TableCell>
                  <TableCell>
                    <Chip 
                      label={testCase.status}
                      size="small"
                      color={
                        testCase.status === 'draft' ? 'default' :
                        testCase.status === 'ready' ? 'success' :
                        'warning'
                      }
                    />
                  </TableCell>
                  <TableCell align="center">
                    <IconButton 
                      size="small" 
                      title="Run Test Case"
                    >
                      <PlayArrowOutlined fontSize="small" />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      title="Edit"
                      onClick={() => handleOpenDialog(testCase)}
                    >
                      <EditOutlined fontSize="small" />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      title="Delete"
                      onClick={() => handleDelete(testCase.id)}
                    >
                      <DeleteOutlined fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {testCases.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center" sx={{ py: 3, color: '#999' }}>
                    No test cases. Click "Create Test Case" to get started.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Test Case' : 'Create New Test Case'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              label="Test Case Name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
            />
            <TextField
              fullWidth
              select
              label="Test Suite"
              name="suiteId"
              value={formData.suiteId}
              onChange={handleInputChange}
            >
              {suites.map(suite => (
                <MenuItem key={suite.id} value={suite.id}>
                  {suite.name}
                </MenuItem>
              ))}
            </TextField>
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
              select
              label="Status"
              name="status"
              value={formData.status}
              onChange={handleInputChange}
            >
              <MenuItem value="draft">Draft</MenuItem>
              <MenuItem value="ready">Ready</MenuItem>
              <MenuItem value="obsolete">Obsolete</MenuItem>
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
