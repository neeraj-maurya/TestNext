import React, { useState, useEffect } from 'react'
import { useSearchParams, useNavigate } from 'react-router-dom'
import axios from 'axios'
import {
  Card, CardContent, CardHeader, Typography, Chip, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, Button, Dialog, DialogTitle,
  DialogContent, DialogActions, Alert
} from '@mui/material'
import { DeleteOutline, ArrowBack } from '@mui/icons-material'

export default function ExecutionView() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const id = searchParams.get('id')
  const [execution, setExecution] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [openDelete, setOpenDelete] = useState(false)

  useEffect(() => {
    if (id) fetchExecution()
  }, [id])

  const fetchExecution = async () => {
    try {
      setLoading(true)
      const res = await axios.get(`/api/executions/${id}`)
      setExecution(res.data)
      setLoading(false)
    } catch (err) {
      setError('Failed to load execution details')
      setLoading(false)
    }
  }

  const handleDelete = async () => {
    try {
      await axios.delete(`/api/executions/${id}`)
      setOpenDelete(false)
      navigate(-1) // Go back
    } catch (err) {
      console.error(err)
      alert('Failed to delete execution. You may not have permission.')
      setOpenDelete(false)
    }
  }

  if (loading) return <Box p={3}>Loading...</Box>
  if (error) return <Box p={3} color="error.main">{error}</Box>
  if (!execution) return <Box p={3}>Execution not found</Box>

  return (
    <Box p={3}>
      <Button startIcon={<ArrowBack />} onClick={() => navigate(-1)} sx={{ mb: 2 }}>
        Back
      </Button>

      <Card sx={{ mb: 3 }}>
        <CardHeader
          title={`Execution ${execution.id.substring(0, 8)}...`}
          subheader={new Date(execution.startedAt).toLocaleString()}
          action={
            <Button
              color="error"
              variant="outlined"
              startIcon={<DeleteOutline />}
              onClick={() => setOpenDelete(true)}
            >
              Delete
            </Button>
          }
        />
        <CardContent>
          <Box display="flex" gap={4} mb={3}>
            <Box>
              <Typography variant="caption" color="textSecondary">Status</Typography>
              <Box>
                <Chip
                  label={execution.status}
                  color={execution.status === 'FINISHED' ? 'success' : execution.status === 'FAILED' ? 'error' : 'warning'}
                />
              </Box>
            </Box>
            <Box>
              <Typography variant="caption" color="textSecondary">Test Suite</Typography>
              <Typography variant="body1">{execution.suiteName || '-'}</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="textSecondary">Test Case</Typography>
              <Typography variant="body1">{execution.testName || '-'}</Typography>
            </Box>
            <Box>
              <Typography variant="caption" color="textSecondary">Duration</Typography>
              <Typography variant="body1">
                {execution.finishedAt
                  ? `${(new Date(execution.finishedAt) - new Date(execution.startedAt)) / 1000}s`
                  : '-'}
              </Typography>
            </Box>
          </Box>

          <Typography variant="h6" gutterBottom>Steps</Typography>
          <TableContainer component={Paper} variant="outlined">
            <Table size="small">
              <TableHead>
                <TableRow sx={{ bgcolor: '#f5f5f5' }}>
                  <TableCell>Step</TableCell>
                  <TableCell>Status</TableCell>
                  <TableCell>Result</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {execution.steps && execution.steps.map((step) => (
                  <TableRow key={step.id}>
                    <TableCell>{step.stepDefinitionId}</TableCell>
                    <TableCell>
                      <Chip
                        label={step.status}
                        size="small"
                        color={step.status === 'FINISHED' ? 'success' : step.status === 'FAILED' ? 'error' : 'default'}
                        variant="outlined"
                      />
                    </TableCell>
                    <TableCell>
                      <Box component="pre" sx={{ m: 0, fontSize: '0.75rem', overflowX: 'auto' }}>
                        {step.result && step.result.raw}
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </CardContent>
      </Card>

      <Dialog open={openDelete} onClose={() => setOpenDelete(false)}>
        <DialogTitle>Delete Execution?</DialogTitle>
        <DialogContent>
          Are you sure you want to delete this execution record? This action cannot be undone.
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenDelete(false)}>Cancel</Button>
          <Button onClick={handleDelete} color="error" variant="contained">Delete</Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
