"use client"

import { useState, useEffect } from "react"
import apiService from "../services/api"
import "./AdminPanel.css"

const AdminPanel = ({ activeSection, onSectionChange }) => {
  const [admins, setAdmins] = useState([])
  const [trainers, setTrainers] = useState([])
  const [members, setMembers] = useState([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [modalType, setModalType] = useState("")
  const [editingItem, setEditingItem] = useState(null)
  const [stats, setStats] = useState({ admins: 0, trainers: 0, members: 0 })

  useEffect(() => {
    loadStats()
  }, [])

  useEffect(() => {
    loadData()
  }, [activeSection])

  const loadStats = async () => {
    try {
      const [admins, trainers, members] = await Promise.all([
        apiService.getAllAdmins(),
        apiService.getAllTrainers(),
        apiService.getAllMembers(),
      ])
      setStats({
        admins: admins.length,
        trainers: trainers.length,
        members: members.length,
      })
    } catch (error) {
      console.error("Error loading stats:", error)
    }
  }

  const loadData = async () => {
    if (!activeSection || activeSection === "dashboard") return
    setLoading(true)
    try {
      switch (activeSection) {
        case "admins":
          const admins = await apiService.getAllAdmins()
          setAdmins(admins)
          break
        case "trainers":
          const trainers = await apiService.getAllTrainers()
          setTrainers(trainers)
          break
        case "members":
          const members = await apiService.getAllMembers()
          setMembers(members)
          break
      }
    } catch (error) {
      console.error("Error loading data:", error)
    } finally {
      setLoading(false)
    }
  }

  const handleAdd = () => {
    setEditingItem(null)
    setModalType(activeSection.slice(0, -1))
    setShowModal(true)
  }

  const handleEdit = (item) => {
    setEditingItem(item)
    setModalType(activeSection.slice(0, -1))
    setShowModal(true)
  }

  const handleDelete = async (id) => {
    if (!window.confirm("Are you sure you want to delete this item?")) return
    try {
      switch (activeSection) {
        case "admins":
          await apiService.deleteAdmin(id)
          break
        case "trainers":
          await apiService.deleteTrainer(id)
          break
        case "members":
          await apiService.deleteMember(id)
          break
      }
      loadData()
      loadStats()
    } catch (error) {
      console.error("Error deleting item:", error)
    }
  }

  const renderDashboard = () => (
    <div className="dashboard-overview">
      <div className="welcome-section">
        <h2>🎯 Admin Dashboard</h2>
        <p>Manage your gym's operations from here</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card admin">
          <div className="stat-icon">👨‍💼</div>
          <div className="stat-info">
            <h3>{stats.admins}</h3>
            <p>Total Admins</p>
          </div>
        </div>

        <div className="stat-card trainer">
          <div className="stat-icon">🏋️‍♂️</div>
          <div className="stat-info">
            <h3>{stats.trainers}</h3>
            <p>Total Trainers</p>
          </div>
        </div>

        <div className="stat-card member">
          <div className="stat-icon">👥</div>
          <div className="stat-info">
            <h3>{stats.members}</h3>
            <p>Total Members</p>
          </div>
        </div>
      </div>

      <div className="quick-actions">
        <h3>Quick Actions</h3>
        <div className="action-buttons">
          <button onClick={() => onSectionChange("admins")} className="action-btn admin">
            Manage Admins
          </button>
          <button onClick={() => onSectionChange("trainers")} className="action-btn trainer">
            Manage Trainers
          </button>
          <button onClick={() => onSectionChange("members")} className="action-btn member">
            Manage Members
          </button>
        </div>
      </div>
    </div>
  )

  const renderTable = () => {
    let data, columns

    switch (activeSection) {
      case "admins":
        data = admins
        columns = ["ID", "Username", "Role", "Actions"]
        break
      case "trainers":
        data = trainers
        columns = ["ID", "Username", "Phone", "Specialty", "Salary", "Actions"]
        break
      case "members":
        data = members
        columns = ["ID", "Username", "Phone", "Gender", "Join Date", "Trainer ID", "Actions"]
        break
      default:
        return null
    }

    return (
      <div className="table-section">
        <div className="section-header">
          <h2>
            {activeSection === "admins" && "👨‍💼 Manage Admins"}
            {activeSection === "trainers" && "🏋️‍♂️ Manage Trainers"}
            {activeSection === "members" && "👥 Manage Members"}
          </h2>
          <button onClick={handleAdd} className="btn btn-primary">
            ➕ Add New {activeSection.slice(0, -1)}
          </button>
        </div>

        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                {columns.map((col) => (
                  <th key={col}>{col}</th>
                ))}
              </tr>
            </thead>
            <tbody>
              {data.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>
                  <td>{item.username}</td>
                  {activeSection === "trainers" && (
                    <>
                      <td>{item.phoneNumber || "N/A"}</td>
                      <td>{item.specialty || "N/A"}</td>
                      <td>${item.salary || 0}</td>
                    </>
                  )}
                  {activeSection === "members" && (
                    <>
                      <td>{item.phoneNumber || "N/A"}</td>
                      <td>{item.gender || "N/A"}</td>
                      <td>{item.joinDate || "N/A"}</td>
                      <td>{item.trainerid || "No Trainer"}</td>
                    </>
                  )}
                  {activeSection === "admins" && <td>{item.role}</td>}
                  <td>
                    <div className="action-buttons">
                      <button onClick={() => handleEdit(item)} className="btn btn-edit">✏️ Edit</button>
                      <button onClick={() => handleDelete(item.id)} className="btn btn-delete">🗑️ Delete</button>
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    )
  }

  return (
    <div className="admin-panel">
      {!activeSection || activeSection === "dashboard"
        ? renderDashboard()
        : loading
        ? <div className="loading">Loading...</div>
        : renderTable()}

      {showModal && (
        <Modal
          type={modalType}
          item={editingItem}
          onClose={() => setShowModal(false)}
          onSave={() => {
            setShowModal(false)
            loadData()
            loadStats()
          }}
        />
      )}
    </div>
  )
}

// 🧩 Modal component stays unchanged (already works fine)
const Modal = ({ type, item, onClose, onSave }) => {
  const [formData, setFormData] = useState(
    item || {
      username: "",
      password: "",
      phoneNumber: "",
      specialty: "",
      salary: "",
      gender: "",
      joinDate: "",
      trainerid: "",
    },
  )

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      if (item) {
        switch (type) {
          case "admin": await apiService.updateAdmin(item.id, formData); break
          case "trainer": await apiService.updateTrainer(item.id, formData); break
          case "member": await apiService.updateMember(item.id, formData); break
        }
      } else {
        switch (type) {
          case "admin": await apiService.createAdmin(formData); break
          case "trainer": await apiService.createTrainer(formData); break
          case "member": await apiService.createMember(formData); break
        }
      }
      onSave()
    } catch (error) {
      console.error("Error saving:", error)
    }
  }

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value })
  }

  return (
    <div className="modal-overlay">
      <div className="modal">
        <div className="modal-header">
          <h3>{item ? "✏️ Edit" : "➕ Add"} {type}</h3>
          <button onClick={onClose} className="close-btn">✕</button>
        </div>
        <form onSubmit={handleSubmit} className="modal-form">
          <div className="form-group">
            <label>Username</label>
            <input type="text" name="username" value={formData.username} onChange={handleChange} required />
          </div>
          <div className="form-group">
            <label>Password</label>
            <input type="password" name="password" value={formData.password} onChange={handleChange}
              required={!item} placeholder={item ? "Leave blank to keep current password" : ""} />
          </div>
          {(type === "trainer" || type === "member") && (
            <div className="form-group">
              <label>Phone Number</label>
              <input type="text" name="phoneNumber" value={formData.phoneNumber} onChange={handleChange} />
            </div>
          )}
          {type === "trainer" && (
            <>
              <div className="form-group">
                <label>Specialty</label>
                <input type="text" name="specialty" value={formData.specialty} onChange={handleChange} />
              </div>
              <div className="form-group">
                <label>Salary</label>
                <input type="number" name="salary" value={formData.salary} onChange={handleChange} step="0.01" />
              </div>
            </>
          )}
          {type === "member" && (
            <>
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
                <label>Trainer ID (0 for no trainer)</label>
                <input type="number" name="trainerid" value={formData.trainerid} onChange={handleChange} />
              </div>
            </>
          )}
          <div className="modal-actions">
            <button type="button" onClick={onClose} className="btn btn-secondary">Cancel</button>
            <button type="submit" className="btn btn-primary">{item ? "Update" : "Create"}</button>
          </div>
        </form>
      </div>
    </div>
  )
}

export default AdminPanel
