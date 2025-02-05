import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.util.EventObject;

public class barangGUI extends JFrame {
    private static final String URL = "jdbc:mysql://localhost:3306/tokoabc";
    private static final String USER = "root";
    private static final String PASSWORD = "";
    private Connection conn;
    
    private JTable table;
    private DefaultTableModel model;
    private JTextField namaField, kategoriField, jumlahField, hargaField;
    private JButton tambahButton;
    
    public barangGUI() {
        setTitle("Toko ABC");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Form Tambah Data
        JPanel inputPanel = new JPanel(new GridLayout(5, 2));
        namaField = new JTextField();
        kategoriField = new JTextField();
        jumlahField = new JTextField();
        hargaField = new JTextField();
        tambahButton = new JButton("Tambah");
        
        inputPanel.add(new JLabel("Nama:"));
        inputPanel.add(namaField);
        inputPanel.add(new JLabel("Kategori:"));
        inputPanel.add(kategoriField);
        inputPanel.add(new JLabel("Jumlah:"));
        inputPanel.add(jumlahField);
        inputPanel.add(new JLabel("Harga:"));
        inputPanel.add(hargaField);
        inputPanel.add(tambahButton);
        
        add(inputPanel, BorderLayout.NORTH);

        // Tabel dengan tombol Update & Hapus
        model = new DefaultTableModel(new String[]{"ID", "Nama", "Kategori", "Jumlah", "Harga", "Update", "Hapus"}, 0);
        table = new JTable(model);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        table.getColumn("Update").setCellRenderer(new ButtonRenderer());
        table.getColumn("Update").setCellEditor(new ButtonEditor("Update"));

        table.getColumn("Hapus").setCellRenderer(new ButtonRenderer());
        table.getColumn("Hapus").setCellEditor(new ButtonEditor("Hapus"));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
       
        conn = getConnection();
        loadBarang();
        
        tambahButton.addActionListener(e -> tambahBarang());
    }

    private Connection getConnection() {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void loadBarang() {
        model.setRowCount(0);
        String sql = "SELECT * FROM barang";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("id"), 
                        rs.getString("nama_barang"), 
                        rs.getString("kategori"), 
                        rs.getInt("jumlah"), 
                        rs.getDouble("harga"), 
                        "Update", 
                        "Hapus"
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void tambahBarang() {
        String nama = namaField.getText();
        String kategori = kategoriField.getText();
        int jumlah = Integer.parseInt(jumlahField.getText());
        double harga = Double.parseDouble(hargaField.getText());

        String sql = "INSERT INTO barang (nama_barang, kategori, jumlah, harga) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, kategori);
            stmt.setInt(3, jumlah);
            stmt.setDouble(4, harga);
            stmt.executeUpdate();
            loadBarang();
            JOptionPane.showMessageDialog(this, "Barang berhasil ditambahkan!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updateBarang(int row) {
        int id = Integer.parseInt(model.getValueAt(row, 0).toString());
        String nama = model.getValueAt(row, 1).toString();
        String kategori = model.getValueAt(row, 2).toString();
        int jumlah = Integer.parseInt(model.getValueAt(row, 3).toString());
        double harga = Double.parseDouble(model.getValueAt(row, 4).toString());

        String sql = "UPDATE barang SET nama_barang=?, kategori=?, jumlah=?, harga=? WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, nama);
            stmt.setString(2, kategori);
            stmt.setInt(3, jumlah);
            stmt.setDouble(4, harga);
            stmt.setInt(5, id);
            stmt.executeUpdate();
            loadBarang();
            JOptionPane.showMessageDialog(this, "Barang berhasil diperbarui!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private int row;

        public ButtonEditor(String label) {
            super(new JCheckBox());
            this.label = label;
            button = new JButton(label);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            this.row = row;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            int id = Integer.parseInt(model.getValueAt(row, 0).toString());
            if (label.equals("Update")) {
                updateBarang(row);
            } else {
                hapusBarang(id);
            }
            return label;
        }
    }

    private void hapusBarang(int id) {
        String sql = "DELETE FROM barang WHERE id=?";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            stmt.executeUpdate();
            loadBarang();
            JOptionPane.showMessageDialog(this, "Barang berhasil dihapus!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new barangGUI().setVisible(true));
    }
}
