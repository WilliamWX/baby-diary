import request from './index'

export function getPostList(page = 1, size = 10, category = '', sort = '', authorId = null) {
  let url = `/post/?page=${page}&size=${size}&category=${category}&sort=${sort}`
  if (authorId) url += `&authorId=${authorId}`
  return request.get(url)
}

export function getPostDetail(id) {
  return request.get(`/post/${id}`)
}

export function createPost(data) {
  return request.post('/post', data)
}

export function toggleLike(targetType, targetId) {
  return request.post('/interact/like', { targetType, targetId })
}

export function getComments(targetType, targetId) {
  return request.get(`/interact/comment/${targetType}/${targetId}`)
}

export function addComment(targetType, targetId, content, parentId = null) {
  return request.post('/interact/comment', { targetType, targetId, content, parentId })
}

export function updatePost(id, data) {
  return request.put(`/post/${id}`, data)
}

export function deletePost(id) {
  return request.delete(`/post/${id}`)
}

export function toggleBookmark(targetType, targetId) {
  return request.post('/interact/bookmark', { targetType, targetId })
}

export function getInteractStatus(targetType, targetId) {
  return request.get(`/interact/status/${targetType}/${targetId}`)
}

export function toggleFollow(userId) {
  return request.post(`/interact/follow/${userId}`)
}

export function getLikedItems() {
  return request.get('/interact/liked-items')
}

export function getBookmarkedItems() {
  return request.get('/interact/bookmarked-items')
}
