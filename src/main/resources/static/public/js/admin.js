// Admin dashboard functionality

// Page load event
window.addEventListener('load', async () => {
  await checkAdminAccess();
});

// Check if user has admin access
async function checkAdminAccess() {
  try {
    // Test admin endpoint to verify access
    const response = await fetch('/api/auth/admin-only', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    if (response.status === 401) {
      window.location.href = 'login.html';
      return;
    }

    if (response.status === 403) {
      showAccessDenied();
      return;
    }

    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }

    const adminData = await response.json();
    console.log('Admin access confirmed:', adminData);

    // Show admin dashboard
    showAdminDashboard(adminData);
    
  // Load statistics
  await loadStatistics();

  } catch (error) {
    console.error('Failed to verify admin access:', error);
    showAccessDenied();
  }
}

// Show admin dashboard
function showAdminDashboard(adminData) {
  document.getElementById('loading').style.display = 'none';
  document.getElementById('admin-dashboard').style.display = 'block';
  
  // Set admin name
  document.getElementById('admin-name').textContent = adminData.username;
}

// Show access denied
function showAccessDenied() {
  document.getElementById('loading').style.display = 'none';
  document.getElementById('access-denied').style.display = 'block';
}

// Load system statistics
async function loadStatistics() {
  try {
    const res = await fetch('/api/admin/stats', { credentials: 'include' });
    if (!res.ok) {
      throw new Error(`HTTP ${res.status}`);
    }
    const data = await res.json();
    document.getElementById('total-users').textContent = data.totalUsers ?? '-';
    document.getElementById('total-boards').textContent = data.totalBoards ?? '-';
    document.getElementById('total-views').textContent = data.totalViews ?? '-';
  } catch (error) {
    console.error('Failed to load statistics:', error);
  }
}

// Test admin access endpoint
async function testAdminAccess() {
  try {
    showTestResults('관리자 권한 테스트 중...');
    
    const response = await fetch('/api/auth/admin-only', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    const responseText = await response.text();
    let result = `Status: ${response.status}\n`;
    
    if (response.ok) {
      const data = JSON.parse(responseText);
      result += `Response: ${JSON.stringify(data, null, 2)}`;
      result += '\n\n✅ 관리자 권한 테스트 성공!';
    } else {
      result += `Error: ${responseText}`;
      result += '\n\n❌ 관리자 권한 테스트 실패';
    }
    
    showTestResults(result);

  } catch (error) {
    showTestResults(`❌ 테스트 실패: ${error.message}`);
  }
}

// Test user access endpoint
async function testUserAccess() {
  try {
    showTestResults('사용자 권한 테스트 중...');
    
    const response = await fetch('/api/auth/user-access', {
      method: 'GET',
      credentials: 'include',
      headers: {
        'Content-Type': 'application/json'
      }
    });

    const responseText = await response.text();
    let result = `Status: ${response.status}\n`;
    
    if (response.ok) {
      const data = JSON.parse(responseText);
      result += `Response: ${JSON.stringify(data, null, 2)}`;
      result += '\n\n✅ 사용자 권한 테스트 성공!';
    } else {
      result += `Error: ${responseText}`;
      result += '\n\n❌ 사용자 권한 테스트 실패';
    }
    
    showTestResults(result);

  } catch (error) {
    showTestResults(`❌ 테스트 실패: ${error.message}`);
  }
}

// Show test results
function showTestResults(message) {
  document.getElementById('test-results').style.display = 'block';
  document.getElementById('test-output').textContent = message;
}
