-- 1. TẠO DATABASE VÀ THIẾT LẬP MÔI TRƯỜNG
USE master;
GO

IF DB_ID('bookhub_db') IS NOT NULL
BEGIN
    ALTER DATABASE bookhub_db SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE bookhub_db;
END
GO

CREATE DATABASE bookhub_db;
GO

USE bookhub_db;
GO

-- 2. TẠO CÁC BẢNG
-- Bảng Danh mục sách (Categories)
-- Phục vụ cho BookSearchActivity (filter chips)
CREATE TABLE Categories (
    category_id INT IDENTITY(1,1) PRIMARY KEY,
    category_name NVARCHAR(100) NOT NULL UNIQUE,
    description NVARCHAR(255)
);
GO

-- Bảng Người dùng (Users)
-- Phục vụ LoginActivity, RegisterActivity, AccountActivity
CREATE TABLE Users (
    user_id INT IDENTITY(1,1) PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL, 
    full_name NVARCHAR(100) NOT NULL,
    phone_number VARCHAR(15),
    avatar_url NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    is_active BIT DEFAULT 1 
);
GO

-- Bảng Sách (Books)
-- Phục vụ HomeActivity, BookDetailActivity, ReadingActivity
CREATE TABLE Books (
    book_id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    author NVARCHAR(255) NOT NULL,
    publisher NVARCHAR(255),
    published_year INT,
    page_count INT,
    description NVARCHAR(MAX),
    price DECIMAL(18, 0) DEFAULT 0, -- Giá sách (lưu số, app tự format VND)
    cover_image_url NVARCHAR(MAX), -- Link ảnh bìa
    
    -- Khóa ngoại trỏ về bảng Category
    category_id INT, 
    CONSTRAINT FK_Books_Categories FOREIGN KEY (category_id) REFERENCES Categories(category_id),

    -- Trạng thái kho
    stock_quantity INT DEFAULT 1, -- Số lượng sách trong kho
    current_status NVARCHAR(50) DEFAULT N'Có sẵn', -- 'Có sẵn', 'Hết hàng'
    
    -- Thống kê (Cache để query nhanh hơn)
    average_rating FLOAT DEFAULT 0,
    review_count INT DEFAULT 0,
    
    created_at DATETIME DEFAULT GETDATE()
);
GO

-- Bảng Mượn trả sách (BorrowRecords)
-- Phục vụ BorrowHistoryActivity (3 tabs: Đang mượn, Lịch sử, Đặt trước)
CREATE TABLE BorrowRecords (
    record_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    borrow_date DATETIME DEFAULT GETDATE(),
    due_date DATETIME NOT NULL, -- Hạn trả
    return_date DATETIME NULL, -- Ngày trả thực tế (NULL nghĩa là chưa trả)
    
    -- Trạng thái mượn: 'Borrowing' (Đang mượn), 'Returned' (Đã trả), 'Overdue' (Quá hạn), 'Reserved' (Đặt trước)
    status VARCHAR(20) DEFAULT 'Borrowing' CHECK (status IN ('Borrowing', 'Returned', 'Overdue', 'Reserved', 'Cancelled')),
    
    CONSTRAINT FK_Borrow_Users FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT FK_Borrow_Books FOREIGN KEY (book_id) REFERENCES Books(book_id)
);
GO

-- Bảng Yêu thích (Favorites)
-- Phục vụ FavoriteActivity
CREATE TABLE Favorites (
    user_id INT,
    book_id INT,
    added_at DATETIME DEFAULT GETDATE(),
    PRIMARY KEY (user_id, book_id), -- Khóa chính phức hợp (Mỗi người chỉ thích 1 sách 1 lần)
    CONSTRAINT FK_Fav_Users FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT FK_Fav_Books FOREIGN KEY (book_id) REFERENCES Books(book_id)
);
GO

