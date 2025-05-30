// Global user variables
const loggedInUser = JSON.parse(localStorage.getItem("echoloopUser"));
const loggedInUserId = loggedInUser?.id;

// ✅ Account redirection
function goToAccount() {
  if (!loggedInUser) {
    alert("You're not logged in.");
    window.location.href = "index.html";
  } else {
    window.location.href = `profile.html?id=${loggedInUser.id}`;
  }
}

// ✅ Search bar key press
function handleSearchKey(event) {
  if (event.key === "Enter") {
    const query = document.getElementById("searchInput").value;
    searchAll(query);
  }
}

// ✅ Combined search: DJs, Communities, Events
function searchAll(query) {
  const container = document.getElementById("searchResults");
  container.innerHTML = "<p>Searching...</p>";

  Promise.all([
    fetch(`/api/users/search?query=${encodeURIComponent(query)}`).then(res => res.json()),
    fetch(`/api/events/search?query=${encodeURIComponent(query)}`).then(res => res.json())
  ])
    .then(([userResults, eventResults]) => {
      container.innerHTML = "";

      if (userResults.djs?.length > 0) {
        const djHeader = document.createElement("h4");
        djHeader.textContent = "DJs";
        container.appendChild(djHeader);

        userResults.djs.forEach(user => {
          const profilePage = `profile.html?id=${user.id}`;
          const div = document.createElement("div");
          div.classList.add("search-result-item");
          div.innerHTML = `
            <a href="${profilePage}">
              <strong>${user.username}</strong>
            </a> — ${user.genres || "no genres"}
          `;
          container.appendChild(div);
        });
      }

      if (userResults.communities?.length > 0) {
        const communityHeader = document.createElement("h4");
        communityHeader.textContent = "Event Communities";
        container.appendChild(communityHeader);

        userResults.communities.forEach(user => {
          const profilePage = loggedInUserId === user.id ? "account_community.html" : `community_profile.html?id=${user.id}`;
          const div = document.createElement("div");
          div.classList.add("search-result-item");
          div.innerHTML = `
            <a href="${profilePage}">
              <strong>${user.username}</strong>
            </a> — Event Community
          `;
          container.appendChild(div);
        });
      }

      if (eventResults?.length > 0) {
        const eventHeader = document.createElement("h4");
        eventHeader.textContent = "Events";
        container.appendChild(eventHeader);

        eventResults.forEach(event => {
          const div = document.createElement("div");
          div.classList.add("search-result-item");
          div.innerHTML = `
            <a href="event_detail.html?eventId=${event.id}">
              <strong>${event.title}</strong>
            </a><br>
            ${event.genre} — ${event.location}
          `;
          container.appendChild(div);
        });
      }

      if (
        (!userResults.djs?.length && !userResults.communities?.length && !eventResults?.length)
      ) {
        container.innerHTML = "<p>No results found.</p>";
      }
    })
    .catch(err => {
      console.error("Search failed:", err);
      container.innerHTML = "<p>Search failed. Try again later.</p>";
    });
}

// ✅ Apply to Event
function applyToEvent(eventId, button) {
  if (!loggedInUser || loggedInUser.role.toLowerCase() !== "dj") {
    alert("Only DJs can apply to events.");
    return;
  }

  fetch(`/api/events/${eventId}/apply/${loggedInUser.id}`, {
    method: "POST"
  })
    .then(res => {
      if (res.ok) {
        button.textContent = "Applied";
        button.disabled = true;
        button.classList.add("applied");
      } else {
        alert("Application failed or already applied.");
      }
    })
    .catch(err => {
      console.error("Apply error:", err);
      alert("Something went wrong.");
    });
}

// ✅ Navigation Bar
function updateNavigationBar() {
  if (!loggedInUser) {
    window.location.href = "index.html";
    return;
  }

  const navLinks = document.querySelector(".feed-nav-links");
  if (!navLinks) return;

  const role = loggedInUser.role?.toLowerCase();
  const navItems = role === "event community"
    ? [
        { href: "feed.html", text: "Feed" },
        { href: "suggestions.html", text: "Suggestions" },
        { href: "create_post.html", text: "Create Post" },
        { href: "create_event.html", text: "Create Event" },
        { href: `community_profile.html?id=${loggedInUser.id}`, text: "My Profile" },
        { href: "account_community.html", text: "Management" }
      ]
    : [
        { href: "feed.html", text: "Feed" },
        { href: "suggestions.html", text: "Suggestions" },
        { href: "create_post.html", text: "Create Post" },
        { href: "opportunities.html", text: "Opportunities" },
        { href: "my_applications.html", text: "My Applications" },
        { href: "#", text: "Account", onclick: "goToAccount(); return false;" }
      ];

  navLinks.innerHTML = "";
  navItems.forEach(item => {
    const link = document.createElement("a");
    link.href = item.href;
    link.textContent = item.text;
    if (item.onclick) link.setAttribute("onclick", item.onclick);
    link.className = window.location.pathname.endsWith(item.href.split('?')[0]) ? "active" : "";
    navLinks.appendChild(link);
  });
}

