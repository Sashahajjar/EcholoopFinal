// Sample post data
const posts = [
  {
    user: "DJ Spectrum",
    time: "2h",
    content: "Excited to share my latest deep house mix with you all!",
    images: []
  },
  {
    user: "Bass Fest",
    time: "6h",
    content: "Get ready for an unforgettable night in Chicago! 🎵",
    images: ["images/img1.jpg", "images/img2.jpg", "images/img3.jpg"]
  },
  {
    user: "DJ Voltage",
    time: "1d",
    content: "Throwback to my live set in Berlin 🔥",
    images: ["images/img4.jpg"]
  }
];

// Get feed container
const feed = document.getElementById("feed");

// Render each post
posts.forEach(post => {
  const postEl = document.createElement("div");
  postEl.className = "feed-post";

  // Avatar initials
  const initials = post.user
    .split(" ")
    .map(word => word[0])
    .join("")
    .toUpperCase();

  // Image section
  const imageHTML = post.images.length
    ? `
    <div class="feed-images">
      ${post.images.map(img => `<img src="${img}" alt="Post image">`).join("")}
    </div>
  `
    : "";

  // Post content
  postEl.innerHTML = `
    <div class="feed-avatar">${initials}</div>
    <div class="feed-post-content">
      <h4>${post.user} <span>${post.time}</span></h4>
      <p>${post.content}</p>
      ${imageHTML}
      <div class="feed-actions">
        <button>👍 Like</button>
        <button>💬 Comment</button>
      </div>
    </div>
  `;

  feed.appendChild(postEl);
});
function login() {
  const username = document.getElementById("loginUsername").value;
  const password = document.getElementById("loginPassword").value;

  fetch("http://localhost:8080/api/users/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password })
  })
    .then(response => {
      if (!response.ok) throw new Error("Invalid credentials");
      return response.json();
    })
    .then(user => {
      closeModal('loginModal');
      if (user.role === "DJ") {
        window.location.href = "account_dj.html";
		} else if (user.role === "Event Community") {
		  window.location.href = "account_community.html";
		}
    })
    .catch(err => alert("Login failed: " + err.message));
}

function signup() {
  const username = document.getElementById("signupUsername").value;
  const password = document.getElementById("signupPassword").value;
  const role = document.getElementById("signupRole").value;

  fetch("http://localhost:8080/api/users/signup", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password, role })
  })
    .then(response => {
      if (!response.ok) throw new Error("Signup failed");
      alert("Signup successful. You can now log in.");
      closeModal('signupModal');
    })
    .catch(err => alert(err.message));
}
