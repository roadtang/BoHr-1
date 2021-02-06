/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.bohr.core.Transaction;
import org.bohr.core.TransactionType;
import org.bohr.crypto.Hex;
import org.bohr.gui.Action;
import org.bohr.gui.BohrGui;
import org.bohr.gui.ComboBoxItem;
import org.bohr.gui.SwingUtil;
import org.bohr.gui.dialog.TransactionDialog;
import org.bohr.gui.laf.DefaultComboBoxUI;
import org.bohr.gui.laf.DefaultTextFieldUI;
import org.bohr.gui.layout.TableLayout;
import org.bohr.gui.model.WalletAccount;
import org.bohr.gui.model.WalletModel;
import org.bohr.gui.uiUtils.FontUtils;
import org.bohr.gui.uiUtils.LAFUtils;
import org.bohr.message.GuiMessages;
import org.bohr.util.ByteArray;
import org.bohr.util.StringUtil;
import org.bohr.util.exception.UnreachableException;

/**
 * Transactions panel displays all transaction from/to accounts of the wallet.
 */
public class TransactionsPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static String[] columnNames = { GuiMessages.get("Type"), GuiMessages.get("FromTo"),
			GuiMessages.get("Value"), GuiMessages.get("Time"), GuiMessages.get("Status") };

	private transient BohrGui gui;
	private transient WalletModel model;

	private JTable table;
	private TransactionsTableModel tableModel;
	private TransactionsPanelFilter panelFilter;

	public TransactionsPanel(BohrGui gui, JFrame frame) {
			initUI(gui, frame);
	}

	private void initUI(BohrGui gui, JFrame frame) {
		this.gui = gui;
		this.model = gui.getModel();
		this.model.addListener(this);

		JPanel panel = LAFUtils.createRoundRectanglePanel(new double[] { 30, TableLayout.FILL, 30 },
				new double[] { 20, TableLayout.FILL, 20 });
		panel.setBackground(new Color(0x272729));

		JPanel mainPanel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL, 40, TableLayout.FILL),
				LAFUtils.getDoubleArray(80, 20, 80, 30, TableLayout.FILL));

		panelFilter = new TransactionsPanelFilter(gui, tableModel);

		mainPanel.add(getTypePanel(), "0,0");

		mainPanel.add(getFromPanel(), "2,0");

		mainPanel.add(getToPanel(), "0,2");

		mainPanel.add(getCountPanel(), "2,2");

		mainPanel.add(getTablePanel(frame), "0,4,2,4");

		panel.add(mainPanel, "1,1");

		setLayout(new BorderLayout());
		add(panel);

		refListData();
	}

	private JPanel getFormatPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.PREFERRED, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, 45));
		return panel;
	}

	private void setComFont(JComponent c) {
		FontUtils.setBOLDFont(c, 14);
		c.setForeground(Color.white);
	}

	private JPanel getTypePanel() {
		JPanel panel = getFormatPanel();
		JLabel type = new JLabel(GuiMessages.get("Type"));
		JComboBox<ComboBoxItem<TransactionType>> typeComboBox = panelFilter.getSelectType();

		setComFont(type);
		typeComboBox.setUI(new DefaultComboBoxUI());
		typeComboBox.setForeground(Color.white);

		panel.add(type, "0,0");
		panel.add(typeComboBox, "0,2,1,2");
		return panel;
	}

	private JPanel getFromPanel() {
		JPanel panel = getFormatPanel();
		JLabel from = new JLabel(GuiMessages.get("From"));
		JComboBox<ComboBoxItem<byte[]>> fromComboBox = panelFilter.getSelectFrom();

		setComFont(from);
		fromComboBox.setUI(new DefaultComboBoxUI());

		panel.add(from, "0,0");
		panel.add(fromComboBox, "0,2,1,2");
		return panel;
	}

	private JPanel getToPanel() {
		JPanel panel = getFormatPanel();
		JLabel to = new JLabel(GuiMessages.get("To"));
		JComboBox<ComboBoxItem<byte[]>> toComboBox = panelFilter.getSelectTo();

		setComFont(to);
		toComboBox.setUI(new DefaultComboBoxUI());

		panel.add(to, "0,0");
		panel.add(toComboBox, "0,2,1,2");
		return panel;
	}

	private JPanel getCountPanel() {
		JPanel panel = LAFUtils.createPanel(
				LAFUtils.getDoubleArray(TableLayout.FILL, 15, TableLayout.PREFERRED, 15, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, 45));

		JLabel amount = new JLabel(GuiMessages.get("Amount"));
		JLabel separator = new JLabel("-");

		setComFont(amount);
		setComFont(separator);

		JTextField min = panelFilter.getTxtMin();
		JTextField max = panelFilter.getTxtMax();

		min.setUI(new DefaultTextFieldUI());

		max.setUI(new DefaultTextFieldUI());

		panel.add(amount, "0,0,L,F");

		panel.add(min, "0,2");
		panel.add(separator, "2,2");
		panel.add(max, "4,2");

		return panel;
	}

	private JPanel getRecordPanelByInfo() {
		JPanel panel = LAFUtils.createPanel(new double[] { TableLayout.PREFERRED },
				new double[] { TableLayout.PREFERRED });

		JLabel label = LAFUtils.createLabel(GuiMessages.get("Transactions"), Font.BOLD, 20F, 0x131415);
		label.setForeground(Color.white);
//		ImageIcon buttonIcon = SwingUtil.loadImage("jy", 72, 28);
//		JLabel label = new JLabel(buttonIcon);
		panel.add(label, "0,0");

		return panel;
	}

	private JPanel getListPanel(JFrame frame) {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.FILL));
		panel.setBackground(new Color(0x272729));
		transactionsList = new JList<Transaction>(proListModel);
		transactionsList.setCellRenderer(new ProListRender());
		transactionsList.setBackground(new Color(0x272729));
		JScrollPane s = new JScrollPane(transactionsList);
		s.setBorder(new EmptyBorder(0, 0, 0, 0));
		s.setBackground(new Color(0x272729));
		panel.add(s, "0,0");

		transactionsList.addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				int index = transactionsList.locationToIndex(e.getPoint());
				Rectangle r = transactionsList.getCellBounds(index, index);
				if (r == null) {
					return;
				}
				boolean bb = r.contains(e.getPoint());
				if (bb) {
					setPopupListSelectedIndex(transactionsList, index);
				} else {
					transactionsList.clearSelection();
				}

				transactionsList.repaint();
			}
		});
		transactionsList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					Transaction tx = transactionsList.getSelectedValue();
					if (tx != null) {
						TransactionDialog dialog = new TransactionDialog(frame, tx, gui.getKernel());
						dialog.setVisible(true);
					}
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				transactionsList.clearSelection();
			}
		});

		refListData();

		return panel;
	}

	protected void setPopupListSelectedIndex(JList list, int index) {
		if (list.getModel().getSize() <= 0) {
			return;
		}
		if (index >= list.getModel().getSize()) {
			index = 0;
		}
		if (index < 0) {
			index = list.getModel().getSize() - 1;
		}
		list.ensureIndexIsVisible(index);
		list.setSelectedIndex(index);
	}

	class ProListRender extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (value instanceof Transaction) {
				Transaction t = (Transaction) value;
				ProListLinePanel pp = new ProListLinePanel(t);

				if (isSelected) {
					pp.setOpaque(true);
				}

				return pp;
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}

	private static final ImageIcon trans_toSelf = SwingUtil.loadImage("trans1", 32, 32);
	private static final ImageIcon trans_out = SwingUtil.loadImage("trans2", 32, 32);
	private static final ImageIcon trans_in = SwingUtil.loadImage("trans3", 32, 32);
	private Set<ByteArray> accounts = new HashSet<>();

	class ProListLinePanel extends JPanel {
		private static final long serialVersionUID = 1L;

		private Color color = new Color(0xf2f3f6);

		ProListLinePanel(Transaction tx) {
			setOpaque(false);
			double[] colSize = { TableLayout.PREFERRED, 15, TableLayout.FILL, TableLayout.PREFERRED, 5 };
			double[] rowSize = { TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10 };
			double[][] tableSize = { colSize, rowSize };
			TableLayout layout = new TableLayout(tableSize);
			setLayout(layout);

			boolean inBound = accounts.contains(ByteArray.of(tx.getTo()));
			boolean outBound = accounts.contains(ByteArray.of(tx.getFrom()));

			JLabel lblType = new JLabel("");
			String bounding = inBound ? "inbound" : "outbound";
			String name = (inBound && outBound) ? "cycle" : (bounding);

			ImageIcon img = null;
			if ("inbound".equals(bounding)) {
				img = trans_in;
			} else if ("outbound".equals(bounding)) {
				img = trans_out;
			} else if ("cycle".equals(name)) {
				img = trans_toSelf;
			}

			lblType.setIcon(img);
			add(lblType, "0,0,0,2,F,C");

			String to = Hex.encode0x(tx.getTo());
			to = StringUtil.hexToBase58(to);
			JLabel labelAddress = new JLabel(to);
			labelAddress.setFont(labelAddress.getFont().deriveFont(Font.BOLD, 12F));
			labelAddress.setForeground(new Color(0x23243b));
			add(labelAddress, "2,0,L,F");

			JLabel lblTime = new JLabel(SwingUtil.formatTimestamp1(tx.getTimestamp()));
			lblTime.setFont(lblTime.getFont().deriveFont(Font.PLAIN, 10F));
			lblTime.setForeground(new Color(0xa2a6ae));
			add(lblTime, "2,2,L,F");

			String mathSign = inBound ? "+" : "-";
			Color col = inBound ? new Color(0xffff00) : new Color(0xffad00);
			String prefix = (inBound && outBound) ? "" : (mathSign);
			JLabel lblAmount = new JLabel(prefix + SwingUtil.formatAmountNoUnit(tx.getValue()));
			lblAmount.setFont(lblAmount.getFont().deriveFont(Font.BOLD, 12F));
			lblAmount.setForeground(col);
			add(lblAmount, "3,0,R,F");

//			JLabel info = new JLabel("Bohr "+GuiMessages.get("Completed"));
			JLabel info = new JLabel(tx.getType().toString());
			info.setFont(info.getFont().deriveFont(Font.PLAIN, 10F));
			info.setForeground(new Color(0xa6aab2));
			add(info, "3,2,R,F");

			setUI(new BasicPanelUI() {
				@Override
				public void update(Graphics g, JComponent c) {
//					super.update(g, c);
					if (c.isOpaque()) {
						g.setColor(c.getBackground());
//						g.fillRect(0, 0, c.getWidth(), c.getHeight());
						if (g != null && g instanceof Graphics2D) {
							Graphics2D g2 = (Graphics2D) g;
							g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

							RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
							rect.setRoundRect(0, 0, c.getWidth(), c.getHeight() - 12, 15, 15);

							g2.fill(rect);
						}
					}
					paint(g, c);
				}
			});

			if (color != null) {
				setBackground(color);
			}
		}
	}

	private JPanel getMoreDataPanel() {
		JPanel panel = LAFUtils.createPanel(
				LAFUtils.getDoubleArray(TableLayout.FILL, TableLayout.PREFERRED, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED));
		panel.setBackground(new Color(0x272729));
		JButton button = new JButton();
		panel.add(button, "1,0,C,T");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				requestCount += NUMBER_OF_TRANSACTIONS;
				refListData();
			}
		});

		button.setText(null);

		button.setVerticalAlignment(AbstractButton.CENTER);
		button.setHorizontalAlignment(AbstractButton.CENTER);

		button.setHorizontalTextPosition(AbstractButton.CENTER);
		button.setVerticalTextPosition(AbstractButton.CENTER);

		button.setFocusPainted(false);
		button.setFocusable(false);
		button.setOpaque(false);
		button.setContentAreaFilled(false);

		button.setBorder(null);

		Dimension dimension = new Dimension(90, 22);
		button.setSize(dimension);
		button.setPreferredSize(dimension);
		button.setMaximumSize(dimension);
		button.setMinimumSize(dimension);

		ImageIcon selectIcon = LAFUtils.createEmptyIconImage(dimension.width, dimension.height, 0x272729);
		ImageIcon selectNoIcon = LAFUtils.createEmptyIconImage(dimension.width, dimension.height, 0x272729);
		Font font = FontUtils.getFont().deriveFont(Font.PLAIN, 14);
		font = FontUtils.getFont().deriveFont(Font.PLAIN, 14);
		String name = GuiMessages.get("ShowMore") + " ＞";
		ImageIcon noSelectIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, null, null, name, "9599a3", font);
		ImageIcon rolloverIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, null, null, name, "c1c4cc", font);
		ImageIcon pressedIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, null, null, name, "808183", font);

		ImageIcon select_icon = LAFUtils.getButtonImageIcon1(selectIcon, null, null, name, "808183", font);

		button.setIcon(noSelectIcon);

		button.setRolloverIcon(rolloverIcon);

		button.setRolloverSelectedIcon(select_icon);
		button.setSelectedIcon(select_icon);

		button.setPressedIcon(pressedIcon);

		button.setDisabledIcon(select_icon);
		button.setDisabledSelectedIcon(select_icon);

		return panel;
	}

	private JPanel getTablePanel(JFrame frame) {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL),
				LAFUtils.getDoubleArray(30, TableLayout.FILL, 15, 43));

		panel.add(getRecordPanelByInfo(), "0,0");

		panel.add(getListPanel(frame), "0,1");

		panel.add(getMoreDataPanel(), "0,3");

		return panel;
	}

	private void initUI_old(BohrGui gui, JFrame frame) {
		this.gui = gui;
		this.model = gui.getModel();
		this.model.addListener(this);

		setLayout(new BorderLayout(0, 0));

		tableModel = new TransactionsTableModel();
		table = new JTable(tableModel);
		table.setName("transactionsTable");
		table.setBackground(Color.WHITE);
		table.setFillsViewportHeight(true);
		table.setGridColor(Color.LIGHT_GRAY);
		table.setRowHeight(25);
		table.getTableHeader().setPreferredSize(new Dimension(10000, 24));
		SwingUtil.setColumnWidths(table, 800, 0.1, 0.4, 0.15, 0.2, 0.15);
		SwingUtil.setColumnAlignments(table, false, false, true, true, true);

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				JTable sourceTable = (JTable) me.getSource();
				Point p = me.getPoint();
				int row = sourceTable.rowAtPoint(p);
				if (me.getClickCount() == 2 && row != -1) {
					Transaction tx = tableModel.getRow(sourceTable.convertRowIndexToModel(row));
					if (tx != null) {
						TransactionDialog dialog = new TransactionDialog(frame, tx, gui.getKernel());
						dialog.setVisible(true);
					}
				}
			}
		});

		// customized table sorter
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		sorter.setComparator(2, SwingUtil.VALUE_COMPARATOR);
		sorter.setComparator(3, SwingUtil.TIMESTAMP_COMPARATOR);
		table.setRowSorter(sorter);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBorder(new LineBorder(Color.LIGHT_GRAY));

		JLabel to = new JLabel(GuiMessages.get("To"));
		JLabel from = new JLabel(GuiMessages.get("From"));
		JLabel type = new JLabel(GuiMessages.get("Type"));
		JLabel amount = new JLabel(GuiMessages.get("Amount"));
		JLabel separator = new JLabel("-");

		// create filter
		panelFilter = new TransactionsPanelFilter(gui, tableModel);

		GroupLayout groupLayout = new GroupLayout(this);
		groupLayout
				.setHorizontalGroup(
						groupLayout
								.createParallelGroup(
										GroupLayout.Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup().addComponent(type).addGap(10)
										.addComponent(panelFilter.getSelectType()).addGap(10).addComponent(from)
										.addGap(10)
										.addComponent(panelFilter.getSelectFrom(), GroupLayout.PREFERRED_SIZE, 210,
												Short.MAX_VALUE)
										.addGap(10).addComponent(to).addGap(10)
										.addComponent(panelFilter.getSelectTo(), GroupLayout.PREFERRED_SIZE, 210,
												Short.MAX_VALUE)
										.addGap(10).addComponent(amount).addGap(10)
										.addComponent(panelFilter.getTxtMin(), GroupLayout.PREFERRED_SIZE, 60,
												Short.MAX_VALUE)
										.addComponent(separator).addComponent(panelFilter.getTxtMax(),
												GroupLayout.PREFERRED_SIZE, 60, Short.MAX_VALUE))
								.addComponent(scrollPane));
		groupLayout.setVerticalGroup(groupLayout.createSequentialGroup().addGroup(groupLayout.createSequentialGroup()
				.addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.CENTER, false).addComponent(type)
						.addComponent(panelFilter.getSelectType(), GroupLayout.PREFERRED_SIZE, 25,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(from)
						.addComponent(panelFilter.getSelectFrom(), GroupLayout.PREFERRED_SIZE, 25,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(to)
						.addComponent(panelFilter.getSelectTo(), GroupLayout.PREFERRED_SIZE, 25,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(amount)
						.addComponent(panelFilter.getTxtMin(), GroupLayout.PREFERRED_SIZE, 25,
								GroupLayout.PREFERRED_SIZE)
						.addComponent(separator).addComponent(panelFilter.getTxtMax(), GroupLayout.PREFERRED_SIZE, 25,
								GroupLayout.PREFERRED_SIZE)))
				.addGap(18).addComponent(scrollPane));
		setLayout(groupLayout);

		refresh();
	}

	class TransactionsTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private transient List<StatusTransaction> transactions;

		TransactionsTableModel() {
			this.transactions = Collections.emptyList();
		}

		public void setData(List<StatusTransaction> transactions) {
			this.transactions = transactions;
			this.fireTableDataChanged();
		}

		Transaction getRow(int row) {
			if (row >= 0 && row < transactions.size()) {
				return transactions.get(row).getTransaction();
			}

			return null;
		}

		@Override
		public int getRowCount() {
			return transactions.size();
		}

		@Override
		public int getColumnCount() {
			return columnNames.length;
		}

		@Override
		public String getColumnName(int column) {
			return columnNames[column];
		}

		@Override
		public Object getValueAt(int row, int column) {
			StatusTransaction tx = transactions.get(row);

			switch (column) {
			case 0:
				return tx.getTransaction().getType().name();
			case 1:
				return SwingUtil.getTransactionDescription(gui, tx.getTransaction());
			case 2:
				return SwingUtil.formatAmount(tx.getTransaction().getValue());
			case 3:
				return SwingUtil.formatTimestamp(tx.getTransaction().getTimestamp());
			case 4:
				return tx.getStatus();
			default:
				return null;
			}
		}
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		Action action = Action.valueOf(e.getActionCommand());

		switch (action) {
		case REFRESH:
				refListData();
			break;
		default:
			throw new UnreachableException();
		}
	}

	protected void refListData() {
		List<StatusTransaction> transactions = new ArrayList<>();

		// add pending transactions
		transactions.addAll(
				gui.getKernel().getPendingManager().getPendingTransactions().parallelStream().filter(pendingTx -> {
					for (WalletAccount acc : model.getAccounts()) {
						if (Arrays.equals(acc.getAddress(), pendingTx.transaction.getFrom())
								|| Arrays.equals(acc.getAddress(), pendingTx.transaction.getTo())) {
							return true;
						}
					}
					return false;
				}).map(pendingTx -> new StatusTransaction(pendingTx.transaction, GuiMessages.get("Pending")))
						.collect(Collectors.toList()));

		// add completed transactions
		Set<ByteArray> hashes = new HashSet<>();
		for (WalletAccount acc : model.getAccounts()) {
			for (Transaction tx : acc.getTransactions()) {
				ByteArray key = ByteArray.of(tx.getHash());
				if (!hashes.contains(key)) {
					transactions.add(new StatusTransaction(tx, GuiMessages.get("Completed")));
					hashes.add(key);
				}
			}
		}
		transactions.sort(
				(tx1, tx2) -> Long.compare(tx2.getTransaction().getTimestamp(), tx1.getTransaction().getTimestamp()));

		// filter transactions
		panelFilter.setTransactions(transactions);

		List<StatusTransaction> filteredTransactions = panelFilter.getFilteredTransactions();
		proListModel.setData(filteredTransactions);

	}

	class ListDefaultListModel extends DefaultListModel {
		void setData(List<StatusTransaction> filteredTransactions) {
			List<Transaction> list = new ArrayList<>();
			for (StatusTransaction tt : filteredTransactions) {
				list.add(tt.getTransaction());
			}

			if (requestCount >= list.size()) {
				requestCount = list.size();
			}

			list = list.size() > requestCount ? list.subList(0, requestCount) : list;

			proListModel.clear();
			for (Transaction tx : list) {
				proListModel.addElement(tx);
			}

			transactionsList.repaint();
		}
	}

	private static int NUMBER_OF_TRANSACTIONS = 5;
	{
			NUMBER_OF_TRANSACTIONS=500;
	}
	private int requestCount = NUMBER_OF_TRANSACTIONS;
	private JList<Transaction> transactionsList;
	private ListDefaultListModel proListModel = new ListDefaultListModel();

	/**
	 * Refreshes this panel.
	 */
	protected void refresh() {
		List<StatusTransaction> transactions = new ArrayList<>();

		// add pending transactions
		transactions.addAll(
				gui.getKernel().getPendingManager().getPendingTransactions().parallelStream().filter(pendingTx -> {
					for (WalletAccount acc : model.getAccounts()) {
						if (Arrays.equals(acc.getAddress(), pendingTx.transaction.getFrom())
								|| Arrays.equals(acc.getAddress(), pendingTx.transaction.getTo())) {
							return true;
						}
					}
					return false;
				}).map(pendingTx -> new StatusTransaction(pendingTx.transaction, GuiMessages.get("Pending")))
						.collect(Collectors.toList()));

		// add completed transactions
		Set<ByteArray> hashes = new HashSet<>();
		for (WalletAccount acc : model.getAccounts()) {
			for (Transaction tx : acc.getTransactions()) {
				ByteArray key = ByteArray.of(tx.getHash());
				if (!hashes.contains(key)) {
					transactions.add(new StatusTransaction(tx, GuiMessages.get("Completed")));
					hashes.add(key);
				}
			}
		}
		transactions.sort(
				(tx1, tx2) -> Long.compare(tx2.getTransaction().getTimestamp(), tx1.getTransaction().getTimestamp()));

		/*
		 * update model
		 */
		Transaction tx = getSelectedTransaction();

		// filter transactions
		panelFilter.setTransactions(transactions);
		List<StatusTransaction> filteredTransactions = panelFilter.getFilteredTransactions();
		tableModel.setData(filteredTransactions);

		if (tx != null) {
			for (int i = 0; i < filteredTransactions.size(); i++) {
				if (Arrays.equals(tx.getHash(), filteredTransactions.get(i).getTransaction().getHash())) {
					table.setRowSelectionInterval(table.convertRowIndexToView(i), table.convertRowIndexToView(i));
					break;
				}
			}
		}
	}

	/**
	 * Returns the selected transaction.
	 *
	 * @return
	 */
	protected Transaction getSelectedTransaction() {
		int row = table.getSelectedRow();
		return (row != -1) ? tableModel.getRow(table.convertRowIndexToModel(row)) : null;
	}

	static class StatusTransaction {
		private final String status;
		private final Transaction transaction;

		public StatusTransaction(Transaction transaction, String status) {

			this.transaction = transaction;
			this.status = status;
		}

		public String getStatus() {
			return status;
		}

		public Transaction getTransaction() {
			return transaction;
		}
	}
}
