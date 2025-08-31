// 내 정보 페이지 로직
let originalUserData;
let isEditMode = false;

window.addEventListener('load', async () => {
	try {
		// API 호출로 사용자 상세 정보 가져오기
		const response = await fetch('/api/users/me', {
			method: 'GET',
			credentials: 'include', // 쿠키/세션 포함
			headers: {
				'Content-Type': 'application/json'
			}
		});

		// 인증 실패 시 로그인 페이지로 리다이렉트
		if (!response.ok) {
			console.log('Authentication failed, redirecting to login');
			window.location.href = 'login.html';
			return;
		}

		// 사용자 정보 파싱
		const userData = await response.json();
		console.log('User profile data loaded:', userData);

		// Store original data
		originalUserData = userData;

		// 화면에 사용자 정보 렌더링
		renderUserProfile(userData);


	} catch (error) {
		console.error('API call failed:', error);
		// 네트워크 오류 등으로 API 호출 실패 시에도 로그인 페이지로
		window.location.href = 'login.html';
	}
});

// 사용자 프로필 정보를 화면에 렌더링하는 함수
function renderUserProfile(userData) {
	// 이름 업데이트
	const nameElement = document.getElementById('name');
	if (nameElement && userData.name) {
		nameElement.value = userData.name;
	}

	// 이메일 업데이트
	const emailElement = document.getElementById('email');
	if (emailElement && userData.email) {
		emailElement.value = userData.email;
	}

	// 사용자 ID 업데이트 (username 사용)
	const userIdElement = document.getElementById('userId');
	if (userIdElement && userData.username) {
		userIdElement.value = userData.username;
	}

	// 가입일 업데이트
	const joinDateElement = document.getElementById('joinDate');
	if (joinDateElement && userData.createdAt) {
		const joinDate = new Date(userData.createdAt);
		const joinDateStr = joinDate.toLocaleDateString('ko-KR', {
			year: 'numeric',
			month: 'long',
			day: 'numeric'
		});
		joinDateElement.value = joinDateStr;
	}

	// 마지막 로그인 시간 업데이트
	const lastLoginElement = document.getElementById('lastLoginDate');
	if (lastLoginElement && userData.lastLogin) {
		const lastLoginDate = new Date(userData.lastLogin);
		const lastLoginStr = lastLoginDate.toLocaleDateString('ko-KR', {
			year: 'numeric',
			month: 'long',
			day: 'numeric',
			hour: '2-digit',
			minute: '2-digit'
		});
		lastLoginElement.value = lastLoginStr;
	}

	// 로딩 상태 해제
	const loadingElement = document.getElementById('loading');
	if (loadingElement) {
		loadingElement.style.display = 'none';
	}

	// 사용자 정보 표시
	const userInfoElement = document.getElementById('user-info');
	if (userInfoElement) {
		userInfoElement.style.display = 'block';
	}
}

// Enable edit mode
function enableEdit() {
	isEditMode = true;

	// Make fields editable
	document.getElementById('name').readOnly = false;
	document.getElementById('email').readOnly = false;

	// Show/hide action buttons
	document.getElementById('edit-actions').style.display = 'block';
	document.getElementById('default-actions').style.display = 'none';
}

// Cancel edit mode
function cancelEdit() {
	if (hasChanges()) {
		if (!confirm('변경사항이 저장되지 않습니다. 정말로 취소하시겠습니까?')) {
			return;
		}
	}

	isEditMode = false;

	// Restore original values
	if (originalUserData) {
		document.getElementById('name').value = originalUserData.name;
		document.getElementById('email').value = originalUserData.email;
	}

	// Make fields readonly
	document.getElementById('name').readOnly = true;
	document.getElementById('email').readOnly = true;

	// Show/hide action buttons
	document.getElementById('edit-actions').style.display = 'none';
	document.getElementById('default-actions').style.display = 'block';
}

// Check if form has changes
function hasChanges() {
	if (!originalUserData) return false;

	const currentName = document.getElementById('name').value.trim();
	const currentEmail = document.getElementById('email').value.trim();

	return currentName !== originalUserData.name || currentEmail !== originalUserData.email;
}

// Handle form submission via API
async function handleFormSubmit(event) {
	event.preventDefault();

	if (!hasChanges()) {
		alert('변경된 내용이 없습니다.');
		return;
	}

	const formData = new FormData();
	formData.append('name', document.getElementById('name').value.trim());
	formData.append('email', document.getElementById('email').value.trim());

	// Show updating state
	document.getElementById('user-info').style.display = 'none';
	document.getElementById('updating').style.display = 'block';

	try {
		const response = await fetch(`/api/users/${originalUserData.id}`, {
			method: 'PUT',
			credentials: 'include',
			body: formData
		});

		if (response.ok) {
			const updatedUser = await response.json();
			originalUserData = updatedUser;
			renderUserProfile(updatedUser);

			// Exit edit mode
			isEditMode = false;
			document.getElementById('name').readOnly = true;
			document.getElementById('email').readOnly = true;
			document.getElementById('edit-actions').style.display = 'none';
			document.getElementById('default-actions').style.display = 'block';

			alert('정보가 성공적으로 업데이트되었습니다.');
		} else {
			const errorData = await response.json().catch(() => null);
			let errorMessage = '정보 업데이트에 실패했습니다.';

			if (errorData && errorData.message) {
				errorMessage = errorData.message;
			} else if (response.status === 409) {
				errorMessage = '이미 사용 중인 이메일입니다.';
			} else if (response.status === 400) {
				errorMessage = '입력 정보를 다시 확인해주세요.';
			}

			showError(errorMessage);
		}
	} catch (error) {
		console.error('Update request failed:', error);
		showError('서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
	}

	// Hide updating state
	document.getElementById('updating').style.display = 'none';
	document.getElementById('user-info').style.display = 'block';
}

// Show error message
function showError(message) {
	const errorDiv = document.getElementById('flash-error');
	if (errorDiv) {
		errorDiv.textContent = message;
		errorDiv.style.display = 'block';

		// Auto-hide error after 5 seconds
		setTimeout(() => {
			errorDiv.style.display = 'none';
		}, 5000);
	}
}

// Add form submit listener
document.addEventListener('DOMContentLoaded', () => {
	const userForm = document.getElementById('user-form');
	if (userForm) {
		userForm.addEventListener('submit', handleFormSubmit);
	}
});
