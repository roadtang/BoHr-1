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
import java.math.BigDecimal;
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
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

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
import org.bohr.gui.laf.DefaultTextAreaUI;
import org.bohr.gui.laf.DefaultTextFieldUI;
import org.bohr.gui.laf.EmptyBlueArcButtonUI;
import org.bohr.gui.laf.HalfButtonUI;
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
import org.bohr.util.exception.UnreachableException;

public class ContractPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	private transient BohrGui gui;
	private transient WalletModel model;
	private transient Kernel kernel;
	private transient Config config;

	private JLabel lblTo;
	private JButton btnAddressBook;

	private JRadioButton rdbtnCreate;
	private JRadioButton rdbtnCall;
	private JComboBox<AccountItem> selectFrom;
	private JComboBox<AccountItem> selectTo;
	private AutoTextField selectToAutoTextField=new AutoTextField();
	private JTextField txtValue;
	private JTextField txtGas;
	private JTextField txtGasPrice;
	private JTextArea txtData;

	private JPanel rectPanel;
	private JPanel toPanel;

	public ContractPanel(BohrGui gui, JFrame frame) {
		initUI(gui);// ui
	}

	private void initUI(BohrGui gui) {
		this.gui = gui;
		this.model = gui.getModel();
		this.model.addListener(this);

		this.kernel = gui.getKernel();
		this.config = kernel.getConfig();

		JPanel panel = LAFUtils.createRoundRectanglePanel(//
				new double[] { 30, TableLayout.FILL, 30 }, //
				new double[] { 30, //
						TableLayout.PREFERRED, //
						TableLayout.PREFERRED, //
						TableLayout.PREFERRED, //
						TableLayout.PREFERRED, //
						TableLayout.PREFERRED, //
						TableLayout.PREFERRED, //
						TableLayout.FILL });

		rectPanel = panel;

		panel.add(getTypePanel(), "1,1");
		panel.add(getFromPanel(), "1,2");

		panel.add(getToPanel3(), "1,3");

		panel.add(getCountMorePanel(), "1,4");
		panel.add(getDataPanel(), "1,5");
		panel.add(getButtonPanel(), "1,6");

		setLayout(new BorderLayout());
		add(panel);

		toggleToAddress(true);

		refresh();
		clear();
	}

	private void setComFont(JComponent c) {
		FontUtils.setBOLDFont(c, 14);
	}

	private JPanel getTypePanel() {
		JPanel panel = LAFUtils.createPanel(
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 111, TableLayout.PREFERRED, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, TableLayout.PREFERRED, 20));

		JLabel lblType = new JLabel(GuiMessages.get("Type") + ":");
		lblType.setHorizontalAlignment(SwingConstants.RIGHT);
		setComFont(lblType);

		rdbtnCall = new JRadioButton(GuiMessages.get("CallContract"));
		rdbtnCreate = new JRadioButton(GuiMessages.get("DeployContract"));
		ButtonGroup btnGroupDataType = new ButtonGroup();
		btnGroupDataType.add(rdbtnCreate);
		btnGroupDataType.add(rdbtnCall);

		rdbtnCall.setUI(new DefaultRadioButtonUI());
		rdbtnCreate.setUI(new DefaultRadioButtonUI());

		rdbtnCall.addActionListener(e -> toggleToAddress(true));
		rdbtnCreate.addActionListener(e -> toggleToAddress(false));

		rdbtnCall.setSelected(true);

		panel.add(lblType, "0,0,L,F");
		panel.add(rdbtnCall, "0,2");
		panel.add(rdbtnCreate, "2,2");

		return panel;
	}

	private JPanel getFromPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.PREFERRED, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, 45, 20));

		JLabel lblFrom = new JLabel(GuiMessages.get("From"));
		lblFrom.setHorizontalAlignment(SwingConstants.RIGHT);
		setComFont(lblFrom);

		selectFrom = new JComboBox<>();
		selectFrom.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
		selectFrom.setUI(new DefaultComboBoxUI());

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
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, 45, 20));

		toPanel = panel;

		lblTo = new JLabel(GuiMessages.get("To"));
		lblTo.setHorizontalAlignment(SwingConstants.RIGHT);
		setComFont(lblTo);

		selectToAutoTextField = (AutoTextField)SwingUtil.textFieldWithCopyPastePopup(selectToAutoTextField);
		selectToAutoTextField.setName("selectTo");
		selectToAutoTextField.setEditable(true);
		selectToAutoTextField.setUI(new HalfTextFieldUI());

		btnAddressBook = new JButton(GuiMessages.get("AddressBook"));
		btnAddressBook.setName("btnAddressBook");
		btnAddressBook.addActionListener(this);
		btnAddressBook.setActionCommand(Action.SHOW_ADDRESS_BOOK.name());
		btnAddressBook.setUI(new HalfButtonUI());

		panel.add(lblTo, "0,0");
		panel.add(selectToAutoTextField, "0,2,1,2");
		panel.add(btnAddressBook, "3,2");

		return panel;
	}


	private JPanel getCountMorePanel() {
		JPanel panel = LAFUtils.createPanel(
				LAFUtils.getDoubleArray(TableLayout.FILL, 40, TableLayout.FILL, 40, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED));

		JPanel countPanel = getCountPanel();
		JPanel GASPanel = getGASPanel();
		JPanel GAS_PRICEPanel = getGAS_PRICEPanel();

		panel.add(countPanel, "0,0");
		panel.add(GASPanel, "2,0");
		panel.add(GAS_PRICEPanel, "4,0");

		return panel;
	}

	private JPanel getFormatPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.PREFERRED, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, 45, 20));
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

	private JPanel getGASPanel() {
		JPanel panel = getFormatPanel();

		JLabel lblGas = new JLabel("GAS");
		lblGas.setHorizontalAlignment(SwingConstants.RIGHT);
		setComFont(lblGas);

		txtGas = SwingUtil.textFieldWithCopyPastePopup();
		txtGas.setName("txtGas");
		txtGas.setColumns(10);
		txtGas.setActionCommand(Action.SEND.name());
		txtGas.addActionListener(this);
		txtGas.setUI(new DefaultTextFieldUI());

		panel.add(lblGas, "0,0");
		panel.add(txtGas, "0,2,1,2");

		return panel;
	}

	private JPanel getGAS_PRICEPanel() {
		JPanel panel = getFormatPanel();

		JLabel lblGasPrice = new JLabel("GAS PRICE(Bohr)  ");
		lblGasPrice.setHorizontalAlignment(SwingConstants.RIGHT);
		setComFont(lblGasPrice);

		txtGasPrice = SwingUtil.textFieldWithCopyPastePopup();
		txtGasPrice.setName("txtGasPrice");
		txtGasPrice.setColumns(10);
		txtGasPrice.setActionCommand(Action.SEND.name());
		txtGasPrice.addActionListener(this);
		txtGasPrice.setUI(new DefaultTextFieldUI());

		panel.add(lblGasPrice, "0,0");
		panel.add(txtGasPrice, "0,2,1,2");

		return panel;
	}

	private JPanel getDataPanel() {
		JPanel panel = LAFUtils.createPanel(LAFUtils.getDoubleArray(TableLayout.PREFERRED, TableLayout.FILL),
				LAFUtils.getDoubleArray(TableLayout.PREFERRED, 15, 80, 20));

		JLabel lblData = new JLabel("DATA(HEX)");
		lblData.setHorizontalAlignment(SwingConstants.RIGHT);

		txtData = SwingUtil.textAreaWithCopyPastePopup(Hex.PREF);
		txtData.setName("txtData");
		JScrollPane dataPane = new JScrollPane(txtData);
		dataPane.setOpaque(false);
		dataPane.setBorder(new EmptyBorder(0, 0, 0, 0));

		txtData.setUI(new DefaultTextAreaUI());

		panel.add(lblData, "0,0");
		panel.add(dataPane, "0,2,1,2");

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

		JButton btnSend = new JButton("send");
		btnSend.setName("btnSend");
		btnSend.addActionListener(this);
		btnSend.setActionCommand(Action.SEND.name());
		btnSend.setUI(new RoundRectButtonUI(new Color(0xffff00)));

		panel.add(btnClear, "0,0");
		panel.add(btnSend, "2,0");

		return panel;
	}

	private Object getToSelectValue() {
			Object selected = selectToAutoTextField.getSelectedItem();
			return selected;
	}

	private String getTo() {

		Object selected = getToSelectValue();
		String ret = "";

		if (selected instanceof AccountItem) {
			// selected item
			AccountItem accountItem = (AccountItem) selected;
			ret = Hex.encode0x(accountItem.address);
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

	public long getGas() throws ParseException {
		BigDecimal gas = SwingUtil.parseNumber(txtGas.getText().trim());
		return gas.longValue();
	}

	public void setGas(long gas) {
		txtGas.setText(SwingUtil.formatNumber(gas));
	}

	public Amount getGasPrice() throws ParseException {
		return SwingUtil.parseAmount(txtGasPrice.getText().trim());
	}

	public void setGasPrice(Amount a) {
		txtGasPrice.setText(SwingUtil.formatAmountNoUnit(a));
	}

	public String getDataText() {
		return txtData.getText().trim();
	}

	public void setDataText(String dataText) {
		txtData.setText(dataText.trim());
	}

	private void toggleToAddress(boolean visible) {
		lblTo.setVisible(visible);


			selectToAutoTextField.setVisible(visible);


		btnAddressBook.setVisible(visible);

		if (toPanel != null) {
			toPanel.setVisible(visible);
		}

		if (rectPanel != null) {
			rectPanel.validate();
		}

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

			// 'to' contains all current accounts and address book, only update if user
			// isn't interacting with it, and wallet is unlocked
			if (!selectToAutoTextField.isPopupVisible() && kernel.getWallet().isUnlocked()) {

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
			long gas = getGas();
			Amount gasPrice = getGasPrice();
			String data = getDataText();

			byte[] to = Hex.decode0x(getTo());
			byte[] rawData = Hex.decode0x(data);
			boolean isCall = rdbtnCall.isSelected();
			TransactionType type = isCall ? TransactionType.CALL : TransactionType.CREATE;
			to = isCall ? to : Bytes.EMPTY_ADDRESS;

			if (acc == null) {
				showErrorDialog(GuiMessages.get("SelectAccount"));
			} else if (value.isNegative()) {
				showErrorDialog(GuiMessages.get("EnterValidValue"));
			} else if (gas < 21_000) {
				showErrorDialog(GuiMessages.get("EnterValidGas"));
			} else if (gasPrice.lessThan(Amount.ONE)) {
				showErrorDialog(GuiMessages.get("EnterValidGasPrice"));
			} else if (to.length != Key.ADDRESS_LEN) {
				showErrorDialog(GuiMessages.get("InvalidReceivingAddress"));
			} else if (rawData.length > config.spec().maxTransactionDataSize(type)) {
				showErrorDialog(
						GuiMessages.get("InvalidData", config.spec().maxTransactionDataSize(TransactionType.TRANSFER)));
			} else {
				int ret = JOptionPane.showConfirmDialog(this,
						isCall ? GuiMessages.get("CallInfo", Hex.encode0x(to)) : GuiMessages.get("CreateInfo"),
						isCall ? GuiMessages.get("ConfirmCall") : GuiMessages.get("ConfirmCreate"),
						JOptionPane.YES_NO_OPTION);
				if (ret == JOptionPane.YES_OPTION) {
					PendingManager.ProcessingResult result = TransactionSender.send(kernel, acc, type, to, value,
							Amount.ZERO, rawData, gas, gasPrice);
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
		setGas(21_000L);
		setGasPrice(config.poolMinGasPrice());
		setDataText("");
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
