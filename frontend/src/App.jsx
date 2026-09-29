import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { AuthProvider, useAuth } from './context/AuthContext.jsx'
import Navbar from './components/Navbar.jsx'
import Login from './pages/Login.jsx'
import Register from './pages/Register.jsx'
import JobList from './pages/JobList.jsx'
import PostJob from './pages/PostJob.jsx'
import RecruiterDashboard from './pages/RecruiterDashboard.jsx'
import CandidateDashboard from './pages/CandidateDashboard.jsx'
import Analytics from './pages/Analytics.jsx'

// quick gate for pages that need a logged in user - redirects to login otherwise
function ProtectedRoute({ children, roleRequired }) {
  const { user } = useAuth()
  if (!user) return <Navigate to="/login" replace />
  if (roleRequired && user.role !== roleRequired) return <Navigate to="/" replace />
  return children
}

function AppRoutes() {
  return (
    <BrowserRouter>
      <Navbar />
      <div className="container">
        <Routes>
          <Route path="/" element={<JobList />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route
            path="/post-job"
            element={
              <ProtectedRoute roleRequired="RECRUITER">
                <PostJob />
              </ProtectedRoute>
            }
          />
          <Route
            path="/recruiter-dashboard"
            element={
              <ProtectedRoute roleRequired="RECRUITER">
                <RecruiterDashboard />
              </ProtectedRoute>
            }
          />
          <Route
            path="/analytics"
            element={
              <ProtectedRoute roleRequired="RECRUITER">
                <Analytics />
              </ProtectedRoute>
            }
          />
          <Route
            path="/my-applications"
            element={
              <ProtectedRoute roleRequired="CANDIDATE">
                <CandidateDashboard />
              </ProtectedRoute>
            }
          />
        </Routes>
      </div>
    </BrowserRouter>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <AppRoutes />
    </AuthProvider>
  )
}
