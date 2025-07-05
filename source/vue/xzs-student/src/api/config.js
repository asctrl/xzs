import axios from 'axios'

export function getAllConfigs() {
  return axios.get('/api/config/all').then(res => res.data)
}