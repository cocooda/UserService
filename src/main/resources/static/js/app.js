document.addEventListener("DOMContentLoaded", function () {
    checkAuthStatus(); // Check session on page load

    document.getElementById("register-form")?.addEventListener("submit", registerUser);
    document.getElementById("login-form")?.addEventListener("submit", loginUser);
    document.getElementById("logout-btn")?.addEventListener("click", logoutUser);
    document.getElementById("reactivate-btn")?.addEventListener("click", reactivateAccount);
});

/* Register User */
async function registerUser(event) {
    event.preventDefault();

    const form = event.target;
    const formData = new FormData(form);
    const avatar = formData.get("avatar");
    let avatarLink = null;

    if (avatar && avatar.size > 0) {
        const uploadFormData = new FormData();
        uploadFormData.append("avatar", avatar);

        const uploadResp = await fetch("/api/avatar/upload", {
            method: "POST",
            body: uploadFormData,
        });

        if (uploadResp.ok) {
            const uploadResult = await uploadResp.json();
            avatarLink = uploadResult.avatarUrl;
        } else {
            alert("Avatar upload failed. Error status: " + uploadResp.status);
            return;
        }
    }

    const userData = {
        email: formData.get("email"),
        password: formData.get("password"),
        userName: formData.get("userName"),
        bio: formData.get("bio") || "",
        avatarLink: avatarLink,
    };

    const response = await fetch("/api/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(userData),
        credentials: "include",
    });

    const result = await response.json();

    if (response.ok) {
        alert("Registration successful! Please log in.");
        window.location.href = "/login.html";
    } else {
        alert("Registration failed: " + result.error);
    }
}

/* Login User */
async function loginUser(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    const response = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
        credentials: "include",
    });

    const result = await response.json();

    if (response.ok) {
        if (result.actionRequired === "reactivate") {
            // Show reactivation prompt if account is soft-deleted and within the reactivation period
            showReactivationPrompt();
        } else {
            alert("Login successful!");
            window.location.href = "/index.html";
        }
    } else {
        alert("Invalid login credentials or OTP.");
    }
}

/* Reactivate Account */
async function reactivateAccount() {
    const response = await fetch("/api/reactivate-account", {
        method: "POST",
        credentials: "include",
    });

    const result = await response.json();
    if (response.ok) {
        alert("Account reactivated successfully!");
        window.location.href = "/index.html";
    } else {
        alert("Failed to reactivate account: " + result.error);
    }
}

/* Logout User */
async function logoutUser() {
    await fetch("/api/logout", {
        method: "POST",
        credentials: "include",
    });

    alert("Logged out successfully.");
    window.location.href = "/index.html";
}

/* Check Authentication Status */
async function checkAuthStatus() {
    const response = await fetch("/api/auth-status", { credentials: "include" });

    if (response.ok) {
        const data = await response.json();
        const authSection = document.getElementById("auth-section");
        const userSection = document.getElementById("user-section");

        if (data.loggedIn) {
            authSection?.classList.add("hidden");
            userSection?.classList.remove("hidden");
        } else {
            authSection?.classList.remove("hidden");
            userSection?.classList.add("hidden");
        }
    }
}
