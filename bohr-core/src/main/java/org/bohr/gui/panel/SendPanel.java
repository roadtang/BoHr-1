/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.gui.panel;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.bohr.Kernel;
import org.bohr.config.Config;
import org.bohr.core.Amount;
import org.bohr.core.PendingManager;
import org.bohr.core.TransactionType;
import org.bohr.crypto.CryptoException;
import org.bohr.crypto.Hex;
import org.bohr.crypto.Key;
import org.bohr.gui.Action;
import org.bohr.gui.BohrGui;
import org.bohr.gui.SwingUtil;
import org.bohr.gui.TransactionSender;
import org.bohr.gui.laf.AutoTextField;
import org.bohr.gui.laf.DefaultComboBoxUI;
import org.bohr.gui.laf.DefaultRadioButtonUI;
import org.bohr.gui.laf.DefaultTextFieldUI;
import org.bohr.gui.laf.EmptyBlueArcButtonUI;
import org.bohr.gui.laf.HalfButtonUI;
import org.bohr.gui.laf.HalfComBoBoxUI;
import org.bohr.gui.laf.HalfTextFieldUI;
import org.bohr.gui.laf.RoundRectButtonUI;
import org.bohr.gui.layout.TableLayout;
import org.bohr.gui.model.WalletAccount;
import org.bohr.gui.model.WalletModel;
import org.bohr.gui.uiUtils.FontUtils;
import org.bohr.gui.uiUtils.LAFUtils;
import org.bohr.gui.uiUtils.ModelUtils;
import org.bohr.message.GuiMessages;
import org.bohr.message.LanguageChoiceConfig;
import org.bohr.util.ByteArray;
import org.bohr.util.Bytes;
import org.bohr.util.StringUtil;
import org.bohr.util.exception.UnreachableException;

