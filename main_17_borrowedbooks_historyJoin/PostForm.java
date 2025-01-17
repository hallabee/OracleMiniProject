package main_17_borrowedbooks_historyJoin;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class PostForm extends JFrame {

//	제목 입력 값
	private JTextField titleTextField;
//  카테고리 입력 값
	private JComboBox<String> categoryComboBox;
//  내용 입력 값
	private JTextArea messageTextArea;
	private JButton submitButton;
	private JButton clearButton;

	private static User user = new User(0, "Username", "Password", "TestUser", 0);

	public PostForm(User user) {
		this.user = user;
		setTitle("건의사항");
		setSize(400, 300);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(5, 5, 5, 5);

		titleTextField = new JTextField(20);
		categoryComboBox = new JComboBox<>(new String[] { "도서 문의", "고객신고", "도서 주문요청" });
		messageTextArea = new JTextArea(5, 20);
		messageTextArea.setLineWrap(true);
		messageTextArea.setWrapStyleWord(true);

		submitButton = new JButton("등록");
		clearButton = new JButton("취소");

		gbc.gridx = 0;
		gbc.gridy = 0;
		add(new JLabel("제목:"), gbc);
		gbc.gridx = 1;
		add(titleTextField, gbc);

		gbc.gridx = 0;
		gbc.gridy = 1;
		add(new JLabel("카테고리:"), gbc);
		gbc.gridx = 1;
		add(categoryComboBox, gbc);

		gbc.gridx = 0;
		gbc.gridy = 2;
		add(new JLabel("건의내용:"), gbc);
		gbc.gridx = 1;
		gbc.fill = GridBagConstraints.BOTH;
		add(new JScrollPane(messageTextArea), gbc);

		gbc.gridx = 0;
		gbc.gridy = 3;
		gbc.fill = GridBagConstraints.NONE;
		gbc.gridwidth = 2;
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(submitButton);
		buttonPanel.add(clearButton);
		add(buttonPanel, gbc);

		submitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleSubmit();
			}
		});

		clearButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				handleClear();
			}
			
		});
		
		
		setVisible(true);
	}

	public PostForm() {
		this(user);
	}
	private void handleSubmit(boolean isUnique) {
		String title = titleTextField.getText();
		String category = (String) categoryComboBox.getSelectedItem();
		String message = messageTextArea.getText();
		// 여기에 코드를 추가하여 데이터를 처리할 수 있습니다(예: 파일이나 데이터베이스에 저장).
		try {
			Connection conn = DatabaseConnection.getConnection();
			String sql = "INSERT INTO FEEDBACK (TITLE, CATEGORY, CONTENT, CREATED_AT, USERNAME) VALUES (?, ?, ?, ?, ?)";
			PreparedStatement pstmt = conn.prepareStatement(sql);

//			제목 넣기
			if (titleTextField.getText() == null) {
//				stub
				System.out.println("제목이 없습니다.");
			} else {
				pstmt.setString(1, titleTextField.getText());
			}

//			카테고리 넣기
			if (categoryComboBox.getSelectedItem() == null) {
//				stub
				System.out.println("카테고리가 없습니다.");
			} else {
				pstmt.setString(2, (String) categoryComboBox.getSelectedItem());
			}

//			내용 넣기
			if (messageTextArea.getText() == null) {
				System.out.println("내용이 없습니다.");
			} else {
				pstmt.setString(3, messageTextArea.getText());
			}

//			날짜 넣기
			pstmt.setString(4, Book.getDate(0));

			pstmt.setString(5, user.getUsername(isUnique));

			pstmt.executeUpdate();
		} catch (SQLException e1) {
//			삽입 에러처리
//			문자열 에러인줄 알고 처리한 부분
//			System.out.println(user.getUsername(false));
			System.out.println("보내는 중 오류가 발생했습니다.");
		}

		JOptionPane.showMessageDialog(this,
				"건의사항이 접수되었습니다!\n 건의 제목: " + title + "\n 카테고리: " + category + "\n 건의 내용: " + message);
	}

	// 등록작업 처리
	private void handleSubmit() {
		handleSubmit(true);
	}

	// 취소 작업 처리
	private void handleClear() {
		titleTextField.setText("");
		categoryComboBox.setSelectedIndex(0);
		messageTextArea.setText("");
	}
}
