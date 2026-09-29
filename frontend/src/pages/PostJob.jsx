import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import api from '../api/axios.js'

export default function PostJob() {
  const [form, setForm] = useState({ title: '', location: '', skillsRequired: '', description: '' })
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(false)
  const navigate = useNavigate()

  function update(field, value) {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    setLoading(true)
    try {
      await api.post('/jobs', form)
      navigate('/recruiter-dashboard')
    } catch (err) {
      setError(err.response?.data || 'Could not post the job')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="form-page">
      <h2>Post a new job</h2>
      <form onSubmit={handleSubmit}>
        <label>Job title</label>
        <input value={form.title} onChange={(e) => update('title', e.target.value)} required />

        <label>Location</label>
        <input value={form.location} onChange={(e) => update('location', e.target.value)} required />

        <label>Skills required (comma separated)</label>
        <input value={form.skillsRequired} onChange={(e) => update('skillsRequired', e.target.value)} placeholder="React, Spring Boot, SQL" />

        <label>Description</label>
        <textarea rows={6} value={form.description} onChange={(e) => update('description', e.target.value)} />

        {error && <p className="error-text">{String(error)}</p>}

        <button type="submit" disabled={loading}>
          {loading ? 'Posting...' : 'Post job'}
        </button>
      </form>
    </div>
  )
}
