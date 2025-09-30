package newpackage;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
public class FinanceManagerApp extends JFrame {
    static class FinanceItem {
        String sourceOrPurpose;
        double amount;
        String datetime; 
        String note;
        FinanceItem(String sourceOrPurpose, double amount, String datetime, String note) {
            this.sourceOrPurpose = sourceOrPurpose;
            this.amount = amount;
            this.datetime = datetime;
            this.note = note;
        }
    }
    private final List<FinanceItem> incomeList = new ArrayList<>();
    private final List<FinanceItem> expenseList = new ArrayList<>();
    private final DefaultTableModel incomeModel = new DefaultTableModel(
            new String[]{"No", "Source/Purpose", "Amount", "Date/Time", "Note"}, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final DefaultTableModel expenseModel = new DefaultTableModel(
            new String[]{"No", "Source/Purpose", "Amount", "Date/Time", "Note"}, 0) {
        public boolean isCellEditable(int r, int c) {
            return false;
        }
    };
    private final JLabel totalIncomeLabel = new JLabel("Total Income: 0");
    private final JLabel totalExpenseLabel = new JLabel("Total Expense: 0");
    private final JLabel dominanceLabel = new JLabel("Dominance: —");
    private final JProgressBar shareBar = new JProgressBar(0, 100);
    private static final Color BG_DARK = new Color(255, 255, 255);   
    private static final Color CARD = new Color(245, 247, 250);   
    private static final Color ACCENT1 = new Color(0x4CAF50);        
    private static final Color ACCENT2 = new Color(0xFF9800);       
    private static final Color ACCENT3 = new Color(0x2196F3);        
    private static final Color TEXT = new Color(33, 33, 33);      
    private static final Color SUBTEXT = new Color(97, 97, 97);     
    private static final Color TITLE_COLOR = new Color(0xFFFF33);     
    private static final Font TITLE_FONT = new Font("Segoe UI", Font.BOLD, 22);
    private static final Font SECTION_FONT = new Font("Segoe UI", Font.BOLD, 16);
    private static final DateTimeFormatter DTF = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DecimalFormat MONEY = new DecimalFormat("#,##0.##");
    public FinanceManagerApp() {
        super("Personal Finance Manager (Income & Expense)");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1100, 720);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_DARK);
        JPanel titleBar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(
                        0, 0, new Color(255, 255, 255), 
                        getWidth(), getHeight(), new Color(245, 247, 250)
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        titleBar.setBorder(new EmptyBorder(16, 20, 16, 20));
        JLabel title = new JLabel("💸 Personal Finance Manager");
        title.setForeground(TEXT);
        title.setFont(TITLE_FONT);
        JLabel hint = new JLabel("Colorful UI • Dialog-based CRUD • CSV export • Analysis");
        hint.setForeground(SUBTEXT);
        JPanel left = new JPanel(new GridLayout(2, 1));
        left.setOpaque(false);
        left.add(title);
        left.add(hint);
        JLabel badge = makePill("LIVE", ACCENT2);
        titleBar.add(left, BorderLayout.WEST);
        titleBar.add(badge, BorderLayout.EAST);
        add(titleBar, BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        styleTabs(tabs);
        tabs.addTab("Income", makeCategoryPanel(true));
        tabs.addTab("Expense", makeCategoryPanel(false));
        tabs.addTab("Analysis", makeAnalysisPanel());
        add(tabs, BorderLayout.CENTER);
        refreshTables();
        refreshAnalysis();
    }
    private JLabel makePill(String text, Color bg) {
        JLabel pill = new JLabel(text);
        pill.setOpaque(true);
        pill.setBackground(bg);
        pill.setForeground(Color.black);
        pill.setBorder(new EmptyBorder(6, 14, 6, 14));
        pill.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pill.setHorizontalAlignment(SwingConstants.CENTER);
        pill.setPreferredSize(new Dimension(80, 28));
        return pill;
    }
    private void styleTabs(JTabbedPane tabs) {
        tabs.setBackground(BG_DARK);
        tabs.setForeground(TEXT);
        tabs.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TabbedPane.selected", CARD);
    }
    private JPanel makeCategoryPanel(boolean income) {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(16, 20, 20, 20));
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(CARD);
        header.setBorder(new EmptyBorder(12, 16, 12, 16));
        JLabel label = new JLabel(income ? "Income List" : "Expense List");
        label.setFont(SECTION_FONT);
        label.setForeground(TEXT);
        JLabel deco = makePill(income ? "INCOME" : "EXPENSE", income ? ACCENT1 : ACCENT3);
        header.add(label, BorderLayout.WEST);
        header.add(deco, BorderLayout.EAST);
        JToolBar bar = new JToolBar();
        bar.setFloatable(false);
        bar.setOpaque(false);
        bar.setBackground(CARD);
        bar.setBorder(new EmptyBorder(10, 0, 0, 0));
        bar.add(makeButton("Add", e -> showAddDialog(income)));
        bar.add(makeButton("Update", e -> showUpdateDialog(income)));
        bar.add(makeButton("Find", e -> showFindDialog(income)));
        bar.add(makeButton("Delete", e -> showDeleteDialog(income)));
        bar.addSeparator(new Dimension(16, 0));
        bar.add(makeButton("Export CSV", e -> exportCSV()));
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(CARD);
        top.add(header, BorderLayout.NORTH);
        top.add(bar, BorderLayout.CENTER);
        JTable table = new JTable(income ? incomeModel : expenseModel);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.setBackground(Color.WHITE);               
        table.setForeground(new Color(33, 33, 33));
        table.setGridColor(new Color(230, 233, 238));    
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setBackground(new Color(245, 247, 250));
        table.getTableHeader().setForeground(new Color(33, 33, 33));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 13));
        JScrollPane sp = new JScrollPane(table);
        sp.getViewport().setBackground(Color.WHITE);
        sp.setBorder(BorderFactory.createLineBorder(new Color(230, 233, 238)));
        root.add(top, BorderLayout.NORTH);
        root.add(sp, BorderLayout.CENTER);
        return root;
    }
    private JButton makeButton(String text, ActionListener action) {
        JButton b = new JButton(text);
        b.addActionListener(action);
        b.setFocusPainted(false);
        b.setFont(new Font("Segoe UI", Font.BOLD, 13));
        return b;
    }
    private JPanel makeAnalysisPanel() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG_DARK);
        root.setBorder(new EmptyBorder(20, 24, 24, 24));
        JPanel card = new JPanel();
        card.setBackground(CARD);
        card.setLayout(new GridBagLayout());
        card.setBorder(new EmptyBorder(24, 24, 24, 24));
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.gridy = 0;
        gc.anchor = GridBagConstraints.WEST;
        gc.insets = new Insets(6, 6, 6, 6);
        JLabel header = new JLabel("Financial Analysis");
        header.setFont(SECTION_FONT);
        header.setForeground(TEXT);
        totalIncomeLabel.setForeground(new Color(0x8AF7FF));
        totalIncomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totalExpenseLabel.setForeground(new Color(0xFFC38A));
        totalExpenseLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        dominanceLabel.setForeground(new Color(0xC3C9D5));
        dominanceLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        shareBar.setStringPainted(true);
        shareBar.setBackground(new Color(44, 48, 64));
        shareBar.setForeground(ACCENT1);
        JButton recompute = new JButton("Recompute");
        recompute.setBackground(ACCENT2);
        recompute.setForeground(Color.black);
        recompute.setFocusPainted(false);
        recompute.addActionListener(e -> refreshAnalysis());
        gc.gridwidth = 2;
        card.add(header, gc);
        gc.gridy++;
        gc.gridwidth = 1;
        card.add(totalIncomeLabel, gc);
        gc.gridx = 1;
        card.add(totalExpenseLabel, gc);
        gc.gridx = 0;
        gc.gridy++;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.weightx = 1;
        card.add(shareBar, gc);
        gc.gridy++;
        gc.gridwidth = 2;
        gc.fill = GridBagConstraints.NONE;
        gc.weightx = 0;
        card.add(dominanceLabel, gc);
        gc.gridy++;
        card.add(recompute, gc);
        root.add(card, BorderLayout.NORTH);
        return root;
    }
    private void showAddDialog(boolean income) {
        JPanel form = makeFormPanel(null);
        int ok = JOptionPane.showConfirmDialog(this, form,
                (income ? "Add Income" : "Add Expense"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok == JOptionPane.OK_OPTION) {
            FinanceItem item = readForm(form);
            if (item == null) {
                return;
            }
            if (income) {
                incomeList.add(item);
            } else {
                expenseList.add(item);
            }
            refreshTables();
            refreshAnalysis();
        }
    }
    private void showFindDialog(boolean income) {
        Integer no = askNo((income ? "Find Income by No" : "Find Expense by No"));
        if (no == null) {
            return;
        }
        FinanceItem item = getByNo(income, no);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Not found.", "Result", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        String msg = "No: " + no
                + "\nSource/Purpose: " + item.sourceOrPurpose
                + "\nAmount: " + MONEY.format(item.amount)
                + "\nDate/Time: " + item.datetime
                + "\nNote: " + item.note;
        JOptionPane.showMessageDialog(this, msg, "Detail", JOptionPane.INFORMATION_MESSAGE);
    }
    private void showUpdateDialog(boolean income) {
        Integer no = askNo((income ? "Update Income by No" : "Update Expense by No"));
        if (no == null) {
            return;
        }
        FinanceItem item = getByNo(income, no);
        if (item == null) {
            JOptionPane.showMessageDialog(this, "Not found.", "Update", JOptionPane.WARNING_MESSAGE);
            return;
        }
        JPanel form = makeFormPanel(item);
        int ok = JOptionPane.showConfirmDialog(this, form,
                (income ? "Update Income" : "Update Expense"),
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
        if (ok == JOptionPane.OK_OPTION) {
            FinanceItem edited = readForm(form);
            if (edited == null) {
                return;
            }
            if (income) {
                incomeList.set(no - 1, edited);
            } else {
                expenseList.set(no - 1, edited);
            }
            refreshTables();
            refreshAnalysis();
        }
    }
    private void showDeleteDialog(boolean income) {
        Integer no = askNo((income ? "Delete Income by No" : "Delete Expense by No"));
        if (no == null) {
            return;
        }
        List<FinanceItem> list = income ? incomeList : expenseList;
        if (no < 1 || no > list.size()) {
            JOptionPane.showMessageDialog(this, "Not found.", "Delete", JOptionPane.WARNING_MESSAGE);
            return;
        }
        FinanceItem item = list.get(no - 1);
        int confirm = JOptionPane.showConfirmDialog(this,
                "Delete this item?\n\nNo: " + no
                + "\nSource/Purpose: " + item.sourceOrPurpose
                + "\nAmount: " + MONEY.format(item.amount)
                + "\nDate/Time: " + item.datetime,
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            list.remove((int) (no - 1));
            refreshTables();
            refreshAnalysis();
        }
    }
    private Integer askNo(String title) {
        String s = JOptionPane.showInputDialog(this, "Enter No:", title, JOptionPane.QUESTION_MESSAGE);
        if (s == null) {
            return null;
        }
        try {
            int no = Integer.parseInt(s.trim());
            if (no <= 0) {
                throw new NumberFormatException();
            }
            return no;
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Invalid No.", "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }
    private FinanceItem getByNo(boolean income, int no) {
        List<FinanceItem> list = income ? incomeList : expenseList;
        if (no < 1 || no > list.size()) {
            return null;
        }
        return list.get(no - 1);
    }
    private JPanel makeFormPanel(FinanceItem preset) {
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(Color.WHITE); // SÁNG
        form.setBorder(new EmptyBorder(16, 16, 16, 16));
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(8, 8, 8, 8);
        gc.anchor = GridBagConstraints.WEST;
        JLabel l1 = makeFieldLabel("Source/Purpose");
        JLabel l2 = makeFieldLabel("Amount");
        JLabel l3 = makeFieldLabel("Date/Time (yyyy-MM-dd HH:mm)");
        JLabel l4 = makeFieldLabel("Note");
        JTextField f1 = makeTextField(preset == null ? "" : preset.sourceOrPurpose);
        JTextField f2 = makeTextField(preset == null ? "" : String.valueOf(preset.amount));
        JTextField f3 = makeTextField(preset == null ? DTF.format(LocalDateTime.now()) : preset.datetime);
        JTextArea f4 = new JTextArea(preset == null ? "" : preset.note, 4, 24);
        styleTextArea(f4);
        gc.gridx = 0;
        gc.gridy = 0;
        form.add(l1, gc);
        gc.gridx = 1;
        form.add(f1, gc);
        gc.gridx = 0;
        gc.gridy = 1;
        form.add(l2, gc);
        gc.gridx = 1;
        form.add(f2, gc);
        gc.gridx = 0;
        gc.gridy = 2;
        form.add(l3, gc);
        gc.gridx = 1;
        form.add(f3, gc);
        gc.gridx = 0;
        gc.gridy = 3;
        form.add(l4, gc);
        gc.gridx = 1;
        form.add(new JScrollPane(f4) {
            {
                getViewport().setBackground(Color.WHITE);
                setBorder(BorderFactory.createLineBorder(new Color(220, 225, 230)));
            }
        }, gc);
        form.putClientProperty("f1", f1);
        form.putClientProperty("f2", f2);
        form.putClientProperty("f3", f3);
        form.putClientProperty("f4", f4);
        return form;
    }
    private JLabel makeFieldLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(TEXT);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        return l;
    }
    private JTextField makeTextField(String preset) {
        JTextField tf = new JTextField(preset, 22);
        tf.setForeground(new Color(33, 33, 33));
        tf.setBackground(new Color(250, 252, 255)); // sáng
        tf.setCaretColor(new Color(33, 33, 33));
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                new EmptyBorder(6, 8, 6, 8)
        ));
        return tf;
    }
    private void styleTextArea(JTextArea ta) {
        ta.setForeground(new Color(33, 33, 33));
        ta.setBackground(Color.WHITE);
        ta.setCaretColor(new Color(33, 33, 33));
        ta.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 225, 230)),
                new EmptyBorder(6, 8, 6, 8)
        ));
        ta.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }
    private FinanceItem readForm(JPanel form) {
        JTextField f1 = (JTextField) form.getClientProperty("f1");
        JTextField f2 = (JTextField) form.getClientProperty("f2");
        JTextField f3 = (JTextField) form.getClientProperty("f3");
        JTextArea f4 = (JTextArea) form.getClientProperty("f4");
        String sp = f1.getText().trim();
        String amountStr = f2.getText().trim();
        String dt = f3.getText().trim();
        String note = f4.getText().trim();
        if (sp.isEmpty() || amountStr.isEmpty() || dt.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill all required fields.",
                    "Validation", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        double amt;
        try {
            amt = Double.parseDouble(amountStr);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Amount must be a number.",
                    "Validation", JOptionPane.ERROR_MESSAGE);
            return null;
        }
        try {
            LocalDateTime.parse(dt, DTF);
        } catch (Exception ex) {
            int yn = JOptionPane.showConfirmDialog(this,
                    "Date/Time format expected: yyyy-MM-dd HH:mm\nUse current time instead?",
                    "Date/Time", JOptionPane.YES_NO_OPTION);
            if (yn == JOptionPane.YES_OPTION) {
                dt = DTF.format(LocalDateTime.now());
            } else {
                return null;
            }
        }
        return new FinanceItem(sp, amt, dt, note);
    }
    private void refreshTables() {
        incomeModel.setRowCount(0);
        for (int i = 0; i < incomeList.size(); i++) {
            FinanceItem it = incomeList.get(i);
            incomeModel.addRow(new Object[]{i + 1, it.sourceOrPurpose, it.amount, it.datetime, it.note});
        }
        expenseModel.setRowCount(0);
        for (int i = 0; i < expenseList.size(); i++) {
            FinanceItem it = expenseList.get(i);
            expenseModel.addRow(new Object[]{i + 1, it.sourceOrPurpose, it.amount, it.datetime, it.note});
        }
    }
    private void refreshAnalysis() {
        double inc = incomeList.stream().mapToDouble(it -> it.amount).sum();
        double exp = expenseList.stream().mapToDouble(it -> it.amount).sum();
        totalIncomeLabel.setText("Total Income: " + MONEY.format(inc));
        totalExpenseLabel.setText("Total Expense: " + MONEY.format(exp));
        double total = inc + exp;
        int incomeShare = (total == 0) ? 0 : (int) Math.round((inc / total) * 100.0);
        shareBar.setValue(incomeShare);
        shareBar.setString("Income " + incomeShare + "%  |  Expense " + (100 - incomeShare) + "%");
        String dom;
        if (inc == exp) {
            dom = "Dominance: Balanced (50% / 50%)";
        } else if (inc > exp) {
            double pct = total == 0 ? 0 : (inc - exp) / total * 100.0;
            dom = "Dominance: Income +" + new DecimalFormat("0.##").format(pct) + "% over Expense";
        } else {
            double pct = total == 0 ? 0 : (exp - inc) / total * 100.0;
            dom = "Dominance: Expense +" + new DecimalFormat("0.##").format(pct) + "% over Income";
        }
        dominanceLabel.setText(dom);
    }
    private void exportCSV() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export CSV");
        chooser.setSelectedFile(new File("finance_export.csv"));
        int res = chooser.showSaveDialog(this);
        if (res != JFileChooser.APPROVE_OPTION) {
            return;
        }
        File file = chooser.getSelectedFile();
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            bw.write("INCOME\n");
            bw.write("No,Source/Purpose,Amount,Date/Time,Note\n");
            for (int i = 0; i < incomeList.size(); i++) {
                FinanceItem it = incomeList.get(i);
                bw.write((i + 1) + "," + csv(it.sourceOrPurpose) + "," + it.amount + ","
                        + csv(it.datetime) + "," + csv(it.note) + "\n");
            }
            bw.write("\nEXPENSE\n");
            bw.write("No,Source/Purpose,Amount,Date/Time,Note\n");
            for (int i = 0; i < expenseList.size(); i++) {
                FinanceItem it = expenseList.get(i);
                bw.write((i + 1) + "," + csv(it.sourceOrPurpose) + "," + it.amount + ","
                        + csv(it.datetime) + "," + csv(it.note) + "\n");
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed: " + ex.getMessage(),
                    "Export CSV", JOptionPane.ERROR_MESSAGE);
            return;
        }
        JOptionPane.showMessageDialog(this, "Exported to:\n" + file.getAbsolutePath(),
                "Export CSV", JOptionPane.INFORMATION_MESSAGE);
    }
    private String csv(String s) {
        if (s == null) {
            return "";
        }
        String out = s.replace("\"", "\"\"");
        if (out.contains(",") || out.contains("\"") || out.contains("\n")) {
            return "\"" + out + "\"";
        }
        return out;
    }
    public static void main(String[] args) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ignored) {
        }
        SwingUtilities.invokeLater(() -> new FinanceManagerApp().setVisible(true));
    }
}
