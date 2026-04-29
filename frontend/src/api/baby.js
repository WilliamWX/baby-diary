import request from './index'

export function createBaby(data) {
  return request.post('/baby', data)
}

export function getBabyList() {
  return request.get('/baby')
}

export function updateBaby(id, data) {
  return request.put(`/baby/${id}`, data)
}
