// Thin wrapper over the meal-planner REST API. Every call returns parsed JSON
// (or undefined for 204) and throws an Error with the server's message on failure.

async function request(path, options = {}) {
  const res = await fetch(`/api${path}`, {
    headers: options.body ? { 'Content-Type': 'application/json' } : undefined,
    ...options,
  })

  if (!res.ok) {
    let message = `${res.status} ${res.statusText}`
    try {
      const problem = await res.json()
      message = problem.message || problem.detail || problem.error || message
    } catch {
      // no JSON body
    }
    throw new Error(message)
  }

  if (res.status === 204) return undefined
  return res.json()
}

const body = (data) => ({ body: JSON.stringify(data) })

export const api = {
  // Foods
  searchFoods: (query) => request(`/foods/search?query=${encodeURIComponent(query)}`),
  frequentFoods: (mealType, limit = 12) =>
    request(`/foods/frequent?limit=${limit}${mealType ? `&mealType=${mealType}` : ''}`),
  savedFoods: () => request('/foods/saved'),
  saveFood: (foodId) => request(`/foods/${foodId}/saved`, { method: 'PUT' }),
  unsaveFood: (foodId) => request(`/foods/${foodId}/saved`, { method: 'DELETE' }),

  // Meals & entries
  mealsForDate: (date) => request(`/meals?date=${date}`),
  logEntry: (payload) => request('/meals/entries', { method: 'POST', ...body(payload) }),
  logRecipe: (payload) => request('/meals/entries/from-recipe', { method: 'POST', ...body(payload) }),
  updateEntry: (id, patch) => request(`/meals/entries/${id}`, { method: 'PATCH', ...body(patch) }),
  deleteEntry: (id) => request(`/meals/entries/${id}`, { method: 'DELETE' }),

  // Summaries
  dailySummary: (date) => request(`/summary?date=${date}`),
  rangeSummary: (startDate, endDate) =>
    request(`/summary/range?startDate=${startDate}&endDate=${endDate}`),

  // Recipes
  recipes: () => request('/recipes'),
  recipe: (id) => request(`/recipes/${id}`),
  createRecipe: (payload) => request('/recipes', { method: 'POST', ...body(payload) }),
  updateRecipe: (id, payload) => request(`/recipes/${id}`, { method: 'PUT', ...body(payload) }),
  deleteRecipe: (id) => request(`/recipes/${id}`, { method: 'DELETE' }),
}

export const MEAL_TYPES = ['BREAKFAST', 'LUNCH', 'DINNER', 'SNACK']

export const mealLabel = (type) =>
  type.charAt(0) + type.slice(1).toLowerCase()
