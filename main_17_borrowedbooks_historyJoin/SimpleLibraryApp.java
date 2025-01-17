package main_17_borrowedbooks_historyJoin;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SimpleLibraryApp extends JFrame {

	private User loginUser;

	private JTextField searchField;
	private JTable bookTable;
	private JTextArea bookDetailsArea;
	private DefaultTableModel tableModel;
	private JLabel bookCountLabel;

	// 도서 데이터를 리스트로 관리
	private List<Book> bookList;
	private Book selectedBook;

	public SimpleLibraryApp(User loginUser) {
		this.loginUser = loginUser;

		setTitle("Halla Library");
		setSize(1000, 800);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);

		// 도서 데이터 초기화
		bookList = new ArrayList<>();
		loadBookList("book_db"); // 도서 데이터 로드

		// 상단 메뉴바
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("메뉴");
		JMenu menu1 = new JMenu("건의사항");
		JMenuItem loginItem = new JMenuItem("로그인");
		JMenuItem logoutItem = new JMenuItem("로그아웃");
		JMenuItem postItem = new JMenuItem("문의하기");
		postItem.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new PostForm(loginUser);
			}
		});
		
		menu.add(loginItem);
		menu.add(logoutItem);
		menu1.add(postItem);
		menuBar.add(menu);
		menuBar.add(menu1);
		setJMenuBar(menuBar);

		// 검색 및 카테고리 패널
		JPanel topPanel = new JPanel(new BorderLayout());
		JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		searchField = new JTextField(20);
		JButton searchButton = new JButton("검색");
		searchPanel.add(new JLabel("도서 검색:"));
		searchPanel.add(searchField);
		searchPanel.add(searchButton);

		JButton myPageButton = new JButton("MYPAGE");
		JPanel categoryPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		JButton allBooksButton = new JButton("전체 도서");
//        JButton category1Button = new JButton("프로그래밍");
		JButton category2Button = new JButton("자기계발");
		JLabel mailbox = MailboxButtonExample.createMailboxLabel("mailbox.jpg", 50, 50, loginUser);

		bookCountLabel = new JLabel(); // 대여가능 권수 확인
		categoryPanel.add(bookCountLabel);
		updateAvailableBooksCount();
//      바꿀 예정
		JLabel UserStateLabel = new JLabel(loginUser.getUsername(false) + "님 환영합니다.") {
		};

		categoryPanel.add(bookCountLabel);
		categoryPanel.add(myPageButton);
		categoryPanel.add(UserStateLabel);
		categoryPanel.add(allBooksButton);
//        categoryPanel.add(category1Button);
		categoryPanel.add(category2Button);
		categoryPanel.add(mailbox);

		topPanel.add(searchPanel, BorderLayout.WEST);
		topPanel.add(categoryPanel, BorderLayout.EAST);
		add(topPanel, BorderLayout.NORTH);

		// 도서 목록 테이블
		String[] columnNames = { "책번호", "제목", "저자", "카테고리", "반납기간", "총 개수", "남은 책 수" };
		tableModel = new DefaultTableModel(columnNames, 0);
		bookTable = new JTable(tableModel);
		loadBooks(null); // 전체 도서 로드
		JScrollPane tableScrollPane = new JScrollPane(bookTable);
		add(tableScrollPane, BorderLayout.CENTER);

		// 도서 상세 정보 패널
		bookDetailsArea = new JTextArea();
		bookDetailsArea.setEditable(false);
		JScrollPane detailsScrollPane = new JScrollPane(bookDetailsArea);
		detailsScrollPane.setPreferredSize(new Dimension(300, 0));

		JPanel rightPanel = new JPanel(new BorderLayout());
		rightPanel.add(detailsScrollPane, BorderLayout.CENTER);

		JPanel actionPanel = new JPanel(new FlowLayout());
		JButton borrowButton = new JButton("대여");
		JButton returnButton = new JButton("반납");

		actionPanel.add(borrowButton);
		actionPanel.add(returnButton);
		rightPanel.add(actionPanel, BorderLayout.SOUTH);

		add(rightPanel, BorderLayout.EAST);

		// 버튼 리스너 추가
		searchButton.addActionListener(new SearchActionListener());
		bookTable.getSelectionModel().addListSelectionListener(e -> showBookDetails());

		// 카테고리 버튼 리스너 추가
		allBooksButton.addActionListener(e -> loadBooks(null));
