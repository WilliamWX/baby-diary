import request from './index'

export function getNotifications(page = 1, size = 20) {
  return request.get(`/notification?page=${page}&size=${size}`)
}

export function getUnreadCount() {
  return request.get('/notification/unread-count')
}

export function markRead(id) {
  return request.put(`/notification/${id}/read`)
}

export function markAllRead() {
  return request.put('/notification/read-all')
}
