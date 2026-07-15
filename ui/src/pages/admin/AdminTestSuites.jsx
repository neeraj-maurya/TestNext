import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, IconButton, Chip, MenuItem
} from '@mui/material'
import { DeleteOutlined, EditOutlined, PlayArrowOutlined } from '@mui/icons-material'
import { useApi } from '../../hooks/useApi'


export default function AdminTestSuites() {
  const [suites, setSuites] = useState([])
  const [tenants, setTenants] = useState([])
  const [loadError, setLoadError] = useState('')
  const [formData, setFormData] = useState({ name: '', description: '', projectId: 1, tenantId: '' })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)
  const api = useApi()

  useEffect(() => {
    fetchSuites()
    fetchTenants()
  }, [])

  const fetchTenants = async () => {
    try {
      const response = await api.get('/api/tenants')
      setTenants(Array.isArray(response) ? response : (response?.value || []))
    } catch (error) {
      console.error('Error fetching tenants:', error)
    }
  }

  const fetchSuites = async () => {
    try {
      const response = await api.get('/api/test-suites')
      setSuites(Array.isArray(response) ? response : (response?.value || []))
    } catch (error) {
      console.error('Error fetching test suites:', error)
      setLoadError('Failed to load test suites. Is the backend running?')
      setSuites([])
    }
  }

  const handleOpenDialog = (suite = null) => {
    if (suite) {
      setFormData({ name: suite.name, description: suite.description, projectId: suite.projectId || 1, tenantId: suite.tenantId || '' })
      setEditingId(suite.id)
    } else {
      setFormData({ name: '', description: '', projectId: 1, tenantId: '' })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ name: '', description: '', projectId: 1, tenantId: '' })
    setEditingId(null)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSave = async () => {
    try {
      if (editingId) {
        await api.put(`/api/test-suites/${editingId}`, formData)
      } else {
        await api.post('/api/test-suites', formData)
      }
      handleCloseDialog()
      fetchSuites()
    } catch (error) {
      console.error('Error saving test suite:', error)
      if (editingId) {
        setSuites(suites.map(s => s.id === editingId ? { ...formData, id: editingId, testCount: 0 } : s))
      } else {
        setSuites([...suites, { ...formData, id: Math.max(...suites.map(s => s.id || 0), 0) + 1, testCount: 0 }])
      }
      handleCloseDialog()
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this test suite?')) {
      try {
        await api.delete(`/api/test-suites/${id}`)
        fetchSuites()
      } catch (error) {
        console.error('Error deleting test suite:', error)
        setSuites(suites.filter(s => s.id !== id))
      }
    }
  }

  const [openTestsDialog, setOpenTestsDialog] = useState(false)
  const [selectedSuite, setSelectedSuite] = useState(null)
  const [suiteTests, setSuiteTests] = useState([])
  const [openCreateTestDialog, setOpenCreateTestDialog] = useState(false)
  const [newTestName, setNewTestName] = useState('')
  const [availableSteps, setAvailableSteps] = useState([])
  const [selectedSteps, setSelectedSteps] = useState([])

  const handleManageTests = async (suite) => {
    setSelectedSuite(suite)
    setOpenTestsDialog(true)
    fetchTests(suite.id)
    fetchSteps()
  }

  const fetchTests = async (suiteId) => {
    try {
      const data = await api.get(`/api/test-suites/${suiteId}/tests`)
      setSuiteTests(Array.isArray(data) ? data : [])
    } catch (error) {
      console.error('Error fetching tests:', error)
      setSuiteTests([])
    }
  }

  const fetchSteps = async () => {
    try {
      const data = await api.get('/api/test-steps-library')
      setAvailableSteps(Array.isArray(data) ? data : [])
    } catch (error) {
      console.error('Error fetching step definitions:', error)
    }
  }

  const handleAddStep = (stepId) => {
    const step = availableSteps.find(s => s.id === stepId)
    if (step) {
      setSelectedSteps([...selectedSteps, { stepDefinitionId: step.id, name: step.name, parameters: {} }])
    }
  }

  const handleCreateTest = async () => {
    if (!newTestName.trim()) return
    try {
      await api.post(`/api/test-suites/${selectedSuite.id}/tests`, {
        name: newTestName,
        steps: selectedSteps
      })
      setOpenCreateTestDialog(false)
      setNewTestName('')
      setSelectedSteps([])
      fetchTests(selectedSuite.id)
    } catch (error) {
      console.error('Error creating test:', error)
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
        {loadError && (
          <div style={{ color: '#d32f2f', marginBottom: 12, padding: '8px 12px', background: '#ffebee', borderRadius: 4 }}>
            {loadError}
          </div>
        )}
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>ID</strong></TableCell>
                <TableCell><strong>Tenant</strong></TableCell>
                <TableCell><strong>Project</strong></TableCell>
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
                  <TableCell>{tenants.find(t => t.id === suite.tenantId)?.name || suite.tenantId || '-'}</TableCell>
                  <TableCell>{suite.projectName || `Project ${suite.projectId}`}</TableCell>
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
                      onClick={() => handleManageTests(suite)}
                      title="Manage Tests"
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
              select
              label="Tenant"
              name="tenantId"
              value={formData.tenantId}
              onChange={handleInputChange}
            >
              <MenuItem value=""><em>None</em></MenuItem>
              {tenants.map(t => (
                <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>
              ))}
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleSave} variant="contained">Save</Button>
        </DialogActions>
      </Dialog>

      {/* Tests List Dialog */}
      <Dialog open={openTestsDialog} onClose={() => setOpenTestsDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Tests in {selectedSuite?.name}
          <Button
            variant="contained"
            size="small"
            sx={{ float: 'right' }}
            onClick={() => setOpenCreateTestDialog(true)}
          >
            Add Test Case
          </Button>
        </DialogTitle>
        <DialogContent>
          <TableContainer>
            <Table size="small">
              <TableHead>
                <TableRow>
                  <TableCell>ID</TableCell>
                  <TableCell>Name</TableCell>
                  <TableCell>Steps</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {suiteTests.map(test => (
                  <TableRow key={test.id}>
                    <TableCell>{test.id}</TableCell>
                    <TableCell>{test.name}</TableCell>
                    <TableCell>{test.steps ? test.steps.length : 0} steps</TableCell>
                  </TableRow>
                ))}
                {suiteTests.length === 0 && (
                  <TableRow><TableCell colSpan={3} align="center">No tests found</TableCell></TableRow>
                )}
              </TableBody>
            </Table>
          </TableContainer>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenTestsDialog(false)}>Close</Button>
        </DialogActions>
      </Dialog>

      {/* Create Test Dialog */}
      <Dialog open={openCreateTestDialog} onClose={() => setOpenCreateTestDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Create New Test Case</DialogTitle>
        <DialogContent>
          <TextField
            autoFocus
            margin="dense"
            label="Test Name"
            fullWidth
            value={newTestName}
            onChange={(e) => setNewTestName(e.target.value)}
          />
          <div style={{ marginTop: 20 }}>
            <h4>Steps</h4>
            {selectedSteps.map((step, idx) => (
              <div key={idx} style={{ padding: 8, background: '#f5f5f5', marginBottom: 4 }}>
                {idx + 1}. {step.name}
              </div>
            ))}
            <div style={{ marginTop: 10 }}>
              <select onChange={(e) => handleAddStep(Number(e.target.value))} value="">
                <option value="">Add a step...</option>
                {availableSteps.map(s => (
                  <option key={s.id} value={s.id}>{s.name}</option>
                ))}
              </select>
            </div>
          </div>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenCreateTestDialog(false)}>Cancel</Button>
          <Button onClick={handleCreateTest} variant="contained" disabled={!newTestName}>Create</Button>
        </DialogActions>
      </Dialog>
    </Card>
  )
}
