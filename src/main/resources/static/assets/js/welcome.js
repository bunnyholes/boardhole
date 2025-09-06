// Welcome 페이지 사용자 인증 및 데이터 로딩
window.addEventListener('load', async () => {
    try {
        // API 호출로 사용자 정보 가져오기
        const response = await fetch('/api/users/me', {
            method: 'GET',
            credentials: 'include', // 쿠키/세션 포함
            headers: {
                'Content-Type': 'application/json'
            }
        });

        // 인증 실패 시 로그인 페이지로 리다이렉트
        if (!response.ok) {

            window.location.href = 'login.html';
            return;
        }

        // 사용자 정보 파싱
        const userData = await response.json();


        // 화면에 사용자 정보 렌더링
        renderUserInfo(userData);

    } catch (error) {
        // 네트워크 오류 등으로 API 호출 실패 시에도 로그인 페이지로
        window.location.href = 'login.html';
    }
});

// 사용자 정보를 화면에 렌더링하는 함수
function renderUserInfo(userData) {
    // 사용자 이름 업데이트
    const usernameElement = document.getElementById('username');
    if (usernameElement && userData.name) {
        usernameElement.textContent = userData.name;
    }

    // 마지막 로그인 시간 업데이트
    const lastLoginElement = document.getElementById('last-login');
    if (lastLoginElement) {
        if (userData.lastLogin) {
            const lastLoginDate = new Date(userData.lastLogin);
            const timeStr = lastLoginDate.toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric',
                hour: '2-digit',
                minute: '2-digit'
            });
            lastLoginElement.textContent = `마지막 로그인: ${timeStr}`;
        } else {
            // API에서 lastLogin 정보가 없으면 현재 시간 사용
            const now = new Date();
            const timeStr = now.toLocaleDateString('ko-KR', {
                year: 'numeric',
                month: 'long',
                day: 'numeric'
            });
            lastLoginElement.textContent = `마지막 로그인: ${timeStr}`;
        }
    }

    // 추가 사용자 정보가 있다면 렌더링
    if (userData.email) {
        const emailElement = document.getElementById('user-email');
        if (emailElement) {
            emailElement.textContent = userData.email;
        }
    }

    // 로딩 상태 해제 (필요한 경우)
    const loadingElement = document.getElementById('loading');
    if (loadingElement) {
        loadingElement.style.display = 'none';
    }

    // 메인 콘텐츠 표시
    const mainContent = document.getElementById('main-content');
    if (mainContent) {
        mainContent.style.display = 'block';
    }
}