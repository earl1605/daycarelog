import { getPasswordStrength } from '../utils/passwordStrength'

export default function PasswordStrengthMeter({ password }) {
  if (!password) return null
  const { score, label, color } = getPasswordStrength(password)
  const segments = 5

  return (
    <div className="mt-1.5">
      <div className="flex gap-1">
        {Array.from({ length: segments }, (_, i) => (
          <div key={i} className="h-1 flex-1 rounded-full bg-gray-100 overflow-hidden">
            <div
              className="h-full rounded-full transition-all duration-200"
              style={{ width: i < score ? '100%' : '0%', backgroundColor: color }}
            />
          </div>
        ))}
      </div>
      <p className="text-xs mt-1 font-medium" style={{ color }}>{label}</p>
    </div>
  )
}
