import { CheckIcon } from './icons'

export default function ImmunizationChecklist({ schedule, records }) {
  const given = new Map(records.map(r => [`${r.vaccineName}|${r.doseNumber}`, r]))

  return (
    <div className="space-y-3">
      {schedule.map(v => (
        <div key={v.name} className="bg-white rounded-xl border border-gray-100 p-4">
          <p className="font-medium text-gray-900 mb-2">{v.name}</p>
          <div className="flex flex-wrap gap-2">
            {Array.from({ length: v.expectedDoses }, (_, i) => i + 1).map(dose => {
              const rec = given.get(`${v.name}|${dose}`)
              return (
                <span key={dose}
                  className={`inline-flex items-center gap-1.5 text-xs font-medium px-2.5 py-1 rounded-full ${rec ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-400'}`}>
                  {rec && <CheckIcon width={12} height={12} />}
                  Dose {dose}{rec ? ` · ${new Date(rec.dateGiven + 'T00:00:00').toLocaleDateString('en-PH')}` : ''}
                </span>
              )
            })}
          </div>
        </div>
      ))}
    </div>
  )
}
