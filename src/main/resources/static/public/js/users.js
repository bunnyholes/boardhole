// Users list functionality
let currentPage = 1;
let currentSearch = '';

// Page load event
window.addEventListener('load', () => {
  loadUsers(1);
});

// Load users from API
async function loadUsers(page = 1, search = '') {
  try {
    // Show loading
    document.getElementById('loading').style.display = 'block';
    document.getElementById('user-list').style.display = 'none';
    document.getElementById('pagination').style.display = 'none';

    // Build API URL
    let url = `/api/users?page=${page - 1}&size=10`;
    if (search) {
      url += `&search=${encodeURIComponent(search)}`;
    }

    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const data = await response.json();
    console.log('Users data loaded:', data);

    // Update global state
    currentPage = page;
    currentSearch = search;

    // Render users
    renderUsers(data);

  } catch (error) {
    console.error('Failed to load users:', error);
    showError('사용자 목록을 불러오는데 실패했습니다.');
  }
}

// Render users to table
function renderUsers(data) {
  const tableBody = document.getElementById('user-table-body');
  const emptyMessage = document.getElementById('empty-message');
  const userList = document.getElementById('user-list');
  const pagination = document.getElementById('pagination');
  const loading = document.getElementById('loading');

  // Hide loading
  loading.style.display = 'none';

  // Clear existing content
  tableBody.innerHTML = '';
  pagination.innerHTML = '';

  if (data.content && data.content.length > 0) {
    // Show user list
    userList.style.display = 'block';
    emptyMessage.style.display = 'none';

    // Render each user
    data.content.forEach(user => {
      const row = document.createElement('tr');
      row.style.borderBottom = '1px solid #eee';
      row.style.cursor = 'pointer';
      row.onclick = () => viewUserProfile(user.id);

      const formattedDate = new Date(user.createdAt).toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });

      row.innerHTML = `
        <td style="padding: 1rem;">
          <div style="font-weight: 500;">${escapeHtml(user.username)}</div>
        </td>
        <td style="padding: 1rem;">${escapeHtml(user.name)}</td>
        <td style="padding: 1rem;">${escapeHtml(user.email)}</td>
        <td style="padding: 1rem; text-align: center;">${formattedDate}</td>
      `;

      tableBody.appendChild(row);
    });

    // Show pagination
    renderPagination(data);
    pagination.style.display = 'block';

  } else {
    // Show empty message
    userList.style.display = 'block';
    emptyMessage.style.display = 'block';
  }
}

// Render pagination controls
function renderPagination(data) {
  const pagination = document.getElementById('pagination');
  const totalPages = data.totalPages;
  const currentPageNum = data.number + 1; // API uses 0-based indexing

  if (totalPages <= 1) return;

  let paginationHtml = '<div style="display: flex; justify-content: center; gap: 0.5rem; align-items: center;">';

  // Previous button
  if (currentPageNum > 1) {
    paginationHtml += `<button onclick="loadUsers(${currentPageNum - 1}, '${currentSearch}')" class="btn btn-secondary">이전</button>`;
  }

  // Page numbers
  const startPage = Math.max(1, currentPageNum - 2);
  const endPage = Math.min(totalPages, currentPageNum + 2);

  for (let i = startPage; i <= endPage; i++) {
    if (i === currentPageNum) {
      paginationHtml += `<span style="padding: 0.5rem 1rem; background: #007bff; color: white; border-radius: 4px;">${i}</span>`;
    } else {
      paginationHtml += `<button onclick="loadUsers(${i}, '${currentSearch}')" class="btn btn-secondary">${i}</button>`;
    }
  }

  // Next button
  if (currentPageNum < totalPages) {
    paginationHtml += `<button onclick="loadUsers(${currentPageNum + 1}, '${currentSearch}')" class="btn btn-secondary">다음</button>`;
  }

  paginationHtml += '</div>';
  pagination.innerHTML = paginationHtml;
}

// Search users
function searchUsers() {
  const searchInput = document.getElementById('search-input');
  const searchTerm = searchInput.value.trim();
  loadUsers(1, searchTerm);
}

// Handle Enter key in search input
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

// Navigate to user profile
function viewUserProfile(userId) {
  window.location.href = `user-profile.html?id=${userId}`;
}

// Utility function to escape HTML
function escapeHtml(text) {
  const div = document.createElement('div');
  div.textContent = text;
  return div.innerHTML;
}

// Show error message
function showError(message) {
  const loading = document.getElementById('loading');
  loading.innerHTML = `<p style="color: red;">${message}</p>`;
  loading.style.display = 'block';
}