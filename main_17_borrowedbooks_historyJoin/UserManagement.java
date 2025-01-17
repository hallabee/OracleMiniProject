package main_17_borrowedbooks_historyJoin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

//통합 연결 구조로 변경
public class UserManagement {
	private JFrame frame;
	private JTextField book_id_Field, title_Field, author_Field, category_Field, return_date_Field, total_copies_Field;
	private JTextArea outputArea;
//	private static final String DATABASE_URL = "jdbc:oracle:thin:@localhost:1521:orcl";
//	private static final String DATABASE_USER = "SCOTT";
//	private static final String DATABASE_PASSWORD = "1234";

	public UserManagement() {
		initializeUI();
	}

	private void initializeUI() {
		frame = new JFrame("User Management");
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				int option = JOptionPane.showConfirmDialog(frame, "삽입을 종료하시겠습니까?", "종료 확인", JOptionPane.YES_NO_OPTION);
				if(option == JOptionPane.YES_OPTION) {
//					자기 프레임만 종료
					frame.dispose();	
				}
			}
		});
		
		
		frame.setSize(800, 400);
		frame.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.insets = new Insets(5, 5, 5, 5);
		JPanel inputPanel = createInputPanel();
		gbc.gridx = 0;
		gbc.gridy = 0;
		frame.add(inputPanel, gbc);
		outputArea = createOutputArea();
		gbc.gridx = 1;
		gbc.gridy = 0;
		gbc.gridheight = 7;
		frame.add(new JScrollPane(outputArea), gbc);
		frame.setVisible(true);
	}

	private JPanel createInputPanel() {
		JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
		panel.setBorder(BorderFactory.createTitledBorder("정보"));
		// 입력 필드들
		book_id_Field = addField(panel, "책 코드:");
		title_Field = addField(panel, "제목:");
		author_Field = addField(panel, "저자:");
		category_Field = addField(panel, "카테고리:");
		return_date_Field = addField(panel, "반납기간:");
		total_copies_Field = addField(panel, "총 갯수:");
		// 버튼들 추가
		addActionButton(panel, "정보 가져오기", this::showSearchInputDialog);
		addActionButton(panel, "정보 생성", this::generateInformation);
		return panel;
	}

	private JTextField addField(JPanel panel, String label) {
		panel.add(new JLabel(label));
		JTextField textField = new JTextField();
		textField.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));
		panel.add(textField);
		return textField;
	}

	private void addActionButton(JPanel panel, String buttonText, Runnable action) {
		JButton button = new JButton(buttonText);
		button.setBackground(new Color(70, 130, 180)); // 버튼 배경색
		button.setForeground(Color.WHITE); // 버튼 글자색
		button.setBorderPainted(false);
		button.setFocusPainted(false);
		button.addActionListener(e -> action.run());
		panel.add(button);
	}

	private JTextArea createOutputArea() {
		JTextArea textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		return textArea;
	}

	private void showSearchInputDialog() {
		JPanel panel = new JPanel();
		JTextField searchField = new JTextField(20);
		panel.add(new JLabel("책 이름:"));
		panel.add(searchField);
		int result = JOptionPane.showConfirmDialog(frame, panel, "정보 검색", JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			fetchDataFromDatabase(searchField.getText());
		}
	}

	private void fetchDataFromDatabase(String name) {
		try (Connection connection = DatabaseConnection.getConnection()) {
			String sql = "SELECT * FROM books WHERE book_id = ?";
			PreparedStatement statement = connection.prepareStatement(sql);
			statement.setString(1, name);
			ResultSet resultSet = statement.executeQuery();
			StringBuilder result = new StringBuilder();
			while (resultSet.next()) {
				Book b = new Book(resultSet.getInt("book_id"), resultSet.getString("author"),
						resultSet.getInt("available_copies"), resultSet.getString("category"),
						resultSet.getInt("return_date"), resultSet.getString("title"),
						resultSet.getInt("total_copies"));
				result.append(String.format("책 코드: %s\n제목: %s\n저자: %s\n카테고리: %s\n총 갯수: %s\n반납기간: %s\n\n", b.getBookId(),
						b.getTitle(), b.getAuthor(), b.getCategory(), b.getTotalCopies(), b.getDate()));
			}
			if (result.length() == 0) {
				result.append("해당 이름의 데이터를 찾을 수 없습니다.");
			}
			outputArea.setText(result.toString());
		} catch (Exception e) {
			outputArea.setText("데이터 검색 중 오류가 발생했습니다: " + e.getMessage());
		}
	}

	private void generateInformation() {
		int book_id = Integer.parseInt(book_id_Field.getText());
		String title = title_Field.getText();
		String author = author_Field.getText();
		String category = category_Field.getText();
		int total_copies = Integer.parseInt(total_copies_Field.getText());
		int return_date = Integer.parseInt(return_date_Field.getText());
		String result = String.format("책 코드: %s | 제목: %s | 저자: %s | 카테고리: %s | 총 갯수: %s | 반납기간: %s", book_id, title,
				author, category, total_copies, return_date);
		outputArea.setText(result);
		Book b = new Book(book_id, author, total_copies, category, return_date, title, total_copies);
		new BookService().addBook(b);
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(UserManagement::new);
	}
}