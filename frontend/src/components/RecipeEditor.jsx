import { useState } from 'react'
import { api } from '../api'
import { kcal } from '../util'
import Modal from './Modal'
import FoodSearch from './FoodSearch'
import QuantityInput from './QuantityInput'

// Create or replace a recipe. Ingredients are a list of { food, servingSize }
// where food is a full payload (fdcId, name, per-100g macros, optional weight hint)
// and servingSize is the API multiplier.
export default function RecipeEditor({ existing, onClose, onSaved }) {
  const [name, setName] = useState(existing?.name ?? '')
  const [ingredients, setIngredients] = useState(
    (existing?.ingredients ?? []).map((i) => ({
      food: {
        fdcId: i.fdcId,
        name: i.foodName,
        calories: i.perServing.calories,
        protein: i.perServing.protein,
        carbs: i.perServing.carbs,
        fat: i.perServing.fat,
        servingGrams: i.servingGrams ?? null,
        servingText: i.servingText ?? null,
      },
      servingSize: i.servingSize,
    })),
  )
  const [adding, setAdding] = useState(false)
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState(null)

  function addIngredient(food) {
    setIngredients((list) => [...list, { food, servingSize: 1 }])
    setAdding(false)
  }

  function setServing(idx, multiplier) {
    setIngredients((list) =>
      list.map((row, i) => (i === idx ? { ...row, servingSize: multiplier ?? row.servingSize } : row)))
  }

  function removeIngredient(idx) {
    setIngredients((list) => list.filter((_, i) => i !== idx))
  }

  async function save() {
    if (!name.trim()) return setError('Give the recipe a name.')
    if (ingredients.length === 0) return setError('Add at least one ingredient.')
    const payload = {
      name: name.trim(),
      ingredients: ingredients.map((row) => {
        const size = Number(row.servingSize)
        return { food: row.food, servingSize: Number.isFinite(size) && size > 0 ? size : 1 }
      }),
    }
    setBusy(true)
    setError(null)
    try {
      existing ? await api.updateRecipe(existing.id, payload) : await api.createRecipe(payload)
      onSaved()
      onClose()
    } catch (e) {
      setError(e.message)
      setBusy(false)
    }
  }

  return (
    <Modal title={existing ? 'Edit recipe' : 'New recipe'} onClose={onClose} wide>
      <label className="field">
        Name
        <input
          className="input"
          value={name}
          autoFocus
          placeholder="e.g. Overnight oats"
          onChange={(e) => setName(e.target.value)}
        />
      </label>

      <h4 className="section-label">Ingredients</h4>
      {ingredients.length === 0 && <p className="muted">None yet.</p>}
      <ul className="ingredient-list">
        {ingredients.map((row, idx) => (
          <li key={idx} className="ingredient-row">
            <div className="ingredient-main">
              <span>{row.food.name}</span>
              <span className="muted">
                {kcal(row.food.calories)} / 100 g · = {kcal(row.food.calories * (row.servingSize || 0))}
              </span>
            </div>
            <QuantityInput
              compact
              initialMultiplier={row.servingSize}
              servingGrams={row.food.servingGrams}
              servingText={row.food.servingText}
              onChange={(m) => setServing(idx, m)}
            />
            <button className="icon-btn danger" onClick={() => removeIngredient(idx)}>×</button>
          </li>
        ))}
      </ul>

      {adding ? (
        <div className="add-ingredient">
          <FoodSearch autoFocus onPick={addIngredient} />
          <button className="btn ghost" onClick={() => setAdding(false)}>Cancel</button>
        </div>
      ) : (
        <button className="btn ghost" onClick={() => setAdding(true)}>+ Add ingredient</button>
      )}

      {error && <p className="error">{error}</p>}

      <div className="row-actions">
        <button className="btn ghost" onClick={onClose} disabled={busy}>Cancel</button>
        <button className="btn primary" onClick={save} disabled={busy}>
          {busy ? 'Saving…' : existing ? 'Save changes' : 'Create recipe'}
        </button>
      </div>
    </Modal>
  )
}
