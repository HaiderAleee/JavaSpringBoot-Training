"use client"

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
      } catch (error) {
        console.error("Invalid token:", error)
        localStorage.removeItem("token")
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

  const logout = () => {
    apiService.removeToken()
    setUser(null)
  }

  const value = {
    user,
    login,
    logout,
    loading,
    isAuthenticated: !!user,
    isAdmin: user?.role === "ROLE_ADMIN",
    isTrainer: user?.role === "ROLE_TRAINER",
    isMember: user?.role === "ROLE_MEMBER",
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
