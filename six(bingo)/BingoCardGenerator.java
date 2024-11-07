import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class BingoCardGenerator extends JFrame {
    private static final int GRID_SIZE = 5;
    private RoundButton[][] buttons = new RoundButton[GRID_SIZE][GRID_SIZE];
    private Set<String> generatedCards = new HashSet<>();
    private boolean[] rowReachNotified = new boolean[GRID_SIZE];
    private boolean[] colReachNotified = new boolean[GRID_SIZE];
    private boolean diag1ReachNotified = false;
    private boolean diag2ReachNotified = false;

    public BingoCardGenerator() {
        setTitle("ビンゴカード生成システム");
        setSize(600, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(34, 45, 65)); // 背景色をダークブルーに設定

        JPanel cardPanel = new JPanel(new GridLayout(GRID_SIZE, GRID_SIZE, 10, 10));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30)); // カードの周りに広めの余白を追加
        cardPanel.setBackground(new Color(34, 45, 65)); // カードパネルの背景色もダークブルーに設定
        add(cardPanel, BorderLayout.CENTER);

        JPanel controlPanel = new JPanel();
        controlPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // 下部のボタン周りに余白を追加
        controlPanel.setBackground(new Color(34, 45, 65));
        JButton resetButton = new JButton("reset");
        resetButton.setFont(new Font("Arial", Font.BOLD, 16));
        resetButton.setBackground(new Color(255, 140, 0)); // オレンジ色の背景
        resetButton.setForeground(Color.WHITE);
        resetButton.addActionListener(e -> {
            cardPanel.removeAll();
            resetReachStatus();
            generateNewCard(cardPanel);
            cardPanel.revalidate();
            cardPanel.repaint();
        });
        controlPanel.add(resetButton);
        add(controlPanel, BorderLayout.SOUTH);

        generateNewCard(cardPanel);
    }

    private void resetReachStatus() {
        for (int i = 0; i < GRID_SIZE; i++) {
            rowReachNotified[i] = false;
            colReachNotified[i] = false;
        }
        diag1ReachNotified = false;
        diag2ReachNotified = false;
    }

    private void generateNewCard(JPanel cardPanel) {
        int[][] card = new int[GRID_SIZE][GRID_SIZE];
        Random random = new Random();

        for (int col = 0; col < GRID_SIZE; col++) {
            int min = col * 15 + 1;
            int max = min + 14;
            Set<Integer> numbersInColumn = new HashSet<>();

            for (int row = 0; row < GRID_SIZE; row++) {
                if (col == 2 && row == 2) {
                    card[row][col] = 0; // 中央セルは「Free」
                    continue;
                }

                int number;
                do {
                    number = random.nextInt(max - min + 1) + min;
                } while (numbersInColumn.contains(number));

                numbersInColumn.add(number);
                card[row][col] = number;
            }
        }

        String cardHash = getCardHash(card);
        while (generatedCards.contains(cardHash)) {
            generateNewCard(cardPanel);
            return;
        }
        generatedCards.add(cardHash);

        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                buttons[i][j] = new RoundButton(card[i][j] == 0 ? "Free" : String.valueOf(card[i][j]));
                buttons[i][j].setFont(new Font("Arial", Font.BOLD, 18));
                buttons[i][j].setBackground(new Color(255, 255, 255)); // 白色の背景
                buttons[i][j].setForeground(new Color(34, 45, 65)); // ダークブルーの文字色
                buttons[i][j].setFocusPainted(false);
                buttons[i][j].addActionListener(new ButtonListener());
                cardPanel.add(buttons[i][j]);
            }
        }
    }

    private String getCardHash(int[][] card) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < GRID_SIZE; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                builder.append(card[i][j]).append(",");
            }
        }

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(builder.toString().getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                hexString.append(String.format("%02x", b));
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private class ButtonListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            RoundButton button = (RoundButton) e.getSource();
            button.setEnabled(false);
            button.setBackground(new Color(211, 211, 211)); // 押されたボタンを灰色に
            checkForBingoOrReach();
        }
    }

    private void checkForBingoOrReach() {
        boolean bingo = false;

        for (int i = 0; i < GRID_SIZE; i++) {
            if (checkRow(i)) {
                bingo = true;
                break;
            }
            if (checkColumn(i)) {
                bingo = true;
                break;
            }
        }

        if (checkDiagonal(1) || checkDiagonal(2)) {
            bingo = true;
        }

        if (bingo) {
            JOptionPane.showMessageDialog(this, "ビンゴ！");
        }
    }

    private boolean checkRow(int row) {
        int disabledCount = 0;
        for (int j = 0; j < GRID_SIZE; j++) {
            if (!buttons[row][j].isEnabled()) disabledCount++;
        }
        if (disabledCount == GRID_SIZE) return true;
        if (disabledCount == GRID_SIZE - 1 && !rowReachNotified[row]) {
            rowReachNotified[row] = true;
            markReachRow(row);
            JOptionPane.showMessageDialog(this, "リーチ！");
        }
        return false;
    }

    private boolean checkColumn(int col) {
        int disabledCount = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            if (!buttons[i][col].isEnabled()) disabledCount++;
        }
        if (disabledCount == GRID_SIZE) return true;
        if (disabledCount == GRID_SIZE - 1 && !colReachNotified[col]) {
            colReachNotified[col] = true;
            markReachColumn(col);
            JOptionPane.showMessageDialog(this, "リーチ！");
        }
        return false;
    }

    private boolean checkDiagonal(int diag) {
        int disabledCount = 0;
        for (int i = 0; i < GRID_SIZE; i++) {
            int j = (diag == 1) ? i : GRID_SIZE - i - 1;
            if (!buttons[i][j].isEnabled()) disabledCount++;
        }
        if (disabledCount == GRID_SIZE) return true;
        if (disabledCount == GRID_SIZE - 1) {
            if (diag == 1 && !diag1ReachNotified) {
                diag1ReachNotified = true;
                markReachDiagonal(1);
                JOptionPane.showMessageDialog(this, "リーチ！");
            } else if (diag == 2 && !diag2ReachNotified) {
                diag2ReachNotified = true;
                markReachDiagonal(2);
                JOptionPane.showMessageDialog(this, "リーチ！");
            }
        }
        return false;
    }

    private void markReachRow(int row) {
        for (int j = 0; j < GRID_SIZE; j++) {
            buttons[row][j].setBorder(BorderFactory.createLineBorder(buttons[row][j].isEnabled() ? new Color(255, 165, 0) : new Color(211, 211, 211), 4));
        }
    }

    private void markReachColumn(int col) {
        for (int i = 0; i < GRID_SIZE; i++) {
            buttons[i][col].setBorder(BorderFactory.createLineBorder(buttons[i][col].isEnabled() ? new Color(255, 165, 0) : new Color(211, 211, 211), 4));
        }
    }

    private void markReachDiagonal(int diag) {
        for (int i = 0; i < GRID_SIZE; i++) {
            int j = (diag == 1) ? i : GRID_SIZE - i - 1;
            buttons[i][j].setBorder(BorderFactory.createLineBorder(buttons[i][j].isEnabled() ? new Color(255, 165, 0) : new Color(211, 211, 211), 4));
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BingoCardGenerator generator = new BingoCardGenerator();
            generator.setVisible(true);
        });
    }
}
