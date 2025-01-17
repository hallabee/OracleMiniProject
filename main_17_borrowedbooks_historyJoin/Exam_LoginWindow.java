package main_17_borrowedbooks_historyJoin;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

// 회원가입/탈퇴 클래스
class UserService {

	// User 객체를 데이터베이스에 삽입하는 메서드
	public static void registerUser(User user) {
		String query = "INSERT INTO users (user_id, username, password, role, borrow_able, nickname) VALUES (?, ?, ?, ?, ?, ?)";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setInt(1, user.getUserId());
			pstmt.setString(2, user.getUsername(true));
			pstmt.setString(3, user.getPassword());
			pstmt.setString(4, user.getRole());
			pstmt.setInt(5, 5);
			pstmt.setString(6, user.getUsername(true));

			pstmt.executeUpdate();

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	public User getUserDetails(String username, String password) throws SQLException {
		User user = new User();
		String query = "SELECT user_id, username, password, role, borrow_able, nickname FROM users WHERE username = ? AND password = ?";
		Connection conn = DatabaseConnection.getConnection();
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(1, username);
		pstmt.setString(2, password);

		ResultSet resultSet = pstmt.executeQuery();
		if (resultSet.next()) {
			int user_id = resultSet.getInt("user_id");
			System.out.println(user_id);
			String username_r = resultSet.getString("username");
			System.out.println(username_r);
			String password_r = resultSet.getString("password");
			System.out.println(password_r);
			String role = resultSet.getString("role");
			System.out.println(role);
			int borrow_able = resultSet.getInt("borrow_able");
			System.out.println(borrow_able);
			String nickname = resultSet.getString("nickname");
			System.out.println(nickname);
			user = new User(user_id, username_r, password_r, role, borrow_able, nickname);
		}

		return user;
	}

	public User getUserDetails(String user_id, String username, String password, String role) throws SQLException {
		User user = new User();
		String query = "SELECT user_id, username, password, role, borrow_able FROM users WHERE user_id = ? AND username = ? AND password = ? AND role = ?";
		Connection conn = DatabaseConnection.getConnection();
		PreparedStatement pstmt = conn.prepareStatement(query);
		pstmt.setString(1, user_id);
		pstmt.setString(2, username);
		pstmt.setString(3, password);
		pstmt.setString(4, role);

		ResultSet resultSet = pstmt.executeQuery();
		if (resultSet.next()) {
			int user_id_r = resultSet.getInt("user_id");
			String username_r = resultSet.getString("username");
			String password_r = resultSet.getString("password");
			String role_r = resultSet.getString("role");
			int borrow_able = resultSet.getInt("borrow_able");
			String nickname = resultSet.getString("nickname");
			user = new User(user_id_r, username_r, password_r, role_r, borrow_able, nickname);
		}

		return user;
	}
}

// 유저 정보를 담는 클래스
class User {
	private int userId;
	private String username;
	private String password;
	private String role;
	private int borrow_able;
	private String nickname = null;

	public User(int userId, String username, String password, String role, int borrow_able, String nickname) {
		this.userId = userId;
		this.username = username;
		this.password = password;
		this.role = role;
		this.borrow_able = borrow_able;
		this.nickname = nickname;
	}

//	계정 생성 시 호출
	public User(int userId, String username, String password, String role, int borrow_able) {
		this(userId, username, password, role, borrow_able, username);
	}

	public User(String username, String password) {
		this(0, username, password, "", 0, username);
	}

	public User() {
		this("", "");
	}

	public int getUserId() {
		return userId;
	}

	public String getUsername(boolean isUnique) {
		if (isUnique) {
			return username;
		}
		return nickname;
	}

	@Deprecated
	public String getUsername() {
		return getUsername(true);
	}

	public String getPassword() {
		return password;
	}

