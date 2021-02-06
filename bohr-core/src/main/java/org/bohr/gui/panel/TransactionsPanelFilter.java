/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.gui.panel;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JTextField;

import org.bohr.core.Amount;
import org.bohr.core.TransactionType;
import org.bohr.gui.BohrGui;
import org.bohr.gui.ComboBoxItem;
import org.bohr.gui.SwingUtil;
import org.bohr.util.ByteArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionsPanelFilter {

	private static final Logger logger = LoggerFactory.getLogger(TransactionsPanelFilter.class);

	private transient final BohrGui gui;

	private final JComboBox<ComboBoxItem<TransactionType>> selectType;
	private final JComboBox<ComboBoxItem<byte[]>> selectFrom;
	private final JComboBox<ComboBoxItem<byte[]>> selectTo;
	private final JTextField txtMin;
	private final JTextField txtMax;

	private final TransactionsComboBoxModel<byte[]> fromModel;
	private final TransactionsComboBoxModel<byte[]> toModel;
	private final TransactionsComboBoxModel<TransactionType> typeModel;

	private TransactionsPanel.TransactionsTableModel tableModel;
	private TransactionsPanel.ListDefaultListModel listModel;

	private transient List<TransactionsPanel.StatusTransaction> transactions = new ArrayList<>();

	public TransactionsPanelFilter(BohrGui gui, TransactionsPanel.TransactionsTableModel tableModel) {
		this.gui = gui;
		this.tableModel = tableModel;

		txtMin = new JTextField();
		txtMax = new JTextField();

		toModel = new TransactionsComboBoxModel<>();
		fromModel = new TransactionsComboBoxModel<>();
		typeModel = new TransactionsComboBoxModel<>();

		selectType = new JComboBox<>(typeModel);
		selectFrom = new JComboBox<>(fromModel);
		selectTo = new JComboBox<>(toModel);
	}

	public TransactionsPanelFilter(BohrGui gui, TransactionsPanel.ListDefaultListModel listModel) {
		this.gui = gui;
		this.listModel = listModel;

		txtMin = new JTextField();
		txtMax = new JTextField();

		toModel = new TransactionsComboBoxModel<>();
		fromModel = new TransactionsComboBoxModel<>();
		typeModel = new TransactionsComboBoxModel<>();

		selectType = new JComboBox<>(typeModel);
		selectFrom = new JComboBox<>(fromModel);
		selectTo = new JComboBox<>(toModel);
	}

	/**
	 * Filter transactions if filters are selected
	 *
	 * @return filtered transactions
	 */
	public List<TransactionsPanel.StatusTransaction> getFilteredTransactions() {
		List<TransactionsPanel.StatusTransaction> filtered = new ArrayList<>();
		TransactionType type = typeModel.getSelectedValue();
		byte[] to = toModel.getSelectedValue();
		byte[] from = fromModel.getSelectedValue();
		TransactionType transactionType = typeModel.getSelectedValue();
		Amount min = getAmount(txtMin);
		Amount max = getAmount(txtMax);

		Set<ByteArray> allTo = new HashSet<>();
		Set<ByteArray> allFrom = new HashSet<>();
		Set<TransactionType> allTransactionType = new HashSet<>();

		// add if not filtered out
		for (TransactionsPanel.StatusTransaction transaction : transactions) {
			if (type != null && !transaction.getTransaction().getType().equals(type)) {
				continue;
			}

			if (to != null && !Arrays.equals(to, transaction.getTransaction().getTo())) {
				continue;
			}

			if (from != null && !Arrays.equals(from, transaction.getTransaction().getFrom())) {
				continue;
			}

			if (min != null && transaction.getTransaction().getValue().lessThan(min)) {
				continue;
			}

			if (max != null && transaction.getTransaction().getValue().greaterThan(max)) {
				continue;
			}

			filtered.add(transaction);
			allTo.add(new ByteArray(transaction.getTransaction().getTo()));
			allFrom.add(new ByteArray(transaction.getTransaction().getFrom()));
			allTransactionType.add(transaction.getTransaction().getType());
		}

		// update filters that are not set for reduced filter set if filter not already
		// set on them for further filtering
		if (to == null && !selectTo.isPopupVisible()) {
			toModel.setData(allTo.stream()
					.map(it -> new ComboBoxItem<>(SwingUtil.describeAddress(gui, it.getData()), it.getData()))
					.collect(Collectors.toCollection(TreeSet::new)));
		}

		if (from == null && !selectFrom.isPopupVisible()) {
			fromModel.setData(allFrom.stream()
					.map(it -> new ComboBoxItem<>(SwingUtil.describeAddress(gui, it.getData()), it.getData()))
					.collect(Collectors.toCollection(TreeSet::new)));
		}

		if (transactionType == null && !selectType.isPopupVisible()) {
			Set<ComboBoxItem<TransactionType>> values = allTransactionType.stream().map(it -> new ComboBoxItem<>(it.toString(), it))
			.collect(Collectors.toCollection(TreeSet::new));
			typeModel.setData(values);
		}

		return filtered;
	}

	/**
	 * Parse an amount from freeform text field.
	 */
	private Amount getAmount(JTextField txtField) {
		try {
			String text = txtField.getText();
			if (text != null && !text.isEmpty()) {
				return SwingUtil.parseAmount(text);
			}
		} catch (ParseException e) {
			logger.debug("Unable to parse amount for {}", txtField.getText());
		}

		return null;
	}

	public JComboBox<ComboBoxItem<TransactionType>> getSelectType() {
		return selectType;
	}

	public JComboBox<ComboBoxItem<byte[]>> getSelectFrom() {
		return selectFrom;
	}

	public JComboBox<ComboBoxItem<byte[]>> getSelectTo() {
		return selectTo;
	}

	public void setTransactions(List<TransactionsPanel.StatusTransaction> transactions) {
		this.transactions = transactions;
	}

	public JTextField getTxtMin() {
		return txtMin;
	}

	public JTextField getTxtMax() {
		return txtMax;
	}

	class TransactionsComboBoxModel<T> extends DefaultComboBoxModel<ComboBoxItem<T>> {

		private static final long serialVersionUID = 1L;

		private final ComboBoxItem<T> defaultItem;

		public TransactionsComboBoxModel() {
			this.defaultItem = new ComboBoxItem<>("", null);
		}

		public synchronized void setData(Set<ComboBoxItem<T>> values) {

			Object selected = getSelectedItem();
			removeAllElements();
			addElement(defaultItem);
			for (ComboBoxItem<T> value : values) {
				addElement(value);
			}
			setSelectedItem(selected);
		}

		@Override
		public void setSelectedItem(Object anObject) {
			// only refresh if new object selected
			T existing = getSelectedValue();
			@SuppressWarnings("unchecked")
			T newValue = (T) (anObject instanceof ComboBoxItem ? ((ComboBoxItem<T>) anObject).getValue() : anObject);
			if (!Objects.equals(existing, newValue)) {
				super.setSelectedItem(anObject);
				List<TransactionsPanel.StatusTransaction> filteredTransactions = getFilteredTransactions();
				if (tableModel != null) {
					tableModel.setData(filteredTransactions);
				}
				if (listModel != null) {
					listModel.setData(filteredTransactions);
				}
			}
		}

		T getSelectedValue() {
			@SuppressWarnings("unchecked")
			ComboBoxItem<T> selected = (ComboBoxItem<T>) getSelectedItem();
			return selected != null ? selected.getValue() : null;
		}
	}

}
