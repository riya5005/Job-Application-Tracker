import axios from 'axios'

// backend runs on 8080 by default, change this if you deploy it somewhere else
const api = axios.create({
  baseURL: 'http://localhost:8080/api'
})

// slap the JWT onto every request if we have one - saves doing it manually everywhere
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

export default api
