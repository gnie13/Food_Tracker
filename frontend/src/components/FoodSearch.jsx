import { useEffect, useRef, useState } from 'react'
import { api } from '../api'
import { kcal, macroLine } from '../util'

// Debounced USDA search box. Calls onPick(food) with a USDA result
// ({ fdcId, name, calories, protein, carbs, fat }).
export default function FoodSearch({ onPick, autoFocus }) {
  const [query, setQuery] = useState('')
  const [results, setResults] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const timer = useRef(null)

  useEffect(() => {
    clearTimeout(timer.current)
    if (query.trim().length < 2) {
      setResults([])
      return
    }
    timer.current = setTimeout(async () => {
      setLoading(true)
      setError(null)
      try {
        setResults(await api.searchFoods(query.trim()))
      } catch (e) {
        setError(e.message)
      } finally {
        setLoading(false)
      }
    }, 300)
    return () => clearTimeout(timer.current)
  }, [query])

  return (
    <div className="food-search">
      <input
        className="input"
        type="search"
        placeholder="Search USDA foods…"
        value={query}
        autoFocus={autoFocus}
        onChange={(e) => setQuery(e.target.value)}
      />
      {loading && <p className="muted">Searching…</p>}
      {error && <p className="error">{error}</p>}
      <ul className="result-list">
        {results.map((food) => (
          <li key={food.fdcId}>
            <button className="result" onClick={() => onPick(food)}>
              <span className="result-name">{food.name}</span>
              <span className="result-macros">
                {kcal(food.calories)} &nbsp; {macroLine(food)}
              </span>
            </button>
          </li>
        ))}
        {!loading && query.trim().length >= 2 && results.length === 0 && !error && (
          <li className="muted">No matches.</li>
        )}
      </ul>
    </div>
  )
}
