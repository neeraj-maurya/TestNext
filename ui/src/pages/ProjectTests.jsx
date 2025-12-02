import React, { useState, useEffect } from 'react'
import { useParams, Link } from 'react-router-dom'
import axios from 'axios'
import {
    Card, CardContent, CardHeader, Table, TableBody, TableCell,
    TableContainer, TableHead, TableRow, Paper, Button, Box, Typography
} from '@mui/material'

export default function ProjectTests() {
    const { projectId } = useParams()
    const [tests, setTests] = useState([])
    const [loading, setLoading] = useState(true)

    useEffect(() => {
        axios.get(`/api/projects/${projectId}/tests`)
            .then(r => {
                setTests(r.data)
                setLoading(false)
            })
            .catch(e => {
                console.error('Failed to load tests', e)
                setLoading(false)
            })
    }, [projectId])

    const handleDelete = (testId) => {
        if (!window.confirm('Delete this test case?')) return
        axios.delete(`/api/tests/${testId}`)
            .then(() => setTests(prev => prev.filter(t => t.id !== testId)))
            .catch(e => alert('Failed to delete: ' + (e.response?.status === 403 ? 'Access Denied' : e.message)))
    }

    if (loading) return <div style={{ padding: 20 }}>Loading...</div>

    return (
        <div style={{ padding: 20 }}>
            <div style={{ marginBottom: 20 }}>
                <Link to="/projects" style={{ textDecoration: 'none', color: '#1976d2' }}>&larr; Back to Projects</Link>
            </div>

            <Card>
                <CardHeader
                    title="Project Test Cases"
                    subheader={`All test cases for Project #${projectId}`}
                />
                <CardContent>
                    <TableContainer component={Paper}>
                        <Table>
                            <TableHead>
                                <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                                    <TableCell><strong>ID</strong></TableCell>
                                    <TableCell><strong>Name</strong></TableCell>
                                    <TableCell><strong>Suite ID</strong></TableCell>
                                    <TableCell><strong>Actions</strong></TableCell>
                                </TableRow>
                            </TableHead>
                            <TableBody>
                                {tests.length === 0 ? (
                                    <TableRow>
                                        <TableCell colSpan={4} align="center" sx={{ py: 3, color: '#999' }}>
                                            No test cases found for this project.
                                        </TableCell>
                                    </TableRow>
                                ) : (
                                    tests.map(test => (
                                        <TableRow key={test.id}>
                                            <TableCell>{test.id}</TableCell>
                                            <TableCell>{test.name}</TableCell>
                                            <TableCell>
                                                <Link to={`/projects/${projectId}/suites/${test.suiteId}`} style={{ textDecoration: 'none', color: '#1976d2' }}>
                                                    {test.suiteId}
                                                </Link>
                                            </TableCell>
                                            <TableCell>
                                                <Button
                                                    size="small"
                                                    color="error"
                                                    onClick={() => handleDelete(test.id)}
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
