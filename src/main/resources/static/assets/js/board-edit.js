// Board edit functionality
let currentBoardId;
let originalBoard;

// Page load event
window.addEventListener('load', async () => {
	// Get board ID from URL
	const urlParams = new URLSearchParams(window.location.search);
	currentBoardId = urlParams.get('id');

	if (!currentBoardId) {
		showError('잘못된 접근입니다.');
		return;
	}

	await loadBoardForEdit();
});

// Load board data for editing
async function loadBoardForEdit() {
	try {
		const response = await fetch(`/api/boards/${currentBoardId}`, {
			method: 'GET',
			credentials: 'include',
			headers: {
				'Content-Type': 'application/json'
			}
		});

		if (response.status === 401) {
			alert('로그인이 필요합니다.');
			window.location.href = 'login.html';
			return;
		}

		if (!response.ok) {
			if (response.status === 404) {
				showError('게시글을 찾을 수 없습니다.');
			} else {
				showError('게시글을 불러오는데 실패했습니다.');
			}
			return;
		}

		originalBoard = await response.json();
		console.log('Board loaded for edit:', originalBoard);

		// Check if user can edit (should be done on server side too)
		const userResponse = await fetch('/api/users/me', {
			credentials: 'include'
		});

		if (userResponse.ok) {
			const currentUser = await userResponse.json();
			if (currentUser.username !== originalBoard.authorName) {
				showError('게시글을 수정할 권한이 없습니다.');
				return;
			}
		}

		renderEditForm();

	} catch (error) {
		console.error('Failed to load board for edit:', error);
		showError('게시글을 불러오는데 실패했습니다.');
	}
}

// Render edit form with board data
function renderEditForm() {
	// Hide loading
	document.getElementById('loading').style.display = 'none';

	// Show form
	const form = document.getElementById('board-form');
	form.style.display = 'block';

	// Bind JS submit handler
	form.addEventListener('submit', submitEditForm);

	// Populate form fields
	form.querySelector('input[name="title"]').value = originalBoard.title;
	form.querySelector('textarea[name="content"]').value = originalBoard.content;

	// Update page title
	document.title = `${originalBoard.title} 수정 - Board Hole`;
}

// Cancel edit
function cancelEdit() {
	if (hasChanges()) {
		if (confirm('변경사항이 저장되지 않습니다. 정말로 취소하시겠습니까?')) {
			window.location.href = `board-detail.html?id=${currentBoardId}`;
		}
	} else {
		window.location.href = `board-detail.html?id=${currentBoardId}`;
	}
}

// Check if form has changes
function hasChanges() {
	const form = document.getElementById('board-form');
	const currentTitle = form.querySelector('input[name="title"]').value.trim();
	const currentContent = form.querySelector('textarea[name="content"]').value.trim();

	return originalBoard && (
			currentTitle !== originalBoard.title ||
			currentContent !== originalBoard.content
	);
}

// Show error message
function showError(message) {
	document.getElementById('loading').style.display = 'none';
	document.getElementById('board-form').style.display = 'none';

	const errorDiv = document.getElementById('error-message');
	errorDiv.querySelector('p').textContent = message;
	errorDiv.style.display = 'block';
}

// Handle edit form submit via fetch
async function submitEditForm(event) {
	event.preventDefault();

	const form = event.target;
	const title = form.querySelector('input[name="title"]').value.trim();
	const content = form.querySelector('textarea[name="content"]').value.trim();

	if (!title) {
		alert('제목을 입력해주세요.');
		return;
	}
	if (!content) {
		alert('내용을 입력해주세요.');
		return;
	}

	// Show updating state
	form.style.display = 'none';
	document.getElementById('updating').style.display = 'block';

	try {
		const formData = new FormData();
		formData.append('title', title);
		formData.append('content', content);

		const response = await fetch(`/api/boards/${currentBoardId}`, {
			method: 'PUT',
			credentials: 'include',
			body: formData
		});

		if (response.status === 401) {
			alert('로그인이 필요합니다.');
			window.location.href = 'login.html';
			return;
		}

		if (!response.ok) {
			const errText = await response.text().catch(() => '');
			throw new Error(`HTTP ${response.status} ${errText}`);
		}

		const updated = await response.json();
		alert('게시글이 수정되었습니다.');
		window.location.href = `board-detail.html?id=${currentBoardId}`;
	} catch (e) {
		console.error('Failed to update board:', e);
		document.getElementById('updating').style.display = 'none';
		form.style.display = 'block';
		alert('수정 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
	}
}
