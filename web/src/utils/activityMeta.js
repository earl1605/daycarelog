import { UsersIcon, ClipboardIcon, HeartIcon, KeyIcon, MailIcon, ClockIcon } from '../components/icons'

export const ENTITY_TYPES = ['CHILD', 'ATTENDANCE', 'HEALTH_RECORD', 'USER', 'GUARDIAN']

const ENTITY_ICON = {
  CHILD: UsersIcon,
  ATTENDANCE: ClipboardIcon,
  HEALTH_RECORD: HeartIcon,
  USER: KeyIcon,
  GUARDIAN: MailIcon,
}

export function activityIcon(entityType) {
  return ENTITY_ICON[entityType] ?? ClockIcon
}

export function activityColor(action) {
  if (action?.endsWith('DELETED')) return 'bg-red-50 text-red-600'
  if (action?.endsWith('CREATED')) return 'bg-green-50 text-green-600'
  if (action?.endsWith('REACTIVATED')) return 'bg-green-50 text-green-600'
  if (action?.endsWith('DEACTIVATED')) return 'bg-gray-100 text-gray-500'
  return 'bg-blue-50 text-blue-600' // UPDATED / RECORDED / CHANGED
}

export function activityLabel(action) {
  if (!action) return ''
  return action.split('_').map(w => w[0] + w.slice(1).toLowerCase()).join(' ')
}

export function entityLabel(entityType) {
  if (!entityType) return ''
  return entityType.split('_').map(w => w[0] + w.slice(1).toLowerCase()).join(' ')
}
