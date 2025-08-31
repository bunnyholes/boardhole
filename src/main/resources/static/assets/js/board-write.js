// Board write functionality

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function () {
	const form = document.getElementById('board-form');
	if (form) {
		form.addEventListener('submit', submitBoard);
	}
});

// Submit board form
async function submitBoard(event) {
	event.preventDefault();

	const form = event.target;
	const formData = new FormData(form);

	const title = formData.get('title').trim();
	const content = formData.get('content').trim();

	// Clear previous errors
	clearFlashMessage();

	// Validation
	if (!title) {
		showFlashMessage('제목을 입력해주세요.', 'error');
		return;
	}

	if (!content) {
		showFlashMessage('내용을 입력해주세요.', 'error');
		return;
	}

	try {
		// Show loading state
		showLoading(true);

		// Submit to API
		const response = await fetch('/api/boards', {
			method: 'POST',
			credentials: 'include',
			body: formData
		});

		if (response.status === 401) {
			showFlashMessage('로그인이 필요합니다.', 'error');
			setTimeout(() => {
				window.location.href = 'login.html';
			}, 1500);
			return;
		}

		if (!response.ok) {
			const errorText = await response.text();
			throw new Error(`HTTP error! status: ${response.status}, message: ${errorText}`);
		}

		const boardData = await response.json();
		console.log('Board created:', boardData);

		// Show success message and redirect
		showFlashMessage('게시글이 성공적으로 작성되었습니다!', 'success');

		setTimeout(() => {
			if (boardData.id) {
				window.location.href = `board-detail.html?id=${boardData.id}`;
			} else {
				window.location.href = 'board.html';
			}
		}, 1000);

	} catch (error) {
		console.error('Failed to create board:', error);

		// Hide loading
		showLoading(false);

		showFlashMessage('게시글 작성에 실패했습니다. 다시 시도해주세요.', 'error');
	}
}

// Show/hide loading state
function showLoading(show) {
	const loading = document.getElementById('loading');
	const form = document.getElementById('board-form');

	if (show) {
		loading.style.display = 'block';
		form.style.display = 'none';
	} else {
		loading.style.display = 'none';
		form.style.display = 'block';
	}
}

// Flash message functions (using flash.js if available, fallback to inline)
function showFlashMessage(message, type = 'info') {
	if (typeof flash !== 'undefined' && flash.show) {
		// Use flash.js if available
		flash.show(message, type);
	} else {
		// Fallback to inline display
		const flashElement = document.getElementById('flash-error');
		if (flashElement) {
			flashElement.textContent = message;
			flashElement.className = `alert text-center ${type === 'error' ? 'alert-danger' : type === 'success' ? 'alert-success' : 'alert-info'}`;
			flashElement.style.display = 'block';
			flashElement.setAttribute('aria-live', 'polite');

			// Auto-hide after 5 seconds for success messages
			if (type === 'success') {
				setTimeout(() => {
					clearFlashMessage();
				}, 5000);
			}
		}
	}
}

function clearFlashMessage() {
	if (typeof flash !== 'undefined' && flash.clear) {
		flash.clear();
	} else {
		const flashElement = document.getElementById('flash-error');
		if (flashElement) {
			flashElement.style.display = 'none';
			flashElement.textContent = '';
		}
	}
}