-- Bảng Đánh giá & Bình luận (Reviews)
-- Phục vụ hiển thị Rating trong BookDetailActivity
CREATE TABLE Reviews (
    review_id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,
    book_id INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5), -- Ràng buộc chỉ 1-5 sao
    comment NVARCHAR(MAX),
    created_at DATETIME DEFAULT GETDATE(),
    
    CONSTRAINT FK_Review_Users FOREIGN KEY (user_id) REFERENCES Users(user_id),
    CONSTRAINT FK_Review_Books FOREIGN KEY (book_id) REFERENCES Books(book_id)
);
GO

-- Bảng Sự kiện (Events)
-- Phục vụ EventsActivity
CREATE TABLE Events (
    event_id INT IDENTITY(1,1) PRIMARY KEY,
    title NVARCHAR(255) NOT NULL,
    description NVARCHAR(MAX),
    start_date DATETIME,
    end_date DATETIME,
    image_banner_url NVARCHAR(MAX),
    is_active BIT DEFAULT 1
);
GO
-- BẢNG CHƯƠNG SÁCH (Lưu nội dung đọc)
CREATE TABLE Chapters (
    chapter_id INT IDENTITY(1,1) PRIMARY KEY,
    book_id INT NOT NULL,
    chapter_num INT NOT NULL, -- Số thứ tự chương (1, 2, 3...)
    title NVARCHAR(255),      -- Tên chương (VD: Chương 1 - Gặp gỡ)
    content NVARCHAR(MAX),    -- Nội dung chính 
    
    CONSTRAINT FK_Chapters_Books FOREIGN KEY (book_id) REFERENCES Books(book_id)
);
GO

-- 3. VIEWS 
-- View xem lịch sử mượn chi tiết (Kết hợp tên sách, tên người dùng)
-- Giúp API lấy dữ liệu cho BorrowHistoryActivity dễ dàng hơn
CREATE VIEW v_BorrowHistoryDetails AS
SELECT 
    br.record_id,
    u.full_name,
    b.title AS book_title,
    b.cover_image_url,
    br.borrow_date,
    br.due_date,
    br.return_date,
    br.status
FROM BorrowRecords br
JOIN Users u ON br.user_id = u.user_id
JOIN Books b ON br.book_id = b.book_id;
GO

-- 4. FUNCTIONS (Hàm xử lý)
-- Hàm tính lại số sao trung bình của một cuốn sách
CREATE FUNCTION fn_CalculateAvgRating (@book_id INT)
RETURNS FLOAT
AS
BEGIN
    DECLARE @avg FLOAT;
    SELECT @avg = AVG(CAST(rating AS FLOAT)) FROM Reviews WHERE book_id = @book_id;
    RETURN ISNULL(@avg, 0);
END;
GO


-- 5. TRIGGERS (Tự động hóa)
-- Trigger 1: Tự động cập nhật số sao trung bình trong bảng Books khi có Review mới
CREATE TRIGGER trg_UpdateBookRating
ON Reviews
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    DECLARE @book_id INT;
    
    -- Lấy book_id từ dòng vừa thay đổi (đơn giản hóa cho trường hợp single row)
    SELECT TOP 1 @book_id = book_id FROM Inserted;
    IF @book_id IS NULL SELECT TOP 1 @book_id = book_id FROM Deleted;

    UPDATE Books
    SET average_rating = dbo.fn_CalculateAvgRating(@book_id),
        review_count = (SELECT COUNT(*) FROM Reviews WHERE book_id = @book_id)
    WHERE book_id = @book_id;
END;
GO

-- Trigger 2: Tự động cập nhật trạng thái sách khi MƯỢN sách
-- Nếu ai đó mượn -> Giảm tồn kho -> Nếu tồn kho = 0 thì status = 'Hết hàng'
CREATE TRIGGER trg_BorrowBook
ON BorrowRecords
AFTER INSERT
AS
BEGIN
    DECLARE @book_id INT;
    DECLARE @status VARCHAR(20);
    
    SELECT @book_id = book_id, @status = status FROM Inserted;

    IF @status = 'Borrowing'
    BEGIN
        UPDATE Books
        SET stock_quantity = stock_quantity - 1
        WHERE book_id = @book_id;

        -- Nếu hết sách thì đổi trạng thái hiển thị
        UPDATE Books
        SET current_status = N'Hết hàng'
        WHERE book_id = @book_id AND stock_quantity <= 0;
    END
