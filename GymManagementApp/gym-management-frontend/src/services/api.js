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

  // Admin endpoints
  async getAllAdmins() {
    const data = await this.request("/admins")
    return data
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
      method: "DELETE" })
  }

  // Trainer endpoints
  async getAllTrainers() {
    const data = await this.request("/trainers")
    return data
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
      method: "DELETE" })
  }

  // Member endpoints
  async getAllMembers() {
    const data = await this.request("/members")
    return data
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
      method: "DELETE" })
  }

  async getMembersByTrainerId(trainerId) {
    return this.request(`/members/by-trainer/${trainerId}`)
  }

  async getMyProfile() {
    return this.request("/members/me")
  }

  // Get current user profile (works for any role)
  async getCurrentUserProfile() {
    const token = this.token
    if (!token) throw new Error("No token available")

    const payload = JSON.parse(atob(token.split(".")[1]))
    const role = payload.role

    if (role === "ROLE_MEMBER") {
      return this.request("/members/me")
    } else if (role === "ROLE_TRAINER") {
      const trainers = await this.getAllTrainers()
      return trainers.find((t) => t.username === payload.sub)
    } else if (role === "ROLE_ADMIN") {
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
