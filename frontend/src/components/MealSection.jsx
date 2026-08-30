import { mealLabel } from '../api'
import { kcal } from '../util'
import EntryRow from './EntryRow'

// One meal (breakfast/lunch/dinner/snack) on the current day.
export default function MealSection({ mealType, meal, onAdd, onPatchEntry, onDeleteEntry }) {
  const entries = meal?.entries ?? []
  const subtotal = meal?.subtotal

  return (
    <section className="meal-section">
      <header className="meal-head">
        <h3>{mealLabel(mealType)}</h3>
        <div className="meal-head-right">
          {subtotal && entries.length > 0 && (
            <span className="muted">{kcal(subtotal.calories)}</span>
          )}
          <button className="btn ghost" onClick={() => onAdd(mealType)}>+ Add</button>
        </div>
      </header>

      {entries.length === 0 ? (
        <p className="muted empty">Nothing logged.</p>
      ) : (
        <ul className="entry-list">
          {entries.map((entry) => (
            <EntryRow
              key={entry.id}
              entry={entry}
              onPatch={(patch) => onPatchEntry(entry.id, patch)}
              onDelete={() => onDeleteEntry(entry.id)}
            />
          ))}
        </ul>
      )}
    </section>
  )
}
