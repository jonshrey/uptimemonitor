// Because the browser is making the request, it talks directly to localhost:8080
const API_URL = 'http://localhost:8080/api/urls';

async function fetchUrls() {
    try {
        const response = await fetch(API_URL);
        const urls = await response.json();
        renderTable(urls);
    } catch (error) {
        console.error('Error fetching URLs:', error);
    }
}

function renderTable(urls) {
    const tbody = document.querySelector('#url-table tbody');
    tbody.innerHTML = '';
    urls.forEach(url => {
        const tr = document.createElement('tr');
        let statusClass = 'status-pending';
        if (url.status === 'UP') statusClass = 'status-up';
        else if (url.status === 'DOWN') statusClass = 'status-down';
        
        tr.innerHTML = `
            <td>${url.address}</td>
            <td class="${statusClass}">${url.status}</td>
            <td>${url.responseTimeMs >= 0 ? url.responseTimeMs + ' ms' : 'N/A'}</td>
            <td>${url.lastChecked ? new Date(url.lastChecked).toLocaleString() : 'Never'}</td>
        `;
        tbody.appendChild(tr);
    });
}

document.getElementById('add-url-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const address = document.getElementById('url-input').value;
    const button = e.target.querySelector('button');
    button.disabled = true;
    button.innerText = 'Adding...';
    
    try {
        // This will trigger our new ResponseEntity 201 Created response!
        await fetch(API_URL, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ address })
        });
        document.getElementById('url-input').value = '';
        fetchUrls(); // Refresh the table immediately
    } catch (error) {
        alert('Error adding URL.');
    } finally {
        button.disabled = false;
        button.innerText = 'Monitor URL';
    }
});

// Initial fetch and auto-refresh every 5 seconds
fetchUrls();
setInterval(fetchUrls, 5000); 