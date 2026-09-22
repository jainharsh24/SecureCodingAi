const api = {
  request: async (path, options = {}) => {
    const response = await fetch(path, {
      credentials: "same-origin",
      headers: {
        "Content-Type": "application/json",
        ...(options.headers || {})
      },
      ...options
    });

    if (response.status === 401 || response.status === 403) {
      throw new ApiError(
        "Your session has expired. Please sign in again.",
        response.status
      );
    }

    const text = await response.text();
    const body = text ? safeJson(text) : null;

    if (!response.ok) {
      throw new ApiError(
        body?.detail ||
        body?.message ||
        `Request failed (${response.status}).`,
        response.status
      );
    }

    return body;
  },

  login: (email, password) =>
    api.request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    }),

  register: (email, password) =>
    api.request("/api/auth/register", {
      method: "POST",
      body: JSON.stringify({ email, password })
    }),

  logout: () =>
    fetch("/logout", {
      method: "POST",
      credentials: "same-origin"
    }),

  challenges: () => api.request("/challenges"),

  challenge: (id) =>
    api.request(`/challenges/${encodeURIComponent(id)}`),

  submit: (challengeId, sourceCode) =>
    api.request(
      `/challenges/${encodeURIComponent(challengeId)}/submissions`,
      {
        method: "POST",
        body: JSON.stringify({ sourceCode })
      }
    )
};


class ApiError extends Error {
  constructor(message, status) {
    super(message);
    this.status = status;
  }
}


const safeJson = (text) => {
  try {
    return JSON.parse(text);
  } catch {
    return null;
  }
};


/* --------------------------------------------------
   IMPORTANT:
   read() and save() MUST be declared before state.
-------------------------------------------------- */

const read = (key) => {
  try {
    return JSON.parse(sessionStorage.getItem(key));
  } catch {
    return null;
  }
};


const save = (key, value) => {
  sessionStorage.setItem(key, JSON.stringify(value));
};


/* --------------------------------------------------
   Application state
-------------------------------------------------- */

const state = {
  user: read("scai-user"),
  challenges: [],
  activeChallenge: null,
  analysis: read("scai-analysis"),
  history: read("scai-history") || []
};


const app = document.querySelector("#app");


/* --------------------------------------------------
   Utility functions
-------------------------------------------------- */

