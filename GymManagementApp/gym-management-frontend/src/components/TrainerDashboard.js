"use client"

import { useState, useEffect } from "react"
import { useAuth } from "../context/AuthContext"
import apiService from "../services/api"
import "./TrainerDashboard.css"

const TrainerDashboard = ({ activeSection, onSectionChange }) => {
  const { user } = useAuth()
  const [myMembers, setMyMembers] = useState([])
  const [allTrainers, setAllTrainers] = useState([])
  const [allMembers, setAllMembers] = useState([])
  const [currentTrainer, setCurrentTrainer] = useState(null)
  const [loading, setLoading] = useState(false)
  const [showEditModal, setShowEditModal] = useState(false)
  const [editingMember, setEditingMember] = useState(null)
  const [showProfileModal, setShowProfileModal] = useState(false)

  useEffect(() => {
    loadCurrentTrainer()
  }, [])

  useEffect(() => {
    if (currentTrainer) {
      loadData()
    }
  }, [activeSection, currentTrainer])

  const loadCurrentTrainer = async () => {
    try {
      const trainers = await apiService.getAllTrainers()
      const trainer = trainers.find((t) => t.username === user?.username)
      setCurrentTrainer(trainer)
    } catch (error) {
      console.error("Error loading current trainer:", error)
    }
  }

  const loadData = async () => {
    if (!activeSection || activeSection === "dashboard") return

    setLoading(true)
    try {
      switch (activeSection) {
        case "my-members":
          const allMembersData = await apiService.getAllMembers()
          const myMembersData = allMembersData.filter((member) => member.trainerid === currentTrainer?.id)
          setMyMembers(myMembersData)
          setAllMembers(allMembersData)
          break
        case "all-trainers":
          const trainersData = await apiService.getAllTrainers()
          setAllTrainers(trainersData)
          break
      }
    } catch (error) {
      console.error("Error loading data:", error)
    } finally {
      setLoading(false)
    }
  }

  const handleEditMember = (member) => {
    setEditingMember(member)
    setShowEditModal(true)
  }

  const handleRemoveMember = async (memberId) => {
    if (!window.confirm("Are you sure you want to remove this member from your training?")) return

    try {
      const member = myMembers.find((m) => m.id === memberId)
      await apiService.updateMember(memberId, { ...member, trainerid: 0 })
      loadData()
    } catch (error) {
      console.error("Error removing member:", error)
    }
  }

  const handleUpdateProfile = async (profileData) => {
    try {
      await apiService.updateTrainer(currentTrainer.id, profileData)
      setCurrentTrainer({ ...currentTrainer, ...profileData })
      setShowProfileModal(false)
    } catch (error) {
      console.error("Error updating profile:", error)
    }
  }

  const renderDashboard = () => (
    <div className="dashboard-overview">
      <div className="welcome-section">
        <h2>🏋️‍♂️ Trainer Dashboard</h2>
        <p>Welcome back, {user?.username}!</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-icon">👥</div>
          <div className="stat-info">
            <h3>{myMembers.length}</h3>
            <p>My Members</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">💪</div>
          <div className="stat-info">
            <h3>{currentTrainer?.specialty || "N/A"}</h3>
            <p>Specialty</p>
          </div>
        </div>

        <div className="stat-card">
          <div className="stat-icon">📞</div>
          <div className="stat-info">
            <h3>{currentTrainer?.phoneNumber || "N/A"}</h3>
            <p>Phone Number</p>
          </div>
        </div>
      </div>

      <div className="quick-actions">
        <h3>Quick Actions</h3>
        <div className="action-buttons">
          <button onClick={() => onSectionChange("my-members")} className="action-btn">
            Manage My Members
          </button>
          <button onClick={() => onSectionChange("all-trainers")} className="action-btn">
            View All Trainers
          </button>
          <button onClick={() => setShowProfileModal(true)} className="action-btn">
            Edit My Profile
          </button>
        </div>
      </div>
    </div>
  )

  const renderMyMembers = () => (
    <div className="members-section">
      <div className="section-header">
        <h2>👥 My Members</h2>
        <p>Members currently training under you</p>
      </div>

      {myMembers.length === 0 ? (
        <div className="empty-state">
          <div className="empty-icon">👥</div>
          <h3>No members assigned yet</h3>
          <p>Members will appear here when they are assigned to you</p>
        </div>
      ) : (
        <div className="members-grid">
          {myMembers.map((member) => (
            <div key={member.id} className="member-card">
              <div className="member-info">
                <h4>{member.username}</h4>
                <p>
                  <strong>Phone:</strong> {member.phoneNumber || "N/A"}
                </p>
                <p>
                  <strong>Gender:</strong> {member.gender || "N/A"}
                </p>
                <p>
                  <strong>Join Date:</strong> {member.joinDate || "N/A"}
                </p>
              </div>
              <div className="member-actions">
                <button onClick={() => handleEditMember(member)} className="btn btn-edit">
                  ✏️ Edit
                </button>
                <button onClick={() => handleRemoveMember(member.id)} className="btn btn-remove">
                  ❌ Remove
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )

  const renderAllTrainers = () => (
    <div className="trainers-section">
      <div className="section-header">
        <h2>🏋️‍♂️ All Trainers</h2>
        <p>View all trainers in the gym</p>
      </div>

      <div className="trainers-grid">
        {allTrainers.map((trainer) => (
          <div key={trainer.id} className={`trainer-card ${trainer.id === currentTrainer?.id ? "current" : ""}`}>
            <div className="trainer-info">
              <h4>
                {trainer.username} {trainer.id === currentTrainer?.id && "(You)"}
              </h4>
              <p>
                <strong>Specialty:</strong> {trainer.specialty || "N/A"}
              </p>
              <p>
                <strong>Phone:</strong> {trainer.phoneNumber || "N/A"}
              </p>
              <p>
                <strong>ID:</strong> {trainer.id}
              </p>
            </div>
          </div>
        ))}
      </div>
    </div>
  )

  if (!activeSection || activeSection === "dashboard") {
    return <div className="trainer-dashboard">{renderDashboard()}</div>
  }

  return (
    <div className="trainer-dashboard">
      {loading ? (
        <div className="loading">Loading...</div>
      ) : (
        <>
          {activeSection === "my-members" && renderMyMembers()}
          {activeSection === "all-trainers" && renderAllTrainers()}
          {activeSection === "profile" && (
            <ProfileSection trainer={currentTrainer} onEdit={() => setShowProfileModal(true)} />
          )}
        </>
      )}

      {showEditModal && (
        <MemberEditModal
          member={editingMember}
          allMembers={allMembers}
          currentTrainerId={currentTrainer?.id}
          onClose={() => setShowEditModal(false)}
          onSave={() => {
            setShowEditModal(false)
            loadData()
          }}
        />
      )}

      {showProfileModal && (
        <ProfileEditModal
          trainer={currentTrainer}
          onClose={() => setShowProfileModal(false)}
          onSave={handleUpdateProfile}
        />
      )}
    </div>
  )
}

const ProfileSection = ({ trainer, onEdit }) => (
  <div className="profile-section">
    <div className="section-header">
      <h2>👤 My Profile</h2>
      <button onClick={onEdit} className="btn btn-primary">
        ✏️ Edit Profile
      </button>
    </div>

    <div className="profile-card">
      <div className="profile-field">
        <label>Username:</label>
        <span>{trainer?.username}</span>
      </div>
      <div className="profile-field">
        <label>Phone Number:</label>
        <span>{trainer?.phoneNumber || "Not provided"}</span>
      </div>
      <div className="profile-field">
        <label>Specialty:</label>
        <span>{trainer?.specialty || "Not specified"}</span>
      </div>
      <div className="profile-field">
        <label>Role:</label>
        <span>{trainer?.role}</span>
      </div>
    </div>
  </div>
)

const MemberEditModal = ({ member, allMembers, currentTrainerId, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    ...member,
    trainerid: member.trainerid || 0,
  })

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      await apiService.updateMember(member.id, formData)
      onSave()
    } catch (error) {
      console.error("Error updating member:", error)
    }
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
          <h3>✏️ Edit Member</h3>
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
            <input type="text" name="phoneNumber" value={formData.phoneNumber || ""} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label>Gender</label>
            <select name="gender" value={formData.gender || ""} onChange={handleChange}>
              <option value="">Select Gender</option>
              <option value="Male">Male</option>
              <option value="Female">Female</option>
            </select>
          </div>

          <div className="form-group">
            <label>Join Date</label>
            <input type="date" name="joinDate" value={formData.joinDate || ""} onChange={handleChange} />
          </div>

          <div className="form-group">
            <label>Trainer ID (0 for no trainer)</label>
            <input type="number" name="trainerid" value={formData.trainerid} onChange={handleChange} />
          </div>

          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn btn-secondary">
              Cancel
            </button>
            <button type="submit" className="btn btn-primary">
              Update Member
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}

const ProfileEditModal = ({ trainer, onClose, onSave }) => {
  const [formData, setFormData] = useState({
    username: trainer?.username || "",
    phoneNumber: trainer?.phoneNumber || "",
    specialty: trainer?.specialty || "",
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
            <label>Specialty</label>
            <input type="text" name="specialty" value={formData.specialty} onChange={handleChange} />
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

export default TrainerDashboard
