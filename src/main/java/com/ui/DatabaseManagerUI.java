package com.ui;

import com.service.DatabaseService;
import com.service.postgress.PostgresService;
import com.service.sql.MySQLService;
import com.service.sqllite.SQLiteService;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseManagerUI {

    // ── Color palette ──────────────────────────────────────────────────────────
    static final Color C_BG = new Color(15, 15, 26);
    static final Color C_PANEL = new Color(26, 26, 46);
    static final Color C_SURFACE = new Color(37, 37, 65);
    static final Color C_TEXT = new Color(224, 224, 224);
    static final Color C_MUTED = new Color(140, 140, 170);
    static final Color C_DDL_1 = new Color(67, 97, 238);
    static final Color C_DDL_2 = new Color(58, 12, 163);
    static final Color C_DML_1 = new Color(247, 127, 0);
    static final Color C_DML_2 = new Color(214, 40, 40);
    static final Color C_DQL_1 = new Color(46, 196, 182);
    static final Color C_DQL_2 = new Color(6, 214, 160);
    static final Color C_SUCCESS = new Color(6, 214, 160);
    static final Color C_LOGOUT = new Color(100, 116, 139);
    static final Color C_ERR_BG = new Color(45, 10, 10);
    static final Color C_ERR_TXT = new Color(255, 77, 109);
    static final Color C_LOG_BG = new Color(8, 8, 16);
    static final Color C_LOG_TXT = new Color(0, 255, 144);

    static final Font F_TITLE = new Font("Segoe UI", Font.BOLD, 20);
    static final Font F_SECT = new Font("Segoe UI", Font.BOLD, 13);
    static final Font F_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    static final Font F_BTN = new Font("Segoe UI", Font.BOLD, 12);
    static final Font F_MONO = new Font("Monospaced", Font.PLAIN, 13);

    // ── Public entry point ─────────────────────────────────────────────────────
    public static void launch() {
        applyNimbus();
        SwingUtilities.invokeLater(DatabaseManagerUI::startLoginFlow);
    }

    static void startLoginFlow() {
        String dbType = showDbPicker();
        if (dbType == null)
            return;
        DatabaseService svc = showLoginDialog(null, dbType);
        if (svc == null)
            return;
        new Dashboard(svc, dbType).setVisible(true);
    }

    // ── Database picker popup ──────────────────────────────────────────────────
    static String showDbPicker() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(C_PANEL);
        root.setBorder(new EmptyBorder(36, 50, 36, 50));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.gridx = 0;
        g.gridy = 0;
        g.insets = new Insets(0, 0, 28, 0);

        JLabel h = new JLabel("Choose Your Database", SwingConstants.CENTER);
        h.setFont(F_TITLE);
        h.setForeground(C_TEXT);
        root.add(h, g);

        String[] labels = { "PostgreSQL (Neon)", "MySQL", "SQLite" };
        String[] icons = { "🐘  ", "🐬  ", "📦  " };
        Color[] cc1 = { C_DDL_1, C_DML_1, C_DQL_1 };
        Color[] cc2 = { C_DDL_2, C_DML_2, C_DQL_2 };

        String[] chosen = { null };
        JDialog[] dlg = { null };

        for (int i = 0; i < labels.length; i++) {
            final String lbl = labels[i];
            GradientBtn card = new GradientBtn(icons[i] + labels[i], cc1[i], cc2[i]);
            card.setFont(new Font("Segoe UI", Font.BOLD, 15));
            card.setPreferredSize(new Dimension(310, 58));
            card.addActionListener(e -> {
                chosen[0] = lbl;
                if (dlg[0] != null)
                    dlg[0].dispose();
            });
            g.gridy = i + 1;
            g.insets = new Insets(7, 0, 7, 0);
            root.add(card, g);
        }

        JDialog dialog = new JDialog((Frame) null, "Database Manager", true);
        dlg[0] = dialog;
        dialog.setContentPane(root);
        dialog.pack();
        dialog.setLocationRelativeTo(null);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        dialog.setVisible(true);
        return chosen[0];
    }

    // ── Login dialog ───────────────────────────────────────────────────────────
    static DatabaseService showLoginDialog(Component owner, String dbType) {
        boolean isSQLite = dbType.contains("SQLite");

        JPanel card = new JPanel(new GridBagLayout());
        card.setBackground(C_PANEL);
        card.setBorder(new EmptyBorder(34, 48, 34, 48));

        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;

        // Title
        JLabel title = new JLabel("Connect to " + dbType, SwingConstants.CENTER);
        title.setFont(F_TITLE);
        title.setForeground(C_SUCCESS);
        g.gridx = 0;
        g.gridy = 0;
        g.gridwidth = 2;
        g.insets = new Insets(0, 0, 8, 0);
        card.add(title, g);

        JLabel sub = new JLabel(isSQLite ? "Enter the JDBC URL for your SQLite file"
                : "Enter your connection credentials", SwingConstants.CENTER);
        sub.setFont(F_BODY);
        sub.setForeground(C_MUTED);
        g.gridy = 1;
        g.insets = new Insets(0, 0, 22, 0);
        card.add(sub, g);

        // Fields
        g.gridwidth = 1;
        g.weightx = 0.35;
        String defUrl = isSQLite ? "jdbc:sqlite:mydb.db"
                : dbType.contains("MySQL") ? "jdbc:mysql://localhost:3306/testdb"
                        : "jdbc:postgresql://host/dbname?sslmode=require&prepareThreshold=0";
        String defUser = isSQLite ? "" : dbType.contains("MySQL") ? "root" : "neondb_owner";

        JTextField urlField = inputField(defUrl);
        JTextField userField = inputField(defUser);
        JPasswordField passField = passField();
        if (isSQLite) {
            userField.setEnabled(false);
            passField.setEnabled(false);
        }

        addRow(card, g, 2, "JDBC URL :", urlField);
        if (!isSQLite) {
            addRow(card, g, 3, "Username :", userField);
            addRow(card, g, 4, "Password :", passField);
        }

        // Connect button
        int nextRow = isSQLite ? 3 : 5;
        GradientBtn connectBtn = new GradientBtn("  Connect  ", C_SUCCESS, new Color(4, 176, 128));
        connectBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        g.gridx = 0;
        g.gridy = nextRow;
        g.gridwidth = 2;
        g.insets = new Insets(22, 50, 6, 50);
        card.add(connectBtn, g);

        JButton cancelBtn = plainBtn("Cancel");
        g.gridy = nextRow + 1;
        g.insets = new Insets(2, 0, 0, 0);
        card.add(cancelBtn, g);

        JDialog dialog = new JDialog((Frame) null, "Login — " + dbType, true);
        dialog.setContentPane(card);
        dialog.pack();
        dialog.setLocationRelativeTo(owner);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        DatabaseService[] result = { null };

        connectBtn.addActionListener(e -> {
            String url  = urlField.getText().trim();
            String user = userField.getText().trim();
            String pass = new String(passField.getPassword());
            if (url.isBlank()) {
                showError(dialog, "Input Error", "JDBC URL cannot be empty.");
                return;
            }
            connectBtn.setEnabled(false);
            connectBtn.setText("Connecting…");
            new SwingWorker<DatabaseService, Void>() {
                @Override
                protected DatabaseService doInBackground() throws Exception {
                    Connection conn;
                    if (isSQLite) {
                        conn = DriverManager.getConnection(url);
                        return new SQLiteService(conn);
                    } else if (dbType.contains("MySQL")) {
                        conn = DriverManager.getConnection(url, user, pass);
                        return new MySQLService(conn);
                    } else {
                        conn = DriverManager.getConnection(url, user, pass);
                        return new PostgresService(conn);
                    }
                }
                @Override
                protected void done() {
                    try {
                        result[0] = get();
                        dialog.dispose();
                    } catch (ExecutionException ex) {
                        connectBtn.setEnabled(true);
                        connectBtn.setText("  Connect  ");
                        Throwable cause = ex.getCause();
                        showError(dialog, "Connection Failed",
                                cause != null ? cause.getMessage() : ex.getMessage());
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }.execute();
        });
        cancelBtn.addActionListener(e -> dialog.dispose());
        dialog.setVisible(true);
        return result[0];
    }

    // ── Main Dashboard ─────────────────────────────────────────────────────────
    static class Dashboard extends JFrame {

        final DatabaseService service;
        final String dbLabel;
        JTextArea logArea;
        JLabel statusLabel;
        final AtomicBoolean busy = new AtomicBoolean(false);

        Dashboard(DatabaseService svc, String label) {
            super("Database Manager");
            this.service = svc;
            this.dbLabel = label;
            setSize(1150, 760);
            setLocationRelativeTo(null);
            setDefaultCloseOperation(EXIT_ON_CLOSE);

            JPanel root = new JPanel(new BorderLayout(0, 0));
            root.setBackground(C_BG);
            root.add(buildHeader(), BorderLayout.NORTH);

            logArea = new JTextArea();
            JSplitPane split = new JSplitPane(
                    JSplitPane.VERTICAL_SPLIT, buildOperations(), buildLogPanel());
            split.setResizeWeight(0.60);
            split.setDividerSize(5);
            split.setBackground(C_BG);
            root.add(split, BorderLayout.CENTER);

            setContentPane(root);
            redirectOut();
            System.out.println("Connected to " + label + " successfully.");
        }

        // Header
        JPanel buildHeader() {
            GradientPanel hdr = new GradientPanel(new Color(27, 27, 53), new Color(13, 13, 32));
            hdr.setLayout(new BorderLayout());
            hdr.setPreferredSize(new Dimension(0, 62));
            hdr.setBorder(new EmptyBorder(12, 22, 12, 22));

            JLabel appName = new JLabel("  Database Manager");
            appName.setFont(new Font("Segoe UI", Font.BOLD, 20));
            appName.setForeground(Color.WHITE);
            hdr.add(appName, BorderLayout.WEST);

            statusLabel = new JLabel("  Ready");
            statusLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            statusLabel.setForeground(C_SUCCESS);
            statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
            hdr.add(statusLabel, BorderLayout.CENTER);

            JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
            right.setOpaque(false);
            JLabel badge = badge(" " + dbLabel + " ");
            right.add(badge);
            GradientBtn logoutBtn = new GradientBtn(" Logout / Switch DB ", C_LOGOUT,
                    new Color(90, 99, 115));
            logoutBtn.setFont(F_BTN);
            logoutBtn.addActionListener(e -> logout());
            right.add(logoutBtn);
            hdr.add(right, BorderLayout.EAST);
            return hdr;
        }

        // 3-column operations
        JPanel buildOperations() {
            JPanel p = new JPanel(new GridLayout(1, 3, 10, 0));
            p.setBackground(C_BG);
            p.setBorder(new EmptyBorder(12, 14, 8, 14));
            p.add(buildDDL());
            p.add(buildDML());
            p.add(buildDQL());
            return p;
        }

        JPanel buildDDL() {
            JPanel btns = btnBox();
            addOp(btns, "Create Table", C_DDL_1, C_DDL_2, () -> {
                String t = ask("Table name:");
                if (t == null) return;
                runDB("Create Table", () -> service.createTable(t));
            });
            addOp(btns, "Drop Table", C_DDL_1, C_DDL_2, () -> {
                String t = ask("Table to drop:");
                if (t == null) return;
                runDB("Drop Table", () -> service.dropTable(t));
            });
            addOp(btns, "Rename Table", C_DDL_1, C_DDL_2, () -> {
                Map<String, String> f = form("Rename Table", "Current name:", "New name:");
                if (f == null) return;
                runDB("Rename Table", () -> service.renameTable(f.get("Current name:"), f.get("New name:")));
            });
            addOp(btns, "List Tables", C_DDL_1, C_DDL_2, () ->
                    runDB("List Tables", () -> service.listTables()));
            addOp(btns, "Describe Table", C_DDL_1, C_DDL_2, () -> {
                String t = ask("Table name:");
                if (t == null) return;
                runDB("Describe Table", () -> service.describeTable(t));
            });
            addOp(btns, "Truncate Table", C_DDL_1, C_DDL_2, () -> {
                String t = ask("Table to truncate:");
                if (t == null) return;
                runDB("Truncate Table", () -> service.truncateTable(t));
            });
            addOp(btns, "Add Column", C_DDL_1, C_DDL_2, () -> {
                Map<String, String> f = form("Add Column", "Table name:", "Column name:",
                        "Data type (e.g. VARCHAR(100), INT):");
                if (f == null) return;
                runDB("Add Column", () -> service.addColumn(f.get("Table name:"),
                        f.get("Column name:"), f.get("Data type (e.g. VARCHAR(100), INT):")));
            });
            addOp(btns, "Drop Column", C_DDL_1, C_DDL_2, () -> {
                Map<String, String> f = form("Drop Column", "Table name:", "Column name:");
                if (f == null) return;
                runDB("Drop Column", () -> service.dropColumn(f.get("Table name:"), f.get("Column name:")));
            });
            addOp(btns, "Rename Column", C_DDL_1, C_DDL_2, () -> {
                Map<String, String> f = form("Rename Column", "Table name:",
                        "Current column name:", "New column name:");
                if (f == null) return;
                runDB("Rename Column", () -> service.renameColumn(f.get("Table name:"),
                        f.get("Current column name:"), f.get("New column name:")));
            });
            return section("DDL — Table & Column Ops", C_DDL_1, C_DDL_2, btns);
        }

        JPanel buildDML() {
            JPanel btns = btnBox();
            addOp(btns, "Insert Record", C_DML_1, C_DML_2, () -> {
                String tbl = ask("Table name:");
                if (tbl == null || tbl.isBlank()) return;
                // Step 1: fetch schema off-EDT, then show form, then insert off-EDT
                if (!busy.compareAndSet(false, true)) {
                    logArea.append("[BUSY] Please wait — another operation is still running.\n");
                    return;
                }
                statusLabel.setText("  \u23F3 Fetching schema\u2026");
                statusLabel.setForeground(C_DML_1);
                new SwingWorker<java.util.List<String>, Void>() {
                    @Override
                    protected java.util.List<String> doInBackground() throws Exception {
                        return service.getTableColumns(tbl);
                    }
                    @Override
                    protected void done() {
                        busy.set(false);
                        statusLabel.setText("  Ready");
                        statusLabel.setForeground(C_SUCCESS);
                        try {
                            java.util.List<String> cols = get();
                            if (cols.isEmpty()) {
                                showError(Dashboard.this, "No Columns",
                                        "Table '" + tbl + "' has no writable columns.");
                                return;
                            }
                            Map<String, String> vals = form("Insert into " + tbl,
                                    cols.toArray(new String[0]));
                            if (vals == null) return;
                            runDB("Insert Record", () -> service.insertRecord(tbl, new LinkedHashMap<>(vals)));
                        } catch (ExecutionException ex) {
                            Throwable cause = ex.getCause();
                            showError(Dashboard.this, "Schema Error",
                                    cause != null ? cause.getMessage() : ex.getMessage());
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }.execute();
            });
            addOp(btns, "Update by ID", C_DML_1, C_DML_2, () -> {
                Map<String, String> f = form("Update by ID", "Table name:", "Record ID:",
                        "Column to update:", "New value:");
                if (f == null) return;
                try {
                    int id = Integer.parseInt(f.get("Record ID:"));
                    runDB("Update Record", () -> service.updateRecord(f.get("Table name:"),
                            id, f.get("Column to update:"), f.get("New value:")));
                } catch (NumberFormatException ex) {
                    showError(this, "Input Error", "ID must be an integer.");
                }
            });
            addOp(btns, "Delete by ID", C_DML_1, C_DML_2, () -> {
                Map<String, String> f = form("Delete by ID", "Table name:", "Record ID:");
                if (f == null) return;
                try {
                    int id = Integer.parseInt(f.get("Record ID:"));
                    runDB("Delete Record", () -> service.deleteRecord(f.get("Table name:"), id));
                } catch (NumberFormatException ex) {
                    showError(this, "Input Error", "ID must be an integer.");
                }
            });
            addOp(btns, "Update by Match", C_DML_1, C_DML_2, () -> {
                Map<String, String> f = form("Update by Exact Match", "Table name:",
                        "Search column:", "Search value:", "Update column:", "New value:");
                if (f == null) return;
                runDB("Update by Match", () -> service.updateRecordsByAnyData(f.get("Table name:"),
                        f.get("Search column:"), f.get("Search value:"),
                        f.get("Update column:"), f.get("New value:")));
            });
            addOp(btns, "Delete by Match", C_DML_1, C_DML_2, () -> {
                Map<String, String> f = form("Delete by Exact Match",
                        "Table name:", "Column name:", "Value to match:");
                if (f == null) return;
                runDB("Delete by Match", () -> service.deleteRecordsByAnyData(
                        f.get("Table name:"), f.get("Column name:"), f.get("Value to match:")));
            });
            addOp(btns, "Delete All Records", C_DML_1, C_DML_2, () -> {
                String t = ask("Table name (ALL records will be deleted):");
                if (t == null) return;
                int ok = JOptionPane.showConfirmDialog(this,
                        "Delete ALL records from '" + t + "'?", "Confirm",
                        JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (ok == JOptionPane.YES_OPTION)
                    runDB("Delete All Records", () -> service.deleteAllRecords(t));
            });
            return section("DML — Insert / Update / Delete", C_DML_1, C_DML_2, btns);
        }

        JPanel buildDQL() {
            JPanel btns = btnBox();
            addOp(btns, "Fetch All Records", C_DQL_1, C_DQL_2, () -> {
                String t = ask("Table name:");
                if (t == null) return;
                runDB("Fetch All Records", () -> service.fetchAllRecords(t));
            });
            addOp(btns, "Count Records", C_DQL_1, C_DQL_2, () -> {
                String t = ask("Table name:");
                if (t == null) return;
                runDB("Count Records", () -> service.countRecords(t));
            });
            addOp(btns, "Find by ID", C_DQL_1, C_DQL_2, () -> {
                Map<String, String> f = form("Find by ID", "Table name:", "Record ID:");
                if (f == null) return;
                try {
                    int id = Integer.parseInt(f.get("Record ID:"));
                    runDB("Find by ID", () -> service.findRecordById(f.get("Table name:"), id));
                } catch (NumberFormatException ex) {
                    showError(this, "Input Error", "ID must be an integer.");
                }
            });
            addOp(btns, "Find by Exact Match", C_DQL_1, C_DQL_2, () -> {
                Map<String, String> f = form("Find by Exact Match",
                        "Table name:", "Column name:", "Value:");
                if (f == null) return;
                runDB("Find by Match", () -> service.findRecordsByAnyData(
                        f.get("Table name:"), f.get("Column name:"), f.get("Value:")));
            });
            addOp(btns, "Find by Pattern", C_DQL_1, C_DQL_2, () -> {
                Map<String, String> f = form("Find by Pattern (LIKE)",
                        "Table name:", "Column name:", "Pattern (e.g. %john%):");
                if (f == null) return;
                runDB("Find by Pattern", () -> service.findRecordsByAnyDataLike(
                        f.get("Table name:"), f.get("Column name:"),
                        f.get("Pattern (e.g. %john%):")));
            });
            addOp(btns, "Find Greater Than", C_DQL_1, C_DQL_2, () -> {
                Map<String, String> f = form("Find Greater Than (Numeric)",
                        "Table name:", "Column name:", "Numeric value:");
                if (f == null) return;
                try {
                    int val = Integer.parseInt(f.get("Numeric value:"));
                    runDB("Find Greater Than", () -> service.findRecordsByAnyDataGreaterThan(
                            f.get("Table name:"), f.get("Column name:"), val));
                } catch (NumberFormatException ex) {
                    showError(this, "Input Error", "Value must be an integer.");
                }
            });
            return section("DQL — Query & Search", C_DQL_1, C_DQL_2, btns);
        }

        // Log panel
        JPanel buildLogPanel() {
            JPanel panel = new JPanel(new BorderLayout(0, 0));
            panel.setBackground(C_BG);
            panel.setBorder(new EmptyBorder(0, 14, 12, 14));

            // Top bar
            JPanel bar = new JPanel(new BorderLayout());
            bar.setBackground(C_SURFACE);
            bar.setBorder(new EmptyBorder(7, 14, 7, 14));
            JLabel lbl = new JLabel("Result Log");
            lbl.setFont(F_SECT);
            lbl.setForeground(C_LOG_TXT);
            bar.add(lbl, BorderLayout.WEST);
            GradientBtn clearBtn = new GradientBtn("  Clear Log  ", C_SURFACE,
                    new Color(58, 58, 85));
            clearBtn.setFont(F_BTN);
            clearBtn.setForeground(new Color(170, 170, 200));
            clearBtn.addActionListener(e -> logArea.setText(""));
            bar.add(clearBtn, BorderLayout.EAST);

            logArea.setEditable(false);
            logArea.setBackground(C_LOG_BG);
            logArea.setForeground(C_LOG_TXT);
            logArea.setFont(F_MONO);
            logArea.setMargin(new Insets(8, 12, 8, 12));
            logArea.setCaretColor(C_SUCCESS);

            JScrollPane scroll = new JScrollPane(logArea);
            scroll.setBorder(BorderFactory.createLineBorder(C_SURFACE, 2));
            scroll.getViewport().setBackground(C_LOG_BG);

            panel.add(bar, BorderLayout.NORTH);
            panel.add(scroll, BorderLayout.CENTER);
            return panel;
        }

        void redirectOut() {
            PrintStream ps = new PrintStream(new OutputStream() {
                @Override
                public void write(int b) {
                    SwingUtilities.invokeLater(() -> {
                        logArea.append(String.valueOf((char) b));
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                    });
                }

                @Override
                public void write(byte[] b, int off, int len) {
                    String s = new String(b, off, len);
                    SwingUtilities.invokeLater(() -> {
                        logArea.append(s);
                        logArea.setCaretPosition(logArea.getDocument().getLength());
                    });
                }
            }, true);
            System.setOut(ps);
            System.setErr(ps);
        }

        void logout() {
            try {
                System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out)));
                System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err)));
                service.close();
            } catch (Exception ignored) {
            }
            dispose();
            SwingUtilities.invokeLater(DatabaseManagerUI::startLoginFlow);
        }

        // Run a DB task off the EDT; prevents UI freezing on slow networks
        void runDB(String opName, Runnable task) {
            if (!busy.compareAndSet(false, true)) {
                logArea.append("[BUSY] Please wait — another operation is still running.\n");
                return;
            }
            statusLabel.setText("  \u23F3 " + opName + "\u2026");
            statusLabel.setForeground(C_DML_1);
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    task.run();
                    return null;
                }
                @Override
                protected void done() {
                    busy.set(false);
                    statusLabel.setText("  Ready");
                    statusLabel.setForeground(C_SUCCESS);
                    try {
                        get();
                    } catch (ExecutionException ex) {
                        Throwable cause = ex.getCause();
                        showError(Dashboard.this, "Operation Error",
                                cause != null ? cause.getMessage() : ex.getMessage());
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                }
            }.execute();
        }

        // ── Input helpers ──────────────────────────────────────────────────────
        String ask(String label) {
            JPanel p = new JPanel(new GridLayout(2, 1, 4, 6));
            p.setBackground(C_PANEL);
            p.setBorder(new EmptyBorder(10, 10, 10, 10));
            JLabel l = new JLabel(label);
            l.setFont(F_BODY);
            l.setForeground(C_TEXT);
            JTextField tf = inputField("");
            p.add(l);
            p.add(tf);
            int r = JOptionPane.showConfirmDialog(this, p, "Input",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION)
                return null;
            String v = tf.getText().trim();
            return v.isEmpty() ? null : v;
        }

        Map<String, String> form(String title, String... labels) {
            JPanel p = new JPanel(new GridLayout(labels.length * 2, 1, 4, 5));
            p.setBackground(C_PANEL);
            p.setBorder(new EmptyBorder(12, 12, 12, 12));
            JTextField[] fields = new JTextField[labels.length];
            for (int i = 0; i < labels.length; i++) {
                JLabel l = new JLabel(labels[i]);
                l.setFont(F_BODY);
                l.setForeground(C_TEXT);
                fields[i] = inputField("");
                p.add(l);
                p.add(fields[i]);
            }
            int r = JOptionPane.showConfirmDialog(this, p, title,
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
            if (r != JOptionPane.OK_OPTION)
                return null;
            LinkedHashMap<String, String> map = new LinkedHashMap<>();
            for (int i = 0; i < labels.length; i++)
                map.put(labels[i], fields[i].getText().trim());
            return map;
        }
    }

    // ── Static UI builders ─────────────────────────────────────────────────────
    static JPanel section(String title, Color c1, Color c2, JPanel btns) {
        JPanel outer = new JPanel(new BorderLayout(0, 0));
        outer.setBackground(C_PANEL);
        outer.setBorder(new CompoundBorder(
                new LineBorder(c1.darker(), 1, true),
                new EmptyBorder(0, 0, 6, 0)));

        GradientPanel hdr = new GradientPanel(c1, c2);
        hdr.setLayout(new FlowLayout(FlowLayout.LEFT, 14, 10));
        JLabel lbl = new JLabel(title);
        lbl.setFont(F_SECT);
        lbl.setForeground(Color.WHITE);
        hdr.add(lbl);
        outer.add(hdr, BorderLayout.NORTH);

        JScrollPane sp = new JScrollPane(btns);
        sp.setBorder(null);
        sp.setBackground(C_PANEL);
        sp.getViewport().setBackground(C_PANEL);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        outer.add(sp, BorderLayout.CENTER);
        return outer;
    }

    static JPanel btnBox() {
        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        p.setBackground(C_PANEL);
        p.setBorder(new EmptyBorder(10, 10, 10, 10));
        return p;
    }

    static void addOp(JPanel box, String label, Color c1, Color c2, Runnable action) {
        GradientBtn btn = new GradientBtn("  " + label, c1, c2);
        btn.setFont(F_BTN);
        btn.setForeground(Color.WHITE);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.addActionListener(e -> action.run());
        box.add(btn);
        box.add(Box.createVerticalStrut(5));
    }

    static void addRow(JPanel p, GridBagConstraints g, int row, String label, JComponent field) {
        g.gridy = row;
        g.gridx = 0;
        g.gridwidth = 1;
        g.insets = new Insets(8, 4, 8, 8);
        g.weightx = 0.3;
        JLabel l = new JLabel(label);
        l.setFont(F_BODY);
        l.setForeground(C_MUTED);
        p.add(l, g);
        g.gridx = 1;
        g.weightx = 0.7;
        g.insets = new Insets(8, 0, 8, 4);
        p.add(field, g);
    }

    static JTextField inputField(String text) {
        JTextField f = new JTextField(22);
        f.setText(text);
        f.setFont(F_BODY);
        f.setBackground(C_SURFACE);
        f.setForeground(C_TEXT);
        f.setCaretColor(C_SUCCESS);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_DDL_1, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        enableCopyPaste(f);
        return f;
    }

    static JPasswordField passField() {
        JPasswordField f = new JPasswordField(22);
        f.setFont(F_BODY);
        f.setBackground(C_SURFACE);
        f.setForeground(C_TEXT);
        f.setCaretColor(C_SUCCESS);
        f.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(C_DDL_1, 1, true),
                new EmptyBorder(6, 10, 6, 10)));
        enableCopyPaste(f);
        return f;
    }

    static void enableCopyPaste(JTextComponent c) {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx();
        InputMap im = c.getInputMap(JComponent.WHEN_FOCUSED);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_C, mask), DefaultEditorKit.copyAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_V, mask), DefaultEditorKit.pasteAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_X, mask), DefaultEditorKit.cutAction);
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_A, mask), DefaultEditorKit.selectAllAction);
    }

    static JLabel badge(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Color.WHITE);
        l.setBackground(C_DDL_1);
        l.setOpaque(true);
        l.setBorder(new EmptyBorder(4, 10, 4, 10));
        return l;
    }

    static JButton plainBtn(String text) {
        JButton b = new JButton(text);
        b.setFont(F_BODY);
        b.setForeground(C_MUTED);
        b.setBackground(C_PANEL);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static void showError(Component parent, String title, String msg) {
        JPanel p = new JPanel(new BorderLayout(10, 10));
        p.setBackground(C_ERR_BG);
        p.setBorder(new EmptyBorder(18, 22, 18, 22));
        JLabel t = new JLabel("  " + title);
        t.setFont(new Font("Segoe UI", Font.BOLD, 15));
        t.setForeground(C_ERR_TXT);
        p.add(t, BorderLayout.NORTH);
        JTextArea ta = new JTextArea(msg);
        ta.setEditable(false);
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setBackground(C_ERR_BG);
        ta.setForeground(C_ERR_TXT);
        ta.setFont(F_BODY);
        p.add(ta, BorderLayout.CENTER);
        JOptionPane.showMessageDialog(parent, p, "Error", JOptionPane.ERROR_MESSAGE);
    }

    static void applyNimbus() {
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            UIManager.put("control", C_PANEL);
            UIManager.put("nimbusBase", new Color(26, 26, 46));
            UIManager.put("nimbusBlueGrey", new Color(37, 37, 65));
            UIManager.put("nimbusFocus", C_DDL_1);
            UIManager.put("nimbusLightBackground", C_SURFACE);
            UIManager.put("text", C_TEXT);
            UIManager.put("nimbusSelectionBackground", C_DDL_1);
            UIManager.put("OptionPane.background", C_PANEL);
            UIManager.put("Panel.background", C_PANEL);
        } catch (Exception ignored) {
        }
    }

    // ── Custom Swing components ────────────────────────────────────────────────
    static class GradientPanel extends JPanel {
        final Color c1, c2;

        GradientPanel(Color c1, Color c2) {
            this.c1 = c1;
            this.c2 = c2;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setPaint(new GradientPaint(0, 0, c1, getWidth(), getHeight(), c2));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
            g2.dispose();
            super.paintComponent(g);
        }
    }

    static class GradientBtn extends JButton {
        final Color c1, c2, hover;

        GradientBtn(String text, Color c1, Color c2) {
            super(text);
            this.c1 = c1;
            this.c2 = c2;
            float[] hsb = Color.RGBtoHSB(c1.getRed(), c1.getGreen(), c1.getBlue(), null);
            this.hover = Color.getHSBColor(hsb[0], Math.max(0, hsb[1] - 0.12f),
                    Math.min(1f, hsb[2] + 0.18f));
            setBackground(c1);
            setForeground(Color.WHITE);
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    setBackground(hover);
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    setBackground(c1);
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            Color bg = getBackground();
            g2.setPaint(new GradientPaint(0, 0, bg, getWidth(), getHeight(),
                    bg == c1 ? c2 : bg));
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}
