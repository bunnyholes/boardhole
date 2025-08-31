// Admin user management functionality
let currentPage = 1;
let searchQuery = '';
let userToDelete = null;

// Page load event
window.addEventListener('load', () => {
  loadUsers(1);
});

// Load users with pagination
async function loadUsers(page = 1, search = '') {
  currentPage = page;
  searchQuery = search;
  
  // Show loading
  document.getElementById('loading').style.display = 'block';
  document.getElementById('user-list').style.display = 'none';
  document.getElementById('pagination').style.display = 'none';
  
  try {
    let url = `/api/users?page=${page - 1}&size=10`;
    if (search.trim()) {
      url += `&search=${encodeURIComponent(search.trim())}`;
    }
    
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });
    
    if (!response.ok) {
      if (response.status === 403) {
        showError('관리자 권한이 필요합니다.');
        return;
      }
      throw new Error(`HTTP ${response.status}`);
    }
    
    const data = await response.json();
    console.log('Users loaded:', data);
    
    renderUsers(data.content);
    renderPagination(data);
    
  } catch (error) {
    console.error('Failed to load users:', error);
    showError('사용자 목록을 불러오는데 실패했습니다.');
  }
}

// Render users table
function renderUsers(users) {
  const tbody = document.getElementById('user-table-body');
  tbody.innerHTML = '';
  
  if (!users || users.length === 0) {
    document.getElementById('empty-message').style.display = 'block';
    document.getElementById('user-list').style.display = 'block';
    document.getElementById('loading').style.display = 'none';
    return;
  }
  
  document.getElementById('empty-message').style.display = 'none';
  
  users.forEach(user => {
    const row = document.createElement('tr');
    
    // Format date
    const createdDate = new Date(user.createdAt);
    const dateStr = createdDate.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
    
    // Format roles
    const rolesBadges = user.roles ? user.roles.map(role => {
      const roleDisplay = role === 'ROLE_ADMIN' ? '관리자' : '사용자';
      const badgeClass = role === 'ROLE_ADMIN' ? 'bg-danger' : 'bg-secondary';
      return `<span class="badge ${badgeClass}">${roleDisplay}</span>`;
    }).join(' ') : '<span class="badge bg-secondary">사용자</span>';
    
    row.innerHTML = `
      <td>
        <strong>${escapeHtml(user.username)}</strong>
        <div class="d-md-none small text-muted">${escapeHtml(user.name)}</div>
        <div class="d-lg-none d-md-block small text-muted">${escapeHtml(user.email)}</div>
      </td>
      <td class="d-none d-md-table-cell">${escapeHtml(user.name)}</td>
      <td class="d-none d-lg-table-cell">${escapeHtml(user.email)}</td>
      <td class="text-center d-none d-sm-table-cell">${rolesBadges}</td>
      <td class="text-center d-none d-md-table-cell">${dateStr}</td>
      <td class="text-center">
        <div class="btn-group btn-group-sm" role="group" aria-label="사용자 관리">
          <button onclick="viewUserProfile(${user.id})" class="btn btn-outline-primary" title="프로필 보기">
            <i class="bi bi-person-circle"></i>
          </button>
          <button onclick="confirmDeleteUser(${user.id}, '${escapeHtml(user.username)}')" 
                  class="btn btn-outline-danger" 
                  title="사용자 삭제"
                  ${user.roles && user.roles.includes('ROLE_ADMIN') ? 'disabled' : ''}>
            <i class="bi bi-trash"></i>
          </button>
        </div>
      </td>
    `;
    
    tbody.appendChild(row);
  });
  
  // Show content
  document.getElementById('loading').style.display = 'none';
  document.getElementById('user-list').style.display = 'block';
}

