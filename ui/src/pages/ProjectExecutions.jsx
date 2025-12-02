import React, { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import axios from 'axios'
import {
    Card, CardContent, CardHeader, Table, TableBody, TableCell,
    TableContainer, TableHead, TableRow, Paper, Chip, Box, Typography, Button
} from '@mui/material'

export default function ProjectExecutions() {
    const { projectId } = useParams()
    const [executions, setExecutions] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        axios.get(`/api/projects/${projectId}/executions`)
            .then(r => {
                setExecutions(r.data)
                setLoading(false)
            })
            .catch(e => {
                console.error('Failed to load executions', e)
                setLoading(false)
            })
    }, [projectId])

    if (loading) return <div style={{ padding: 20 }}>Loading...</div>

    return (
        <div style={{ padding: 20 }}>
            <div style={{ marginBottom: 20 }}>
                <Link to="/projects" style={{ textDecoration: 'none', color: '#1976d2' }}>&larr; Back to Projects</Link>
            </div>

            <Card>
                <CardHeader
                    title="Project Executions"
                    subheader={`History of test executions for Project #${projectId}`}
                />
                <CardContent>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                    <TableCell><strong>ID</strong></TableCell>
                                    <TableCell><strong>Test Suite</strong></TableCell>
                                    <TableCell><strong>Test Case</strong></TableCell>
                                    <TableCell><strong>Started</strong></TableCell>
                                    <TableCell><strong>Status</strong></TableCell>
                                    <TableCell><strong>Result</strong></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {executions.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={6} align="center" sx={{ py: 3, color: '#999' }}>
                                            No executions found for this project.
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    executions.map(execution => (
                                        <TableRow key={execution.id}>
                                            <TableCell>
                                                <Link to={`/execution?id=${execution.id}`} style={{ textDecoration: 'none', color: '#1976d2' }}>
                                                    {execution.id.substring(0, 8)}...
                                                </Link>
                                            </TableCell>
                                            <TableCell>{execution.suiteName || '-'}</TableCell>
                                            <TableCell>{execution.testName || '-'}</TableCell>
                                            <TableCell>{execution.startedAt ? new Date(execution.startedAt).toLocaleString() : '-'}</TableCell>
                                            <TableCell>
                                                <Chip
                                                    label={execution.status}
                                                    size="small"
                                                    color={
                                                        execution.status === 'FINISHED' ? 'success' :
                                                            execution.status === 'FAILED' ? 'error' :
                                                                'warning'
                                                    }
                                                />
                                            </TableCell>
                                            <TableCell>
                                                {/* Result summary could go here if available, or just status */}
                                                {execution.status === 'FINISHED' ? 'Pass' : execution.status === 'FAILED' ? 'Fail' : '-'}
                                            </TableCell>
                                            <TableCell>
                                                <Button
                                                    size="small"
                                                    color="error"
                                                    onClick={() => {
                                                        if (window.confirm('Delete this execution?')) {
                                                            axios.delete(`/api/executions/${execution.id}`)
                                                                .then(() => setExecutions(prev => prev.filter(e => e.id !== execution.id)))
                                                                .catch(err => alert('Failed to delete: ' + (err.response?.status === 403 ? 'Access Denied' : err.message)))
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
                    </TableContainer>
                </CardContent>
            </Card>
        </div>
    )
}
