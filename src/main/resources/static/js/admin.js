import { learningTopics } from "./learning-topics.js";

const $ = s => document.querySelector(s);
const esc = v => String(v ?? "").replace(/[&<>'"]/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", "'": "&#39;", '"': "&quot;" }[c]));

class ApiError extends Error {
    constructor(m, s) { super(m); this.status = s; }
}

const store = {
    token: () => sessionStorage.getItem("scai-token"),
    user: () => JSON.parse(sessionStorage.getItem("scai-user") || "null"),
    save: u => { sessionStorage.setItem("scai-token", u.token); sessionStorage.setItem("scai-user", JSON.stringify(u)); },
    clear: () => { sessionStorage.removeItem("scai-token"); sessionStorage.removeItem("scai-user"); }
};

const api = {
    async r(url, options = {}) {
        let token = store.token(),
            response = await fetch(url, {
                credentials: "omit",
                headers: {
                    "Content-Type": "application/json",
                    ...(token ? { Authorization: `Bearer ${token}` } : {})
                },
                ...options
            }),
            text = await response.text(),
            body;
        try { body = text ? JSON.parse(text) : null; } catch {}
        if (!response.ok) throw new ApiError(body?.detail || body?.message || `Request failed (${response.status})`, response.status);
        return body;
    },
    me: () => api.r("/api/auth/me"),
    summary: () => api.r("/api/student/summary"),
    profile: name => api.r("/api/student/profile", { method: "PUT", body: JSON.stringify({ name }) }),
    password: (currentPassword, newPassword) => api.r("/api/student/password", { method: "PUT", body: JSON.stringify({ currentPassword, newPassword }) }),
    users: () => api.r("/api/admin/users"),
    createUser: x => api.r("/api/admin/users", { method: "POST", body: JSON.stringify(x) }),
    updateUser: (id, x) => api.r(`/api/admin/users/${id}`, { method: "PUT", body: JSON.stringify(x) }),
    deleteUser: id => api.r(`/api/admin/users/${id}`, { method: "DELETE" }),
    challenges: () => api.r("/challenges"),
    challenge: id => api.r(`/challenges/${id}`),
    createChallenge: x => api.r("/challenges", { method: "POST", body: JSON.stringify(x) }),
    updateChallenge: (id, x) => api.r(`/challenges/${id}`, { method: "PUT", body: JSON.stringify(x) }),
    deleteChallenge: id => api.r(`/challenges/${id}`, { method: "DELETE" })
};

const state = { user: null, users: [], challenges: [], summary: null };

const ensureBasicDifficultyOption = () => {
    document.querySelectorAll('select[name="difficulty"]').forEach(select => {
        if (!select.querySelector('option[value="BASIC"]')) {
            select.insertAdjacentHTML("afterbegin", '<option value="BASIC">BASIC</option>');
        }
    });
};
new MutationObserver(ensureBasicDifficultyOption).observe(document.documentElement, { childList: true, subtree: true });

function signOut() {
    store.clear();
    location.replace("/");
}

function unauthorized(error) {
    if (error.status === 401 || error.status === 403) {
        signOut();
        return true;
    }
    return false;
}

function shell(content, page) {
    let nav = [
        ["home", "Home"],
        ["challenges", "Challenges"],
        ["learning", "Learning Hub"],
        ["users", "Users"],
        ["profile", "Profile"]
    ];
    let adminName = esc(state.summary?.name || state.user?.name || state.user?.email || "Admin");
    let topbarAccount = page === "home"
        ? `<span class="topbar-actions"><button type="button" class="home-user-chip" id="admin-user-chip" title="View Profile">${adminName}</button><button id="logout" class="secondary logout-button">Logout</button></span>`
        : `<span class="topbar-actions"><span class="user-chip" id="admin-user-chip" title="View Profile">${adminName}</span><button id="logout" class="secondary logout-button">Logout</button></span>`;

    $("#app").innerHTML = `<div class="layout admin-layout">
        <aside class="sidebar">
            <div class="brand">SecureCode <span>AI</span></div>
            <nav class="nav">
                ${nav.map(x => `<button data-go="${x[0]}" class="${x[0] === page ? "active" : ""}">${x[1]}</button>`).join("")}
            </nav>
            <div class="sidebar-footer">Administrator area</div>
        </aside>
        <main class="main">
            <header class="topbar">
                <strong>${nav.find(x => x[0] === page)?.[1] || "Admin Dashboard"}</strong>
                ${topbarAccount}
            </header>
            <div class="page">${content}</div>
        </main>
    </div>`;

    document.querySelectorAll("[data-go]").forEach(x => x.onclick = () => go(x.dataset.go));
    $("#logout").onclick = signOut;
    $("#admin-user-chip")?.addEventListener("click", () => go("profile"));
}

function go(page) {
    history.pushState({}, "", `#/${page}`);
    route();
}

async function home() {
    shell("Loading...", "home");
    try {
        let [summary, challengesList] = await Promise.all([
            api.summary().catch(() => null),
            api.challenges().catch(() => [])
        ]);
        if (summary) state.summary = summary;
        state.challenges = challengesList;

        let s = state.summary || { challengesSolved: 0, challengesAttempted: 0, totalSubmissions: 0 };
        let metrics = [
            [state.challenges.length, "Total challenges"],
            [s.challengesSolved, "Challenges solved"],
            [s.totalSubmissions, "Total submissions"]
        ];

        let topics = [
            ["?", "SQL", "Injection", "coral"],
            ["?", "NoSQL", "Injection", "green"],
            ["?", "Command", "Injection", "blue"],
            ["?", "LDAP", "Injection", "violet"],
            ["</>", "XPath", "Injection", "blue"],
            ["?", "XML", "Injection", "cyan"],
            ["?", "XXE", "Injection", "blue"],
            ["?", "Email Header", "Injection", "orange"],
            ["?", "Template", "Injection", "teal"],
            ["</>", "Code", "Injection", "blue"],
            [" ", "CRLF", "Injection", "coral"],
            ["?", "GraphQL", "Injection", "violet"]
        ];

        shell(`<section class="home-page redesigned-home">
            <section class="home-hero">
                <div class="home-hero-copy">
                    <p class="home-kicker">SECURE CODING PRACTICE</p>
                    <h1>Practice. Learn.<br>Write <span>Secure Code.</span></h1>
                    <p>SecureCode AI Administrator Workspace. Manage coding challenges, configure prerequisite progression, supervise accounts, and review platform feedback.</p>
                    <button class="primary home-cta" id="b">Manage Challenges <b>→</b></button>
                </div>
                <div class="hero-visual" aria-hidden="true">
                    <div class="hero-orb orb-one"></div>
                    <div class="hero-orb orb-two"></div>
                    <div class="hero-shield">🛡️<small>ADMIN</small></div>
                    <div class="hero-developer">
                        <div class="dev-head"></div>
                        <div class="dev-body"></div>
                        <div class="dev-laptop"><i></i><i></i><i></i><i></i></div>
                    </div>
                    <div class="hero-check check-one"><b>✓</b><span>Functional Tests<br><strong>PASS</strong></span></div>
                    <div class="hero-check check-two"><b>✓</b><span>Security Analysis<br><strong>SAFE</strong></span></div>
                    <div class="hero-note"><span>Manage<br>Curate<br>Configure<br>Review</span><b>🔒</b></div>
                </div>
            </section>
            <section class="home-aim">
                <div class="aim-icon">🎯<b>!</b></div>
                <h2>Aim of SecureCode AI</h2>
                <p>The aim of this platform is to help students and developers learn and practice secure coding through hands-on challenges. It focuses on real-world vulnerabilities, provides instant functional and security feedback, and builds the skills needed to write safe and secure software.</p>
            </section>
            <section class="home-content-grid">
                <article class="home-panel why-panel">
                    <h2>Why Learn Secure Coding?</h2>
                    <p>Security vulnerabilities can lead to data breaches, financial loss and real-world impact. Learning secure coding helps you:</p>
                    <ul>
                        <li><i>✓</i>Build safer and more reliable software</li>
                        <li><i>✓</i>Protect users and sensitive data</li>
                        <li><i>✓</i>Improve your skills and career opportunities</li>
                        <li><i>✓</i>Develop a security-first mindset</li>
                    </ul>
                </article>
                <article class="home-panel learn-panel">
                    <h2>What to Learn?</h2>
                    <p>Practice challenges across common vulnerability types, starting with injection attacks and more.</p>
                    <div class="topic-grid">
                        ${topics.map(([icon, top, bottom, color]) => `<div class="topic-card ${color}"><b>${icon}</b><span>${top}<br>${bottom}</span></div>`).join("")}
                    </div>
                </article>
                <article class="home-panel how-panel">
                    <h2>How to Learn?</h2>
                    <p>Follow a simple process to learn and improve with each attempt.</p>
                    <ol>
                        <li><b>1</b><div><strong>Learn the Concept</strong><span>Read the challenge description and key concepts in the Learning Hub.</span></div></li>
                        <li><b>2</b><div><strong>Solve the Challenge</strong><span>Write and submit your code in the in-built editor.</span></div></li>
                        <li><b>3</b><div><strong>Get Feedback</strong><span>Receive functional test results and security analysis from automated tools.</span></div></li>
                        <li><b>4</b><div><strong>Improve and Practice</strong><span>Use the feedback, hints and explanations to fix issues and strengthen your secure coding skills.</span></div></li>
                    </ol>
                </article>
            </section>
            <section class="home-summary redesigned-summary">
                ${metrics.map(([value, label]) => `<div><b>${esc(value)}</b><span>${esc(label)}</span></div>`).join("")}
            </section>
        </section>`, "home");

        $("#b").onclick = () => go("challenges");
    } catch (e) {
        if (!unauthorized(e)) shell(esc(e.message), "home");
    }
}

function learningHub() {
    let cards = learningTopics.map(topic => `<article class="card learning-card">
        <h2>${esc(topic.title)}</h2>
        <dl>
            <dt>What it is</dt><dd>${esc(topic.definition)}</dd>
            <dt>Where it occurs</dt><dd>${esc(topic.where)}</dd>
            <dt>Simple example</dt><dd><code>${esc(topic.example)}</code></dd>
            <dt>Impact</dt><dd>${esc(topic.impact)}</dd>
            <dt>Secure coding principle</dt><dd>${esc(topic.principle)}</dd>
        </dl>
    </article>`).join("");

    shell(`<section class="learning-page">
        <h1 class="page-title">Learning Hub</h1>
        <p class="page-intro">Learn the fundamentals behind every injection challenge before you start coding.</p>
        <div class="grid two learning-grid">${cards}</div>
    </section>`, "learning");
}

async function profile() {
    shell("Loading...", "profile");
    try {
        let s = await api.summary().catch(() => null);
        if (s) state.summary = s;
        let displayName = esc(state.summary?.name || state.user?.name || "Admin");
        let displayEmail = esc(state.summary?.email || state.user?.email || "");

        shell(`<h1 class="page-title">Profile & settings</h1>
        <div class="grid two">
            <section class="card">
                <h2>Account details</h2>
                <p><b>Name:</b> ${displayName}</p>
                <p><b>Email:</b> ${displayEmail}</p>
                <p><b>Role:</b> <span class="pill pass">${esc(state.user?.role || "ADMIN")}</span></p>
                <p><b>Challenges solved:</b> ${state.summary?.challengesSolved ?? 0}</p>
            </section>
            <section class="card">
                <h2>Update profile</h2>
                <form id="name-form" class="admin-form">
                    <label class="field">Name
                        <input name="name" value="${displayName}" required maxlength="120">
                    </label>
                    <button class="secondary">Save name</button>
                </form>
                <h2 style="margin-top:20px;">Change password</h2>
                <form id="pass-form" class="admin-form">
                    <label class="field">Current password
                        <input name="current" type="password" required>
                    </label>
                    <label class="field">New password
                        <input name="next" type="password" minlength="12" required>
                    </label>
                    <button class="secondary">Change password</button>
                </form>
                <div id="msg" class="admin-note" style="margin-top:10px;"></div>
                <button id="out-btn" class="link-button" style="margin-top:14px;">Sign out</button>
            </section>
        </div>`, "profile");

        $("#name-form").onsubmit = async e => {
            e.preventDefault();
            let msg = $("#msg");
            try {
                let updated = await api.profile(new FormData(e.target).get("name"));
                state.summary = updated;
                state.user.name = updated.name;
                store.save({ ...state.user, name: updated.name });
                msg.style.color = "var(--success)";
                msg.textContent = "Name updated successfully.";
                let chip = $("#admin-user-chip");
                if (chip) chip.textContent = updated.name;
            } catch (x) {
                msg.style.color = "var(--danger)";
                msg.textContent = x.message;
            }
        };

        $("#pass-form").onsubmit = async e => {
            e.preventDefault();
            let msg = $("#msg");
            let f = new FormData(e.target);
            try {
                await api.password(f.get("current"), f.get("next"));
                msg.style.color = "var(--success)";
                msg.textContent = "Password updated successfully.";
                e.target.reset();
            } catch (x) {
                msg.style.color = "var(--danger)";
                msg.textContent = x.message;
            }
        };

        $("#out-btn").onclick = signOut;
    } catch (e) {
        if (!unauthorized(e)) shell(esc(e.message), "profile");
    }
}

function userForm(user = { name: "", email: "", role: "STUDENT" }, creating = false) {
    return `<section class="card">
        <h2>${creating ? "Add user" : "Manage user"}</h2>
        <form class="admin-form" id="user-form">
            <input type="hidden" name="id" value="${user.id || ""}">
            <div class="admin-form-grid">
                <label class="field">Name<input name="name" required maxlength="120" value="${esc(user.name)}"></label>
                <label class="field">Email<input name="email" type="email" required ${creating ? "" : "readonly"} value="${esc(user.email)}"></label>
            </div>
            <div class="admin-form-grid">
                <label class="field">Role
                    <select name="role">
                        <option value="STUDENT" ${user.role === "STUDENT" ? "selected" : ""}>STUDENT</option>
                        <option value="ADMIN" ${user.role === "ADMIN" ? "selected" : ""}>ADMIN</option>
                    </select>
                </label>
                <label class="field">${creating ? "Password" : "New password (leave blank to keep)"}
                    <input name="password" type="password" ${creating ? "required minlength=12" : "minlength=12"}>
                </label>
            </div>
            <div class="admin-actions">
                <button class="primary">${creating ? "Create user" : "Save user"}</button>
                ${creating ? '<button type="button" id="cancel-user" class="secondary">Cancel</button>' : ""}
            </div>
            <div id="user-note" class="admin-note"></div>
        </form>
    </section>`;
}

async function users() {
    shell(`<div class="admin-toolbar">
        <div>
            <h1 class="page-title">Users</h1>
            <p class="page-intro">View and manage user accounts.</p>
        </div>
        <button class="primary" id="add-user">Add user</button>
    </div>
    <div id="user-form-slot"></div>
    <section class="card table-wrap">
        <table class="table">
            <thead>
                <tr><th>Name</th><th>Email</th><th>Role</th><th>Created</th><th></th></tr>
            </thead>
            <tbody id="users"></tbody>
        </table>
    </section>`, "users");

    try {
        state.users = await api.users();
        renderUsers();
    } catch (error) {
        if (!unauthorized(error)) $("#users").innerHTML = `<tr><td colspan="5">${esc(error.message)}</td></tr>`;
    }
    $("#add-user").onclick = () => openUserForm();
}

function renderUsers() {
    $("#users").innerHTML = state.users.map(user => `<tr>
        <td>${esc(user.name)}</td>
        <td>${esc(user.email)}</td>
        <td><span class="pill ${user.role === "ADMIN" ? "warning" : "gray"}">${esc(user.role)}</span></td>
        <td>${esc(new Date(user.createdAt).toLocaleDateString())}</td>
        <td class="admin-actions">
            <button class="link-button" data-edit-user="${user.id}">Edit</button>
            <button class="link-button" data-delete-user="${user.id}">Delete</button>
        </td>
    </tr>`).join("") || '<tr><td colspan="5" class="muted">No users found.</td></tr>';

    document.querySelectorAll("[data-edit-user]").forEach(x =>
        x.onclick = () => openUserForm(state.users.find(u => u.id === Number(x.dataset.editUser))));
    document.querySelectorAll("[data-delete-user]").forEach(x =>
        x.onclick = () => deleteUser(Number(x.dataset.deleteUser)));
}

function openUserForm(user) {
    let creating = !user;
    $("#user-form-slot").innerHTML = userForm(user, creating);
    $("#cancel-user")?.addEventListener("click", () => $("#user-form-slot").innerHTML = "");
    $("#user-form").onsubmit = async event => {
        event.preventDefault();
        let form = new FormData(event.target),
            request = {
                name: form.get("name"),
                email: form.get("email"),
                role: form.get("role"),
                password: form.get("password") || null
            };
        try {
            creating ? await api.createUser(request) : await api.updateUser(form.get("id"), request);
            $("#user-form-slot").innerHTML = "";
            state.users = await api.users();
            renderUsers();
        } catch (error) {
            if (!unauthorized(error)) $("#user-note").textContent = error.message;
        }
    };
}

async function deleteUser(id) {
    let user = state.users.find(x => x.id === id);
    if (!user || !confirm(`Delete ${user.email}? This also removes their submission data.`)) return;
    try {
        await api.deleteUser(id);
        state.users = await api.users();
        renderUsers();
    } catch (error) {
        if (!unauthorized(error)) alert(error.message);
    }
}

function challengeForm(challenge = { language: "java", difficulty: "MEDIUM", testCases: [], prerequisites: [] }, creating = false) {
    let tests = challenge.testCases?.length ? challenge.testCases : [{ inputData: "", expectedOutput: "", hidden: true, position: 0 }];
    let currentPrereqs = challenge.prerequisites || [];
    let currentPrereqNumbers = currentPrereqs.map(p => Number(p.challengeNumber));

    let currentDisplay = currentPrereqs.length
        ? currentPrereqs.map(p => `<span class="prereq-tag">No. ${p.challengeNumber}: ${esc(p.title)}</span>`).join(" ")
        : '<span class="muted">None</span>';

    // Build options excluding current challenge to prevent self-reference
    let prereqOptions = (state.challenges || [])
        .filter(c => !challenge.id || c.id !== challenge.id)
        .map(c => {
            let isSelected = currentPrereqNumbers.includes(Number(c.challengeNumber));
            let numLabel = c.catalogNumber ? `${c.catalogNumber} (No. ${c.challengeNumber})` : `No. ${c.challengeNumber}`;
            return `<option value="${c.challengeNumber}" ${isSelected ? "selected" : ""}>[${numLabel}] ${esc(c.title)}</option>`;
        }).join("");

    return `<section class="card">
        <h2>${creating ? "Add challenge" : `Update challenge (No. ${challenge.challengeNumber})`}</h2>
        <form class="admin-form" id="challenge-form">
            <div class="admin-form-grid">
                <label class="field">Title<input name="title" required maxlength="255" value="${esc(challenge.title || "")}"></label>
                <label class="field">Language<input name="language" required maxlength="32" value="${esc(challenge.language || "java")}"></label>
            </div>
            <label class="field">Description<textarea name="description" required>${esc(challenge.description || "")}</textarea></label>
            <div class="admin-form-grid">
                <label class="field">Vulnerability type<input name="vulnerabilityType" maxlength="100" value="${esc(challenge.vulnerabilityType || "")}"></label>
                <label class="field">Vulnerability subtype<input name="vulnerabilitySubtype" maxlength="100" value="${esc(challenge.vulnerabilitySubtype || "")}"></label>
            </div>
            <div class="admin-form-grid">
                <label class="field">CWE<input name="cwe" maxlength="32" value="${esc(challenge.cwe || "")}"></label>
                <label class="field">Difficulty
                    <select name="difficulty">
                        ${["BASIC", "EASY", "MEDIUM", "HARD"].map(x => `<option ${challenge.difficulty === x ? "selected" : ""}>${x}</option>`).join("")}
                    </select>
                </label>
            </div>
            
            <div class="field">
                <div class="admin-prereq-display">
                    <b>Current Prerequisites:</b> ${currentDisplay}
                </div>
                <label class="field">Manage Prerequisites
                    <select name="prerequisites" multiple class="admin-select-prereqs" aria-label="Select prerequisite challenges">
                        ${prereqOptions}
                    </select>
                </label>
                <small class="muted">Hold Ctrl (or Cmd on Mac) to select, add, or deselect prerequisite challenges. Select none to clear prerequisites.</small>
            </div>

            <label class="field">Starter code<textarea class="code-input" name="starterCode">${esc(challenge.starterCode || "")}</textarea></label>
            
            <div class="admin-toolbar" style="margin-top:16px;">
                <h2>Test cases</h2>
                <button type="button" class="secondary" id="add-test">Add test case</button>
            </div>
            <div id="tests">${tests.map(testCase).join("")}</div>
            
            <div class="admin-actions">
                <button class="primary">${creating ? "Create challenge" : "Save challenge"}</button>
                <button type="button" id="cancel-challenge" class="secondary">Cancel</button>
            </div>
            <div id="challenge-note" class="admin-note"></div>
        </form>
    </section>`;
}

function testCase(test = { inputData: "", expectedOutput: "", hidden: true, position: 0 }) {
    return `<div class="admin-test">
        <label class="field">Input<textarea name="inputData" required>${esc(test.inputData)}</textarea></label>
        <label class="field">Expected output<textarea name="expectedOutput" required>${esc(test.expectedOutput)}</textarea></label>
        <label class="field">Position<input name="position" type="number" min="0" required value="${Number(test.position) || 0}"></label>
        <label><input name="hidden" type="checkbox" ${test.hidden ? "checked" : ""}> Hidden</label>
        <button type="button" class="link-button remove-test">Remove</button>
    </div>`;
}

async function challenges() {
    shell(`<div class="admin-toolbar">
        <div>
            <h1 class="page-title">Challenges</h1>
            <p class="page-intro">View, add, update, and configure challenge prerequisites.</p>
        </div>
        <button class="primary" id="add-challenge">Add challenge</button>
    </div>
    <div id="challenge-form-slot"></div>
    <section class="card table-wrap">
        <table class="table">
            <thead>
                <tr>
                    <th>No.</th>
                    <th>Title</th>
                    <th>Language</th>
                    <th>Difficulty</th>
                    <th>Prerequisites</th>
                    <th>Tests</th>
                    <th></th>
                </tr>
            </thead>
            <tbody id="challenges"></tbody>
        </table>
    </section>`, "challenges");

    try {
        state.challenges = await api.challenges();
        renderChallenges();
    } catch (error) {
        if (!unauthorized(error)) $("#challenges").innerHTML = `<tr><td colspan="7">${esc(error.message)}</td></tr>`;
    }
    $("#add-challenge").onclick = () => openChallengeForm();
}

function renderChallenges() {
    $("#challenges").innerHTML = state.challenges.map(challenge => {
        let numDisplay = challenge.catalogNumber ? `${challenge.catalogNumber} (No. ${challenge.challengeNumber})` : `No. ${challenge.challengeNumber}`;
        let prereqDisplay = challenge.prerequisites?.length
            ? challenge.prerequisites.map(p => `<span class="prereq-tag">No. ${p.challengeNumber}</span>`).join(" ")
            : '<span class="muted">None</span>';

        return `<tr>
            <td><strong>${numDisplay}</strong></td>
            <td>${esc(challenge.title)}</td>
            <td>${esc(challenge.language)}</td>
            <td><span class="difficulty-pill difficulty-${String(challenge.difficulty || "").toLowerCase()}">${esc(challenge.difficulty)}</span></td>
            <td>${prereqDisplay}</td>
            <td>${challenge.testCases?.length || 0}</td>
            <td class="admin-actions">
                <button class="link-button" data-edit-challenge="${challenge.id}">Edit</button>
                <button class="link-button" data-delete-challenge="${challenge.id}">Delete</button>
            </td>
        </tr>`;
    }).join("") || '<tr><td colspan="7" class="muted">No challenges found.</td></tr>';

    document.querySelectorAll("[data-edit-challenge]").forEach(x => x.onclick = async () => {
        try {
            openChallengeForm(await api.challenge(x.dataset.editChallenge));
        } catch (error) {
            if (!unauthorized(error)) alert(error.message);
        }
    });
    document.querySelectorAll("[data-delete-challenge]").forEach(x => x.onclick = () => deleteChallenge(Number(x.dataset.deleteChallenge)));
}

function openChallengeForm(challenge) {
    let creating = !challenge;
    $("#challenge-form-slot").innerHTML = challengeForm(challenge, creating);
    $("#cancel-challenge").onclick = () => $("#challenge-form-slot").innerHTML = "";
    $("#add-test").onclick = () => {
        $("#tests").insertAdjacentHTML("beforeend", testCase({ position: $("#tests").children.length }));
        bindTestRemovals();
    };
    bindTestRemovals();

    $("#challenge-form").onsubmit = async event => {
        event.preventDefault();
        let form = event.target,
            values = new FormData(form),
            tests = [...form.querySelectorAll(".admin-test")].map(row => ({
                inputData: row.querySelector('[name="inputData"]').value,
                expectedOutput: row.querySelector('[name="expectedOutput"]').value,
                hidden: row.querySelector('[name="hidden"]').checked,
                position: Number(row.querySelector('[name="position"]').value)
            })),
            selectedPrereqs = [...form.querySelectorAll('[name="prerequisites"] option:checked')].map(o => Number(o.value));

        // Client-side self-reference check
        if (challenge?.challengeNumber && selectedPrereqs.includes(Number(challenge.challengeNumber))) {
            $("#challenge-note").textContent = "A challenge cannot have itself as a prerequisite.";
            return;
        }

        let request = {
            title: values.get("title"),
            description: values.get("description"),
            language: values.get("language"),
            vulnerabilityType: values.get("vulnerabilityType") || null,
            vulnerabilitySubtype: values.get("vulnerabilitySubtype") || null,
            cwe: values.get("cwe") || null,
            starterCode: values.get("starterCode") || null,
            difficulty: values.get("difficulty"),
            testCases: tests,
            prerequisiteChallengeNumbers: selectedPrereqs
        };

        try {
            creating ? await api.createChallenge(request) : await api.updateChallenge(challenge.id, request);
            $("#challenge-form-slot").innerHTML = "";
            state.challenges = await api.challenges();
            renderChallenges();
        } catch (error) {
            if (!unauthorized(error)) $("#challenge-note").textContent = error.message;
        }
    };
}

function bindTestRemovals() {
    document.querySelectorAll(".remove-test").forEach(button => button.onclick = () => button.closest(".admin-test").remove());
}

async function deleteChallenge(id) {
    let challenge = state.challenges.find(x => x.id === id);
    if (!challenge || !confirm(`Delete "${challenge.title}"? This also removes its submissions.`)) return;
    try {
        await api.deleteChallenge(id);
        state.challenges = await api.challenges();
        renderChallenges();
    } catch (error) {
        if (!unauthorized(error)) alert(error.message);
    }
}

function route() {
    if (!state.user || state.user.role !== "ADMIN") {
        location.replace("/");
        return;
    }
    let page = (location.hash.slice(2) || "home").split("/")[0];
    ({ home, challenges, learning: learningHub, users, profile }[page] || home)();
}

async function restore() {
    if (!store.token()) {
        location.replace("/");
        return;
    }
    try {
        state.user = await api.me();
        if (state.user.role !== "ADMIN") {
            location.replace("/");
            return;
        }
        route();
    } catch {
        signOut();
    }
}

window.onpopstate = route;
window.onhashchange = route;
restore();
