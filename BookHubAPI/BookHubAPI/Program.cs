var builder = WebApplication.CreateBuilder(args);

// 1. Thêm dịch vụ Controllers (Dòng này rất quan trọng để nhận diện thư mục Controllers)
builder.Services.AddControllers();

// 2. Cấu hình Swagger (để test API)
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen();

var app = builder.Build();

// 3. Cấu hình HTTP request pipeline.
if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseHttpsRedirection();

app.UseAuthorization();

// 4. Kích hoạt Controllers (Dòng này giúp API chạy được)
app.MapControllers();

app.Run();