	public String getRole() {
		return role;
	}
}

class ReturnService {
	public void addReturnRecord(int userId, int copyId, int bookId) {
		String returnSql = "INSERT INTO ReturnedBooks (user_id, copy_id, return_date) VALUES (?, ?, ?)";
		String updateCopySql = "UPDATE BookCopies SET available = TRUE WHERE copy_id = ?";
		String updateBookSql = "UPDATE Books SET available_copies = available_copies + 1 WHERE book_id = ?";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement returnStmt = conn.prepareStatement(returnSql);
				PreparedStatement updateCopyStmt = conn.prepareStatement(updateCopySql);
				PreparedStatement updateBookStmt = conn.prepareStatement(updateBookSql)) {

			conn.setAutoCommit(false); // 트랜잭션 시작

			returnStmt.setInt(1, userId);
			returnStmt.setInt(2, copyId);
			returnStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			returnStmt.executeUpdate();

			updateCopyStmt.setInt(1, copyId);
			updateCopyStmt.executeUpdate();

			updateBookStmt.setInt(1, bookId);
			updateBookStmt.executeUpdate();

			conn.commit(); // 트랜잭션 커밋

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

class BorrowService {
	public void addBorrowRecord(int userId, int copyId, int bookId) {
		String borrowSql = "INSERT INTO BorrowedBooks (user_id, copy_id, borrow_date) VALUES (?, ?, ?)";
		String updateCopySql = "UPDATE BookCopies SET available = FALSE WHERE copy_id = ?";
		String updateBookSql = "UPDATE Books SET available_copies = available_copies - 1 WHERE book_id = ?";

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement borrowStmt = conn.prepareStatement(borrowSql);
				PreparedStatement updateCopyStmt = conn.prepareStatement(updateCopySql);
				PreparedStatement updateBookStmt = conn.prepareStatement(updateBookSql)) {

			conn.setAutoCommit(false); // 트랜잭션 시작

			borrowStmt.setInt(1, userId);
			borrowStmt.setInt(2, copyId);
			borrowStmt.setTimestamp(3, new Timestamp(System.currentTimeMillis()));
			borrowStmt.executeUpdate();

			updateCopyStmt.setInt(1, copyId);
			updateCopyStmt.executeUpdate();

			updateBookStmt.setInt(1, bookId);
			updateBookStmt.executeUpdate();

			conn.commit(); // 트랜잭션 커밋

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}

class DatabaseConnection {
	private static final String URL = "jdbc:oracle:thin:@localhost:1521:orcl";
	private static final String USER = "SCOTT";
	private static final String PASSWORD = "1234";

	public static Connection getConnection() throws SQLException {
		return DriverManager.getConnection(URL, USER, PASSWORD);
	}
}

class AdminFrameEdit {
	public JTabbedPane tabbedPane; // JTabbedPane을 클래스 변수로 선언

	// 데이터베이스에서 데이터를 로드하여 JTable에 표시하는 메서드
	public void loadTableData(JTable table, String tableName) {
		String query = "SELECT * FROM " + tableName;

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {

			// 결과 메타데이터를 얻어서 열 이름을 가져옵니다
			int columnCount = rs.getMetaData().getColumnCount();
			String[] columnNames = new String[columnCount];
			for (int i = 1; i <= columnCount; i++) {
				columnNames[i - 1] = rs.getMetaData().getColumnName(i);
			}

			// 데이터 모델 생성 및 설정
			DefaultTableModel model = new DefaultTableModel(columnNames, 0);
			while (rs.next()) {
				Object[] row = new Object[columnCount];
				for (int i = 1; i <= columnCount; i++) {
					row[i - 1] = rs.getObject(i);
				}
				model.addRow(row);
			}
			table.setModel(model); // 테이블 모델을 설정

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 탭이 있는 프레임을 생성하는 메서드
	public void createTabbedFrame() {
		JFrame frame = new JFrame("Tabbed Table Frame");
		frame.setTitle("관리자 창");
		frame.setSize(800, 600);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {

			}
		});

		frame.setLayout(new BorderLayout());

		// JTabbedPane 생성
		tabbedPane = new JTabbedPane();

		// 새로고침 버튼 생성
		JButton refreshButton = new JButton("새로고침");
		refreshButton.addActionListener(e -> {
			int selectedIndex = tabbedPane.getSelectedIndex();
			if (selectedIndex != -1) {
				String tableName = tabbedPane.getTitleAt(selectedIndex);
				refreshTab(selectedIndex, tableName);
			}
		});

//      삽입 버튼 생성
		JButton insertButton = new JButton("삽입");
		insertButton.addActionListener(e -> {
			int selectedIndex = tabbedPane.getSelectedIndex();
			if (selectedIndex != -1) {
				String tableName = tabbedPane.getTitleAt(selectedIndex);
				SwingUtilities.invokeLater(UserManagement::new);
				refreshTab(selectedIndex, tableName);
			}
		});

		// 버튼과 탭 패널을 프레임에 추가
		frame.add(refreshButton, BorderLayout.NORTH);
		frame.add(insertButton, BorderLayout.SOUTH);
		// 탭이 있는 패널을 프레임에 추가
		frame.add(tabbedPane, BorderLayout.CENTER);

		// 프레임을 화면에 표시
		frame.setVisible(true);

		// 프레임 포커스 이벤트 추가
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowActivated(WindowEvent e) {
				int selectedIndex = tabbedPane.getSelectedIndex();
				if (selectedIndex != -1) {
					String tableName = tabbedPane.getTitleAt(selectedIndex);
					refreshTab(selectedIndex, tableName);
				}
			}
		});
	}

	// 특정 탭을 새로고침하는 메서드
	public void refreshTab(int index, String tableName) {
		// 탭의 패널을 가져옵니다
		JPanel panel = (JPanel) tabbedPane.getComponentAt(index);
		// 패널에서 JTable을 찾습니다
		JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();

		// 데이터 새로 고침
		loadTableData(table, tableName);
	}

	// 새로운 탭을 추가하는 메서드
	public void addTab(String tableName) {
		JPanel panel = new JPanel(new BorderLayout());
		JTable table = new JTable();
		panel.add(new JScrollPane(table), BorderLayout.CENTER);
		loadTableData(table, tableName); // 데이터 로드
		tabbedPane.addTab(tableName, panel); // 탭 추가
	}
}

public class Exam_LoginWindow {

