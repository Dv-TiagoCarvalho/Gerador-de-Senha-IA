import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.plaf.basic.BasicSliderUI;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.font.TextAttribute;
import java.awt.geom.RoundRectangle2D;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Gerador de senhas com interface gráfica (Swing, já vem com o Java),
 * em um visual limpo e minimalista: fundo branco, título grande,
 * muito espaço em branco e botão azul em formato de pílula.
 *
 * Compilar:  javac -encoding UTF-8 GeradorDeSenha.java
 * Executar:  java GeradorDeSenha
 */
public class GeradorDeSenha {

    // ---------------------------------------------------------------
    // Lógica de geração
    // ---------------------------------------------------------------

    private static final String MINUSCULAS = "abcdefghijklmnopqrstuvwxyz";
    private static final String MAIUSCULAS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITOS = "0123456789";
    private static final String SIMBOLOS = "!@#$%&*()-_=+[]{};:,.?";

    // SecureRandom é próprio para segurança; java.util.Random é previsível e não serve para senhas.
    private static final SecureRandom RANDOM = new SecureRandom();

    /** Monta a lista de grupos de caracteres escolhidos. */
    static List<String> grupos(boolean minusculas, boolean maiusculas, boolean digitos, boolean simbolos) {
        List<String> grupos = new ArrayList<>();
        if (minusculas) grupos.add(MINUSCULAS);
        if (maiusculas) grupos.add(MAIUSCULAS);
        if (digitos) grupos.add(DIGITOS);
        if (simbolos) grupos.add(SIMBOLOS);
        return grupos;
    }

    /**
     * Gera uma senha com o tamanho pedido, garantindo pelo menos
     * um caractere de cada grupo escolhido.
     */
    static String gerar(int tamanho, List<String> grupos) {
        if (grupos.isEmpty()) {
            throw new IllegalArgumentException("Escolha pelo menos um tipo de caractere.");
        }
        if (tamanho < grupos.size()) {
            throw new IllegalArgumentException(
                    "O tamanho precisa ser de pelo menos " + grupos.size() + ".");
        }

        char[] senha = new char[tamanho];
        int i = 0;

        // 1) Um caractere de cada grupo, para garantir que todos apareçam.
        for (String grupo : grupos) {
            senha[i++] = sortear(grupo);
        }

        // 2) O resto vem de todos os grupos juntos.
        String todos = String.join("", grupos);
        while (i < tamanho) {
            senha[i++] = sortear(todos);
        }

        // 3) Embaralha (Fisher-Yates), senão o começo seguiria sempre a mesma ordem de grupos.
        for (int k = senha.length - 1; k > 0; k--) {
            int j = RANDOM.nextInt(k + 1);
            char temp = senha[k];
            senha[k] = senha[j];
            senha[j] = temp;
        }

        return new String(senha);
    }

    private static char sortear(String caracteres) {
        return caracteres.charAt(RANDOM.nextInt(caracteres.length()));
    }

    /** Entropia em bits: quanto maior, mais difícil de adivinhar por força bruta. */
    static double entropia(int tamanho, List<String> grupos) {
        int alfabeto = String.join("", grupos).length();
        if (alfabeto == 0) return 0;
        return tamanho * (Math.log(alfabeto) / Math.log(2));
    }

    // ---------------------------------------------------------------
    // Estilo: cores e fontes
    // ---------------------------------------------------------------

    private static final Color FUNDO = Color.WHITE;
    private static final Color TEXTO = new Color(0x1D1D1F);
    private static final Color TEXTO_SECUNDARIO = new Color(0x6E6E73);
    private static final Color CINZA_CLARO = new Color(0xF5F5F7);
    private static final Color BORDA = new Color(0xD2D2D7);
    private static final Color BORDA_HOVER = new Color(0x86868B);
    private static final Color AZUL = new Color(0x0071E3);
    private static final Color AZUL_HOVER = new Color(0x0077ED);
    private static final Color AZUL_PRESSIONADO = new Color(0x0062C4);
    private static final Color AZUL_LINK = new Color(0x0066CC);
    private static final Color VERMELHO = new Color(0xFF3B30);
    private static final Color LARANJA = new Color(0xFF9500);
    private static final Color VERDE = new Color(0x34C759);

