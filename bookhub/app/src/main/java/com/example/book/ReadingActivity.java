package com.example.book;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ReadingActivity extends AppCompatActivity {

    private TextView tvChapterTitle, tvContent, tvScreenTitle;
    private Button btnPrevChapter, btnNextChapter;
    private ImageButton btnBookmark, btnBack, btnSearch, btnNotification;
    private int currentChapter = 1;
    private int totalChapters = 3;
    private boolean isBookmarked = false;

    // Dữ liệu chương sách
    private final String[] chapterTitles = {
            "Nhà Giả Kim - Chương 1",
            "Nhà Giả Kim - Chương 2",
            "Nhà Giả Kim - Chương 3"
    };

    private final String[] chapterContents = {
            "Cậu bé chăn cừu tên là Santiago. Cậu đã dành hai ngày ở thị trấn Tarifa để bán lông cừu cho một thương gia và mua thêm sách. Cậu đã quyết định dành thêm một ngày nữa ở lại thị trấn. Cậu biết rằng mình nên tiếp tục hành trình, nhưng thị trấn này khiến cậu cảm thấy thích thú.\n\n" +
                    "Cậu đi dọc theo những con phố hẹp của thị trấn, dừng lại trước một tiệm sách cũ. Cửa sổ tiệm sách trưng bày một cuốn sách về giấc mơ và ý nghĩa của chúng. Santiago mỉm cười. Cậu thường mơ thấy cùng một giấc mơ: cậu đang chăn cừu thì một đứa trẻ xuất hiện và bắt đầu chơi với những con cừu. Bỗng nhiên, đứa trẻ cầm tay Santiago và dẫn cậu đến Kim Tự Tháp ở Ai Cập.\n\n" +
                    "\"Một ngày nào đó, cậu sẽ đến đó và tìm thấy một kho báu ẩn giấu,\" đứa trẻ nói trước khi biến mất.\n\n" +
                    "Santiago đã kể giấc mơ này cho mẹ cậu, bà bảo rằng tất cả những người chăn cừu đều mơ thấy mình trở nên giàu có. Nhưng đối với Santiago, giấc mơ này có vẻ khác biệt. Nó lặp đi lặp lại, và mỗi lần như vậy, cậu cảm thấy một sự thôi thúc kỳ lạ.\n\n" +
                    "Cậu bước vào tiệm sách. Người bán hàng, một ông lão với cặp kính dày, nhìn cậu từ phía sau quầy.\n\n" +
                    "\"Tôi muốn mua cuốn sách về giấc mơ,\" Santiago nói.\n\n" +
                    "Ông lão gật đầu, lấy cuốn sách từ kệ và đưa cho cậu. \"Đây là một cuốn sách hay. Nó sẽ giúp cậu hiểu được những thông điệp mà linh hồn gửi đến thông qua giấc mơ.\"\n\n" +
                    "Santiago trả tiền và rời tiệm sách, lòng đầy phấn khích. Cậu không biết rằng cuốn sách này sẽ là khởi đầu cho một hành trình thay đổi cuộc đời cậu mãi mãi.",

            "Santiago rời khỏi thị trấn Tarifa với cuốn sách về giấc mơ trong túi. Cậu cảm thấy một sự phấn khích kỳ lạ, như thể có điều gì đó lớn lao sắp xảy ra. Cậu quyết định sẽ không trở về với đàn cừu ngay lập tức, mà sẽ dành thêm thời gian để nghiền ngẫm về giấc mơ của mình.\n\n" +
                    "Trên đường đi, cậu gặp một ông lão ngồi bên vệ đường. Ông ta có vẻ ngoài đơn giản nhưng đôi mắt lại toát lên một sự thông thái sâu sắc. Santiago dừng lại và chào hỏi ông.\n\n" +
                    "\"Chào ông,\" Santiago nói.\n\n" +
                    "Ông lão mỉm cười. \"Chào cậu bé. Cậu đang trên đường đi đâu vậy?\"\n\n" +
                    "\"Tôi đang trên đường trở về với đàn cừu của mình,\" Santiago trả lời.\n\n" +
                    "\"Nhưng có vẻ như cậu không hoàn toàn chắc chắn về điều đó,\" ông lão nói, đôi mắt như có thể nhìn thấu tâm can của Santiago.\n\n" +
                    "Santiago ngạc nhiên. Làm sao ông lão có thể biết được? Cậu kể cho ông nghe về giấc mơ của mình và về cuốn sách cậu vừa mua.\n\n" +
                    "Ôld lão lắng nghe chăm chú, rồi nói: \"Đôi khi, những giấc mơ không chỉ là giấc mơ. Chúng là thông điệp từ vũ trụ. Cậu nên lắng nghe chúng.\"",

            "Cuộc trò chuyện với ông lão đã khiến Santiago suy nghĩ rất nhiều. Cậu nhận ra rằng mình không thể tiếp tục cuộc sống chăn cừu như trước đây. Có một điều gì đó lớn lao hơn đang chờ đợi cậu, và cậu cảm thấy mình phải đi tìm nó.\n\n" +
                    "Santiago quyết định sẽ không trở về với đàn cừu. Thay vào đó, cậu sẽ đi đến Ai Cập để tìm kiếm kho báu mà cậu đã mơ thấy. Đó là một quyết định táo bạo, nhưng cậu cảm thấy đó là điều mình phải làm.\n\n" +
                    "Cậu bán hết đàn cừu và dùng số tiền đó để mua vé tàu đến châu Phi. Đó là lần đầu tiên trong đời Santiago rời khỏi Tây Ban Nha. Cậu cảm thấy vừa sợ hãi vừa phấn khích.\n\n" +
                    "Khi con tàu rời bến, Santiago nhìn lại quê hương lần cuối. Cậu không biết điều gì đang chờ đợi mình ở phía trước, nhưng cậu tin rằng mình đang đi đúng hướng."
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading);

        initViews();
        setupClickListeners();
        updateChapter();

        // Ẩn action bar mặc định
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
    }

    private void initViews() {
        tvScreenTitle = findViewById(R.id.tvScreenTitle);
        tvChapterTitle = findViewById(R.id.tvChapterTitle);
        tvContent = findViewById(R.id.tvContent);
        btnPrevChapter = findViewById(R.id.btnPrevChapter);
        btnNextChapter = findViewById(R.id.btnNextChapter);
        btnBookmark = findViewById(R.id.btnBookmark);
        btnBack = findViewById(R.id.btnBack);
        btnSearch = findViewById(R.id.btnSearch);
        btnNotification = findViewById(R.id.btnNotification);
    }

    private void setupClickListeners() {
        btnBack.setOnClickListener(v -> finish());

        btnSearch.setOnClickListener(v ->
                showToast("Tính năng tìm kiếm"));

        btnNotification.setOnClickListener(v ->
                showToast("Thông báo"));

        btnPrevChapter.setOnClickListener(v -> {
            if (currentChapter > 1) {
                currentChapter--;
                updateChapter();
                showToast("Đã chuyển đến chương trước");
            }
        });

        btnNextChapter.setOnClickListener(v -> {
            if (currentChapter < totalChapters) {
                currentChapter++;
                updateChapter();
                showToast("Đã chuyển đến chương tiếp theo");
            }
        });

        btnBookmark.setOnClickListener(v -> {
            isBookmarked = !isBookmarked;
            updateBookmarkButton();

            if (isBookmarked) {
                showToast("Đã lưu trang sách");
            } else {
                showToast("Đã bỏ lưu trang sách");
            }
        });
    }

    private void updateChapter() {
        // Cập nhật tiêu đề và nội dung
        tvChapterTitle.setText(chapterTitles[currentChapter - 1]);
        tvContent.setText(chapterContents[currentChapter - 1]);

        // Cập nhật trạng thái nút
        btnPrevChapter.setEnabled(currentChapter > 1);
        btnNextChapter.setEnabled(currentChapter < totalChapters);

        // Hiệu ứng khi chuyển chương
        btnPrevChapter.animate().scaleX(0.95f).scaleY(0.95f).setDuration(100)
                .withEndAction(() -> btnPrevChapter.animate().scaleX(1f).scaleY(1f).setDuration(100));
    }

    private void updateBookmarkButton() {
        if (isBookmarked) {
            btnBookmark.setImageResource(R.drawable.ic_bookmark);
            btnBookmark.animate().scaleX(1.2f).scaleY(1.2f).setDuration(200)
                    .withEndAction(() -> btnBookmark.animate().scaleX(1f).scaleY(1f).setDuration(200));
        } else {
            btnBookmark.setImageResource(R.drawable.ic_bookmark_border);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}