/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.gui.panel;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractButton;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToggleButton;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import org.bohr.core.Block;
import org.bohr.core.Transaction;
import org.bohr.core.TransactionType;
import org.bohr.core.state.Delegate;
import org.bohr.crypto.Hex;
import org.bohr.gui.Action;
import org.bohr.gui.BohrGui;
import org.bohr.gui.MainFrame;
import org.bohr.gui.SwingUtil;
import org.bohr.gui.laf.DefaultButtonUI;
import org.bohr.gui.laf.RoundRectButtonUI;
import org.bohr.gui.laf.RoundRectPanel;
import org.bohr.gui.layout.TableLayout;
import org.bohr.gui.model.WalletAccount;
import org.bohr.gui.model.WalletModel;
import org.bohr.gui.uiUtils.FontUtils;
import org.bohr.gui.uiUtils.LAFUtils;
import org.bohr.message.GuiMessages;
import org.bohr.util.ByteArray;
import org.bohr.util.StringUtil;
import org.bohr.util.exception.UnreachableException;

import  org.bohr.gui.BohrGui;

public class HomePanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private static int NUMBER_OF_TRANSACTIONS = 5;
	{
		NUMBER_OF_TRANSACTIONS=500;
	}
	

	private static final EnumSet<TransactionType> FEDERATED_TRANSACTION_TYPES = EnumSet.of(TransactionType.COINBASE,
			TransactionType.TRANSFER, TransactionType.CALL, TransactionType.CREATE);

	private transient BohrGui gui;
	private transient WalletModel model;

	// Overview Table
	private JLabel bestBlockNum;
	private JLabel blockNum;
	private JLabel blockTime;
	private JLabel coinbase;
	private JLabel status;
	private JLabel available;
	private JLabel locked;
	private JLabel total;

	// Consensus Table
	private JLabel primaryValidator;
	private JLabel backupValidator;
	private JLabel nextValidator;
	private JLabel roundEndBlock;
	private JLabel roundEndTime;

	// Transactions Table
	private JPanel transactions;
	private JList<Transaction> transactionsList;
	private DefaultListModel<Transaction> proListModel = new DefaultListModel<Transaction>();

	public HomePanel(BohrGui gui) {
			initUI(gui);
	}

	private void initUI(BohrGui gui) {
		this.gui = gui;
		this.model = gui.getModel();
		this.model.addListener(this);

		double[] colSize = { TableLayout.FILL };
		double[] rowSize = { 215, 30, TableLayout.FILL };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		setLayout(layout);
		setOpaque(false);

		add(getTopPanel(), "0,0");
		add(getBottomPanel(), "0,2");
	}

	private JPanel getTopPanel() {
		
		int width=420+60;

		JPanel panel = LAFUtils.createPanel(new double[] { width, 40, TableLayout.FILL },
				new double[] { TableLayout.FILL });
		panel.add(getMoneyPanel(), "0,0");
		panel.add(getUserPanel(), "2,0");
		return panel;
	}

	private JPanel getMoneyPanel() {
		JPanel panel = LAFUtils.createRoundRectanglePanel(new double[] { 38, TableLayout.FILL, 31 },
				new double[] { 30, TableLayout.FILL, 20 });
		panel.setBackground(new Color(0x272729));
		JPanel showPanel = getShowMoneyPanel();

		panel.add(showPanel, "1,1");

		return panel;
	}

	private JPanel getShowMoneyPanel() {
		double[] colSize = { TableLayout.FILL };
		double[] rowSize = { TableLayout.PREFERRED, 15, TableLayout.PREFERRED, TableLayout.FILL };
		JPanel panel = LAFUtils.createPanel(colSize, rowSize);
		panel.setBackground(new Color(0x272729));

		JPanel moneyPanel = getTotalMoneyPanel();
		JPanel imgPanel = getImgPanel();

		panel.add(moneyPanel, "0,0");
		panel.add(imgPanel, "0,3");

		return panel;
	}

	private JPanel getTotalMoneyPanel() {
		double[] colSize = { TableLayout.PREFERRED, 8, TableLayout.PREFERRED, TableLayout.FILL };
		double[] rowSize = { TableLayout.PREFERRED };
		JPanel panel = LAFUtils.createPanel(colSize, rowSize);

		available = new JLabel();
		refAvailable();

		available.setFont(available.getFont().deriveFont(Font.BOLD, 32F));
		available.setForeground(new Color(0xffffff));
		panel.add(available, "0,0");

		JLabel unit = new JLabel("Bohr");
		unit.setBorder(new EmptyBorder(0, 0, 8, 0));
		unit.setFont(unit.getFont().deriveFont(Font.PLAIN, 10F));
		unit.setForeground(new Color(0xadaeb6));
		panel.add(unit, "2,0,L,B");

		return panel;
	}

	private void refAvailable() {
		available.setText(SwingUtil.formatAmountNoUnit(model.getTotalAvailable()));
		available.setToolTipText(SwingUtil.formatAmountNoUnit(model.getTotalAvailable()));
	}



	private void refLocked() {
		if(locked != null){
			locked.setText(SwingUtil.formatAmountNoUnit(model.getTotalLocked()));
			locked.setToolTipText(SwingUtil.formatAmountNoUnit(model.getTotalLocked()));
		}
	}

	private JPanel getImgPanel() {
		double[] colSize = { TableLayout.FILL };
		double[] rowSize = { TableLayout.FILL };
		JPanel panel = LAFUtils.createPanel(colSize, rowSize);

		int width=355;

		ImageIcon icon = SwingUtil.loadImage("money", width, 86);
		JLabel label = new JLabel(icon);
		panel.add(label, "0,0");

		return panel;
	}

	private JPanel getUserPanel() {
		JPanel panel = LAFUtils.createPanel(new double[] { TableLayout.FILL }, new double[] { 48, 15, 67, 15, 67 });

		panel.add(getButtonPanel(), "0,0");
		panel.add(getPublicKeyPanel(), "0,2");
		panel.add(getPrivateKeyPanel(), "0,4");

		return panel;
	}

	/**
	 * @return
	 */
	private String getCurrUserAddress() {
		String showText = Hex.encode0x(model.getAccounts().get(0).getKey().toAddress());
		return StringUtil.hexToBase58(showText);
	}

	/**
	 *
	 * @return
	 */
	private String getPrivateKey() {
		String showText = Hex.encode0x(model.getAccounts().get(0).getKey().getPrivateKey());
		return showText;
	}

	private String getPrivateKeyByLen() {
		String showText = SwingUtil.getAddressAbbr_new(model.getAccounts().get(0).getKey().getPrivateKey(), 8);
		return showText;
	}

	private String getPrivateKeyByPoint() {
		return "····························································";
	}

	private void copyAddress() {
		String address = getCurrUserAddress();
		StringSelection stringSelection = new StringSelection(address);
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
		JOptionPane.showMessageDialog(this, GuiMessages.get("AddressCopied", address));
	}

	private JPanel getPublicKeyPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 8, TableLayout.FILL));
		panel.setBackground(new Color(0x272729));
		JLabel info = LAFUtils.createLabel(GuiMessages.get("WalletAddress"), Font.PLAIN, 12F, 0xa6aab2);
		panel.add(info, "0,0,L,F");
		JPanel showPanel = new RoundRectPanel(new Color(0x272729));
		showPanel.setBackground(new Color(0x272729));
		panel.add(showPanel, "0,2");

		double[] colSize = LAFUtils.getDoubleArray(19, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED,
				21);
		double[] rowSize = LAFUtils.getDoubleArray(TableLayout.FILL);
		double[][] tableSize = new double[][] { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		showPanel.setLayout(layout);

		String showText = getCurrUserAddress();

		JLabel show = LAFUtils.createLabel(showText, Font.BOLD, 11F, 0x414257);
		showPanel.add(show, "1,0");

		JButton button = new JButton();
		button.setUI(new DefaultButtonUI() {
			@Override
			public boolean isFillBackGroup() {
				return false;
			}
		});
		showPanel.add(button, "3,0");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				copyAddress();
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

		Dimension dimension = new Dimension(65, 24);
		button.setSize(dimension);
		button.setPreferredSize(dimension);
		button.setMaximumSize(dimension);
		button.setMinimumSize(dimension);

		ImageIcon buttonIcon = SwingUtil.loadImage("copy", 24, 24);
		ImageIcon selectIcon = LAFUtils.createEmptyIconImage(dimension.width, dimension.height);
		ImageIcon selectNoIcon = LAFUtils.createEmptyIconImage(dimension.width, dimension.height);
		Font font = FontUtils.getFont().deriveFont(Font.BOLD, 14);
		String name = GuiMessages.get("Copy");
		ImageIcon noSelectIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, buttonIcon, null, name, "8E939D", font);
		ImageIcon rolloverIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, buttonIcon, "FFAD00", name, "FFAD00", font);
		ImageIcon pressedIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, buttonIcon, "4F74FE", name, "4F74FE", font);

		ImageIcon select_icon = LAFUtils.getButtonImageIcon1(selectIcon, buttonIcon, "ffff00", name, "ffff00", font);

		button.setIcon(noSelectIcon);

		button.setRolloverIcon(rolloverIcon);//

		button.setRolloverSelectedIcon(select_icon);//
		button.setSelectedIcon(select_icon);//

		button.setPressedIcon(pressedIcon);//

		button.setDisabledIcon(select_icon);//
		button.setDisabledSelectedIcon(select_icon);//

		return panel;
	}

	private JPanel getPrivateKeyPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 8, TableLayout.FILL));
		JLabel info = LAFUtils.createLabel(GuiMessages.get("PrivateKey"), Font.PLAIN, 12F, 0xa6aab2);
		panel.add(info, "0,0,L,F");
		JPanel showPanel = new RoundRectPanel(new Color(0x272729));
		panel.add(showPanel, "0,2");

		double[] colSize = LAFUtils.getDoubleArray(19, TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED,
				21);
		double[] rowSize = LAFUtils.getDoubleArray(TableLayout.FILL);
		double[][] tableSize = new double[][] { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		showPanel.setLayout(layout);

		final JLabel show = LAFUtils.createLabel(getPrivateKeyByPoint(), Font.BOLD, 14F, 0x414257);
		showPanel.add(show, "1,0");

		final JToggleButton button = new JToggleButton();
		showPanel.add(button, "3,0");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (button.isSelected()) {
					show.setText(getPrivateKeyByLen());
				} else {
					show.setText(getPrivateKeyByPoint());
				}
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

		Dimension dimension = new Dimension(30, 24);
		button.setSize(dimension);
		button.setPreferredSize(dimension);
		button.setMaximumSize(dimension);
		button.setMinimumSize(dimension);

		ImageIcon buttonIcon = SwingUtil.loadImage("eye", 24, 24);
		ImageIcon selectIcon = LAFUtils.createEmptyIconImage(dimension.width, dimension.height);
		ImageIcon selectNoIcon = LAFUtils.createEmptyIconImage(dimension.width, dimension.height);
		Font font = FontUtils.getFont().deriveFont(Font.BOLD, 14);
		String name = null;
		ImageIcon noSelectIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, buttonIcon, null, name, "8E939D", font);
		ImageIcon rolloverIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, buttonIcon, "FFAD00", name, "FFAD00", font);
		ImageIcon pressedIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, buttonIcon, "4F74FE", name, "4F74FE", font);

		ImageIcon select_icon = LAFUtils.getButtonImageIcon1(selectIcon, buttonIcon, "ffff00", name, "ffff00", font);

		button.setIcon(noSelectIcon);//

		button.setRolloverIcon(rolloverIcon);//

		button.setRolloverSelectedIcon(select_icon);//
		button.setSelectedIcon(select_icon);//

		button.setPressedIcon(pressedIcon);//

		button.setDisabledIcon(select_icon);//
		button.setDisabledSelectedIcon(select_icon);//

		return panel;
	}

	private JPanel getButtonPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(126, TableLayout.FILL, 126, TableLayout.FILL, 126),
				LAFUtils.getDoubleArray(48));
		JButton sendJButton = new JButton(GuiMessages.get("Send"));
		JButton receiveJButton = new JButton(GuiMessages.get("Receive"));
		JButton voteJButton = new JButton(GuiMessages.get("Vote"));

		sendJButton.setUI(new RoundRectButtonUI(new Color(0xffad00)));
		receiveJButton.setUI(new RoundRectButtonUI(new Color(0xffad00)));
		voteJButton.setUI(new RoundRectButtonUI(new Color(0xffad00)));

		panel.add(sendJButton, "0,0");
		panel.add(receiveJButton, "2,0");
		panel.add(voteJButton, "4,0");

		sendJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.chooseView(Action.SHOW_SEND, MainFrame.class.getSimpleName());
			}
		});

		receiveJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.chooseView(Action.SHOW_RECEIVE, MainFrame.class.getSimpleName());
			}
		});

		voteJButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				model.chooseView(Action.SHOW_DELEGATES, MainFrame.class.getSimpleName());
			}
		});

		return panel;
	}

	private JPanel getBottomPanel() {
		JPanel panel = LAFUtils.createPanel(new double[] { 266, 40, TableLayout.FILL },
				new double[] { TableLayout.FILL });

		panel.add(getNodePanel(), "0,0");
		panel.add(getRecordPanel(), "2,0");

		return panel;
	}

	private JPanel getNodePanel() {
		JPanel panel = LAFUtils.createPanel(new double[] { 122, TableLayout.FILL, 122 },
				new double[] { 100, 20, 100, 20, 100, TableLayout.FILL });

		ImageIcon a1ImageIcon = SwingUtil.loadImage("a1", 24, 24);
		ImageIcon a2ImageIcon = SwingUtil.loadImage("a2", 24, 24);
		ImageIcon a3ImageIcon = SwingUtil.loadImage("a3", 24, 24);
		ImageIcon a4ImageIcon = SwingUtil.loadImage("a4", 24, 24);
		ImageIcon a5ImageIcon = SwingUtil.loadImage("a5", 24, 24);

		JPanel p1 = getAPanel(GuiMessages.get("PrimaryValidator"), a1ImageIcon);
		JPanel p2 = getAPanel(GuiMessages.get("BackupValidator"), a2ImageIcon);
		JPanel p3 = getAPanel(GuiMessages.get("NextValidator"), a3ImageIcon);
		JPanel p4 = getAPanel(GuiMessages.get("RoundEndBlock"), a4ImageIcon);
		JPanel p5 = getAPanel(GuiMessages.get("RoundEndTime"), a5ImageIcon);

		panel.add(p1, "0,0");
		panel.add(p2, "2,0");
		panel.add(p3, "0,2");
		panel.add(p4, "2,2");

		panel.add(p5, "0,4,2,4");

		return panel;
	}

	private JPanel getAPanel(String info, ImageIcon icon) {
		JPanel panel = LAFUtils.createRoundRectanglePanel(new double[] { 16, TableLayout.FILL },
				new double[] { 11, TableLayout.PREFERRED, 17, TableLayout.PREFERRED, 7, TableLayout.FILL });
		panel.setBackground(new Color(0x272729));
		JLabel iconLabel = new JLabel(icon);
		//panel.add(iconLabel, "1,1,L,T");

		JLabel nameLabel = new JLabel(" ");

		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14F));
		//panel.add(nameLabel, "1,3,L,T");
		panel.add(nameLabel, "1,1,L,T");

		if (GuiMessages.get("PrimaryValidator").equals(info)) {
			nameLabel.setForeground(BohrGui.getColorYello1());
			primaryValidator = nameLabel;
			primaryValidator.setName("primaryValidator");
		} else if (GuiMessages.get("BackupValidator").equals(info)) {
			backupValidator = nameLabel;
			nameLabel.setForeground(BohrGui.getColorYello2());
			backupValidator.setName("backupValidator");
		} else if (GuiMessages.get("NextValidator").equals(info)) {
			nextValidator = nameLabel;
			nameLabel.setForeground(new Color(0x8E939D));
			nextValidator.setName("nextValidator");
		} else if (GuiMessages.get("RoundEndBlock").equals(info)) {
			nameLabel.setForeground(BohrGui.getColorYello3());
			roundEndBlock = nameLabel;
			roundEndBlock.setName("roundEndBlock");
		} else if (GuiMessages.get("RoundEndTime").equals(info)) {
			nameLabel.setForeground(BohrGui.getColorYello1());
			roundEndTime = nameLabel;
			roundEndTime.setName("roundEndTime");
		}

		JLabel infoLabel = new JLabel(info);
		infoLabel.setFont(infoLabel.getFont().deriveFont(Font.BOLD, 10F));
		infoLabel.setForeground(new Color(0xc3c6cb));
		//panel.add(infoLabel, "1,5,L,T");
		panel.add(infoLabel, "1,3,L,T");

		Border border = iconLabel.getBorder();
		Border margin = new EmptyBorder(0,0,0,20);
		iconLabel.setBorder(new CompoundBorder(border, margin));

		panel.add(iconLabel, "1,5,R,T");


		return panel;
	}

	private void refValidator() {
		Block block = model.getLatestBlock();
		primaryValidator.setText(model.getValidatorDelegate(0).map(Delegate::getNameString).orElse("-"));
		backupValidator.setText(model.getValidatorDelegate(1).map(Delegate::getNameString).orElse("-"));
		nextValidator.setText(model.getNextPrimaryValidatorDelegate().map(Delegate::getNameString).orElse("-"));
		roundEndBlock.setText(model.getNextValidatorSetUpdate().map(String::valueOf).orElse("-"));
		roundEndTime.setText(model.getNextValidatorSetUpdate()
				.map(n -> SwingUtil.formatTimestamp(block.getTimestamp() + (n - block.getNumber() - 1) * 30 * 1000))
				.orElse("-"));
	}

	private JPanel getRecordPanel() {
		JPanel panel = LAFUtils.createRoundRectanglePanel(new double[] { 30, TableLayout.FILL, 30 },
				new double[] { 25, TableLayout.PREFERRED, TableLayout.FILL, 16, 43 });
		panel.setBackground(new Color(0x272729));
		panel.add(getRecordPanelByInfo(), "1,1");

		panel.add(getListPanel(), "1,2");//

		panel.add(getMoreDataPanel(), "1,4");//

		return panel;
	}

	private JPanel getListPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.FILL));
		panel.setBackground(new Color(0x272729));
		transactionsList = new JList<Transaction>(proListModel);
		transactionsList.setBackground(new Color(0x272729));
		transactionsList.setCellRenderer(new ProListRender());
		JScrollPane s = new JScrollPane(transactionsList);
		s.setBorder(new EmptyBorder(0, 0, 0, 0));
		s.setBackground(new Color(0x272729));
		panel.add(s, "0,0");

		refListData();

		return panel;
	}

	class ProListRender extends DefaultListCellRenderer {
		private static final long serialVersionUID = 1L;

		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			if (value instanceof Transaction) {
				Transaction t = (Transaction) value;
				return new ProListLinePanel(t);
			}
			return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		}
	}

	private static final ImageIcon trans_toSelf = SwingUtil.loadImage("trans1", 32, 32);
	private static final ImageIcon trans_out = SwingUtil.loadImage("trans2", 32, 32);
	private static final ImageIcon trans_in = SwingUtil.loadImage("trans3", 32, 32);

	class ProListLinePanel extends JPanel {
		private static final long serialVersionUID = 1L;

		ProListLinePanel(Transaction tx) {
			setOpaque(false);
			double[] colSize = { TableLayout.PREFERRED, 15, TableLayout.FILL, TableLayout.PREFERRED, 5 };
			double[] rowSize = { TableLayout.PREFERRED, 5, TableLayout.PREFERRED, 10 };
			double[][] tableSize = { colSize, rowSize };
			TableLayout layout = new TableLayout(tableSize);
			setLayout(layout);

			boolean inBound = accounts.contains(ByteArray.of(tx.getTo()));
			boolean outBound = accounts.contains(ByteArray.of(tx.getFrom()));

			//
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

			//
			String to = Hex.encode0x(tx.getTo());
			to = StringUtil.hexToBase58(to);
			JLabel labelAddress = new JLabel(to);
			labelAddress.setFont(labelAddress.getFont().deriveFont(Font.BOLD, 12F));
			labelAddress.setForeground(new Color(0x23243b));
			add(labelAddress, "2,0,L,F");

			//
			JLabel lblTime = new JLabel(SwingUtil.formatTimestamp1(tx.getTimestamp()));
			lblTime.setFont(lblTime.getFont().deriveFont(Font.PLAIN, 10F));
			lblTime.setForeground(new Color(0xa2a6ae));
			add(lblTime, "2,2,L,F");

			//
			String mathSign = inBound ? "+" : "-";
			Color col = inBound ? new Color(0xffff00) : new Color(0xffad00);
			String prefix = (inBound && outBound) ? "" : (mathSign);
			JLabel lblAmount = new JLabel(prefix + SwingUtil.formatAmountNoUnit(tx.getValue()));
			lblAmount.setFont(lblAmount.getFont().deriveFont(Font.BOLD, 12F));
			lblAmount.setForeground(col);
			add(lblAmount, "3,0,R,F");

//			JLabel info = new JLabel("Bohr " + GuiMessages.get("Completed"));
			JLabel info = new JLabel(tx.getType().toString());
			info.setFont(info.getFont().deriveFont(Font.PLAIN, 10F));
			info.setForeground(new Color(0xa6aab2));
			add(info, "3,2,R,F");
		}
	}

	private int requestCount = NUMBER_OF_TRANSACTIONS;
	private Set<ByteArray> accounts = new HashSet<>();

	private void refListData() {
		Set<ByteArray> hashes = new HashSet<>();
		List<Transaction> list = new ArrayList<>();
		for (WalletAccount acc : model.getAccounts()) {
			for (Transaction tx : acc.getTransactions()) {
				ByteArray key = ByteArray.of(tx.getHash());
				if (FEDERATED_TRANSACTION_TYPES.contains(tx.getType()) && !hashes.contains(key)) {
					list.add(tx);
					hashes.add(key);
				}
			}
		}
		list.sort((tx1, tx2) -> Long.compare(tx2.getTimestamp(), tx1.getTimestamp()));

		if (requestCount >= list.size()) {
			requestCount = list.size();
		}

		list = list.size() > requestCount ? list.subList(0, requestCount) : list;

		accounts = new HashSet<>();
		for (WalletAccount a : model.getAccounts()) {
			accounts.add(ByteArray.of(a.getKey().toAddress()));
		}

		proListModel.clear();
		for (Transaction tx : list) {
			proListModel.addElement(tx);
		}
		transactionsList.repaint();
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
				//
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
		font = FontUtils.getFont().deriveFont(Font.PLAIN, 14);//
		String name = GuiMessages.get("ShowMore") + " ＞";
		ImageIcon noSelectIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, null, null, name, "9599a3", font);
		ImageIcon rolloverIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, null, null, name, "c1c4cc", font);
		ImageIcon pressedIcon = LAFUtils.getButtonImageIcon1(selectNoIcon, null, null, name, "808183", font);

		ImageIcon select_icon = LAFUtils.getButtonImageIcon1(selectIcon, null, null, name, "808183", font);

		button.setIcon(noSelectIcon);//

		button.setRolloverIcon(rolloverIcon);//

		button.setRolloverSelectedIcon(select_icon);//
		button.setSelectedIcon(select_icon);//

		button.setPressedIcon(pressedIcon);//

		button.setDisabledIcon(select_icon);//
		button.setDisabledSelectedIcon(select_icon);//

		return panel;
	}

	private JPanel getRecordPanelByInfo() {
		JPanel panel = LAFUtils.createPanel(
				new double[] { TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED },
				new double[] { TableLayout.PREFERRED });
		panel.setBackground(new Color(0x272729));
		JLabel label = LAFUtils.createLabel(GuiMessages.get("Transactions")/*  */, Font.BOLD, 20F, 0xffffff);
		panel.add(label, "0,0");

		return panel;
	}


	public static class TransactionPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public TransactionPanel(Transaction tx, boolean inBound, boolean outBound, String description) {
			this.setBorder(new EmptyBorder(10, 10, 10, 10));

			JLabel lblType = new JLabel("");
			String bounding = inBound ? "inbound" : "outbound";
			String name = (inBound && outBound) ? "cycle" : (bounding);
			lblType.setIcon(SwingUtil.loadImage(name, 42, 42));
			String mathSign = inBound ? "+" : "-";
			String prefix = (inBound && outBound) ? "" : (mathSign);
			JLabel lblAmount = new JLabel(prefix + SwingUtil.formatAmount(tx.getValue()));
			lblAmount.setToolTipText(SwingUtil.formatAmount(tx.getValue()));
			lblAmount.setHorizontalAlignment(SwingConstants.RIGHT);

			JLabel lblTime = new JLabel(SwingUtil.formatTimestamp(tx.getTimestamp()));

			JLabel labelAddress = new JLabel(description);
			labelAddress.setForeground(Color.GRAY);

			// @formatter:off
			GroupLayout groupLayout = new GroupLayout(this);
			groupLayout.setHorizontalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
					.createSequentialGroup().addContainerGap().addComponent(lblType).addGap(18)
					.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
							.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblTime, GroupLayout.PREFERRED_SIZE, 169, GroupLayout.PREFERRED_SIZE)
									.addPreferredGap(ComponentPlacement.RELATED, 87, Short.MAX_VALUE).addComponent(
											lblAmount, GroupLayout.PREFERRED_SIZE, 128, GroupLayout.PREFERRED_SIZE))
							.addGroup(groupLayout.createSequentialGroup()
									.addComponent(labelAddress, GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
									.addContainerGap()))));
			groupLayout.setVerticalGroup(groupLayout.createParallelGroup(Alignment.LEADING).addGroup(groupLayout
					.createSequentialGroup()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING, false)
							.addGroup(groupLayout.createSequentialGroup()
									.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
											.addComponent(lblTime, GroupLayout.PREFERRED_SIZE, 19,
													GroupLayout.PREFERRED_SIZE)
											.addComponent(lblAmount, GroupLayout.PREFERRED_SIZE, 19,
													GroupLayout.PREFERRED_SIZE))
									.addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE,
											Short.MAX_VALUE)
									.addComponent(labelAddress))
							.addComponent(lblType, Alignment.LEADING, GroupLayout.PREFERRED_SIZE, 36,
									GroupLayout.PREFERRED_SIZE))
					.addContainerGap()));
			this.setLayout(groupLayout);
			// @formatter:on
		}
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		Action action = Action.valueOf(e.getActionCommand());

		switch (action) {
		case REFRESH:
				refresh();
			break;
		default:
			throw new UnreachableException();
		}
	}

	protected void refresh() {
		refLocked();
		refAvailable();
		refValidator();
	}

}
