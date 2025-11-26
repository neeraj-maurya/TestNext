import React from 'react';
import { TextField, Select, MenuItem, Button } from '@mui/material';

export default function DynamicForm({ schema, onSubmit, initialValues = {} }) {
  const [state, setState] = React.useState(initialValues);

  const renderField = (field) => {
    const value = state[field.name] || '';
    switch (field.type) {
      case 'string':
        return (<TextField key={field.name} label={field.label || field.name} value={value} onChange={e => setState({ ...state, [field.name]: e.target.value })} fullWidth margin="normal" />);
      case 'json':
        return (<TextField key={field.name} label={field.label || field.name} value={value} onChange={e => setState({ ...state, [field.name]: e.target.value })} multiline rows={4} fullWidth margin="normal" />);
      case 'select':
        return (
          <Select key={field.name} value={value} onChange={e => setState({ ...state, [field.name]: e.target.value })} fullWidth>
            {(field.enum || []).map(opt => <MenuItem key={opt} value={opt}>{opt}</MenuItem>)}
          </Select>
        );
      default:
        return (<TextField key={field.name} label={field.label || field.name} value={value} onChange={e => setState({ ...state, [field.name]: e.target.value })} fullWidth margin="normal" />);
    }
  };

  return (
    <form onSubmit={e => { e.preventDefault(); onSubmit(state); }}>
      {(schema.fields || []).map(renderField)}
      <Button type="submit" variant="contained">Submit</Button>
    </form>
  );
}
