import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Typography, Box
} from '@mui/material'
import { useApi } from '../../hooks/useApi'

export default function AdminStepDefinitions() {
  const [steps, setSteps] = useState([])
  const [loading, setLoading] = useState(true)
  const api = useApi()

  useEffect(() => {
    loadSteps()
  }, [])

  const loadSteps = async () => {
    setLoading(true)
    try {
      const data = await api.get('/api/test-steps-library')
      const stepList = Array.isArray(data) ? data : (data?.value || [])
      setSteps(stepList)
    } catch (error) {
      console.error('Error loading steps:', error)
      alert('Failed to load test steps')
    } finally {
      setLoading(false)
    }
  }

  if (loading) return <Box p={3}>Loading Test Steps...</Box>

  return (
    <Card>
      <CardHeader
        title="Test Steps Library"
        subheader="Automatically scanned available test steps from backend"
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell style={{ width: 50 }}><strong>#</strong></TableCell>
                <TableCell><strong>Name</strong></TableCell>
                <TableCell><strong>Description</strong></TableCell>
                <TableCell><strong>Input Type</strong></TableCell>
                <TableCell><strong>Output Type</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {steps.map((step, index) => (
                <TableRow key={step.id || index}>
                  <TableCell>{index + 1}</TableCell>
                  <TableCell>
                    <Box>
                      <Typography variant="subtitle2">{step.name}</Typography>
                      <Typography variant="caption" color="textSecondary">{step.refId}</Typography>
                    </Box>
                  </TableCell>
                  <TableCell>{step.description}</TableCell>
                  <TableCell>
                    <Typography variant="body2">{step.parameterTypes || 'None'}</Typography>
                  </TableCell>
                  <TableCell>
                    <Typography variant="body2">{step.returnType || 'Void'}</Typography>
                  </TableCell>
                </TableRow>
              ))}
              {steps.length === 0 && (
                <TableRow>
                  <TableCell colSpan={5} align="center">
                    No test steps found in the library.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>
    </Card>
  )
}
