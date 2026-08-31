// Date + number helpers. Dates are handled as plain YYYY-MM-DD strings so they
// line up with the API and never drift across timezones.

export function todayIso() {
  const now = new Date()
  return toIso(now.getFullYear(), now.getMonth() + 1, now.getDate())
}

function toIso(y, m, d) {
  return `${y}-${String(m).padStart(2, '0')}-${String(d).padStart(2, '0')}`
}

export function shiftIso(iso, days) {
  const [y, m, d] = iso.split('-').map(Number)
  const dt = new Date(y, m - 1, d + days)
  return toIso(dt.getFullYear(), dt.getMonth() + 1, dt.getDate())
}

export function prettyDate(iso) {
  const [y, m, d] = iso.split('-').map(Number)
  const dt = new Date(y, m - 1, d)
  const rel = relativeDay(iso)
  const base = dt.toLocaleDateString(undefined, {
    weekday: 'short', month: 'short', day: 'numeric',
  })
  return rel ? `${base} · ${rel}` : base
}

function relativeDay(iso) {
  const today = todayIso()
  if (iso === today) return 'Today'
  if (iso === shiftIso(today, -1)) return 'Yesterday'
  if (iso === shiftIso(today, 1)) return 'Tomorrow'
  return null
}

// Round for display without trailing float noise.
export const n1 = (x) => Math.round(x * 10) / 10
export const kcal = (x) => `${Math.round(x)} kcal`
export const grams = (x) => `${n1(x)} g`

export function macroLine(nutrition) {
  return `P ${n1(nutrition.protein)} · C ${n1(nutrition.carbs)} · F ${n1(nutrition.fat)}`
}

// ---- quantity units ----
// Stored macros are per 100 g, so the API's servingSize is grams / 100.
export const GRAMS_PER = { g: 1, oz: 28.349523125, lb: 453.59237 }

export function unitOptions(servingGrams) {
  const opts = [
    { id: 'g', label: 'g' },
    { id: 'oz', label: 'oz' },
    { id: 'lb', label: 'lb' },
  ]
  if (servingGrams) opts.push({ id: 'serving', label: 'serving' })
  opts.push({ id: 'x', label: '×100g' })
  return opts
}

// A quantity {value, unit} -> the servingSize multiplier the API expects.
export function toMultiplier(value, unit, servingGrams) {
  const v = Number(value)
  if (!Number.isFinite(v) || v <= 0) return null
  if (unit === 'x') return v
  if (unit === 'serving') return servingGrams ? (v * servingGrams) / 100 : v
  return (v * (GRAMS_PER[unit] ?? 1)) / 100
}

// The servingSize multiplier -> a value in the given unit (for pre-filling).
export function fromMultiplier(multiplier, unit, servingGrams) {
  const grams = multiplier * 100
  if (unit === 'x') return n1(multiplier)
  if (unit === 'serving') return servingGrams ? n1(grams / servingGrams) : n1(multiplier)
  return n1(grams / (GRAMS_PER[unit] ?? 1))
}
