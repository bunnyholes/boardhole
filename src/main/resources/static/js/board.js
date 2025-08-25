// 간단 게시판 목록 페이지 로직 (서버 페이징/검색 없음)
let boardsCache = [];

window.addEventListener('load', async () => {
  await loadBoards();
});

// 로그아웃: 서버 세션 종료
async function logout() {
  await fetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
  window.location.href = 'index.html';
}

// 게시글 목록 불러오기
async function loadBoards() {
  try {
    showLoading(true);
    const res = await fetch('/api/boards');
    if (!res.ok) throw new Error('게시글을 불러오지 못했습니다');
    boardsCache = await res.json();
    renderBoardList(boardsCache);
  } catch (e) {
    showError(e.message || '게시글을 불러올 수 없습니다.');
  } finally {
    showLoading(false);
  }
}

// 간단 검색 (클라이언트 필터)
function searchBoards() {
  const q = document.getElementById('searchInput').value.trim().toLowerCase();
  const filtered = q ? boardsCache.filter(b => (b.title||'').toLowerCase().includes(q)) : boardsCache;
  renderBoardList(filtered);
}

function clearSearchAndReload() {
  document.getElementById('searchInput').value = '';
  renderBoardList(boardsCache);
}

// 게시글 목록 렌더링
function renderBoardList(boards) {
  const tbody = document.getElementById('board-tbody');
  const boardList = document.getElementById('board-list');
  const emptyMessage = document.getElementById('empty-message');
  
  if (!boards || boards.length === 0) {
    boardList.style.display = 'none';
    emptyMessage.style.display = 'block';
    return;
  }

  boardList.style.display = 'block';
  emptyMessage.style.display = 'none';

  // 테이블 내용 생성
  tbody.innerHTML = '';
  boards.forEach(board => {
    const row = document.createElement('tr');
    row.innerHTML = `
      <td>${board.id}</td>
      <td>
        <a href="board-form.html?id=${board.id}" class="board-title-link">
          ${escapeHtml(board.title)}
        </a>
      </td>
      <td>${escapeHtml(board.authorName || 'Unknown')}</td>
      <td>${formatDate(board.createdAt)}</td>
      <td>${board.viewCount || 0}</td>
    `;
    tbody.appendChild(row);
  });

  // 페이지네이션 제거: 간단 리스트만 표시
  document.getElementById('pagination').innerHTML = '';
}

// 로딩 상태 표시/숨김
function showLoading(show) {
  const loading = document.getElementById('loading');
  const boardList = document.getElementById('board-list');
  const emptyMessage = document.getElementById('empty-message');
  
  if (show) {
    loading.style.display = 'block';
    boardList.style.display = 'none';
    emptyMessage.style.display = 'none';
  } else {
    loading.style.display = 'none';
  }
}

// 에러 메시지 표시
function showError(message) {
  alert(message);
}

// HTML 이스케이프
function escapeHtml(text) {
  if (!text) return '';
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, function(m) { return map[m]; });
}

// 날짜 포맷팅
function formatDate(dateString) {
  if (!dateString) return '';
  
  const date = new Date(dateString);
  const now = new Date();
  const diff = now - date;
  const diffDays = Math.floor(diff / (1000 * 60 * 60 * 24));
  
  if (diffDays === 0) {
    return date.toLocaleTimeString('ko-KR', { 
      hour: '2-digit', 
      minute: '2-digit' 
    });
  } else if (diffDays < 7) {
    return `${diffDays}일 전`;
  } else {
    return date.toLocaleDateString('ko-KR', {
      month: 'short',
      day: 'numeric'
    });
  }
}
