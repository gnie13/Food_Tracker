import { useState } from 'react'
import { kcal, macroLine, n1 } from '../util'
import QuantityInput from './QuantityInput'

// One logged food. Quantity is editable inline (PATCH); the row can be deleted.
export default function EntryRow({ entry, onPatch, onDelete }) {
  const [editing, setEditing] = useState(false)
  const [multiplier, setMultiplier] = useState(entry.servingSize)
  const [busy, setBusy] = useState(false)

  async function commit() {
    if (!multiplier || multiplier <= 0 || multiplier === entry.servingSize) {
      setEditing(false)
      return
    }
    setBusy(true)
    try {
      await onPatch({ servingSize: multiplier })
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
          <div
            className="entry-edit"
            onKeyDown={(e) => {
              if (e.key === 'Enter') commit()
              if (e.key === 'Escape') { setMultiplier(entry.servingSize); setEditing(false) }
            }}
          >
            <QuantityInput
              compact
              initialMultiplier={entry.servingSize}
              servingGrams={entry.servingGrams}
              servingText={entry.servingText}
              onChange={setMultiplier}
              autoFocus
            />
            <button className="btn ghost" onClick={commit} disabled={busy}>Save</button>
          </div>
        ) : (
          <button className="serving-chip" onClick={() => setEditing(true)} title="Edit quantity">
            {n1(entry.grams)} g
          </button>
        )}
      </div>

      <button className="icon-btn danger" onClick={onDelete} aria-label="Remove entry">×</button>
    </li>
  )
}
