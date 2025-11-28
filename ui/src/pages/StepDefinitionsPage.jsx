import React from 'react'
import axios from 'axios'
import { 
  Button, 
  TextField, 
  Card, 
  CardContent, 
  CardActions,
  Grid,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow
} from '@mui/material'

// Predefined step definitions for common test actions
const PREDEFINED_STEPS = [
  {
    name: 'Navigate to URL',
    description: 'Navigate to a specified URL',
    inputs: [{ name: 'url', type: 'string', required: true }]
  },
  {
    name: 'Book a Trade',
    description: 'Book a trade with specified parameters',
    inputs: [
      { name: 'security_id', type: 'string', required: true },
      { name: 'quantity', type: 'number', required: true },
      { name: 'price', type: 'number', required: true },
      { name: 'side', type: 'string', required: true }
    ]
  },
  {
    name: 'Click Element',
    description: 'Click on a page element by selector',
    inputs: [{ name: 'selector', type: 'string', required: true }]
  },
  {
    name: 'Enter Text',
    description: 'Enter text into an input field',
    inputs: [
      { name: 'selector', type: 'string', required: true },
      { name: 'text', type: 'string', required: true }
    ]
  },
  {
    name: 'Verify Element Text',
    description: 'Verify element contains expected text',
    inputs: [
      { name: 'selector', type: 'string', required: true },
      { name: 'expected_text', type: 'string', required: true }
    ]
  },
  {
    name: 'Wait for Element',
    description: 'Wait for element to be visible',
    inputs: [
      { name: 'selector', type: 'string', required: true },
      { name: 'timeout_ms', type: 'number', required: false }
    ]
  }
]

export default function StepDefinitionsPage(){
  const [customDefs, setCustomDefs] = React.useState([])
  const [name, setName] = React.useState('')
  const [description, setDescription] = React.useState('')
  const [openDialog, setOpenDialog] = React.useState(false)
  const [selectedStep, setSelectedStep] = React.useState(null)
  
  React.useEffect(()=> {
    axios.get('/api/tenants/1/step-definitions')
      .then(r => setCustomDefs(r.data))
      .catch(e => console.log('API not yet implemented'))
  }, [])
  
  const addPredefinedStep = (step) => {
    axios.post('/api/tenants/1/step-definitions', { 
      name: step.name, 
      description: step.description,
      input_schema: step.inputs 
    })
      .then(r => setCustomDefs(prev => [...prev, r.data]))
      .catch(e => {
        // If API not available, add to local state
        setCustomDefs(prev => [...prev, { id: Date.now(), ...step }])
      })
    setOpenDialog(false)
  }
  
  const createCustom = () => {
    if (!name.trim()) return
    axios.post('/api/tenants/1/step-definitions', { 
      name, 
      description,
      input_schema: []
    })
      .then(r => {
        setCustomDefs(prev => [...prev, r.data])
        setName('')
        setDescription('')
      })
      .catch(e => {
        // If API not available, add to local state
        setCustomDefs(prev => [...prev, { id: Date.now(), name, description, inputs: [] }])
        setName('')
        setDescription('')
      })
  }

  const allSteps = [
    ...PREDEFINED_STEPS.map((s, i) => ({ ...s, id: `predefined-${i}`, isPredefined: true })),
    ...customDefs.map(s => ({ ...s, id: s.id || `custom-${Date.now()}`, isPredefined: false }))
  ]

  return (
    <div style={{ padding: 20 }}>
      <h2>Step Definitions</h2>
      <p style={{ color: '#666' }}>Create reusable test steps that can be used in your test cases</p>
      
      {/* Custom Step Creation */}
      <Card style={{ marginBottom: 20, backgroundColor: '#f5f5f5' }}>
        <CardContent>
          <h3>Create Custom Step</h3>
          <div style={{ display: 'flex', gap: 8, marginBottom: 12 }}>
            <TextField 
              label="Step Name" 
              value={name} 
              onChange={e => setName(e.target.value)}
              placeholder="e.g., Login as Admin"
              size="small"
            />
            <TextField 
              label="Description" 
              value={description} 
              onChange={e => setDescription(e.target.value)}
              placeholder="What does this step do?"
              size="small"
              style={{ flex: 1 }}
            />
          </div>
          <Button 
            onClick={createCustom} 
            variant="contained" 
            color="primary"
            disabled={!name.trim()}
          >
            Add Custom Step
          </Button>
        </CardContent>
      </Card>

      {/* All Steps Display */}
      <div>
        <h3 style={{ marginBottom: 16 }}>Available Steps</h3>
        <Table>
          <TableHead>
            <TableRow style={{ backgroundColor: '#f0f0f0' }}>
              <TableCell><strong>Step Name</strong></TableCell>
              <TableCell><strong>Description</strong></TableCell>
              <TableCell><strong>Input Parameters</strong></TableCell>
              <TableCell><strong>Type</strong></TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {allSteps.map((step, idx) => (
              <TableRow key={idx} style={{ borderBottom: '1px solid #ddd' }}>
                <TableCell><strong>{step.name}</strong></TableCell>
                <TableCell>{step.description}</TableCell>
                <TableCell>
                  {step.inputs && step.inputs.length > 0 ? (
                    <ul style={{ margin: '4px 0', paddingLeft: 16 }}>
                      {step.inputs.map((inp, i) => (
                        <li key={i}>
                          {inp.name} ({inp.type}){inp.required ? ' *' : ''}
                        </li>
                      ))}
                    </ul>
                  ) : (
                    <span style={{ color: '#999' }}>None</span>
                  )}
                </TableCell>
                <TableCell>
                  {step.isPredefined ? (
                    <span style={{ 
                      backgroundColor: '#e3f2fd', 
                      padding: '4px 8px', 
                      borderRadius: 4,
                      fontSize: '0.85em',
                      color: '#1565c0'
                    }}>
                      Predefined
                    </span>
                  ) : (
                    <span style={{ 
                      backgroundColor: '#f3e5f5', 
                      padding: '4px 8px', 
                      borderRadius: 4,
                      fontSize: '0.85em',
                      color: '#6a1b9a'
                    }}>
                      Custom
                    </span>
                  )}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </div>

      {!customDefs.length && PREDEFINED_STEPS.length > 0 && (
        <div style={{ marginTop: 20, padding: 16, backgroundColor: '#f0f4c3', borderRadius: 4 }}>
          <p style={{ margin: 0 }}>
            💡 <strong>Tip:</strong> These predefined steps are ready to use in your test cases. 
            Create custom steps above for any specific workflows you need.
          </p>
        </div>
      )}
    </div>
  )
}
