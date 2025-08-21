"use client"

import { useState, useEffect } from "react"
import { useAuth } from "../context/AuthContext"
import apiService from "../services/api"
import "./MemberDashboard.css"

const MemberDashboard = ({ activeSection, onSectionChange }) => {
  const { user } = useAuth()
  const [profile, setProfile] = useState(null)
  const [trainers, setTrainers] = useState([])
  const [loading, setLoading] = useState(false)
  const [showProfileModal, setShowProfileModal] = useState(false)

  useEffect(() => {
    loadData()
  }, [activeSection])

  const loadData = async () => {
    if (!activeSection || activeSection === "dashboard") {
      try {
        const profileData = await apiService.getMyProfile()
        setProfile(profileData)
      } catch (error) {
        console.error("Error loading profile:", error)
      }
      return
    }

    setLoading(true)
    try {
      if (activeSection === "profile") {
        const profileData = await apiService.getMyProfile()
        setProfile(profileData)
      } else if (activeSection === "trainers") {
        const trainersData = await apiService.getAllTrainers()
        setTrainers(trainersData)
      }
    } catch (error) {
      console.error("Error loading data:", error)
    } finally {
      setLoading(false)
    }
  }

  // ✅ Fixed: only pass one object argument
  const handleUpdateProfile = async (profileData) => {
    try {
      const updated = { ...profile, ...profileData } // merge old + new
      await apiService.updateMyProfile(updated)
      setProfile(updated)
      setShowProfileModal(false)
    } catch (error) {
      console.error("Error updating profile:", error)
    }
  }

  const renderDashboard = () => (
    <div className="dashboard-overview">
      <div className="welcome-section">
        <h2>👤 Member Dashboard</h2>
        <p>Welcome back, {user?.username}!</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">👤</div>
          <div className="stat-info">
            <h3>{profile?.username || "N/A"}</h3>
            <p>Username</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">🏋️‍♂️</div>
          <div className="stat-info">
            <h3>{profile?.trainerid ? `Trainer #${profile.trainerid}` : "No Trainer"}</h3>
            <p>Current Trainer</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">📅</div>
          <div className="stat-info">
            <h3>{profile?.joinDate || "N/A"}</h3>
            <p>Join Date</p>
          </div>
        </div>
      </div>

      <div className="quick-actions">
        <h3>Quick Actions</h3>
        <div className="action-buttons">
          <button onClick={() => onSectionChange("profile")} className="action-btn">
            Edit My Profile
          </button>
          <button onClick={() => onSectionChange("trainers")} className="action-btn">
            View Trainers
          </button>
        </div>
      </div>
    </div>
  )

  const renderProfile = () => (
    <div className="profile-section">
      <div className="section-header">
        <h2>👤 My Profile</h2>
        <button onClick={() => setShowProfileModal(true)} className="btn btn-primary">
          ✏️ Edit Profile
        </button>
      </div>

      <div className="profile-card">
        <div className="profile-field">
          <label>Username:</label>
          <span>{profile?.username}</span>
        </div>
        <div className="profile-field">
          <label>Phone Number:</label>
          <span>{profile?.phoneNumber || "Not provided"}</span>
        </div>
        <div className="profile-field">
          <label>Gender:</label>
          <span>{profile?.gender || "Not specified"}</span>
        </div>
        <div className="profile-field">
          <label>Join Date:</label>
          <span>{profile?.joinDate || "Not available"}</span>
        </div>
        <div className="profile-field">
          <label>Trainer:</label>
          <span>{profile?.trainerid ? `Trainer #${profile.trainerid}` : "Not assigned"}</span>
        </div>
        <div className="profile-field">
          <label>Role:</label>
          <span>{profile?.role}</span>
        </div>
      </div>
    </div>
  )

  const renderTrainers = () => (
    <div className="trainers-section">
      <div className="section-header">
        <h2>🏋️‍♂️ Available Trainers</h2>
        <p>Browse all trainers in the gym</p>
      </div>

      <div className="trainers-grid">
        {trainers.map((trainer) => (
          <div key={trainer.id} className="trainer-card">
            <div className="trainer-info">
              <h4>{trainer.username}</h4>
              <p>
                <strong>Specialty:</strong> {trainer.specialty || "N/A"}
              </p>
              <p>
                <strong>Phone:</strong> {trainer.phoneNumber || "N/A"}
              </p>
              <p>
                <strong>ID:</strong> {trainer.id}
              </p>
              {profile?.trainerid === trainer.id && (
                <div className="current-trainer-badge">Your Current Trainer</div>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  )

  if (!activeSection || activeSection === "dashboard") {
    return <div className="member-dashboard">{renderDashboard()}</div>
  }

  return (
    <div className="member-dashboard">
      {loading ? (
        <div className="loading">Loading...</div>
      ) : (
        <>
          {activeSection === "profile" && renderProfile()}
          {activeSection === "trainers" && renderTrainers()}
        </>
      )}

      {showProfileModal && (
        <ProfileEditModal
          member={profile}
          onClose={() => setShowProfileModal(false)}
          onSave={handleUpdateProfile}
        />
      )}
    </div>
  )
}

const ProfileEditModal = ({ member, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    username: member?.username || "",
    phoneNumber: member?.phoneNumber || "",
    gender: member?.gender || "",
    joinDate: member?.joinDate || "",
    password: "",
  })

  const handleSubmit = async (e) => {
    e.preventDefault()
    const updateData = { ...formData }
    if (!updateData.password) {
      delete updateData.password
    }
    onSave(updateData)
  }

  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value,
    })
  }

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>✏️ Edit My Profile</h3>
          <button onClick={onClose} className="close-btn">
            ✕
          </button>
        </div>

        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label>Username</label>
            <input type="text" name="username" value={formData.username} onChange={handleChange} required />
          </div>

          <div className="form-group">
            <label>Phone Number</label>
            <input type="text" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label>Gender</label>
            <select name="gender" value={formData.gender} onChange={handleChange}>
              <option value="">Select Gender</option>
              <option value="Male">Male</option>
              <option value="Female">Female</option>
            </select>
          </div>

          <div className="form-group">
            <label>Join Date</label>
            <input type="date" name="joinDate" value={formData.joinDate} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label>Password (leave blank to keep current)</label>
            <input type="password" name="password" value={formData.password} onChange={handleChange} />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Update Profile
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default MemberDashboard
