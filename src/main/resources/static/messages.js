// Get current user ID from localStorage
const currentUser = JSON.parse(localStorage.getItem('echoloopUser'));
const currentUserId = currentUser ? currentUser.id : null;
if (!currentUserId) {
    console.error('User ID not found');
}

let activeConversationUserId = null;
let activeConversationUsername = null;
let messagePollingInterval = null;
let allConversations = []; // Store all conversations for filtering

// DOM Elements
const conversationsList = document.getElementById('conversationsList');
const conversationsContent = document.getElementById('conversationsContent');
const conversationSearch = document.getElementById('conversationSearch');
const chatMessages = document.getElementById('chatMessages');
const chatHeader = document.getElementById('chatHeader');
const messageForm = document.getElementById('messageForm');
const messageInput = document.getElementById('messageInput');
const emptyState = document.getElementById('emptyState');
const chatContent = document.getElementById('chatContent');

// Search functionality
conversationSearch.addEventListener('input', (e) => {
    const searchTerm = e.target.value.toLowerCase().trim();
    filterConversations(searchTerm);
});

function filterConversations(searchTerm) {
    if (!searchTerm) {
        displayConversations(allConversations);
        return;
    }

    const filteredConversations = allConversations.filter(conv => {
        const usernameMatch = conv.username.toLowerCase().includes(searchTerm);
        const messageMatch = conv.lastMessage && conv.lastMessage.toLowerCase().includes(searchTerm);
        return usernameMatch || messageMatch;
    });
    
    if (filteredConversations.length === 0) {
        conversationsContent.innerHTML = `
            <div class="no-results">
                <p>No conversations found matching "${searchTerm}"</p>
                <p>Try searching for a different term</p>
            </div>
        `;
        return;
    }
    
    displayConversations(filteredConversations);
}

// Check for new conversation parameter
const urlParams = new URLSearchParams(window.location.search);
const newChatUserId = urlParams.get('userId');
const newChatUsername = urlParams.get('username');
if (newChatUserId && newChatUsername) {
    startNewConversation(newChatUserId, newChatUsername);
}

// Mark all messages as read
async function markAllMessagesAsRead() {
    try {
        const response = await fetch('/api/messages/mark-all-read', {
            method: 'POST',
            headers: {
                'User-Id': currentUserId
            }
        });
        
        if (!response.ok) throw new Error('Failed to mark messages as read');
        
        // Update the notification badge in the navbar
        const badge = document.getElementById('messageNotificationBadge');
        if (badge) {
            badge.style.display = 'none';
            badge.textContent = '0';
        }
    } catch (error) {
        console.error('Error marking messages as read:', error);
    }
}

// Initialize the messaging interface
async function initializeMessaging() {
    await loadConversations();
    startMessagePolling();
    messageInput.disabled = false;
    // Mark all messages as read when opening the messages page
    await markAllMessagesAsRead();
}

// Select a conversation
async function selectConversation(userId, username) {
    if (!userId) {
        console.error('No user ID provided for conversation');
        return;
    }

    // Remove active class from all conversations
    document.querySelectorAll('.conversation-item').forEach(item => {
        item.classList.remove('active');
    });

    // Add active class to selected conversation
    const conversationItem = document.querySelector(`.conversation-item[data-user-id="${userId}"]`);
    if (conversationItem) {
        conversationItem.classList.add('active');
        // If username wasn't provided, get it from the conversation item
        if (!username) {
            const usernameElement = conversationItem.querySelector('.conversation-header span:last-child');
            username = usernameElement ? usernameElement.textContent : 'Unknown User';
        }
        
        // Remove unread badge from the conversation
        const unreadBadge = conversationItem.querySelector('.unread-badge');
        if (unreadBadge) {
            unreadBadge.remove();
        }
    }

    activeConversationUserId = userId;
    activeConversationUsername = username;
    chatHeader.innerHTML = `<h2>Chat with ${activeConversationUsername}</h2>`;
    
    // Show chat content and hide empty state
    emptyState.style.display = 'none';
    chatContent.style.display = 'flex';
    const chatInput = document.getElementById('chatInput');
    if (chatInput) {
        chatInput.style.display = 'block';
    }

    // Mark messages as read for this conversation
    try {
        const response = await fetch(`/api/messages/mark-read/${userId}`, {
            method: 'POST',
            headers: {
                'User-Id': currentUserId
            }
        });
        
        if (!response.ok) throw new Error('Failed to mark messages as read');
        
        // Update the conversation in allConversations to show no unread messages
        const conversationIndex = allConversations.findIndex(conv => conv.userId === userId);
        if (conversationIndex !== -1) {
            allConversations[conversationIndex].unreadCount = 0;
        }
        
        // Update the notification badge in the navbar
        const badge = document.getElementById('messageNotificationBadge');
        if (badge) {
            // Get total unread count from all conversations
            const totalUnread = allConversations.reduce((sum, conv) => sum + (conv.unreadCount || 0), 0);
            if (totalUnread === 0) {
                badge.style.display = 'none';
            } else {
                badge.textContent = totalUnread;
            }
        }
    } catch (error) {
        console.error('Error marking messages as read:', error);
    }

    await loadConversationMessages(userId);
}

