// Board list functionality
let currentPage = 1;
let currentSearch = '';

// Page load event
window.addEventListener('load', () => {
	loadBoards(1);
});

// Load boards from API
async function loadBoards(page = 1, search = '') {
	try {
		// Show loading
		document.getElementById('loading').style.display = 'block';
		document.getElementById('board-list').style.display = 'none';
		// Keep pagination visible to avoid layout shift

		// Build API URL
		let url = `/api/boards?page=${page - 1}&size=10`;
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
		console.log('Board data loaded:', data);

		// Update global state
		currentPage = page;
		currentSearch = search;

		// Render boards
		renderBoards(data);

	} catch (error) {
		console.error('Failed to load boards:', error);
		showError('게시글을 불러오는데 실패했습니다.');
	}
}

// Render boards to table
function renderBoards(data) {
	const tableBody = document.getElementById('board-table-body');
	const emptyMessage = document.getElementById('empty-message');
	const boardList = document.getElementById('board-list');
	const pagination = document.getElementById('pagination');
	const loading = document.getElementById('loading');

	// Hide loading
	loading.style.display = 'none';

	// Clear existing content (keep fixed pagination markup)
	tableBody.innerHTML = '';

	if (data.content && data.content.length > 0) {
		// Show board list
		boardList.style.display = 'block';
		emptyMessage.style.display = 'none';

		// Render each board
		data.content.forEach(board => {
			const row = document.createElement('tr');
			row.style.borderBottom = '1px solid #eee';
			row.style.cursor = 'pointer';
			row.onclick = () => viewBoard(board.id);

			const formattedDate = new Date(board.createdAt).toLocaleDateString('ko-KR', {
				year: 'numeric',
				month: '2-digit',
				day: '2-digit'
			});

			row.innerHTML = `
        <td><strong>${escapeHtml(board.title)}</strong></td>
        <td class="d-none d-md-table-cell">${escapeHtml(board.authorName)}</td>
        <td class="text-center d-none d-sm-table-cell">${board.viewCount || 0}</td>
        <td class="text-center d-none d-md-table-cell">${formattedDate}</td>
      `;

			tableBody.appendChild(row);
		});

		// Update fixed pagination controls with page numbers (groups of 10)
		renderPaginationFixed(data);

	} else {
		// Show empty message
		boardList.style.display = 'block';
		emptyMessage.style.display = 'block';
	}
}

// Fixed prev/next with dynamic page numbers (10 per group)
function renderPaginationFixed(data) {
	const totalPages = data.totalPages ?? 1;
	const currentPageNum = (data.number ?? 0) + 1;

	const ul = document.getElementById('pg-list');
	const firstItem = document.getElementById('pg-first');
	const prevItem = document.getElementById('pg-prev');
	const nextItem = document.getElementById('pg-next');
	const lastItem = document.getElementById('pg-last');
	const firstLink = document.getElementById('pg-first-link');
	const prevLink = document.getElementById('pg-prev-link');
	const nextLink = document.getElementById('pg-next-link');
	const lastLink = document.getElementById('pg-last-link');

	if (!ul || !prevItem || !nextItem || !firstItem || !lastItem) return;

	// Remove old page items between prev and next
	Array.from(ul.querySelectorAll('li.page-item'))
			.filter(li => li !== firstItem && li !== prevItem && li !== nextItem && li !== lastItem)
			.forEach(li => li.remove());

	// Compute page group (1-10, 11-20, ...)
	const groupStart = Math.floor((currentPageNum - 1) / 10) * 10 + 1;
	const groupEnd = Math.min(groupStart + 9, totalPages);

	// Insert numeric page buttons before 'next'
	for (let i = groupStart; i <= groupEnd; i++) {
		const li = document.createElement('li');
		li.className = 'page-item' + (i === currentPageNum ? ' active' : '');
		const a = document.createElement('a');
		a.className = 'page-link';
		a.href = '#';
		a.textContent = String(i);
		a.onclick = (e) => {
			e.preventDefault();
			loadBoards(i, currentSearch);
		};
		li.appendChild(a);
		ul.insertBefore(li, nextItem);
	}

	// Prev/Next enable/disable (always visible, only state toggles)
	const canPrev = currentPageNum > 1;
	togglePageItem(prevItem, prevLink, canPrev, () => loadBoards(currentPageNum - 1, currentSearch));

	const canNext = currentPageNum < totalPages;
	togglePageItem(nextItem, nextLink, canNext, () => loadBoards(currentPageNum + 1, currentSearch));

	// First/Last enable/disable
	const canFirst = currentPageNum > 1;
	const canLast = currentPageNum < totalPages;
	togglePageItem(firstItem, firstLink, canFirst, () => loadBoards(1, currentSearch));
	togglePageItem(lastItem, lastLink, canLast, () => loadBoards(totalPages, currentSearch));
}

function togglePageItem(itemEl, linkEl, enabled, onClick) {
	if (!itemEl || !linkEl) return;
	if (enabled) {
		itemEl.classList.remove('disabled');
		linkEl.removeAttribute('tabindex');
		linkEl.removeAttribute('aria-disabled');
		linkEl.onclick = (e) => {
			e.preventDefault();
			onClick();
		};
	} else {
		itemEl.classList.add('disabled');
		linkEl.setAttribute('tabindex', '-1');
		linkEl.setAttribute('aria-disabled', 'true');
		linkEl.onclick = (e) => { e.preventDefault(); };
	}
}

// Search boards
function searchBoards() {
	const searchInput = document.getElementById('search-input');
	const searchTerm = searchInput.value.trim();
	loadBoards(1, searchTerm);
}

// Handle Enter key in search input
document.addEventListener('DOMContentLoaded', () => {
	const searchInput = document.getElementById('search-input');
	if (searchInput) {
		searchInput.addEventListener('keypress', (e) => {
			if (e.key === 'Enter') {
				searchBoards();
			}
		});
	}
});

// Navigate to board detail
function viewBoard(boardId) {
	window.location.href = `board-detail.html?id=${boardId}`;
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
