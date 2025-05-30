function handleSearchKey(event) {
  if (event.key === "Enter") {
    const query = document.getElementById("searchInput").value;
    searchAll(query);
  }
}

function searchAll(query) {
  const container = document.getElementById("searchResults");
  container.innerHTML = "<p>Searching...</p>";

  Promise.all([
    fetch(`/api/users/search?query=${encodeURIComponent(query)}`).then(res => res.json()),
    fetch(`/api/events/search?query=${encodeURIComponent(query)}`).then(res => res.json())
  ])
    .then(([userResults, eventResults]) => {
      container.innerHTML = "";

      if (userResults.djs && userResults.djs.length > 0) {
        const djHeader = document.createElement("h4");
        djHeader.textContent = "DJs";
        container.appendChild(djHeader);

        userResults.djs.forEach(user => {
          const div = document.createElement("div");
          div.classList.add("search-result-item");
          div.innerHTML = `
            <a href="profile.html?id=${user.id}">
              <strong>${user.username}</strong>
            </a> — ${user.genres || "no genres"}
          `;
          container.appendChild(div);
        });
      }

      if (userResults.communities && userResults.communities.length > 0) {
        const communityHeader = document.createElement("h4");
        communityHeader.textContent = "Event Communities";
        container.appendChild(communityHeader);

        userResults.communities.forEach(user => {
          const div = document.createElement("div");
          div.classList.add("search-result-item");
          div.innerHTML = `
            <a href="community_profile.html?id=${user.id}">
              <strong>${user.username}</strong>
            </a> — Event Community
          `;
          container.appendChild(div);
        });
      }

      if (eventResults.length > 0) {
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
        (!userResults.djs || userResults.djs.length === 0) &&
        (!userResults.communities || userResults.communities.length === 0) &&
        eventResults.length === 0
      ) {
        container.innerHTML = "<p>No results found.</p>";
      }
    })
    .catch(err => {
      console.error("Search error:", err);
      container.innerHTML = "<p>Search failed. Try again later.</p>";
    });
}

function displaySearchResults(results) {
  const container = document.getElementById('searchResults');
  container.innerHTML = '';

  if (results.length === 0) {
    container.innerHTML = '<p>No results found</p>';
    return;
  }

  results.forEach(user => {
    const div = document.createElement('div');
    div.className = 'search-result';
    
    const profileLink = user.role?.toLowerCase() === "event community" 
      ? `community_profile.html?id=${user.id}`
      : `profile.html?id=${user.id}`;

    div.innerHTML = `
      <a href="${profileLink}">
        <strong>${user.username}</strong>
      </a>
      <p>${user.role}</p>
    `;
    container.appendChild(div);
  });
}

// Search functionality
function handleSearch(query) {
  const container = document.getElementById("searchResults");
  container.innerHTML = "<p>Searching...</p>";

  Promise.all([
    fetch(`/api/users/search?query=${encodeURIComponent(query)}`).then(res => res.json()),
    fetch(`/api/events/search?query=${encodeURIComponent(query)}`).then(res => res.json())
  ])
    .then(([userResults, eventResults]) => {
      container.innerHTML = "";

      // DJs
      if (userResults.djs?.length > 0) {
        const djHeader = document.createElement("h4");
        djHeader.textContent = "DJs";
        container.appendChild(djHeader);

        userResults.djs.forEach(user => {
          const isSelf = loggedInUserId === user.id;
          const profilePage = isSelf 
            ? `profile.html?id=${loggedInUserId}` 
            : `profile.html?id=${user.id}`;
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

      // Event Communities
      if (userResults.communities?.length > 0) {
        const communityHeader = document.createElement("h4");
        communityHeader.textContent = "Event Communities";
        container.appendChild(communityHeader);

        userResults.communities.forEach(user => {
          const isSelf = loggedInUserId === user.id;
          const profilePage = isSelf 
            ? `community_profile.html?id=${loggedInUserId}` 
            : `community_profile.html?id=${user.id}`;
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

      // Events
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
        (!userResults.djs || userResults.djs.length === 0) &&
        (!userResults.communities || userResults.communities.length === 0) &&
        (!eventResults || eventResults.length === 0)
      ) {
        container.innerHTML = "<p>No results found.</p>";
      }
    })
    .catch(err => {
      console.error("Search failed:", err);
      container.innerHTML = "<p>Search failed. Try again later.</p>";
    });
}
