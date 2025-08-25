// 통합 게시글 폼 로직
let currentMode = 'view'; // view, create, edit
let currentBoard = null;
let currentUser = null;
let boardId = null;

// 페이지 로드 시 실행
window.addEventListener('load', async () => {
  const urlParams = new URLSearchParams(window.location.search);
  const mode = urlParams.get('mode');
  boardId = urlParams.get('id');
  
  // 현재 사용자 확인
  currentUser = await getCurrentUser();
  
  // 모드 결정
  if (mode === 'create') {
    currentMode = 'create';
    initCreateMode();
  } else if (mode === 'edit' && boardId) {
    currentMode = 'edit';
    await loadBoardForEdit();
  } else if (boardId) {
    currentMode = 'view';
    await loadBoardForView();
  } else {
    // ID 없고 mode도 없으면 목록으로
    window.location.href = 'board.html';
  }
});

// 현재 로그인 사용자
async function getCurrentUser() {
  try {
    const res = await fetch('/api/users/me');
    if (!res.ok) return null;
    const me = await res.json();
    return { id: me.userId, username: me.username, name: me.name };
  } catch {
    return null;
  }
}

// 생성 모드 초기화
function initCreateMode() {
  document.getElementById('page-title').textContent = '새글 쓰기';
  document.title = 'Board Hole - 새글 쓰기';
  
  // 폼 활성화
  setFormEnabled(true);
  
  // 버튼 표시
  document.getElementById('create-actions').style.display = 'flex';
  document.getElementById('title-required').style.display = 'inline';
  document.getElementById('content-required').style.display = 'inline';
  
  // 로딩 숨기고 폼 표시
  showLoading(false);
  document.getElementById('board-form-container').style.display = 'block';
}

// 보기용 게시글 로드
async function loadBoardForView() {
  try {
    showLoading(true);
    
    const res = await fetch(`/api/boards/${boardId}`);
    if (!res.ok) throw new Error('게시글을 불러오지 못했습니다');
    currentBoard = await res.json();
    
    displayBoard('view');
  } catch (error) {
    console.error('Error loading board:', error);
    showError('게시글을 찾을 수 없습니다.');
  } finally {
    showLoading(false);
  }
}

// 수정용 게시글 로드
async function loadBoardForEdit() {
  try {
    showLoading(true);
    
    // 로그인 체크
    if (!currentUser) {
      const next = encodeURIComponent(`board-form.html?mode=edit&id=${boardId}`);
      window.location.href = `login.html?next=${next}`;
      return;
    }
    
    const res = await fetch(`/api/boards/${boardId}`);
    if (!res.ok) throw new Error('게시글을 불러오지 못했습니다');
    currentBoard = await res.json();
    
    // 권한 체크
    if (currentBoard.authorId !== currentUser.id) {
      alert('수정 권한이 없습니다.');
      window.location.href = `board-form.html?id=${boardId}`;
      return;
    }
    
    displayBoard('edit');
  } catch (error) {
    console.error('Error loading board:', error);
    showError('게시글을 찾을 수 없거나 수정 권한이 없습니다.');
  } finally {
    showLoading(false);
  }
}

// 게시글 표시
function displayBoard(mode) {
  const form = document.getElementById('board-form');
  
  // 제목과 내용 설정
  form.title.value = currentBoard.title;
  form.content.value = currentBoard.content;
  
  if (mode === 'view') {
    document.getElementById('page-title').textContent = '게시글 상세';
    document.title = `Board Hole - ${currentBoard.title}`;
    
    // 메타 정보 표시
    document.getElementById('post-author').textContent = currentBoard.authorName || 'Unknown';
    document.getElementById('post-date').textContent = formatDate(currentBoard.createdAt);
    if (currentBoard.viewCount !== undefined) {
      document.getElementById('post-views').textContent = currentBoard.viewCount;
    }
    document.getElementById('post-meta').style.display = 'block';
    
    // 폼 비활성화
    setFormEnabled(false);
    
    // 필수 표시 숨김
    document.getElementById('title-required').style.display = 'none';
    document.getElementById('content-required').style.display = 'none';
    
    // 보기 모드 버튼 표시
    document.getElementById('view-actions').style.display = 'flex';
    
    // 작성자인 경우만 수정 버튼 표시
    if (currentUser && currentBoard.authorId === currentUser.id) {
      document.getElementById('edit-btn').style.display = 'inline-block';
    } else {
      document.getElementById('edit-btn').style.display = 'none';
    }
  } else if (mode === 'edit') {
    document.getElementById('page-title').textContent = '게시글 수정';
    document.title = 'Board Hole - 게시글 수정';
    
    // 폼 활성화
    setFormEnabled(true);
    
    // 수정 모드 버튼 표시
    document.getElementById('edit-actions').style.display = 'flex';
    document.getElementById('title-required').style.display = 'inline';
    document.getElementById('content-required').style.display = 'inline';
  }
  
  // 폼 컨테이너 표시
  document.getElementById('board-form-container').style.display = 'block';
}

// 수정 모드 활성화
function enableEditMode() {
  window.location.href = `board-form.html?mode=edit&id=${boardId}`;
}

