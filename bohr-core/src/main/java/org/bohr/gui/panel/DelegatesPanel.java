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
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.RoundRectangle2D;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.basic.BasicPanelUI;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.bohr.Kernel;
import org.bohr.Network;
import org.bohr.config.Config;
import org.bohr.config.Constants;
import org.bohr.core.Amount;
import org.bohr.core.Blockchain;
import org.bohr.core.BlockchainImpl.ValidatorStats;
import org.bohr.core.PendingManager;
import org.bohr.core.Transaction;
import org.bohr.core.TransactionType;
import org.bohr.core.state.Delegate;
import org.bohr.core.state.DelegateState;
import org.bohr.gui.*;
import org.bohr.gui.dialog.DelegateDialog;
import org.bohr.gui.laf.*;
import org.bohr.gui.layout.TableLayout;
import org.bohr.gui.model.WalletAccount;
import org.bohr.gui.model.WalletDelegate;
import org.bohr.gui.model.WalletModel;
import org.bohr.gui.uiUtils.FontUtils;
import org.bohr.gui.uiUtils.LAFUtils;
import org.bohr.message.GuiMessages;
import org.bohr.message.LanguageChoiceConfig;
import org.bohr.util.Bytes;
import org.bohr.util.SystemUtil;
import org.bohr.util.exception.UnreachableException;

