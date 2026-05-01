import request from './index'

export function askDoctor(data) {
  return request.post('/ai-doctor/ask', data)
}
