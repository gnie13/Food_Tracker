import { useCallback, useEffect, useState } from 'react'
import { api, MEAL_TYPES } from '../api'
import { prettyDate, shiftIso, todayIso } from '../util'
import TotalsBar from './TotalsBar'
import MealSection from './MealSection'
import QuickAddRail from './QuickAddRail'
import AddFoodDialog from './AddFoodDialog'

export default function DayView() {
  const [date, setDate] = useState(todayIso())
  const [summary, setSummary] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [addTo, setAddTo] = useState(null) // meal type for the add dialog
  const [targetMeal, setTargetMeal] = useState('BREAKFAST')
  const [refreshKey, setRefreshKey] = useState(0)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setSummary(await api.dailySummary(date))
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [date])

  useEffect(() => { load() }, [load])

  const refresh = () => {
    load()
    setRefreshKey((k) => k + 1)
  }

  async function patchEntry(id, patch) {
    try {
      await api.updateEntry(id, patch)
      refresh()
    } catch (e) {
      setError(e.message)
    }
  }

  async function deleteEntry(id) {
    try {
      await api.deleteEntry(id)
      refresh()
    } catch (e) {
      setError(e.message)
    }
  }

  const mealByType = (type) => summary?.meals.find((m) => m.mealType === type) ?? null

  return (
    <div className="day-view">
      <div className="day-main">
        <div className="date-nav">
          <button className="btn ghost" onClick={() => setDate((d) => shiftIso(d, -1))}>‹</button>
          <div className="date-label">
            <strong>{prettyDate(date)}</strong>
            <input
              type="date"
              value={date}
              onChange={(e) => e.target.value && setDate(e.target.value)}
            />
          </div>
          <button className="btn ghost" onClick={() => setDate((d) => shiftIso(d, 1))}>›</button>
          {date !== todayIso() && (
            <button className="btn ghost today" onClick={() => setDate(todayIso())}>Today</button>
          )}
        </div>

        {error && <p className="error">{error}</p>}
        {summary && <TotalsBar totals={summary.totals} />}

        {loading && !summary ? (
          <p className="muted">Loading…</p>
        ) : (
          MEAL_TYPES.map((type) => (
            <MealSection
              key={type}
              mealType={type}
              meal={mealByType(type)}
              onAdd={setAddTo}
              onPatchEntry={patchEntry}
              onDeleteEntry={deleteEntry}
            />
          ))
        )}
      </div>

      <QuickAddRail
        date={date}
        targetMeal={targetMeal}
        onTargetMealChange={setTargetMeal}
        onChanged={refresh}
        refreshKey={refreshKey}
      />

      {addTo && (
        <AddFoodDialog
          date={date}
          mealType={addTo}
          onClose={() => setAddTo(null)}
          onLogged={refresh}
        />
      )}
    </div>
  )
}
