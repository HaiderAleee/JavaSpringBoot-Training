"use client"
import { AuthProvider, useAuth } from "./context/AuthContext"
import { useState } from "react"
import Layout from "./components/Layout"
import Login from "./components/Login"
import AdminPanel from "./components/AdminPanel"
import TrainerDashboard from "./components/TrainerDashboard"
import MemberDashboard from "./components/MemberDashboard"
import ProfileCompletion from "./components/ProfileCompletion"
import "./App.css"

function AppContent() {
  const { isAuthenticated, loading, isAdmin, isTrainer, isMember, needsProfileCompletion } = useAuth()
  const [activeSection, setActiveSection] = useState("dashboard")

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner"></div>
        <p>Loading...</p>
      </div>
    )
  }

  if (!isAuthenticated) {
    return <Login />
  }

  // Show profile completion for new Google users
  if (needsProfileCompletion) {
    return <ProfileCompletion />
  }

  const renderDashboard = () => {
    if (isAdmin) {
      return <AdminPanel activeSection={activeSection} onSectionChange={setActiveSection} />
    } else if (isTrainer) {
      return <TrainerDashboard activeSection={activeSection} onSectionChange={setActiveSection} />
    } else if (isMember) {
      return <MemberDashboard activeSection={activeSection} onSectionChange={setActiveSection} />
    }
    return <div>Unknown role</div>
  }

  return (
    <Layout activeSection={activeSection} onSectionChange={setActiveSection}>
      {renderDashboard()}
    </Layout>
  )
}

function App() {
  return (
    <AuthProvider>
      <div className="App">
        <AppContent />
      </div>
    </AuthProvider>
  )
}

export default App