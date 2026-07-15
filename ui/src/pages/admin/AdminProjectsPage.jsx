import React, { useState, useEffect } from 'react'
import {
    Card, CardContent, Button, Table, TableBody, TableCell,
    TableContainer, TableHead, TableRow, Paper, Dialog, DialogTitle,
    DialogContent, DialogActions, TextField, Select, MenuItem, InputLabel, FormControl, IconButton, Chip, Checkbox, ListItemText
} from '@mui/material'
import DeleteIcon from '@mui/icons-material/Delete'
import SettingsIcon from '@mui/icons-material/Settings'
import { useApi } from '../../hooks/useApi'

export default function AdminProjectsPage() {
    const [tenants, setTenants] = useState([])
    const [projects, setProjects] = useState([])
    const [usersMap, setUsersMap] = useState({}) // tenantId -> users list
    const [openDialog, setOpenDialog] = useState(false)
    const [openAccessDialog, setOpenAccessDialog] = useState(false)
    const [selectedProject, setSelectedProject] = useState(null)
    const [accessData, setAccessData] = useState({ projectManagerId: '', assignedUserIds: [] })
    const [formData, setFormData] = useState({ tenantId: '', name: '', description: '' })
    const api = useApi()

    useEffect(() => {
        fetchTenantsAndProjects()
    }, [])

    const fetchTenantsAndProjects = async () => {
        try {
            const tResponse = await api.get('/api/tenants')
            const tenantList = Array.isArray(tResponse) ? tResponse : (tResponse.value || [])
            setTenants(tenantList)

            // Fetch projects and users for all tenants
            const allProjects = []
            const userMapping = {}
            for (const t of tenantList) {
                try {
                    const pResponse = await api.get(`/api/tenants/${t.id}/projects`)
                    const pList = Array.isArray(pResponse) ? pResponse : []
                    pList.forEach(p => allProjects.push({ ...p, tenantName: t.name, tenantId: t.id }))
                    
                    const uResponse = await api.get(`/api/tenants/${t.id}/users`)
                    userMapping[t.id] = Array.isArray(uResponse) ? uResponse : []
                } catch (e) {
                    console.error(`Failed to load data for tenant ${t.id}`, e)
                }
            }
            setProjects(allProjects)
            setUsersMap(userMapping)
        } catch (error) {
            console.error('Error loading data:', error)
        }
    }

    const handleCreate = async () => {
        if (!formData.tenantId || !formData.name) return
        try {
            await api.post(`/api/tenants/${formData.tenantId}/projects`, {
                name: formData.name,
                description: formData.description
            })
            setOpenDialog(false)
            setFormData({ tenantId: '', name: '', description: '' })
            fetchTenantsAndProjects()
        } catch (error) {
            console.error('Error creating project:', error)
        }
    }

    const handleDelete = async (tenantId, projectId) => {
        if (!window.confirm("Are you sure you want to delete this project?")) return
        try {
            await api.delete(`/api/tenants/${tenantId}/projects/${projectId}`)
            fetchTenantsAndProjects()
        } catch (error) {
            console.error('Error deleting project:', error)
        }
    }

    const handleManageAccess = (project) => {
        setSelectedProject(project)
        setAccessData({
            projectManagerId: project.projectManagerId || '',
            assignedUserIds: project.assignedUserIds || []
        })
        setOpenAccessDialog(true)
    }

    const handleSaveAccess = async () => {
        try {
            if (accessData.projectManagerId !== selectedProject.projectManagerId) {
                // If it was cleared, send explicit null to backend to unset manager
                const val = accessData.projectManagerId ? `"${accessData.projectManagerId}"` : "null";
                await api.put(`/api/tenants/${selectedProject.tenantId}/projects/${selectedProject.id}/manager`, val, {
                    headers: { 'Content-Type': 'application/json' }
                })
            }
            // Update assigned users
            await api.put(`/api/tenants/${selectedProject.tenantId}/projects/${selectedProject.id}/assignments`, accessData.assignedUserIds)
            
            setOpenAccessDialog(false)
            fetchTenantsAndProjects()
        } catch (error) {
            console.error('Error updating access:', error)
        }
    }

    const getUserName = (tenantId, userId) => {
        const users = usersMap[tenantId] || []
        const user = users.find(u => u.id === userId)
        return user ? user.displayName || user.username : userId
    }

    return (
        <div style={{ padding: 20 }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: 20 }}>
                <h2>Global Project Management</h2>
                <Button variant="contained" color="primary" size="small" onClick={() => setOpenDialog(true)}>
                    New Project
                </Button>
            </div>

            <TableContainer component={Paper}>
                <Table>
                    <TableHead>
                        <TableRow style={{ backgroundColor: '#f5f5f5' }}>
                            <TableCell><strong>Tenant</strong></TableCell>
                            <TableCell><strong>Project Name</strong></TableCell>
                            <TableCell><strong>Description</strong></TableCell>
                            <TableCell><strong>Project Manager</strong></TableCell>
                            <TableCell><strong>Actions</strong></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {projects.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={5} align="center">No projects found</TableCell>
                            </TableRow>
                        ) : (
                            projects.map(p => (
                                <TableRow key={p.id}>
                                    <TableCell>{p.tenantName}</TableCell>
                                    <TableCell>{p.name}</TableCell>
                                    <TableCell>{p.description || '-'}</TableCell>
                                    <TableCell>
                                        {p.projectManagerId ? <Chip label={getUserName(p.tenantId, p.projectManagerId)} size="small" /> : '-'}
                                    </TableCell>
                                    <TableCell>
                                        <IconButton size="small" color="primary" onClick={() => handleManageAccess(p)} title="Manage Access">
                                            <SettingsIcon />
                                        </IconButton>
                                        <IconButton size="small" color="error" onClick={() => handleDelete(p.tenantId, p.id)} title="Delete Project">
                                            <DeleteIcon />
                                        </IconButton>
                                    </TableCell>
                                </TableRow>
                            ))
                        )}
                    </TableBody>
                </Table>
            </TableContainer>

            <Dialog open={openDialog} onClose={() => setOpenDialog(false)} maxWidth="sm" fullWidth>
                <DialogTitle>Create New Project</DialogTitle>
                <DialogContent>
                    <div style={{ display: 'flex', flexDirection: 'column', gap: 16, paddingTop: 10 }}>
                        <FormControl fullWidth>
                            <InputLabel>Tenant</InputLabel>
                            <Select
                                value={formData.tenantId}
                                label="Tenant"
                                onChange={e => setFormData({ ...formData, tenantId: e.target.value })}
                            >
                                {tenants.map(t => (
                                    <MenuItem key={t.id} value={t.id}>{t.name}</MenuItem>
                                ))}
                            </Select>
                        </FormControl>
                        <TextField
                            label="Project Name"
                            value={formData.name}
                            onChange={e => setFormData({ ...formData, name: e.target.value })}
                            fullWidth
                        />
                        <TextField
                            label="Description"
                            value={formData.description}
                            onChange={e => setFormData({ ...formData, description: e.target.value })}
                            fullWidth
                            multiline
                            rows={3}
                        />
                    </div>
                </DialogContent>
                <DialogActions>
                    <Button onClick={() => setOpenDialog(false)}>Cancel</Button>
                    <Button onClick={handleCreate} color="primary" variant="contained" disabled={!formData.tenantId || !formData.name}>
                        Create
                    </Button>
                </DialogActions>
            </Dialog>

            {selectedProject && (
                <Dialog open={openAccessDialog} onClose={() => setOpenAccessDialog(false)} maxWidth="sm" fullWidth>
                    <DialogTitle>Manage Access: {selectedProject.name}</DialogTitle>
                    <DialogContent>
                        <div style={{ display: 'flex', flexDirection: 'column', gap: 16, paddingTop: 10 }}>
                            <FormControl fullWidth>
                                <InputLabel>Project Manager</InputLabel>
                                <Select
                                    value={accessData.projectManagerId || ''}
                                    label="Project Manager"
                                    onChange={e => setAccessData({ ...accessData, projectManagerId: e.target.value })}
                                >
                                    <MenuItem value=""><em>None</em></MenuItem>
                                    {(usersMap[selectedProject.tenantId] || [])
                                        .filter(u => u.role === 'ROLE_PROJECT_MANAGER')
                                        .map(u => (
                                        <MenuItem key={u.id} value={u.id}>{u.displayName || u.username}</MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                            
                            <FormControl fullWidth>
                                <InputLabel>Assigned Users (Testers)</InputLabel>
                                <Select
                                    multiple
                                    value={accessData.assignedUserIds}
                                    label="Assigned Users (Testers)"
                                    onChange={e => {
                                        const val = e.target.value;
                                        setAccessData({ ...accessData, assignedUserIds: typeof val === 'string' ? val.split(',') : val });
                                    }}
                                    renderValue={(selected) => selected.map(id => getUserName(selectedProject.tenantId, id)).join(', ')}
                                >
                                    {(usersMap[selectedProject.tenantId] || []).map(u => (
                                        <MenuItem key={u.id} value={u.id}>
                                            <Checkbox checked={accessData.assignedUserIds.indexOf(u.id) > -1} />
                                            <ListItemText primary={u.displayName || u.username} />
                                        </MenuItem>
                                    ))}
                                </Select>
                            </FormControl>
                        </div>
                    </DialogContent>
                    <DialogActions>
                        <Button onClick={() => setOpenAccessDialog(false)}>Cancel</Button>
                        <Button onClick={handleSaveAccess} color="primary" variant="contained">
                            Save
                        </Button>
                    </DialogActions>
                </Dialog>
            )}
        </div>
    )
}
