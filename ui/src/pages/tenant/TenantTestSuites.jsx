import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, Button, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Chip, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, TextField,
  FormControl, InputLabel, Select, MenuItem, Box, Typography
} from '@mui/material'
import { Add, PlayArrow, Edit, Delete } from '@mui/icons-material'
import { useApi } from '../../hooks/useApi'

export default function TenantTestSuites() {
  const [suites, setSuites] = useState([])
  const [projects, setProjects] = useState([])
  const [selectedProjectId, setSelectedProjectId] = useState('')

  const [openDialog, setOpenDialog] = useState(false)
  const [formData, setFormData] = useState({ name: '', description: '' })

  const api = useApi()

  useEffect(() => {
    fetchProjects()
  }, [])

  useEffect(() => {
    if (selectedProjectId) {
      fetchSuites(selectedProjectId)
    } else {
      setSuites([])
    }
  }, [selectedProjectId])

  const fetchProjects = async () => {
    try {
      // TEMPORARY: Hardcoding tenantId=1 for dev/demo if not available. 
      const tenantId = 1;
      const res = await api.get(`/api/tenants/${tenantId}/projects`)
      if (res && Array.isArray(res)) {
        setProjects(res)
        if (res.length > 0) setSelectedProjectId(res[0].id)
      }
    } catch (error) {
      console.error('Error fetching projects:', error)
    }
  }

  const fetchSuites = async (projectId) => {
    try {
      const res = await api.get(`/api/projects/${projectId}/suites`)
      if (res && Array.isArray(res)) setSuites(res)
      else setSuites([])
    } catch (error) {
      console.error('Error fetching suites:', error)
      setSuites([])
    }
  }

  const handleCreate = async () => {
    if (!selectedProjectId) return
    try {
      await api.post(`/api/projects/${selectedProjectId}/suites`, formData)
      setOpenDialog(false)
      setFormData({ name: '', description: '' })
      fetchSuites(selectedProjectId)
    } catch (error) {
      console.error('Error creating suite:', error)
    }
  }

  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
      <Card>
        <CardContent>
          <FormControl fullWidth>
            <InputLabel>Select Project</InputLabel>
            <Select
              value={selectedProjectId}
              label="Select Project"
              onChange={(e) => setSelectedProjectId(e.target.value)}
            >
              {projects.map(p => (
                <MenuItem key={p.id} value={p.id}>{p.name}</MenuItem>
              ))}
            </Select>
          </FormControl>
        </CardContent>
      </Card>

      {selectedProjectId && (
        <Card>
          <CardHeader
            title="Test Suites"
            action={
              <Button
                variant="contained"
                startIcon={<Add />}
                onClick={() => setOpenDialog(true)}
              >
                New Suite
              </Button>
            }
          />
          <CardContent>
            <TableContainer component={Paper}>
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>ID</TableCell>
                    <TableCell>Name</TableCell>
                    <TableCell>Description</TableCell>
                    <TableCell align="right">Actions</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {suites.length === 0 ? (
                    <TableRow>
                      <TableCell colSpan={4} align="center">No suites found</TableCell>
                    </TableRow>
                  ) : (
                    suites.map(suite => (
                      <TableRow key={suite.id}>
                        <TableCell>{suite.id}</TableCell>
                        <TableCell>{suite.name}</TableCell>
                        <TableCell>{suite.description}</TableCell>
                        <TableCell align="right">
                          <IconButton size="small"><PlayArrow /></IconButton>
                          <IconButton size="small"><Edit /></IconButton>
                          <IconButton size="small"><Delete /></IconButton>
                        </TableCell>
                      </TableRow>
                    ))
                  )}
                </TableBody>
              </Table>
            </TableContainer>
          </CardContent>
        </Card>
      )}

      <Dialog open={openDialog} onClose={() => setOpenDialog(false)}>
        <DialogTitle>Create Test Suite</DialogTitle>
        <DialogContent sx={{ pt: 2, display: 'flex', flexDirection: 'column', gap: 2, minWidth: 400 }}>
          <TextField
            label="Name"
            fullWidth
            value={formData.name}
            onChange={(e) => setFormData({ ...formData, name: e.target.value })}
          />
          <TextField
            label="Description"
            fullWidth
            multiline
            rows={3}
            value={formData.description}
            onChange={(e) => setFormData({ ...formData, description: e.target.value })}
          />
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
          <Button variant="contained" onClick={handleCreate}>Create</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
