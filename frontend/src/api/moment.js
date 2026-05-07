import request from './index'

export function getMomentList(page = 1, size = 12, babyId = null) {
  let url = `/moment/?page=${page}&size=${size}`
  if (babyId) url += `&babyId=${babyId}`
  return request.get(url)
}

export function getMomentDetail(id) {
  return request.get(`/moment/${id}`)
}

export function createMoment(data) {
  return request.post('/moment', data)
}

export function updateMoment(id, data) {
  return request.put(`/moment/${id}`, data)
}

export function deleteMoment(id) {
  return request.delete(`/moment/${id}`)
}

export function uploadVideo(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/moment/upload-video', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    timeout: 120000
  })
}

export function uploadCover(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/moment/upload-cover', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}

export { toggleLike } from './post'
