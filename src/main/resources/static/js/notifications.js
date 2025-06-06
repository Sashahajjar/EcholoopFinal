class NotificationManager {
    constructor(userId) {
        if (!userId) {
            console.error('NotificationManager requires a userId');
            return;
        }
        this.userId = userId;
        this.socket = null;
        this.stompClient = null;
        this.unreadCount = 0;
        this.notifications = [];
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
        this.reconnectDelay = 2000;
        
        // Wait a short moment for the navigation bar to be ready
        setTimeout(() => {
            this.initializeUI();
            this.initializeWebSocket();
            this.loadNotifications();
        }, 100);
    }

    initializeWebSocket() {
        try {
            console.log('Initializing WebSocket connection...');
            const socket = new SockJS('/ws');
            this.stompClient = Stomp.over(socket);
            
            // Disable STOMP debug logging
            this.stompClient.debug = null;
            
            const connectCallback = () => {
                console.log('✅ WebSocket connected successfully');
                this.reconnectAttempts = 0; // Reset reconnect attempts on successful connection
                
                // Subscribe to personal notifications
                this.stompClient.subscribe(`/user/${this.userId}/queue/notifications`, (notification) => {
                    try {
                        const data = JSON.parse(notification.body);
                        this.handleNewNotification(data);
                    } catch (error) {
                        console.error('Error handling notification:', error);
                    }
                });

                // Subscribe to broadcast notifications
                this.stompClient.subscribe('/topic/notifications', (notification) => {
                    try {
                        const data = JSON.parse(notification.body);
                        this.handleNewNotification(data);
                    } catch (error) {
                        console.error('Error handling broadcast notification:', error);
                    }
                });
            };

            const errorCallback = (error) => {
                console.error('❌ WebSocket connection error:', error);
                this.handleReconnection();
            };

            this.stompClient.connect({}, connectCallback, errorCallback);
        } catch (error) {
            console.error('Failed to initialize WebSocket:', error);
            this.handleReconnection();
        }
    }

    handleReconnection() {
        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`🔄 Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
            setTimeout(() => {
                this.initializeWebSocket();
            }, this.reconnectDelay * this.reconnectAttempts);
        } else {
            console.error('❌ Max reconnection attempts reached');
        }
    }

    initializeUI() {
        try {
            // Find the navigation links container
            const navLinks = document.querySelector('.feed-nav-links');
            if (!navLinks) {
                console.error('Navigation links container not found');
                return;
            }

            // Check if notification wrapper already exists
            if (document.querySelector('.notification-wrapper')) {
                console.log('Notification wrapper already exists, skipping initialization');
                return;
            }

            // Create the notification wrapper
            const wrapper = document.createElement('div');
            wrapper.className = 'notification-wrapper';
            wrapper.innerHTML = `
                <button id="notification-bell" class="notification-bell" title="Notifications">
                    <i class="fas fa-bell"></i>
                    <span id="notification-count" class="notification-count ${this.unreadCount > 0 ? 'show' : ''}">
                        ${this.unreadCount}
                    </span>
                </button>
                <div id="notification-dropdown" class="notification-dropdown">
                    <div id="notification-list" class="notification-list"></div>
                </div>
            `;

            // Insert the notification wrapper into the navigation
            navLinks.appendChild(wrapper);

            // Add event listeners
            const bell = wrapper.querySelector('#notification-bell');
            const dropdown = wrapper.querySelector('#notification-dropdown');

            bell.addEventListener('click', (e) => {
                e.preventDefault();
                e.stopPropagation();
                dropdown.classList.toggle('show');
            });

            // Close dropdown when clicking outside
            document.addEventListener('click', (e) => {
                if (!wrapper.contains(e.target)) {
                    dropdown.classList.remove('show');
                }
            });

            // Request notification permission
            if ("Notification" in window) {
                Notification.requestPermission();
            }
        } catch (error) {
            console.error('Error initializing notification UI:', error);
        }
    }

    async loadNotifications() {
        try {
            const response = await fetch(`/api/notifications?userId=${this.userId}`);
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            this.notifications = await response.json();
            this.updateUnreadCount();
            this.renderNotifications();
        } catch (error) {
            console.error('Error loading notifications:', error);
        }
    }

    handleNewNotification(notification) {
        try {
            console.log('📬 Received notification:', notification);
            
            // Validate notification data
            if (!notification || !notification.type) {
                console.error('Invalid notification received:', notification);
                return;
            }
            
            // Ensure relatedEntityId is present for post-related notifications
            if ((notification.type === 'POST_LIKE' || notification.type === 'POST_COMMENT') && !notification.relatedEntityId) {
                console.error('Missing relatedEntityId for post notification:', notification);
                return;
            }
            
            this.notifications.unshift(notification);
            this.unreadCount++;
            this.updateUnreadCount();
            this.renderNotifications();

            // Show a browser notification if permission is granted
            if (Notification.permission === "granted") {
                new Notification(notification.type, {
                    body: notification.message,
                    icon: "/images/logoecho.png"
                });
            }
        } catch (error) {
            console.error('Error handling new notification:', error);
        }
    }

    updateUnreadCount() {
        try {
            const countElement = document.getElementById('notification-count');
            if (countElement) {
                this.unreadCount = this.notifications.filter(n => !n.read).length;
                countElement.textContent = this.unreadCount;
                countElement.classList.toggle('show', this.unreadCount > 0);
            }
        } catch (error) {
            console.error('Error updating unread count:', error);
        }
    }

    renderNotifications() {
        try {
            const listElement = document.getElementById('notification-list');
            if (!listElement) return;

            listElement.innerHTML = this.notifications.length ? 
                this.notifications.map(notification => this.renderNotification(notification)).join('') :
                '<div class="no-notifications">No notifications</div>';
        } catch (error) {
            console.error('Error rendering notifications:', error);
        }
    }

    renderNotification(notification) {
        try {
            console.group('Rendering Notification');
            console.log('Full notification object:', notification);
            
            const timeString = new Date(notification.createdAt).toLocaleString();
            let displayMessage = notification.message;
            let profileUrl = '';
            
            if (notification.type === 'NEW_FOLLOWER') {
                // Get the follower's ID from relatedEntityId
                const followerId = notification.relatedEntityId;
                
                // Parse the notification message
                const [baseMessage, followerType] = displayMessage.split('|');
                displayMessage = baseMessage; // Clean message for display
                
                console.log('Processing follow notification:', {
                    originalMessage: notification.message,
                    baseMessage,
                    followerType,
                    followerId
                });
                
                // Set profile URL based on follower type
                if (followerType && followerType.trim().toLowerCase() === 'dj') {
                    profileUrl = `/profile.html?id=${followerId}`;
                } else if (followerType && followerType.trim().toLowerCase() === 'community') {
                    profileUrl = `/community_profile.html?id=${followerId}`;
                } else {
                    console.warn('No follower type found in message, using fallback logic');
                    // Fallback logic
                    const username = baseMessage.split(' started following')[0];
                    if (username.includes('.com')) {
                        profileUrl = `/community_profile.html?id=${followerId}`;
                    } else {
                        profileUrl = `/profile.html?id=${followerId}`;
                    }
                }
            } else if (notification.type === 'POST_LIKE' || notification.type === 'POST_COMMENT') {
                // For likes and comments, redirect to the specific post in the feed
                const postId = notification.relatedEntityId;
                profileUrl = `/feed.html#post-${postId}`;
            } else if (notification.type === 'EVENT_APPLICATION') {
                // For event applications, redirect to the specific application in account_community.html
                const eventId = notification.relatedEntityId;
                // Extract DJ username from the message (format: "<DJUsername> applied to your event: <EventTitle>")
                const djUsername = notification.message.split(' applied to')[0];
                
                // Create click handler
                const handleClick = async (event) => {
                    event.preventDefault();
                    event.stopPropagation();
                    console.log('Notification clicked - Processing:', {
                        type: notification.type,
                        eventId,
                        djUsername,
                        originalMessage: notification.message
                    });
                    
                    // Mark notification as read first
                    if (!notification.read) {
                        await this.markAsRead(notification.id);
                    }
                    
                    try {
                        // Get the DJ's ID
                        const response = await fetch(`/api/users/by-username/${djUsername}`);
                        if (response.ok) {
                            const djData = await response.json();
                            // Navigate to the specific application
                            const applicationHash = `#application-${eventId}-${djData.id}`;
                            
                            if (window.location.pathname.endsWith('/account_community.html')) {
                                // We're on the community account page, just update the hash and scroll
                                window.location.hash = applicationHash;
                            } else {
                                // We're on a different page, navigate to account_community with hash
                                window.location.href = `/account_community.html${applicationHash}`;
                            }
                        } else {
                            // Fallback to just the event if we can't get the DJ ID
                            const eventHash = `#event-${eventId}`;
                            if (window.location.pathname.endsWith('/account_community.html')) {
                                window.location.hash = eventHash;
                            } else {
                                window.location.href = `/account_community.html${eventHash}`;
                            }
                        }
                    } catch (error) {
                        console.error('Error processing notification click:', error);
                        // Fallback to just the event
                        const eventHash = `#event-${eventId}`;
                        if (window.location.pathname.endsWith('/account_community.html')) {
                            window.location.hash = eventHash;
                        } else {
                            window.location.href = `/account_community.html${eventHash}`;
                        }
                    }
                };
                
                const handlerId = `handleNotificationClick_${notification.id}`;
                window[handlerId] = handleClick;
                
                return `
                    <div class="notification-item ${notification.read ? '' : 'unread'}" 
                         data-id="${notification.id}"
                         style="cursor: pointer"
                         onclick="${handlerId}(event)">
                        <div class="notification-content">
                            <p>${displayMessage}</p>
                            <small>${timeString}</small>
                        </div>
                    </div>
                `;
            } else if (notification.type === 'APPLICATION_ACCEPTED' || notification.type === 'APPLICATION_REJECTED') {
                // For application status updates, redirect to the event details
                const eventId = notification.relatedEntityId;
                profileUrl = `/event_detail.html?eventId=${eventId}`;
            }
            
            // Create click handler
            const handleClick = async (event) => {
                event.preventDefault();
                event.stopPropagation();
                console.log('Notification clicked - Redirecting:', {
                    type: notification.type,
                    url: profileUrl,
                    originalMessage: notification.message
                });
                
                // Mark notification as read
                if (!notification.read) {
                    await this.markAsRead(notification.id);
                }
                
                if (profileUrl) {
                    // If we're already on the feed page and it's a post notification
                    if (notification.type === 'POST_LIKE' || notification.type === 'POST_COMMENT') {
                        const currentPath = window.location.pathname;
                        const postId = notification.relatedEntityId;
                        
                        if (currentPath.endsWith('/feed.html')) {
                            // We're on the feed page, just update the hash and scroll
                            window.location.hash = `post-${postId}`;
                            if (window.handleHashNavigation) {
                                window.handleHashNavigation();
                            }
                        } else {
                            // We're on a different page, navigate to feed with hash
                            window.location.href = profileUrl;
                        }
                    } else if (notification.type === 'EVENT_APPLICATION') {
                        const currentPath = window.location.pathname;
                        const eventId = notification.relatedEntityId;
                        
                        if (currentPath.endsWith('/account_community.html')) {
                            // We're on the community account page, just update the hash and scroll
                            window.location.hash = `event-${eventId}`;
                            // The scroll logic is already in the DOMContentLoaded handler
                        } else {
                            // We're on a different page, navigate to account_community with hash
                            window.location.href = profileUrl;
                        }
                    } else if (notification.type === 'APPLICATION_ACCEPTED' || notification.type === 'APPLICATION_REJECTED') {
                        // For application status updates, just navigate to the event detail page
                        window.location.href = profileUrl;
                    } else {
                        // For other types of notifications, just navigate
                        window.location.href = profileUrl;
                    }
                }
            };
            
            const handlerId = `handleNotificationClick_${notification.id}`;
            window[handlerId] = handleClick;
            
            // Add appropriate icon based on notification type
            let icon = '';
            if (notification.type === 'APPLICATION_ACCEPTED') {
                icon = '<i class="fas fa-check" style="color: #4CAF50; margin-right: 8px;"></i>';
            } else if (notification.type === 'APPLICATION_REJECTED') {
                icon = '<i class="fas fa-times" style="color: #f44336; margin-right: 8px;"></i>';
            }
            
            return `
                <div class="notification-item ${notification.read ? '' : 'unread'}" 
                     data-id="${notification.id}"
                     style="cursor: pointer"
                     onclick="${handlerId}(event)">
                    <div class="notification-content">
                        <p>${icon}${displayMessage}</p>
                        <small>${timeString}</small>
                    </div>
                </div>
            `;
        } catch (error) {
            console.error('Error rendering notification item:', error);
            console.groupEnd();
            return '';
        }
    }

    async markAsRead(notificationId) {
        try {
            const response = await fetch(`/api/notifications/${notificationId}/read`, {
                method: 'POST'
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const notification = this.notifications.find(n => n.id === notificationId);
            if (notification) {
                notification.read = true;
                this.updateUnreadCount();
                this.renderNotifications();
            }
        } catch (error) {
            console.error('Error marking notification as read:', error);
        }
    }

    async getDjIdFromUsername(username) {
        try {
            const response = await fetch(`/api/users/by-username/${username}`);
            if (response.ok) {
                const user = await response.json();
                return user.id;
            }
        } catch (error) {
            console.error('Error getting DJ ID:', error);
        }
        return null;
    }
}