import { useCallback, useEffect, useState } from 'react'
import { api, mealLabel } from '../api'
import { kcal } from '../util'
import MealPicker from './MealPicker'

// Sidebar of one-tap adds: frequently-logged foods, pinned foods, and saved
// recipes. Everything lands in the meal chosen at the top.
export default function QuickAddRail({ date, targetMeal, onTargetMealChange, onChanged, refreshKey }) {
  const [frequent, setFrequent] = useState([])
  const [saved, setSaved] = useState([])
  const [recipes, setRecipes] = useState([])
  const [error, setError] = useState(null)
  const [pendingId, setPendingId] = useState(null)

  const load = useCallback(async () => {
    setError(null)
    try {
      const [f, s, r] = await Promise.all([
        api.frequentFoods(targetMeal, 12),
        api.savedFoods(),
        api.recipes(),
      ])
      setFrequent(f)
      setSaved(s)
      setRecipes(r)
    } catch (e) {
      setError(e.message)
    }
  }, [targetMeal])

  useEffect(() => { load() }, [load, refreshKey])

  async function addFood(food) {
    setPendingId(`food-${food.foodId}`)
    try {
      // One serving if the food carries a weight, else 100 g (multiplier 1).
      const servingSize = food.servingGrams ? food.servingGrams / 100 : 1
      await api.logEntry({
        date,
        mealType: targetMeal,
        servingSize,
        food: {
          fdcId: food.fdcId,
          name: food.name,
          calories: food.calories,
          protein: food.protein,
          carbs: food.carbs,
          fat: food.fat,
          servingGrams: food.servingGrams ?? null,
          servingText: food.servingText ?? null,
        },
      })
      onChanged()
    } catch (e) {
      setError(e.message)
    } finally {
      setPendingId(null)
    }
  }

  async function addRecipe(recipe) {
    setPendingId(`recipe-${recipe.id}`)
    try {
      await api.logRecipe({ date, mealType: targetMeal, recipeId: recipe.id, factor: 1 })
      onChanged()
    } catch (e) {
      setError(e.message)
    } finally {
      setPendingId(null)
    }
  }

  async function togglePin(food, pin) {
    try {
      pin ? await api.saveFood(food.foodId) : await api.unsaveFood(food.foodId)
      load()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <aside className="rail">
      <div className="rail-target">
        <span className="rail-target-label">Quick add to</span>
        <MealPicker value={targetMeal} onChange={onTargetMealChange} />
      </div>

      {error && <p className="error">{error}</p>}

      <RailGroup title={`Frequent · ${mealLabel(targetMeal)}`}>
        {frequent.length === 0 && (
          <li className="muted">Nothing logged at {mealLabel(targetMeal).toLowerCase()} yet.</li>
        )}
        {frequent.map((food) => (
          <li key={food.foodId} className="rail-item">
            <button
              className="rail-add"
              disabled={pendingId === `food-${food.foodId}`}
              onClick={() => addFood(food)}
            >
              <span className="rail-name">{food.name}</span>
              <span className="rail-sub">
                {kcal(food.calories)}/100g · {food.timesLogged}× here
              </span>
            </button>
            <button
              className={`star${food.saved ? ' on' : ''}`}
              title={food.saved ? 'Unpin' : 'Pin'}
              onClick={() => togglePin(food, !food.saved)}
            >
              {food.saved ? '★' : '☆'}
            </button>
          </li>
        ))}
      </RailGroup>

      <RailGroup title="Saved">
        {saved.length === 0 && <li className="muted">Pin foods with the ☆.</li>}
        {saved.map((food) => (
          <li key={food.foodId} className="rail-item">
            <button
              className="rail-add"
              disabled={pendingId === `food-${food.foodId}`}
              onClick={() => addFood(food)}
            >
              <span className="rail-name">{food.name}</span>
              <span className="rail-sub">
                {kcal(food.calories)}/100g{food.servingText ? ` · ${food.servingText}` : ''}
              </span>
            </button>
            <button className="star on" title="Unpin" onClick={() => togglePin(food, false)}>★</button>
          </li>
        ))}
      </RailGroup>

      <RailGroup title="Recipes">
        {recipes.length === 0 && <li className="muted">No recipes yet.</li>}
        {recipes.map((recipe) => (
          <li key={recipe.id} className="rail-item">
            <button
              className="rail-add"
              disabled={pendingId === `recipe-${recipe.id}`}
              onClick={() => addRecipe(recipe)}
            >
              <span className="rail-name">{recipe.name}</span>
              <span className="rail-sub">
                {kcal(recipe.totals.calories)} · {recipe.ingredients.length} items
              </span>
            </button>
          </li>
        ))}
      </RailGroup>

      <p className="rail-hint muted">
        Adds one serving (or 100 g) / one batch to {mealLabel(targetMeal)}.
      </p>
    </aside>
  )
}

function RailGroup({ title, children }) {
  return (
    <div className="rail-group">
      <h4>{title}</h4>
      <ul>{children}</ul>
    </div>
  )
}
