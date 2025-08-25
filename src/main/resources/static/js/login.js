async function loginCheck(event) {
  event.preventDefault();

  const formData = new FormData(event.target);
  const payload = {
    username: formData.get('id').trim(),
    password: formData.get('password')
  };

  try {
    const res = await fetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!res.ok) {
      // 204가 아니면 에러 바디를 읽으려 시도
      const err = await res.json().catch(() => ({}));
      throw new Error(err.message || '로그인 실패');
    }

    // next 파라미터가 있으면 해당 페이지로 이동
    const params = new URLSearchParams(location.search);
    const next = params.get('next');
    window.location.href = next ? next : 'welcome.html';
  } catch (e) {
    alert(e.message || '아이디 또는 비밀번호가 올바르지 않습니다.');
  }
}