END;
GO

-- Trigger 3: Tự động cập nhật trạng thái sách khi TRẢ sách
CREATE TRIGGER trg_ReturnBook
ON BorrowRecords
AFTER UPDATE
AS
BEGIN
    DECLARE @book_id INT;
    DECLARE @new_status VARCHAR(20);
    DECLARE @old_status VARCHAR(20);

    SELECT @book_id = book_id, @new_status = status FROM Inserted;
    SELECT @old_status = status FROM Deleted;

    -- Nếu chuyển từ 'Borrowing' sang 'Returned'
    IF @old_status = 'Borrowing' AND @new_status = 'Returned'
    BEGIN
        UPDATE Books
        SET stock_quantity = stock_quantity + 1
        WHERE book_id = @book_id;

        -- Có sách trả về thì update lại là Có sẵn
        UPDATE Books
        SET current_status = N'Có sẵn'
        WHERE book_id = @book_id AND stock_quantity > 0;
    END
END;
GO

-- 6. DỮ LIỆU MẪU (SEED DATA)
-- Thêm Categories
INSERT INTO Categories (category_name) VALUES 
(N'Văn học'), (N'Kinh tế'), (N'Tâm lý'), (N'Khoa học'), (N'Lịch sử'), (N'Kỹ năng');

-- Thêm Users 
INSERT INTO Users (full_name, username, email, password_hash) VALUES 
(N'Tô Đông Cẩn', 'todongcan', 'todongcan@test.com', '123456'),
(N'Nguyễn Văn A', 'vana', 'vana@test.com', '123456'),
(N'Trần Thị B', 'thib', 'thib@test.com', '123456');