// Render pagination
function renderPagination(pageData) {
  const pagination = document.getElementById('pagination');
  
  if (pageData.totalPages <= 1) {
    pagination.style.display = 'none';
    return;
  }
  
  let html = '<ul class="pagination justify-content-center">';
  
  // Previous button
  const prevDisabled = pageData.first ? 'disabled' : '';
  html += `
    <li class="page-item ${prevDisabled}">
      <button class="page-link" onclick="loadUsers(${currentPage - 1}, '${searchQuery}')" ${prevDisabled}>
        <i class="bi bi-chevron-left"></i> 이전
      </button>
    </li>
  `;
  
  // Page numbers
  const startPage = Math.max(1, currentPage - 2);
  const endPage = Math.min(pageData.totalPages, currentPage + 2);
  
  for (let i = startPage; i <= endPage; i++) {
    const activeClass = i === currentPage ? 'active' : '';
    html += `
      <li class="page-item ${activeClass}">
        <button class="page-link" onclick="loadUsers(${i}, '${searchQuery}')">${i}</button>
      </li>
    `;
  }
  
  // Next button
  const nextDisabled = pageData.last ? 'disabled' : '';
  html += `
    <li class="page-item ${nextDisabled}">
      <button class="page-link" onclick="loadUsers(${currentPage + 1}, '${searchQuery}')" ${nextDisabled}>
        다음 <i class="bi bi-chevron-right"></i>
      </button>
    </li>
  `;
  
  html += '</ul>';
  pagination.innerHTML = html;
  pagination.style.display = 'block';
}

// Search users
function searchUsers() {
  const input = document.getElementById('search-input');
  const query = input.value.trim();
  loadUsers(1, query);
}

// View user profile
function viewUserProfile(userId) {
  window.location.href = `user-profile.html?id=${userId}`;
}

// Confirm user deletion
function confirmDeleteUser(userId, username) {
  userToDelete = userId;
  document.getElementById('delete-username').textContent = username;
  
  const modal = new bootstrap.Modal(document.getElementById('deleteUserModal'));
  modal.show();
}

// Execute user deletion
async function executeUserDelete() {
  if (!userToDelete) return;
  
  try {
    const response = await fetch(`/api/users/${userToDelete}`, {
      method: 'DELETE',
      credentials: 'include'
    });
    
    if (response.ok) {
      // Close modal
      const modal = bootstrap.Modal.getInstance(document.getElementById('deleteUserModal'));
      modal.hide();
      
      // Show success message
      showSuccessMessage('사용자가 성공적으로 삭제되었습니다.');
      
      // Reload current page
      loadUsers(currentPage, searchQuery);
      
    } else if (response.status === 403) {
      showError('사용자 삭제 권한이 없습니다.');
    } else if (response.status === 404) {
      showError('사용자를 찾을 수 없습니다.');
    } else {
      showError('사용자 삭제에 실패했습니다.');
    }
    
  } catch (error) {
    console.error('User deletion failed:', error);
    showError('서버에 연결할 수 없습니다. 잠시 후 다시 시도해주세요.');
  }
  
  userToDelete = null;
}

// Show error message
function showError(message) {
  const alertHtml = `
    <div class="alert alert-danger alert-dismissible fade show" role="alert">
      <i class="bi bi-exclamation-triangle-fill"></i>
      <strong>오류:</strong> ${escapeHtml(message)}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  `;
  
  // Insert alert at top of card body
  const cardBody = document.querySelector('.card-body');
  cardBody.insertAdjacentHTML('afterbegin', alertHtml);
  
  // Auto-remove after 5 seconds
  setTimeout(() => {
    const alert = cardBody.querySelector('.alert-danger');
    if (alert) alert.remove();
  }, 5000);
}

// Show success message
function showSuccessMessage(message) {
  const alertHtml = `
    <div class="alert alert-success alert-dismissible fade show" role="alert">
      <i class="bi bi-check-circle-fill"></i>
      <strong>성공:</strong> ${escapeHtml(message)}
      <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
    </div>
  `;
  
  // Insert alert at top of card body
  const cardBody = document.querySelector('.card-body');
  cardBody.insertAdjacentHTML('afterbegin', alertHtml);
  
  // Auto-remove after 3 seconds
  setTimeout(() => {
    const alert = cardBody.querySelector('.alert-success');
    if (alert) alert.remove();
  }, 3000);
}

// Utility function to escape HTML
function escapeHtml(text) {
  const map = {
    '&': '&amp;',
    '<': '&lt;',
    '>': '&gt;',
    '"': '&quot;',
    "'": '&#039;'
  };
  return text.replace(/[&<>"']/g, m => map[m]);
}

// Handle search on Enter key
document.addEventListener('DOMContentLoaded', () => {
  const searchInput = document.getElementById('search-input');
  if (searchInput) {
    searchInput.addEventListener('keypress', (e) => {
      if (e.key === 'Enter') {
        searchUsers();
      }
    });
  }
});