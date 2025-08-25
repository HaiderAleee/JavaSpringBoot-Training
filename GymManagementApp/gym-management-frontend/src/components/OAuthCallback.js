"use client"

import { useEffect } from "react"
import { useAuth } from "../context/AuthContext"

const OAuthCallback = () => {
  const { loading } = useAuth()

  useEffect(() => {
  }, [])

  if (loading) {
    return (
      <div className="loading-screen">
        <div className="loading-spinner"></div>
        <p>Processing Google login...</p>
      </div>
    )
  }

  return null
}

export default OAuthCallback