	private static User user;

	// UI 컴포넌트 필드
	private JFrame frame;
	private JTextField usernameField;
	private JPasswordField passwordField;
	private JButton loginButton;
	private JLabel statusLabel;
	private JButton registerButton;

	private void showTable(String tableName) {
		// 새로운 프레임 생성
		JFrame tableFrame = new JFrame("테이블 데이터");
		
		
		tableFrame.setSize(800, 600); // 테이블 창의 크기
		tableFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		tableFrame.setLayout(new BorderLayout());

		// 테이블 패널 생성 및 설정
		JPanel tablePanel = new JPanel(new BorderLayout());
		JTable table = new JTable(); // 새로운 JTable 컴포넌트 생성
		tablePanel.add(new JScrollPane(table), BorderLayout.CENTER); // 스크롤 가능하게 설정

		tableFrame.add(tablePanel, BorderLayout.CENTER); // 테이블 패널을 프레임에 추가

		// 테이블 데이터 로드
		loadTableData(table, tableName);

		// 테이블 프레임을 화면에 표시
		tableFrame.setVisible(true);
	}

	// 테이블의 데이터를 로드하여 표시하는 메서드
	private void loadTableData(JTable table, String tableName) {
		String query = "SELECT * FROM " + tableName;

		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(query);
				ResultSet rs = stmt.executeQuery()) {

			// 결과 메타데이터를 얻어서 열 이름을 가져옵니다
			int columnCount = rs.getMetaData().getColumnCount();
			String[] columnNames = new String[columnCount];
			for (int i = 1; i <= columnCount; i++) {
				columnNames[i - 1] = rs.getMetaData().getColumnName(i);
			}

			// 데이터 모델 생성 및 설정
			DefaultTableModel model = new DefaultTableModel(columnNames, 0);
			while (rs.next()) {
				Object[] row = new Object[columnCount];
				for (int i = 1; i <= columnCount; i++) {
					row[i - 1] = rs.getObject(i);
				}
				model.addRow(row);
			}
			table.setModel(model); // 테이블 모델을 설정

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	// 데이터베이스에서 아이디와 비밀번호 확인 및 역할 반환
	private String authenticate(String userId, String password) {
		String query = "SELECT role FROM users WHERE username = ? AND password = ?";
		String result = null;
		try (Connection conn = DatabaseConnection.getConnection();
				PreparedStatement pstmt = conn.prepareStatement(query)) {

			pstmt.setString(1, userId);
			pstmt.setString(2, password);
			try (ResultSet rs = pstmt.executeQuery()) {
				if (rs.next()) {
					result = rs.getString("role"); // role 값을 반환
				}
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		user = new User(0, userId, password, result, 0);
		return result; // 인증 실패 시 null 반환
	}

	public class ImagePanel extends JPanel {
		private BufferedImage backgroundImage;

		public ImagePanel(String imagePath) {
			try {
				// 이미지 파일 읽기
				backgroundImage = ImageIO.read(new File(imagePath));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			if (backgroundImage != null) {
				// 배경 이미지 그리기
				g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
			}
		}
	}

	public void initialize() {
		frame = new JFrame("로그인");
		frame.setSize(800, 600); // 프레임 크기 두 배로 증가
		frame.setResizable(false); // 프레임 크기 조정 불가능
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setLayout(new BorderLayout());
		frame.setLocationRelativeTo(null);

		// 중앙 패널 생성 및 설정
//         JPanel centerPanel = new ImagePanel(null);

		JPanel centerPanel = new JPanel();

		centerPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(10, 10, 10, 10); // 여백 설정

		// UI 컴포넌트 초기화
		JLabel usernameLabel = new JLabel("아이디:");
		usernameField = new JTextField(20);
		JLabel passwordLabel = new JLabel("비밀번호:");
		passwordField = new JPasswordField(20);
		
		loginButton = new JButton("로그인");
		registerButton = new JButton("회원가입");
		statusLabel = new JLabel("상태: ");

		
		// 컴포넌트 배치
		gbc.gridx = 0;
		gbc.gridy = 0;
		centerPanel.add(usernameLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 0;
		centerPanel.add(usernameField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		centerPanel.add(passwordLabel, gbc);

		gbc.gridx = 1;
		gbc.gridy = 1;
		centerPanel.add(passwordField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.gridwidth = 2; // 로그인 버튼의 세로 크기를 두 필드의 합으로 설정
		gbc.anchor = gbc.CENTER;
//		gbc.insets = new Insets(10, 10, 10, 10); // 오른쪽 여백 조정
		centerPanel.add(loginButton, gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
//		gbc.gridwidth = 2; // 회원가입 버튼을 전체 너비에 맞게 조정
//		gbc.insets = new Insets(10, 10, 10, 10); // 전체 여백 설정
		centerPanel.add(registerButton, gbc);

		gbc.gridx = 0;
		gbc.gridy = 4;
//		gbc.gridwidth = 3; // 상태 레이블을 전체 너비에 맞게 조정
		centerPanel.add(statusLabel, gbc);

		// 프레임에 중앙 패널 추가
		frame.add(centerPanel, BorderLayout.CENTER);

		// 로그인 버튼 이벤트 처리
		loginButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String userId = usernameField.getText();
				String password = new String(passwordField.getPassword());

				String role = authenticate(userId, password);
				if (role != null) {
					statusLabel.setText("상태: 로그인 성공");
					if (role.equals("관리자")) {
						adminFrame();
						frame.setVisible(false);
					} else {
						try {
							User session = new UserService().getUserDetails(userId, password);
							userFrame(session);
						} catch (SQLException e1) {
							e1.printStackTrace();
						}
						frame.setVisible(false);
					}
				} else {
					statusLabel.setText("상태: 로그인 실패");
				}
			}
		});
		// 회원가입 버튼 이벤트 처리
		registerButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				openRegisterFrame(); // 회원가입 창을 여는 메서드 호출
			}

			private void openRegisterFrame() {
				JFrame registerFrame = new JFrame("회원가입");
				registerFrame.setSize(800, 600);
				registerFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				registerFrame.setLayout(new BorderLayout());

				// 회원가입 패널 생성 및 설정
				JPanel registerPanel = new JPanel();
				registerPanel.setLayout(new GridLayout(6, 2));

				// 회원가입 UI 컴포넌트 초기화
				JLabel registerUsernameLabel = new JLabel("아이디:");
				JTextField registerUsernameField = new JTextField(20);
				JLabel registerPasswordLabel = new JLabel("비밀번호:");
				JPasswordField registerPasswordField = new JPasswordField(20);
				JLabel registerConfirmPasswordLabel = new JLabel("비밀번호 확인:");
				JPasswordField registerConfirmPasswordField = new JPasswordField(20);
				JButton registerSubmitButton = new JButton("회원가입");
				JLabel registerStatusLabel = new JLabel("상태: ");

				// 회원가입 패널에 컴포넌트 추가
				registerPanel.add(registerUsernameLabel);
				registerPanel.add(registerUsernameField);
				registerPanel.add(registerPasswordLabel);
				registerPanel.add(registerPasswordField);
				registerPanel.add(registerConfirmPasswordLabel);
				registerPanel.add(registerConfirmPasswordField);
				registerPanel.add(new JLabel("")); // 빈 레이블로 위치 조정
				registerPanel.add(registerSubmitButton);
				registerPanel.add(registerStatusLabel);

				// 회원가입 프레임에 패널 추가
				registerFrame.add(registerPanel, BorderLayout.CENTER);

				// 회원가입 버튼 이벤트 처리
				registerSubmitButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						String newUserId = registerUsernameField.getText();
						String newPassword = new String(registerPasswordField.getPassword());
						String confirmPassword = new String(registerConfirmPasswordField.getPassword());

						if (!newPassword.equals(confirmPassword)) {
							registerStatusLabel.setText("상태: 비밀번호가 일치하지 않습니다.");
					 		return;
						}

						boolean success = registerUser(newUserId, newPassword);
						if (success) {
							registerStatusLabel.setText("상태: 회원가입 성공");
						} else {
							registerStatusLabel.setText("상태: 회원가입 실패");
						}
					}

					// 사용자 등록 메서드
					private boolean registerUser(String userId, String password) {
						String query = "INSERT INTO users (username, password, role, borrow_able) VALUES (?, ?, '사용자', ?)";
						try (Connection conn = DatabaseConnection.getConnection();
								PreparedStatement pstmt = conn.prepareStatement(query)) {

							pstmt.setString(1, userId);
							pstmt.setString(2, password);
							pstmt.setInt(3, 5);
							int affectedRows = pstmt.executeUpdate();
							return affectedRows > 0;
						} catch (SQLException e) {
//							e.printStackTrace();
							System.out.println("회원가입 실패" + "registerUser()");
							return false;
						}
					}
				});

				// 회원가입 프레임을 화면에 표시
				registerFrame.setVisible(true);
			}
		});
		// 프레임을 화면에 표시
		frame.setVisible(true);
	}

	// 메인 메서드
	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
			Exam_LoginWindow loginWindow = new Exam_LoginWindow();
			loginWindow.initialize();
		});
	}

	private void userFrame(User login_User) {
		SwingUtilities.invokeLater(() -> {
			try {
				new SimpleLibraryApp(login_User).setVisible(true);
			} catch (HeadlessException e) {
				e.printStackTrace();
			}
		});
	}

	private static void adminFrame() {
		AdminFrameEdit example = new AdminFrameEdit();
		example.createTabbedFrame();

		example.addTab("users");
		example.addTab("feedback");
		example.addTab("book_db");
		example.addTab("borrowedbooks");
		example.addTab("borrowedbooks_history");
		example.addTab("returnedbooks");
//		PostForm exam = new PostForm(user);
	}
}