// Common functionality for all pages
document.addEventListener("DOMContentLoaded", () => {
    // Check if user is logged in
    const loggedInUser = JSON.parse(localStorage.getItem('echoloopUser'));
    if (!loggedInUser && !window.location.pathname.endsWith('index.html')) {
        window.location.href = "index.html";
        return;
    }

    // Remove any existing navbars to prevent duplicates
    const existingNavbars = document.querySelectorAll('.feed-navbar, .main-nav');
    existingNavbars.forEach(navbar => navbar.remove());

    // Inject the navbar
    injectNavbar();

    // Initialize notifications if user is logged in
    if (loggedInUser) {
        initializeNotifications();
    }
});

// Global function for mobile menu toggle
window.toggleMobileMenu = function() {
    const navLinks = document.querySelector('.feed-nav-links');
    if (navLinks) {
        navLinks.classList.toggle('show');
    }
};

function injectNavbar() {
    const loggedInUser = JSON.parse(localStorage.getItem('echoloopUser'));
    
    // Determine role-specific navigation items
    const role = loggedInUser?.role?.toLowerCase();
    const roleSpecificLinks = role === "event community"
        ? `<a href="feed.html" data-page="feed">Feed</a>
           <a href="suggestions.html" data-page="suggestions">Suggestions</a>
           <a href="create_post.html" data-page="create_post">Create Post</a>
           <a href="create_event.html" data-page="create_event">Create Event</a>
           <a href="messages.html" data-page="messages">Messages</a>
           <a href="account_community.html" data-page="account_community">Management</a>
           <a href="my_profile.html" data-page="my_profile">Account</a>`
        : `<a href="feed.html" data-page="feed">Feed</a>
                <a href="suggestions.html" data-page="suggestions">Suggestions</a>
                <a href="create_post.html" data-page="create_post">Create Post</a>
           <a href="opportunities.html" data-page="opportunities">Opportunities</a>
                <a href="messages.html" data-page="messages">Messages</a>
           <a href="my_applications.html" data-page="my_applications">My Applications</a>
           <a href="my_profile.html" data-page="my_profile">Account</a>`;
    
    // Check if we're on the Management page
    const currentPage = window.location.pathname.split('/').pop() || 'feed.html';
    const isManagementPage = currentPage === 'account_community.html';

    const notificationBell = !isManagementPage ? `
                <div class="notification-wrapper">
                    <button id="notification-bell" class="notification-bell" title="Notifications">
                        <i class="fas fa-bell"></i>
                        <span id="notification-count" class="notification-count">0</span>
                    </button>
                    <div id="notification-dropdown" class="notification-dropdown">
                        <div id="notification-list" class="notification-list"></div>
                    </div>
                </div>
    ` : '';
    
    const navbarHTML = `
        <nav class="feed-navbar">
            <a href="feed.html" class="feed-logo">
                <img src="images/logoecho.png" alt="EchoLoop" class="feed-logo-img"/>
            </a>
            <button class="mobile-menu-button" onclick="toggleMobileMenu()">
                <i class="fas fa-bars"></i>
            </button>
            <div class="feed-nav-links">
                ${roleSpecificLinks}
                ${notificationBell}
            </div>
        </nav>
    `;

    // Insert at the beginning of the body
    document.body.insertAdjacentHTML('afterbegin', navbarHTML);

    // Get navbar elements
    const navLinksContainer = document.querySelector('.feed-nav-links');
    const navLinks = document.querySelectorAll('.feed-nav-links a[data-page]');
    const mobileMenuBtn = document.querySelector('.mobile-menu-button');
    
    // Update active state for current page
    navLinks.forEach(link => {
        if (link.getAttribute('data-page') === currentPage.split('.')[0]) {
            link.classList.add('active');
        }
    });

    // Initialize mobile menu functionality
    if (mobileMenuBtn && navLinksContainer) {
        // Close mobile menu when clicking outside
        document.addEventListener('click', (e) => {
            if (!navLinksContainer.contains(e.target) && 
                !mobileMenuBtn.contains(e.target) && 
                navLinksContainer.classList.contains('show')) {
                navLinksContainer.classList.remove('show');
            }
        });

        // Close mobile menu when window is resized above mobile breakpoint
        window.addEventListener('resize', () => {
            if (window.innerWidth > 768) {
                navLinksContainer.classList.remove('show');
            }
        });

        // Close mobile menu when clicking a link
        navLinksContainer.addEventListener('click', (e) => {
            if (e.target.tagName === 'A') {
                navLinksContainer.classList.remove('show');
            }
        });
    }
}

