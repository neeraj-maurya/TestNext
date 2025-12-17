import React, { useState, useEffect } from 'react'
import {
    Card, CardContent, Button, Table, TableBody, TableCell,
    TableContainer, TableHead, TableRow, Paper, Dialog, DialogTitle,
    DialogContent, DialogActions, TextField, Select, MenuItem, InputLabel, FormControl
} from '@mui/material'
import { useApi } from '../../hooks/useApi'

export default function AdminProjectsPage() {
    const [tenants, setTenants] = useState([])
    const [projects, setProjects] = useState([])
    const [openDialog, setOpenDialog] = useState(false)
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

            // Fetch projects for all tenants
            const allProjects = []
            for (const t of tenantList) {
                try {
                    const pResponse = await api.get(`/api/tenants/${t.id}/projects`)
                    const pList = Array.isArray(pResponse) ? pResponse : []
                    pList.forEach(p => allProjects.push({ ...p, tenantName: t.name, tenantId: t.id }))
                } catch (e) {
                    console.error(`Failed to load projects for tenant ${t.id}`, e)
                }
            }
            setProjects(allProjects)
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
                            <TableCell><strong>ID</strong></TableCell>
                        </TableRow>
                    </TableHead>
                    <TableBody>
                        {projects.length === 0 ? (
                            <TableRow>
                                <TableCell colSpan={4} align="center">No projects found</TableCell>
                            </TableRow>
                        ) : (
                            projects.map(p => (
                                <TableRow key={p.id}>
                                    <TableCell>{p.tenantName}</TableCell>
                                    <TableCell>{p.name}</TableCell>
                                    <TableCell>{p.description || '-'}</TableCell>
                                    <TableCell>{p.id}</TableCell>
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
        </div>
    )
}
