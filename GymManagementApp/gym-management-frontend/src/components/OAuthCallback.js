"use client"

import { useEffect } from "react"
import { useAuth } from "../context/AuthContext"

const OAuthCallback = () => {
  const { loading } = useAuth()

  useEffect(() => {
    // The AuthContext will handle the OAuth callback processing
    // This component just shows a loading state while processing
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