//        프로그래밍 제외하고 넣을 것
//        category1Button.addActionListener(e -> loadBooks("프로그래밍"));
		category2Button.addActionListener(e -> loadBooks("자기계발"));

		// 마이페이지 버튼 이벤트 처리
		myPageButton.addActionListener(e -> {
			myPageApp myPage = new myPageApp(loginUser); // 사용자 정보를 전달
			myPage.setVisible(true); // 새 창 표시
		});

		// 대여 버튼 이벤트 처리
		borrowButton.addActionListener(e -> {
			List<Book> selectedBooks = getSelectedBooks(); // 선택된 책 목록 가져오기
			if (!selectedBooks.isEmpty()) {
				borrowSelectedBooks(selectedBooks);
			} else {
				JOptionPane.showMessageDialog(this, "대여할 책을 선택해 주세요.");
			}
		});

		// 반납 버튼 이벤트 처리
		returnButton.addActionListener(e -> {
			if (selectedBook != null && selectedBook.isBorrowed(loginUser.getUserId())) {
				returnBook(selectedBook);
			} else {
				JOptionPane.showMessageDialog(this, "반납할 수 없는 도서입니다.");
			}
		});
	}

	public SimpleLibraryApp() throws HeadlessException, SQLException {
		this(new User());
	}

	// 검색 기능
	private class SearchActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			String query = searchField.getText().toLowerCase();
			loadBooks(query);
		}
	}

	public void updatePage() {
		loadBooks(null);
	}

//  책 형태의 테이블 가져오기
	private void loadBookList() {
		bookList = new BookService().getAllBooks(); // 기본 테이블 books 가져오기
	}

