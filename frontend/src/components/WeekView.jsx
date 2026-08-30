import { useCallback, useEffect, useState } from 'react'
import { api } from '../api'
import { n1, prettyDate, shiftIso, todayIso } from '../util'

// Seven-day macro totals ending on the chosen date.
export default function WeekView() {
  const [endDate, setEndDate] = useState(todayIso())
  const [range, setRange] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setRange(await api.rangeSummary(shiftIso(endDate, -6), endDate))
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [endDate])

  useEffect(() => { load() }, [load])

  return (
    <div className="week-view">
      <div className="week-head">
        <h2>Week</h2>
        <div className="date-nav">
          <button className="btn ghost" onClick={() => setEndDate((d) => shiftIso(d, -7))}>‹ Prev</button>
          <input type="date" value={endDate} onChange={(e) => e.target.value && setEndDate(e.target.value)} />
          <button className="btn ghost" onClick={() => setEndDate((d) => shiftIso(d, 7))}>Next ›</button>
        </div>
      </div>

      {error && <p className="error">{error}</p>}
      {loading && !range ? (
        <p className="muted">Loading…</p>
      ) : range && (
        <table className="week-table">
          <thead>
            <tr><th>Day</th><th>kcal</th><th>Protein</th><th>Carbs</th><th>Fat</th></tr>
          </thead>
          <tbody>
            {range.days.map((day) => (
              <tr key={day.date}>
                <td>{prettyDate(day.date)}</td>
                <td>{Math.round(day.totals.calories)}</td>
                <td>{n1(day.totals.protein)}</td>
                <td>{n1(day.totals.carbs)}</td>
                <td>{n1(day.totals.fat)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <th>Total</th>
              <td>{Math.round(range.totals.calories)}</td>
              <td>{n1(range.totals.protein)}</td>
              <td>{n1(range.totals.carbs)}</td>
              <td>{n1(range.totals.fat)}</td>
            </tr>
            <tr className="avg-row">
              <th>Daily avg</th>
              <td>{Math.round(range.totals.calories / range.days.length)}</td>
              <td>{n1(range.totals.protein / range.days.length)}</td>
              <td>{n1(range.totals.carbs / range.days.length)}</td>
              <td>{n1(range.totals.fat / range.days.length)}</td>
            </tr>
          </tfoot>
        </table>
      )}
    </div>
  )
}