// Start a new conversation
async function startNewConversation(userId, username) {
    if (!userId || !username) {
        console.error('Invalid user data for conversation:', { userId, username });
        return;
    }

    activeConversationUserId = userId;
    activeConversationUsername = username;
    chatHeader.innerHTML = `<h2>Chat with ${activeConversationUsername}</h2>`;
    chatMessages.innerHTML = '';
    
    // Show chat content, input, and hide empty state
    emptyState.style.display = 'none';
    chatContent.style.display = 'flex';
    const chatInput = document.getElementById('chatInput');
    if (chatInput) {
        chatInput.style.display = 'block';
    }
    
    // Add the new conversation to the list if it doesn't exist
    const existingConversation = document.querySelector(`.conversation-item[data-user-id="${userId}"]`);
    if (!existingConversation) {
        // Add to allConversations array to maintain state
        allConversations.unshift({
            userId: userId,
            username: username,
            lastMessage: 'No messages yet',
            lastMessageTime: new Date().toISOString(),
            unreadCount: 0,
            online: false
        });
        
        // Update the conversations display
        displayConversations(allConversations);
        
        // Add active class to the new conversation
        const newConversation = document.querySelector(`.conversation-item[data-user-id="${userId}"]`);
        if (newConversation) {
            newConversation.classList.add('active');
        }
    } else {
        selectConversation(userId, username);
    }

    // Load existing messages if any
    await loadConversationMessages(userId);
}

// Load all conversations
async function loadConversations() {
    try {
        const response = await fetch('/api/messages/conversations', {
            headers: {
                'User-Id': currentUserId
            }
        });
        
        if (!response.ok) throw new Error('Failed to load conversations');
        
        const conversations = await response.json();
        // Store conversations for filtering and ensure timestamps are properly formatted
        allConversations = conversations.map(conv => ({
            ...conv,
            lastMessageTime: conv.lastMessageTime ? new Date(conv.lastMessageTime).toISOString() : null
        }));
        
        displayConversations(allConversations);
        
        // If there's an active conversation, reselect it with the stored username
        if (activeConversationUserId) {
            const activeConv = allConversations.find(conv => conv.userId === activeConversationUserId);
            selectConversation(activeConversationUserId, activeConv ? activeConv.username : activeConversationUsername);
        }
    } catch (error) {
        console.error('Error loading conversations:', error);
        conversationsContent.innerHTML = '<div class="empty-state"><p>Failed to load conversations</p></div>';
    }
}

// Display conversations in the sidebar
function displayConversations(conversations) {
    if (!Array.isArray(conversations) || conversations.length === 0) {
        conversationsContent.innerHTML = `
            <div class="empty-state">
                <p>No conversations yet</p>
                <p>Visit user profiles to start chatting</p>
            </div>
        `;
        return;
    }

    // Create a new array for sorting to avoid modifying the original
    const sortedConversations = [...conversations].sort((a, b) => {
        // Ensure we have valid dates to compare
        const timeA = a.lastMessageTime ? new Date(a.lastMessageTime).getTime() : 0;
        const timeB = b.lastMessageTime ? new Date(b.lastMessageTime).getTime() : 0;
        
        if (timeA === timeB) {
            // If times are equal, sort by username as secondary criteria
            return (a.username || '').localeCompare(b.username || '');
        }
        return timeB - timeA; // Most recent first
    });

    conversationsContent.innerHTML = sortedConversations.map(conv => {
        // Ensure we have valid data before rendering
        const username = conv.username || 'Unknown User';
        const userId = conv.userId || '';
        const lastMessage = conv.lastMessage || 'No messages yet';
        const time = formatMessageTime(conv.lastMessageTime);
        const unreadCount = conv.unreadCount || 0;
        const isActive = userId === activeConversationUserId;
        const isOnline = conv.online || false;

        return `
            <div class="conversation-item${isActive ? ' active' : ''}" 
                 data-user-id="${userId}" 
                 onclick="selectConversation(${userId}, '${username}')">
                <div class="conversation-header">
                    <span class="user-status ${isOnline ? 'online' : 'offline'}"></span>
                    <span>${username}</span>
                    ${unreadCount > 0 ? `<span class="unread-badge">${unreadCount}</span>` : ''}
                </div>
                <div class="last-message">${lastMessage}</div>
                <div class="last-message-time">${time}</div>
            </div>
        `;
    }).join('');
}

