import request from './index'

export function getGrowthRecords(babyId) {
  return request.get(`/growth/record/${babyId}`)
}

export function getGrowthChart(babyId) {
  return request.get(`/growth/chart/${babyId}`)
}

export function addGrowthRecord(data) {
  return request.post('/growth/record', data)
}

export function addFeedingRecord(data) {
  return request.post('/feeding', data)
}

export function getFeedingList(babyId, date) {
  return request.get(`/feeding/list/${babyId}?date=${date || ''}`)
}

export function addSleepRecord(data) {
  return request.post('/sleep', data)
}

export function getSleepList(babyId, date) {
  return request.get(`/sleep/list/${babyId}?date=${date || ''}`)
}

export function addMilestone(data) {
  return request.post('/milestone', data)
}

export function getMilestoneList(babyId) {
  return request.get(`/milestone/list/${babyId}`)
}
