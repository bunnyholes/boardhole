// 내 정보 페이지 로직 (서버 세션 기반)
window.addEventListener('load', async () => {
  try {
    const res = await fetch('/api/users/me');
    if (!res.ok) throw new Error('not logged in');
    const me = await res.json();
    renderUserProfile({
      name: me.name,
      email: me.email,
      userId: me.userId,
      joinDate: me.createdAt,
      lastLogin: me.lastLogin
    });
  } catch (e) {
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

  // 사용자 ID 업데이트
  const userIdElement = document.getElementById('userId');
  if (userIdElement && userData.userId) {
    userIdElement.value = userData.userId;
  }

  // 가입일 업데이트
  const joinDateElement = document.getElementById('joinDate');
  if (joinDateElement && userData.joinDate) {
    const joinDate = new Date(userData.joinDate);
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

// 로그아웃 (서버 세션 종료)
async function logout() {
  await fetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
  window.location.href = 'index.html';
}
