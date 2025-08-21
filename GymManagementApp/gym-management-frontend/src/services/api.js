class ApiService {
  constructor(base = "") {
    this.base = base.replace(/\/+$/, "");
    this.token = localStorage.getItem("token");
    this.csrfToken = null;
  }

  setToken(token) {
    this.token = token;
    localStorage.setItem("token", token);
  }

  removeToken() {
    this.token = null;
    localStorage.removeItem("token");
  }

  async getCsrfTokenFromLoginPage() {
    try {
      const res = await fetch(`${this.base}/login`, { method: "GET", credentials: "include", cache: "no-store" });
      if (!res.ok) return null;
      const html = await res.text();
      const patterns = [
        /<meta name="_csrf" content="([^"]+)"/,
        /<input[^>]*name="_csrf"[^>]*value="([^"]+)"/,
        /var csrfToken = "([^"]+)"/
      ];
      for (const p of patterns) {
        const m = html.match(p);
        if (m) {
          this.csrfToken = m[1];
          return m[1];
        }
      }
      return null;
    } catch {
      return null;
    }
  }

  async ensureCsrfToken() {
    if (!this.csrfToken) await this.getCsrfTokenFromLoginPage();
  }

  buildUrl(endpoint) {
    if (/^https?:\/\//i.test(endpoint)) return endpoint;
    const e = endpoint.replace(/^\/+/, "");
    return this.base ? `${this.base}/${e}` : `/${e}`;
  }

  buildHeaders(extra = {}, hasJsonBody = false) {
    const h = { ...extra };
    if (hasJsonBody) h["Content-Type"] = "application/json";
    if (this.token) h.Authorization = `Bearer ${this.token}`;
    if (this.csrfToken) h["X-XSRF-TOKEN"] = this.csrfToken;
    return h;
  }

  async request(endpoint, options = {}) {
    const method = (options.method || "GET").toUpperCase();
    if (method !== "GET") await this.ensureCsrfToken();

    const isStringBody = typeof options.body === "string";
    const hasJsonBody = isStringBody && options.body.trim().startsWith("{");

    const res = await fetch(this.buildUrl(endpoint), {
      credentials: "include",
      ...options,
      headers: this.buildHeaders(options.headers || {}, hasJsonBody),
    });

    if (!res.ok) {
      const txt = await res.text().catch(() => "");
      throw new Error(`HTTP ${res.status}${txt ? `: ${txt}` : ""}`);
    }

    const ct = res.headers.get("content-type") || "";
    return ct.includes("application/json") ? res.json() : res;
  }

  async login(username, password) {
    await this.ensureCsrfToken();
    const form = new FormData();
    form.append("username", username);
    form.append("password", password);
    if (this.csrfToken) form.append("_csrf", this.csrfToken);

    const res = await fetch(this.buildUrl("login"), {
      method: "POST",
      body: form,
      credentials: "include",
      headers: this.csrfToken ? { "X-XSRF-TOKEN": this.csrfToken } : {},
    });

    if (!res.ok) throw new Error("Login failed");
    const data = await res.json();
    this.setToken(data.access_token);
    return data;
  }

  googleLogin() {
    window.location.href = this.buildUrl("oauth2/authorization/google");
  }

  _getAll(endpoint) {
    return this.request(endpoint);
  }

  _getById(endpoint, id) {
    return this.request(`${endpoint}/${id}`);
  }

  _create(endpoint, data) {
    return this.request(endpoint, { method: "POST", body: JSON.stringify(data), headers: { "Content-Type": "application/json" } });
  }

  _update(endpoint, id, data) {
    return this.request(`${endpoint}/${id}`, { method: "PUT", body: JSON.stringify(data), headers: { "Content-Type": "application/json" } });
  }

  _delete(endpoint, id) {
    return this.request(`${endpoint}/${id}`, { method: "DELETE" });
  }

  getAllAdmins() { return this._getAll("api/admins"); }
  getAdminById(id) { return this._getById("api/admins", id); }
  createAdmin(admin) { return this._create("api/admins", admin); }
  updateAdmin(id, admin) { return this._update("api/admins", id, admin); }
  deleteAdmin(id) { return this._delete("api/admins", id); }

  getAllTrainers() { return this._getAll("api/trainers"); }
  getTrainerById(id) { return this._getById("api/trainers", id); }
  createTrainer(trainer) { return this._create("api/trainers", trainer); }
  updateTrainer(id, trainer) { return this._update("api/trainers", id, trainer); }
  deleteTrainer(id) { return this._delete("api/trainers", id); }

  trainerExists(trainerId) {
    return this.getTrainerById(trainerId).then(() => true).catch(() => false);
  }

  getAllMembers() { return this._getAll("api/members"); }
  getMemberById(id) { return this._getById("api/members", id); }
  createMember(member) { return this._create("api/members", member); }

  updateMyProfile(member) {
    return this.request("api/members/me", { method: "PUT", body: JSON.stringify(member), headers: { "Content-Type": "application/json" } });
  }

  updateMemberByAdmin(id, member) { return this._update("api/members", id, member); }
  deleteMember(id) { return this._delete("api/members", id); }
  getMembersByTrainerId(trainerId) { return this._getById("api/members/by-trainer", trainerId); }
  getMyProfile() { return this._getAll("api/members/me"); }

  getJwtPayload() {
    if (!this.token) throw new Error("No token available");
    const [, payload] = this.token.split(".");
    return JSON.parse(atob(payload));
  }

  async getCurrentUserProfile() {
    const { role, sub } = this.getJwtPayload();
    if (role === "ROLE_MEMBER") return this.getMyProfile();
    if (role === "ROLE_TRAINER") {
      const trainers = await this.getAllTrainers();
      return trainers.find(t => t.username === sub) || null;
    }
    if (role === "ROLE_ADMIN") {
      const admins = await this.getAllAdmins();
      return admins.find(a => a.username === sub) || null;
    }
    return null;
    }

  async updateCurrentUserProfile(profileData) {
    const { role, sub } = this.getJwtPayload();
    if (role === "ROLE_MEMBER") return this.updateMyProfile(profileData);
    if (role === "ROLE_TRAINER") {
      const trainers = await this.getAllTrainers();
      const me = trainers.find(t => t.username === sub);
      if (!me) throw new Error("Trainer not found");
      return this.updateTrainer(me.id, profileData);
    }
    return null;
  }
}

export default new ApiService();