-- Thêm Books với đầy đủ thông tin publisher, published_year, page_count, cover_image_url
INSERT INTO Books (title, author, category_id, publisher, published_year, page_count, price, description, average_rating, review_count, stock_quantity, current_status, cover_image_url) VALUES
(N'Nhà Giả Kim', N'Paulo Coelho', 1, N'NXB Văn Học', 1988, 208, 79000, N'Hành trình tìm kiếm vận mệnh của cậu bé chăn cừu Santiago qua sa mạc, tìm kiếm kho báu và ý nghĩa cuộc sống.', 4.7, 2548, 5, N'Có sẵn', 'https://cf.shopee.vn/file/1afbf7e5ee656f00543619174a8839cb'),
(N'Đắc Nhân Tâm', N'Dale Carnegie', 6, N'NXB Tổng hợp TP.HCM', 1936, 320, 86000, N'Cuốn sách kinh điển về nghệ thuật giao tiếp, thu phục lòng người và xây dựng mối quan hệ thành công.', 4.8, 3000, 0, N'Hết hàng', 'https://salt.tikicdn.com/cache/w1200/media/catalog/product/d/a/dacnhantam_2_1_1.jpg'), 
(N'Sapiens: Lược sử loài người', N'Yuval Noah Harari', 4, N'NXB Thế giới', 2011, 443, 150000, N'Khám phá lịch sử loài người từ thời kỳ đồ đá đến kỷ nguyên công nghệ, phân tích các cuộc cách mạng nhận thức, nông nghiệp và khoa học.', 4.8, 1200, 3, N'Có sẵn', 'https://tse2.mm.bing.net/th/id/OIP.2gw0VDgQgFBpXTaPqz-IbAHaK1?cb=ucfimg2&ucfimg=1&rs=1&pid=ImgDetMain&o=7&rm=3'),
(N'Tư Duy Phản Biện', N'Zoe McKey', 6, N'NXB Lao động', 2019, 280, 90000, N'Rèn luyện kỹ năng tư duy logic, phân tích vấn đề và đưa ra quyết định sáng suốt trong mọi tình huống.', 4.4, 500, 2, N'Có sẵn', 'https://cdn0.fahasa.com/media/catalog/product/8/9/8936066689922.jpg'),
(N'Bố Già', N'Mario Puzo', 1, N'NXB Văn học', 1969, 450, 110000, N'Tiểu thuyết kinh điển về thế giới mafia tại Mỹ, kể câu chuyện về gia đình Corleone và những cuộc chiến quyền lực.', 4.8, 3500, 5, N'Có sẵn', 'https://salt.tikicdn.com/cache/w1200/media/catalog/product/b/o/bo-gia.jpg'),
(N'Khởi Nghiệp Từ Con Số 0', N'Ashlee Vance', 2, N'NXB Thế giới', 2015, 400, 120000, N'Cuốn sách về hành trình xây dựng đế chế của Elon Musk - từ PayPal, SpaceX đến Tesla.', 4.7, 800, 5, N'Có sẵn', 'https://cdn-images.vtv.vn/2020/4/15/photo-1-1586951214865365002820.jpg'),
(N'Tâm Lý Học Hành Vi', N'Daniel Kahneman', 3, N'NXB Thế giới', 2011, 499, 180000, N'Cuốn sách khám phá về hai hệ thống tư duy của con người: hệ thống 1 nhanh và trực giác, hệ thống 2 chậm và logic.', 4.8, 2000, 3, N'Có sẵn', 'https://cdn1.fahasa.com/media/catalog/product/i/m/image_196453_1.jpg'),
(N'Lịch Sử Vạn Vật', N'Bill Bryson', 5, N'NXB Dân trí', 2003, 478, 150000, N'Hành trình khám phá lịch sử của vũ trụ, Trái Đất và loài người từ Big Bang đến hiện tại.', 4.7, 1800, 6, N'Có sẵn', 'https://product.hstatic.net/200000979221/product/lichsuvanvat-sachkhaitam_6c12a78184074455bbed50a2570881d9_master.jpg');

-- Thêm Reviews mẫu để test Trigger tính điểm
INSERT INTO Reviews (user_id, book_id, rating, comment) VALUES
(1, 1, 5, N'Sách rất hay!'),
(2, 1, 4, N'Cũng được, hơi ngắn.'),
(1, 5, 5, N'Truyện hay, hình ảnh đẹp'),
(2, 6, 4, N'Cảm hứng khởi nghiệp'),
(3, 7, 5, N'Hiểu về tâm lý bản thân'),
(1, 8, 4, N'Kiến thức lịch sử phong phú');

-- Thêm Lịch sử mượn mẫu
INSERT INTO BorrowRecords (user_id, book_id, borrow_date, due_date, status) VALUES
(1, 2, '2023-11-01', '2023-11-15', 'Returned'),
(1, 3, GETDATE(), DATEADD(day, 14, GETDATE()), 'Borrowing'),
(2, 5, '2023-12-01', '2023-12-15', 'Returned'),
(3, 6, GETDATE(), DATEADD(day, 14, GETDATE()), 'Borrowing');

GO 