// ✅ Feed rendering
document.addEventListener("DOMContentLoaded", async function () {
  updateNavigationBar();

  const feed = document.getElementById("feed");
  if (!feed) return;

  if (!loggedInUser) {
    window.location.href = "index.html";
    return;
  }

  try {
    const [posts, events, appliedEvents] = await Promise.all([
      fetch(`/api/posts/following/${loggedInUser.id}`).then(res => res.json()),
      fetch(`/api/events/feed/following/${loggedInUser.id}`).then(res => res.json()),
      loggedInUser.role.toLowerCase() === "dj"
        ? fetch(`/api/events/check-applications/${loggedInUser.id}`).then(res => res.json())
        : Promise.resolve([])
    ]);

    const items = [
      ...posts.map(post => ({
        type: "post",
        createdAt: new Date(post.createdAt).getTime(),
        data: post
      })),
      ...events.map(event => ({
        type: "event",
        createdAt: new Date(event.createdAt).getTime(),
        data: event
      }))
    ].sort((a, b) => b.createdAt - a.createdAt);

    feed.innerHTML = "";

    for (const item of items) {
      const div = document.createElement("div");
      const timestamp = new Date(item.createdAt).toLocaleString('en-US', {
        weekday: 'short',
        month: 'short',
        day: 'numeric',
        hour: 'numeric',
        minute: '2-digit',
        hour12: true
      });

      if (item.type === "post") {
        const p = item.data;
        const initials = p.username ? p.username[0].toUpperCase() : "?";
        const imageHTML = p.imageUrl ? `<img src="/${p.imageUrl}" alt="Post image" class="feed-post-image" />` : "";

        const commentsHTML = p.recentComments && p.recentComments.length > 0
          ? `<div class="feed-comments">
              ${p.recentComments.map(c => `
                <div class="feed-comment">
                  <strong>${c.username}</strong>: ${c.content}
                </div>
              `).join("")}
            </div>`
          : `<div class="feed-comments"><em>No comments yet</em></div>`;

        div.className = "feed-post";
        div.innerHTML = `
          <div class="feed-post-header">
            <div class="feed-avatar">${initials}</div>
            <div class="feed-post-content">
              <h4>${p.username} <small>${timestamp}</small></h4>
              <p>${p.content}</p>
              ${imageHTML}
              <div class="post-actions">
                <button onclick="likePost(${p.id}, this)" class="feed-button">Like (${p.likeCount})</button>
                <button onclick="commentOnPost(${p.id})" class="feed-button">Comment (${p.commentCount})</button>
              </div>
              ${commentsHTML}
            </div>
          </div>
        `;
      } else {
        const e = item.data;
        const eventDate = new Date(e.eventDate).toLocaleDateString('en-US', {
          weekday: 'long',
          month: 'long',
          day: 'numeric',
          year: 'numeric'
        });

        div.className = "feed-event";
        div.innerHTML = `
          <h4>
            <a href="event_detail.html?eventId=${e.id}">${e.title}</a>
            <small>Posted ${timestamp}</small>
          </h4>
          <p><strong>Genre:</strong> ${e.genre}</p>
          <p><strong>Location:</strong> ${e.location}</p>
          <p><strong>Event Date:</strong> ${eventDate}</p>
        `;
        if (e.imageUrl) {
          div.innerHTML += `
            <div class="feed-event-image">
              <img src="/${e.imageUrl}" alt="Event image" />
            </div>
          `;
        }
        
        if (loggedInUser.role.toLowerCase() === "dj") {
          const buttonContainer = document.createElement('div');
          buttonContainer.className = 'event-apply-button';
          const isAccepted = Array.isArray(e.performingDjs) && e.performingDjs.some(dj => dj.id === loggedInUser.id);
          const hasApplied = appliedEvents.includes(e.id) || isAccepted;

          const button = document.createElement('button');
          button.onclick = () => applyToEvent(e.id, button);
          button.className = `feed-button${hasApplied ? ' applied' : ''}`;
          button.disabled = hasApplied;
          button.textContent = hasApplied ? 'Applied' : 'Apply';

          buttonContainer.appendChild(button);
          div.appendChild(buttonContainer);
        }
      }

      feed.appendChild(div);
    }
  } catch (err) {
    console.error("Error loading feed:", err);
    feed.innerHTML = "<p>Failed to load feed. Please try again later.</p>";
  }
});

// ✅ Like a post
function likePost(postId, button) {
  fetch(`/api/social/posts/${postId}/like?userId=${loggedInUser.id}`, {
    method: 'POST'
  })
    .then(res => res.json())
    .then(data => {
      button.textContent = `Like (${data.count})`;
      if (data.liked) {
        button.classList.add("applied");
      } else {
        button.classList.remove("applied");
      }
    })
    .catch(err => {
      console.error("Like error:", err);
      alert("Could not like post.");
    });
}

// ✅ Comment on a post
function commentOnPost(postId) {
  const content = prompt("Enter your comment:");
  if (!content) return;

  fetch(`/api/social/posts/${postId}/comments?userId=${loggedInUser.id}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content })
  })
    .then(res => res.json())
    .then(() => {
      alert("Comment added!");
      location.reload(); // Refresh to show new comment
    })
    .catch(err => {
      console.error("Comment error:", err);
      alert("Could not post comment.");
    });
}

// ✅ Style for applied buttons
const style = document.createElement("style");
style.textContent = `
  .feed-button.applied {
    background-color: #4CAF50;
    cursor: not-allowed;
    opacity: 0.8;
  }
`;
document.head.appendChild(style);
