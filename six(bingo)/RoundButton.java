import javax.swing.*;
import java.awt.*;

public class RoundButton extends JButton {
    private boolean isReach = false;

    public RoundButton(String label) {
        super(label);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }

    // リーチのときに余白に色をつけるためのフラグを設定
    public void setReach(boolean reach) {
        this.isReach = reach;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();

        // アンチエイリアスを有効にして滑らかな描画
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // ボタンの影を描画
        if (getModel().isPressed()) {
            g2.setColor(new Color(170, 170, 170)); // 押されたときの影の色
            g2.fillOval(4, 4, getWidth() - 8, getHeight() - 8);
        } else {
            g2.setColor(new Color(200, 200, 200, 100)); // 通常時の影の色
            g2.fillOval(4, 4, getWidth() - 8, getHeight() - 8);
        }

        // リーチ状態なら余白に色を付ける
        if (isReach) {
            g2.setColor(new Color(255, 165, 0, 150)); // オレンジ色の半透明
            g2.fillOval(0, 0, getWidth(), getHeight());
        }

        // ボタンの円を描画
        g2.setColor(getModel().isPressed() ? Color.LIGHT_GRAY : getBackground());
        g2.fillOval(6, 6, getWidth() - 12, getHeight() - 12);

        // ボタンのテキストを描画
        g2.setColor(getForeground());
        FontMetrics fm = g2.getFontMetrics();
        int textX = (getWidth() - fm.stringWidth(getText())) / 2;
        int textY = (getHeight() + fm.getAscent()) / 2 - 2;
        g2.drawString(getText(), textX, textY);

        g2.dispose();
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(getForeground());
        g.drawOval(6, 6, getWidth() - 12, getHeight() - 12);
    }

    @Override
    public boolean contains(int x, int y) {
        int radius = Math.min(getWidth(), getHeight()) / 2;
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        return (Math.pow(x - centerX, 2) + Math.pow(y - centerY, 2)) <= Math.pow(radius, 2);
    }
}
