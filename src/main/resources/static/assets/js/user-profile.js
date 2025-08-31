// User profile functionality
let currentUserId;

// Page load event
window.addEventListener('load', async () => {
	// Get user ID from URL
	const urlParams = new URLSearchParams(window.location.search);
	currentUserId = urlParams.get('id');

	if (!currentUserId) {
		showError('잘못된 접근입니다.');
		return;
	}

	await loadUserProfile();
});

// Load user profile data
async function loadUserProfile() {
	try {
		const response = await fetch(`/api/users/${currentUserId}`, {
			method: 'GET',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			}
		});

		if (!response.ok) {
			if (response.status === 404) {
				showError('사용자를 찾을 수 없습니다.');
			} else {
				showError('사용자 정보를 불러오는데 실패했습니다.');
			}
			return;
		}

		const userData = await response.json();
		console.log('User profile loaded:', userData);

		renderUserProfile(userData);
		await loadUserBoards();

	} catch (error) {
		console.error('Failed to load user profile:', error);
		showError('사용자 정보를 불러오는데 실패했습니다.');
	}
}

// Render user profile
function renderUserProfile(userData) {
	// Hide loading
	document.getElementById('loading').style.display = 'none';

	// Show profile
	document.getElementById('user-profile').style.display = 'block';

	// Set titles
	document.getElementById('profile-title').textContent = `${userData.name}님의 프로필`;
	document.getElementById('profile-subtitle').textContent = `@${userData.username}`;
	document.title = `${userData.name} - Board Hole`;

	// Set profile data
	document.getElementById('user-username').textContent = userData.username;
	document.getElementById('user-name').textContent = userData.name;
	document.getElementById('user-email').textContent = userData.email;

	// Format join date
	const joinDate = new Date(userData.createdAt);
	document.getElementById('user-join-date').textContent = joinDate.toLocaleDateString('ko-KR', {
		year: 'numeric',
		month: 'long',
		day: 'numeric'
	});
}

// Load user's boards (simplified - would need separate endpoint or filter)
async function loadUserBoards() {
	try {
		// Note: This is a simplified approach - in real app you'd have a separate endpoint
		// for user's boards or filter the main boards endpoint
		const response = await fetch('/api/boards?size=100', {
			method: 'GET',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			}
		});

		if (response.ok) {
			const boardsData = await response.json();

			// Filter boards by this user (client-side filtering - not ideal for production)
			const userBoards = boardsData.content.filter(board => board.authorId == currentUserId);

			renderUserBoards(userBoards);
		} else {
			// If boards can't be loaded, just hide the section
			document.getElementById('boards-loading').style.display = 'none';
		}

	} catch (error) {
		console.error('Failed to load user boards:', error);
		document.getElementById('boards-loading').style.display = 'none';
	}
}

// Render user's boards
function renderUserBoards(boards) {
	document.getElementById('boards-loading').style.display = 'none';
	document.getElementById('user-boards').style.display = 'block';

	const boardsList = document.getElementById('boards-list');
	const noBoards = document.getElementById('no-boards');

	if (boards.length === 0) {
		noBoards.style.display = 'block';
		return;
	}

	// Render boards
	boards.forEach(board => {
		const boardCard = document.createElement('div');
		boardCard.style.cssText = `
      border: 1px solid #ddd;
      border-radius: 4px;
      padding: 1rem;
      margin-bottom: 1rem;
      cursor: pointer;
      transition: background-color 0.2s;
    `;

		boardCard.onmouseover = () => boardCard.style.backgroundColor = '#f8f9fa';
		boardCard.onmouseout = () => boardCard.style.backgroundColor = 'white';
		boardCard.onclick = () => window.location.href = `board-detail.html?id=${board.id}`;

		const formattedDate = new Date(board.createdAt).toLocaleDateString('ko-KR', {
			year: 'numeric',
			month: '2-digit',
			day: '2-digit'
		});

		boardCard.innerHTML = `
      <div style="display: flex; justify-content: space-between; align-items: start;">
        <div style="flex: 1;">
          <div style="font-weight: 500; font-size: 1.1em; margin-bottom: 0.5rem;">
            ${escapeHtml(board.title)}
          </div>
          <div style="color: #666; font-size: 0.9em;">
            ${formattedDate} · 조회수 ${board.viewCount || 0}
          </div>
        </div>
      </div>
    `;

		boardsList.appendChild(boardCard);
	});
}

// Utility function to escape HTML
function escapeHtml(text) {
	const div = document.createElement('div');
	div.textContent = text;
	return div.innerHTML;
}

// Show error message
function showError(message) {
	document.getElementById('loading').style.display = 'none';
	document.getElementById('user-profile').style.display = 'none';

	const errorDiv = document.getElementById('error-message');
	errorDiv.querySelector('p').textContent = message;
	errorDiv.style.display = 'block';
}