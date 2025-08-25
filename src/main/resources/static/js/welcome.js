// Welcome 페이지: 서버 세션 기반 로그인 확인
window.addEventListener('load', async () => {
  try {
    const res = await fetch('/api/users/me');
    if (!res.ok) throw new Error('not logged in');
    const me = await res.json();
    renderUserInfo({ name: me.name, lastLogin: me.lastLogin });
  } catch (e) {
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

// 로그아웃 (서버 세션 종료)
async function logout() {
  await fetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
  window.location.href = 'index.html';
}
