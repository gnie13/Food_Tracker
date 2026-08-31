import { useState } from 'react'
import { kcal, macroLine, n1 } from '../util'

// One logged food. Serving size is editable inline (PATCH); the row also moves
// between meals via the meal-type <select> and can be deleted.
export default function EntryRow({ entry, onPatch, onDelete }) {
  const [editing, setEditing] = useState(false)
  const [serving, setServing] = useState(entry.servingSize)
  const [busy, setBusy] = useState(false)

  async function commit() {
    const value = Number(serving)
    if (!Number.isFinite(value) || value <= 0) {
      setServing(entry.servingSize)
      setEditing(false)
      return
    }
    if (value === entry.servingSize) {
      setEditing(false)
      return
    }
    setBusy(true)
    try {
      await onPatch({ servingSize: value })
    } finally {
      setBusy(false)
      setEditing(false)
    }
  }

  return (
    <li className="entry-row">
      <div className="entry-main">
        <span className="entry-name">{entry.foodName}</span>
        <span className="entry-macros">{kcal(entry.nutrition.calories)} · {macroLine(entry.nutrition)}</span>
      </div>

      <div className="entry-serving">
        {editing ? (
          <input
            className="input serving-input"
            type="number"
            min="0"
            step="0.25"
            value={serving}
            autoFocus
            disabled={busy}
            onChange={(e) => setServing(e.target.value)}
            onBlur={commit}
            onKeyDown={(e) => {
              if (e.key === 'Enter') commit()
              if (e.key === 'Escape') { setServing(entry.servingSize); setEditing(false) }
            }}
          />
        ) : (
          <button className="serving-chip" onClick={() => setEditing(true)} title="Edit serving size">
            ×{n1(entry.servingSize)}
          </button>
        )}
      </div>

      <button className="icon-btn danger" onClick={onDelete} aria-label="Remove entry">×</button>
    </li>
  )
}