    private static final Set<String> FONTES_INSTALADAS = new HashSet<>(Arrays.asList(
            GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames()));

    /** Usa a primeira fonte da lista que existir no computador; senão, a genérica do Java. */
    private static Font escolherFonte(String generica, int estilo, int tamanho, String... preferidas) {
        for (String familia : preferidas) {
            if (FONTES_INSTALADAS.contains(familia)) {
                return new Font(familia, estilo, tamanho);
            }
        }
        return new Font(generica, estilo, tamanho);
    }

    private static Font fonteTexto(int estilo, int tamanho) {
        return escolherFonte(Font.SANS_SERIF, estilo, tamanho,
                "SF Pro Display", "SF Pro Text", "Helvetica Neue", "Segoe UI", "Inter", "Roboto", "Noto Sans");
    }

    private static Font fonteSenha(int tamanho) {
        return escolherFonte(Font.MONOSPACED, Font.PLAIN, tamanho,
                "SF Mono", "Menlo", "Consolas", "DejaVu Sans Mono");
    }

    private static JLabel rotulo(String texto, int estilo, int tamanho, Color cor) {
        JLabel rotulo = new JLabel(texto);
        rotulo.setFont(fonteTexto(estilo, tamanho));
        rotulo.setForeground(cor);
        return rotulo;
    }

    /** Cópia do Graphics com bordas e texto suavizados. */
    private static Graphics2D suave(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        return g2;
    }

    // ---------------------------------------------------------------
    // Interface gráfica
    // ---------------------------------------------------------------

    private final VisorDeSenha visor = new VisorDeSenha();
    private final BarraDeForca barra = new BarraDeForca();
    private final JLabel rotuloForca = rotulo(" ", Font.PLAIN, 13, TEXTO_SECUNDARIO);
    private final JLabel rotuloTamanho = rotulo(" ", Font.PLAIN, 15, TEXTO_SECUNDARIO);
    private final JSlider slider = new JSlider(4, 64, 16);
    private final Opcao optMinusculas = new Opcao("Minúsculas", "a – z");
    private final Opcao optMaiusculas = new Opcao("Maiúsculas", "A – Z");
    private final Opcao optDigitos = new Opcao("Números", "0 – 9");
    private final Opcao optSimbolos = new Opcao("Símbolos", "! @ # $ % & *");
    private final Pilula botaoGerar = new Pilula("Gerar nova", true);
    private final Pilula botaoCopiar = new Pilula("Copiar ›", false);

    private String senhaAtual = "";

