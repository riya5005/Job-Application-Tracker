import { useEffect, useState } from 'react'
import api from '../api/axios.js'
import { useAuth } from '../context/AuthContext.jsx'

const PAGE_SIZE = 9

export default function JobList() {
  const [jobs, setJobs] = useState([])
  const [search, setSearch] = useState('')
  const [location, setLocation] = useState('')
  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalResults, setTotalResults] = useState(0)
  const [loading, setLoading] = useState(true)
  const [appliedIds, setAppliedIds] = useState(new Set())
  const [message, setMessage] = useState('')

  const { user } = useAuth()

  useEffect(() => {
    if (user?.role === 'CANDIDATE') loadMyApplications()
  }, [])

  // reload whenever the page changes; search/location changes go through handleSearch
  useEffect(() => {
    loadJobs(search, location, page)
  }, [page])

  async function loadJobs(searchTerm, loc, pageNum) {
    setLoading(true)
    try {
      const res = await api.get('/jobs', {
        params: { search: searchTerm, location: loc, page: pageNum, size: PAGE_SIZE }
      })
      setJobs(res.data.jobs)
      setTotalPages(res.data.totalPages)
      setTotalResults(res.data.totalResults)
    } catch (err) {
      console.error('failed to load jobs', err)
    } finally {
      setLoading(false)
    }
  }

  async function loadMyApplications() {
    try {
      const res = await api.get('/applications/mine')
      setAppliedIds(new Set(res.data.map((a) => a.jobId)))
    } catch (err) {
      // not critical, the "applied" state just won't show
    }
  }

  function handleSearch(e) {
    e.preventDefault()
    if (page === 0) loadJobs(search, location, 0)
    else setPage(0) // triggers the effect above
  }

  function clearFilters() {
    setSearch('')
    setLocation('')
    if (page === 0) loadJobs('', '', 0)
    else setPage(0)
  }

  async function handleApply(jobId) {
    setMessage('')
    try {
      await api.post(`/applications/jobs/${jobId}`, {})
      setAppliedIds((prev) => new Set(prev).add(jobId))
      setMessage('Applied! Go to "My applications" to upload your resume and track progress.')
    } catch (err) {
      setMessage(String(err.response?.data || 'Could not apply to this job'))
    }
  }

  return (
    <div>
      <h2>Open positions</h2>

      <form onSubmit={handleSearch} className="search-bar">
        <input
          placeholder="Title or skill (e.g. React)"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
        <input
          placeholder="Location"
          value={location}
          onChange={(e) => setLocation(e.target.value)}
        />
        <button type="submit">Search</button>
        <button type="button" className="secondary-btn" onClick={clearFilters}>Clear</button>
      </form>

      {message && <p className="info-text">{message}</p>}
      {loading && <p>Loading jobs...</p>}
      {!loading && <p className="hint-text">{totalResults} job(s) found</p>}

      <div className="job-grid">
        {jobs.map((job) => (
          <div className="job-card" key={job.id}>
            <h3>{job.title}</h3>
            <p className="job-meta">{job.location} · posted by {job.postedByName}</p>
            {job.skillsRequired && (
              <div className="skill-tags">
                {job.skillsRequired.split(',').map((s) => (
                  <span className="skill-tag" key={s}>{s.trim()}</span>
                ))}
              </div>
            )}
            <p>{job.description}</p>

            {user?.role === 'CANDIDATE' && (
              appliedIds.has(job.id) ? (
                <button disabled>Already applied</button>
              ) : (
                <button onClick={() => handleApply(job.id)}>Apply</button>
              )
            )}

            {!user && <p className="hint-text">Log in as a candidate to apply</p>}
          </div>
        ))}
      </div>

      {totalPages > 1 && (
        <div className="pagination">
          <button disabled={page === 0} onClick={() => setPage(page - 1)}>Previous</button>
          <span>Page {page + 1} of {totalPages}</span>
          <button disabled={page >= totalPages - 1} onClick={() => setPage(page + 1)}>Next</button>
        </div>
      )}
    </div>
  )
}
