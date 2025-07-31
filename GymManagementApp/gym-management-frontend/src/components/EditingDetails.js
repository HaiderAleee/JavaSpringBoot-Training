import { useState } from "react"
import apiService from "./apiService" 


const handleChange = (e, setFormData) => {
  setFormData((prevState) => ({
    ...prevState,
    [e.target.name]: e.target.value,
  }))
}

const AdminEditingExample = () => {
  const [activeSection, setActiveSection] = useState("") 
  const [showModal, setShowModal] = useState(false)
  const [modalType, setModalType] = useState("") 
  const [editingItem, setEditingItem] = useState(null)

  const handleEdit = (item) => {
    setEditingItem(item) 
    setModalType(activeSection.slice(0, -1)) 
    setShowModal(true)
  }

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
            case "admin":
              await apiService.updateAdmin(item.id, formData)
              break
            case "trainer":
              await apiService.updateTrainer(item.id, formData)
              break
            case "member":
              await apiService.updateMember(item.id, formData)
              break
          }
        } else {
          switch (type) {
            case "admin":
              await apiService.createAdmin(formData)
              break
            case "trainer":
              await apiService.createTrainer(formData)
              break
            case "member":
              await apiService.createMember(formData)
              break
          }
        }
        onSave() 
      } catch (error) {
        console.error("Error saving:", error)
      }
    }

    return (
      <div className="modal-overlay">
        <div className="modal">
          <form onSubmit={handleSubmit}>
            <input name="username" value={formData.username} onChange={(e) => handleChange(e, setFormData)} />
            {type === "trainer" && (
              <>
                <input name="specialty" value={formData.specialty} onChange={(e) => handleChange(e, setFormData)} />
                <input name="salary" value={formData.salary} onChange={(e) => handleChange(e, setFormData)} />
              </>
            )}
          </form>
        </div>
      </div>
    )
  }

}

const TrainerEditingExample = () => {
  const MemberEditModal = ({ member, onClose, onSave }) => {
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


    return (
      <div className="modal-overlay">
        <form onSubmit={handleSubmit}>
          <input name="username" value={formData.username} onChange={(e) => handleChange(e, setFormData)} />
          <input name="phoneNumber" value={formData.phoneNumber} onChange={(e) => handleChange(e, setFormData)} />
          <select name="gender" value={formData.gender} onChange={(e) => handleChange(e, setFormData)}>
            <option value="Male">Male</option>
            <option value="Female">Female</option>
          </select>
          <input name="joinDate" type="date" value={formData.joinDate} onChange={(e) => handleChange(e, setFormData)} />
          <input
            name="trainerid"
            type="number"
            value={formData.trainerid}
            onChange={(e) => handleChange(e, setFormData)}
          />
        </form>
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

    return (
      <div className="modal-overlay">
        <form onSubmit={handleSubmit}>
          <input name="username" value={formData.username} onChange={(e) => handleChange(e, setFormData)} />
          <input name="phoneNumber" value={formData.phoneNumber} onChange={(e) => handleChange(e, setFormData)} />
          <input name="specialty" value={formData.specialty} onChange={(e) => handleChange(e, setFormData)} />
          <input name="password" type="password" placeholder="Leave blank to keep current" />
        </form>
      </div>
    )
  }

}

const MemberEditingExample = () => {
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

    return (
      <div className="modal-overlay">
        <form onSubmit={handleSubmit}>
          <input name="username" value={formData.username} onChange={(e) => handleChange(e, setFormData)} />
          <input name="phoneNumber" value={formData.phoneNumber} onChange={(e) => handleChange(e, setFormData)} />
          <select name="gender" value={formData.gender} onChange={(e) => handleChange(e, setFormData)}>
            <option value="">Select Gender</option>
            <option value="Male">Male</option>
            <option value="Female">Female</option>
          </select>
          <input name="joinDate" type="date" value={formData.joinDate} onChange={(e) => handleChange(e, setFormData)} />
          <input name="password" type="password" placeholder="Leave blank to keep current" />
        </form>
      </div>
    )
  }


}
