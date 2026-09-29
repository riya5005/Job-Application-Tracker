import { useEffect, useState } from 'react'
import api from '../api/axios.js'

const STAGES = ['APPLIED', 'SHORTLISTED', 'INTERVIEW', 'HIRED', 'REJECTED']

export default function RecruiterDashboard() {
  const [jobs, setJobs] = useState([])
  const [selectedJobId, setSelectedJobId] = useState(null)
  const [applicants, setApplicants] = useState([])
  const [loadingApplicants, setLoadingApplicants] = useState(false)
  const [statusFilter, setStatusFilter] = useState('ALL')
  const [noteDrafts, setNoteDrafts] = useState({}) // applicationId -> unsaved note text

  useEffect(() => {
    loadJobs()
  }, [])

  async function loadJobs() {
    const res = await api.get('/jobs/mine')
    setJobs(res.data)
  }

  async function viewApplicants(jobId) {
    setSelectedJobId(jobId)
    setStatusFilter('ALL')
    setLoadingApplicants(true)
    try {
      const res = await api.get(`/applications/jobs/${jobId}`)
      setApplicants(res.data)
      // seed the note textareas with whatever's already saved
      const drafts = {}
      res.data.forEach((a) => { drafts[a.id] = a.recruiterNotes || '' })
      setNoteDrafts(drafts)
    } finally {
      setLoadingApplicants(false)
    }
  }

  // one helper for status / rating / notes since they all hit the same PATCH endpoint
  async function updateApplication(applicationId, changes) {
    const res = await api.patch(`/applications/${applicationId}/status`, changes)
    setApplicants((prev) => prev.map((a) => (a.id === applicationId ? res.data : a)))
    loadJobs() // keeps applicant counts fresh
  }

  async function closeJob(jobId) {
    if (!confirm('Close this job? It will stop accepting new applications.')) return
    await api.patch(`/jobs/${jobId}/close`)
    loadJobs()
  }

  // resume downloads need the auth header, so a plain <a href> won't work -
  // fetch as a blob and trigger the download manually
  async function downloadResume(app) {
    const res = await api.get(`/applications/${app.id}/resume`, { responseType: 'blob' })
    const url = window.URL.createObjectURL(new Blob([res.data]))
    const link = document.createElement('a')
    link.href = url
    link.download = app.resumeFileName || 'resume'
    document.body.appendChild(link)
    link.click()
    link.remove()
    window.URL.revokeObjectURL(url)
  }

  const visibleApplicants = statusFilter === 'ALL'
    ? applicants
    : applicants.filter((a) => a.status === statusFilter)

  return (
    <div>
      <h2>Your job postings</h2>

      <table className="data-table">
        <thead>
          <tr>
            <th>Title</th>
            <th>Location</th>
            <th>Applicants</th>
            <th>Status</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          {jobs.map((job) => (
            <tr key={job.id}>
              <td>{job.title}</td>
              <td>{job.location}</td>
              <td>{job.applicantCount}</td>
              <td>{job.active ? 'Open' : 'Closed'}</td>
              <td>
                <button onClick={() => viewApplicants(job.id)}>View applicants</button>
                {job.active && <button className="secondary-btn" onClick={() => closeJob(job.id)}>Close</button>}
              </td>
            </tr>
          ))}
          {jobs.length === 0 && (
            <tr><td colSpan={5}>You haven't posted any jobs yet.</td></tr>
          )}
        </tbody>
      </table>

      {selectedJobId && (
        <div className="applicants-section">
          <div className="applicants-header">
            <h3>Applicants</h3>
            <select value={statusFilter} onChange={(e) => setStatusFilter(e.target.value)}>
              <option value="ALL">All stages</option>
              {STAGES.map((s) => <option key={s} value={s}>{s}</option>)}
            </select>
          </div>

          {loadingApplicants && <p>Loading...</p>}
          {!loadingApplicants && visibleApplicants.length === 0 && <p>No applicants to show.</p>}

          {visibleApplicants.map((app) => (
            <div className="applicant-card" key={app.id}>
              <div className="applicant-top">
                <div>
                  <strong>{app.candidateName}</strong>
                  <div className="applied-date">
                    {app.candidateEmail} · applied {new Date(app.appliedAt).toLocaleDateString()}
                  </div>
                  <div className="resume-links">
                    {app.hasResumeFile && (
                      <button className="link-btn dark" onClick={() => downloadResume(app)}>
                        Download resume ({app.resumeFileName})
                      </button>
                    )}
                    {app.resumeLink && (
                      <a href={app.resumeLink} target="_blank" rel="noreferrer">Resume link</a>
                    )}
                    {!app.hasResumeFile && !app.resumeLink && <span className="hint-text">No resume provided</span>}
                  </div>
                </div>

                <div className="applicant-controls">
                  <select value={app.status} onChange={(e) => updateApplication(app.id, { status: e.target.value })}>
                    {STAGES.map((stage) => <option key={stage} value={stage}>{stage}</option>)}
                  </select>
                  <select
                    value={app.rating || ''}
                    onChange={(e) => e.target.value && updateApplication(app.id, { rating: Number(e.target.value) })}
                  >
                    <option value="">Rate...</option>
                    {[1, 2, 3, 4, 5].map((n) => <option key={n} value={n}>{'★'.repeat(n)}</option>)}
                  </select>
                </div>
              </div>

              <div className="notes-row">
                <textarea
                  rows={2}
                  placeholder="Feedback or notes (visible to the candidate)"
                  value={noteDrafts[app.id] ?? ''}
                  onChange={(e) => setNoteDrafts((prev) => ({ ...prev, [app.id]: e.target.value }))}
                />
                <button
                  disabled={(noteDrafts[app.id] ?? '') === (app.recruiterNotes || '')}
                  onClick={() => updateApplication(app.id, { recruiterNotes: noteDrafts[app.id] })}
                >
                  Save note
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}
