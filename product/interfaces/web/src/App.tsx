import { useEffect, useState } from 'react'
import { useSimulationStore } from './stores/simulation'
import { Toolbar } from './components/layout/Toolbar'
import { Sidebar } from './components/layout/Sidebar'
import { BottomDrawer } from './components/layout/BottomDrawer'
import { KpiCards } from './components/dashboard/KpiCards'
import { TimeSeriesChart } from './components/dashboard/TimeSeriesChart'
import { FactoryFlow } from './components/dashboard/FactoryFlow'
import { MachineTable } from './components/dashboard/MachineTable'
import { JobTracker } from './components/dashboard/JobTracker'
import { WelcomeOverlay } from './components/onboarding/WelcomeOverlay'
import { ErrorBoundary } from './components/shared/ErrorBoundary'
import { Toast } from './components/shared/Toast'

type Tab = 'flow' | 'machines' | 'jobs'

export default function App() {
  const { snapshot, error, clearError, connectSse, disconnectSse } =
    useSimulationStore()
  const [activeTab, setActiveTab] = useState<Tab>('flow')
  const [showWelcome, setShowWelcome] = useState(true)

  useEffect(() => {
    connectSse()
    return () => disconnectSse()
  }, [connectSse, disconnectSse])

  const scenarioLoaded = snapshot?.scenario_loaded ?? false

  return (
    <ErrorBoundary>
      <div className="flex min-h-screen flex-col bg-background text-foreground">
        <Toolbar />

        {showWelcome && !scenarioLoaded && (
          <WelcomeOverlay onDismiss={() => setShowWelcome(false)} />
        )}

        {error && (
          <Toast message={error} type="error" onDismiss={clearError} />
        )}

        <div className="flex flex-1 overflow-hidden">
          <main className="flex flex-1 flex-col gap-4 overflow-y-auto p-4">
            <KpiCards />

            <div className="h-72 rounded-lg border border-border bg-card p-4">
              <TimeSeriesChart />
            </div>

            <div className="flex gap-2 border-b border-border">
              {(['flow', 'machines', 'jobs'] as Tab[]).map((tab) => (
                <button
                  key={tab}
                  onClick={() => setActiveTab(tab)}
                  className={`px-4 py-2 text-sm font-medium capitalize transition-colors ${
                    activeTab === tab
                      ? 'border-b-2 border-primary text-primary'
                      : 'text-muted-foreground hover:text-foreground'
                  }`}
                >
                  {tab === 'flow' ? 'Factory Flow' : tab}
                </button>
              ))}
            </div>

            <div className="min-h-48">
              {activeTab === 'flow' && <FactoryFlow />}
              {activeTab === 'machines' && <MachineTable />}
              {activeTab === 'jobs' && <JobTracker />}
            </div>
          </main>

          <aside className="w-80 shrink-0 overflow-y-auto border-l border-border">
            <Sidebar />
          </aside>
        </div>

        <BottomDrawer />
      </div>
    </ErrorBoundary>
  )
}
