document.addEventListener("DOMContentLoaded", async function () {
    await checkAuthStatus(); // Ensure user is logged in
    await fetchUserProfile(); // Load profile if authenticated

    document.getElementById("logout-btn").addEventListener("click", logoutUser);
    document.getElementById("update-profile-form").addEventListener("submit", updateUserProfile);
    document.getElementById("delete-account-btn").addEventListener("click", deleteUser);
    document.getElementById("new-avatar").addEventListener("change", handleAvatarUpload);
});

/* Check if user is authenticated */
async function checkAuthStatus() {
    try {
        const response = await fetch("/api/auth-status", { credentials: "include" });
        if (!response.ok) {
            window.location.href = "/login.html"; // Redirect if not logged in
            return false;
        }
        return true;
    } catch (error) {
        console.error("Auth check failed:", error);
        return false;
    }
}

/* Fetch and display user profile */
async function fetchUserProfile() {
    console.log("Fetching user profile...");

    try {
        const response = await fetch("/api/user/profile", { credentials: "include" });

        if (!response.ok) {
            console.error("Failed to fetch user profile (401 Unauthorized?)");
            return;
        }

        const user = await response.json();
        console.log("Received user data:", user);

        // Ensure profile elements exist before updating
        const usernameElement = document.getElementById("username");
        const avatarElement = document.getElementById("avatar");
        const bioElement = document.getElementById("bio");

        if (!usernameElement || !avatarElement || !bioElement) {
            console.error("One or more profile elements are missing in HTML.");
            return;
        }

        // Update the profile elements
        usernameElement.textContent = user.userName || "No Name";
        avatarElement.src = user.avatarLink || "/images/default-avatar.png"; // Use default avatar if empty
        bioElement.textContent = user.bio || "No bio available.";

    } catch (error) {
        console.error("Error fetching profile:", error);
    }
}

/* Logout User */
async function logoutUser() {
    await fetch("/api/logout", { method: "POST", credentials: "include" });
    alert("Logged out successfully.");
    window.location.href = "/index.html";
}

/* Handle avatar file upload and update hidden input */
async function handleAvatarUpload(event) {
    const file = event.target.files[0];
    if (!file) return;

    const formData = new FormData();
    formData.append("avatar", file);

    const response = await fetch("/api/avatar/upload", {
        method: "POST",
        body: formData,
    });

    const result = await response.json();

    if (response.ok) {
        const avatarUrl = result.avatarUrl;
        document.getElementById("avatarLink").value = avatarUrl;
        document.getElementById("avatar").src = avatarUrl;
        alert("Avatar uploaded!");
    } else {
        alert("Failed to upload avatar.");
    }
}

/* Update Profile */
async function updateUserProfile(event) {
    event.preventDefault();

    const formData = new FormData(event.target);
    const data = Object.fromEntries(formData.entries());

    console.log("Sending profile update:", data); // Log request body

    const response = await fetch("/api/user/update", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(data),
        credentials: "include",
    });

    if (response.ok) {
        alert("Profile updated successfully.");
        fetchUserProfile(); // Refresh profile
    } else {
        const errorText = await response.text(); // Get server response
        console.error("Failed to update profile:", errorText);
        alert("Failed to update profile: " + errorText);
    }
}

/* Delete Account */
async function deleteUser() {
    if (!confirm("Are you sure? This action cannot be undone, and your account will be deactivated for 30 days before permanent deletion.")) return;

    const response = await fetch("/api/user/delete", { 
        method: "DELETE",
        credentials: "include",
    });

    if (response.ok) {
        const message = await response.text(); // Get the response message
        alert(message); // Display the message to the user
        window.location.href = "/index.html"; // Redirect to homepage
    } else {
        alert("Failed to delete account.");
    }
}

