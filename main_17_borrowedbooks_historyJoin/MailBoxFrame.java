package main_17_borrowedbooks_historyJoin;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class MailBoxFrame extends JFrame {

	private JTextArea contentArea; // 내용 표시를 위한 텍스트 영역

	public MailBoxFrame(User user) {
		setTitle("우편함");
		setSize(900, 600);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);

		// 메인 패널 생성
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());

		// 사용자 정보 패널
		JPanel userInfoPanel = new JPanel();
		userInfoPanel.setLayout(new GridLayout(1, 2));

		// 피드백 목록 테이블 모델 생성
		DefaultTableModel feedbackModel = new DefaultTableModel();
		feedbackModel.addColumn("번호");
		feedbackModel.addColumn("제목");
		feedbackModel.addColumn("카테고리");
		feedbackModel.addColumn("생성일시");
		feedbackModel.addColumn("사용자 이름"); // USERNAME 열 추가

		// 데이터베이스 연결 및 데이터 가져오기
		try {

			Connection connection = DatabaseConnection.getConnection();

			// 사용자 정보 가져오기
			String userInfoSql = "SELECT user_id, username, role FROM users WHERE user_id = ?";
			PreparedStatement userInfoStmt = connection.prepareStatement(userInfoSql);
			userInfoStmt.setInt(1, user.getUserId()); // userId를 int로 변환
			ResultSet userInfoResultSet = userInfoStmt.executeQuery();

			// 사용자 정보 처리
			if (userInfoResultSet.next()) {
				String username = userInfoResultSet.getString("username");
				userInfoPanel.add(new JLabel("사용자: " + user.getUsername(false)));
			}

			userInfoResultSet.close();
			userInfoStmt.close();

			// 피드백 목록 가져오기
			String feedbackSql = "SELECT FEEDBACK_ID, TITLE, CATEGORY, CONTENT, CREATED_AT, USERNAME FROM FEEDBACK";
//          추가 조건 설정 특정 유저만 가져오도록 where 추가
			feedbackSql += " WHERE USERNAME = '" + user.getUsername(true) + "'";

			PreparedStatement feedbackStmt = connection.prepareStatement(feedbackSql);
			ResultSet feedbackResultSet = feedbackStmt.executeQuery();

			while (feedbackResultSet.next()) {
				int feedbackId = feedbackResultSet.getInt("FEEDBACK_ID");
				String title = feedbackResultSet.getString("TITLE");
				String category = feedbackResultSet.getString("CATEGORY");
				String createdAt = feedbackResultSet.getString("CREATED_AT");
				String username = feedbackResultSet.getString("USERNAME"); // USERNAME 가져오기
				feedbackModel.addRow(new Object[] { feedbackId, title, category, createdAt, username // USERNAME 추가
				});
			}

			feedbackResultSet.close();
			feedbackStmt.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
		}

		// 피드백 목록 JTable 생성
		JTable feedbackTable = new JTable(feedbackModel);
		JScrollPane feedbackScrollPane = new JScrollPane(feedbackTable);

		// 각 열의 너비 설정
		feedbackTable.getColumnModel().getColumn(0).setPreferredWidth(50); // 번호 열 크기 작게
		feedbackTable.getColumnModel().getColumn(3).setPreferredWidth(200); // 생성일시 열 크기 크게

		feedbackTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent event) {
				if (!event.getValueIsAdjusting()) {
					int selectedRow = feedbackTable.getSelectedRow();
					if (selectedRow != -1) {
						int feedbackId = (int) feedbackModel.getValueAt(selectedRow, 0);
						loadFeedbackContent(feedbackId);
					}
				}
			}
		});

		// 내용 표시 패널 생성
		JPanel contentPanel = new JPanel();
		contentPanel.setLayout(new BorderLayout());
		contentArea = new JTextArea();
		contentArea.setEditable(false);
		contentPanel.add(new JScrollPane(contentArea), BorderLayout.CENTER);

		// 패널에 추가
		mainPanel.add(userInfoPanel, BorderLayout.NORTH);
		mainPanel.add(feedbackScrollPane, BorderLayout.WEST);
		mainPanel.add(contentPanel, BorderLayout.CENTER); // 내용 패널 추가

		// 프레임에 패널 추가
		add(mainPanel);
	}

	// 피드백 내용을 로드하는 메소드
	private void loadFeedbackContent(int feedbackId) {
		try {
			Connection connection = DatabaseConnection.getConnection();
			String contentSql = "SELECT CONTENT FROM FEEDBACK WHERE FEEDBACK_ID = ?";
			PreparedStatement contentStmt = connection.prepareStatement(contentSql);
			contentStmt.setInt(1, feedbackId);
			ResultSet contentResultSet = contentStmt.executeQuery();

			if (contentResultSet.next()) {
				String content = contentResultSet.getString("CONTENT");
				contentArea.setText(content); // 내용 표시
			} else {
				contentArea.setText("내용이 없습니다.");
			}

			contentResultSet.close();
			contentStmt.close();
			connection.close();

		} catch (SQLException e) {
			e.printStackTrace();
			contentArea.setText("내용 로드에 실패했습니다.");
		}
	}

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> {
//            MailBoxFrame frame = new MailBoxFrame(); // 실제 user_id로 교체
//            frame.setVisible(true);
		});
	}
}
