import request from './index'

export function askDoctor(data) {
  return request.post('/ai-doctor/ask', data)
}

export function getHistory(page = 1, size = 20) {
  return request.get(`/ai-doctor/history?page=${page}&size=${size}`)
}