public class DelegatesPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private transient WalletModel model;

	private transient Kernel kernel;
	private transient Config config;

	private JTable table;
	private DelegatesTableModel tableModel;

	private JTextField textVote;
	private JTextField textUnvote;
	private JTextField textName;
	private JLabel labelSelectedDelegate;

	private JComboBox<AccountItem> selectFrom;

	public DelegatesPanel(BohrGui gui, JFrame frame) {
			initUI(gui, frame);// ui
	}

	private void initUI(BohrGui gui, JFrame frame) {
		this.model = gui.getModel();
		this.model.addListener(this);

		this.kernel = gui.getKernel();
		this.config = kernel.getConfig();

		JPanel panel = LAFUtils.createRoundRectanglePanel(new double[] { TableLayout.FILL, 306 },
				new double[] { TableLayout.FILL });
		panel.setBackground(new Color(0x272729));

		panel.add(getTablePanel(gui, frame), "0,0");
		panel.add(getInfoPanel(), "1,0");

		setLayout(new BorderLayout());
		add(panel);

		refreshAccounts();
		refreshDelegates();
	}

	private JPanel getTablePanel(BohrGui gui, JFrame frame) {
		JPanel panel = new JPanel(new BorderLayout());
		panel.setOpaque(false);

		tableModel = new DelegatesTableModel();
		table = new JTable(tableModel);
		table.setName("DelegatesTable");
		table.setBackground(new Color(0x272729));
		table.setForeground(new Color(0x666666));
		table.getTableHeader().setBackground(new Color(0x272729));
		table.setFillsViewportHeight(true);
		table.setGridColor(Color.WHITE);
		table.setSelectionBackground(new Color(0xffad00));
		table.setSelectionForeground(Color.WHITE);
		table.setRowHeight(45);
		table.setFont(FontUtils.getFont().deriveFont(Font.BOLD, 14F));

		table.setShowGrid(false);
		table.setShowVerticalLines(false);
		table.setShowHorizontalLines(false);

		table.setTableHeader(new JTableHeader(table.getColumnModel()) {
			@Override
			public Dimension getPreferredSize() {
				Dimension d = super.getPreferredSize();
				d.height = 35;
				return d;
			}
		});

		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.getTableHeader().setReorderingAllowed(false);

		table.setIntercellSpacing(new Dimension(0, 0));

		table.setOpaque(true);
		table.setBorder(new EmptyBorder(0, 0, 0, 0));


		JTableHeader tableHead = table.getTableHeader();

		tableHead.setOpaque(false);
		tableHead.setForeground(new Color(0xa2a6ae));
		tableHead.setBackground(new Color(0x272729));
		tableHead.setFont(tableHead.getFont().deriveFont(Font.PLAIN, 12F));
		tableHead.setDefaultRenderer(new TableHeadRender(JLabel.CENTER, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT,
				JLabel.LEFT, JLabel.LEFT, JLabel.LEFT));

		SwingUtil.setColumnWidths(table, 600, 0.05, 0.2, 0.2, 0.15, 0.15, 0.15, 0.1);
		table.setDefaultRenderer(Object.class, new TableRender(true, JLabel.CENTER, JLabel.LEFT, JLabel.LEFT,
				JLabel.LEFT, JLabel.LEFT, JLabel.LEFT, JLabel.LEFT));

		table.getSelectionModel().addListSelectionListener(event -> {
			if (!event.getValueIsAdjusting() && !table.getSelectionModel().isSelectionEmpty()) {
				updateSelectedDelegateLabel();
			}
		});

		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				JTable sourceTable = (JTable) me.getSource();
				Point p = me.getPoint();
				int row = sourceTable.rowAtPoint(p);
				if (me.getClickCount() == 2 && row != -1) {
					WalletDelegate d = tableModel.getRow(sourceTable.convertRowIndexToModel(row));
					if (d != null) {
						DelegateDialog dialog = new DelegateDialog(gui, frame, d);
						dialog.setVisible(true);
					}
				}
			}
		});

		// customized table sorter
		TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
		sorter.setComparator(0, SwingUtil.NUMBER_COMPARATOR);
		sorter.setComparator(3, SwingUtil.NUMBER_COMPARATOR);
		sorter.setComparator(4, SwingUtil.NUMBER_COMPARATOR);
		sorter.setComparator(6, SwingUtil.PERCENTAGE_COMPARATOR);
		table.setRowSorter(sorter);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.setOpaque(false);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		double[] colSize = { TableLayout.FILL };
		double[] rowSize = { 20, TableLayout.FILL, 20 };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		panel.setLayout(layout);

		panel.add(scrollPane, "0,1");

		return panel;
	}

	private JPanel getInfoPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(new Color(0x272729));
		panel.setUI(new BasicPanelUI() {
			@Override
			public void update(Graphics g, JComponent c) {
//				super.update(g, c);
				if (c.isOpaque()) {
					g.setColor(c.getBackground());
//					g.fillRect(0, 0, c.getWidth(), c.getHeight());
					if (g != null && g instanceof Graphics2D) {
						Graphics2D g2 = (Graphics2D) g;
						g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

						RoundRectangle2D.Double rect = new RoundRectangle2D.Double();
						int offset = 30;
						rect.setRoundRect(0 - offset, 0, c.getWidth() + offset, c.getHeight(), 30, 30);

						g2.fill(rect);
					}
				}
				paint(g, c);
			}
		});

		double[] colSize = { 25, TableLayout.FILL, 25 };
		double[] rowSize = { 20, 45, 30, TableLayout.PREFERRED, 15, 45, 10, 45, 20, TableLayout.PREFERRED, 40,
				TableLayout.PREFERRED, 15, 45, 15, 45 };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		panel.setLayout(layout);

		selectFrom = new JComboBox<>();
		selectFrom.setName("selectFrom");
		selectFrom.setActionCommand(Action.SELECT_ACCOUNT.name());
		selectFrom.addActionListener(this);
		selectFrom.setUI(new DefaultComboBoxUI());
		panel.add(selectFrom, "1,1");

		labelSelectedDelegate = new JLabel(GuiMessages.get("PleaseSelectDelegate"));
		labelSelectedDelegate.setName("SelectedDelegateLabel");
		labelSelectedDelegate.setForeground(new Color(0xffffff));
		labelSelectedDelegate.setHorizontalAlignment(JLabel.LEFT);

		if (LanguageChoiceConfig.is_choose_en()) {
			FontUtils.setBOLDFont(labelSelectedDelegate, 14);
		}

		panel.add(labelSelectedDelegate, "1,3");

		panel.add(getVotePanel(), "1,5");

		panel.add(getUnvotePanel(), "1,7");

		panel.add(getNotePanel(), "1,9");

		JLabel regLabel = new JLabel(GuiMessages.get("RegisterAsDelegate"));
		regLabel.setForeground(new Color(0xffffff));
		regLabel.setHorizontalAlignment(JLabel.LEFT);
		FontUtils.setBOLDFont(regLabel, 16);
		panel.add(regLabel, "1,11");

		textName = SwingUtil.textFieldWithCopyPastePopup();

		textName.setToolTipText(GuiMessages.get("DelegateName"));
		textName.setName("textName");

		textName.setColumns(10);
		textName.setActionCommand(Action.DELEGATE.name());
		textName.addActionListener(this);

		new PlaceHolder(GuiMessages.get("DelegateName"), textName);

		textName.setUI(new DefaultTextFieldUI());
		panel.add(textName, "1,13");

		JButton btnDelegate = SwingUtil.createDefaultButton(GuiMessages.get("reg"), this, Action.DELEGATE);
		btnDelegate.setName("btnDelegate");
		btnDelegate.setToolTipText(GuiMessages.get("RegisterAsDelegateToolTip",
				SwingUtil.formatAmount(config.spec().minDelegateBurnAmount())));
		btnDelegate.setUI(new RoundRectButtonUI(new Color(0xffad00)));

		panel.add(btnDelegate, "1,15");

		return panel;
	}

	private JPanel getVotePanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL, 1, 60),
				LAFUtils.getDoubleArray(TableLayout.FILL));

		textVote = SwingUtil.textFieldWithCopyPastePopup();
		textVote.setName("textVote");
		textVote.setToolTipText(GuiMessages.get("NumVotes"));
		textVote.setColumns(10);
		textVote.setActionCommand(Action.VOTE.name());
		textVote.addActionListener(this);
