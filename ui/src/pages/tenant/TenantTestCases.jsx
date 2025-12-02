import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, IconButton,
  Dialog, DialogTitle, DialogContent, DialogActions, Button, TextField
} from '@mui/material'
import { PlayArrowOutlined } from '@mui/icons-material'
import { useApi } from '../../hooks/useApi'

const MOCK_SUITES = [
  { id: 1, name: 'Login Tests', description: 'Test user authentication', projectId: 1, testCount: 5 },
  { id: 2, name: 'Payment Tests', description: 'Test payment flows', projectId: 1, testCount: 8 },
]

export default function TenantTestCases() {
  const [suites, setSuites] = useState(MOCK_SUITES)
  const api = useApi()

  useEffect(() => {
    fetchSuites()
  }, [])

  const fetchSuites = async () => {
    try {
      const response = await api.get('/api/test-suites')
      if (Array.isArray(response)) {
        setSuites(response)
      } else if (response?.value) {
        setSuites(response.value)
      } else {
        setSuites(MOCK_SUITES)
      }
    } catch (error) {
      console.error('Error fetching test suites:', error)
      setSuites(MOCK_SUITES)
    }
  }

  const [openTestsDialog, setOpenTestsDialog] = useState(false)
  const [selectedSuite, setSelectedSuite] = useState(null)
  const [suiteTests, setSuiteTests] = useState([])
  const [openCreateTestDialog, setOpenCreateTestDialog] = useState(false)
  const [newTestName, setNewTestName] = useState('')
  const [availableSteps, setAvailableSteps] = useState([])
  const [selectedSteps, setSelectedSteps] = useState([])

  const [executionResult, setExecutionResult] = useState(null)
  const [runningTestId, setRunningTestId] = useState(null)

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
      const data = await api.get('/api/step-definitions')
      setAvailableSteps(Array.isArray(data) ? data : [])
    } catch (error) {
      console.error('Error fetching steps:', error)
    }
  }

  const handleAddStep = (stepId) => {
    const step = availableSteps.find(s => s.id === stepId)
    if (step) {
      const initialParams = {}
      if (step.inputs) {
        step.inputs.forEach(input => {
          initialParams[input.name] = ''
        })
      }
      setSelectedSteps([...selectedSteps, {
        stepDefinitionId: step.id,
        name: step.name,
        inputs: step.inputs,
        parameters: initialParams
      }])
    }
  }

  const handleParamChange = (stepIdx, paramName, value) => {
    const newSteps = [...selectedSteps]
    newSteps[stepIdx].parameters[paramName] = value
    setSelectedSteps(newSteps)
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

  const handleRunTest = async (testId) => {
    setRunningTestId(testId)
    try {
      let exec = await api.post(`/api/tests/${testId}/executions`)
      // Poll for completion
      while (['ACCEPTED', 'PENDING', 'RUNNING'].includes(exec.status)) {
        await new Promise(r => setTimeout(r, 1000))
        exec = await api.get(`/api/executions/${exec.id}`)
      }
      setExecutionResult(exec)
    } catch (error) {
      console.error('Error running test:', error)
      alert('Execution failed to start')
    } finally {
      setRunningTestId(null)
    }
  }

  return (
    <Card>
      <CardHeader
        title="Test Suites (Tenant View)"
        subheader="Browse and run test suites"
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>ID</strong></TableCell>
                <TableCell><strong>Name</strong></TableCell>
                <TableCell><strong>Description</strong></TableCell>
                <TableCell align="center"><strong>Test Count</strong></TableCell>
                <TableCell align="center"><strong>Action</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {suites.map(suite => (
                <TableRow key={suite.id}>
                  <TableCell>{suite.id}</TableCell>
                  <TableCell><strong>{suite.name}</strong></TableCell>
                  <TableCell>{suite.description}</TableCell>
                  <TableCell align="center">{suite.testCount || 0}</TableCell>
                  <TableCell align="center">
                    <IconButton
                      size="small"
                      onClick={() => handleManageTests(suite)}
                      title="Manage Tests"
                    >
                      <PlayArrowOutlined fontSize="small" />
                    </IconButton>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>

      {/* Tests List Dialog */}
      <Dialog open={openTestsDialog} onClose={() => setOpenTestsDialog(false)} maxWidth="md" fullWidth>
        <DialogTitle>
          Tests in {selectedSuite?.name}
          <Button
            variant="contained"
            size="small"
            sx={{ float: 'right' }}
            onClick={() => {
              setNewTestName('')
              setSelectedSteps([])
              setOpenCreateTestDialog(true)
            }}
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
                  <TableCell align="center">Action</TableCell>
                </TableRow>
              </TableHead>
              <TableBody>
                {suiteTests.map(test => (
                  <TableRow key={test.id}>
                    <TableCell>{test.id}</TableCell>
                    <TableCell>{test.name}</TableCell>
                    <TableCell>{test.steps ? test.steps.length : 0} steps</TableCell>
                    <TableCell align="center">
                      <Button
                        size="small"
                        variant="outlined"
                        onClick={() => handleRunTest(test.id)}
                        disabled={runningTestId === test.id}
                      >
                        {runningTestId === test.id ? 'Running...' : 'Run'}
                      </Button>
                    </TableCell>
                  </TableRow>
                ))}
                {suiteTests.length === 0 && (
                  <TableRow><TableCell colSpan={4} align="center">No tests found</TableCell></TableRow>
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
              <div key={idx} style={{ padding: 12, background: '#f5f5f5', marginBottom: 8, borderRadius: 4 }}>
                <div style={{ fontWeight: 'bold', marginBottom: 8 }}>{idx + 1}. {step.name}</div>
                {step.inputs && step.inputs.map(input => (
                  <TextField
                    key={input.name}
                    label={input.name}
                    size="small"
                    fullWidth
                    margin="dense"
                    value={step.parameters[input.name] || ''}
                    onChange={(e) => handleParamChange(idx, input.name, e.target.value)}
                    helperText={`Type: ${input.type}`}
                  />
                ))}
              </div>
            ))}
            <div style={{ marginTop: 10 }}>
              <select
                onChange={(e) => handleAddStep(Number(e.target.value))}
                value=""
                style={{ padding: 8, width: '100%' }}
              >
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

      {/* Execution Result Dialog */}
      <Dialog open={!!executionResult} onClose={() => setExecutionResult(null)} maxWidth="sm" fullWidth>
        <DialogTitle>Execution Result</DialogTitle>
        <DialogContent>
          {executionResult && (
            <div>
              <p><strong>Status:</strong> {executionResult.status}</p>
              <p><strong>Steps:</strong></p>
              {executionResult.steps && executionResult.steps.map((step, idx) => (
                <div key={idx} style={{
                  padding: 8,
                  marginBottom: 4,
                  borderLeft: `4px solid ${step.status === 'FINISHED' ? 'green' : 'red'}`,
                  background: '#fafafa'
                }}>
                  <div><strong>Step {idx + 1}:</strong> {step.status}</div>
                  {step.result && step.result.raw && (
                    <pre style={{ fontSize: '0.8em', overflow: 'auto' }}>
                      {JSON.stringify(JSON.parse(step.result.raw), null, 2)}
                    </pre>
                  )}
                </div>
              ))}
            </div>
          )}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setExecutionResult(null)}>Close</Button>
        </DialogActions>
      </Dialog>
    </Card>
  )
}