public class SendPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private transient BohrGui gui;
	private transient WalletModel model;
	private transient Kernel kernel;
	private transient Config config;

	private JComboBox<AccountItem> selectFrom;
	private JComboBox<AccountItem> selectTo;
	private AutoTextField selectToAutoTextField=new AutoTextField();
	private JTextField txtValue;
	private JTextField txtFee;
	private JTextField txtData;
	private JRadioButton rdbtnText;
	private JRadioButton rdbtnHex;

	public SendPanel(BohrGui gui, JFrame frame) {
		initUI(gui);
	}

	private void initUI(BohrGui gui) {
		this.gui = gui;
		this.model = gui.getModel();
		this.model.addListener(this);

		this.kernel = gui.getKernel();
		this.config = kernel.getConfig();

		JPanel panel = LAFUtils.createRoundRectanglePanel(new double[] { 30, TableLayout.FILL, 30 }, new double[] { 30,
				TableLayout.PREFERRED, 30,
				TableLayout.PREFERRED, 30,
				TableLayout.PREFERRED, 30,
				TableLayout.PREFERRED, 40,
				TableLayout.PREFERRED,
				TableLayout.FILL });

		panel.setBackground(new Color(0x272729));

		panel.add(getFromPanel(), "1,1");

		panel.add(getToPanel3(), "1,3");

		panel.add(getCountTranPanel(), "1,5");
		panel.add(getDataPanel(), "1,7");
		panel.add(getButtonPanel(), "1,9");

		setLayout(new BorderLayout());
		add(panel);

		refresh();
		clear();
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

	private JPanel getFromPanel() {
		JPanel panel = getFormatPanel();

		JLabel lblFrom = new JLabel(GuiMessages.get("From"));
		lblFrom.setHorizontalAlignment(SwingConstants.RIGHT);
		setComFont(lblFrom);

		selectFrom = new JComboBox<>();
		selectFrom.setFont(new Font(Font.MONOSPACED, Font.BOLD, 13));
		selectFrom.setUI(new DefaultComboBoxUI());
		selectFrom.setBackground(Color.BLACK);
		selectFrom.setForeground(Color.white);


		panel.add(lblFrom, "0,0");
		panel.add(selectFrom, "0,2,1,2");

		return panel;
	}

	private JPanel getToPanel3() {
		int addressWidth = 100;
		if (LanguageChoiceConfig.is_choose_en()) {
			addressWidth = 150;
		}

		JPanel panel = LAFUtils.createPanel(
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, TableLayout.FILL, 1, addressWidth),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, 45));

		JLabel lblTo = new JLabel(GuiMessages.get("To"));
		lblTo.setHorizontalAlignment(SwingConstants.RIGHT);
		setComFont(lblTo);

		selectToAutoTextField = (AutoTextField)SwingUtil.textFieldWithCopyPastePopup(selectToAutoTextField);
		selectToAutoTextField.setName("selectTo");
		selectToAutoTextField.setEditable(true);
		selectToAutoTextField.setUI(new HalfTextFieldUI());

		JButton btnAddressBook = new JButton(GuiMessages.get("AddressBook"));
		btnAddressBook.setName("btnAddressBook");
		btnAddressBook.addActionListener(this);
		btnAddressBook.setActionCommand(Action.SHOW_ADDRESS_BOOK.name());
		btnAddressBook.setUI(new HalfButtonUI());


		panel.add(lblTo, "0,0");
		panel.add(selectToAutoTextField, "0,2,1,2");
		panel.add(btnAddressBook, "3,2");

		return panel;
	}

	private JPanel getToPanel() {
		int addressWidth = 100;

		if (LanguageChoiceConfig.is_choose_en()) {
			addressWidth = 150;
		}

		JPanel panel = LAFUtils.createPanel(
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, TableLayout.FILL, 1, addressWidth),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, 45));

		JLabel lblTo = new JLabel(GuiMessages.get("To"));
		lblTo.setHorizontalAlignment(SwingConstants.RIGHT);
		setComFont(lblTo);

		selectTo = SwingUtil.comboBoxWithCopyPastePopup();
		selectTo.setName("selectTo");
		selectTo.setEditable(true);
		selectTo.setUI(new HalfComBoBoxUI());

		JButton btnAddressBook = new JButton(GuiMessages.get("AddressBook"));
		btnAddressBook.setName("btnAddressBook");
		btnAddressBook.addActionListener(this);
		btnAddressBook.setActionCommand(Action.SHOW_ADDRESS_BOOK.name());
		btnAddressBook.setUI(new HalfButtonUI());

		panel.add(lblTo, "0,0");
		panel.add(selectTo, "0,2,1,2");
		panel.add(btnAddressBook, "3,2");

		return panel;
	}

	private JPanel getCountTranPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL, 40, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED));

		JPanel countPanel = getCountPanel();
		JPanel tranPanel = getTranPanel();

		panel.add(countPanel, "0,0");
		panel.add(tranPanel, "2,0");

		return panel;
	}

	private JPanel getCountPanel() {
		JPanel panel = getFormatPanel();

		JLabel lblValue = new JLabel(GuiMessages.get("Value") + "(Bohr)");
		lblValue.setHorizontalAlignment(SwingConstants.RIGHT);
		setComFont(lblValue);

		txtValue = SwingUtil.textFieldWithCopyPastePopup();
		txtValue.setName("txtValue");
		txtValue.setColumns(10);
		txtValue.setActionCommand(Action.SEND.name());
		txtValue.addActionListener(this);
		txtValue.setUI(new DefaultTextFieldUI());

		panel.add(lblValue, "0,0");
		panel.add(txtValue, "0,2,1,2");

		return panel;
	}

	private JPanel getTranPanel() {
		JPanel panel = getFormatPanel();

		JLabel lblFee = new JLabel(GuiMessages.get("Fee") + "(Bohr)");
		lblFee.setHorizontalAlignment(SwingConstants.RIGHT);
		lblFee.setToolTipText(GuiMessages.get("FeeTip", SwingUtil.formatAmount(config.spec().minTransactionFee())));
		setComFont(lblFee);

		txtFee = SwingUtil.textFieldWithCopyPastePopup();
		txtFee.setName("txtFee");
		txtFee.setColumns(10);
		txtFee.setActionCommand(Action.SEND.name());
		txtFee.addActionListener(this);
		txtFee.setUI(new DefaultTextFieldUI());

		panel.add(lblFee, "0,0");
		panel.add(txtFee, "0,2,1,2");

		return panel;
	}

	private JPanel getData1Panel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.PREFERRED, TableLayout.FILL,
				TableLayout.PREFERRED, 25, TableLayout.PREFERRED),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, 45));

		JLabel lblData = new JLabel(GuiMessages.get("Data"));
		lblData.setHorizontalAlignment(SwingConstants.RIGHT);
		lblData.setToolTipText(GuiMessages.get("DataTip"));
		setComFont(lblData);

		rdbtnText = new JRadioButton(GuiMessages.get("Text"));
		rdbtnHex = new JRadioButton(GuiMessages.get("Hex"));
		rdbtnText.setUI(new DefaultRadioButtonUI());
		rdbtnHex.setUI(new DefaultRadioButtonUI());
		rdbtnText.setForeground(Color.white);
		rdbtnHex.setForeground(Color.white);

		ButtonGroup btnGroupDataType = new ButtonGroup();
		btnGroupDataType.add(rdbtnText);
		btnGroupDataType.add(rdbtnHex);

		rdbtnText.setSelected(true);

		txtData = SwingUtil.textFieldWithCopyPastePopup();
		txtData.setName("txtData");
		txtData.setColumns(10);
		txtData.setActionCommand(Action.SEND.name());
		txtData.addActionListener(this);
		txtData.setToolTipText(GuiMessages.get("DataTip"));
		txtData.setUI(new DefaultTextFieldUI());

		panel.add(lblData, "0,0");

		panel.add(rdbtnText, "2,0");
		panel.add(rdbtnHex, "4,0");

		panel.add(txtData, "0,2,4,2");

		return panel;
	}

	private JPanel getDataPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.FILL, 40, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED));

		panel.add(getData1Panel(), "0,0");

		return panel;
	}

	private JPanel getButtonPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(126, 15, 126, TableLayout.FILL),
				LAFUtils.getDoubleArray(48));

		JButton btnClear = new JButton(GuiMessages.get("Clear"));
		btnClear.setName("btnClear");
		btnClear.addActionListener(this);
		btnClear.setActionCommand(Action.CLEAR.name());
		btnClear.setUI(new EmptyBlueArcButtonUI(new Color(0xFFAD00)));

		JButton btnSend = new JButton(GuiMessages.get("SendButton")/*  */);
		btnSend.setName("btnSend");
		btnSend.addActionListener(this);
		btnSend.setActionCommand(Action.SEND.name());
		btnSend.setUI(new RoundRectButtonUI(new Color(0xffad00)));

		panel.add(btnClear, "0,0");
		panel.add(btnSend, "2,0");

		return panel;
	}


	private Object getToSelectValue() {
		Object selected = selectToAutoTextField.getText();
		return selected;
	}

	private String getTo() {

		Object selected = getToSelectValue();
		String ret = "";

		if (selected instanceof AccountItem) {
			// selected item
			AccountItem accountItem = (AccountItem) selected;
			ret = StringUtil.hexToBase58(Hex.encode0x(accountItem.address));
		} else if (selected != null) {
			// manually entered
			return selected.toString().trim();
		}

		return ret;
	}

	public void setTo(byte[] address) {
		selectToAutoTextField.setSelectedItem(Hex.encode(address));
	}

	public Amount getValue() throws ParseException {
		return SwingUtil.parseAmount(txtValue.getText().trim());
	}

	public void setValue(Amount a) {
		txtValue.setText(SwingUtil.formatAmountNoUnit(a));
	}

	public Amount getFee() throws ParseException {
		return SwingUtil.parseAmount(txtFee.getText().trim());
	}

	public void setFee(Amount f) {
		txtFee.setText(SwingUtil.formatAmountNoUnit(f));
	}

	public String getData() {
		return txtData.getText().trim();
	}

	public void setData(String dataText) {
		txtData.setText(dataText.trim());
	}

	@Override
	public synchronized void actionPerformed(ActionEvent e) {
		Action action = Action.valueOf(e.getActionCommand());

		switch (action) {
		case REFRESH:
			refresh();
			break;
		case SEND:
			send();
			break;
		case CLEAR:
			clear();
			break;
		case SHOW_ADDRESS_BOOK:
			showAddressBook();
			break;
		default:
			throw new UnreachableException();
		}
	}

	/**
	 * Refreshes the GUI.
	 */
	protected void refresh() {
		List<WalletAccount> list = model.getAccounts();

		List<AccountItem> accountItems = new ArrayList<>();
		for (WalletAccount aList : list) {
			AccountItem accountItem = new AccountItem(aList);
			accountItems.add(accountItem);
		}

		// update account list if user is not interacting with it
		if (!selectFrom.isPopupVisible()) {
			// record selected account
			AccountItem selected = (AccountItem) selectFrom.getSelectedItem();

			selectFrom.removeAllItems();

			for (AccountItem accountItem : accountItems) {
				selectFrom.addItem(accountItem);
			}

			// recover selected account
			if (selected != null) {
				for (AccountItem item : accountItems) {
					if (Arrays.equals(item.address, selected.address)) {
						selectFrom.setSelectedItem(item);
						break;
					}
				}
			}
		}



			if(!selectToAutoTextField.isPopupVisible() && kernel.getWallet().isUnlocked()) {
				// add aliases to list of accounts
				for (Map.Entry<ByteArray, String> address : kernel.getWallet().getAddressAliases().entrySet()) {
					// only add aliases not in wallet
					if (kernel.getWallet().getAccount(address.getKey().getData()) == null) {
						accountItems.add(new AccountItem(address.getValue(), address.getKey().getData()));
					}
				}

				Object toSelected = getToSelectValue();

				selectToAutoTextField.removeAllItems();
				for (AccountItem accountItem : accountItems) {
					selectToAutoTextField.addItem(accountItem);
				}
				selectToAutoTextField.setSelectedItem(toSelected);
			}

	}

	/**
	 * Sends transaction.
	 */
	protected void send() {
		try {
			WalletAccount acc = getSelectedAccount();
			Amount value = getValue();
			Amount fee = getFee();
			String data = getData();

			// decode0x recipient address
			String toBase58 = getTo();
			String toHex = StringUtil.base58ToHex(toBase58);
			byte[] to = Hex.decode0x(toHex);
			byte[] rawData = rdbtnText.isSelected() ? Bytes.of(data) : Hex.decode0x(data);

			if (acc == null) {
				showErrorDialog(GuiMessages.get("SelectAccount"));
			} else if (value.isNotPositive()) {
				showErrorDialog(GuiMessages.get("EnterValidValue"));
			} else if (fee.lessThan(config.spec().minTransactionFee())) {
				showErrorDialog(GuiMessages.get("TransactionFeeTooLow"));
			} else if (value.add(fee).greaterThan(acc.getAvailable())) {
				showErrorDialog(GuiMessages.get("InsufficientFunds", SwingUtil.formatAmount(value.add(fee))));
			} else if (to.length != Key.ADDRESS_LEN) {
				showErrorDialog(GuiMessages.get("InvalidReceivingAddress"));
			} else if (rawData.length > config.spec().maxTransactionDataSize(TransactionType.TRANSFER)) {
				showErrorDialog(
						GuiMessages.get("InvalidData", config.spec().maxTransactionDataSize(TransactionType.TRANSFER)));
			} else {
				byte[] code = kernel.getBlockchain().getAccountState().getCode(to);
				if (code != null && code.length > 0) {
					int ret = JOptionPane.showConfirmDialog(this, GuiMessages.get("SendToContract"),
							GuiMessages.get("SendToContractWarning"), JOptionPane.OK_CANCEL_OPTION);
					if (ret != JOptionPane.OK_OPTION) {
						return;
					}
				}

				int ret = JOptionPane.showConfirmDialog(this,
						GuiMessages.get("TransferInfo", SwingUtil.formatAmountFull(value), StringUtil.hexToBase58(Hex.encode0x(to)) ),
						GuiMessages.get("ConfirmTransfer"), JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					TransactionType type = TransactionType.TRANSFER;
					PendingManager.ProcessingResult result = TransactionSender.send(kernel, acc, type, to, value, fee,
							rawData);
					handleTransactionResult(result);
				}
			}
		} catch (ParseException | CryptoException ex) {
			showErrorDialog(GuiMessages.get("EnterValidValue"));
		}
	}

	/**
	 * Clears all input fields.
	 */
	protected void clear() {
		setTo(Bytes.EMPTY_BYTES);
		setValue(Amount.ZERO);
		setFee(config.spec().minTransactionFee());
		setData("");
	}

	/**
	 * Shows the address book.
	 */
	protected void showAddressBook() {
		gui.getAddressBookDialog().setVisible(true);
	}

	/**
	 * Returns the selected account.
	 *
	 * @return
	 */
	protected WalletAccount getSelectedAccount() {
		AccountItem selected = (AccountItem) selectFrom.getSelectedItem();
		return selected == null ? null
				: model.getAccounts().stream()
						.filter(walletAccount -> Arrays.equals(selected.address, walletAccount.getAddress()))
						.findFirst().orElse(null);
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
			showErrorDialog(GuiMessages.get("TransactionFailed", result.error.toString()));
		}
	}

	/**
	 * Shows an error dialog.
	 *
	 * @param message
	 */
	protected void showErrorDialog(String message) {
		JOptionPane.showMessageDialog(this, message, GuiMessages.get("ErrorDialogTitle"), JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Represents an item in the account drop list.
	 */
	protected static class AccountItem implements Comparable<AccountItem> {
		final byte[] address;
		final String name;

		AccountItem(WalletAccount a) {
			Optional<String> alias = a.getName();

			this.address = a.getKey().toAddress();
			this.name = a.getKey().toAddressBase58() + ", " // address
					+ (alias.map(s -> s + ", ").orElse("")) // alias
					+ SwingUtil.formatAmount(a.getAvailable()); // available
		}

		AccountItem(String alias, byte[] address) {
			this.name = Hex.encode0x(address) + ", " + alias;
			this.address = address;
		}

		@Override
		public String toString() {
			return ModelUtils.getKEY(address);
			//return this.name;
		}

		@Override
		public int compareTo(AccountItem o) {
			return name.compareTo(o.name);
		}
	}
}