//    오버로딩
	private void loadBookList(String table_name) {
		bookList = new BookService().getAllBooks(table_name);
	}

	// 도서 목록 로딩 (카테고리 또는 검색어 필터링)
	private void loadBooks(String filter) {
		tableModel.setRowCount(0); // 기존 테이블 데이터 초기화

		for (Book book : bookList) {
			if (filter == null || book.getTitle().toLowerCase().contains(filter)
					|| book.getAuthor().toLowerCase().contains(filter)
					|| book.getCategory().toLowerCase().contains(filter)) {
				tableModel.addRow(new Object[] { book.getBookId(), book.getTitle(), book.getAuthor(),
						book.getCategory(), book.getReturnDate(), book.getTotalCopies(), book.getAvailableCopies() });
			}
		}
	}

	// 도서 상세 정보 표시
	private void showBookDetails() {
		int selectedRow = bookTable.getSelectedRow();
		if (selectedRow != -1) {
			String title = (String) bookTable.getValueAt(selectedRow, 1);
			selectedBook = getBookByTitle(title);

			if (selectedBook != null) {
				bookDetailsArea.setText("\n제목: " + selectedBook.getTitle() + "\n저자: " + selectedBook.getAuthor()
						+ "\n카테고리: " + selectedBook.getCategory() + "\n반납기간: " + selectedBook.getReturnDate()
						+ "\n총 개수: " + selectedBook.getTotalCopies() + "\n대여 상태: "
						+ (selectedBook.isBorrowed(loginUser.getUserId()) ? "대여 중" : "대여 가능"));
			}
		}
	}

	private Book getBookByTitle(String title) {
		for (Book book : bookList) {
			if (book.getTitle().equals(title)) {
				return book;
			}
		}
		return null;
	}

	private void updateAvailableBooksCount() {
		int availableBooksCount = calculateAvailableBooksCount(); // 대여 가능한 책 수 계산
		bookCountLabel.setText(availableBooksCount + "권 대여가능"); // UI 업데이트
	}

	// 대여 가능 권수 확인 메소드
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

	private List<Book> getSelectedBooks() {
		List<Book> selectedBooks = new ArrayList<>();
		int[] selectedRows = bookTable.getSelectedRows(); // 선택된 행의 인덱스 가져오기
		for (int rowIndex : selectedRows) {
			int bookId = (int) bookTable.getValueAt(rowIndex, 0); // 책 ID 가져오기
			Book book = getBookById(bookId); // 책 객체 가져오기
			if (book != null) {
				selectedBooks.add(book); // 선택된 책 목록에 추가
			}
		}
		return selectedBooks;
	}

	private Book getBookById(int bookId) {
		for (Book book : bookList) {
			if (book.getBookId() == bookId) {
				return book;
			}
		}
		return null; // 해당 ID의 책이 없으면 null 반환
	}

	// 책 대여 메소드
	private void borrowSelectedBooks(List<Book> selectedBooks) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			conn.setAutoCommit(false); // 트랜잭션 시작

			if (calculateAvailableBooksCount() < selectedBooks.size()) {
				JOptionPane.showMessageDialog(this, "대여할 수 있는 도서 수를 초과했습니다.");
				return; // 대여할 수 없는 경우 종료
			}

			for (Book book : selectedBooks) {
				// 대여 가능 조건 확인
				if (book.getAvailableCopies() < 1) {
					JOptionPane.showMessageDialog(this, book.getTitle() + "는 대여 가능 도서가 없습니다.");
					continue; // 대여 불가능시 다음 책으로
				}

				// 대여 중 책 수 확인
				String checkBorrowedSql = "SELECT COUNT(*) FROM borrowedBooks WHERE book_id = ? AND user_id = ?";
				try (PreparedStatement pstmt = conn.prepareStatement(checkBorrowedSql)) {
					pstmt.setInt(1, book.getBookId());
					pstmt.setInt(2, loginUser.getUserId());
					ResultSet rs = pstmt.executeQuery();
					if (rs.next() && rs.getInt(1) > 0) {
						JOptionPane.showMessageDialog(this, book.getTitle() + "는 이미 대여 중입니다.");
						continue; // 다음 책으로 넘어감
					}
				}

				// borrowedbooks의 count 1씩 증가
				String updateCountSql = "UPDATE borrowedbooks SET count = count + 1 WHERE user_id = ?";
				try (PreparedStatement pstmt = conn.prepareStatement(updateCountSql)) {
					pstmt.setInt(1, loginUser.getUserId());
					pstmt.executeUpdate();
				}

				// 대여 처리 로직
				// borrowedBooks 테이블에 추가
				int userCount = 0;
				String countSql = "SELECT count FROM borrowedBooks WHERE user_id = ?";
				try (PreparedStatement pstmtCount = conn.prepareStatement(countSql)) {
					pstmtCount.setInt(1, loginUser.getUserId());
					ResultSet rsCount = pstmtCount.executeQuery();
					if (rsCount.next()) {
						userCount = rsCount.getInt("count");
						String insertBorrowSql = "INSERT INTO borrowedBooks (user_id, book_id, borrow_date, count, return_status) VALUES (?, ?, TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'), ?, ?)";
						try (PreparedStatement pstmtInsert = conn.prepareStatement(insertBorrowSql)) {
							pstmtInsert.setInt(1, loginUser.getUserId());
							pstmtInsert.setInt(2, book.getBookId());
							// pstmtInsert.setString(3, currentDate);
							pstmtInsert.setInt(3, userCount); // 새로운 대여
							pstmtInsert.setInt(4, 1); // 대여 상태 1
							pstmtInsert.executeUpdate();
							JOptionPane.showMessageDialog(this, book.getTitle() + "선택된 책이 대여되었습니다.");
						}
					} else {
						// user_id가 존재하지 않는 경우, 새로 삽입
						String insertBorrowSql = "INSERT INTO borrowedBooks (user_id, book_id, borrow_date, count, return_status) VALUES (?, ?, TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'), ?, ?)";
						try (PreparedStatement pstmtInsert = conn.prepareStatement(insertBorrowSql)) {
							pstmtInsert.setInt(1, loginUser.getUserId());
							pstmtInsert.setInt(2, book.getBookId());
							// pstmtInsert.setString(3, currentDate);
							pstmtInsert.setInt(3, 1); // 새로운 대여
							pstmtInsert.setInt(4, 1); // 대여 상태 1
							pstmtInsert.executeUpdate();
							JOptionPane.showMessageDialog(this, book.getTitle() + "선택된 책이 대여되었습니다.");
						}
					}
				}

				// 대여 가능 도서 수 업데이트
				String updateCopiesSql = "UPDATE book_db SET available_copies = available_copies - 1 WHERE book_id = ?";
				try (PreparedStatement pstmt = conn.prepareStatement(updateCopiesSql)) {
					pstmt.setInt(1, book.getBookId());
					pstmt.executeUpdate();
				}
			}
			conn.commit(); // 모든 작업 성공적으로 완료
			// JOptionPane.showMessageDialog(this, "선택된 책이 대여되었습니다.");

			loadBookList(); // 이 메소드를 추가하여 bookList를 로드
			loadBooks(null); // 전체 도서 리스트 다시 로드
			showBookDetails(); // 선택한 도서 상세 정보 업데이트
			updateAvailableBooksCount(); // 대여 가능 권수 업데이트

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "대여 중 오류가 발생했습니다.");
		}
	}

	private void returnBook(Book book) {
		try (Connection conn = DatabaseConnection.getConnection()) {
			conn.setAutoCommit(false); // 트랜잭션 시작

			// borrowedBooks에서 사용자 ID와 책 ID로 해당 대여 정보를 조회
			String getBorrowedSql = "SELECT count FROM borrowedBooks WHERE user_id = ? AND book_id = ?";
			int currentCount = 0;
			try (PreparedStatement pstmt = conn.prepareStatement(getBorrowedSql)) {
				pstmt.setInt(1, loginUser.getUserId());
				pstmt.setInt(2, book.getBookId());
				ResultSet rs = pstmt.executeQuery();
				if (rs.next()) {
					currentCount = rs.getInt("count");
				} else {
					JOptionPane.showMessageDialog(this, "이 책은 대여 중이지 않습니다.");
					return;
				}
			}

			// borrowedBooks의 count를 1 감소
			String updateCountSql = "UPDATE borrowedBooks SET count = count - 1 WHERE user_id = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(updateCountSql)) {
				pstmt.setInt(1, loginUser.getUserId());
				pstmt.executeUpdate();
			}

			// 반납한 도서 삭제
			String deleteBorrowedSql = "DELETE FROM borrowedBooks WHERE user_id = ? AND book_id = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(deleteBorrowedSql)) {
				pstmt.setInt(1, loginUser.getUserId());
				pstmt.setInt(2, book.getBookId());
				pstmt.executeUpdate();
			}

			// return_status를 0으로 변경
			String updateStatusSql = "UPDATE borrowedBooks SET return_status = 0 WHERE user_id = ? AND book_id = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(updateStatusSql)) {
				pstmt.setInt(1, loginUser.getUserId());
				pstmt.setInt(2, book.getBookId());
				pstmt.executeUpdate();
			}

			// borrowedbooks의 count 1씩 증가
			String updateCountSql2 = "UPDATE returnedBooks SET count = count + 1 WHERE user_id = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(updateCountSql2)) {
				pstmt.setInt(1, loginUser.getUserId());
				pstmt.executeUpdate();
			}

			int userCount = 0;
			String countSql = "SELECT count FROM returnedBooks WHERE user_id = ?";
			try (PreparedStatement pstmtCount = conn.prepareStatement(countSql)) {
				pstmtCount.setInt(1, loginUser.getUserId());
				ResultSet rsCount = pstmtCount.executeQuery();
				if (rsCount.next()) {
					userCount = rsCount.getInt("count");
					String insertBorrowSql = "INSERT INTO returnedBooks (user_id, copy_id, return_date, count) VALUES (?, ?, TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'), ?)";
					try (PreparedStatement pstmtInsert = conn.prepareStatement(insertBorrowSql)) {
						pstmtInsert.setInt(1, loginUser.getUserId());
						pstmtInsert.setInt(2, book.getBookId());
						// pstmtInsert.setString(3, currentDate);
						pstmtInsert.setInt(3, userCount);
						pstmtInsert.executeUpdate();
					}
				} else {
					// user_id가 존재하지 않는 경우, 새로 삽입
					String insertBorrowSql = "INSERT INTO returnedBooks (user_id, copy_id, return_date, count) VALUES (?, ?, TO_CHAR(CURRENT_TIMESTAMP, 'YYYY-MM-DD HH24:MI:SS'), ?)";
					try (PreparedStatement pstmtInsert = conn.prepareStatement(insertBorrowSql)) {
						pstmtInsert.setInt(1, loginUser.getUserId());
						pstmtInsert.setInt(2, book.getBookId());
						// pstmtInsert.setString(3, currentDate);
						pstmtInsert.setInt(3, 1); // 새로운 대여
						pstmtInsert.executeUpdate();
					}
				}
			}

			// book_db에서 available_copies 증가
			String updateCopiesSql = "UPDATE book_db SET available_copies = available_copies + 1 WHERE book_id = ?";
			try (PreparedStatement pstmt = conn.prepareStatement(updateCopiesSql)) {
				pstmt.setInt(1, book.getBookId());
				pstmt.executeUpdate();
			}

			conn.commit(); // 거래 전부 승인
			JOptionPane.showMessageDialog(this, book.getTitle() + "가 성공적으로 반납되었습니다.");
			loadBookList(); // 책 목록 새로 고침
			loadBooks(null); // 도서 리스트 새로 고침
			showBookDetails(); // 상세 정보 업데이트
			updateAvailableBooksCount(); // 대여 가능 권수 업데이트

		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, "반납 중 오류가 발생했습니다.");
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			new SimpleLibraryApp(new User()).setVisible(true);
		});
	}
}
