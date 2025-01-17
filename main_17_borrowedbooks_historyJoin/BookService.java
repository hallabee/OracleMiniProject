package main_17_borrowedbooks_historyJoin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;


public class BookService {
//	삭제하지 않는 상태
//	public boolean returnBook(int bookId, int copiesToReturn) {
//	    String getBorrowedCopiesQuery = "SELECT count, return_status FROM borrowedbooks WHERE book_id = ?";
//	    String updateBorrowedBooksQuery = "UPDATE borrowedbooks SET count = count - ?, return_status = ? WHERE book_id = ? AND return_status = 0";
//	    
//	    try (Connection conn = DatabaseConnection.getConnection();
//	         PreparedStatement getBorrowedStmt = conn.prepareStatement(getBorrowedCopiesQuery);
//	         PreparedStatement updateBorrowedStmt = conn.prepareStatement(updateBorrowedBooksQuery)) {
//
//	        // 1. 현재 빌린 책의 개수와 반납 상태 조회
//	        getBorrowedStmt.setInt(1, bookId);
//	        ResultSet rs = getBorrowedStmt.executeQuery();
//
//	        if (rs.next()) {
//	            int borrowedCopies = rs.getInt("count");
//	            int returnStatus = rs.getInt("return_status");
//
//	            // 2. 반환할 수 있는지 확인 (반환할 수량이 빌린 수량을 초과하지 않는지 확인)
//	            if (copiesToReturn <= borrowedCopies && returnStatus == 0) {
//	                // 3. 대출 기록 업데이트: 반납된 책 개수 차감 및 반납 상태 업데이트
//	                updateBorrowedStmt.setInt(1, copiesToReturn);
//	                updateBorrowedStmt.setInt(2, copiesToReturn == borrowedCopies ? 1 : 0); // 만약 모든 개수가 반환되면 반납 완료로 표시
//	                updateBorrowedStmt.setInt(3, bookId);
//	                
//	                int affectedRows = updateBorrowedStmt.executeUpdate();
//	                return affectedRows > 0; // 성공적으로 업데이트되면 true 반환
//	            }
//	        }
//	    } catch (SQLException e) {
//	        e.printStackTrace();
//	    }
//	    return false; // 실패하거나 데이터가 잘못된 경우 false 반환
//	}
//    // 책을 반납하는 함수, 실패 시 롤백
//    public boolean returnBook(int bookId, int copiesToReturn, String tableName) {
//        String checkQuery = "SELECT available_copies, total_copies FROM " + tableName + " WHERE book_id = ?";
//        String updateQuery = "UPDATE " + tableName
//                + " SET available_copies = available_copies + ? WHERE book_id = ? AND available_copies + ? <= total_copies";
//
//        try (Connection conn = DatabaseConnection.getConnection()) {
//            conn.setAutoCommit(false); // 수동 커밋 모드로 설정
//
//            // Check the current available and total copies
//            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
//                checkStmt.setInt(1, bookId);
//                ResultSet rs = checkStmt.executeQuery();
//
//                if (rs.next()) {
//                    int availableCopies = rs.getInt("available_copies");
//                    int totalCopies = rs.getInt("total_copies");
//
//                    // Check if returning copies will not exceed total copies
//                    if (availableCopies + copiesToReturn <= totalCopies) {
//                        // Update available copies if the total does not exceed maximum
//                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
//                            updateStmt.setInt(1, copiesToReturn);
//                            updateStmt.setInt(2, bookId);
//                            updateStmt.setInt(3, copiesToReturn);
//                            int affectedRows = updateStmt.executeUpdate();
//
//                            if (affectedRows > 0) {
//                                conn.commit(); // 성공적으로 업데이트되면 커밋
//                                return true; // 성공
//                            } else {
//                                conn.rollback(); // 업데이트 실패 시 롤백
//                                return false; // 반납 실패
//                            }
//                        }
//                    } else {
//                        conn.rollback(); // 조건 불충족 시 롤백
//                        return false; // 반납 실패
//                    }
//                } else {
//                    conn.rollback(); // 책 ID가 존재하지 않으면 롤백
//                    return false; // 책이 존재하지 않음
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false; // 예외 발생 시 실패 반환
//        }
//    }
//	 // 유저가 빌릴 수 있는가를 판단
//    public boolean deductBorrowable(int userId, int amountToDeduct) {
//        // 커넥션을 얻고, 트랜잭션을 시작합니다
//        try (Connection conn = DatabaseConnection.getConnection()) {
//            conn.setAutoCommit(false); // 수동 커밋 모드로 설정
//
//            // 1. 현재 borrowable 값을 가져오는 쿼리
//            String getQuery = "SELECT borrow_able FROM users WHERE user_id = ?";
//            try (PreparedStatement getStmt = conn.prepareStatement(getQuery)) {
//                getStmt.setInt(1, userId);
//                ResultSet rs = getStmt.executeQuery();
//
//                if (!rs.next()) {
//                    conn.rollback(); // 사용자 ID가 없으면 롤백
//                    return false; // 사용자 ID가 유효하지 않음
//                }
//
//                int currentBorrowable = rs.getInt("borrow_able");
//
//                // 2. borrowable 값이 충분한지 확인
//                if (currentBorrowable < amountToDeduct) {
//                    conn.rollback(); // borrowable 값이 부족하면 롤백
//                    return false; // 차감할 수 없음
//                }
//
//                // 3. borrowable 값을 차감하는 쿼리
//                String updateQuery = "UPDATE users SET borrow_able = borrow_able - ? WHERE user_id = ?";
//                try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
//                    updateStmt.setInt(1, amountToDeduct);
//                    updateStmt.setInt(2, userId);
//                    int affectedRows = updateStmt.executeUpdate();
//
//                    if (affectedRows > 0) {
//                        conn.commit(); // 성공적으로 업데이트되면 커밋
//                        return true; // 차감 성공
//                    } else {
//                        conn.rollback(); // 업데이트 실패 시 롤백
//                        return false; // 차감 실패
//                    }
//                }
//            }
//        } catch (SQLException e) {
//            System.out.println("차감 실패");
////            e.printStackTrace();
//            return false; // 예외 발생 시 실패 반환
//        }
//    }
//
//    public boolean borrowBook(int userId, int bookId, int copiesToBorrow, String tableName) {
//        String checkQuery = "SELECT available_copies FROM " + tableName + " WHERE book_id = ?";
//        String updateQuery = "UPDATE " + tableName
//                + " SET available_copies = available_copies - ? WHERE book_id = ? AND available_copies >= ?";
//
//        try (Connection conn = DatabaseConnection.getConnection();
//             PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
//             PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
//
//            // 트랜잭션 시작
//            conn.setAutoCommit(false);
//
//            // 현재 이용 가능한 책 수량을 확인
//            checkStmt.setInt(1, bookId);
//            ResultSet rs = checkStmt.executeQuery();
//
//            if (rs.next()) {
//                int availableCopies = rs.getInt("available_copies");
//
//                // `deductBorrowable` 함수 호출하여 추가 조건 검사
//                if (availableCopies >= copiesToBorrow && deductBorrowable(userId, copiesToBorrow)) {
//                    // 충분한 수량이 있으면 수량을 업데이트
//                    updateStmt.setInt(1, copiesToBorrow);
//                    updateStmt.setInt(2, bookId);
//                    updateStmt.setInt(3, copiesToBorrow);
//                    int affectedRows = updateStmt.executeUpdate();
//
//                    if (affectedRows > 0) {
//                        conn.commit(); // 성공적으로 업데이트되면 커밋
//                        return true; // 성공
//                    } else {
//                        conn.rollback(); // 업데이트 실패 시 롤백
//                    }
//                } else {
//                    conn.rollback(); // 차감 실패 시 롤백
//                }
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//        return false; // 실패
//    }

//	리스트로 바꾸는 부분
	public ArrayList<Book> getAllBooks(String table_name) {
		ArrayList<Book> books = new ArrayList<>();
		String query = "SELECT * FROM " + table_name;

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query);
				ResultSet rs = pstmt.executeQuery()) {

			while (rs.next()) {
				int bookId = rs.getInt("book_id");
				String author = rs.getString("author");
				int availableCopies = rs.getInt("available_copies");
				String category = rs.getString("category");
				int returnDate = rs.getInt("return_date");
				String title = rs.getString("title");
				int totalCopies = rs.getInt("total_copies");

				Book book = new Book(bookId, author, availableCopies, category, returnDate, title, totalCopies);
				books.add(book);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}

		return books;
	}

	public ArrayList<Book> getAllBooks() {
		return getAllBooks("book_db");
	}

	public boolean borrowBook(Book targetBook, String table_name) {

//		겹치지 않을 경우
		if (checkDuplicate(targetBook, table_name)) {
			ArrayList<Book> allList = getAllBooks(table_name);
			for (Book book : allList) {
				if (book.getBookId() == targetBook.getBookId()) {
					if (book.getAvailableCopies() > 0) {
						
					}
					return true;
				}
			}
		}

		return false;
	}