function initializeNotifications() {
    const loggedInUser = JSON.parse(localStorage.getItem('echoloopUser'));
    if (!loggedInUser) return;

    // Initialize NotificationManager
    if (window.NotificationManager) {
        window.notificationManager = new NotificationManager(loggedInUser.id);
    }

    // Start checking for unread messages
    startUnreadMessagesCheck();
}

// Function to check for unread messages
async function startUnreadMessagesCheck() {
    const checkUnreadMessages = async () => {
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
    };

    // Check immediately
    await checkUnreadMessages();
    
    // Then check every 30 seconds
    setInterval(checkUnreadMessages, 30000);
}

// Scroll to post with retries and proper error handling
function scrollToPost(postId, retryCount = 0, maxRetries = 20) {
  try {
    console.log('Attempting to scroll to post:', { postId, retryCount });
    
    const postElement = document.getElementById(`post-${postId}`);
    if (postElement) {
      // Remove highlight from any previously highlighted posts
      document.querySelectorAll('.feed-post.highlighted').forEach(post => {
        post.classList.remove('highlighted');
      });
      
      // Add highlight to the target post
      postElement.classList.add('highlighted');
      
      // Get the navbar height for offset
      const navbar = document.querySelector('.feed-navbar');
      const navbarHeight = navbar ? navbar.offsetHeight : 0;
      
      // Calculate the scroll position with offset
      const elementPosition = postElement.getBoundingClientRect().top + window.pageYOffset;
      const offsetPosition = elementPosition - navbarHeight - 20; // 20px extra padding
      
      // Scroll to the post with offset
      window.scrollTo({
        top: offsetPosition,
        behavior: 'smooth'
      });
      
      console.log('Successfully scrolled to post:', {
        postId,
        retryCount,
        elementPosition,
        offsetPosition
      });

      return true;
    } else if (retryCount < maxRetries) {
      // If post not found and we haven't exceeded max retries,
      // try again after a short delay
      console.log(`Post ${postId} not found, retrying... (${retryCount + 1}/${maxRetries})`);
      setTimeout(() => scrollToPost(postId, retryCount + 1, maxRetries), 100);
      return false;
    } else {
      console.warn('Target post not found after max retries:', postId);
      return false;
    }
  } catch (err) {
    console.error('Error scrolling to post:', err);
    return false;
  }
}

// Handle hash-based navigation
function handleHashNavigation() {
  const hash = window.location.hash;
  if (hash && hash.startsWith('#post-')) {
    const postId = hash.replace('#post-', '');
    scrollToPost(postId);
  }
}

// Export functions for use in other files
window.scrollToPost = scrollToPost;
window.handleHashNavigation = handleHashNavigation;

// Add highlight animation styles
const highlightStyles = document.createElement("style");
highlightStyles.textContent = `
  .feed-post {
    position: relative;
    transition: background-color 0.3s ease;
  }

  .feed-post.highlighted {
    background-color: rgba(124, 92, 255, 0.1);
    animation: highlight-fade 2s forwards;
  }

  .feed-post.highlighted::before {
    content: '';
    position: absolute;
    left: -2px;
    top: 0;
    bottom: 0;
    width: 4px;
    background-color: var(--accent-primary);
    animation: highlight-bar 2s forwards;
  }

  @keyframes highlight-fade {
    0% {
      background-color: rgba(124, 92, 255, 0.1);
    }
    100% {
      background-color: transparent;
    }
  }

  @keyframes highlight-bar {
    0%, 80% {
      opacity: 1;
    }
    100% {
      opacity: 0;
    }
  }
`;
document.head.appendChild(highlightStyles); 