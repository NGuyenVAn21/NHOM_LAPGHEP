// wwwroot/js/api.js hoặc wwwroot/admin/js/api.js
// ✅ FIX: Dùng đường dẫn tuyệt đối từ root để tránh lỗi CORS

// Lấy origin hiện tại (https://localhost:7273)
const API_BASE = `${window.location.origin}/api`;

console.log('🔗 API Base URL:', API_BASE);

// ✅ Expose globally để tất cả scripts đều dùng được
window.api = {
    get: (url) => {
        const fullUrl = `${API_BASE}${url}`;
        console.log('📥 GET:', fullUrl);

        return fetch(fullUrl)
            .then(r => {
                console.log(`✅ Response ${r.status}:`, fullUrl);
                if (!r.ok) throw new Error(`HTTP ${r.status}: ${r.statusText}`);
                return r.json();
            })
            .catch(err => {
                console.error('❌ GET Error:', fullUrl, err);
                throw err;
            });
    },

    post: (url, data) => {
        const fullUrl = `${API_BASE}${url}`;
        console.log('📤 POST:', fullUrl, data);

        return fetch(fullUrl, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(r => {
            console.log(`✅ Response ${r.status}:`, fullUrl);
            if (!r.ok) throw new Error(`HTTP ${r.status}: ${r.statusText}`);
            return r.json();
        }).catch(err => {
            console.error('❌ POST Error:', fullUrl, err);
            throw err;
        });
    },

    put: (url, data) => {
        const fullUrl = `${API_BASE}${url}`;
        console.log('📝 PUT:', fullUrl, data);

        return fetch(fullUrl, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(data)
        }).then(r => {
            console.log(`✅ Response ${r.status}:`, fullUrl);
            if (!r.ok) throw new Error(`HTTP ${r.status}: ${r.statusText}`);
            // PUT có thể trả về 204 No Content
            return r.status === 204 ? {} : r.json();
        }).catch(err => {
            console.error('❌ PUT Error:', fullUrl, err);
            throw err;
        });
    },

    del: (url) => {
        const fullUrl = `${API_BASE}${url}`;
        console.log('🗑️ DELETE:', fullUrl);

        return fetch(fullUrl, {
            method: 'DELETE'
        }).then(r => {
            console.log(`✅ Response ${r.status}:`, fullUrl);
            if (!r.ok) throw new Error(`HTTP ${r.status}: ${r.statusText}`);
            return r.ok;
        }).catch(err => {
            console.error('❌ DELETE Error:', fullUrl, err);
            throw err;
        });
    }
};

// ✅ Export cho module (nếu cần import)
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { api: window.api };
}