// Handle URL parameters for highlighting and scrolling
function handleUrlParameters() {
    const urlParams = new URLSearchParams(window.location.search);
    let elementToHighlight;
    
    // Handle post highlighting in feed
    const postId = urlParams.get('postId');
    if (postId && window.location.pathname.endsWith('feed.html')) {
        elementToHighlight = document.querySelector(`.post-item[data-post-id="${postId}"]`);
    }
    
    // Handle event highlighting
    const eventId = urlParams.get('eventId');
    if (eventId) {
        if (window.location.pathname.endsWith('event.html')) {
            elementToHighlight = document.querySelector(`.event-details[data-event-id="${eventId}"]`);
        } else if (window.location.pathname.endsWith('my_applications.html')) {
            elementToHighlight = document.querySelector(`.application-item[data-event-id="${eventId}"]`);
        }
    }
    
    // If we found an element to highlight
    if (elementToHighlight) {
        // Add highlight class
        elementToHighlight.classList.add('highlight-item');
        // Scroll into view
        elementToHighlight.scrollIntoView({ behavior: 'smooth', block: 'center' });
        // Remove highlight after animation
        setTimeout(() => {
            elementToHighlight.classList.remove('highlight-item');
        }, 2000);
    }
}

// Call when DOM is loaded
document.addEventListener('DOMContentLoaded', handleUrlParameters); 