import { useState } from 'react'
import { fromMultiplier, n1, toMultiplier, unitOptions } from '../util'

// Number + unit picker for how much of a food to log. Emits the API's
// servingSize multiplier via onChange whenever the value or unit changes.
// Uncontrolled: seed it with initialMultiplier, read results through onChange.
export default function QuantityInput({
  initialMultiplier = 1,
  servingGrams,
  servingText,
  onChange,
  autoFocus,
  compact,
}) {
  const [unit, setUnit] = useState('g')
  const [value, setValue] = useState(String(fromMultiplier(initialMultiplier, 'g', servingGrams)))

  function emit(nextValue, nextUnit) {
    setValue(nextValue)
    setUnit(nextUnit)
    onChange(toMultiplier(nextValue, nextUnit, servingGrams))
  }

  function changeUnit(nextUnit) {
    // keep the real amount steady, just re-express it in the new unit
    const current = toMultiplier(value, unit, servingGrams) ?? initialMultiplier
    emit(String(fromMultiplier(current, nextUnit, servingGrams)), nextUnit)
  }

  const grams = toMultiplier(value, unit, servingGrams)
    ? n1(toMultiplier(value, unit, servingGrams) * 100)
    : null

  return (
    <div className={`qty${compact ? ' qty-compact' : ''}`}>
      <input
        className="input qty-value"
        type="number"
        min="0"
        step="any"
        value={value}
        autoFocus={autoFocus}
        onChange={(e) => emit(e.target.value, unit)}
      />
      <select className="input qty-unit" value={unit} onChange={(e) => changeUnit(e.target.value)}>
        {unitOptions(servingGrams).map((o) => (
          <option key={o.id} value={o.id}>{o.label}</option>
        ))}
      </select>
      {!compact && (
        <span className="qty-hint muted">
          {unit !== 'g' && grams != null && `≈ ${grams} g`}
          {servingGrams ? ` · 1 serving = ${n1(servingGrams)} g${servingText ? ` (${servingText})` : ''}` : ''}
        </span>
      )}
    </div>
  )
}