const escapeHtml = (value) =>
  String(value ?? "").replace(
    /[&<>'"]/g,
    c => ({
      "&": "&amp;",
      "<": "&lt;",
      ">": "&gt;",
      "'": "&#39;",
      '"': "&quot;"
    }[c])
  );


const empty = (title, text) =>
  `<div class="card empty">
    <h2>${escapeHtml(title)}</h2>
    <p>${escapeHtml(text)}</p>
  </div>`;


const statusClass = (status) =>
  ({
    PASS: "pass",
    COMPLETED: "pass",
    FAIL: "fail",
    ERROR: "error",
    EVALUATION_ERROR: "error",
    TIMEOUT: "warning",
    SKIPPED: "warning"
  }[status] || "gray");


const statusPill = (status) =>
  `<span class="pill ${statusClass(status)}">
    ${escapeHtml(status || "Unavailable")}
  </span>`;

function renderShell(content, page) {
  const pages = [["home", "Home"], ["challenges", "Challenges"], ["learning", "Learning Hub"], ["analytics", "Analytics"], ["history", "History"], ["profile", "Profile"]];
  app.innerHTML = `<div class="layout"><aside class="sidebar"><div class="brand">SecureCode <span>AI</span></div><nav class="nav">${pages.map(([id, label]) => `<button data-route="${id}" class="${page === id ? "active" : ""}">${label}</button>`).join("")}</nav><div class="sidebar-footer">Functional correctness first.<br>Security analysis follows a passing submission.</div></aside><main class="main"><header class="topbar"><strong>${escapeHtml(pages.find(([id]) => id === page)?.[1] || "SecureCode AI")}</strong><span class="user-chip">${escapeHtml(state.user?.email || "Student")}</span></header><div class="page">${content}</div></main></div>`;
  app.querySelectorAll("[data-route]").forEach(button => button.addEventListener("click", () => route(button.dataset.route)));
}

function renderAuth(mode = "login") {
  const login = mode === "login";
  app.innerHTML = `<main class="auth-layout"><section class="auth-card"><div class="brand">SecureCode <span>AI</span></div><h1>${login ? "Welcome back" : "Create your student account"}</h1><p class="muted">${login ? "Sign in to continue your secure coding practice." : "Registration creates a STUDENT account."}</p><form id="auth-form"><label class="field">Email<input name="email" type="email" autocomplete="email" required></label><label class="field">Password<input name="password" type="password" autocomplete="${login ? "current-password" : "new-password"}" minlength="${login ? 1 : 12}" required></label>${login ? "" : '<p class="muted form-note">Use at least 12 characters.</p>'}<div id="auth-notice"></div><button class="primary" type="submit">${login ? "Sign in" : "Register"}</button></form><p class="form-note">${login ? "New here?" : "Already have an account?"} <button class="link-button" id="switch-auth">${login ? "Register" : "Sign in"}</button></p></section></main>`;
  app.querySelector("#switch-auth").onclick = () => renderAuth(login ? "register" : "login");
  app.querySelector("#auth-form").addEventListener("submit", async event => {
    event.preventDefault();
    const form = new FormData(event.currentTarget), button = event.currentTarget.querySelector("button[type=submit]"), notice = app.querySelector("#auth-notice");
    button.disabled = true; button.textContent = "Please wait…"; notice.innerHTML = "";
    try {
      const user = login ? await api.login(form.get("email"), form.get("password")) : await api.register(form.get("email"), form.get("password"));
      if (!login) { notice.innerHTML = `<p class="notice success">${escapeHtml(user.message)} Please sign in.</p>`; button.disabled = false; button.textContent = "Register"; return; }
      state.user = user; save("scai-user", user); route("home");
    } catch (error) { notice.innerHTML = `<p class="notice error">${escapeHtml(error.message)}</p>`; button.disabled = false; button.textContent = login ? "Sign in" : "Register"; }
  });
}

function home() {
  const attempts = state.history.length, solved = state.history.filter(item => item.result.status === "PASS").length;
  renderShell(`<h1 class="page-title">Welcome, ${escapeHtml(state.user?.email?.split("@")[0] || "student")}</h1><p class="page-intro">SecureCode AI checks whether your solution works first, then runs security analysis for passing Java submissions.</p><div class="grid three"><div class="card"><div class="stat">${solved}</div><div class="stat-label">Passing submissions this session</div></div><div class="card"><div class="stat">${attempts}</div><div class="stat-label">Attempts this session</div></div><div class="card"><div class="stat">${state.challenges.length || "—"}</div><div class="stat-label">Available challenges</div></div></div><div class="card" style="margin-top:18px"><h2>Start with a challenge</h2><p class="muted">Open a challenge to review its requirements and edit backend-provided starter code.</p><button class="primary" id="go-challenges">Browse challenges</button></div>`, "home");
  app.querySelector("#go-challenges").onclick = () => route("challenges");
}

async function challenges() {
  renderShell(`<h1 class="page-title">Challenges</h1><p class="page-intro">Choose a challenge to inspect its instructions and starter code.</p><div id="challenge-list" class="grid two"><div class="loading">Loading challenges…</div></div>`, "challenges");
  const list = app.querySelector("#challenge-list");
  try {
    state.challenges = await api.challenges();
    list.innerHTML = state.challenges.length ? state.challenges.map(challenge => `<article class="card challenge-card"><span class="pill">${escapeHtml(challenge.language || "Language unavailable")}</span><div><h2>${escapeHtml(challenge.title)}</h2><p class="muted">${escapeHtml(challenge.description || "No description provided.")}</p></div>${challenge.vulnerabilityType ? `<span class="pill gray">${escapeHtml(challenge.vulnerabilityType)}</span>` : ""}<button class="primary" data-challenge="${challenge.id}">Open challenge</button></article>`).join("") : empty("No challenges yet", "An administrator has not added any challenges.");
    app.querySelectorAll("[data-challenge]").forEach(button => button.onclick = () => openChallenge(button.dataset.challenge));
  } catch (error) { list.innerHTML = empty("Challenges unavailable", error.message); handleAuthError(error); }
}

async function openChallenge(id) {
  renderShell(`<div class="loading">Loading challenge details…</div>`, "challenges");
  try { state.activeChallenge = await api.challenge(id); renderEditor(); }
  catch (error) { renderShell(empty("Challenge unavailable", error.message), "challenges"); handleAuthError(error); }
}

function renderEditor() {
  const challenge = state.activeChallenge;
  if (!challenge) return route("challenges");
  const visibleTests = (challenge.testCases || []).filter(testCase => !testCase.hidden);
  renderShell(`<h1 class="page-title">${escapeHtml(challenge.title)}</h1><p class="page-intro">Edit the supplied starter code and submit when ready.</p><div class="editor-layout"><section class="details"><article class="card"><h2>Challenge details</h2><p>${escapeHtml(challenge.description || "No description provided by the backend.")}</p><div class="detail-list"><div><strong>Required language</strong><span>${escapeHtml(challenge.language || "Not specified")}</span></div><div><strong>Category</strong><span>${escapeHtml(challenge.vulnerabilityType || "Not provided")}</span></div>${challenge.vulnerabilitySubtype ? `<div><strong>Subtype</strong><span>${escapeHtml(challenge.vulnerabilitySubtype)}</span></div>` : ""}${challenge.cwe ? `<div><strong>CWE</strong><span>${escapeHtml(challenge.cwe)}</span></div>` : ""}</div></article><article class="card"><h2>Requirements and notes</h2><p class="muted">Difficulty and constraints will appear here when the backend exposes those fields.</p>${visibleTests.length ? `<h3 style="margin-top:16px">Visible examples</h3>${visibleTests.map(testCase => `<p><strong>Input:</strong> <code>${escapeHtml(testCase.inputData)}</code><br><strong>Expected output:</strong> <code>${escapeHtml(testCase.expectedOutput)}</code></p>`).join("")}` : ""}</article></section><section class="card editor-card"><div class="editor-toolbar"><strong>Starter code · ${escapeHtml(challenge.language || "unknown")}</strong><span class="muted">Loaded from challenge API</span></div><textarea id="source-code" class="editor" spellcheck="false" aria-label="Source code">${escapeHtml(challenge.starterCode || "")}</textarea><div id="submission-notice"></div><div class="editor-actions"><button class="primary" id="submit-code">Submit Code</button></div></section></div>`, "challenges");
  app.querySelector("#submit-code").onclick = submitCode;
}

async function submitCode() {
  const sourceCode = app.querySelector("#source-code").value, button = app.querySelector("#submit-code"), notice = app.querySelector("#submission-notice");
  if (!sourceCode.trim()) { notice.innerHTML = '<p class="notice error">Source code is required.</p>'; return; }
  button.disabled = true; button.textContent = "Submitting and evaluating…";
  notice.innerHTML = '<p class="notice">The existing backend is running functional evaluation. Security analysis only runs after a passing Java submission.</p>';
  try {
    const result = await api.submit(state.activeChallenge.id, sourceCode);
    state.analysis = { challenge: state.activeChallenge, result, submittedAt: new Date().toISOString() };
    save("scai-analysis", state.analysis); state.history.unshift(state.analysis); state.history = state.history.slice(0, 30); save("scai-history", state.history); analysis();
  } catch (error) { notice.innerHTML = `<p class="notice error">${escapeHtml(error.message)}</p>`; button.disabled = false; button.textContent = "Submit Code"; handleAuthError(error); }
}

function analysis() {
  const data = state.analysis;
  if (!data) { renderShell(`<h1 class="page-title">Analysis</h1>${empty("No submission selected", "Submit code from a challenge to see its evaluation result.")}`, "history"); return; }
  const { challenge, result } = data, security = result.securityEvaluation, findings = security?.normalizedResult?.findings || security?.findings || [];
  renderShell(`<div class="result-head"><div><h1 class="page-title">Submission analysis</h1><p class="page-intro">${escapeHtml(challenge.title)} · Submission #${escapeHtml(result.submissionId)}</p></div>${statusPill(result.status)}</div><div class="grid two"><article class="card"><h2>Functional evaluation</h2><p class="stat">${escapeHtml(result.passedTests)}/${escapeHtml(result.totalTests)}</p><p class="muted">Tests passed</p>${statusPill(result.status)}</article><article class="card"><h2>Security evaluation</h2>${security ? `${statusPill(security.status)}<p class="muted">Evaluator: ${escapeHtml(security.evaluator || "Unavailable")}</p><p>${security.detected ? "Security findings were reported." : "No security findings were reported."}</p>` : '<p class="muted">Security evaluation was not run. The current backend invokes Semgrep only after a functionally passing Java submission.</p>'}</article></div><article class="card" style="margin-top:18px"><h2>Security evidence</h2>${!security ? '<p class="muted">No security evidence is available for this submission.</p>' : security.error ? `<p class="notice error">${escapeHtml(security.error)}</p>` : findings.length ? findings.map(finding => { const location = finding.location || {}, line = location.startLine ?? finding.line; return `<div class="finding"><strong>${escapeHtml(finding.vulnerability || finding.vulnerabilityType || finding.ruleId || "Finding")}</strong> ${statusPill(finding.severity || "Reported")}<p>${escapeHtml(finding.evidence || finding.message || "No evidence text supplied.")}</p><p class="muted">${finding.cwe ? `CWE: ${escapeHtml(finding.cwe)} · ` : ""}${line ? `Line: ${escapeHtml(line)}` : "Location unavailable"}</p>${finding.code ? `<pre>${escapeHtml(finding.code)}</pre>` : ""}</div>`; }).join("") : '<p class="muted">The evaluator completed without reporting findings.</p>'}</article><button class="secondary" id="back-to-challenges" style="margin-top:18px">Back to challenges</button>`, "history");
  app.querySelector("#back-to-challenges").onclick = () => route("challenges");
}

function learning() { renderShell(`<h1 class="page-title">Learning Hub</h1><p class="page-intro">A place for secure coding concepts and guided practice.</p>${empty("Learning content is coming soon", "CWE material, cybersecurity datasets, and attacker/defender examples will be added here in a later phase.")}`, "learning"); }
function analytics() { const attempts = state.history.length, solved = state.history.filter(item => item.result.status === "PASS").length, findings = state.history.reduce((count, item) => count + (item.result.securityEvaluation?.normalizedResult?.findings?.length || item.result.securityEvaluation?.findings?.length || 0), 0); renderShell(`<h1 class="page-title">Analytics</h1><p class="page-intro">Statistics available from submissions made in this browser session.</p><div class="grid three"><div class="card"><div class="stat">${solved}</div><div class="stat-label">Passing submissions</div></div><div class="card"><div class="stat">${attempts}</div><div class="stat-label">Attempts</div></div><div class="card"><div class="stat">${findings}</div><div class="stat-label">Reported security findings</div></div></div><p class="disabled-note" style="margin-top:18px">The backend does not yet expose student analytics or submission-history endpoints. This page shows current-session records only.</p>`, "analytics"); }
function history() { const records = state.history; renderShell(`<h1 class="page-title">Challenge history</h1><p class="page-intro">Submission results stored for this browser session.</p>${records.length ? `<div class="card table-wrap"><table class="table"><thead><tr><th>Submission</th><th>Challenge ID</th><th>Category</th><th>Functional status</th><th>Security evidence</th></tr></thead><tbody>${records.map((item, index) => { const security = item.result.securityEvaluation, count = security?.normalizedResult?.findings?.length ?? security?.findings?.length ?? 0; return `<tr><td><button class="link-button" data-history="${index}">#${escapeHtml(item.result.submissionId)}</button></td><td>${escapeHtml(item.challenge.id)}</td><td>${escapeHtml(item.challenge.vulnerabilityType || "Not provided")}</td><td>${statusPill(item.result.status)}</td><td>${security ? `${statusPill(security.status)} ${count} finding${count === 1 ? "" : "s"}` : "Not evaluated"}</td></tr>`; }).join("")}</tbody></table></div>` : empty("No submissions yet", "Open a challenge and submit code to build your history.")}<p class="disabled-note" style="margin-top:18px">Persistent backend history is not currently available; this view is session-only.</p>`, "history"); app.querySelectorAll("[data-history]").forEach(button => button.onclick = () => { state.analysis = state.history[Number(button.dataset.history)]; save("scai-analysis", state.analysis); analysis(); }); }
function profile() { const user = state.user || {}; renderShell(`<h1 class="page-title">Profile & settings</h1><p class="page-intro">Your authenticated account details.</p><div class="grid two"><section class="card"><h2>Account</h2><div class="detail-list"><div><strong>Email</strong><span>${escapeHtml(user.email || "Unavailable")}</span></div><div><strong>Role</strong><span>${escapeHtml(user.role || "STUDENT")}</span></div></div></section><section class="card settings-card"><h2>Account settings</h2><p class="disabled-note">The current backend has no profile, name-change, or password-change endpoints. Those actions are intentionally not simulated.</p><label class="field">Display name<input disabled placeholder="Backend support required"></label><button class="secondary" disabled style="margin-top:14px">Change name</button><label class="field">New password<input disabled type="password" placeholder="Backend support required"></label><button class="secondary" disabled style="margin-top:14px">Change password</button><div style="margin-top:20px"><button id="sign-out" class="secondary">Sign out</button></div></section></div>`, "profile"); app.querySelector("#sign-out").onclick = async () => { try { await api.logout(); } finally { sessionStorage.removeItem("scai-user"); state.user = null; renderAuth(); } }; }
function handleAuthError(error) { if (error?.status === 401 || error?.status === 403) { sessionStorage.removeItem("scai-user"); state.user = null; setTimeout(() => renderAuth(), 300); } }
function route(page) { if (!state.user) { renderAuth(); return; } const pages = { home, challenges, learning, analytics, history, profile, analysis }; (pages[page] || home)(); }

route(state.user ? "home" : "login");
