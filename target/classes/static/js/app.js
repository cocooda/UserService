document.addEventListener("DOMContentLoaded", function () {
    checkAuthStatus(); // ✅ Check session on page load

    document.getElementById("register-form")?.addEventListener("submit", registerUser);
    document.getElementById("login-form")?.addEventListener("submit", loginUser);
    document.getElementById("logout-btn")?.addEventListener("click", logoutUser);
    document.getElementById("reactivate-btn")?.addEventListener("click", reactivateAccount);
});


/** ✅ Register User */
async function registerUser(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    data.avatarLink = data.avatarLink || ""; 
    data.bio = data.bio || ""; 

    console.log("Sending JSON data:", JSON.stringify(data)); // ✅ Debug before sending

    const response = await fetch("/api/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },  // ✅ Ensure JSON
        body: JSON.stringify(data), 
        credentials: "include",
    });

    const result = await response.json();
    console.log("Server response:", result); // ✅ Debug server response

    if (response.ok) {
        alert("Registration successful! Please log in.");
        window.location.href = "/login.html";
    } else {
        alert("Registration failed: " + result.error);
    }
}


/** ✅ Login User */
async function loginUser(event) {
    event.preventDefault();
    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    const response = await fetch("/api/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
        credentials: "include", // ✅ Ensure session persistence
    });

    const result = await response.json();

    if (response.ok) {
        console.log("User ID from login response: " + result.userId);
        if (result.actionRequired === "reactivate") {
            // Show reactivation prompt if account is soft-deleted and within the reactivation period
            showReactivationPrompt();
        } else {
            alert("Login successful!");
            window.location.href = "/index.html"; // Redirect to home
        }
    } else {
        alert("Invalid login credentials or OTP.");
    }
}

/** ✅ Reactivate Account */
async function reactivateAccount() {
    const response = await fetch("/api/reactivate-account", {
        method: "POST",
        credentials: "include", // Ensure session is cleared on the backend
    });

    const result = await response.json();
    if (response.ok) {
        alert("Account reactivated successfully!");
        window.location.href = "/index.html"; // Redirect to home after reactivation
    } else {
        alert("Failed to reactivate account: " + result.error);
    }
}



/** ✅ Logout User */
async function logoutUser() {
    await fetch("/api/logout", { 
        method: "POST",
        credentials: "include", // ✅ Ensure session is cleared on the backend
    });

    alert("Logged out successfully.");
    window.location.href = "/index.html";
}

/** ✅ Check Authentication Status */
async function checkAuthStatus() {
    const response = await fetch("/api/auth-status", { credentials: "include" });

    if (response.ok) {
        const data = await response.json();
        const authSection = document.getElementById("auth-section");
        const userSection = document.getElementById("user-section");

        if (data.loggedIn) {
            authSection?.classList.add("hidden"); // ✅ Hide login/register
            userSection?.classList.remove("hidden"); // ✅ Show profile button
        } else {
            authSection?.classList.remove("hidden"); // ✅ Show login/register
            userSection?.classList.add("hidden"); // ✅ Hide profile button
        }
    }
}
