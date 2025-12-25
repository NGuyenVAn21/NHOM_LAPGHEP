// wwwroot/js/core.js hoặc wwwroot/admin/js/core.js
// ✅ FIX: Xử lý đường dẫn động dựa trên vị trí hiện tại

export const Core = {
    routes: {
        'dashboard': { title: 'Dashboard', view: 'dashboard' },
        'books': { title: 'Quản lý Sách', view: 'books' },
        'users': { title: 'Người dùng', view: 'users' },
        'borrowings': { title: 'Mượn & Trả', view: 'borrowings' },
        'events': { title: 'Sự kiện', view: 'events' }
    },

    currentView: null,

    // ✅ Xác định base path cho views
    getBasePath() {
        const path = window.location.pathname;
        console.log('📍 Current path:', path);

        // Nếu đang ở /admin/, return path admin
        if (path.includes('/admin/')) {
            return '/admin/js/views';
        }
        // Nếu không, return path gốc
        return '/js/views';
    },

    init() {
        console.log('🚀 Core.init() - Khởi tạo hệ thống...');
        console.log('📂 Base path:', this.getBasePath());

        // Toggle sidebar
        const menuToggle = document.getElementById('menuToggle');
        if (menuToggle) {
            menuToggle.addEventListener('click', () => {
                document.getElementById('wrapper').classList.toggle('toggled');
            });
        }

        // Sidebar click → chuyển trang
        document.getElementById('sidebarMenu').addEventListener('click', (e) => {
            const link = e.target.closest('[data-route]');
            if (link) {
                e.preventDefault();
                this.navigateTo(link.getAttribute('data-route'));
            }
        });

        // Kiểm tra window.api đã sẵn sàng chưa
        if (typeof window.api === 'undefined') {
            console.error('❌ window.api chưa được load! Kiểm tra lại api.js');
            alert('❌ Lỗi: Không thể kết nối API. Vui lòng kiểm tra console để biết thêm chi tiết.');
        } else {
            console.log('✅ window.api đã sẵn sàng');
        }
    },

    async navigateTo(route) {
        console.log(`🔀 Navigating to: ${route}`);

        const config = this.routes[route];
        if (!config) {
            console.error(`❌ Route không tồn tại: ${route}`);
            return;
        }

        document.title = `BookHub Admin - ${config.title}`;
        this.updateBreadcrumb(route);

        // Hiển thị loading
        document.getElementById('mainContent').innerHTML = `
            <div class="text-center py-5">
                <div class="spinner-border text-primary" role="status"></div>
                <p class="mt-3">Đang tải ${config.title}...</p>
            </div>
        `;

        try {
            // ✅ Load view HTML
            const viewPath = `${this.getBasePath()}/${config.view}.html`;
            console.log(`📄 Loading view: ${viewPath}`);

            const response = await fetch(viewPath);
            if (!response.ok) {
                throw new Error(`HTTP ${response.status}: ${response.statusText}`);
            }

            const html = await response.text();
            document.getElementById('mainContent').innerHTML = html;

            // Highlight menu
            document.querySelectorAll('[data-route]').forEach(el => el.classList.remove('active'));
            const activeLink = document.querySelector(`[data-route="${route}"]`);
            if (activeLink) {
                activeLink.classList.add('active');
            }

            // ✅ Gọi init function (đã được load từ controllers.js)
            const initFunctionName = `${config.view}Init`;
            if (typeof window[initFunctionName] === 'function') {
                console.log(`✅ Gọi ${initFunctionName}()`);
                await window[initFunctionName]();
            } else {
                console.error(`❌ Không tìm thấy function ${initFunctionName}() trong ${config.view}.js`);
            }

        } catch (err) {
            console.error('❌ Lỗi load view:', err);
            document.getElementById('mainContent').innerHTML = `
                <div class="alert alert-danger m-4">
                    <h5><i class="fas fa-exclamation-triangle"></i> Lỗi tải trang</h5>
                    <p>Không thể tải trang "<strong>${config.title}</strong>"</p>
                    <hr>
                    <p class="mb-0"><strong>Chi tiết:</strong> ${err.message}</p>
                    <button class="btn btn-primary mt-3" onclick="Core.navigateTo('dashboard')">
                        <i class="fas fa-home me-1"></i> Về Dashboard
                    </button>
                </div>
            `;
        }
    },

    updateBreadcrumb(route) {
        const titles = {
            'dashboard': 'Dashboard',
            'books': 'Quản lý Sách',
            'users': 'Người dùng',
            'borrowings': 'Mượn & Trả',
            'events': 'Sự kiện'
        };

        const breadcrumbEl = document.getElementById('breadcrumb');
        if (breadcrumbEl) {
            breadcrumbEl.innerHTML = `
                <ol class="breadcrumb mb-0">
                    <li class="breadcrumb-item">
                        <a href="#" onclick="event.preventDefault(); Core.navigateTo('dashboard')" class="text-decoration-none">
                            Admin
                        </a>
                    </li>
                    <li class="breadcrumb-item active">${titles[route] || route}</li>
                </ol>
            `;
        }
    }
};