// 로그아웃 기능
async function logout() {
	try {
		const response = await fetch('/api/auth/logout', {
			method: 'POST',
			credentials: 'include' // 쿠키/세션 포함
		});

		if (response.ok) {
			console.log('Logout successful');
			// 로그아웃 성공 시 홈페이지로 이동
			window.location.href = 'index.html';
		} else {
			console.error('Logout failed with status:', response.status);
			// 실패해도 홈페이지로 이동 (클라이언트 측에서 세션 정리)
			window.location.href = 'index.html';
		}
	} catch (error) {
		console.error('Logout request failed:', error);
		// 네트워크 오류 등이 발생해도 홈페이지로 이동
		window.location.href = 'index.html';
	}
}

// 확인 후 로그아웃 (선택적 사용)
function confirmLogout() {
	if (confirm('정말 로그아웃하시겠습니까?')) {
		logout();
	}
}
