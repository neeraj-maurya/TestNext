import React from 'react'
import axios from 'axios'
import { useParams, Link } from 'react-router-dom'
import DynamicForm from '../components/DynamicForm'
import {
    Button,
    Card,
    CardContent,
    Typography,
    Table,
    TableBody,
    TableCell,
    TableHead,
    TableRow,
    Dialog,
    DialogTitle,
    DialogContent,
    DialogActions,
    TextField,
    Select,
    MenuItem,
    FormControl,
    InputLabel,
    IconButton,
    Box
} from '@mui/material'

export default function SuiteDetails() {
    const { projectId, suiteId } = useParams()
    const [suite, setSuite] = React.useState(null)
    const [tests, setTests] = React.useState([])
    const [loading, setLoading] = React.useState(true)
    const [error, setError] = React.useState(null)

    // Create Test Dialog State
    const [openDialog, setOpenDialog] = React.useState(false)
    const [testName, setTestName] = React.useState('')
    const [testSteps, setTestSteps] = React.useState([])

    // Step Definitions
    const [stepDefs, setStepDefs] = React.useState([])

    const loadData = React.useCallback(() => {
        setLoading(true)
        axios.get(`/api/projects/${projectId}/suites/${suiteId}`)
            .then(r => {
                setSuite(r.data)
                // Fetch step definitions using tenantId from suite
                if (r.data.tenantId) {
                    axios.get(`/api/tenants/${r.data.tenantId}/step-definitions`)
                        .then(sd => setStepDefs(sd.data))
                        .catch(e => console.error('Failed to load step definitions', e))
                }
                return axios.get(`/api/test-suites/${suiteId}/tests`)
            })
            .then(r => {
                setTests(r.data)
                setLoading(false)
            })
            .catch(e => {
                console.error(e)
                setError('Failed to load suite details')
                setLoading(false)
            })
    }, [projectId, suiteId])

    React.useEffect(() => {
        loadData()
    }, [loadData])

    const addStep = (defId) => {
        setTestSteps(prev => [...prev, { stepDefinitionId: defId, parameters: {} }])
    }

    const removeStep = (index) => {
        setTestSteps(prev => prev.filter((_, i) => i !== index))
    }

    const updateStepParam = (index, params) => {
        const newSteps = [...testSteps]
        newSteps[index].parameters = params
        setTestSteps(newSteps)
    }

    const createTest = () => {
        if (!testName.trim()) return
        axios.post(`/api/test-suites/${suiteId}/tests`, { name: testName, steps: testSteps })
            .then(() => {
                setTestName('')
                setTestSteps([])
                setOpenDialog(false)
                loadData()
            })
            .catch(e => alert('Error creating test: ' + (e.response?.data?.message || e.message)))
    }

    if (loading) return <div style={{ padding: 20 }}>Loading...</div>
    if (error) return <div style={{ padding: 20, color: 'red' }}>{error}</div>
    if (!suite) return <div style={{ padding: 20 }}>Suite not found</div>

    return (
        <div style={{ padding: 20 }}>
            <div style={{ marginBottom: 20 }}>
                <Link to="/projects" style={{ textDecoration: 'none', color: '#1976d2' }}>&larr; Back to Projects</Link>
            </div>

            <Card style={{ marginBottom: 20 }}>
                <CardContent>
                    <Typography variant="h4" gutterBottom>{suite.name}</Typography>
                    <Typography variant="body1" color="textSecondary">{suite.description || 'No description'}</Typography>
                </CardContent>
            </Card>

            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 16 }}>
                <Typography variant="h5">Test Cases</Typography>
                <Button variant="contained" color="primary" onClick={() => setOpenDialog(true)}>
                    Create Test Case
                </Button>
            </div>

            <Card>
                <Table>
                    <TableHead>
                        <TableRow style={{ backgroundColor: '#f5f5f5' }}>
                            <TableCell><strong>ID</strong></TableCell>
                            <TableCell><strong>Name</strong></TableCell>
                            <TableCell><strong>Steps</strong></TableCell>
                            <TableCell><strong>Actions</strong></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {tests.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={4} align="center">No test cases found</TableCell>
                            </TableRow>
                        ) : (
                            tests.map(test => (
                                <TableRow key={test.id}>
                                    <TableCell>{test.id}</TableCell>
                                    <TableCell>{test.name}</TableCell>
                                    <TableCell>{test.steps ? test.steps.length : 0}</TableCell>
                                    <TableCell>
                                        <Button size="small" variant="outlined" style={{ marginRight: 8 }}>Edit</Button>
                                        <Button
                                            size="small"
                                            color="error"
                                            onClick={() => {
                                                if (window.confirm('Delete test case?')) {
                                                    axios.delete(`/api/tests/${test.id}`)
                                                        .then(() => setTests(prev => prev.filter(t => t.id !== test.id)))
                                                        .catch(e => alert('Failed to delete: ' + (e.response?.status === 403 ? 'Access Denied' : e.message)))
                                                }
                                            }}
                                        >
                                            Delete
                                        </Button>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </Card>

            <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="md" fullWidth>
                <DialogTitle>Create New Test Case</DialogTitle>
                <DialogContent>
                    <TextField
                        autoFocus
                        margin="dense"
                        label="Test Case Name"
                        fullWidth
                        value={testName}
                        onChange={e => setTestName(e.target.value)}
                        style={{ marginBottom: 20 }}
                    />

                    <Typography variant="h6" gutterBottom>Steps</Typography>

                    <div style={{ display: 'flex', gap: 8, flexWrap: 'wrap', marginBottom: 16 }}>
                        {stepDefs.map(def => (
                            <Button key={def.id} variant="outlined" size="small" onClick={() => addStep(def.id)}>
                                + {def.name}
                            </Button>
                        ))}
                    </div>

                    <div style={{ maxHeight: 400, overflowY: 'auto', border: '1px solid #eee', padding: 8 }}>
                        {testSteps.length === 0 && <Typography color="textSecondary">No steps added yet.</Typography>}
                        {testSteps.map((step, idx) => {
                            const def = stepDefs.find(d => d.id === step.stepDefinitionId)
                            return (
                                <Card key={idx} variant="outlined" style={{ marginBottom: 8 }}>
                                    <CardContent style={{ padding: '8px 16px' }}>
                                        <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
                                            <Typography variant="subtitle2">{idx + 1}. {def ? def.name : 'Unknown Step'}</Typography>
                                            <Button size="small" color="error" onClick={() => removeStep(idx)}>Remove</Button>
                                        </div>
                                        <Box mt={1}>
                                            <DynamicForm
                                                schema={{ fields: [{ name: 'param', type: 'string', label: 'Parameters (JSON)' }] }}
                                                onSubmit={(data) => updateStepParam(idx, data)}
                                            />
                                        </Box>
                                    </CardContent>
                                </Card>
                            )
                        })}
                    </div>

                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
                    <Button onClick={createTest} color="primary" variant="contained" disabled={!testName.trim()}>
                        Create
                    </Button>
                </DialogActions>
            </Dialog>
        </div>
    )
}
