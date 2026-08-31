import { useState } from 'react'
import { api, mealLabel } from '../api'
import { kcal, macroLine } from '../util'
import Modal from './Modal'
import FoodSearch from './FoodSearch'
import QuantityInput from './QuantityInput'

// Search USDA, pick a food, choose a serving multiplier, log it against the
// given meal on the given date.
export default function AddFoodDialog({ date, mealType, onClose, onLogged }) {
  const [picked, setPicked] = useState(null)
  const [multiplier, setMultiplier] = useState(1)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  async function log() {
    if (!multiplier || multiplier <= 0) {
      setError('Enter a quantity greater than zero.')
      return
    }
    setBusy(true)
    setError(null)
    try {
      await api.logEntry({
        date,
        mealType,
        servingSize: multiplier,
        food: {
          fdcId: picked.fdcId,
          name: picked.name,
          calories: picked.calories,
          protein: picked.protein,
          carbs: picked.carbs,
          fat: picked.fat,
          servingGrams: picked.servingGrams ?? null,
          servingText: picked.servingText ?? null,
        },
      })
      onLogged()
      onClose()
    } catch (e) {
      setError(e.message)
      setBusy(false)
    }
  }

  return (
    <Modal title={`Add to ${mealLabel(mealType)}`} onClose={onClose} wide>
      {!picked ? (
        <FoodSearch autoFocus onPick={setPicked} />
      ) : (
        <div className="picked">
          <div className="picked-food">
            <strong>{picked.name}</strong>
            <span className="muted">
              {kcal(picked.calories)} · {macroLine(picked)} <em>per 100 g</em>
            </span>
          </div>

          <label className="field">
            Quantity
            <QuantityInput
              initialMultiplier={1}
              servingGrams={picked.servingGrams}
              servingText={picked.servingText}
              onChange={setMultiplier}
              autoFocus
            />
          </label>

          {error && <p className="error">{error}</p>}

          <div className="row-actions">
            <button className="btn ghost" onClick={() => setPicked(null)} disabled={busy}>
              ← Back to search
            </button>
            <button className="btn primary" onClick={log} disabled={busy}>
              {busy ? 'Adding…' : 'Add'}
            </button>
          </div>
        </div>
      )}
    </Modal>
  )
}
