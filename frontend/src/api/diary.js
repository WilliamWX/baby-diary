import request from './index'

export function createDiary(data) {
  return request.post('/diary', data)
}

export function getDiaryList(page = 1, size = 10) {
  return request.get(`/diary/?page=${page}&size=${size}`)
}

export function getDiaryDetail(id) {
  return request.get(`/diary/${id}`)
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
