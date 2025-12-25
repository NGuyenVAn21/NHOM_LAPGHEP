// wwwroot/admin/js/controllers.js
// ✅ Controllers đã fix để khớp với API backend

// ==================== DASHBOARD CONTROLLER ====================
window.dashboardInit = async () => {
    try {
        console.log('📊 Khởi tạo Dashboard...');

        const books = await window.api.get('/books');
        const users = await window.api.get('/users');
        const borrowings = await window.api.get('/borrowings?status=Borrowing');
        const events = await window.api.get('/events');

        console.log(`✅ Loaded: ${books.length} books, ${users.length} users, ${borrowings.length} borrowings, ${events.length} events`);

        document.getElementById('totalBooks').textContent = books.length;
        document.getElementById('totalUsers').textContent = users.length;
        document.getElementById('borrowingCount').textContent = borrowings.length;
        document.getElementById('eventCount').textContent = events.length;

        // Chart 1: Top Books (Lấy từ API popular)
        const popularBooks = await window.api.get('/books/popular');
        const topBooksCtx = document.getElementById('topBooksChart');
        if (topBooksCtx && popularBooks.length > 0) {
            new Chart(topBooksCtx, {
                type: 'bar',
                data: {
                    labels: popularBooks.slice(0, 5).map(b => b.title),
                    datasets: [{
                        label: 'Số lượt mượn',
                        data: popularBooks.slice(0, 5).map(b => b.borrow_count || 0),
                        backgroundColor: '#4e73df'
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false
                }
            });
        }

        // Chart 2: Status Pie
        const availableBooks = books.filter(b => b.status === 'Có sẵn').length;
        const outOfStockBooks = books.filter(b => b.status === 'Hết hàng').length;

        const statusChartCtx = document.getElementById('statusChart');
        if (statusChartCtx) {
            new Chart(statusChartCtx, {
                type: 'doughnut',
                data: {
                    labels: ['Có sẵn', 'Hết hàng'],
                    datasets: [{
                        data: [availableBooks, outOfStockBooks],
                        backgroundColor: ['#1cc88a', '#e74a3b']
                    }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false
                }
            });
        }

        console.log('✅ Dashboard đã load xong!');

    } catch (err) {
        console.error('❌ Lỗi dashboard:', err);
        document.getElementById('mainContent').innerHTML = `
            <div class="alert alert-danger m-4">
                <h5>Lỗi tải dữ liệu Dashboard</h5>
                <p>${err.message}</p>
            </div>
        `;
    }
};

// ==================== BOOKS CONTROLLER ====================
window.booksInit = async () => {
    console.log('📚 Khởi tạo Books page...');

    let books = [];

    async function loadBooks() {
        try {
            books = await window.api.get('/books');
            console.log(`✅ Đã load ${books.length} sách`);
            renderBooks();
        } catch (err) {
            console.error('❌ Lỗi tải sách:', err);
            alert('Lỗi tải sách: ' + err.message);
        }
    }

    function renderBooks() {
        const tbody = document.getElementById('bookTable');
        const categoryFilter = document.getElementById('categoryFilter').value;
        const keyword = document.getElementById('searchInput').value.toLowerCase();

        let filtered = books;

        if (categoryFilter) {
            const categoryName = getCategoryName(parseInt(categoryFilter));
            filtered = filtered.filter(b => b.category === categoryName);
        }

        if (keyword) {
            filtered = filtered.filter(b =>
                b.title.toLowerCase().includes(keyword) ||
                b.author.toLowerCase().includes(keyword)
            );
        }

        tbody.innerHTML = filtered.map(book => `
            <tr>
                <td>${book.id}</td>
                <td><img src="${book.coverImageUrl || 'https://via.placeholder.com/40x60'}" class="cover-img"></td>
                <td>${book.title}</td>
                <td>${book.author}</td>
                <td>${book.category}</td>
                <td><span class="badge bg-${book.status === 'Có sẵn' ? 'success' : 'danger'}">${book.status}</span></td>
                <td>${book.stock}</td>
                <td>
                    <button class="btn btn-sm btn-warning me-1" onclick="editBook(${book.id})">
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteBook(${book.id})">
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');
    }

    function getCategoryName(id) {
        const names = { 1: 'Văn học', 2: 'Kinh tế', 3: 'Tâm lý', 4: 'Khoa học', 5: 'Lịch sử', 6: 'Kỹ năng' };
        return names[id] || 'Khác';
    }

    window.openAddBookModal = function () {
        document.getElementById('modalTitle').textContent = 'Thêm sách mới';
        document.getElementById('bookId').value = '';
        ['title', 'author', 'description', 'coverImageUrl'].forEach(id =>
            document.getElementById(id).value = ''
        );
        document.getElementById('price').value = 0;
        document.getElementById('stockQuantity').value = 1;
        document.getElementById('categoryId').value = '';
        document.getElementById('coverPreview').src = 'https://via.placeholder.com/150';
        new bootstrap.Modal(document.getElementById('bookModal')).show();
    };

    window.editBook = async function (id) {
        const book = books.find(b => b.id == id);
        if (!book) return;

        document.getElementById('modalTitle').textContent = 'Chỉnh sửa sách';
        document.getElementById('bookId').value = book.id;
        document.getElementById('title').value = book.title;
        document.getElementById('author').value = book.author;
        document.getElementById('categoryId').value = book.categoryId || '';

        // ✅ Fix: Parse price (loại bỏ " VND" và dấu phẩy)
        const priceNum = book.price ? parseFloat(book.price.replace(/[^\d]/g, '')) : 0;
        document.getElementById('price').value = priceNum;

        document.getElementById('description').value = book.description || '';
        document.getElementById('stockQuantity').value = book.stock || 1;
        document.getElementById('coverImageUrl').value = book.coverImageUrl || '';
        document.getElementById('coverPreview').src = book.coverImageUrl || 'https://via.placeholder.com/150';

        new bootstrap.Modal(document.getElementById('bookModal')).show();
    };

    async function saveBook() {
        const id = document.getElementById('bookId').value;
        const book = {
            title: document.getElementById('title').value,
            author: document.getElementById('author').value,
            categoryId: parseInt(document.getElementById('categoryId').value) || null,
            price: parseFloat(document.getElementById('price').value) || 0,
            description: document.getElementById('description').value,
            stockQuantity: parseInt(document.getElementById('stockQuantity').value) || 1,
            coverImageUrl: document.getElementById('coverImageUrl').value
        };

        try {
            if (id) {
                await window.api.put(`/books/${id}`, book);
            } else {
                await window.api.post('/books', book);
            }
            await loadBooks();
            bootstrap.Modal.getInstance(document.getElementById('bookModal')).hide();
            alert('✅ Lưu thành công!');
        } catch (err) {
            console.error('❌ Lỗi lưu sách:', err);
            alert('❌ Lỗi: ' + err.message);
        }
    }

    window.deleteBook = async function (id) {
        if (!confirm('Xóa sách này?')) return;
        try {
            await window.api.del(`/books/${id}`);
            await loadBooks();
            alert('✅ Xóa thành công!');
        } catch (err) {
            console.error('❌ Lỗi xóa sách:', err);
            alert('❌ Lỗi: ' + err.message);
        }
    };

    await loadBooks();

    document.getElementById('searchBtn').addEventListener('click', renderBooks);
    document.getElementById('categoryFilter').addEventListener('change', renderBooks);
    document.getElementById('searchInput').addEventListener('keyup', (e) => {
        if (e.key === 'Enter') renderBooks();
    });
    document.getElementById('coverImageUrl').addEventListener('input', (e) => {
        document.getElementById('coverPreview').src = e.target.value || 'https://via.placeholder.com/150';
    });
    document.getElementById('saveBookBtn').addEventListener('click', saveBook);
};

// ==================== USERS CONTROLLER ====================
window.usersInit = async () => {
    console.log('👥 Khởi tạo Users page...');

    try {
        const users = await window.api.get('/users');
        console.log(`✅ Đã load ${users.length} người dùng`);

        const tbody = document.getElementById('userTable');

        if (users.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="fas fa-users fa-3x mb-3 d-block"></i>
                        Chưa có người dùng nào
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = users.map(u => `
            <tr>
                <td>${u.userId}</td>
                <td>
                    <img src="${u.avatarUrl || 'https://ui-avatars.com/api/?name=' + encodeURIComponent(u.fullName) + '&background=4e73df&color=fff'}"
                         class="rounded-circle"
                         width="40"
                         height="40"
                         alt="${u.fullName}">
                </td>
                <td>${u.fullName}</td>
                <td>${u.username}</td>
                <td>${u.email}</td>
                <td>
                    <span class="badge bg-${u.isActive ? 'success' : 'danger'}">
                        ${u.isActive ? 'Hoạt động' : 'Bị khóa'}
                    </span>
                </td>
                <td>
                    <button class="btn btn-sm btn-warning me-1" onclick="editUser(${u.userId})" disabled>
                        <i class="fas fa-edit"></i>
                    </button>
                    <button class="btn btn-sm btn-danger" onclick="deleteUser(${u.userId})" disabled>
                        <i class="fas fa-trash"></i>
                    </button>
                </td>
            </tr>
        `).join('');

    } catch (err) {
        console.error('❌ Lỗi tải người dùng:', err);
        document.getElementById('userTable').innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-danger py-4">
                    <i class="fas fa-exclamation-triangle fa-2x mb-2 d-block"></i>
                    Lỗi: ${err.message}
                </td>
            </tr>
        `;
    }
};

window.editUser = function (id) {
    alert(`Chức năng sửa user #${id} đang phát triển`);
};

window.deleteUser = function (id) {
    alert(`Chức năng xóa user #${id} đang phát triển`);
};

// ==================== BORROWINGS CONTROLLER ====================
window.borrowingsInit = () => {
    console.log('🔄 Khởi tạo Borrowings page...');
    loadBorrowings('Borrowing');
};

async function loadBorrowings(status) {
    try {
        console.log(`📥 Đang load borrowings với status: ${status}`);

        const borrowings = await window.api.get(`/borrowings?status=${status}`);
        console.log(`✅ Đã load ${borrowings.length} bản ghi`);

        // Update active button
        document.querySelectorAll('.btn-group button').forEach(btn => {
            btn.classList.remove('active');
        });
        event?.target?.classList.add('active');

        const tbody = document.getElementById('borrowingTable');

        if (borrowings.length === 0) {
            tbody.innerHTML = `
                <tr>
                    <td colspan="7" class="text-center text-muted py-4">
                        <i class="fas fa-inbox fa-3x mb-3 d-block"></i>
                        Không có bản ghi nào
                    </td>
                </tr>
            `;
            return;
        }

        tbody.innerHTML = borrowings.map(b => {
            const userName = b.user?.fullName || 'N/A';
            const bookTitle = b.book?.title || 'N/A';
            const borrowDate = new Date(b.borrowDate).toLocaleDateString('vi-VN');
            const dueDate = new Date(b.dueDate).toLocaleDateString('vi-VN');

            let badgeClass = 'secondary';
            if (b.status === 'Borrowing') badgeClass = 'warning';
            else if (b.status === 'Returned') badgeClass = 'success';
            else if (b.status === 'Overdue') badgeClass = 'danger';

            return `
                <tr>
                    <td>${b.recordId}</td>
                    <td>${userName}</td>
                    <td>${bookTitle}</td>
                    <td>${borrowDate}</td>
                    <td>${dueDate}</td>
                    <td><span class="badge bg-${badgeClass}">${b.status}</span></td>
                    <td>
                        ${b.status === 'Borrowing' ? `
                            <button class="btn btn-sm btn-success me-1" onclick="returnBook(${b.recordId})">
                                <i class="fas fa-check me-1"></i> Trả
                            </button>
                            <button class="btn btn-sm btn-info" onclick="extendBook(${b.recordId})">
                                <i class="fas fa-clock"></i> Gia hạn
                            </button>
                        ` : `
                            <button class="btn btn-sm btn-secondary" disabled>
                                Đã xử lý
                            </button>
                        `}
                    </td>
                </tr>
            `;
        }).join('');

    } catch (err) {
        console.error('❌ Lỗi tải mượn/trả:', err);
        document.getElementById('borrowingTable').innerHTML = `
            <tr>
                <td colspan="7" class="text-center text-danger py-4">
                    <i class="fas fa-exclamation-triangle fa-2x mb-2 d-block"></i>
                    Lỗi: ${err.message}
                </td>
            </tr>
        `;
    }
}

window.loadBorrowings = loadBorrowings;

window.returnBook = async function (id) {
    if (!confirm('Xác nhận trả sách?')) return;

    try {
        console.log(`📤 Đang xử lý trả sách ID: ${id}`);
        await window.api.put(`/borrowings/${id}/return`, {});
        console.log('✅ Trả sách thành công!');

        loadBorrowings('Borrowing');
        alert('✅ Trả sách thành công!');
    } catch (err) {
        console.error('❌ Lỗi trả sách:', err);
        alert('❌ Lỗi: ' + err.message);
    }
};

window.extendBook = async function (id) {
    if (!confirm('Gia hạn thêm 7 ngày?')) return;

    try {
        await window.api.put(`/borrowings/${id}/extend`, { days: 7 });
        loadBorrowings('Borrowing');
        alert('✅ Gia hạn thành công!');
    } catch (err) {
        console.error('❌ Lỗi gia hạn:', err);
        alert('❌ Lỗi: ' + err.message);
    }
};

// ==================== EVENTS CONTROLLER ====================
window.eventsInit = async () => {
    console.log('📅 Khởi tạo Events page...');

    try {
        const events = await window.api.get('/events');
        console.log(`✅ Đã load ${events.length} sự kiện`);

        const container = document.getElementById('eventList');

        if (events.length === 0) {
            container.innerHTML = `
                <div class="col-12">
                    <div class="alert alert-info text-center">
                        <i class="fas fa-calendar-plus fa-3x mb-3 d-block"></i>
                        <h5>Chưa có sự kiện nào</h5>
                        <p class="mb-0">Nhấn "Tạo sự kiện" để bắt đầu</p>
                    </div>
                </div>
            `;
            return;
        }

        container.innerHTML = events.map(e => {
            const startDate = e.startDate ? new Date(e.startDate).toLocaleDateString('vi-VN') : 'N/A';
            const endDate = e.endDate ? new Date(e.endDate).toLocaleDateString('vi-VN') : 'N/A';
            const isActive = e.isActive;

            return `
                <div class="col-md-6 mb-4">
                    <div class="card border-left-${isActive ? 'primary' : 'secondary'} shadow h-100">
                        <div class="card-body">
                            <div class="row no-gutters align-items-center">
                                <div class="col mr-2">
                                    <div class="text-xs font-weight-bold text-${isActive ? 'primary' : 'secondary'} text-uppercase mb-1">
                                        ${e.title}
                                    </div>
                                    <div class="h6 mb-2 text-gray-800">
                                        ${e.description || 'Không có mô tả'}
                                    </div>
                                    <div class="text-muted small mt-2">
                                        <i class="far fa-calendar me-2"></i>
                                        ${startDate} - ${endDate}
                                    </div>
                                    <div class="mt-2">
                                        <span class="badge bg-${isActive ? 'success' : 'secondary'}">
                                            ${isActive ? 'Đang diễn ra' : 'Đã kết thúc'}
                                        </span>
                                    </div>
                                </div>
                                <div class="col-auto">
                                    <i class="fas fa-${isActive ? 'calendar-check' : 'calendar-times'} fa-2x text-gray-300"></i>
                                </div>
                            </div>
                            <div class="mt-3">
                                <button class="btn btn-sm btn-primary me-2" disabled>
                                    <i class="fas fa-edit me-1"></i> Chỉnh sửa
                                </button>
                                <button class="btn btn-sm btn-danger" disabled>
                                    <i class="fas fa-trash"></i> Xóa
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

    } catch (err) {
        console.error('❌ Lỗi tải sự kiện:', err);
        document.getElementById('eventList').innerHTML = `
            <div class="col-12">
                <div class="alert alert-danger">
                    <h5><i class="fas fa-exclamation-triangle"></i> Lỗi</h5>
                    <p>${err.message}</p>
                </div>
            </div>
        `;
    }
};

console.log('✅ Controllers loaded!');