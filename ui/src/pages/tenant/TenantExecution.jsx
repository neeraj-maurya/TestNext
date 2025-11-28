import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, MenuItem, Chip
} from '@mui/material'
import { PlayArrowOutlined } from '@mui/icons-material'

export default function TenantExecution() {
  const [executions, setExecutions] = useState([])
  const [suites, setSuites] = useState([])
  const [formData, setFormData] = useState({ suiteId: '' })
  const [openDialog, setOpenDialog] = useState(false)

  useEffect(() => {
    fetchExecutions()
    fetchSuites()
  }, [])

  const fetchExecutions = async () => {
    try {
      const response = await fetch('/api/executions')
      const data = await response.json()
      setExecutions(data || [])
    } catch (error) {
      console.error('Error fetching executions:', error)
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

  const handleOpenDialog = () => {
    setFormData({ suiteId: '' })
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ suiteId: '' })
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleExecute = async () => {
    try {
      const response = await fetch('/api/executions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      })
      if (response.ok) {
        handleCloseDialog()
        fetchExecutions()
      }
    } catch (error) {
      console.error('Error starting execution:', error)
    }
  }

  const getSuiteName = (suiteId) => {
    const suite = suites.find(s => s.id === suiteId)
    return suite ? suite.name : `Suite #${suiteId}`
  }

  return (
    <Card>
      <CardHeader 
        title="Test Execution"
        subheader="Run tests and view execution history"
        action={<Button variant="contained" startIcon={<PlayArrowOutlined />} onClick={handleOpenDialog}>Run Tests</Button>}
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>ID</strong></TableCell>
                <TableCell><strong>Test Suite</strong></TableCell>
                <TableCell><strong>Started</strong></TableCell>
                <TableCell><strong>Duration</strong></TableCell>
                <TableCell><strong>Status</strong></TableCell>
                <TableCell><strong>Pass/Fail</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {executions.map(execution => (
                <TableRow key={execution.id}>
                  <TableCell>{execution.id}</TableCell>
                  <TableCell>{getSuiteName(execution.suiteId)}</TableCell>
                  <TableCell>{execution.startTime || '—'}</TableCell>
                  <TableCell>{execution.duration || '—'}</TableCell>
                  <TableCell>
                    <Chip 
                      label={execution.status}
                      size="small"
                      color={
                        execution.status === 'completed' ? 'success' :
                        execution.status === 'failed' ? 'error' :
                        'warning'
                      }
                    />
                  </TableCell>
                  <TableCell>
                    <Box sx={{ display: 'flex', gap: 1 }}>
                      <Chip label={`${execution.passCount || 0} ✓`} size="small" color="success" variant="outlined" />
                      <Chip label={`${execution.failCount || 0} ✗`} size="small" color="error" variant="outlined" />
                    </Box>
                  </TableCell>
                </TableRow>
              ))}
              {executions.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 3, color: '#999' }}>
                    No executions yet. Click "Run Tests" to execute a test suite.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>Execute Test Suite</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              select
              label="Select Test Suite"
              name="suiteId"
              value={formData.suiteId}
              onChange={handleInputChange}
            >
              {suites.map(suite => (
                <MenuItem key={suite.id} value={suite.id}>
                  {suite.name} ({suite.testCount || 0} tests)
                </MenuItem>
              ))}
            </TextField>
          </Box>
        </DialogContent>
        <DialogActions>
          <Button onClick={handleCloseDialog}>Cancel</Button>
          <Button onClick={handleExecute} variant="contained" startIcon={<PlayArrowOutlined />}>Execute</Button>
        </DialogActions>
      </Dialog>
    </Card>
  )
}
