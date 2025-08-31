async function loginCheck(event) {
	event.preventDefault();

	const formEl = event.target;
	const formData = new FormData(formEl);
	const username = formData.get('username')?.toString().trim() || '';
	const password = formData.get('password')?.toString() || '';
	const body = new URLSearchParams({username, password});

	try {
		const response = await fetch('/api/auth/login', {
			method: 'POST',
			credentials: 'include', // 쿠키/세션 포함
			headers: {'Content-Type': 'application/x-www-form-urlencoded'},
			body
		});

		if (response.ok) {
			// 로그인 성공
			console.log('Login successful');
			window.location.href = 'welcome.html';
		} else {
			// 로그인 실패
			const errorData = await response.json().catch(() => null);
			const errorMessage = errorData?.message || '아이디 또는 비밀번호가 올바르지 않습니다.';
			alert(errorMessage);
		}
	} catch (error) {
		console.error('Login request failed:', error);
		alert('서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
	}
}
