import { useEffect, useState } from 'react'
import {
  BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, CartesianGrid, Cell
} from 'recharts'
import api from '../api/axios.js'

const STAGE_COLORS = {
  APPLIED: '#888',
  SHORTLISTED: '#3b82f6',
  INTERVIEW: '#f59e0b',
  HIRED: '#22c55e',
  REJECTED: '#ef4444'
}

export default function Analytics() {
  const [data, setData] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    api.get('/analytics/recruiter')
      .then((res) => setData(res.data))
      .catch(() => setError('Could not load analytics'))
  }, [])

  if (error) return <p className="error-text">{error}</p>
  if (!data) return <p>Loading analytics...</p>

  const funnel = Object.entries(data.statusBreakdown).map(([stage, count]) => ({ stage, count }))
  const perJob = Object.entries(data.applicantsPerJob).map(([title, count]) => ({ title, count }))

  // rough hiring conversion: hired / total applicants
  const hired = data.statusBreakdown.HIRED || 0
  const conversion = data.totalApplicants ? ((hired / data.totalApplicants) * 100).toFixed(1) : '0.0'

  return (
    <div>
      <h2>Hiring analytics</h2>

      <div className="stat-grid">
        <div className="stat-card"><div className="stat-num">{data.totalJobs}</div><div>Jobs posted</div></div>
        <div className="stat-card"><div className="stat-num">{data.activeJobs}</div><div>Currently open</div></div>
        <div className="stat-card"><div className="stat-num">{data.totalApplicants}</div><div>Total applicants</div></div>
        <div className="stat-card"><div className="stat-num">{conversion}%</div><div>Applicant → hired</div></div>
        <div className="stat-card">
          <div className="stat-num">{data.averageRating ? data.averageRating.toFixed(1) : '–'}</div>
          <div>Avg. candidate rating</div>
        </div>
      </div>

      <div className="chart-box">
        <h3>Pipeline funnel</h3>
        <ResponsiveContainer width="100%" height={260}>
          <BarChart data={funnel}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="stage" />
            <YAxis allowDecimals={false} />
            <Tooltip />
            <Bar dataKey="count">
              {funnel.map((entry) => <Cell key={entry.stage} fill={STAGE_COLORS[entry.stage]} />)}
            </Bar>
          </BarChart>
        </ResponsiveContainer>
      </div>

      <div className="chart-box">
        <h3>Applicants per job</h3>
        {perJob.length === 0 ? <p className="hint-text">No applications yet.</p> : (
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={perJob}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="title" />
              <YAxis allowDecimals={false} />
              <Tooltip />
              <Bar dataKey="count" fill="#2563eb" />
            </BarChart>
          </ResponsiveContainer>
        )}
      </div>
    </div>
  )
}
