import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, TextField, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Box, MenuItem, Grid
} from '@mui/material'

export default function TenantReports() {
  const [executions, setExecutions] = useState([])
  const [suites, setSuites] = useState([])
  const [selectedSuite, setSelectedSuite] = useState('')
  const [reportData, setReportData] = useState(null)

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

  const handleSuiteChange = (e) => {
    setSelectedSuite(e.target.value)
  }

  const getSuiteName = (suiteId) => {
    const suite = suites.find(s => s.id === suiteId)
    return suite ? suite.name : `Suite #${suiteId}`
  }

  const calculateStats = () => {
    const suiteExecutions = selectedSuite
      ? executions.filter(ex => ex.suiteId === parseInt(selectedSuite))
      : executions

    if (suiteExecutions.length === 0) return null

    const totalPass = suiteExecutions.reduce((sum, ex) => sum + (ex.passCount || 0), 0)
    const totalFail = suiteExecutions.reduce((sum, ex) => sum + (ex.failCount || 0), 0)
    const successRate = totalPass + totalFail > 0 
      ? ((totalPass / (totalPass + totalFail)) * 100).toFixed(2)
      : 0

    return {
      totalExecutions: suiteExecutions.length,
      totalPass,
      totalFail,
      successRate,
      executions: suiteExecutions
    }
  }

  const stats = calculateStats()

  return (
    <Card>
      <CardHeader 
        title="Test Reports & Analytics"
        subheader="View execution trends and test quality metrics"
      />
      <CardContent>
        <Box sx={{ mb: 3 }}>
          <TextField
            select
            label="Filter by Test Suite"
            value={selectedSuite}
            onChange={handleSuiteChange}
            sx={{ minWidth: 300 }}
          >
            <MenuItem value="">All Suites</MenuItem>
            {suites.map(suite => (
              <MenuItem key={suite.id} value={suite.id}>
                {suite.name}
              </MenuItem>
            ))}
          </TextField>
        </Box>

        {stats ? (
          <>
            <Grid container spacing={2} sx={{ mb: 3 }}>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Box sx={{ textAlign: 'center' }}>
                      <Box sx={{ fontSize: '32px', fontWeight: 'bold', color: '#4caf50' }}>
                        {stats.totalExecutions}
                      </Box>
                      <Box sx={{ color: '#666', fontSize: '14px' }}>
                        Total Executions
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Box sx={{ textAlign: 'center' }}>
                      <Box sx={{ fontSize: '32px', fontWeight: 'bold', color: '#4caf50' }}>
                        {stats.totalPass}
                      </Box>
                      <Box sx={{ color: '#666', fontSize: '14px' }}>
                        Passed Tests
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Box sx={{ textAlign: 'center' }}>
                      <Box sx={{ fontSize: '32px', fontWeight: 'bold', color: '#f44336' }}>
                        {stats.totalFail}
                      </Box>
                      <Box sx={{ color: '#666', fontSize: '14px' }}>
                        Failed Tests
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
              <Grid item xs={12} sm={6} md={3}>
                <Card variant="outlined">
                  <CardContent>
                    <Box sx={{ textAlign: 'center' }}>
                      <Box sx={{ fontSize: '32px', fontWeight: 'bold', color: '#2196f3' }}>
                        {stats.successRate}%
                      </Box>
                      <Box sx={{ color: '#666', fontSize: '14px' }}>
                        Success Rate
                      </Box>
                    </Box>
                  </CardContent>
                </Card>
              </Grid>
            </Grid>

            <Box sx={{ mb: 3 }}>
              <h3>Execution History</h3>
              <TableContainer component={Paper}>
                <Table size="small">
                  <TableHead>
                    <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                      <TableCell><strong>ID</strong></TableCell>
                      <TableCell><strong>Suite</strong></TableCell>
                      <TableCell><strong>Status</strong></TableCell>
                      <TableCell><strong>Pass</strong></TableCell>
                      <TableCell><strong>Fail</strong></TableCell>
                      <TableCell><strong>Success Rate</strong></TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {stats.executions.map(execution => {
                      const rate = execution.passCount + execution.failCount > 0
                        ? ((execution.passCount / (execution.passCount + execution.failCount)) * 100).toFixed(0)
                        : 0
                      return (
                        <TableRow key={execution.id}>
                          <TableCell>{execution.id}</TableCell>
                          <TableCell>{getSuiteName(execution.suiteId)}</TableCell>
                          <TableCell>{execution.status}</TableCell>
                          <TableCell sx={{ color: '#4caf50' }}>{execution.passCount}</TableCell>
                          <TableCell sx={{ color: '#f44336' }}>{execution.failCount}</TableCell>
                          <TableCell>{rate}%</TableCell>
                        </TableRow>
                      )
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            </Box>
          </>
        ) : (
          <Box sx={{ textAlign: 'center', py: 5, color: '#999' }}>
            No execution data available. Run some tests to see reports.
          </Box>
        )}
      </CardContent>
    </Card>
  )
}
