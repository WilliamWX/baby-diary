import request from './index'

export function createDiary(data) {
  return request.post('/diary', data)
}

export function getDiaryList(page = 1, size = 10, babyId = null) {
  let url = `/diary/?page=${page}&size=${size}`
  if (babyId) url += `&babyId=${babyId}`
  return request.get(url)
}

export function getDiaryDetail(id) {
  return request.get(`/diary/${id}`)
}

export function updateDiary(id, data) {
  return request.put(`/diary/${id}`, data)
}

export function deleteDiary(id) {
  return request.delete(`/diary/${id}`)
}

export function uploadImage(file) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post('/diary/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' }
  })
}
