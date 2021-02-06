/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.bohr.Kernel;
import org.bohr.core.Wallet;
import org.bohr.gui.Action;
import org.bohr.gui.BohrGui;
import org.bohr.gui.SwingUtil;
import org.bohr.gui.laf.EmptyBlueRectButtonUI;
import org.bohr.gui.laf.RoundRectButtonUI;
import org.bohr.gui.laf.TableHeadRender;
import org.bohr.gui.laf.TableRender;
import org.bohr.gui.layout.AbsLayoutManager;
import org.bohr.gui.layout.TableLayout;
import org.bohr.gui.model.WalletAccount;
import org.bohr.gui.model.WalletModel;
import org.bohr.gui.uiUtils.FontUtils;
import org.bohr.gui.uiUtils.LAFUtils;
import org.bohr.message.GuiMessages;
import org.bohr.message.LanguageChoiceConfig;
import org.bohr.util.exception.UnreachableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.WriterException;

public class ReceivePanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static final Logger logger = LoggerFactory.getLogger(ReceivePanel.class);

	private static String[] columnNames ;
	
	static {
			columnNames = new String[] { GuiMessages.get("Num"), GuiMessages.get("Name"), GuiMessages.get("Address"),
					GuiMessages.get("Available") + "(Bohr)", GuiMessages.get("Locked") + "(Bohr)" };
	}

	private static final int QR_SIZE = 200;

	private transient BohrGui gui;
	private transient WalletModel model;
	private transient Kernel kernel;

	private JTable table;
	private ReceiveTableModel tableModel;
	private JLabel qr;

	private JPanel tablePanel;

	private JTextArea textArea;

	public ReceivePanel(BohrGui gui) {
			initUI(gui);
	}

	private void initUI(BohrGui gui) {
		this.gui = gui;
		this.model = gui.getModel();
		this.kernel = gui.getKernel();

		this.model.addListener(this);

		JPanel panel = LAFUtils.createRoundRectanglePanel(new double[] { TableLayout.FILL, 306 },
				new double[] { TableLayout.FILL });
		panel.setBackground(new Color(0x272729));

		panel.add(getTablePanel(), "0,0");
		panel.add(getInfoPanel(), "1,0");

		setLayout(new BorderLayout());
		add(panel);

		refresh();
	}

	private JPanel getTablePanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		panel.setBackground(new Color(0x272729));

		tablePanel = panel;
		tablePanel.setBackground(new Color(0x272729));


		tableModel = new ReceiveTableModel();
		table = new JTable(tableModel);
		table.setName("accountsTable");
		table.setBackground(new Color(0x272729));
		table.setFillsViewportHeight(true);
		table.setGridColor(Color.WHITE);
		table.setSelectionBackground(new Color(0xFFFF00));
		table.setSelectionForeground(new Color(0x666666));
		table.setForeground(new Color(0x666666));
		table.setRowHeight(45);
		table.setFont(FontUtils.getFont().deriveFont(Font.BOLD, 14F));

		table.setShowGrid(false);
		table.setShowVerticalLines(false);
		table.setShowHorizontalLines(false);

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);

		table.setIntercellSpacing(new Dimension(0, 0));

		table.setOpaque(true);
		table.setBorder(new EmptyBorder(0, 0, 0, 0));

		table.setTableHeader(new JTableHeader(table.getColumnModel()) {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 35;
				return d;
			}
		});

		JTableHeader tableHead = table.getTableHeader();
		tableHead.setPreferredSize(new Dimension(10000, 24));

		tableHead.setOpaque(false);
		tableHead.setForeground(new Color(0xa2a6ae));
		tableHead.setBackground(new Color(0x272729));
		tableHead.setFont(tableHead.getFont().deriveFont(Font.PLAIN, 12F));
		tableHead.setDefaultRenderer(
				new TableHeadRender(JLabel.CENTER, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT));

		SwingUtil.setColumnWidths(table, 575, 0.1, 0.13, 0.25, 0.19, 0.19);
		table.setDefaultRenderer(Object.class,
				new TableRender(JLabel.CENTER, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT));

		table.setPreferredScrollableViewportSize(new Dimension(10000, 50));

		table.getSelectionModel().addListSelectionListener(
				ev -> actionPerformed(new ActionEvent(ReceivePanel.this, 0, Action.SELECT_ACCOUNT.name())));

		// customized table sorter
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		sorter.setComparator(0, SwingUtil.NUMBER_COMPARATOR);
		sorter.setComparator(3, SwingUtil.VALUE_COMPARATOR);
		sorter.setComparator(4, SwingUtil.VALUE_COMPARATOR);
		table.setRowSorter(sorter);

		final JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setBackground(new Color(0x272729));
		scrollPane.setOpaque(false);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		final JButton buttonNewAccount = SwingUtil.createDefaultButton("＋" + GuiMessages.get("CreateNewAccount"), this,
				Action.NEW_ACCOUNT);
		buttonNewAccount.setName("buttonNewAccount");

		panel.setLayout(new AbsLayoutManager() {
			public void layoutContainer(java.awt.Container parent) {
				Dimension panelDim = parent.getSize();

				int panelWidth = panelDim.width;
				int panelHeight = panelDim.height;

				int top = 20;
				int bottom = 20;

				panelHeight = panelHeight - (top + bottom);

				int spaceHeight = 25;

				int tableHeaderHeigh = table.getTableHeader().getHeight();
				int tableContentHeight = table.getModel().getRowCount() * table.getRowHeight() + 5;

				int tableMinHeight = tableHeaderHeigh + table.getRowHeight() * 3 + 5;

				int currTableHeight = tableHeaderHeigh + tableContentHeight;

				int tableHeight = Math.max(tableMinHeight, currTableHeight);

				Dimension buttonDim = buttonNewAccount.getPreferredSize();
				int buttonHeight = buttonDim.height;
				buttonHeight = 40;

				int totalHeight = tableHeight + spaceHeight + buttonHeight;

				if (totalHeight > panelHeight) {
					scrollPane.setBounds(0, top, panelWidth, panelHeight - buttonHeight - spaceHeight);
					buttonNewAccount.setBounds(30, panelHeight - buttonHeight + bottom, panelWidth - 30 - 26,
							buttonHeight);
				} else {
					scrollPane.setBounds(0, top, panelWidth, tableHeight);
					buttonNewAccount.setBounds(30, top + tableHeight + spaceHeight, panelWidth - 30 - 26, buttonHeight);
				}
			};
		});

		panel.add(scrollPane);
		panel.add(buttonNewAccount);

		buttonNewAccount.setUI(new EmptyBlueRectButtonUI(new Color(0xFFAD00)));

		return panel;
	}

	private JPanel getInfoPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0xFFFF00));
		panel.setUI(new BasicPanelUI() {
			@Override
			public void update(Graphics g, JComponent c) {
//				super.update(g, c);
				if (c.isOpaque()) {
					g.setColor(c.getBackground());
//					g.fillRect(0, 0, c.getWidth(), c.getHeight());
					if (g != null && g instanceof Graphics2D) {
						Graphics2D g2 = (Graphics2D) g;
						//
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

						//
						RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
						int offset = 30;
						rect.setRoundRect(0 - offset, 0, c.getWidth() + offset, c.getHeight(), 20, 20);

						g2.fill(rect);
					}
				}
				paint(g, c);
			}
		});

		double[] colSize = { TableLayout.FILL };
		double[] rowSize = { 20, TableLayout.PREFERRED, 50, TableLayout.PREFERRED, TableLayout.FILL };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		panel.setLayout(layout);

		panel.add(getUserImagePanel(), "0,1,C,F");
		panel.add(getButtonPanel(), "0,3,C,F");

		return panel;
	}

	private JPanel getUserImagePanel() {
		JPanel panel = LAFUtils.createRoundRectanglePanel(LAFUtils.getDoubleArray(23, 200, 23),
				LAFUtils.getDoubleArray(30, 200, 40, 42, 15, TableLayout.PREFERRED, 30));

		qr = new JLabel("");
		qr.setIcon(SwingUtil.emptyImage(QR_SIZE, QR_SIZE));
//		qr.setBorder(new LineBorder(Color.LIGHT_GRAY));
		panel.add(qr, "1,1");

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setOpaque(false);
		textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN, 12F));
		textArea.setForeground(new Color(0x5f6071));
		textArea.setWrapStyleWord(true);
		textArea.setLineWrap(true);

		panel.add(textArea, "1,3");

		JButton btnCopyAddress = SwingUtil.createDefaultButton(GuiMessages.get("CopyAddress"), this,
				Action.COPY_ADDRESS);
		btnCopyAddress.setName("btnCopyAddress");
		LAFUtils.setNotFillDefaultButtonUI(btnCopyAddress);

		panel.add(btnCopyAddress, "1,5");

		btnCopyAddress.setText(null);

		btnCopyAddress.setVerticalAlignment(AbstractButton.CENTER);
		btnCopyAddress.setHorizontalAlignment(AbstractButton.CENTER);

		btnCopyAddress.setHorizontalTextPosition(AbstractButton.CENTER);
		btnCopyAddress.setVerticalTextPosition(AbstractButton.CENTER);

		btnCopyAddress.setFocusPainted(false);
		btnCopyAddress.setFocusable(false);
		btnCopyAddress.setOpaque(false);
		btnCopyAddress.setContentAreaFilled(false);

		btnCopyAddress.setBorder(null);

		Dimension dimension = new Dimension(100, 24);

		if (LanguageChoiceConfig.is_choose_en()) {
			dimension = new Dimension(150, 24);
		}
		btnCopyAddress.setSize(dimension);
		btnCopyAddress.setPreferredSize(dimension);
		btnCopyAddress.setMaximumSize(dimension);
		btnCopyAddress.setMinimumSize(dimension);

		ImageIcon buttonIcon = SwingUtil.loadImage("copy", 24, 24);
		ImageIcon selectIcon = LAFUtils.createEmptyIconImage(dimension.width, dimension.height);
		ImageIcon selectNoIcon = LAFUtils.createEmptyIconImage(dimension.width, dimension.height);
		Font font = FontUtils.getFont().deriveFont(Font.BOLD, 14);
		String name = GuiMessages.get("CopyAddress");
		ImageIcon noSelectIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, buttonIcon, "ffad00", name, "ffad00", font);
		ImageIcon rolloverIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, buttonIcon, "ffa200", name, "ffa200", font);
		ImageIcon pressedIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, buttonIcon, "FAD891", name, "FAD891", font);

		ImageIcon select_icon = LAFUtils.getButtonImageIcon1(selectIcon, buttonIcon, "FAD891", name, "FAD891", font);

		btnCopyAddress.setIcon(noSelectIcon);//

		btnCopyAddress.setRolloverIcon(rolloverIcon);//

		btnCopyAddress.setRolloverSelectedIcon(select_icon);//
		btnCopyAddress.setSelectedIcon(select_icon);//

		btnCopyAddress.setPressedIcon(pressedIcon);//

		btnCopyAddress.setDisabledIcon(select_icon);//
		btnCopyAddress.setDisabledSelectedIcon(select_icon);//

		return panel;
	}

	private JPanel getButtonPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL, 126, 15, 126, TableLayout.FILL),
				LAFUtils.getDoubleArray(48));

		JButton btnAddressBook = SwingUtil.createDefaultButton(GuiMessages.get("AddressBook"), this,
				Action.SHOW_ADDRESS_BOOK);
		btnAddressBook.setName("btnAddressBook");
		btnAddressBook.setUI(new RoundRectButtonUI(new Color(0xFFAD00)));

		JButton btnDeleteAddress = SwingUtil.createDefaultButton(GuiMessages.get("DeleteAccount"), this,
				Action.DELETE_ACCOUNT);
		btnDeleteAddress.setName("btnDeleteAddress");
		btnDeleteAddress.setUI(new RoundRectButtonUI(new Color(0xFFAD00)));

		panel.add(btnAddressBook, "1,0");
		panel.add(btnDeleteAddress, "3,0");

		return panel;
	}

	private static class ReceiveTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private transient List<WalletAccount> data;

		public ReceiveTableModel() {
			this.data = Collections.emptyList();
		}

		public void setData(List<WalletAccount> data) {
			this.data = data;
			this.fireTableDataChanged();
		}

		public WalletAccount getRow(int row) {
			if (row >= 0 && row < data.size()) {
				return data.get(row);
			}

			return null;
		}

		@Override
		public int getRowCount() {
			return data.size();
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
			WalletAccount acc = data.get(row);

			switch (column) {
			case 0:
				return SwingUtil.formatNumber(row);
			case 1:
				return acc.getName().orElse("");
			case 2:
					return acc.getKey().toAddressBase58();
			case 3:
					return SwingUtil.formatAmountNoUnit(acc.getAvailable());
			case 4:
					return SwingUtil.formatAmountNoUnit(acc.getLocked());
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
				refresh();
			break;
		case SELECT_ACCOUNT:
				selectAccount();
			break;
		case COPY_ADDRESS:
				copyAddress();
			break;
		case NEW_ACCOUNT:
				newAccount();
			break;
		case DELETE_ACCOUNT:
				deleteAccount();
			break;
		case SHOW_ADDRESS_BOOK:
				showAddressBook();
			break;
		default:
			throw new UnreachableException();
		}
	}

	/**
	 * Processes the REFRESH event.
	 */
	protected void refresh() {
		List<WalletAccount> accounts = model.getAccounts();

		/*
		 * update table model
		 */
		WalletAccount acc = getSelectedAccount();
		tableModel.setData(accounts);

		if (acc != null) {
			for (int i = 0; i < accounts.size(); i++) {
				if (Arrays.equals(accounts.get(i).getKey().toAddress(), acc.getKey().toAddress())) {
					table.setRowSelectionInterval(table.convertRowIndexToView(i), table.convertRowIndexToView(i));
					break;
				}
			}
		} else if (!accounts.isEmpty()) {
			table.setRowSelectionInterval(0, 0);
		}

		selectAccount();

		updataTablePanel();
	}

	/**
	 * Processes the SELECT_ACCOUNT event.
	 */
	protected void selectAccount() {
		try {
			WalletAccount acc = getSelectedAccount();

			if (acc != null) {
				BufferedImage bi = SwingUtil.createQrImage("bohr://" +  acc.getKey().toAddressBase58(),
						QR_SIZE, QR_SIZE);
				qr.setIcon(new ImageIcon(bi));
			} else {
				qr.setIcon(SwingUtil.emptyImage(QR_SIZE, QR_SIZE));
			}

			//
			if (textArea != null) {
				if (acc != null) {
					textArea.setText(acc.getKey().toAddressBase58());
				} else {
					textArea.setText("");
				}
			}

		} catch (WriterException exception) {
			logger.error("Unable to generate QR code", exception);
		}

	}

	/**
	 * Processes the COPY_ADDRESS event
	 */
	protected void copyAddress() {
		WalletAccount acc = getSelectedAccount();
		if (acc == null) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("SelectAccount"));
		} else {
			String address =  acc.getKey().toAddressBase58();
			StringSelection stringSelection = new StringSelection(address);
			Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			clipboard.setContents(stringSelection, null);

			JOptionPane.showMessageDialog(this, GuiMessages.get("AddressCopied", address));
		}
	}

	/**
	 * Process the RENAME_ACCOUNT event
	 */
	protected void showAddressBook() {
		gui.getAddressBookDialog().setVisible(true);
	}

	/**
	 * Processes the NEW_ACCOUNT event.
	 */
	protected void newAccount() {
		Wallet wallet = kernel.getWallet();

		if (gui.isHdWalletEnabled().orElse(BohrGui.ENABLE_HD_WALLET_BY_DEFAULT)) {
			wallet.addAccountWithNextHdKey();
		} else {
			wallet.addAccountRandom();
		}

		if (wallet.flush()) {
			gui.updateModel();
			JOptionPane.showMessageDialog(this, GuiMessages.get("NewAccountCreated"));
			updataTablePanel();
		} else {
			JOptionPane.showMessageDialog(this, GuiMessages.get("WalletSaveFailed"));
		}
	}

	private void updataTablePanel() {
		if (tablePanel != null) {
			tablePanel.validate();
		}
	}

	/**
	 * Processes the DELETE_ACCOUNT event.
	 */
	protected void deleteAccount() {
		WalletAccount acc = getSelectedAccount();
		if (acc == null) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("SelectAccount"));
		} else {
			int count=table.getModel().getRowCount();
			if(count>1) {
				int ret = JOptionPane.showConfirmDialog(this, GuiMessages.get("ConfirmDeleteAccount"),
						GuiMessages.get("DeleteAccount"), JOptionPane.YES_NO_OPTION);
				
				if (ret == JOptionPane.OK_OPTION) {
					Wallet wallet = kernel.getWallet();
					wallet.removeAccount(acc.getKey());
					wallet.flush();
					
					gui.updateModel();
					
					JOptionPane.showMessageDialog(this, GuiMessages.get("AccountDeleted"));
					
					updataTablePanel();
				}
			}else {
			}
		}
	}

	/**
	 * Returns the selected account.
	 *
	 * @return
	 */
	protected WalletAccount getSelectedAccount() {
		int row = table.getSelectedRow();
		return (row != -1) ? tableModel.getRow(table.convertRowIndexToModel(row)) : null;
	}

}
