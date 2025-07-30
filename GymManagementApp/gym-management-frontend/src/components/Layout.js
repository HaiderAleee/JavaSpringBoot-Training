"use client"
import { useAuth } from "../context/AuthContext"
import "./Layout.css"

const Layout = ({ children, activeSection, onSectionChange }) => {
  const { user, logout, isAdmin, isTrainer, isMember } = useAuth()

  const handleLogout = () => {
    logout()
  }

  const handleNavClick = (section) => {
    if (onSectionChange) {
      onSectionChange(section)
    }
  }

  return (
    <div className="layout">
      <header className="header">
        <div className="header-content">
          <h1 className="logo">💪 Gym Management System</h1>
          <div className="user-info">
            <span className="welcome">Welcome, {user?.username}</span>
            <span className={`role-badge ${user?.role?.replace("ROLE_", "").toLowerCase()}`}>
              {user?.role?.replace("ROLE_", "")}
            </span>
            <button onClick={handleLogout} className="logout-btn">
              Logout
            </button>
          </div>
        </div>
      </header>

      <div className="main-content">
        <nav className="sidebar">
          <ul className="nav-menu">
            <li>
              <button
                className={`nav-button ${activeSection === "dashboard" ? "active" : ""}`}
                onClick={() => handleNavClick("dashboard")}
              >
                📊 Dashboard
              </button>
            </li>

            {isAdmin && (
              <>
                <li>
                  <button
                    className={`nav-button ${activeSection === "admins" ? "active" : ""}`}
                    onClick={() => handleNavClick("admins")}
                  >
                    👨‍💼 Admins
                  </button>
                </li>
                <li>
                  <button
                    className={`nav-button ${activeSection === "trainers" ? "active" : ""}`}
                    onClick={() => handleNavClick("trainers")}
                  >
                    🏋️‍♂️ Trainers
                  </button>
                </li>
                <li>
                  <button
                    className={`nav-button ${activeSection === "members" ? "active" : ""}`}
                    onClick={() => handleNavClick("members")}
                  >
                    👥 Members
                  </button>
                </li>
              </>
            )}

            {isTrainer && (
              <>
                <li>
                  <button
                    className={`nav-button ${activeSection === "my-members" ? "active" : ""}`}
                    onClick={() => handleNavClick("my-members")}
                  >
                    👥 My Members
                  </button>
                </li>
                <li>
                  <button
                    className={`nav-button ${activeSection === "all-trainers" ? "active" : ""}`}
                    onClick={() => handleNavClick("all-trainers")}
                  >
                    🏋️‍♂️ All Trainers
                  </button>
                </li>
                <li>
                  <button
                    className={`nav-button ${activeSection === "profile" ? "active" : ""}`}
                    onClick={() => handleNavClick("profile")}
                  >
                    👤 My Profile
                  </button>
                </li>
              </>
            )}

            {isMember && (
              <>
                <li>
                  <button
                    className={`nav-button ${activeSection === "profile" ? "active" : ""}`}
                    onClick={() => handleNavClick("profile")}
                  >
                    👤 My Profile
                  </button>
                </li>
                <li>
                  <button
                    className={`nav-button ${activeSection === "trainers" ? "active" : ""}`}
                    onClick={() => handleNavClick("trainers")}
                  >
                    🏋️‍♂️ Trainers
                  </button>
                </li>
              </>
            )}
          </ul>
        </nav>

        <main className="content">{children}</main>
      </div>
    </div>
  )
}

export default Layout
