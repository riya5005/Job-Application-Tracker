import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext.jsx'

export default function Navbar() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()

  function handleLogout() {
    logout()
    navigate('/')
  }

  return (
    <nav className="navbar">
      <Link to="/" className="brand">JobTracker</Link>

      <div className="nav-links">
        {!user && (
          <>
            <Link to="/login">Login</Link>
            <Link to="/register">Sign up</Link>
          </>
        )}

        {user && user.role === 'RECRUITER' && (
          <>
            <Link to="/post-job">Post a job</Link>
            <Link to="/recruiter-dashboard">Dashboard</Link>
            <Link to="/analytics">Analytics</Link>
          </>
        )}

        {user && user.role === 'CANDIDATE' && (
          <Link to="/my-applications">My applications</Link>
        )}

        {user && (
          <>
            <span className="user-badge">{user.fullName} ({user.role.toLowerCase()})</span>
            <button onClick={handleLogout} className="link-btn">Logout</button>
          </>
        )}
      </div>
    </nav>
  )
}
