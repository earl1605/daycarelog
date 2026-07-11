import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts'
import { useTheme } from '../contexts/ThemeContext'

export default function GrowthChart({ records }) {
  const { theme } = useTheme()
  const isDark = theme === 'dark'

  const data = [...records]
    .filter(r => r.weightKg != null || r.heightCm != null)
    .sort((a, b) => a.measurementDate.localeCompare(b.measurementDate))
    .map(r => ({
      date: new Date(r.measurementDate + 'T00:00:00').toLocaleDateString('en-PH', { month: 'short', day: 'numeric' }),
      weight: r.weightKg,
      height: r.heightCm,
    }))

  if (data.length < 2) {
    return <p className="text-gray-400 text-sm">Add at least two health records to see a growth chart.</p>
  }

  return (
    <ResponsiveContainer width="100%" height={220}>
      <LineChart data={data}>
        <CartesianGrid strokeDasharray="3 3" stroke={isDark ? '#374151' : '#F0F0EE'} vertical={false} />
        <XAxis dataKey="date" tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} />
        <YAxis yAxisId="weight" tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} width={36} />
        <YAxis yAxisId="height" orientation="right" tick={{ fontSize: 12, fill: '#9CA3AF' }} axisLine={false} tickLine={false} width={36} />
        <Tooltip
          contentStyle={{
            borderRadius: '10px',
            border: `1px solid ${isDark ? '#374151' : '#E5E7EB'}`,
            boxShadow: '0 4px 16px rgba(0,0,0,0.06)',
            fontSize: '13px',
            backgroundColor: isDark ? '#1f2937' : '#fff',
            color: isDark ? '#f9fafb' : '#111827',
          }}
        />
        <Legend wrapperStyle={{ fontSize: '12px' }} />
        <Line yAxisId="weight" type="monotone" dataKey="weight" name="Weight (kg)" stroke="#16a34a" strokeWidth={2} dot={{ r: 3 }} connectNulls />
        <Line yAxisId="height" type="monotone" dataKey="height" name="Height (cm)" stroke="#2563eb" strokeWidth={2} dot={{ r: 3 }} connectNulls />
      </LineChart>
    </ResponsiveContainer>
  )
}
