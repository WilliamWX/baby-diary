import request from './index'

export function getFriendList() {
  return request.get('/friend/list')
}

export function addFriend(id) {
  return request.post('/friend/add/' + id)
}

export function removeFriend(id) {
  return request.delete('/friend/remove/' + id)
}

export function searchUsers(keyword) {
  return request.get('/friend/search', { params: { keyword } })
}

export function getFriendRequests() {
  return request.get('/friend/requests')
}

export function acceptFriend(requestId) {
  return request.post('/friend/accept/' + requestId)
}

export function rejectFriend(requestId) {
  return request.delete('/friend/reject/' + requestId)
}
