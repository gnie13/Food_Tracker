import { useCallback, useEffect, useState } from 'react'
import { api } from '../api'
import { kcal, macroLine } from '../util'
import RecipeEditor from './RecipeEditor'

export default function RecipeManager() {
  const [recipes, setRecipes] = useState([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [editing, setEditing] = useState(null) // recipe object, or 'new', or null

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      setRecipes(await api.recipes())
    } catch (e) {
      setError(e.message)
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => { load() }, [load])

  async function remove(id) {
    if (!confirm('Delete this recipe?')) return
    try {
      await api.deleteRecipe(id)
      load()
    } catch (e) {
      setError(e.message)
    }
  }

  return (
    <div className="recipe-manager">
      <div className="manager-head">
        <h2>Recipes</h2>
        <button className="btn primary" onClick={() => setEditing('new')}>+ New recipe</button>
      </div>

      {error && <p className="error">{error}</p>}
      {loading ? (
        <p className="muted">Loading…</p>
      ) : recipes.length === 0 ? (
        <p className="muted">No recipes yet. Create one to quick-add it to any meal.</p>
      ) : (
        <ul className="recipe-list">
          {recipes.map((recipe) => (
            <li key={recipe.id} className="recipe-card">
              <div className="recipe-card-head">
                <strong>{recipe.name}</strong>
                <div className="recipe-card-actions">
                  <button className="btn ghost" onClick={() => setEditing(recipe)}>Edit</button>
                  <button className="btn ghost danger" onClick={() => remove(recipe.id)}>Delete</button>
                </div>
              </div>
              <span className="muted">
                {kcal(recipe.totals.calories)} · {macroLine(recipe.totals)} · one batch
              </span>
              <ul className="recipe-ingredients">
                {recipe.ingredients.map((i) => (
                  <li key={i.id}>
                    {i.foodName} <span className="muted">×{i.servingSize}</span>
                  </li>
                ))}
              </ul>
            </li>
          ))}
        </ul>
      )}

      {editing && (
        <RecipeEditor
          existing={editing === 'new' ? null : editing}
          onClose={() => setEditing(null)}
          onSaved={load}
        />
      )}
    </div>
  )
}
