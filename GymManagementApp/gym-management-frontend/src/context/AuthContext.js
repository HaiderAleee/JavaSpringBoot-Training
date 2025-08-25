import { createContext, useContext, useState, useEffect } from "react"
import apiService from "../services/api"

const AuthContext = createContext()

export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null)
  const [loading, setLoading] = useState(true)
  const [isNewUser, setIsNewUser] = useState(false)
  const [needsProfileCompletion, setNeedsProfileCompletion] = useState(false)

  useEffect(() => {
    const token = localStorage.getItem("token")

    if (token) {
      // Decode JWT to get user info
      try {
        const payload = JSON.parse(atob(token.split(".")[1]))
        setUser({
          username: payload.sub,
          role: payload.role,
        })

        // Check if this is a new Google user who needs to complete profile
        if (payload.isNewUser) {
          setIsNewUser(true)
          setNeedsProfileCompletion(true)
        }
      } catch (error) {
        console.error("Invalid token:", error)
        localStorage.removeItem("token")
      }
    }

    // Handle OAuth callback from URL parameters (on homepage)
    const urlParams = new URLSearchParams(window.location.search)
    const oauthToken = urlParams.get("token")
    const isNewUserParam = urlParams.get("isNewUser")

    if (oauthToken) {
      apiService.setToken(oauthToken)
      try {
        const payload = JSON.parse(atob(oauthToken.split(".")[1]))
        setUser({
          username: payload.sub,
          role: payload.role,
        })

        if (isNewUserParam === "true") {
          setIsNewUser(true)
          setNeedsProfileCompletion(true)
        }

        // Clean up URL parameters
        window.history.replaceState({}, document.title, window.location.pathname)
      } catch (error) {
        console.error("Invalid OAuth token:", error)
      }
    }

    setLoading(false)
  }, [])

  const login = async (username, password) => {
    try {
      const response = await apiService.login(username, password)
      const payload = JSON.parse(atob(response.access_token.split(".")[1]))

      const userData = {
        username: payload.sub,
        role: payload.role,
      }

      setUser(userData)
      return userData
    } catch (error) {
      throw error
    }
  }

  const googleLogin = async () => {
    try {
      await apiService.googleLogin()
    } catch (error) {
      throw error
    }
  }

  const completeProfile = async (profileData) => {
    try {
      console.log("AuthContext: Completing profile with data:", profileData)
      await apiService.completeProfile(profileData)
      setNeedsProfileCompletion(false)
      setIsNewUser(false)
      return true
    } catch (error) {
      console.error("AuthContext: Profile completion failed:", error)
      throw error
    }
  }

  const logout = () => {
    apiService.removeToken()
    setUser(null)
    setIsNewUser(false)
    setNeedsProfileCompletion(false)
  }

  const value = {
    user,
    login,
    googleLogin,
    completeProfile,
    logout,
    loading,
    isAuthenticated: !!user,
    isAdmin: user?.role === "ROLE_ADMIN",
    isTrainer: user?.role === "ROLE_TRAINER",
    isMember: user?.role === "ROLE_MEMBER",
    isNewUser,
    needsProfileCompletion,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}