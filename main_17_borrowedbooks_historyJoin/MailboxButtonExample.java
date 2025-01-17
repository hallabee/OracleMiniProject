package main_17_borrowedbooks_historyJoin;

import javax.swing.*;
import javax.swing.border.Border;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

public class MailboxButtonExample {

    private static MailBoxFrame mailboxFrame; // 우편함 창을 관리하는 변수
    private static Image createPlaceholderImage(int width, int height) {
        // BufferedImage로 흰색 배경의 이미지를 생성
        BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = placeholder.createGraphics();
        g2d.setColor(Color.WHITE); // 배경색을 흰색으로 설정
        g2d.fillRect(0, 0, width, height); // 이미지 전체를 흰색으로 채움
        g2d.dispose(); // 그래픽 리소스 해제
        return placeholder;
    }
    
    public static JLabel createMailboxLabel(String imagePath, int width, int height, User user) {
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

        // 클릭 효과 및 이벤트 처리
        imageLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                // 클릭 시 배경색을 변경하여 눌린 효과를 줌
                imageLabel.setBackground(Color.BLACK);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                // 클릭 해제 시 원래 배경색으로 복원
                imageLabel.setBackground(Color.WHITE);
                openMailboxWindow(user);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // 마우스가 올라갔을 때 경계선을 추가하여 hover 효과를 줌
                imageLabel.setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                // 마우스가 벗어났을 때 경계선을 원래대로 돌림
                imageLabel.setBorder(defaultBorder);
            }
        });

        // 생성된 JLabel 반환
        return imageLabel;
    }

    // 우편함 창을 여는 메서드 (하나만 열리도록 제한)
    private static void openMailboxWindow(User user) {
        if (mailboxFrame == null || !mailboxFrame.isVisible()) {
            mailboxFrame = new MailBoxFrame(user);
            mailboxFrame.setVisible(true);
            // 창이 닫힐 때 변수 초기화
            mailboxFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                @Override
                public void windowClosing(java.awt.event.WindowEvent e) {
                    mailboxFrame = null;
                }
            });
        } else {
            mailboxFrame.toFront(); // 이미 열려 있는 경우 해당 창을 최상단으로
        }
    }
}
