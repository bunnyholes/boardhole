// 회원가입 처리
async function signupCheck(event) {
  event.preventDefault();

  const formData = new FormData(event.target);
  const username = formData.get('username').trim();
  const password = formData.get('password');
  const passwordConfirm = formData.get('passwordConfirm');
  const name = formData.get('name').trim();
  const email = formData.get('email').trim();

  // 클라이언트 사이드 유효성 검사
  if (!validateSignupForm(username, password, passwordConfirm, name, email)) {
    return;
  }

  const signupData = {
    username: username,
    password: password,
    name: name,
    email: email
  };

  try {
    const res = await fetch('/api/auth/signup', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(signupData)
    });
    if (!res.ok) {
      const err = await res.json().catch(() => ({}));
      const msg = err.message || (res.status === 409 ? '이미 사용 중인 아이디 또는 이메일입니다.' : '회원가입에 실패했습니다.');
      throw new Error(msg);
    }
    // 회원가입 성공
    alert('회원가입이 완료되었습니다. 로그인해주세요.');
    window.location.href = 'login.html';
  } catch (error) {
    alert(error.message || '회원가입에 실패했습니다.');
  }
}

// 회원가입 폼 유효성 검사
function validateSignupForm(username, password, passwordConfirm, name, email) {
  // 아이디 검사
  if (username.length < 4 || username.length > 20) {
    alert('아이디는 4~20자로 입력해주세요.');
    return false;
  }

  if (!/^[a-zA-Z0-9_-]+$/.test(username)) {
    alert('아이디는 영문, 숫자, _, - 만 사용 가능합니다.');
    return false;
  }

  // 비밀번호 검사
  if (password.length < 6) {
    alert('비밀번호는 6자 이상 입력해주세요.');
    return false;
  }

  // 비밀번호 확인
  if (password !== passwordConfirm) {
    alert('비밀번호가 일치하지 않습니다.');
    return false;
  }

  // 이름 검사
  if (name.length < 1 || name.length > 50) {
    alert('이름을 올바르게 입력해주세요.');
    return false;
  }

  // 이메일 검사
  const emailPattern = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  if (!emailPattern.test(email)) {
    alert('올바른 이메일 형식으로 입력해주세요.');
    return false;
  }

  return true;
}

// 실시간 비밀번호 확인 검사 (선택사항)
window.addEventListener('load', () => {
  const passwordInput = document.querySelector('input[name="password"]');
  const passwordConfirmInput = document.querySelector('input[name="passwordConfirm"]');

  if (passwordConfirmInput) {
    passwordConfirmInput.addEventListener('input', () => {
      const password = passwordInput.value;
      const passwordConfirm = passwordConfirmInput.value;

      if (passwordConfirm && password !== passwordConfirm) {
        passwordConfirmInput.setCustomValidity('비밀번호가 일치하지 않습니다.');
      } else {
        passwordConfirmInput.setCustomValidity('');
      }
    });
  }
});
