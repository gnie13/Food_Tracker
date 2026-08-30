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
