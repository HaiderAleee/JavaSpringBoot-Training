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

  // Get CSRF token
  async getCsrfTokenFromLoginPage() {
    try {
      const response = await fetch(`login`, {
        method: "GET",
        credentials: "include",
      })

      if (response.ok) {
        const html = await response.text()

        const metaMatch = html.match(/<meta name="_csrf" content="([^"]+)"/)
        if (metaMatch) {
          this.csrfToken = metaMatch[1]
          return metaMatch[1]
        }

        const inputMatch = html.match(/<input[^>]*name="_csrf"[^>]*value="([^"]+)"/)
        if (inputMatch) {
          this.csrfToken = inputMatch[1]
          return inputMatch[1]
        }

        const jsMatch = html.match(/var csrfToken = "([^"]+)"/)
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
    const headers = { "Content-Type": "application/json" }

    if (this.token) {
      headers.Authorization = `Bearer ${this.token}`
    }

    if (this.csrfToken) {
      headers["X-XSRF-TOKEN"] = this.csrfToken
    }

    return headers
  }

  async request(endpoint, options = {}) {
    const config = {
      headers: this.getHeaders(),
      credentials: "include",
      ...options,
    }

    try {
      const response = await fetch(endpoint, config)

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

    const response = await fetch(`login`, {
      method: "POST",
      body: formData,
      credentials: "include",
      headers,
    })

    if (!response.ok) {
      throw new Error("Login failed")
    }

    const data = await response.json()
    this.setToken(data.access_token)
    return data
  }

  async googleLogin() {
    window.location.href = `oauth2/authorization/google`
  }

  async completeProfile(profileData) {
    try {
      if (!this.csrfToken) {
        await this.getCsrfTokenFromLoginPage()
      }

      return await this.request("api/members/complete-profile", {
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

        return await this.updateMyProfile(updateData)
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

  // --------------------
  // Admin endpoints
  // --------------------
  async getAllAdmins() {
    await this.ensureCsrfToken()
    return this.request("api/admins")
  }

  async getAdminById(id) {
    await this.ensureCsrfToken()
    return this.request(`api/admins/${id}`)
  }

  async createAdmin(admin) {
    await this.ensureCsrfToken()
    return this.request("api/admins", {
      method: "POST",
      body: JSON.stringify(admin),
    })
  }

  async updateAdmin(id, admin) {
    await this.ensureCsrfToken()
    return this.request(`api/admins/${id}`, {
      method: "PUT",
      body: JSON.stringify(admin),
    })
  }

  async deleteAdmin(id) {
    await this.ensureCsrfToken()
    return this.request(`api/admins/${id}`, {
      method: "DELETE",
    })
  }

  // --------------------
  // Trainer endpoints
  // --------------------
  async getAllTrainers() {
    await this.ensureCsrfToken()
    return this.request("api/trainers")
  }

  async getTrainerById(id) {
    await this.ensureCsrfToken()
    return this.request(`api/trainers/${id}`)
  }

  async createTrainer(trainer) {
    await this.ensureCsrfToken()
    return this.request("api/trainers", {
      method: "POST",
      body: JSON.stringify(trainer),
    })
  }

  async updateTrainer(id, trainer) {
    await this.ensureCsrfToken()
    return this.request(`api/trainers/${id}`, {
      method: "PUT",
      body: JSON.stringify(trainer),
    })
  }

  async deleteTrainer(id) {
    await this.ensureCsrfToken()
    return this.request(`api/trainers/${id}`, {
      method: "DELETE",
    })
  }

  async trainerExists(trainerId) {
    try {
      await this.getTrainerById(trainerId)
      return true
    } catch {
      return false
    }
  }

  // --------------------
  // Member endpoints
  // --------------------
  async getAllMembers() {
    await this.ensureCsrfToken()
    return this.request("api/members")
  }

  async getMemberById(id) {
    await this.ensureCsrfToken()
    return this.request(`api/members/${id}`)
  }

  async createMember(member) {
    await this.ensureCsrfToken()
    return this.request("api/members", {
      method: "POST",
      body: JSON.stringify(member),
    })
  }

  // Member updating own profile
  async updateMyProfile(member) {
    await this.ensureCsrfToken()
    return this.request(`api/members/me`, {
      method: "PUT",
      body: JSON.stringify(member),
    })
  }

  // Admin updating member by id
  async updateMemberByAdmin(id, member) {
    await this.ensureCsrfToken()
    return this.request(`api/members/${id}`, {
      method: "PUT",
      body: JSON.stringify(member),
    })
  }

  async deleteMember(id) {
    await this.ensureCsrfToken()
    return this.request(`api/members/${id}`, {
      method: "DELETE",
    })
  }

  async getMembersByTrainerId(trainerId) {
    await this.ensureCsrfToken()
    return this.request(`api/members/by-trainer/${trainerId}`)
  }

  async getMyProfile() {
    await this.ensureCsrfToken()
    return this.request("api/members/me")
  }

  // --------------------
  // Current user profile
  // --------------------
  async getCurrentUserProfile() {
    const token = this.token
    if (!token) throw new Error("No token available")

    const payload = JSON.parse(atob(token.split(".")[1]))
    const role = payload.role

    if (role === "ROLE_MEMBER") {
      return this.request("api/members/me")
    } else if (role === "ROLE_TRAINER") {
      const trainers = await this.getAllTrainers()
      return trainers.find((t) => t.username === payload.sub)
    } else if (role === "ROLE_ADMIN") {
      const admins = await this.getAllAdmins()
      return admins.find((a) => a.username === payload.sub)
    }
  }

  async updateCurrentUserProfile(profileData) {
    const token = this.token
    if (!token) throw new Error("No token available")

    const payload = JSON.parse(atob(token.split(".")[1]))
    const role = payload.role

    if (role === "ROLE_MEMBER") {
      return this.updateMyProfile(profileData)
    } else if (role === "ROLE_TRAINER") {
      const trainers = await this.getAllTrainers()
      const trainer = trainers.find((t) => t.username === payload.sub)
      return this.updateTrainer(trainer.id, profileData)
    }
  }
}

export default new ApiService()
