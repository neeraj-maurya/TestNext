import React, { useState, useEffect } from 'react'
import {
  Card, CardContent, CardHeader, Table, TableBody, TableCell,
  TableContainer, TableHead, TableRow, Paper, Chip, MenuItem, Select,
  FormControl, InputLabel, Box, Typography
} from '@mui/material'
import { useApi } from '../../hooks/useApi'

export default function AdminRoleManagement() {
  const [users, setUsers] = useState([])
  const [tenants, setTenants] = useState([])
  const [loading, setLoading] = useState(true)
  const api = useApi()

  useEffect(() => {
    loadData()
  }, [])

  const loadData = async () => {
    setLoading(true)
    try {
      const [usersData, tenantsData] = await Promise.all([
        api.get('/api/system/users'),
        api.get('/api/tenants')
      ])

      const userList = Array.isArray(usersData) ? usersData : (usersData?.value || [])
      const tenantList = Array.isArray(tenantsData) ? tenantsData : (tenantsData?.value || [])

      setUsers(userList)
      setTenants(tenantList)
    } catch (error) {
      console.error('Error loading data:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleRoleChange = async (userId, newRole) => {
    try {
      // We need to fetch the full user object first or just patch the role if API supported PATCH
      // Since our API is PUT /api/system/users/{id}, we need to send the full object or at least what's required
      const user = users.find(u => u.id === userId)
      if (!user) return

      const updatedUser = { ...user, role: newRole }

      // Optimistic update
      setUsers(prev => prev.map(u => u.id === userId ? updatedUser : u))

      await api.put(`/api/system/users/${userId}`, updatedUser)
    } catch (error) {
      console.error('Error updating role:', error)
      alert('Failed to update role')
      loadData() // Revert on error
    }
  }

  const getManagedTenant = (userId) => {
    const tenant = tenants.find(t => t.testManagerId === userId)
    return tenant ? tenant.name : '-'
  }

  if (loading) return <Box p={3}>Loading...</Box>

  return (
    <Card>
      <CardHeader
        title="Role Management"
        subheader="Manage user roles and view tenant assignments"
      />
      <CardContent>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow sx={{ backgroundColor: '#f5f5f5' }}>
                <TableCell><strong>User</strong></TableCell>
                <TableCell><strong>Current Role</strong></TableCell>
                <TableCell><strong>Managed Tenant</strong></TableCell>
                <TableCell><strong>Actions</strong></TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {users.map(user => (
                <TableRow key={user.id}>
                  <TableCell>
                    <Box>
                      <Typography variant="subtitle2">{user.display_name || user.displayName}</Typography>
                      <Typography variant="caption" color="textSecondary">{user.username}</Typography>
                    </Box>
                  </TableCell>
                  <TableCell>
                    <Chip
                      label={user.role}
                      color={
                        user.role === 'ROLE_SYSTEM_ADMIN' ? 'primary' :
                          user.role === 'ROLE_TEST_MANAGER' ? 'secondary' : 'default'
                      }
                      size="small"
                    />
                  </TableCell>
                  <TableCell>
                    {user.role === 'ROLE_TEST_MANAGER' ? (
                      <Chip label={getManagedTenant(user.id)} variant="outlined" size="small" />
                    ) : (
                      <Typography variant="caption" color="textSecondary">N/A</Typography>
                    )}
                  </TableCell>
                  <TableCell>
                    <FormControl size="small" sx={{ minWidth: 150 }}>
                      <Select
                        value={user.role || 'ROLE_VIEWER'}
                        onChange={(e) => handleRoleChange(user.id, e.target.value)}
                        variant="standard"
                      >
                        <MenuItem value="ROLE_SYSTEM_ADMIN">System Admin</MenuItem>
                        <MenuItem value="ROLE_TEST_MANAGER">Test Manager</MenuItem>
                        <MenuItem value="ROLE_TEST_ENGINEER">Test Engineer</MenuItem>
                        <MenuItem value="ROLE_VIEWER">Viewer</MenuItem>
                      </Select>
                    </FormControl>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </CardContent>
    </Card>
  )
}
