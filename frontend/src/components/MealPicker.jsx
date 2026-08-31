import { MEAL_TYPES, mealLabel } from '../api'

// Segmented control for choosing which meal a quick-add lands in.
export default function MealPicker({ value, onChange }) {
  return (
    <div className="meal-picker" role="tablist">
      {MEAL_TYPES.map((type) => (
        <button
          key={type}
          role="tab"
          aria-selected={value === type}
          className={value === type ? 'seg active' : 'seg'}
          onClick={() => onChange(type)}
        >
          {mealLabel(type)}
        </button>
      ))}
    </div>
  )
}
