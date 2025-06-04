// Global user variables
const loggedInUser = JSON.parse(localStorage.getItem("echoloopUser"));
const loggedInUserId = loggedInUser?.id;

// Remove interest from event
function removeInterest(eventId, button) {
  if (!loggedInUser) {
    alert("You must be logged in to remove interest.");
    return;
  }

  fetch(`/api/events/${loggedInUser.id}/interested/${eventId}`, { 
    method: "DELETE" 
  })
  .then(res => {
    if (res.ok) {
      // Remove the entire event card
      const eventCard = button.closest('.feed-event');
      eventCard.remove();

      // If no more events, show the empty message
      const container = document.getElementById("interestingEvents");
      if (container.children.length === 0) {
        container.innerHTML = "<p>No interesting events found. Check out the Suggestions page to find events!</p>";
      }
    } else {
      alert("Failed to remove interest.");
    }
  })
  .catch(err => {
    console.error("Interest removal error:", err);
    alert("Failed to remove interest. Please try again.");
  });
}

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

// Start a conversation from profile
function startConversation(userId, username) {
  // If called from profile page with parameters
  if (userId && username) {
    window.location.href = `messages.html?userId=${userId}&username=${username}`;
    return;
  }
  
  // If called from profile page without parameters (old method)
  const urlParams = new URLSearchParams(window.location.search);
  const profileUserId = urlParams.get('id');
  const profileUsername = document.getElementById('profileName')?.textContent || 
                        document.getElementById('communityName')?.textContent;
  
  if (profileUserId && profileUsername) {
    window.location.href = `messages.html?userId=${profileUserId}&username=${profileUsername}`;
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
          if (user.id === loggedInUserId) return; // Skip self
          const profilePage = `profile.html?id=${user.id}`;
          const div = document.createElement("div");
          div.classList.add("search-result-item");
          div.innerHTML = `
            <div class="search-result-content">
            <a href="${profilePage}">
              <strong>${user.username}</strong>
            </a> — ${user.genres || "no genres"}
            </div>
          `;
          container.appendChild(div);
        });
      }

      if (userResults.communities?.length > 0) {
        const communityHeader = document.createElement("h4");
        communityHeader.textContent = "Event Communities";
        container.appendChild(communityHeader);

        userResults.communities.forEach(user => {
          if (user.id === loggedInUserId) return; // Skip self
          const profilePage = `community_profile.html?id=${user.id}`;
          const div = document.createElement("div");
          div.classList.add("search-result-item");
          div.innerHTML = `
            <div class="search-result-content">
            <a href="${profilePage}">
              <strong>${user.username}</strong>
            </a> — Event Community
            </div>
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
  const messagesItem = {
    href: "messages.html",
    text: "Messages",
    html: `
      <div class="nav-link-content">
        <span>Messages</span>
        <span class="message-notification-badge" id="messageNotificationBadge" style="display: none">0</span>
      </div>
    `
  };

  const navItems = role === "event community"
    ? [
        { href: "feed.html", text: "Feed" },
        { href: "suggestions.html", text: "Suggestions" },
        { href: "create_post.html", text: "Create Post" },
        { href: "create_event.html", text: "Create Event" },
        messagesItem,
        { href: "account_community.html", text: "Management", preserveText: true },
        { href: "my_profile.html", text: "Account" }
      ]
    : [
        { href: "feed.html", text: "Feed" },
        { href: "suggestions.html", text: "Suggestions" },
        { href: "create_post.html", text: "Create Post" },
        { href: "opportunities.html", text: "Opportunities" },
        messagesItem,
        { href: "my_applications.html", text: "My Applications" },
        { href: "my_profile.html", text: "Account" }
      ];

  navLinks.innerHTML = "";
  navItems.forEach(item => {
    const link = document.createElement("a");
    link.href = item.href;
    
    // Set the text content or HTML
    if (item.html) {
      link.innerHTML = item.html;
    } else {
      link.textContent = item.text;
    }
    
    // Add active class if current page matches
    const currentPage = window.location.pathname.split('/').pop() || 'feed.html';
    if (currentPage === item.href) {
      link.classList.add("active");
      // Only preserve text if specified
      if (!item.preserveText) {
        link.textContent = item.text;
      }
    }
    
    navLinks.appendChild(link);
  });

  // Start checking for unread messages
  startUnreadMessagesCheck();
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
        const imageHTML = p.imageUrl ? `<img src="${p.imageUrl}" alt="Post image" class="feed-post-image" />` : "";

        const commentsHTML = p.recentComments && p.recentComments.length > 0
          ? `<div class="feed-comments" id="comments-${p.id}">
              ${p.recentComments.slice(0, 2).map(c => `
                <div class="feed-comment">
                  <a href="${c.userRole?.toLowerCase() === 'event community' ? 'community_profile.html' : 'profile.html'}?id=${c.userId}">
                    <strong>${c.username}</strong>
                  </a>: ${c.content}
                </div>
              `).join("")}
              ${p.recentComments.length > 2 ? `
                <div class="feed-comments-hidden" id="hidden-comments-${p.id}" style="display: none;">
                  ${p.recentComments.slice(2).map(c => `
                    <div class="feed-comment">
                      <a href="${c.userRole?.toLowerCase() === 'event community' ? 'community_profile.html' : 'profile.html'}?id=${c.userId}">
                        <strong>${c.username}</strong>
                      </a>: ${c.content}
                    </div>
                  `).join("")}
                </div>
                <button onclick="toggleComments(${p.id})" class="feed-button-text" id="toggle-comments-${p.id}">
                  See ${p.recentComments.length - 2} more comments
                </button>
              ` : ''}
            </div>`
          : `<div class="feed-comments"><em>No comments yet</em></div>`;

        div.className = "feed-post";
        div.id = `post-${p.id}`;
        div.innerHTML = `
          <div class="feed-post-header">
            <div class="feed-avatar">${initials}</div>
            <div class="feed-post-content">
              <h4>
                <a href="${p.userRole?.toLowerCase() === 'event community' ? 'community_profile.html' : 'profile.html'}?id=${p.userId}">
                  ${p.username}
                </a>
                <small>${timestamp}</small>
              </h4>
              <p>${p.content}</p>
              ${imageHTML}
              <div class="post-actions">
                <button onclick="likePost(${p.id}, this)" class="feed-button">Like (${p.likeCount})</button>
                <button onclick="toggleCommentInput(${p.id})" class="feed-button">Comment (${p.commentCount})</button>
              </div>
              <div class="feed-comment-input" id="commentInput-${p.id}" style="display: none;">
                <textarea placeholder="Write a comment..." class="feed-textarea" rows="2"></textarea>
                <div class="feed-comment-actions">
                  <button onclick="submitComment(${p.id})" class="feed-button">Post</button>
                  <button onclick="toggleCommentInput(${p.id})" class="feed-button">Cancel</button>
                </div>
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
          <p><strong>Event Community:</strong> <a href="community_profile.html?id=${e.communityId}">${e.communityName}</a></p>
        `;
        if (e.imageUrl) {
          div.innerHTML += `
            <div class="feed-event-image">
              <img src="${e.imageUrl}" alt="Event image" />
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

// ✅ Style for buttons and badges
const globalStyles = document.createElement("style");
globalStyles.textContent = `
  .feed-button.applied {
    background-color: #4CAF50;
    cursor: not-allowed;
    opacity: 0.8;
  }

  .nav-link-content {
    position: relative;
    display: inline-flex;
    align-items: center;
    gap: 4px;
  }

  .message-notification-badge {
    position: absolute;
    top: -6px;
    right: -10px;
    background: var(--accent-primary);
    color: white;
    border-radius: 12px;
    padding: 1px 6px;
    font-size: 0.7rem;
    min-width: 16px;
    height: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: 600;
    box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  }

  .feed-nav-links a {
    position: relative;
  }

  .feed-comment-input {
    margin-top: 12px;
    border-radius: 8px;
    overflow: hidden;
  }

  .feed-comment-input textarea {
    width: 100%;
    padding: 8px 12px;
    border: 1px solid var(--border);
    border-radius: 8px;
    margin-bottom: 8px;
    font-family: inherit;
    font-size: 0.95rem;
    resize: none;
    background: var(--bg-primary);
  }

  .feed-comment-input textarea:focus {
    outline: none;
    border-color: var(--accent-primary);
  }

  .feed-comment-actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
    margin-bottom: 12px;
  }

  .feed-comments {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid var(--border);
  }

  .feed-comment {
    padding: 8px;
    margin-bottom: 8px;
    background: var(--bg-primary);
    border-radius: 8px;
    font-size: 0.95rem;
  }

  .feed-button-text {
    background: none;
    border: none;
    color: var(--accent-primary);
    font-size: 0.9rem;
    padding: 4px 8px;
    cursor: pointer;
    font-weight: 500;
    transition: color 0.2s;
    margin-top: 8px;
  }

  .feed-button-text:hover {
    color: var(--accent-secondary);
    text-decoration: underline;
  }

  .feed-comments-hidden {
    margin-top: 8px;
  }
`;
document.head.appendChild(globalStyles);

// Update profile UI based on user data
function updateProfileUI(userData) {
  document.getElementById('profileName').textContent = userData.username;
  document.getElementById('profileRole').textContent = userData.role;
  document.getElementById('followingCount').textContent = userData.following?.length || 0;
  document.getElementById('followerCount').textContent = userData.followers?.length || 0;

  const isOwnProfile = userData.id === loggedInUserId;
  document.getElementById('logoutBtn').style.display = isOwnProfile ? 'inline-block' : 'none';
  document.getElementById('editBtn').style.display = isOwnProfile ? 'inline-block' : 'none';
  document.getElementById('followBtn').style.display = isOwnProfile ? 'none' : 'inline-block';
  document.getElementById('messageBtn').style.display = isOwnProfile ? 'none' : 'inline-block';

  // ... rest of the existing updateProfileUI code ...
}

function updateNavLinks() {
    const user = JSON.parse(localStorage.getItem('echoloopUser'));
    const navLinksContainer = document.querySelector('.feed-nav-links');
    
    if (user) {
        navLinksContainer.innerHTML = `
            <a href="feed.html" class="nav-link">
                <i class="fas fa-home"></i>
                <span>Home</span>
            </a>
            <a href="search.html" class="nav-link">
                <i class="fas fa-search"></i>
                <span>Search</span>
            </a>
            <a href="messages.html" class="nav-link" id="messagesLink">
                <div class="nav-link-content">
                    <i class="fas fa-envelope"></i>
                    <span>Messages</span>
                    <span class="message-notification-badge" id="messageNotificationBadge" style="display: none">0</span>
                </div>
            </a>
            <a href="my_profile.html" class="nav-link">
                <i class="fas fa-user"></i>
                <span>Account</span>
            </a>
        `;

        // Start checking for unread messages
        startUnreadMessagesCheck();
    } else {
        navLinksContainer.innerHTML = `
            <a href="feed.html" class="nav-link">
                <i class="fas fa-home"></i>
                <span>Home</span>
            </a>
            <a href="search.html" class="nav-link">
                <i class="fas fa-search"></i>
                <span>Search</span>
            </a>
        `;
    }
}

// Function to check for unread messages
async function checkUnreadMessages() {
    const user = JSON.parse(localStorage.getItem('echoloopUser'));
    if (!user) return;

    try {
        const response = await fetch('/api/messages/unread-count', {
            headers: {
                'User-Id': user.id
            }
        });

        if (!response.ok) throw new Error('Failed to fetch unread count');

        const { unreadCount } = await response.json();
        const badge = document.getElementById('messageNotificationBadge');
        
        if (badge) {
            if (unreadCount > 0) {
                badge.textContent = unreadCount > 99 ? '99+' : unreadCount;
                badge.style.display = 'flex';
            } else {
                badge.style.display = 'none';
            }
        }
    } catch (error) {
        console.error('Error checking unread messages:', error);
    }
}

// Function to start periodic checks for unread messages
function startUnreadMessagesCheck() {
    // Check immediately
    checkUnreadMessages();
    
    // Then check every 30 seconds
    setInterval(checkUnreadMessages, 30000);
}

// Comment functions
function toggleCommentInput(postId) {
  const commentInput = document.getElementById(`commentInput-${postId}`);
  commentInput.style.display = commentInput.style.display === 'none' ? 'block' : 'none';
  if (commentInput.style.display === 'block') {
    commentInput.querySelector('textarea').focus();
  }
}

function submitComment(postId) {
  const commentInput = document.getElementById(`commentInput-${postId}`);
  const content = commentInput.querySelector('textarea').value.trim();
  
  if (!content) return;

  fetch(`/api/social/posts/${postId}/comments?userId=${loggedInUser.id}`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ content })
  })
    .then(res => res.json())
    .then(() => {
      location.reload(); // Refresh to show new comment
    })
    .catch(err => {
      console.error("Comment error:", err);
      alert("Could not post comment.");
    });
}

// Toggle comments visibility
function toggleComments(postId) {
  const hiddenComments = document.getElementById(`hidden-comments-${postId}`);
  const toggleButton = document.getElementById(`toggle-comments-${postId}`);
  const isHidden = hiddenComments.style.display === 'none';
  
  hiddenComments.style.display = isHidden ? 'block' : 'none';
  const commentCount = hiddenComments.querySelectorAll('.feed-comment').length;
  toggleButton.textContent = isHidden ? 'Show less' : `See ${commentCount} more comments`;
}
