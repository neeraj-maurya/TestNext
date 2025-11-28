import React from 'react'
import axios from 'axios'
import { 
  Button, 
  TextField,
  Card,
  CardContent,
  CardActions,
  Grid,
  Chip,
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

export default function ProjectsPage(){
  const [tenants, setTenants] = React.useState([])
  const [projects, setProjects] = React.useState({})
  const [tenantName, setTenantName] = React.useState('')
  const [projectName, setProjectName] = React.useState('')
  const [selectedTenant, setSelectedTenant] = React.useState(null)
  const [openProjectDialog, setOpenProjectDialog] = React.useState(false)
  const [error, setError] = React.useState(null)
  
  React.useEffect(() => {
    axios.get('/api/tenants')
      .then(r => {
        console.log('Tenants loaded:', r.data)
        setTenants(r.data || [])
        const newProjects = {}
        ;(r.data || []).forEach(t => {
          axios.get(`/api/projects?tenantId=${t.id}`)
            .then(pr => {
              console.log('Projects for tenant', t.id, ':', pr.data)
              newProjects[t.id] = pr.data || []
              setProjects({...newProjects})
            })
            .catch(e => {
              console.log('Error loading projects for tenant', t.id)
              newProjects[t.id] = []
              setProjects({...newProjects})
            })
        })
      })
      .catch(e => {
        console.error('Error loading tenants:', e)
        setError('Failed to load tenants')
      })
  }, [])

  const createTenant = () => {
    if (!tenantName.trim()) return
    axios.post('/api/tenants', { name: tenantName })
      .then(r => {
        setTenants(prev => [...prev, r.data])
        setProjects(prev => ({...prev, [r.data.id]: []}))
        setTenantName('')
      })
      .catch(e => console.log('Error creating tenant', e))
  }

  const createProject = () => {
    if (!projectName.trim() || !selectedTenant) return
    axios.post('/api/projects', { 
      name: projectName,
      tenantId: selectedTenant,
      description: ''
    })
      .then(r => {
        setProjects(prev => ({
          ...prev, 
          [selectedTenant]: [...(prev[selectedTenant] || []), r.data]
        }))
        setProjectName('')
        setOpenProjectDialog(false)
      })
      .catch(e => {
        // Mock: add locally if API not available
        const newProject = {
          id: Date.now(),
          name: projectName,
          tenantId: selectedTenant,
          description: ''
        }
        setProjects(prev => ({
          ...prev,
          [selectedTenant]: [...(prev[selectedTenant] || []), newProject]
        }))
        setProjectName('')
        setOpenProjectDialog(false)
      })
  }

  return (
    <div style={{ padding: 20 }}>
      {error && <div style={{ color: 'red', marginBottom: 10 }}>{error}</div>}
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: 20 }}>
        <div>
          <h2 style={{ margin: 0 }}>Tenants & Projects</h2>
          <p style={{ color: '#666', margin: '4px 0 0 0' }}>Manage test automation tenants and their projects</p>
        </div>
      </div>

      {/* Create Tenant Card */}
      <Card style={{ marginBottom: 20, backgroundColor: '#fafafa' }}>
        <CardContent>
          <h3 style={{ marginTop: 0 }}>Add New Tenant</h3>
          <div style={{ display: 'flex', gap: 8, alignItems: 'center' }}>
            <TextField 
              label="Tenant Name" 
              value={tenantName} 
              onChange={e => setTenantName(e.target.value)}
              placeholder="e.g., Acme Corp"
              size="small"
              style={{ flex: 1, maxWidth: 300 }}
            />
            <Button 
              onClick={createTenant} 
              variant="contained" 
              color="primary"
              disabled={!tenantName.trim()}
            >
              Create Tenant
            </Button>
          </div>
        </CardContent>
      </Card>

      {/* Tenants and Projects Grid */}
      {(!tenants || tenants.length === 0) ? (
        <div style={{ padding: 20, textAlign: 'center', backgroundColor: '#f5f5f5', borderRadius: 4 }}>
          <p style={{ color: '#666' }}>No tenants created yet. Create one above to get started.</p>
        </div>
      ) : (
        <Grid container spacing={2}>
          {tenants.map(tenant => (
            <Grid item xs={12} key={tenant.id}>
              <Card>
                <CardContent>
                  <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'start', marginBottom: 12 }}>
                    <div>
                      <h3 style={{ margin: '0 0 4px 0' }}>{tenant.name}</h3>
                      <p style={{ margin: 0, fontSize: '0.85em', color: '#666' }}>Schema: {tenant.schemaName}</p>
                    </div>
                    <Chip 
                      label={`${(projects[tenant.id] || []).length} projects`}
                      color="primary"
                      variant="outlined"
                    />
                  </div>

                  {/* Projects Table */}
                  {(projects[tenant.id] || []).length > 0 ? (
                    <Table size="small">
                      <TableHead>
                        <TableRow style={{ backgroundColor: '#f0f0f0' }}>
                          <TableCell><strong>Project Name</strong></TableCell>
                          <TableCell><strong>Description</strong></TableCell>
                        </TableRow>
                      </TableHead>
                      <TableBody>
                        {(projects[tenant.id] || []).map(proj => (
                          <TableRow key={proj.id}>
                            <TableCell>{proj.name}</TableCell>
                            <TableCell>{proj.description || '-'}</TableCell>
                          </TableRow>
                        ))}
                      </TableBody>
                    </Table>
                  ) : (
                    <p style={{ color: '#999', fontSize: '0.9em', margin: 0 }}>No projects yet</p>
                  )}
                </CardContent>
                <CardActions>
                  <Button 
                    size="small" 
                    color="primary"
                    onClick={() => {
                      setSelectedTenant(tenant.id)
                      setOpenProjectDialog(true)
                    }}
                  >
                    Add Project
                  </Button>
                </CardActions>
              </Card>
            </Grid>
          ))}
        </Grid>
      )}

      {/* Add Project Dialog */}
      <Dialog open={openProjectDialog} onClose={() => setOpenProjectDialog(false)} maxWidth="sm" fullWidth>
        <DialogTitle>Add New Project</DialogTitle>
        <DialogContent>
          <div style={{ paddingTop: 12 }}>
            <TextField 
              label="Project Name" 
              value={projectName} 
              onChange={e => setProjectName(e.target.value)}
              fullWidth
              autoFocus
              placeholder="e.g., Trading Platform Tests"
            />
          </div>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenProjectDialog(false)}>Cancel</Button>
          <Button 
            onClick={createProject} 
            variant="contained" 
            color="primary"
            disabled={!projectName.trim()}
          >
            Create
          </Button>
        </DialogActions>
      </Dialog>
    </div>
  )
}