    /** Monta todo o conteúdo da janela. */
    JPanel criarPainel() {
        // --- Título ---
        JLabel titulo = rotulo("Gerador de senhas", Font.BOLD, 36, TEXTO);
        // Letras um pouco mais juntas no título, como em títulos de vitrine.
        titulo.setFont(titulo.getFont().deriveFont(Map.of(TextAttribute.TRACKING, -0.02f)));
        JLabel subtitulo = rotulo("Fortes, aleatórias e prontas em um clique.", Font.PLAIN, 17, TEXTO_SECUNDARIO);

        // --- Tamanho ---
        JPanel linhaTamanho = new JPanel(new BorderLayout());
        linhaTamanho.setOpaque(false);
        linhaTamanho.add(rotulo("Tamanho", Font.BOLD, 15, TEXTO), BorderLayout.WEST);
        linhaTamanho.add(rotuloTamanho, BorderLayout.EAST);

        slider.setUI(new SliderLimpo(slider));
        slider.setOpaque(false);
        slider.setBorder(BorderFactory.createEmptyBorder());

        // --- Tipos de caractere ---
        JPanel opcoes = new JPanel(new GridLayout(2, 2, 12, 12));
        opcoes.setOpaque(false);
        Opcao[] todas = {optMinusculas, optMaiusculas, optDigitos, optSimbolos};
        for (Opcao opcao : todas) {
            opcao.setSelected(true);
            opcao.addActionListener(e -> atualizar());
            opcoes.add(opcao);
        }

        // --- Botões ---
        // Reserva a largura do texto mais comprido, para nada "pular" ao trocar para "Copiado".
        botaoCopiar.setText("Copiado");
        Dimension larguraCopiado = botaoCopiar.getPreferredSize();
        botaoCopiar.setText("Copiar ›");
        Dimension larguraCopiar = botaoCopiar.getPreferredSize();
        botaoCopiar.setPreferredSize(new Dimension(
                Math.max(larguraCopiado.width, larguraCopiar.width), larguraCopiar.height));

        JPanel botoes = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        botoes.setOpaque(false);
        botoes.add(botaoGerar);
        botoes.add(botaoCopiar);

        // --- Eventos: qualquer mudança gera uma senha nova ---
        slider.addChangeListener(e -> atualizar());
        botaoGerar.addActionListener(e -> atualizar());
        botaoCopiar.addActionListener(e -> copiar());

        // --- Montagem, de cima para baixo ---
        JPanel painel = new JPanel();
        painel.setLayout(new BoxLayout(painel, BoxLayout.Y_AXIS));
        painel.setBackground(FUNDO);
        painel.setBorder(BorderFactory.createEmptyBorder(48, 48, 44, 48));

        painel.add(centro(titulo));
        painel.add(Box.createVerticalStrut(8));
        painel.add(centro(subtitulo));
        painel.add(Box.createVerticalStrut(32));
        painel.add(centro(visor));
        painel.add(Box.createVerticalStrut(16));
        painel.add(centro(barra));
        painel.add(Box.createVerticalStrut(8));
        painel.add(centro(rotuloForca));
        painel.add(Box.createVerticalStrut(32));
        painel.add(centro(alturaFixa(linhaTamanho)));
        painel.add(Box.createVerticalStrut(6));
        painel.add(centro(alturaFixa(slider)));
        painel.add(Box.createVerticalStrut(24));
        painel.add(centro(alturaFixa(opcoes)));
        painel.add(Box.createVerticalStrut(36));
        painel.add(centro(alturaFixa(botoes)));

        atualizar();
        return painel;
    }

    private static JComponent centro(JComponent c) {
        c.setAlignmentX(JComponent.CENTER_ALIGNMENT);
        return c;
    }

    /** Deixa o componente esticar na largura, mas não na altura. */
    private static JComponent alturaFixa(JComponent c) {
        c.setMaximumSize(new Dimension(Integer.MAX_VALUE, c.getPreferredSize().height));
        return c;
    }

    /** Gera uma senha nova com as opções atuais e atualiza a tela. */
    private void atualizar() {
        int tamanho = slider.getValue();
        rotuloTamanho.setText(tamanho + " caracteres");

        List<String> grupos = grupos(optMinusculas.isSelected(), optMaiusculas.isSelected(),
                optDigitos.isSelected(), optSimbolos.isSelected());

        if (grupos.isEmpty()) {
            senhaAtual = "";
            visor.definir("");
            barra.definir(0, VERMELHO);
            rotuloForca.setText("Escolha pelo menos um tipo de caractere.");
            botaoGerar.setEnabled(false);
            botaoCopiar.setEnabled(false);
            return;
        }

        botaoGerar.setEnabled(true);
        botaoCopiar.setEnabled(true);
        senhaAtual = gerar(tamanho, grupos);
        visor.definir(senhaAtual);

        double bits = entropia(tamanho, grupos);
        String nivel;
        Color cor;
        if (bits < 40) {
            nivel = "Fraca";
            cor = VERMELHO;
        } else if (bits < 60) {
            nivel = "Razoável";
            cor = LARANJA;
        } else if (bits < 80) {
            nivel = "Forte";
            cor = VERDE;
        } else {
            nivel = "Muito forte";
            cor = VERDE;
        }
        barra.definir(Math.min(1.0, bits / 128.0), cor);
        rotuloForca.setText(nivel + "  ·  " + Math.round(bits) + " bits de entropia");
    }

    /** Copia a senha para a área de transferência. */
    private void copiar() {
        Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(senhaAtual), null);