// Helper function to format message time
function formatMessageTime(timestamp) {
    if (!timestamp) return '';
    
    try {
        const messageDate = new Date(timestamp);
        if (isNaN(messageDate.getTime())) return ''; // Invalid date
        
        const now = new Date();
        const diffInHours = (now - messageDate) / (1000 * 60 * 60);
        
        if (diffInHours < 24) {
            // Today - show time only
            return messageDate.toLocaleTimeString([], { 
                hour: '2-digit', 
                minute: '2-digit',
                hour12: true 
            });
        } else if (diffInHours < 48) {
            // Yesterday
            return 'Yesterday';
        } else if (diffInHours < 168) {
            // Within a week - show day name
            return messageDate.toLocaleDateString([], { weekday: 'short' });
        } else {
            // Older - show date
            return messageDate.toLocaleDateString([], { 
                month: 'short', 
                day: 'numeric'
            });
        }
    } catch (error) {
        console.error('Error formatting time:', error);
        return '';
    }
}

// Load messages for a specific conversation
async function loadConversationMessages(otherUserId) {
    try {
        const response = await fetch(`/api/messages/conversation/${otherUserId}`, {
            headers: {
                'User-Id': currentUserId
            }
        });
        
        if (!response.ok) throw new Error('Failed to load messages');
        
        const messages = await response.json();
        displayMessages(messages);
    } catch (error) {
        console.error('Error loading messages:', error);
    }
}

// Display messages in the chat area
function displayMessages(messages) {
    // Sort messages by timestamp in ascending order (oldest first)
    const sortedMessages = [...messages].sort((a, b) => new Date(a.sentAt) - new Date(b.sentAt));
    
    chatMessages.innerHTML = sortedMessages.map(msg => `
        <div class="message ${msg.senderId === currentUserId ? 'sent' : 'received'}">
            <div class="message-content">${msg.content}</div>
            <div class="message-time">
                ${new Date(msg.sentAt).toLocaleTimeString()} · 
                ${msg.readAt ? 'Read' : 'Delivered'}
            </div>
        </div>
    `).join('');
    
    // Scroll to bottom
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// Update conversation after sending a message
async function updateConversationAfterMessage(message) {
    try {
        // Find the conversation in the list
        const conversationIndex = allConversations.findIndex(
            conv => conv.userId === activeConversationUserId
        );

        if (conversationIndex !== -1) {
            // Update the conversation with new message info
            allConversations[conversationIndex] = {
                ...allConversations[conversationIndex],
                lastMessage: message.content,
                lastMessageTime: new Date().toISOString()
            };

            // Re-display conversations to update order
            displayConversations(allConversations);
        }
    } catch (error) {
        console.error('Error updating conversation:', error);
    }
}

// Modify the sendMessage function to update conversation order
async function sendMessage(content) {
    if (!activeConversationUserId) return;
    
    try {
        const response = await fetch('/api/messages/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'User-Id': currentUserId
            },
            body: JSON.stringify({
                recipientId: activeConversationUserId,
                content: content
            })
        });
        
        if (!response.ok) throw new Error('Failed to send message');
        
        const message = await response.json();
        appendMessage(message);
        messageInput.value = '';
        
        // Update conversation order after sending message
        await updateConversationAfterMessage(message);
    } catch (error) {
        console.error('Error sending message:', error);
    }
}

// Append a new message to the chat
function appendMessage(message) {
    const messageElement = document.createElement('div');
    messageElement.className = `message ${message.senderId === currentUserId ? 'sent' : 'received'}`;
    messageElement.innerHTML = `
        <div class="message-content">${message.content}</div>
        <div class="message-time">
            ${new Date(message.sentAt).toLocaleTimeString()} · 
            ${message.readAt ? 'Read' : 'Delivered'}
        </div>
    `;
    
    chatMessages.appendChild(messageElement);
    chatMessages.scrollTop = chatMessages.scrollHeight;
}

