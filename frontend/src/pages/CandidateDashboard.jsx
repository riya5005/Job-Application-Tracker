import { useEffect, useState } from 'react'
import api from '../api/axios.js'

// color coding so the pipeline stage is easy to scan at a glance
const STAGE_COLORS = {
  APPLIED: '#888',
  SHORTLISTED: '#3b82f6',
  INTERVIEW: '#f59e0b',
  HIRED: '#22c55e',
  REJECTED: '#ef4444'
}

export default function CandidateDashboard() {
  const [applications, setApplications] = useState([])
  const [loading, setLoading] = useState(true)
  const [uploadingId, setUploadingId] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/applications/mine')
      .then((res) => setApplications(res.data))
      .finally(() => setLoading(false))
  }, [])

  async function handleUpload(applicationId, file) {
    if (!file) return
    setError('')
    setUploadingId(applicationId)
    try {
      const formData = new FormData()
      formData.append('file', file)
      const res = await api.post(`/applications/${applicationId}/resume`, formData)
      setApplications((prev) => prev.map((a) => (a.id === applicationId ? res.data : a)))
    } catch (err) {
      setError(String(err.response?.data || 'Upload failed (PDF/DOC/DOCX under 5MB only)'))
    } finally {
      setUploadingId(null)
    }
  }

  if (loading) return <p>Loading your applications...</p>

  return (
    <div>
      <h2>My applications</h2>
      {error && <p className="error-text">{error}</p>}

      {applications.length === 0 && <p>You haven't applied to anything yet, go browse open jobs!</p>}

      <div className="applications-list">
        {applications.map((app) => (
          <div className="application-row" key={app.id}>
            <div>
              <strong>{app.jobTitle}</strong>
              <div className="applied-date">Applied {new Date(app.appliedAt).toLocaleDateString()}</div>
            </div>

            <span className="status-pill" style={{ backgroundColor: STAGE_COLORS[app.status] }}>
              {app.status}
            </span>

            <div className="resume-upload">
              {app.hasResumeFile ? (
                <span className="hint-text">Resume: {app.resumeFileName}</span>
              ) : (
                <span className="hint-text">No resume uploaded yet</span>
              )}
              <label className="file-label">
                {uploadingId === app.id ? 'Uploading...' : app.hasResumeFile ? 'Replace' : 'Upload resume'}
                <input
                  type="file"
                  accept=".pdf,.doc,.docx"
                  hidden
                  disabled={uploadingId === app.id}
                  onChange={(e) => handleUpload(app.id, e.target.files[0])}
                />
              </label>
            </div>

            {app.recruiterNotes && (
              <p className="recruiter-note">Note from recruiter: {app.recruiterNotes}</p>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