//		new PlaceHolder(GuiMessages.get("NumVotes"), textVote);
		textVote.setUI(new HalfTextFieldUI());

		JButton btnVote = SwingUtil.createDefaultButton(GuiMessages.get("Vote"), this, Action.VOTE);
		btnVote.setName("btnVote");
		btnVote.setUI(new HalfButtonUI());
		FontUtils.setBOLDFont(btnVote, 14);

		panel.add(textVote, "0,0");
		panel.add(btnVote, "2,0");

		return panel;
	}

	private JPanel getUnvotePanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL, 1, 60),
				LAFUtils.getDoubleArray(TableLayout.FILL));

		textUnvote = SwingUtil.textFieldWithCopyPastePopup();
		textUnvote.setName("textUnvote");
		textUnvote.setToolTipText(GuiMessages.get("NumVotes"));
		textUnvote.setColumns(10);
		textUnvote.setActionCommand(Action.UNVOTE.name());
		textUnvote.addActionListener(this);
//		new PlaceHolder(GuiMessages.get("NumVotes"), textUnvote);
		textUnvote.setUI(new HalfTextFieldUI());

		JButton btnUnvote = SwingUtil.createDefaultButton(GuiMessages.get("Cancel")/*  */, this, Action.UNVOTE);
		btnUnvote.setName("btnUnvote");
		btnUnvote.setUI(new HalfButtonUI(0x9297a0));
		FontUtils.setBOLDFont(btnUnvote, 14);

		panel.add(textUnvote, "0,0");
		panel.add(btnUnvote, "2,0");

		return panel;
	}

	private JPanel getNotePanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(16, 8, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED));

		JLabel label = new JLabel(GuiMessages.get("DelegateRegistrationNoteHtml",
				//SwingUtil.formatAmountNoUnit(config.spec().minDelegateBurnAmount()),
				SwingUtil.formatAmount(config.spec().minTransactionFee())));
		label.setForeground(new Color(0xffffff));
		FontUtils.setBOLDFont(label, 14);

		ImageIcon buttonIcon = SwingUtil.loadImage("gth", 16, 16);

		JLabel iconLabel = new JLabel(buttonIcon);

		panel.add(iconLabel, "0,0,F,T");
		panel.add(label, "2,0");

		return panel;
	}


	class DelegatesTableModel extends AbstractTableModel {

		private static final long serialVersionUID = 1L;

		private String[] columnNames=null;
		{

				columnNames = new String[] { GuiMessages.get("Num"), GuiMessages.get("Delegate"),
						GuiMessages.get("Address"), GuiMessages.get("Votes"), GuiMessages.get("VotesFromMe"),
						GuiMessages.get("Status"), GuiMessages.get("Rate") };
		}

		private transient List<WalletDelegate> delegates;

		public DelegatesTableModel() {
			this.delegates = Collections.emptyList();
		}

		public void setData(List<WalletDelegate> delegates) {
			this.delegates = delegates;
			this.fireTableDataChanged();
		}

		public WalletDelegate getRow(int row) {
			if (row >= 0 && row < delegates.size()) {
				return delegates.get(row);
			}

			return null;
		}

		@Override
		public int getRowCount() {
			return delegates.size();
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
			WalletDelegate d = delegates.get(row);

			switch (column) {
			case 0:
				return SwingUtil.formatNumber(row + 1);
			case 1:
				return d.getNameString();
			case 2:
				return d.getAddressBase58();
			case 3:
				return SwingUtil.formatVote(d.getVotes());
			case 4:
				return SwingUtil.formatVote(d.getVotesFromMe());
			case 5:
				return d.isValidator() ? GuiMessages.get("Validator") : GuiMessages.get("Delegate");
			case 6:
				return SwingUtil.formatPercentage(d.getRate());
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
				refreshAccounts();
				refreshDelegates();
			break;
		case SELECT_ACCOUNT:
				refreshDelegates();
			break;
		case VOTE:
		case UNVOTE:
				voteOrUnvote(action);
			break;
		case DELEGATE:
			delegate();
			break;
		default:
			throw new UnreachableException();
		}
	}

	/**
	 * Refreshes account list.
	 */
	protected void refreshAccounts() {
		List<WalletAccount> list = model.getAccounts();

		// record selected account
		AccountItem selected = (AccountItem) selectFrom.getSelectedItem();

		// update account list if user is not interacting with it
		if (!selectFrom.isPopupVisible()) {
			selectFrom.removeAllItems();
			for (WalletAccount aList : list) {
				selectFrom.addItem(new AccountItem(aList));
			}

			// recover selected account
			if (selected != null) {
				for (int i = 0; i < list.size(); i++) {
					if (Arrays.equals(list.get(i).getAddress(), selected.account.getAddress())) {
						selectFrom.setSelectedIndex(i);
						break;
					}
				}
			}
		}
	}

	/**
	 * Refreshes delegate list.
	 */
	protected void refreshDelegates() {
		List<WalletDelegate> delegates = model.getDelegates();
		delegates.sort((d1, d2) -> {
			if (d1.getVotes() != d2.getVotes()) {
				return d2.getVotes().compareTo(d1.getVotes());
			}

			return d1.getNameString().compareTo(d2.getNameString());
		});

		WalletAccount acc = getSelectedAccount();
		if (acc != null) {
			byte[] voter = acc.getKey().toAddress();
			Blockchain chain = kernel.getBlockchain();
			DelegateState ds = chain.getDelegateState();
			for (WalletDelegate wd : delegates) {
				Amount vote = ds.getVote(voter, wd.getAddress());
				wd.setVotesFromMe(vote);

				ValidatorStats s = chain.getValidatorStats(wd.getAddress());
				wd.setNumberOfBlocksForged(s.getBlocksForged());
				wd.setNumberOfTurnsHit(s.getTurnsHit());
				wd.setNumberOfTurnsMissed(s.getTurnsMissed());
			}
		}

		/*
		 * update table model
		 */
		Delegate d = getSelectedDelegate();
		tableModel.setData(delegates);

		if (d != null) {
			for (int i = 0; i < delegates.size(); i++) {
				if (Arrays.equals(d.getAddress(), delegates.get(i).getAddress())) {
					table.setRowSelectionInterval(table.convertRowIndexToView(i), table.convertRowIndexToView(i));
					break;
				}
			}
		}
	}

	/**
	 * Handles vote or unvote.
	 *
	 * @param action
	 */
	protected void voteOrUnvote(Action action) {
		WalletAccount a = getSelectedAccount();
		WalletDelegate d = getSelectedDelegate();
		AccountItem selected = (AccountItem) selectFrom.getSelectedItem();
		String v = action.equals(Action.VOTE) ? textVote.getText() : textUnvote.getText();
		Amount value;
		try {
			value = SwingUtil.parseAmount(v);
		} catch (ParseException ex) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("EnterValidNumberOfVotes"));
			return;
		}
		Amount fee = config.spec().minTransactionFee();

		if (a == null) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("SelectAccount"));
		} else if (d == null) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("SelectDelegate"));
		} else if (value.isNotPositive()) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("EnterValidNumberOfVotes"));
		} else {
			if (action == Action.VOTE) {
				Amount valueWithFee = value.add(fee);
				if (valueWithFee.greaterThan(a.getAvailable())) {
					JOptionPane.showMessageDialog(this,
							GuiMessages.get("InsufficientFunds", SwingUtil.formatAmount(valueWithFee)));
					return;
				}
				if (value.greaterThan(config.maxVoteAmount())) {
					JOptionPane.showMessageDialog(this,
							GuiMessages.get("InvalidVoteAmount", SwingUtil.formatAmount(config.maxVoteAmount())));
					return;
				}
				if (selected.voteCount >= config.maxVoteCount()) {
					JOptionPane.showMessageDialog(this, GuiMessages.get("InvalidVoteCount", config.maxVoteCount()));
					return;
				}

				if (a.getAvailable().subtract(valueWithFee).lessThan(fee)) {
					int ret = JOptionPane.showConfirmDialog(this, GuiMessages.get("NotEnoughBalanceToUnvote"),
							GuiMessages.get("ConfirmDelegateRegistration"), JOptionPane.YES_NO_OPTION);
					if (ret != JOptionPane.YES_OPTION) {
						return;
					}
				}
			} else {
				if (fee.greaterThan(a.getAvailable())) {
					JOptionPane.showMessageDialog(this,
							GuiMessages.get("InsufficientFunds", SwingUtil.formatAmount(fee)));
					return;
				}

				if (value.greaterThan(a.getLocked())) {
					JOptionPane.showMessageDialog(this,
							GuiMessages.get("InsufficientLockedFunds", SwingUtil.formatAmount(value)));
					return;
				}

				// check that user has voted more than amount to unvote
				if (value.greaterThan(d.getVotesFromMe())) {
					JOptionPane.showMessageDialog(this, GuiMessages.get("InsufficientVotes"));
					return;
				}
			}

			TransactionType type = action.equals(Action.VOTE) ? TransactionType.VOTE : TransactionType.UNVOTE;
			byte[] to = d.getAddress();
			byte[] data = Bytes.EMPTY_BYTES;
			PendingManager.ProcessingResult result = TransactionSender.send(kernel, a, type, to, value, fee, data);
			handleTransactionResult(result);
		}
	}

	/**
	 * Handles delegate registration.
	 */
	protected void delegate() {
		WalletAccount a = getSelectedAccount();
		String name = textName.getText();
		if (a == null) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("SelectAccount"));
		}
		//
		else if (
				!name.matches("[_a-z0-9A-Z]{3,16}")
		) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("AccountNameError"));
		} else if (a.getAvailable()
				.lessThan(Amount.ZERO)) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("InsufficientFunds", SwingUtil
					.formatAmount(Amount.ZERO)));
		} else {
			// validate delegate address
			DelegateState delegateState = kernel.getBlockchain().getDelegateState();
			if (delegateState.getDelegateByAddress(a.getAddress()) != null) {
				JOptionPane.showMessageDialog(this, GuiMessages.get("DelegateRegistrationDuplicatedAddress"),
						GuiMessages.get("ErrorDialogTitle"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			// validate delegate name
			if (delegateState.getDelegateByName(Bytes.of(name)) != null) {
				JOptionPane.showMessageDialog(this, GuiMessages.get("DelegateRegistrationDuplicatedName"),
						GuiMessages.get("ErrorDialogTitle"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			// delegate system requirements
			if (config.network() == Network.MAINNET && !SystemUtil.bench()) {
				JOptionPane.showMessageDialog(this, GuiMessages.get("computerNotQualifiedDelegate"),
						GuiMessages.get("ErrorDialogTitle"), JOptionPane.ERROR_MESSAGE);
				return;
			}

			// confirm burning amount
			if (JOptionPane.showConfirmDialog(this,
					GuiMessages.get("DelegateRegistrationInfo",
							SwingUtil.formatAmount(Amount.ZERO)),
					GuiMessages.get("ConfirmDelegateRegistration"),
					JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
				return;
			}

			TransactionType type = TransactionType.DELEGATE;
			byte[] to = Constants.DELEGATE_BURN_ADDRESS;
			Amount value = config.spec().minDelegateBurnAmount();
			Amount fee = config.spec().minTransactionFee();
			byte[] data = Bytes.of(name);

			PendingManager.ProcessingResult result = TransactionSender.send(kernel, a, type, to, value, fee, data);
			handleTransactionResult(result);
		}
	}

	/**
	 * Handles pending transaction result.
	 *
	 * @param result
	 */
	protected void handleTransactionResult(PendingManager.ProcessingResult result) {
		if (result.error == null) {
			JOptionPane.showMessageDialog(this, GuiMessages.get("TransactionSent", 3),
					GuiMessages.get("SuccessDialogTitle"), JOptionPane.INFORMATION_MESSAGE);
			clear();
		} else {
			JOptionPane.showMessageDialog(this, GuiMessages.get("TransactionFailed", result.error.toString()),
					GuiMessages.get("ErrorDialogTitle"), JOptionPane.ERROR_MESSAGE);
		}
	}

	/**
	 * Updates the selected delegate label.
	 */
	protected void updateSelectedDelegateLabel() {
		Delegate d = getSelectedDelegate();
		if (d != null) {
			labelSelectedDelegate.setText(GuiMessages.get("SelectedDelegate", d.getNameString()));
		}
	}

	/**
	 * Returns the selected account.
	 *
	 * @return
	 */
	protected WalletAccount getSelectedAccount() {
		int idx = selectFrom.getSelectedIndex();
		return (idx == -1) ? null : model.getAccounts().get(idx);
	}

	/**
	 * Returns the selected delegate.
	 *
	 * @return
	 */
	protected WalletDelegate getSelectedDelegate() {
		int row = table.getSelectedRow();
		return (row == -1) ? null : tableModel.getRow(table.convertRowIndexToModel(row));
	}

	/**
	 * Clears all input fields
	 */
	protected void clear() {

		textVote.setText("");
		textUnvote.setText("");
		//textName.setText("");
	}

	/**
	 * Represents an item in the account drop list.
	 */
	protected static class AccountItem {
		final WalletAccount account;
		final String name;
		final int voteCount;
		final int unvoteCount;

		public AccountItem(WalletAccount a) {
			this.account = a;
			this.name = a.getName().orElse(SwingUtil.getAddressAbbr(a.getAddress(),8)) + ", " // alias or abbreviation
					+ SwingUtil.formatAmount(account.getAvailable());

			int voteCountTmp = 0;
			int unvoteCountTmp = 0;
			for (Transaction tx : a.getTransactions()) {
				if (tx.getType().equals(TransactionType.VOTE)) {
					voteCountTmp++;
				} else if (tx.getType().equals(TransactionType.UNVOTE)) {
					unvoteCountTmp++;
				}
			}
			this.voteCount = voteCountTmp;
			this.unvoteCount = unvoteCountTmp;
		}

		@Override
		public String toString() {
			return this.name;
		}
	}

}
