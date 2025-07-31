"use client"

import { useState, useEffect } from "react"
import { useAuth } from "../context/AuthContext"
import apiService from "../services/api"
import "./ProfileCompletion.css"

const ProfileCompletion = () => {
  const { user, completeProfile, logout } = useAuth()
  const [formData, setFormData] = useState({
    phoneNumber: "",
    gender: "",
    trainerId: "",
  })
  const [trainers, setTrainers] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState("")

  useEffect(() => {
    loadTrainers()
  }, [])

  const loadTrainers = async () => {
    try {
      const trainersData = await apiService.getAllTrainers()
      setTrainers(trainersData)
    } catch (error) {
      console.error("Error loading trainers:", error)
    }
  }

  const handleChange = (e) => {
    const { name, value } = e.target
    setFormData({
      ...formData,
      [name]: value === "" ? null : value,
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setError("")
    setLoading(true)

    try {
      // Convert trainerId to number or null
      const profileData = {
        phoneNumber: formData.phoneNumber || null,
        gender: formData.gender || null,
        trainerId: formData.trainerId ? Number.parseInt(formData.trainerId) : null,
      }

      console.log("Submitting profile data:", profileData)
      await completeProfile(profileData)
    } catch (error) {
      console.error("Profile completion error:", error)
      setError("Failed to complete profile. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  const handleSkip = async () => {
    setError("")
    setLoading(true)
    try {
      console.log("Skipping profile completion")
      await completeProfile({
        phoneNumber: null,
        gender: null,
        trainerId: null,
      })
    } catch (error) {
      console.error("Skip profile error:", error)
      setError("Failed to skip profile completion. Please try again.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="profile-completion-container">
      <div className="profile-completion-card">
        <div className="welcome-header">
          <h2>🎉 Welcome to Our Gym!</h2>
          <p>Hi {user?.username}, let's complete your profile to get started</p>
          <button onClick={logout} className="logout-button">
            Logout
          </button>
        </div>

        <form onSubmit={handleSubmit} className="profile-form">
          {error && <div className="error-message">{error}</div>}

          <div className="form-group">
            <label htmlFor="phoneNumber">📞 Phone Number</label>
            <input
              type="tel"
              id="phoneNumber"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              className="form-input"
              placeholder="Enter your phone number"
            />
          </div>

          <div className="form-group">
            <label htmlFor="gender">👤 Gender</label>
            <select id="gender" name="gender" value={formData.gender} onChange={handleChange} className="form-select">
              <option value="">Select your gender</option>
              <option value="Male">Male</option>
              <option value="Female">Female</option>
              <option value="Other">Other</option>
              <option value="Prefer not to say">Prefer not to say</option>
            </select>
          </div>

          <div className="form-group">
            <label htmlFor="trainerId">🏋️‍♂️ Personal Trainer (Optional)</label>
            <select
              id="trainerId"
              name="trainerId"
              value={formData.trainerId}
              onChange={handleChange}
              className="form-select"
            >
              <option value="">No trainer (you can choose later)</option>
              {trainers.map((trainer) => (
                <option key={trainer.id} value={trainer.id}>
                  {trainer.username} - {trainer.specialty || "General Training"}
                </option>
              ))}
            </select>
          </div>

          <div className="form-actions">
            <button type="submit" disabled={loading} className="complete-button">
              {loading ? "Completing..." : "Complete Profile"}
            </button>

            <button type="button" onClick={handleSkip} disabled={loading} className="skip-button">
              {loading ? "Skipping..." : "Skip for now"}
            </button>
          </div>
        </form>

        <div className="info-section">
          <h3>What's next?</h3>
          <ul>
            <li>✅ Access your member dashboard</li>
            <li>✅ View available trainers</li>
            <li>✅ Update your profile anytime</li>
            <li>✅ Start your fitness journey!</li>
          </ul>
        </div>
      </div>
    </div>
  )
}

export default ProfileCompletion
