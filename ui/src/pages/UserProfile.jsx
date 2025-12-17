import React, { useState, useEffect } from 'react'
import {
    Box, Typography, Paper, Button, Divider,
    Grid, TextField, Alert, CircularProgress, IconButton
} from '@mui/material'
import ContentCopyIcon from '@mui/icons-material/ContentCopy'
import { useApi } from '../hooks/useApi'

export default function UserProfile() {
    const api = useApi()
    const [user, setUser] = useState(null)
    const [loading, setLoading] = useState(true)
    const [error, setError] = useState('')
    const [apiKey, setApiKey] = useState('')
    const [generating, setGenerating] = useState(false)

    useEffect(() => {
        loadProfile()
    }, [])

    const loadProfile = async () => {
        try {
            const data = await api.get('/api/system/users/me')
            setUser(data)
            setApiKey(data.apiKey || '')
        } catch (err) {
            setError('Failed to load profile')
            console.error(err)
        } finally {
            setLoading(false)
        }
    }

    const handleGenerateKey = async () => {
        if (!user) return
        setGenerating(true)
        try {
            // Backend returns plain string, so axios usually returns it in data.
            // If it returns a string, axios might try to parse JSON and fail if it's not valid JSON (though a simple string usually works).
            // However, Spring REST Controller returning String produces a text/plain response.
            // Axios response.data will be the string itself.
            const response = await api.post(`/api/system/users/${user.id}/api-key`)
            setApiKey(response)
        } catch (err) {
            console.error('API Key Generation Error:', err)
            // If the error is due to parsing, we might still have the key in response text if we access it differently,
            // but standard axios setup usually handles strings fine. 
            // The issue might be that `api.post` (from useApi wrapper) expects JSON.
            // Let's assume standard useApi behavior.
            setError('Failed to generate API key')
        } finally {
            setGenerating(false)
        }
    }

    const copyToClipboard = () => {
        navigator.clipboard.writeText(apiKey)
    }

    if (loading) return <Box p={3}><CircularProgress /></Box>

    return (
        <Box>
            <Typography variant="h4" gutterBottom>User Profile</Typography>

            {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}

            <Paper sx={{ p: 3, maxWidth: 800 }}>
                <Grid container spacing={3}>
                    <Grid item xs={12}>
                        <Typography variant="h6" gutterBottom>Account Details</Typography>
                        <Divider sx={{ mb: 2 }} />
                    </Grid>

                    <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" color="textSecondary">Display Name</Typography>
                        <Typography variant="body1">{user?.displayName || '-'}</Typography>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" color="textSecondary">Username</Typography>
                        <Typography variant="body1">{user?.username}</Typography>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" color="textSecondary">Email</Typography>
                        <Typography variant="body1">{user?.email}</Typography>
                    </Grid>

                    <Grid item xs={12} sm={6}>
                        <Typography variant="subtitle2" color="textSecondary">Role</Typography>
                        <Typography variant="body1">{user?.role}</Typography>
                    </Grid>

                    <Grid item xs={12}>
                        <Box sx={{ mt: 2 }}>
                            <Typography variant="h6" gutterBottom>API Access</Typography>
                            <Divider sx={{ mb: 2 }} />

                            <Typography variant="body2" color="textSecondary" paragraph>
                                Use this API Key to authenticate programmatic requests.
                                Include it in the <code>x-api-key</code> header.
                            </Typography>

                            <Box sx={{ display: 'flex', alignItems: 'center', gap: 2, mb: 2 }}>
                                <TextField
                                    fullWidth
                                    label="API Key"
                                    value={apiKey || 'No API Key generated'}
                                    InputProps={{
                                        readOnly: true,
                                        endAdornment: apiKey && (
                                            <IconButton onClick={copyToClipboard} edge="end">
                                                <ContentCopyIcon />
                                            </IconButton>
                                        )
                                    }}
                                />
                                <Button
                                    variant="contained"
                                    onClick={handleGenerateKey}
                                    disabled={generating}
                                    sx={{ minWidth: 150 }}
                                >
                                    {generating ? 'Generating...' : (apiKey ? 'Regenerate' : 'Generate Key')}
                                </Button>
                            </Box>
                        </Box>
                    </Grid>
                </Grid>
            </Paper>
        </Box>
    )
}
