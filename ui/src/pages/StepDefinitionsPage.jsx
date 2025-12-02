import React from 'react'
import axios from 'axios'
import {
  Button,
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

export default function StepDefinitionsPage() {
  const [customDefs, setCustomDefs] = React.useState([])

  React.useEffect(() => {
    axios.get('/api/step-definitions')
      .then(r => setCustomDefs(r.data))
      .catch(e => console.log('API not yet implemented'))
  }, [])

  const allSteps = [
    ...PREDEFINED_STEPS.map((s, i) => ({ ...s, id: `predefined-${i}`, isPredefined: true })),
    ...customDefs.map(s => ({ ...s, id: s.id || `custom-${Date.now()}`, isPredefined: false }))
  ]

  return (
    <div style={{ padding: 20 }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div>
          <h2>Step Definitions</h2>
          <p style={{ color: '#666' }}>Reusable test steps for your test cases</p>
        </div>
        <Button
          onClick={() => {
            axios.post('/api/step-definitions/load-runtime')
              .then(r => setCustomDefs(r.data))
              .catch(e => console.error('Failed to load runtime steps', e))
          }}
          variant="outlined"
          color="secondary"
        >
          Load Runtime Steps
        </Button>
      </div>

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
    </div>
  )
}
