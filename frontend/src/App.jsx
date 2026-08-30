import { useState } from 'react'
import DayView from './components/DayView'
import RecipeManager from './components/RecipeManager'
import WeekView from './components/WeekView'

const TABS = [
  ['day', 'Day'],
  ['week', 'Week'],
  ['recipes', 'Recipes'],
]

export default function App() {
  const [tab, setTab] = useState('day')

  return (
    <div className="app">
      <header className="app-bar">
        <h1>Meal Planner</h1>
        <nav className="tabs">
          {TABS.map(([id, label]) => (
            <button
              key={id}
              className={tab === id ? 'tab active' : 'tab'}
              onClick={() => setTab(id)}
            >
              {label}
            </button>
          ))}
        </nav>
      </header>

      <main className="app-main">
        {tab === 'day' && <DayView />}
        {tab === 'week' && <WeekView />}
        {tab === 'recipes' && <RecipeManager />}
      </main>
    </div>
  )
}