-- Thêm nội dung sách 
-- 1. SÁCH: NHÀ GIẢ KIM (ID = 1)
INSERT INTO Chapters (book_id, chapter_num, title, content) VALUES 
(1, 1, N'Chương 1: Cậu bé chăn cừu', 
N'Cậu bé chăn cừu tên là Santiago. Cậu đã dành hai ngày ở thị trấn Tarifa để bán lông cừu cho một thương gia và mua thêm sách. Cậu đã quyết định dành thêm một ngày nữa ở lại thị trấn. Cậu biết rằng mình nên tiếp tục hành trình, nhưng thị trấn này khiến cậu cảm thấy thích thú.
Cậu đi dọc theo những con phố hẹp của thị trấn, dừng lại trước một tiệm sách cũ. Cửa sổ tiệm sách trưng bày một cuốn sách về giấc mơ và ý nghĩa của chúng. Santiago mỉm cười. Cậu thường mơ thấy cùng một giấc mơ: cậu đang chăn cừu thì một đứa trẻ xuất hiện và bắt đầu chơi với những con cừu. Bỗng nhiên, đứa trẻ cầm tay Santiago và dẫn cậu đến Kim Tự Tháp ở Ai Cập.
"Một ngày nào đó, cậu sẽ đến đó và tìm thấy một kho báu ẩn giấu," đứa trẻ nói trước khi biến mất.
Santiago đã kể giấc mơ này cho mẹ cậu, bà bảo rằng tất cả những người chăn cừu đều mơ thấy mình trở nên giàu có. Nhưng đối với Santiago, giấc mơ này có vẻ khác biệt. Nó lặp đi lặp lại, và mỗi lần như vậy, cậu cảm thấy một sự thôi thúc kỳ lạ.
Cậu bước vào tiệm sách. Người bán hàng, một ông lão với cặp kính dày, nhìn cậu từ phía sau quầy.
"Tôi muốn mua cuốn sách về giấc mơ," Santiago nói.
Ông lão gật đầu, lấy cuốn sách từ kệ và đưa cho cậu. "Đây là một cuốn sách hay. Nó sẽ giúp cậu hiểu được những thông điệp mà linh hồn gửi đến thông qua giấc mơ."'),
(1, 2, N'Chương 2: Lên đường', 
N'Santiago rời khỏi thị trấn Tarifa với cuốn sách về giấc mơ trong túi. Cậu cảm thấy một sự phấn khích kỳ lạ, như thể có điều gì đó lớn lao sắp xảy ra. Cậu quyết định sẽ không trở về với đàn cừu ngay lập tức, mà sẽ dành thêm thời gian để nghiền ngẫm về giấc mơ của mình.
Trên đường đi, cậu gặp một ông lão ngồi bên vệ đường. Ông ta có vẻ ngoài đơn giản nhưng đôi mắt lại toát lên một sự thông thái sâu sắc. Santiago dừng lại và chào hỏi ông.
"Chào ông," Santiago nói.
Ôld lão mỉm cười. "Chào cậu bé. Cậu đang trên đường đi đâu vậy?"
"Tôi đang trên đường trở về với đàn cừu của mình," Santiago trả lời.
"Nhưng có vẻ như cậu không hoàn toàn chắc chắn về điều đó," ông lão nói, đôi mắt như có thể nhìn thấu tâm can của Santiago.
Santiago ngạc nhiên. Làm sao ông lão có thể biết được? Cậu kể cho ông nghe về giấc mơ của mình và về cuốn sách cậu vừa mua.
Ông lão lắng nghe chăm chú, rồi nói: "Đôi khi, những giấc mơ không chỉ là giấc mơ. Chúng là thông điệp từ vũ trụ. Cậu nên lắng nghe chúng."');

-- 2. SÁCH: ĐẮC NHÂN TÂM (ID = 2)
INSERT INTO Chapters (book_id, chapter_num, title, content) VALUES 
(2, 1, N'Chương 1: Không chỉ trích, oán trách hay than phiền', 
N'Vào ngày 7 tháng 5 năm 1931, tiếng súng truy bắt tên tội phạm nguy hiểm nhất New York vang lên rền rĩ. Crowley "Hai Súng", kẻ giết người hàng loạt, bị vây bắt trong căn hộ của người tình. Hơn 150 cảnh sát bao vây ngôi nhà. Họ đục thủng mái nhà, dùng hơi cay để lôi hắn ra.
Crowley đã viết một bức thư: "Dưới lớp áo này là một trái tim mệt mỏi, nhưng dịu dàng, một trái tim không bao giờ làm hại ai". Thật nực cười! Một trái tim dịu dàng ư? Hắn vừa bắn chết một cảnh sát chỉ vì người đó hỏi giấy tờ xe.
Bài học rút ra ở đây là gì? Con người hiếm khi tự nhận mình sai. Chỉ trích là vô bổ, vì nó khiến người ta phải phòng thủ và thường cố gắng biện hộ cho chính mình. Chỉ trích là nguy hiểm, vì nó làm tổn thương lòng kiêu hãnh quý giá của con người, gây ra sự oán giận.
B.F. Skinner, nhà tâm lý học nổi tiếng thế giới, đã chứng minh qua các thí nghiệm rằng một con vật được khen thưởng vì hành vi tốt sẽ học nhanh hơn và nhớ lâu hơn nhiều so với một con vật bị trừng phạt vì hành vi xấu. Điều tương tự cũng đúng với con người.'),
(2, 2, N'Chương 2: Thành thật khen ngợi', 
N'Chỉ có một cách duy nhất trên thế giới này để khiến bất kỳ ai làm bất cứ điều gì. Bạn có bao giờ dừng lại để suy nghĩ về điều đó không? Vâng, chỉ có một cách. Đó là làm cho người đó *muốn* làm điều đó.
Hãy nhớ rằng, không có cách nào khác.
Tất nhiên, bạn có thể khiến một người làm việc bằng cách dí súng vào đầu họ. Bạn có thể khiến nhân viên làm việc bằng cách đe dọa đuổi việc. Nhưng những phương pháp thô bạo này mang lại những hậu quả vô cùng tồi tệ.
Sigmund Freud nói rằng mọi hành động của con người đều xuất phát từ hai động cơ: nhu cầu tình dục và khát khao được là người quan trọng. John Dewey, triết gia sâu sắc nhất của nước Mỹ, diễn đạt hơi khác một chút: "Động cơ sâu xa nhất trong bản chất con người là khao khát được đánh giá cao".');

-- 3. SÁCH: SAPIENS (ID = 3)
INSERT INTO Chapters (book_id, chapter_num, title, content) VALUES 
(3, 1, N'Chương 1: Cách mạng Nhận thức', 
N'Khoảng 13,5 tỷ năm trước, vật chất, năng lượng, thời gian và không gian đã hình thành trong một sự kiện được gọi là Big Bang. Câu chuyện về những đặc tính cơ bản này của vũ trụ được gọi là vật lý.
Khoảng 300.000 năm sau khi xuất hiện, vật chất và năng lượng bắt đầu kết hợp thành các cấu trúc phức tạp, gọi là nguyên tử, sau đó kết hợp thành phân tử. Câu chuyện về các nguyên tử, phân tử và sự tương tác của chúng được gọi là hóa học.
Khoảng 3,8 tỷ năm trước, trên một hành tinh gọi là Trái Đất, các phân tử nhất định đã kết hợp để tạo thành các cấu trúc đặc biệt lớn và phức tạp gọi là sinh vật. Câu chuyện về các sinh vật được gọi là sinh học.
Khoảng 70.000 năm trước, các sinh vật thuộc loài Homo sapiens bắt đầu hình thành các cấu trúc còn phức tạp hơn gọi là văn hóa. Sự phát triển tiếp theo của các nền văn hóa loài người được gọi là lịch sử. Ba cuộc cách mạng quan trọng đã định hình tiến trình lịch sử: Cách mạng Nhận thức khởi động lịch sử khoảng 70.000 năm trước. Cách mạng Nông nghiệp tăng tốc nó khoảng 12.000 năm trước. Cách mạng Khoa học, mới chỉ bắt đầu cách đây 500 năm.'),
(3, 2, N'Chương 2: Một loài động vật không quan trọng', 
N'Khoảng 2,5 triệu năm trước, con người tiền sử cũng chẳng quan trọng gì hơn so với các loài động vật khác sống chung với họ. Nếu bạn đi dạo ở Đông Phi 2 triệu năm trước, bạn có thể bắt gặp một dàn nhân vật quen thuộc: những bà mẹ lo lắng bế con, những đứa trẻ nghịch ngợm trong bùn, những thanh niên nóng nảy chống lại trật tự xã hội.
Họ yêu, chơi đùa, hình thành tình bạn thân thiết và cạnh tranh địa vị. Nhưng tinh tinh, khỉ đầu chó và voi cũng làm như vậy. Chẳng có gì đặc biệt cả. Con người lúc đó chỉ là một loài động vật tầm thường, tác động của họ lên môi trường cũng chẳng hơn gì khỉ đột, đom đóm hay sứa.');

-- 4. SÁCH: TƯ DUY PHẢN BIỆN (ID = 4)
INSERT INTO Chapters (book_id, chapter_num, title, content) VALUES 
(4, 1, N'Chương 1: Khám phá bộ não', 
N'Tại sao bạn lại suy nghĩ theo cách bạn đang suy nghĩ? Có bao giờ bạn tự hỏi tại sao mình lại đưa ra một quyết định tồi tệ trong quá khứ không? Hoặc tại sao bạn lại tin vào một điều gì đó mà sau này hóa ra là sai lầm?
Tư duy phản biện không phải là về việc trở nên thông minh hơn hay có nhiều kiến thức hơn. Đó là về việc suy nghĩ tốt hơn. Đó là khả năng phân tích thông tin một cách khách quan và đưa ra phán đoán hợp lý.
Bộ não của chúng ta là một cỗ máy kỳ diệu, nhưng nó cũng đầy rẫy những lối tắt và thiên kiến. Những lối tắt này, hay còn gọi là heuristics, giúp chúng ta đưa ra quyết định nhanh chóng trong cuộc sống hàng ngày, nhưng chúng cũng có thể dẫn chúng ta đi lạc hướng. Ví dụ, chúng ta thường có xu hướng tin vào những thông tin xác nhận niềm tin có sẵn của mình (thiên kiến xác nhận) và phớt lờ những bằng chứng trái ngược.'),
(4, 2, N'Chương 2: Rào cản của tư duy', 
N'Một trong những rào cản lớn nhất đối với tư duy phản biện là cái tôi của chúng ta. Chúng ta thích được đúng. Chúng ta ghét bị sai. Khi ai đó thách thức ý kiến của chúng ta, phản ứng tự nhiên của chúng ta là phòng thủ.
Để trở thành một người tư duy phản biện giỏi, bạn cần phải khiêm tốn về mặt trí tuệ. Bạn cần phải chấp nhận rằng mình có thể sai và sẵn sàng thay đổi quan điểm khi có bằng chứng mới.
Ngoài cái tôi, cảm xúc cũng đóng một vai trò lớn. Sợ hãi, giận dữ, và tham lam có thể làm mờ đi lý trí của chúng ta. Hãy tưởng tượng bạn đang tức giận với một đồng nghiệp. Liệu bạn có thể đánh giá khách quan ý tưởng của họ trong cuộc họp không? Rất khó.');

-- 5. SÁCH: BỐ GIÀ (ID = 5)
INSERT INTO Chapters (book_id, chapter_num, title, content) VALUES 
(5, 1, N'Chương 1: Đám cưới Connie', 
N'Vito Corleone, ông trùm của gia đình Corleone, đang tổ chức đám cưới cho con gái mình. Trong văn hóa Sicilia, không ai từ chối lời đề nghị vào ngày cưới của con gái ông trùm. Các thân chủ và bạn bè đến chúc mừng, mỗi người mang theo một ân huệ và hy vọng được ông giúp đỡ trong tương lai.'),
(5, 2, N'Chương 2: Lời đề nghị', 
N'Don Corleone nhận được một lời đề nghị từ Virgil Sollozzo về việc đầu tư vào ma túy. Sollozzo muốn sử dụng ảnh hưởng chính trị của Corleone để bảo vệ hoạt động buôn bán ma túy. Tuy nhiên, Don Corleone từ chối vì cho rằng ma túy là thứ buôn bán bẩn thỉu, khác xa với rượu và cờ bạc mà ông đang kinh doanh.');

-- 6. SÁCH: KHỞI NGHIỆP TỪ CON SỐ 0 (ID = 6)
INSERT INTO Chapters (book_id, chapter_num, title, content) VALUES 
(6, 1, N'Chương 2: Tuổi thơ ở Nam Phi', 
N'Elon Musk sinh năm 1971 tại Pretoria, Nam Phi. Từ nhỏ, Elon đã thể hiện sự thông minh và đam mê với công nghệ. Năm 10 tuổi, cậu tự học lập trình và tạo ra trò chơi video đầu tiên. Tuổi thơ không dễ dàng khi cha mẹ ly dị, nhưng điều đó đã rèn luyện cho Elon tính độc lập và kiên cường.'),
(6, 2, N'Bước chân vào thung lũng Silicon', 
N'Năm 1995, Elon Musk cùng em trai Kimbal thành lập Zip2, một công ty cung cấp bản đồ và danh bạ doanh nghiệp trực tuyến. Với chỉ 28.000 USD vốn từ cha, hai anh em làm việc không ngừng nghỉ. Năm 1999, Compaq mua lại Zip2 với giá 307 triệu USD, mang về cho Elon 22 triệu USD từ thương vụ này.');

-- 7. SÁCH: TÂM LÝ HỌC HÀNH VI (ID = 7)
INSERT INTO Chapters (book_id, chapter_num, title, content) VALUES 
(7, 1, N'Chương 1: Hai hệ thống tư duy', 
N'Bộ não của chúng ta vận hành thông qua hai hệ thống: Hệ thống 1 hoạt động tự động, nhanh chóng, không cần nỗ lực và không tự giác. Hệ thống 2 phân bổ sự chú ý đến các hoạt động tinh thần đòi hỏi nỗ lực, bao gồm các tính toán phức tạp.'),
(7, 2, N'Chương 2: Ảo tưởng về sự chú ý', 
N'Chúng ta thường đánh giá quá cao khả năng chú ý của mình. Thí nghiệm "vô hình khỉ" cho thấy khi tập trung vào một nhiệm vụ, chúng ta có thể bỏ lỡ những sự kiện rõ ràng ngay trước mắt. Điều này giải thích tại sao tài xế đang nói chuyện điện thoại có thể không nhìn thấy người đi bộ qua đường.');

-- 8. SÁCH: LỊCH SỬ VẠN VẬT (ID = 8)
INSERT INTO Chapters (book_id, chapter_num, title, content) VALUES 
(8, 1, N'Chương 1: Vụ nổ Big Bang', 
N'Vũ trụ bắt đầu từ một vụ nổ lớn cách đây 13,8 tỷ năm. Từ một điểm kỳ dị cực nhỏ, vũ trụ giãn nở với tốc độ kinh khủng. Trong phần triệu giây đầu tiên, các hạt cơ bản như quark và gluon hình thành. Sau đó, chúng kết hợp thành proton và neutron.'),
(8, 2, N'Chương 2: Sự hình thành Trái Đất', 
N'Trái Đất hình thành từ đám bụi và khí quanh Mặt Trời khoảng 4,6 tỷ năm trước. Ban đầu, Trái Đất là một quả cầu nóng chảy. Dần dần, nó nguội đi và hình thành lớp vỏ. Các đại dương xuất hiện từ hơi nước ngưng tụ và băng từ các sao chổi.');

GO

-- BỔ SUNG: mối quan hệ giữa người dùng và sự kiện
-- Thêm dữ liệu mẫu cho Events
INSERT INTO Events (title, description, start_date, end_date) VALUES 
(N'Tuần lễ Sách Mới', N'Giảm giá 20% cho các đầu sách mới xuất bản trong tháng này.', GETDATE(), DATEADD(day, 7, GETDATE())),
(N'Hội thảo Văn học', N'Giao lưu với các tác giả nổi tiếng.', GETDATE(), DATEADD(day, 3, GETDATE()));


SELECT * FROM Chapters WHERE book_id = 1;
GO