// Start polling for new messages
function startMessagePolling() {
    if (messagePollingInterval) {
        clearInterval(messagePollingInterval);
    }
    
    messagePollingInterval = setInterval(async () => {
        await loadConversations();
        if (activeConversationUserId) {
            await loadConversationMessages(activeConversationUserId);
        }
    }, 5000); // Poll every 5 seconds
}

// Event Listeners
messageForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const content = messageInput.value.trim();
    if (content) {
        await sendMessage(content);
    }
});

messageInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter' && !e.shiftKey) {
        e.preventDefault();
        messageForm.dispatchEvent(new Event('submit'));
    }
});

// Search for new users to message
function handleNewMessageSearch(event) {
    const searchInput = document.getElementById("newMessageSearch");
    const query = searchInput.value.trim();
    const resultsContainer = document.getElementById("userSearchResults");

    if (query.length >= 1) {
        searchUsers(query);
        resultsContainer.style.display = "block";
    } else {
        resultsContainer.style.display = "none";
    }
}

function searchUsers(query) {
    const resultsContainer = document.getElementById("userSearchResults");
    const searchInput = document.getElementById("newMessageSearch");
    const searchTerm = query.toLowerCase().trim();
    
    fetch(`/api/users/search?query=${encodeURIComponent(searchTerm)}`)
        .then(response => response.json())
        .then(data => {
            resultsContainer.innerHTML = "";
            
            // Extract users array from response, handling different response formats
            let users = [];
            if (Array.isArray(data)) {
                users = data;
            } else if (typeof data === 'object') {
                // Check for common array properties that might contain users
                if (data.users) users = data.users;
                else if (data.results) users = data.results;
                else if (data.djs) users = [...data.djs || [], ...(data.communities || [])];
                else {
                    console.warn('Unexpected API response format:', data);
                    users = [];
                }
            }

            // Filter users by username only
            users = users.filter(user => 
                user.username && 
                user.username.toLowerCase().includes(searchTerm)
            );
            
            if (users.length === 0) {
                resultsContainer.innerHTML = `
                    <div class="no-results">
                        <p>No users found matching "${query}"</p>
                    </div>
                `;
                return;
            }

            users.forEach(user => {
                if (user.id === currentUserId) return; // Skip current user
                
                const div = document.createElement("div");
                div.className = "user-search-item";
                const profileUrl = user.role?.toLowerCase() === "event community" 
                    ? `community_profile.html?id=${user.id}`
                    : `profile.html?id=${user.id}`;
                
                div.innerHTML = `
                    <div class="user-avatar">${user.username.charAt(0).toUpperCase()}</div>
                    <div class="user-info">
                        <a href="${profileUrl}" class="username">${user.username}</a>
                        <div class="user-role">${user.role || ''}</div>
                    </div>
                    <button class="feed-button message-btn">Message</button>
                `;
                
                const messageBtn = div.querySelector('.message-btn');
                messageBtn.onclick = (e) => {
                    e.preventDefault();
                    e.stopPropagation();
                    startNewConversation(user.id, user.username);
                    resultsContainer.style.display = "none";
                    if (searchInput) {
                        searchInput.value = "";
                    }
                };
                
                resultsContainer.appendChild(div);
            });
        })
        .catch(error => {
            console.error('Error searching users:', error);
            resultsContainer.innerHTML = `
                <div class="no-results">
                    <p>Error searching for users. Please try again.</p>
                </div>
            `;
        });
}

// Add event listeners for search
document.addEventListener('DOMContentLoaded', () => {
    const searchInput = document.getElementById("newMessageSearch");
    if (searchInput) {
        searchInput.addEventListener('input', handleNewMessageSearch);
        
        // Close search results when clicking outside
        document.addEventListener('click', (e) => {
            const resultsContainer = document.getElementById("userSearchResults");
            if (!searchInput.contains(e.target) && !resultsContainer.contains(e.target)) {
                resultsContainer.style.display = "none";
            }
        });
    }
});

// Add styles for the new message button
const style = document.createElement('style');
style.textContent = `
    .user-search-item {
        justify-content: space-between;
    }
    .user-search-item .message-btn {
        padding: 4px 12px;
        font-size: 0.9em;
    }
    .user-search-item .username {
        text-decoration: none;
        color: var(--text-primary);
        font-weight: 500;
    }
    .user-search-item .username:hover {
        color: var(--accent-primary);
    }
`;
document.head.appendChild(style);

// Initialize the messaging interface
initializeMessaging(); 