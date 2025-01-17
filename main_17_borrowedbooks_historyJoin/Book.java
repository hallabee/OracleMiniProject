package main_17_borrowedbooks_historyJoin;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.sql.*;

//도서 클래스
public class Book {
	private int bookId = 0;
	private String author = "";
	private int availableCopies = 0;
	private String category = "";
	private int returnDate = 0;
	private String title = "";
	private int totalCopies = 0;

	// 기본 생성자
	public Book() {
	}

	// 매개변수가 있는 생성자
	public Book(int bookId, String author, int availableCopies, String category, int returnDate, String title,
			int totalCopies) {
		this.bookId = bookId;
		this.author = author;
		this.availableCopies = availableCopies;
		this.category = category;
		this.returnDate = returnDate;
		this.title = title;
		this.totalCopies = totalCopies;
	}

	// Getter 및 Setter 메서드
	public int getBookId() {
		return bookId;
	}

	public void setBookId(int bookId) {
		this.bookId = bookId;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getAvailableCopies() {
		return availableCopies;
	}

	public void setAvailableCopies(int availableCopies) {
		this.availableCopies = availableCopies;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getReturnDate() {
		return returnDate;
	}

	public void setReturnDate(int returnDate) {
		this.returnDate = returnDate;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public int getTotalCopies() {
		return totalCopies;
	}

	public void setTotalCopies(int totalCopies) {
		this.totalCopies = totalCopies;
	}

	public static String getDate(int daysToAdd) {
		// 현재 날짜 가져오기
		LocalDate currentDate = LocalDate.now();
		// 현재 날짜에 daysToAdd 만큼 더하기
		LocalDate futureDate = currentDate.plusDays(daysToAdd);
		// 날짜를 문자열로 포맷팅
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
		try {
			// 데이터베이스 연결
			Connection conn = DatabaseConnection.getConnection();

			// SYSDATE를 이용해 현재 날짜와 시간을 가져오는 쿼리 실행
			String query = "SELECT SYSDATE FROM DUAL";
			PreparedStatement pstmt = conn.prepareStatement(query);

			ResultSet rs = pstmt.executeQuery();

			if (rs.next()) {
				// SYSDATE로부터 시간 가져오기
				Timestamp timestamp = rs.getTimestamp(1);

				// Timestamp를 LocalDateTime으로 변환
				LocalDateTime dateTime = timestamp.toLocalDateTime();

				// 포맷터 생성
				formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

				// 포맷팅된 문자열로 변환
				String formattedDateTime = dateTime.format(formatter);

				// 포맷팅된 시간 출력
				return formattedDateTime;
			}

			// 자원 해제
			rs.close();
			pstmt.close();
			conn.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("로컬 시간으로 반환");
		return futureDate.format(formatter);
	}

	public String getDate() {
		// 현재 날짜 가져오기
		return getDate(this.returnDate);
	}

	public boolean isBorrowed(int userId) {
		String query = "SELECT book_id FROM borrowedBooks WHERE book_id = ? AND user_id = ?";
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query)) {

			stmt.setInt(1, this.bookId); // 현재 책의 ID 설정
			stmt.setInt(2, userId); // 사용자의 ID 설정

			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return rs.getInt(1) > 0; // 존재하는 행 수에 따라 true 또는 false 리턴
			}
		} catch (SQLException e) {
			e.printStackTrace(); // 에러 로깅
		}
		return false; // 예외 발생 시 기본값 false 리턴
	}
}