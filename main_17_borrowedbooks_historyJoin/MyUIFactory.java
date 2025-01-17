package main_17_borrowedbooks_historyJoin;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

public class MyUIFactory {
//	이미지 라벨 생성
    private static Image createPlaceholderImage(int width, int height) {
        // BufferedImage로 흰색 배경의 이미지를 생성
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.WHITE); // 배경색을 흰색으로 설정
        g2d.fillRect(0, 0, width, height); // 이미지 전체를 흰색으로 채움
        g2d.dispose(); // 그래픽 리소스 해제
        return placeholder;
    }
	public static JLabel createImageLabel(String imagePath, int width, int height) {
		// 이미지 로드 및 스케일링
		Image img = new ImageIcon(imagePath).getImage();
		if(img == null || img.getWidth(null) == -1) {
        	img = createPlaceholderImage(width, height);
        }
		Image scaledImg = img.getScaledInstance(width, height, Image.SCALE_SMOOTH);
		ImageIcon scaledIcon = new ImageIcon(scaledImg);

		// JLabel 생성 및 설정
		JLabel imageLabel = new JLabel(scaledIcon);
		imageLabel.setOpaque(true); // 배경색을 설정하기 위한 설정
		imageLabel.setBackground(Color.WHITE); // 배경색 설정

		// JLabel의 크기를 고정하기 위해 크기 설정
		imageLabel.setPreferredSize(new Dimension(width, height));
		imageLabel.setMinimumSize(new Dimension(width, height));
		imageLabel.setMaximumSize(new Dimension(width, height));

		// 기본 빈 여백을 추가하여 크기 변화 방지
		Border defaultBorder = BorderFactory.createEmptyBorder(2, 2, 2, 2);
		imageLabel.setBorder(defaultBorder);

		// 생성된 JLabel 반환
		return imageLabel;
	}

	public static JTextField createHintTextField(String hint, int columns) {
		JTextField textField = new JTextField(columns) {
			private boolean showingHint = true;

			@Override
			public void setText(String text) {
				if (text.isEmpty() && showingHint) {
					super.setText(hint);
					setForeground(Color.GRAY);
				} else {
					super.setText(text);
					setForeground(Color.BLACK);
				}
			}
		};

		textField.setForeground(Color.GRAY);
		textField.setText(hint);
		textField.setPreferredSize(new Dimension(200, 30));

		textField.addFocusListener(new FocusAdapter() {
			boolean isVisibleHint = true;

			@Override
			public void focusGained(FocusEvent e) {
				if (isVisibleHint) {
					textField.setText("");
					textField.setForeground(Color.BLACK);
					isVisibleHint = false;
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (textField.getText().isEmpty() && !isVisibleHint) {
					textField.setText(hint);
					textField.setForeground(Color.GRAY);
					isVisibleHint = true;
				}
			}
		});

		return textField;
	}

	public static JPasswordField createHintPasswordField(String hint, int columns) {
		JPasswordField passwordField = new JPasswordField(columns) {
			private boolean showingHint = true;

			@Override
			public void setText(String text) {
				if (text.isEmpty() && showingHint) {
					super.setText(hint);
					setEchoChar((char) 0); // Hide characters
					setForeground(Color.GRAY);
				} else {
					super.setText(text);
					setEchoChar('*'); // Show asterisks
					setForeground(Color.BLACK);
				}
			}
		};

		passwordField.setForeground(Color.GRAY);
		passwordField.setText(hint);
		passwordField.setPreferredSize(new Dimension(200, 30));

		passwordField.addFocusListener(new FocusAdapter() {
			boolean isVisibleHint = true;

			@Override
			public void focusGained(FocusEvent e) {
				if (isVisibleHint) {
					passwordField.setText("");
					passwordField.setEchoChar('*'); // Show asterisks
					passwordField.setForeground(Color.BLACK);
					isVisibleHint = false;
				}
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (new String(passwordField.getPassword()).isEmpty() && !isVisibleHint) {
					passwordField.setText(hint);
					passwordField.setEchoChar((char) 0); // Hide characters
					passwordField.setForeground(Color.GRAY);
					isVisibleHint = true;
				}
			}
		});

		return passwordField;
	}
}