//	겹치는 경우 true 겹치지 않는 경우 false
	public boolean checkDuplicate(Book targetBook, String table_name) {
		ArrayList<Book> allList = getAllBooks(table_name);
		for (Book book : allList) {
			if (book.getBookId() == targetBook.getBookId()) {
				System.out.println("중복 ID로 책 삽입 불가");
				return true;
			}
		}
		return false;
	}

	// 책을 데이터베이스에 삽입하는 메서드
	public void addBook(Book book, String table_name) {
		String insertSql = "INSERT INTO " + table_name
				+ " (book_id, author, available_copies, category, return_date, title, total_copies) VALUES (?, ?, ?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

			// 중복검사 추가
			int add_bookcount = 0;
			if (checkDuplicate(book, table_name)) {
				System.out.println("책 코드가 중복되었습니다. 개수만 추가하시겠습니까?");
				String input = inputDialogShow();
				add_bookcount = Integer.parseInt(input);
			}

			pstmt.setInt(1, book.getBookId());
			pstmt.setString(2, book.getAuthor());
			pstmt.setInt(3, book.getAvailableCopies() + add_bookcount);
			pstmt.setString(4, book.getCategory());
			pstmt.setInt(5, book.getReturnDate());
			pstmt.setString(6, book.getTitle());
			pstmt.setInt(7, book.getTotalCopies() + add_bookcount);

			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void addBook(Book book) {
		addBook(book, "books");
	}

	// 책을 데이터베이스에서 삭제하는 메서드
	public void deleteBook(int bookId) {
		String deleteSql = "DELETE FROM Books WHERE book_id = ?";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(deleteSql)) {

			pstmt.setInt(1, bookId);
			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public void deleteBook(Book book) {
		deleteBook(book.getBookId());
	}

	private static String inputDialogShow() {
		String result = "";
		int response = JOptionPane.showConfirmDialog(null, "경고: 책 코드가 중복되었습니다.개수만 추가하시겠습니까?", "확인",
				JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
		if (response == JOptionPane.YES_OPTION) {
			result = JOptionPane.showInputDialog(null, "추가할 개수를 입력하세요:", "책 개수 추가", JOptionPane.QUESTION_MESSAGE);
		}
		return result;
	}
}