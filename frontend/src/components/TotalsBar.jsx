import { n1 } from '../util'

const MACROS = [
  ['calories', 'kcal', 0],
  ['protein', 'protein', 1],
  ['carbs', 'carbs', 1],
  ['fat', 'fat', 1],
]

export default function TotalsBar({ totals, label = 'Day total' }) {
  return (
    <div className="totals-bar">
      <span className="totals-label">{label}</span>
      <div className="totals-figures">
        {MACROS.map(([key, unit, dp]) => (
          <div key={key} className="figure">
            <strong>{dp === 0 ? Math.round(totals[key]) : n1(totals[key])}</strong>
            <span>{unit}</span>
          </div>
        ))}
      </div>
    </div>
  )
}