// 폼 활성화/비활성화
function setFormEnabled(enabled) {
  const form = document.getElementById('board-form');
  form.title.disabled = !enabled;
  form.content.disabled = !enabled;
  
  if (!enabled) {
    // 보기 모드에서는 textarea를 읽기 전용 스타일로
    form.content.style.backgroundColor = '#f9f9f9';
    form.title.style.backgroundColor = '#f9f9f9';
  } else {
    form.content.style.backgroundColor = '';
    form.title.style.backgroundColor = '';
  }
}

// 폼 제출
async function submitForm(event) {
  event.preventDefault();
  
  const form = event.target;
  const formData = {
    title: form.title.value.trim(),
    content: form.content.value.trim()
  };
  
  // 유효성 검사
  clearErrors();
  let hasError = false;
  
  if (!formData.title) {
    showFieldError('title', '제목을 입력해주세요');
    hasError = true;
  }
  if (!formData.content) {
    showFieldError('content', '내용을 입력해주세요');
    hasError = true;
  }
  
  if (hasError) return;
  
  try {
    let response;
    
    if (currentMode === 'create') {
      // 생성
      showSubmitLoading('create', true);
      response = await fetch('/api/boards', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
    } else if (currentMode === 'edit') {
      // 수정
      showSubmitLoading('update', true);
      response = await fetch(`/api/boards/${boardId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(formData)
      });
    }
    
    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.message || '요청 처리 실패');
    }
    
    const result = await response.json();
    
    if (currentMode === 'create') {
      alert('게시글이 작성되었습니다.');
      window.location.href = `board-form.html?id=${result.id}`;
    } else {
      alert('게시글이 수정되었습니다.');
      window.location.href = `board-form.html?id=${boardId}`;
    }
  } catch (error) {
    console.error('Submit failed:', error);
    if (error.status === 401) {
      const next = encodeURIComponent(window.location.href);
      window.location.href = `login.html?next=${next}`;
    } else {
      alert(error.message || '저장에 실패했습니다.');
    }
  } finally {
    showSubmitLoading('create', false);
    showSubmitLoading('update', false);
  }
}

// 게시글 삭제
function deletePost() {
  if (!currentBoard) return;
  document.getElementById('delete-modal').style.display = 'flex';
}

// 삭제 확인
async function confirmDelete() {
  try {
    const res = await fetch(`/api/boards/${boardId}`, { method: 'DELETE' });
    if (!res.ok) throw new Error('삭제 실패');
    alert('게시글이 삭제되었습니다.');
    window.location.href = 'board.html';
  } catch (error) {
    console.error('Delete failed:', error);
    if (error.status === 401) {
      const next = encodeURIComponent(window.location.href);
      window.location.href = `login.html?next=${next}`;
    } else {
      alert('게시글 삭제에 실패했습니다.');
    }
  } finally {
    closeDeleteModal();
  }
}

// 삭제 모달 닫기
function closeDeleteModal() {
  document.getElementById('delete-modal').style.display = 'none';
}

// 취소
function cancelAction() {
  if (currentMode === 'create') {
    window.location.href = 'board.html';
  } else {
    window.location.href = `board-form.html?id=${boardId}`;
  }
}

// 로딩 표시
function showLoading(show) {
  const loading = document.getElementById('loading');
  const container = document.getElementById('board-form-container');
  const error = document.getElementById('error-message');
  
  if (show) {
    loading.style.display = 'block';
    container.style.display = 'none';
    error.style.display = 'none';
  } else {
    loading.style.display = 'none';
  }
}

// 에러 표시
function showError(message) {
  document.getElementById('loading').style.display = 'none';
  document.getElementById('board-form-container').style.display = 'none';
  document.getElementById('error-text').textContent = message;
  document.getElementById('error-message').style.display = 'block';
}

// 제출 로딩
function showSubmitLoading(type, show) {
  const textEl = document.getElementById(`${type}-text`);
  const loadingEl = document.getElementById(`${type}-loading`);
  
  if (textEl && loadingEl) {
    textEl.style.display = show ? 'none' : 'inline';
    loadingEl.style.display = show ? 'inline' : 'none';
  }
}

// 필드 에러 표시
function showFieldError(field, message) {
  const errorEl = document.getElementById(`${field}-error`);
  if (errorEl) {
    errorEl.textContent = message;
    errorEl.style.display = 'block';
  }
}

// 에러 클리어
function clearErrors() {
  document.querySelectorAll('.field-error').forEach(el => {
    el.textContent = '';
    el.style.display = 'none';
  });
}

// 날짜 포맷팅
function formatDate(dateString) {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  return date.toLocaleString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
}

// 모달 클릭 이벤트
document.addEventListener('click', (e) => {
  const modal = document.getElementById('delete-modal');
  if (e.target === modal) {
    closeDeleteModal();
  }
});

// ESC 키로 모달 닫기
document.addEventListener('keydown', (e) => {
  if (e.key === 'Escape') {
    closeDeleteModal();
  }
});

// 로그아웃
async function logout() {
  await fetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
  window.location.href = 'index.html';
}