        botaoCopiar.setText("Copiado");
        Timer timer = new Timer(1500, e -> botaoCopiar.setText("Copiar ›"));
        timer.setRepeats(false);
        timer.start();
    }

    // ---------------------------------------------------------------
    // Componentes desenhados à mão
    // ---------------------------------------------------------------

    /** Cartão cinza-claro com a senha no centro; a letra diminui se a senha for comprida. */
    @SuppressWarnings("serial")
    private static class VisorDeSenha extends JComponent {
        private String texto = "";

        VisorDeSenha() {
            setPreferredSize(new Dimension(464, 84));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        }

        void definir(String texto) {
            this.texto = texto;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            g2.setColor(CINZA_CLARO);
            g2.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 36, 36));

            int larguraUtil = getWidth() - 48;
            int tamanhoFonte = 24;
            g2.setFont(fonteSenha(tamanhoFonte));
            while (tamanhoFonte > 10 && g2.getFontMetrics().stringWidth(texto) > larguraUtil) {
                tamanhoFonte--;
                g2.setFont(fonteSenha(tamanhoFonte));
            }

            FontMetrics fm = g2.getFontMetrics();
            int x = (getWidth() - fm.stringWidth(texto)) / 2;
            int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(TEXTO);
            g2.drawString(texto, x, y);
            g2.dispose();
        }
    }

    /** Barrinha fina e colorida que mostra a força da senha. */
    @SuppressWarnings("serial")
    private static class BarraDeForca extends JComponent {
        private double fracao;
        private Color cor = VERMELHO;

        BarraDeForca() {
            Dimension tamanho = new Dimension(180, 4);
            setPreferredSize(tamanho);
            setMaximumSize(tamanho);
        }

        void definir(double fracao, Color cor) {
            this.fracao = fracao;
            this.cor = cor;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int w = getWidth();
            int h = getHeight();
            g2.setColor(BORDA);
            g2.fillRoundRect(0, 0, w, h, h, h);
            g2.setColor(cor);
            g2.fillRoundRect(0, 0, (int) Math.round(w * fracao), h, h, h);
            g2.dispose();
        }
    }

    /**
     * Botão em dois estilos: preenchido (pílula azul com texto branco)
     * ou link (só o texto azul, sublinhado ao passar o mouse).
     */
    @SuppressWarnings("serial")
    private static class Pilula extends JButton {
        private final boolean preenchido;

        Pilula(String texto, boolean preenchido) {
            super(texto);
            this.preenchido = preenchido;
            setFont(fonteTexto(Font.PLAIN, 17));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        public Dimension getPreferredSize() {
            if (isPreferredSizeSet()) return super.getPreferredSize();
            int larguraTexto = getFontMetrics(getFont()).stringWidth(getText());
            return new Dimension(larguraTexto + (preenchido ? 56 : 8), 46);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            int w = getWidth();
            int h = getHeight();
            boolean ativo = isEnabled();
            boolean mouseEmCima = ativo && getModel().isRollover();
            boolean pressionado = ativo && getModel().isArmed();

            Color corTexto;
            if (preenchido) {
                Color corFundo = pressionado ? AZUL_PRESSIONADO : mouseEmCima ? AZUL_HOVER : AZUL;
                if (!ativo) corFundo = new Color(AZUL.getRed(), AZUL.getGreen(), AZUL.getBlue(), 90);
                g2.setColor(corFundo);
                g2.fill(new RoundRectangle2D.Float(0, 0, w, h, h, h));
                corTexto = Color.WHITE;
            } else {
                corTexto = ativo ? AZUL_LINK : BORDA_HOVER;
            }

            g2.setFont(getFont());
            FontMetrics fm = g2.getFontMetrics();
            int x = (w - fm.stringWidth(getText())) / 2;
            int y = (h - fm.getHeight()) / 2 + fm.getAscent();
            g2.setColor(corTexto);
            g2.drawString(getText(), x, y);
            if (!preenchido && mouseEmCima) {
                g2.drawLine(x, y + 2, x + fm.stringWidth(getText()), y + 2);
            }
            g2.dispose();
        }
    }

    /** Cartão de opção liga/desliga: borda azul quando ligado, cinza quando desligado. */
    @SuppressWarnings("serial")
    private static class Opcao extends JToggleButton {
        private final String titulo;
        private final String detalhe;

        Opcao(String titulo, String detalhe) {
            this.titulo = titulo;
            this.detalhe = detalhe;
            setPreferredSize(new Dimension(226, 68));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = suave(g);
            boolean ligado = isSelected();
            float espessura = ligado ? 2f : 1f;
            float folga = espessura / 2f;

            RoundRectangle2D.Float forma = new RoundRectangle2D.Float(
                    folga, folga, getWidth() - espessura, getHeight() - espessura, 24, 24);
            g2.setColor(FUNDO);
            g2.fill(forma);
            g2.setColor(ligado ? AZUL : getModel().isRollover() ? BORDA_HOVER : BORDA);
            g2.setStroke(new BasicStroke(espessura));
            g2.draw(forma);

            g2.setFont(fonteTexto(Font.BOLD, 15));
            g2.setColor(ligado ? TEXTO : TEXTO_SECUNDARIO);
            g2.drawString(titulo, 18, 29);

            g2.setFont(fonteTexto(Font.PLAIN, 12));
            g2.setColor(TEXTO_SECUNDARIO);
            g2.drawString(detalhe, 18, 49);
            g2.dispose();
        }
    }

    /** Controle deslizante: trilho fino, parte preenchida em azul e botão redondo branco. */
    private static class SliderLimpo extends BasicSliderUI {

        SliderLimpo(JSlider slider) {
            super(slider);
        }

        @Override
        protected Dimension getThumbSize() {
            return new Dimension(26, 26);
        }

        @Override
        public void paintFocus(Graphics g) {
            // sem contorno de foco
        }

        @Override
        public void paintTrack(Graphics g) {
            Graphics2D g2 = suave(g);
            int altura = 4;
            int y = trackRect.y + (trackRect.height - altura) / 2;
            int meioDoBotao = thumbRect.x + thumbRect.width / 2;
            // O trilho vai de ponta a ponta, alinhado com o resto da tela.
            int folga = thumbRect.width / 2;
            int inicio = trackRect.x - folga;
            g2.setColor(BORDA);
            g2.fillRoundRect(inicio, y, trackRect.width + 2 * folga, altura, altura, altura);
            g2.setColor(AZUL);
            g2.fillRoundRect(inicio, y, meioDoBotao - inicio, altura, altura, altura);
            g2.dispose();
        }

        @Override
        public void paintThumb(Graphics g) {
            Graphics2D g2 = suave(g);
            int d = 22;
            int x = thumbRect.x + (thumbRect.width - d) / 2;
            int y = thumbRect.y + (thumbRect.height - d) / 2;
            g2.setColor(new Color(0, 0, 0, 28)); // sombra suave
            g2.fillOval(x, y + 2, d, d);
            g2.setColor(Color.WHITE);
            g2.fillOval(x, y, d, d);
            g2.setColor(new Color(0, 0, 0, 45));
            g2.drawOval(x, y, d - 1, d - 1);
            g2.dispose();
        }

        /** Clicar no trilho leva o botão direto para o ponto clicado. */
        @Override
        protected void scrollDueToClickInTrack(int direcao) {
            Point mouse = slider.getMousePosition();
            if (mouse != null) {
                slider.setValue(valueForXPosition(mouse.x));
            }
        }
    }

    public static void main(String[] args) {
        // Texto suavizado nos rótulos (precisa vir antes de qualquer coisa de tela).
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");

        // Tudo que mexe na tela precisa rodar na thread de eventos do Swing.
        SwingUtilities.invokeLater(() -> {
            JFrame janela = new JFrame("Gerador de senhas");
            janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            janela.setContentPane(new GeradorDeSenha().criarPainel());
            janela.pack();
            janela.setResizable(false);
            janela.setLocationRelativeTo(null); // centraliza na tela
            janela.setVisible(true);
        });
    }
}
