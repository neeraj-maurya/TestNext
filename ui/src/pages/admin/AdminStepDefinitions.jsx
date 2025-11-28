import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Dialog, DialogTitle, DialogContent,
  DialogActions, IconButton, Chip
} from '@mui/material'
import { DeleteOutlined, EditOutlined } from '@mui/icons-material'

export default function AdminStepDefinitions() {
  const [steps, setSteps] = useState([])
  const [formData, setFormData] = useState({ 
    name: '', 
    description: '', 
    inputParameters: '', 
    stepType: 'custom' 
  })
  const [openDialog, setOpenDialog] = useState(false)
  const [editingId, setEditingId] = useState(null)

  useEffect(() => {
    fetchSteps()
  }, [])

  const fetchSteps = async () => {
    try {
      const response = await fetch('/api/step-definitions')
      const data = await response.json()
      setSteps(data || [])
    } catch (error) {
      console.error('Error fetching step definitions:', error)
    }
  }

  const handleOpenDialog = (step = null) => {
    if (step) {
      setFormData({
        name: step.name,
        description: step.description,
        inputParameters: step.inputParameters || '',
        stepType: step.stepType
      })
      setEditingId(step.id)
    } else {
      setFormData({ name: '', description: '', inputParameters: '', stepType: 'custom' })
      setEditingId(null)
    }
    setOpenDialog(true)
  }

  const handleCloseDialog = () => {
    setOpenDialog(false)
    setFormData({ name: '', description: '', inputParameters: '', stepType: 'custom' })
    setEditingId(null)
  }

  const handleInputChange = (e) => {
    const { name, value } = e.target
    setFormData(prev => ({ ...prev, [name]: value }))
  }

  const handleSave = async () => {
    try {
      const method = editingId ? 'PUT' : 'POST'
      const url = editingId ? `/api/step-definitions/${editingId}` : '/api/step-definitions'
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      })
      if (response.ok) {
        handleCloseDialog()
        fetchSteps()
      }
    } catch (error) {
      console.error('Error saving step definition:', error)
    }
  }

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this step definition?')) {
      try {
        const response = await fetch(`/api/step-definitions/${id}`, { method: 'DELETE' })
        if (response.ok) {
          fetchSteps()
        }
      } catch (error) {
        console.error('Error deleting step definition:', error)
      }
    }
  }

  return (
    <Card>
      <CardHeader 
        title="Step Definitions (Admin View)"
        subheader="Manage all step definitions and pending requests"
        action={<Button variant="contained" onClick={() => handleOpenDialog()}>Add Step</Button>}
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>ID</strong></TableCell>
                <TableCell><strong>Name</strong></TableCell>
                <TableCell><strong>Description</strong></TableCell>
                <TableCell><strong>Type</strong></TableCell>
                <TableCell><strong>Status</strong></TableCell>
                <TableCell align="center"><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {steps.map(step => (
                <TableRow key={step.id}>
                  <TableCell>{step.id}</TableCell>
                  <TableCell><strong>{step.name}</strong></TableCell>
                  <TableCell>{step.description}</TableCell>
                  <TableCell>
                    <Chip 
                      label={step.stepType}
                      size="small"
                      color={step.stepType === 'predefined' ? 'primary' : 'default'}
                    />
                  </TableCell>
                  <TableCell>
                    <Chip 
                      label={step.status === 'pending' ? 'Pending Request' : 'Approved'}
                      color={step.status === 'pending' ? 'warning' : 'success'}
                      size="small"
                    />
                  </TableCell>
                  <TableCell align="center">
                    <IconButton 
                      size="small" 
                      onClick={() => handleOpenDialog(step)}
                      title="Edit"
                    >
                      <EditOutlined fontSize="small" />
                    </IconButton>
                    <IconButton 
                      size="small" 
                      onClick={() => handleDelete(step.id)}
                      title="Delete"
                    >
                      <DeleteOutlined fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
              {steps.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} align="center" sx={{ py: 3, color: '#999' }}>
                    No step definitions found.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>

      <Dialog open={openDialog} onClose={handleCloseDialog} maxWidth="sm" fullWidth>
        <DialogTitle>{editingId ? 'Edit Step Definition' : 'Add New Step Definition'}</DialogTitle>
        <DialogContent sx={{ pt: 2 }}>
          <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
            <TextField
              fullWidth
              label="Step Name"
              name="name"
              value={formData.name}
              onChange={handleInputChange}
            />
            <TextField
              fullWidth
              label="Description"
              name="description"
              multiline
              rows={2}
              value={formData.description}
              onChange={handleInputChange}
            />
            <TextField
              fullWidth
              label="Input Parameters (JSON)"
              name="inputParameters"
              multiline
              rows={3}
              value={formData.inputParameters}
              onChange={handleInputChange}
              helperText='e.g., [{"name":"url","type":"string"}]'
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
