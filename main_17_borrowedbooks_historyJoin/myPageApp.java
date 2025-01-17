package main_17_borrowedbooks_historyJoin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class myPageApp extends JFrame {
	private User loginUser;

	public myPageApp(User user) {
		this.loginUser = user;
		setTitle("My Page");
		setSize(700, 300);
		setLocationRelativeTo(null); // 화면의 가운데에 위치

		// 탭을 생성하는 메서드 호출
		JTabbedPane tabbedPane = createTabbedPane();

		// 전체 패널을 프레임에 추가
		getContentPane().removeAll(); // 모든 기존 내용을 제거
		add(tabbedPane);
		revalidate(); // 레이아웃 새로 고침
		repaint(); // 화면 업데이트
	}

	// 탭을 생성하는 팩토리 메서드
	private JTabbedPane createTabbedPane() {
		JTabbedPane tabbedPane = new JTabbedPane();

		// 나의 정보 탭
		JPanel myInfoPanel = createMyInfoPanel();
		tabbedPane.addTab("나의 정보", myInfoPanel);

		// 대여 중인 책 목록 탭
		JTable borrowedTable = new JTable();
		DefaultTableModel borrowedModel = new DefaultTableModel(new String[] { "책 제목", "저자", "카테고리", "대여일", "반납 기한일" },
				0);
		borrowedTable.setModel(borrowedModel);
		loadBorrowedBooks(borrowedModel);
		tabbedPane.addTab("대여중인 책 목록", new JScrollPane(borrowedTable));

		// 대여했던 책 목록 탭
		JTable borrowedHistoryTable = new JTable();
		DefaultTableModel borrowedHistoryModel = new DefaultTableModel(
				new String[] { "책 제목", "저자", "카테고리", "대여일", "대여 개수" }, 0);
		borrowedHistoryTable.setModel(borrowedHistoryModel);
		loadBorrowedHistory(borrowedHistoryModel);
		tabbedPane.addTab("대여했던 책 목록", new JScrollPane(borrowedHistoryTable));

		// 반납 목록 탭
		JTable returnedTable = new JTable();
		DefaultTableModel returnedModel = new DefaultTableModel(new String[] { "책 제목", "저자", "카테고리", "반납일", "반납 개수" }, 0);
		returnedTable.setModel(returnedModel);
		loadReturnedBooks(returnedModel);
		tabbedPane.addTab("반납 목록", new JScrollPane(returnedTable));

		return tabbedPane;
	}

	// 나의 정보 탭 패널 생성
	private JPanel createMyInfoPanel() {
		JPanel myInfoPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10); // 간격 설정

		// 아이디 레이블
		gbc.gridx = 0; // 열 위치
		gbc.gridy = 0; // 행 위치
		myInfoPanel.add(new JLabel("아이디:"), gbc);

		gbc.gridx = 1;
		myInfoPanel.add(new JLabel(loginUser.getUsername(true)), gbc);

		// 회원등급 레이블
		gbc.gridx = 0;
		gbc.gridy = 1;
		myInfoPanel.add(new JLabel("회원등급:"), gbc);

		gbc.gridx = 1;
		myInfoPanel.add(new JLabel(loginUser.getRole()), gbc);

		// 대여 권수 레이블
		gbc.gridx = 0;
		gbc.gridy = 2;
		myInfoPanel.add(new JLabel("대여 가능 권수:"), gbc);

		gbc.gridx = 1;
		myInfoPanel.add(new JLabel(String.valueOf(calculateAvailableBooksCount())), gbc);

		// 반납 권수 레이블
		gbc.gridx = 0;
		gbc.gridy = 3;
		myInfoPanel.add(new JLabel("반납 권수:"), gbc);

		gbc.gridx = 1;
		myInfoPanel.add(new JLabel(String.valueOf(getReturnedBooksCount())), gbc);

		// 총 대여 권수 레이블
		gbc.gridx = 0;
		gbc.gridy = 4;
		myInfoPanel.add(new JLabel("총 대여 권수:"), gbc);

		gbc.gridx = 1;
		myInfoPanel.add(new JLabel(String.valueOf(allCount())), gbc);

		return myInfoPanel;
	}

	// 대여 가능 권수 메소드
	private int calculateAvailableBooksCount() {
		int borrowAbleCount = 0;
		int borrowedCount = 0;

		try (Connection conn = DatabaseConnection.getConnection()) {
			// borrow_able 값 가져오기
			String borrowAbleSql = "SELECT borrow_able FROM users WHERE user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(borrowAbleSql)) {
				stmt.setInt(1, loginUser.getUserId());
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					borrowAbleCount = rs.getInt("borrow_able"); // borrow_able 값
				}
			}

			// borrowedBooks 테이블에서 대여 중인 책 수 가져오기
			String borrowedSql = "SELECT count FROM borrowedBooks WHERE user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(borrowedSql)) {
				stmt.setInt(1, loginUser.getUserId());
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					borrowedCount = rs.getInt(1); // 대여 중인 책 수
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "대여 가능 권수를 계산하는 데 실패했습니다: " + e.getMessage());
		}

		return borrowAbleCount - borrowedCount; // 대여 가능한 권수 계산
	}

	// 반납 권수 메소드
	private int getReturnedBooksCount() {
		int returnedCount = 0;
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT COUNT(*) as count FROM returnedBooks WHERE user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, loginUser.getUserId());
			ResultSet rs = stmt.executeQuery();

			if (rs.next()) {
				returnedCount = rs.getInt("count"); // 퍼센트 가져오기
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "반납 권수 로드 중 오류 발생");
		}
		return returnedCount; // 반환된 권수 반환
	}

	// 총 대여 권수 메소드
	private int allCount() {
		int borrowedCount = 0;
		int returnedCount = 0;

		try (Connection conn = DatabaseConnection.getConnection()) {
			// 대여 중인 책 수 가져오기
			String borrowedSql = "SELECT COUNT(*) FROM borrowedBooks WHERE user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(borrowedSql)) {
				stmt.setInt(1, loginUser.getUserId());
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					borrowedCount = rs.getInt(1); // 대여 중인 책 수
				}
			}

			// 반납된 책 수 가져오기
			String returnedSql = "SELECT COUNT(*) FROM returnedBooks WHERE user_id = ?";
			try (PreparedStatement stmt = conn.prepareStatement(returnedSql)) {
				stmt.setInt(1, loginUser.getUserId());
				ResultSet rs = stmt.executeQuery();
				if (rs.next()) {
					returnedCount = rs.getInt(1); // 반납된 책 수
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "총 대여 권수를 계산하는 데 실패했습니다: " + e.getMessage());
		}

		return borrowedCount + returnedCount; // 총 대여 권수 계산
	}

	// 대여 목록 데이터 로드 메소드
	public void loadBorrowedBooks(DefaultTableModel model) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT b.title, b.author, b.category, bb.borrow_date " + "FROM borrowedBooks bb "
					+ "JOIN book_db b ON bb.book_id = b.book_id " + "WHERE bb.user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, loginUser.getUserId()); // 사용자의 ID 설정
			ResultSet rs = stmt.executeQuery();

			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd"); // 날짜 형식 지정

			while (rs.next()) {
				// 대여일 가져오기
				String borrowDate = rs.getString("borrow_date");
				try {
					java.util.Date date = sdf.parse(borrowDate);
					Calendar cal = Calendar.getInstance();
					cal.setTime(date);
					cal.add(Calendar.DAY_OF_MONTH, 7); // 7일 추가

					// 반납 기한일 계산
					String returnDate = sdf.format(cal.getTime());

					model.addRow(new Object[] { rs.getString("title"), // 제목
							rs.getString("author"), // 저자
							rs.getString("category"), // 카테고리
							borrowDate, // 대여일
							returnDate // 반납 기한일
					});
				} catch (ParseException e) {
					e.printStackTrace();
					JOptionPane.showMessageDialog(this, "날짜 형식 오류: " + e.getMessage());
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "대여 목록 로드 중 오류 발생");
		}
	}

	// 반납 목록 데이터 로드 메소드
	private void loadReturnedBooks(DefaultTableModel model) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			String sql = "SELECT b.title, b.author, b.category, bb.return_date " + "FROM returnedBooks bb "
					+ "JOIN book_db b ON bb.copy_id = b.book_id " + // copy_id를 사용하여 book_db와 조인
					"WHERE bb.user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, loginUser.getUserId());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				model.addRow(new Object[] { rs.getString("title"), // 제목
						rs.getString("author"), // 저자
						rs.getString("category"), // 카테고리
						rs.getString("return_date") });
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "반납 목록 로드 중 오류 발생");
		}
	}

	// 대여했던 책 목록 데이터 로드 메소드
	private void loadBorrowedHistory(DefaultTableModel model) {
		try (Connection conn = DatabaseConnection.getConnection()) {

//            String sql = "SELECT user_id, book_id, borrow_date, count, return_status FROM borrowedbooks_history WHERE user_id = ?";
			String sql = "SELECT b.title, b.author, b.category, bb.borrow_date, bb.count, bb.return_status " + "FROM borrowedBooks_history bb "
					+ "JOIN book_db b ON bb.book_id = b.book_id " + "WHERE bb.user_id = ?";
			PreparedStatement stmt = conn.prepareStatement(sql);
			stmt.setInt(1, loginUser.getUserId());
			ResultSet rs = stmt.executeQuery();

			while (rs.next()) {
				model.addRow(new Object[] { rs.getString("title"), // 제목
						rs.getString("author"), // 저자
						rs.getString("category"), // 카테고리
						rs.getString("borrow_date"), // 대여일
						rs.getString("count"), // 대여 개수
						rs.getString("return_status"), // 대여 가능
				}); // 반납 기한일
			}
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "대여했던 책 목록 로드 중 오류 발생");
		}
	}
}
