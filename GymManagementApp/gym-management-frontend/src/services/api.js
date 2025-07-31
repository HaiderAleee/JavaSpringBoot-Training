const API_BASE_URL = "http://localhost:8080"

class ApiService {
  constructor() {
    this.token = localStorage.getItem("token")
  }

  setToken(token) {
    this.token = token
    localStorage.setItem("token", token)
  }

  removeToken() {
    this.token = null
    localStorage.removeItem("token")
  }

  getHeaders() {
    const headers = {
      "Content-Type": "application/json",
    }

    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
    }

    return headers
  }

  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`
    const config = {
      headers: this.getHeaders(),
      ...options,
    }

    try {
      const response = await fetch(url, config)

      if (!response.ok) {
        const errorText = await response.text()
        console.error(`API Error: ${response.status} - ${errorText}`)
        throw new Error(`HTTP error! status: ${response.status}`)
      }

      const contentType = response.headers.get("content-type")
      if (contentType && contentType.includes("application/json")) {
        return await response.json()
      }

      return response
    } catch (error) {
      console.error("API request failed:", error)
      throw error
    }
  }

  // Authentication
  async login(username, password) {
    const formData = new FormData()
    formData.append("username", username)
    formData.append("password", password)

    const response = await fetch(`${API_BASE_URL}/login`, {
      method: "POST",
      body: formData,
    })

    if (!response.ok) {
      throw new Error("Login failed")
    }

    const data = await response.json()
    this.setToken(data.access_token)
    return data
  }

  // Google OAuth login
  async googleLogin() {
    // Redirect to Google OAuth endpoint
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`
  }

  // Complete profile for new Google users
  async completeProfile(profileData) {
    try {
      // First, try the dedicated complete-profile endpoint
      return await this.request("/members/complete-profile", {
        method: "POST",
        body: JSON.stringify(profileData),
      })
    } catch (error) {
      console.log("Complete profile endpoint not found, trying to update current user profile...")

      // Fallback: Get current user profile and update it
      try {
        const currentProfile = await this.getMyProfile()

        // Update the member with the new profile data
        const updateData = {
          ...currentProfile,
          phoneNumber: profileData.phoneNumber,
          gender: profileData.gender,
          trainerid: profileData.trainerId || profileData.trainerid || 0,
        }

        return await this.updateMember(currentProfile.id, updateData)
      } catch (fallbackError) {
        console.error("Fallback profile update failed:", fallbackError)
        throw fallbackError
      }
    }
  }

  // Admin endpoints
  async getAllAdmins() {
    return this.request("/admins")
  }

  async getAdminById(id) {
    return this.request(`/admins/${id}`)
  }

  async createAdmin(admin) {
    return this.request("/admins", {
      method: "POST",
      body: JSON.stringify(admin),
    })
  }

  async updateAdmin(id, admin) {
    return this.request(`/admins/${id}`, {
      method: "PUT",
      body: JSON.stringify(admin),
    })
  }

  async deleteAdmin(id) {
    return this.request(`/admins/${id}`, {
      method: "DELETE",
    })
  }

  // Trainer endpoints
  async getAllTrainers() {
    return this.request("/trainers")
  }

  async getTrainerById(id) {
    return this.request(`/trainers/${id}`)
  }

  async createTrainer(trainer) {
    return this.request("/trainers", {
      method: "POST",
      body: JSON.stringify(trainer),
    })
  }

  async updateTrainer(id, trainer) {
    return this.request(`/trainers/${id}`, {
      method: "PUT",
      body: JSON.stringify(trainer),
    })
  }

  async deleteTrainer(id) {
    return this.request(`/trainers/${id}`, {
      method: "DELETE",
    })
  }

  // Check if trainer exists
  async trainerExists(trainerId) {
    try {
      await this.getTrainerById(trainerId)
      return true
    } catch (error) {
      return false
    }
  }

  // Member endpoints
  async getAllMembers() {
    return this.request("/members")
  }

  async getMemberById(id) {
    return this.request(`/members/${id}`)
  }

  async createMember(member) {
    return this.request("/members", {
      method: "POST",
      body: JSON.stringify(member),
    })
  }

  async updateMember(id, member) {
    return this.request(`/members/${id}`, {
      method: "PUT",
      body: JSON.stringify(member),
    })
  }

  async deleteMember(id) {
    return this.request(`/members/${id}`, {
      method: "DELETE",
    })
  }

  async getMembersByTrainerId(trainerId) {
    return this.request(`/members/by-trainer/${trainerId}`)
  }

  async getMyProfile() {
    return this.request("/members/me")
  }

  // Get current user profile (works for any role)
  async getCurrentUserProfile() {
    // Try to get profile based on role
    const token = this.token
    if (!token) throw new Error("No token available")

    const payload = JSON.parse(atob(token.split(".")[1]))
    const role = payload.role

    if (role === "ROLE_MEMBER") {
      return this.request("/members/me")
    } else if (role === "ROLE_TRAINER") {
      // For trainers, we'll need to find them by username
      const trainers = await this.getAllTrainers()
      return trainers.find((t) => t.username === payload.sub)
    } else if (role === "ROLE_ADMIN") {
      // For admins, we'll need to find them by username
      const admins = await this.getAllAdmins()
      return admins.find((a) => a.username === payload.sub)
    }
  }

  // Update current user profile
  async updateCurrentUserProfile(profileData) {
    const token = this.token
    if (!token) throw new Error("No token available")

    const payload = JSON.parse(atob(token.split(".")[1]))
    const role = payload.role

    if (role === "ROLE_MEMBER") {
      const profile = await this.getMyProfile()
      return this.updateMember(profile.id, profileData)
    } else if (role === "ROLE_TRAINER") {
      const trainers = await this.getAllTrainers()
      const trainer = trainers.find((t) => t.username === payload.sub)
      return this.updateTrainer(trainer.id, profileData)
    }
  }
}

export default new ApiService()
