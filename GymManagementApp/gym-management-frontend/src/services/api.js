const API_BASE_URL = "http://localhost:8080"

class ApiService {
  constructor() {
    this.token = localStorage.getItem("token")
    this.csrfToken = null
  }

  setToken(token) {
    this.token = token
    localStorage.setItem("token", token)
  }

  removeToken() {
    this.token = null
    localStorage.removeItem("token")
  }

  // Get CSRF token from login page HTML
  async getCsrfTokenFromLoginPage() {
    try {
      const response = await fetch(`${API_BASE_URL}/login`, {
        method: "GET",
        credentials: "include",
      })

      if (response.ok) {
        const html = await response.text()
        
        // Extract CSRF token from meta tag
        const metaMatch = html.match(/<meta name="_csrf" content="([^"]+)"/);
        if (metaMatch) {
          this.csrfToken = metaMatch[1]
          return metaMatch[1]
        }

        const inputMatch = html.match(/<input[^>]*name="_csrf"[^>]*value="([^"]+)"/);
        if (inputMatch) {
          this.csrfToken = inputMatch[1]
          return inputMatch[1]
        }

        const jsMatch = html.match(/var csrfToken = "([^"]+)"/);
        if (jsMatch) {
          this.csrfToken = jsMatch[1]
          return jsMatch[1]
        }

        console.warn("CSRF token not found in login page HTML")
      }
    } catch (error) {
      console.error("Failed to get CSRF token from login page:", error)
    }
    return null
  }

  getHeaders() {
    const headers = {
      "Content-Type": "application/json",
    }

    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
    }

    if (this.csrfToken) {
      headers["X-XSRF-TOKEN"] = this.csrfToken
    }

    return headers
  }

  async request(endpoint, options = {}) {
    const url = `${API_BASE_URL}${endpoint}`
    const config = {
      headers: this.getHeaders(),
      credentials: "include", 
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

  async login(username, password) {
    await this.getCsrfTokenFromLoginPage()

    const formData = new FormData()
    formData.append("username", username)
    formData.append("password", password)

    if (this.csrfToken) {
      formData.append("_csrf", this.csrfToken)
    }

    const headers = {}
    if (this.csrfToken) {
      headers["X-XSRF-TOKEN"] = this.csrfToken
    }

    const response = await fetch(`${API_BASE_URL}/login`, {
      method: "POST",
      body: formData,
      credentials: "include",
      headers: headers,
    })

    if (!response.ok) {
      throw new Error("Login failed")
    }

    const data = await response.json()
    this.setToken(data.access_token)
    return data
  }

  async googleLogin() {
    window.location.href = `${API_BASE_URL}/oauth2/authorization/google`
  }

  async completeProfile(profileData) {
    try {
      if (!this.csrfToken) {
        await this.getCsrfTokenFromLoginPage()
      }

      return await this.request("/members/complete-profile", {
        method: "POST",
        body: JSON.stringify(profileData),
      })
    } catch (error) {
      console.log("Complete profile endpoint not found, trying to update current user profile...")

      try {
        const currentProfile = await this.getMyProfile()

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

  async ensureCsrfToken() {
    if (!this.csrfToken) {
      await this.getCsrfTokenFromLoginPage()
    }
  }

  // Admin endpoints
  async getAllAdmins() {
    await this.ensureCsrfToken()
    return this.request("/admins")
  }

  async getAdminById(id) {
    await this.ensureCsrfToken()
    return this.request(`/admins/${id}`)
  }

  async createAdmin(admin) {
    await this.ensureCsrfToken()
    return this.request("/admins", {
      method: "POST",
      body: JSON.stringify(admin),
    })
  }

  async updateAdmin(id, admin) {
    await this.ensureCsrfToken()
    return this.request(`/admins/${id}`, {
      method: "PUT",
      body: JSON.stringify(admin),
    })
  }

  async deleteAdmin(id) {
    await this.ensureCsrfToken()
    return this.request(`/admins/${id}`, {
      method: "DELETE",
    })
  }

  // Trainer endpoints
  async getAllTrainers() {
    await this.ensureCsrfToken()
    return this.request("/trainers")
  }

  async getTrainerById(id) {
    await this.ensureCsrfToken()
    return this.request(`/trainers/${id}`)
  }

  async createTrainer(trainer) {
    await this.ensureCsrfToken()
    return this.request("/trainers", {
      method: "POST",
      body: JSON.stringify(trainer),
    })
  }

  async updateTrainer(id, trainer) {
    await this.ensureCsrfToken()
    return this.request(`/trainers/${id}`, {
      method: "PUT",
      body: JSON.stringify(trainer),
    })
  }

  async deleteTrainer(id) {
    await this.ensureCsrfToken()
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
    await this.ensureCsrfToken()
    return this.request("/members")
  }

  async getMemberById(id) {
    await this.ensureCsrfToken()
    return this.request(`/members/${id}`)
  }

  async createMember(member) {
    await this.ensureCsrfToken()
    return this.request("/members", {
      method: "POST",
      body: JSON.stringify(member),
    })
  }

  async updateMember(id, member) {
    await this.ensureCsrfToken()
    return this.request(`/members/${id}`, {
      method: "PUT",
      body: JSON.stringify(member),
    })
  }

  async deleteMember(id) {
    await this.ensureCsrfToken()
    return this.request(`/members/${id}`, {
      method: "DELETE",
    })
  }

  async getMembersByTrainerId(trainerId) {
    await this.ensureCsrfToken()
    return this.request(`/members/by-trainer/${trainerId}`)
  }

  async getMyProfile() {
    await this.ensureCsrfToken()
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
