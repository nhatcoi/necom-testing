FROM eclipse-temurin:11-jdk

# Cài đặt các gói cần thiết cho MySQL và các gói phụ thuộc cho project Spring
RUN apt-get update \
    && apt-get install -y --no-install-recommends netcat-openbsd locales \
    && locale-gen C.UTF-8 \
    && apt-get clean && rm -rf /var/lib/apt/lists/*

# Đặt thư mục làm việc mặc định
WORKDIR /necom-server

# Sao chép các tệp lưu trữ của project Spring vào trong Dockerfile
COPY target/necom-0.0.1-SNAPSHOT.jar /necom-server

# Mở cổng cho MySQL và Spring
EXPOSE 8085

# Đặt định dạng mã hóa Unicode
ENV LANG=C.UTF-8
ENV LC_ALL=C.UTF-8

# Chạy ứng dụng Spring
CMD ["java", "-jar", "necom-0.0.1-SNAPSHOT.jar"]
