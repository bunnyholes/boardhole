// Board detail functionality
let currentBoardId;
let currentUser;

// Page load event
window.addEventListener('load', async () => {
  // Get board ID from URL
  const urlParams = new URLSearchParams(window.location.search);
  currentBoardId = urlParams.get('id');
  
  if (!currentBoardId) {
    showError('잘못된 접근입니다.');
    return;
  }

  // Configure delete form action for method override redirect flow
  var delForm = document.getElementById('delete-form');
  if (delForm) {
    delForm.action = `/api/boards/${currentBoardId}`;
  }

  // Load current user info first
  await loadCurrentUser();
  
  // Load board data
  await loadBoard();
});

// Load current user information
async function loadCurrentUser() {
  try {
    const response = await fetch('/api/users/me', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (response.ok) {
      currentUser = await response.json();
      console.log('Current user loaded:', currentUser);
    }
  } catch (error) {
    console.log('No authenticated user or failed to load user info');
  }
}

// Load board data
async function loadBoard() {
  try {
    const response = await fetch(`/api/boards/${currentBoardId}`, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      if (response.status === 404) {
        showError('게시글을 찾을 수 없습니다.');
      } else {
        showError('게시글을 불러오는데 실패했습니다.');
      }
      return;
    }

    const board = await response.json();
    console.log('Board loaded:', board);

    renderBoard(board);

  } catch (error) {
    console.error('Failed to load board:', error);
    showError('게시글을 불러오는데 실패했습니다.');
  }
}

// Render board content
function renderBoard(board) {
  // Hide loading
  document.getElementById('loading').style.display = 'none';
  
  // Show content
  document.getElementById('board-content').style.display = 'block';

  // Set title
  document.getElementById('board-title').textContent = board.title;
  document.title = `${board.title} - Board Hole`;

  // Set metadata
  document.getElementById('board-author').textContent = board.authorName;
  document.getElementById('board-views').textContent = board.viewCount || 0;

  // Format date
  const createdDate = new Date(board.createdAt);
  document.getElementById('board-date').textContent = createdDate.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });

  // Set content
  document.getElementById('board-body').textContent = board.content;

  // Show action buttons based on user permissions
  showActionButtons(board, currentUser);
}

// Edit board
function editBoard() {
  window.location.href = `board-edit.html?id=${currentBoardId}`;
}

// Confirm delete
function confirmDelete() {
  if (confirm('정말로 이 게시글을 삭제하시겠습니까?')) {
    deleteBoard();
  }
}

// Delete board
async function deleteBoard() {
  try {
    const response = await fetch(`/api/boards/${currentBoardId}`, {
      method: 'DELETE',
      credentials: 'include'
    });

    if (response.status === 401) {
      alert('로그인이 필요합니다.');
      window.location.href = 'login.html';
      return;
    }

    if (!response.ok) {
      const errText = await response.text().catch(() => '');
      alert(`삭제에 실패했습니다. 다시 시도해주세요.\n${errText}`);
      return;
    }

    alert('게시글이 삭제되었습니다.');
    window.location.href = 'board.html';
  } catch (e) {
    console.error('Failed to delete board:', e);
    alert('삭제 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
  }
}

// Show action buttons based on user permissions
function showActionButtons(board, user) {
  const editBtn = document.getElementById('edit-btn');
  const deleteBtn = document.getElementById('delete-btn');
  
  if (!user) {
    // No user logged in - hide all action buttons
    if (editBtn) editBtn.style.display = 'none';
    if (deleteBtn) deleteBtn.style.display = 'none';
    return;
  }
  
  const isOwner = user.username === board.authorName;
  const isAdmin = user.roles && user.roles.includes('ROLE_ADMIN');
  
  // Edit button: only owner can edit
  if (editBtn) {
    editBtn.style.display = isOwner ? 'inline-block' : 'none';
  }
  
  // Delete button: owner or admin can delete
  if (deleteBtn) {
    deleteBtn.style.display = (isOwner || isAdmin) ? 'inline-block' : 'none';
  }
}

// Show error message
function showError(message) {
  document.getElementById('loading').style.display = 'none';
  document.getElementById('board-content').style.display = 'none';
  
  const errorDiv = document.getElementById('error-message');
  errorDiv.querySelector('span').textContent = message;
  errorDiv.style.display = 'block';
}
