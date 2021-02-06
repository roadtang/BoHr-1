/**
 * Copyright (c) 2019 The Bohr Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.bohr.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Vector;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.metal.MetalLookAndFeel;

import org.bohr.Kernel;
import org.bohr.core.Wallet;
import org.bohr.crypto.Hex;
import org.bohr.gui.dialog.InputDialog;
import org.bohr.gui.laf.DefaultLookAndFeel;
import org.bohr.gui.laf.MyDefaultMetalTheme;
import org.bohr.gui.laf.WhiteSimpleComBoBoxUI;
import org.bohr.gui.layout.AbsLayoutManager;
import org.bohr.gui.layout.TableLayout;
import org.bohr.gui.model.WalletAccount;
import org.bohr.gui.model.WalletModel;
import org.bohr.gui.panel.ContractPanel;
import org.bohr.gui.panel.DelegatesPanel;
import org.bohr.gui.panel.HomePanel;
import org.bohr.gui.panel.ReceivePanel;
import org.bohr.gui.panel.SendPanel;
import org.bohr.gui.panel.TransactionsPanel;
import org.bohr.gui.render.MainFrameUserComboBoxRender;
import org.bohr.gui.uiUtils.ColorUtils;
import org.bohr.gui.uiUtils.FontUtils;
import org.bohr.gui.uiUtils.LAFUtils;
import org.bohr.message.GuiMessages;
import org.bohr.util.StringUtil;
import org.bohr.util.exception.UnreachableException;

import sun.swing.SwingUtilities2;

public class MainFrame extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;

	private transient Kernel kernel;
	private transient WalletModel model;

	private LockGlassPane lockGlassPane;

	private HomePanel panelHome;
	private SendPanel panelSend;
	private ReceivePanel panelReceive;
	private ContractPanel panelContract;
	private TransactionsPanel panelTransactions;
	private DelegatesPanel panelDelegates;
	private StatusBar statusBar;
	private StatusBarNew statusBarNew;

	private AbstractButton btnHome;
	private AbstractButton btnSend;
	private AbstractButton btnReceive;
	private AbstractButton btnContract;
	private AbstractButton btnTransactions;
	private AbstractButton btnDelegates;
	private AbstractButton btnLock;

	private JPanel activePanel;

	private AbstractButton activeButton;

	private BohrGui gui;

	private JComboBox<WalletAccount> userComboBox;

	public MainFrame(BohrGui gui) {
			initUI2(gui);
	}
	
	private void initUI2(BohrGui gui) {
//		MetalLookAndFeel.setCurrentTheme(new MyDefaultMetalTheme());
//		try {
//			//UIManager.setLookAndFeel(new MetalLookAndFeel());
//			UIManager.setLookAndFeel(new DefaultLookAndFeel());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		SwingUtilities.updateComponentTreeUI(this);


		JPanel mainPanel = initSystem(gui);
		mainPanel.setBackground(ColorUtils.createColor("#1E1E1F"));

		double[] mainColSize = { TableLayout.FILL };
		double[] mainRowSize = {100, TableLayout.FILL, 75 };
		double[][] mainSize = { mainColSize, mainRowSize };
		TableLayout mainTableLayout = new TableLayout(mainSize);
		mainPanel.setLayout(mainTableLayout);

		JPanel menuPanel=getMenuPanel();
		JPanel viewPanel=getViewPanel();
		JPanel statusPanel=getStatusPanel();
		
		
		mainPanel.add(menuPanel, "0,0");
		mainPanel.add(viewPanel, "0,1");
		mainPanel.add(statusPanel, "0,2");
	}
	
	private JPanel getTopButtonPanel() {
		panelHome = new HomePanel(gui);
		panelSend = new SendPanel(gui, this);
		panelReceive = new ReceivePanel(gui);
		panelContract = new ContractPanel(gui, this);
		panelTransactions = new TransactionsPanel(gui, this);
		panelDelegates = new DelegatesPanel(gui, this);

		btnHome = new JToggleButton();
		btnSend = new JToggleButton();
		btnReceive = new JToggleButton();
		btnContract = new JToggleButton();
		btnTransactions = new JToggleButton();
		btnDelegates = new JToggleButton();
		btnLock = new JButton();

		ButtonGroup group = new ButtonGroup();
		group.add(btnHome);
		group.add(btnSend);
		group.add(btnReceive);
		//group.add(btnContract);
		group.add(btnTransactions);
		group.add(btnDelegates);

		btnHome.setSelected(true);

		createButton1(btnHome, GuiMessages.get("Home"), "home1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelHome, GuiMessages.get("Home"));
			}
		}, KeyEvent.VK_H);

		createButton1(btnReceive, GuiMessages.get("Receive"), "receive1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelReceive, GuiMessages.get("Receive"));
			}
		}, KeyEvent.VK_R);

		createButton1(btnSend, GuiMessages.get("Send"), "send1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelSend, GuiMessages.get("Send"));
			}
		}, KeyEvent.VK_S);

		//createButton1(btnContract, GuiMessages.get("Contract"), "contract1", new ActionListener() {
		//	@Override
		//	public void actionPerformed(ActionEvent e) {
		//		selectPanel(panelContract, GuiMessages.get("Contract"));
		//	}
		//}, KeyEvent.VK_C);

		createButton1(btnTransactions, GuiMessages.get("Transactions"), "transactions1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelTransactions, GuiMessages.get("Transactions"));
			}
		}, KeyEvent.VK_T);

		createButton1(btnDelegates, GuiMessages.get("Delegates"), "delegates1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelDelegates, GuiMessages.get("Delegates"));
			}
		}, KeyEvent.VK_D);

		createButton1(btnLock, GuiMessages.get("Lock"), "lock1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lock();
			}
		}, KeyEvent.VK_L);
		
		int sp=8;
		
		double[] colSize = {
				TableLayout.PREFERRED,sp,
				TableLayout.PREFERRED,sp,
				TableLayout.PREFERRED,sp,
				TableLayout.PREFERRED,sp,
				TableLayout.PREFERRED,sp,
				TableLayout.PREFERRED,sp,
				TableLayout.PREFERRED,
				};
		double[] rowSize = {5, TableLayout.PREFERRED };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		JPanel panel = new JPanel(layout);
		panel.setOpaque(false);
		buttonPanel = panel;
		
		panel.add(btnHome,"0,1");
		panel.add(btnReceive,"2,1");
		panel.add(btnSend,"4,1");
		//panel.add(btnContract,"6,1");
		panel.add(btnTransactions,"8,1");
		panel.add(btnDelegates,"10,1");
		panel.add(btnLock,"12,1");
		
		return panel;
	}
	
	private JPanel getMenuPanel() {
		double[] colSize = { 40, 
				TableLayout.PREFERRED,
				TableLayout.FILL, TableLayout.PREFERRED,  27, 330, 40 };
		double[] rowSize = {20, 
				TableLayout.PREFERRED,
				TableLayout.FILL };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		JPanel panel = new JPanel(layout);
		panel.setOpaque(false);
		
		panel.add(getTopButtonPanel(), "1,1");
		

		Vector<WalletAccount> items = new Vector<>();

		List<WalletAccount> list = model.getAccounts();
		for (WalletAccount v : list) {
			items.add(v);
		}

		userComboBox = new JComboBox<WalletAccount>(new DefaultComboBoxModel<WalletAccount>(items));
		userComboBox.setRenderer(new MainFrameUserComboBoxRender());
		panel.add(userComboBox, "5,1");

		userComboBox.setFont(userComboBox.getFont().deriveFont(18F));
		userComboBox.setForeground(Color.white);

		userComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int index = userComboBox.getSelectedIndex();
					if (index >= 0) {
						WalletAccount wa = items.get(index);
//						String add = wa.getKey().toAddressString();
						updateUserData(wa);
					}
				}
			}
		});

		userComboBox.setUI(new WhiteSimpleComBoBoxUI() {
			@Override
			public String getListValue(Object value) {
				if (value instanceof WalletAccount) {
					WalletAccount v = (WalletAccount) value;
					String showText = Hex.encode0x(v.getKey().toAddress());
					String txt = StringUtil.hexToBase58(showText);
					return txt;
				}
				return super.getListValue(value);
			}
		});
		
		return panel;
	}
	
	private JPanel getViewPanel() {
		double[] colSize = { 40, TableLayout.FILL, 40 };
		double[] rowSize = {TableLayout.FILL };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		JPanel panel = new JPanel(layout);
		panel.setOpaque(false);
		
		activePanel = new JPanel();
		activePanel.setOpaque(false);
		activePanel.setLayout(new BorderLayout());
		panel.add(activePanel, "1,0");

		selectPanel(panelHome, GuiMessages.get("Home"));
		
		return panel;
	}
	
	private JPanel getStatusPanel() {
		double[] colSize = { 40, TableLayout.FILL, 20 };
		double[] rowSize = {TableLayout.FILL,TableLayout.PREFERRED, 20};
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		JPanel panel = new JPanel(layout);
		panel.setOpaque(false);
		
		statusBarNew = new StatusBarNew();
		model.getSyncProgress().ifPresent(statusBarNew::setProgress);

		panel.add(statusBarNew, "1,1");
		
		return panel;
	}

	private JPanel initSystem(BohrGui gui) {
		this.gui = gui;
		// ensure that all windows are released before it starts closing the Kernel
		this.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// stop GUI threads
				gui.stop();

				// destroy all frames
				for (Frame frame : Frame.getFrames()) {
					frame.setVisible(false);
					frame.dispose();
				}

				// trigger the shutdown-hook of Kernel class then exits the process
				System.exit(0);
			}
		});
		this.model = gui.getModel();
		this.model.addListener(this);

		this.kernel = gui.getKernel();

		lockGlassPane = new LockGlassPane();
		lockGlassPane.setOpaque(false);
		this.setGlassPane(lockGlassPane);

		// setup menu bar
		JMenuBar menuBar = new MenuBar(gui, this);
		this.setJMenuBar(menuBar);

		// setup frame properties
		this.setTitle(GuiMessages.get("BohrWallet"));
		this.setIconImage(SwingUtil.loadImage("logo", 128, 128).getImage());

		int w=960+60+30;
		int h=770;

		Dimension mainPanelDim = new Dimension(w, h);
		JPanel mainPanel = new JPanel();
		mainPanel.setPreferredSize(mainPanelDim);
		mainPanel.setMinimumSize(mainPanelDim);
		getContentPane().add(mainPanel);
		pack();
		setLocationRelativeTo(null);

		Rectangle frameRectangle = getBounds();
		setMinimumSize(frameRectangle.getSize());
		return mainPanel;
	}

	private JLabel titleLabel;

	private JPanel createRightPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(ColorUtils.createColor("#F8F8F8"));

		double[] colSize = { 40, TableLayout.FILL, 40 };
		double[] rowSize = { 100, TableLayout.FILL, 40 };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		panel.setLayout(layout);

		panel.add(getInfoPanel(), "1,0");

		activePanel = new JPanel();
		activePanel.setOpaque(false);
		activePanel.setLayout(new BorderLayout());
		panel.add(activePanel, "1,1");

		selectPanel(panelHome, GuiMessages.get("Home"));

		return panel;
	}


	private Component getInfoPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);

		double[] colSize = { TableLayout.PREFERRED, TableLayout.FILL, TableLayout.PREFERRED, 27, 330 };
		double[] rowSize = { 35, 35, TableLayout.FILL };
		double[][] tableSize = { colSize, rowSize };
		TableLayout layout = new TableLayout(tableSize);
		panel.setLayout(layout);

		titleLabel = new JLabel();
		titleLabel.setFont(titleLabel.getFont().deriveFont(Font.BOLD, 25));
		panel.add(titleLabel, "0,1,L,C");


		Vector<WalletAccount> items = new Vector<>();

		List<WalletAccount> list = model.getAccounts();
		for (WalletAccount v : list) {
			items.add(v);
		}

		userComboBox = new JComboBox<WalletAccount>(new DefaultComboBoxModel<WalletAccount>(items));
		userComboBox.setRenderer(new MainFrameUserComboBoxRender());
		panel.add(userComboBox, "4,1");

		userComboBox.setFont(userComboBox.getFont().deriveFont(18F));
		userComboBox.setForeground(Color.white);

		userComboBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					int index = userComboBox.getSelectedIndex();
					if (index >= 0) {
						WalletAccount wa = items.get(index);
//						String add = wa.getKey().toAddressString();
						updateUserData(wa);
					}
				}
			}
		});

		userComboBox.setUI(new WhiteSimpleComBoBoxUI() {
			@Override
			public String getListValue(Object value) {
				if (value instanceof WalletAccount) {
					WalletAccount v = (WalletAccount) value;
					String showText = Hex.encode0x(v.getKey().toAddress());
					String txt = StringUtil.hexToBase58(showText);
					return txt;
				}
				return super.getListValue(value);
			}
		});

		return panel;
	}

	private void updateUserData(WalletAccount wa) {
		// TODO
	}

	private void updateUserList() {
		if (userComboBox != null) {
			DefaultComboBoxModel<WalletAccount> tmodel = (DefaultComboBoxModel) userComboBox.getModel();
			tmodel.removeAllElements();
			List<WalletAccount> list = model.getAccounts();
			for (WalletAccount v : list) {
				tmodel.addElement(v);
			}
			userComboBox.repaint();
		}
	}



	private JPanel createLeftPanel() {
		JPanel panel = new JPanel();
		panel.setBackground(ColorUtils.createColor("#E2E5F5"));

		double[] mainColSize = { //
				20, //
				TableLayout.FILL };
		double[] mainRowSize = { //
				50, // logo
				TableLayout.PREFERRED, // logo

				20, // logo

				TableLayout.FILL, //

				TableLayout.PREFERRED, //

				50,//

		};
		double[][] mainSize = { mainColSize, mainRowSize };
		TableLayout mainTableLayout = new TableLayout(mainSize);
		panel.setLayout(mainTableLayout);

		// logo
		panel.add(getComPanel(), "1,1");

		panel.add(getButtonListPanel(), "1,3");

		statusBarNew = new StatusBarNew();
		model.getSyncProgress().ifPresent(statusBarNew::setProgress);

		panel.add(statusBarNew, "1,4");

		return panel;
	}

	private JPanel buttonPanel;

	private JPanel getButtonListPanel() {
		panelHome = new HomePanel(gui);
		panelSend = new SendPanel(gui, this);
		panelReceive = new ReceivePanel(gui);
		panelContract = new ContractPanel(gui, this);
		panelTransactions = new TransactionsPanel(gui, this);
		panelDelegates = new DelegatesPanel(gui, this);

		btnHome = new JToggleButton();
		btnSend = new JToggleButton();
		btnReceive = new JToggleButton();
		btnContract = new JToggleButton();
		btnTransactions = new JToggleButton();
		btnDelegates = new JToggleButton();
		btnLock = new JButton();

		ButtonGroup group = new ButtonGroup();
		group.add(btnHome);
		group.add(btnSend);
		group.add(btnReceive);
		//group.add(btnContract);
		group.add(btnTransactions);
		group.add(btnDelegates);

		btnHome.setSelected(true);

		createButton(btnHome, GuiMessages.get("Home"), "home1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelHome, GuiMessages.get("Home"));
			}
		}, KeyEvent.VK_H);

		createButton(btnReceive, GuiMessages.get("Receive"), "receive1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelReceive, GuiMessages.get("Receive"));
			}
		}, KeyEvent.VK_R);

		createButton(btnSend, GuiMessages.get("Send"), "send1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelSend, GuiMessages.get("Send")/*  */);
			}
		}, KeyEvent.VK_S);

		//createButton(btnContract, GuiMessages.get("Contract"), "contract1", new ActionListener() {
		//	@Override
		//	public void actionPerformed(ActionEvent e) {
		//		selectPanel(panelContract, GuiMessages.get("Contract")/*  */);
		//	}
		//}, KeyEvent.VK_C);

		createButton(btnTransactions, GuiMessages.get("Transactions"), "transactions1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelTransactions, GuiMessages.get("Transactions")/*  */);
			}
		}, KeyEvent.VK_T);

		createButton(btnDelegates, GuiMessages.get("Delegates"), "delegates1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				selectPanel(panelDelegates, GuiMessages.get("Delegates")/*  */);
			}
		}, KeyEvent.VK_D);

		createButton(btnLock, GuiMessages.get("Lock"), "lock1", new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				lock();
			}
		}, KeyEvent.VK_L);
		
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		buttonPanel = panel;

		panel.add(btnHome);
		panel.add(btnReceive);
		panel.add(btnSend);
		//panel.add(btnContract);
		panel.add(btnTransactions);
		panel.add(btnDelegates);
		panel.add(btnLock);

		panel.setLayout(new AbsLayoutManager() {
			@Override
			public void layoutContainer(Container parent) {
				int y = 0;
				int space = 55;
				Dimension dim = btnHome.getPreferredSize();
//				space = dim.height;
				btnHome.setBounds(0, y, dim.width, dim.height);
				btnReceive.setBounds(0, y = y + space, dim.width, dim.height);
				btnSend.setBounds(0, y = y + space, dim.width, dim.height);
				btnContract.setBounds(0, y = y + space, dim.width, dim.height);
				btnTransactions.setBounds(0, y = y + space, dim.width, dim.height);
				btnDelegates.setBounds(0, y = y + space, dim.width, dim.height);
				btnLock.setBounds(0, y = y + space, dim.width, dim.height);
			}
		});

		return panel;
	}

	private JPanel getComPanel() {
		JPanel panel = new JPanel();
		panel.setOpaque(false);
		double[] mainColSize = { 45, 10, TableLayout.PREFERRED, TableLayout.FILL };
		double[] mainRowSize = { 45 };
		double[][] mainSize = { mainColSize, mainRowSize };
		TableLayout mainTableLayout = new TableLayout(mainSize);
		panel.setLayout(mainTableLayout);

		JLabel imgLabel = new JLabel(SwingUtil.loadImage("com", 45, 45));

		JLabel nameLabel = new JLabel("Bohr WALLET");
		nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD, 14F));
		nameLabel.setForeground(ColorUtils.createColor("#22243C"));

		panel.add(imgLabel, "0,0");
		panel.add(nameLabel, "2,0,l,c");

		return panel;
	}


	@Override
	public void actionPerformed(ActionEvent e) {
		Action action = Action.valueOf(e.getActionCommand());

		switch (action) {
		case SHOW_HOME:
				btnHome.setSelected(true);
				selectPanel(panelHome, GuiMessages.get("Home"));
			break;
		case SHOW_SEND:
				btnSend.setSelected(true);
				selectPanel(panelSend, GuiMessages.get("Send")/*  */);
			break;
		case SHOW_RECEIVE:
				btnReceive.setSelected(true);
				selectPanel(panelReceive, GuiMessages.get("Receive"));
			break;
		case SHOW_CONTRACT:
				btnContract.setSelected(true);
				selectPanel(panelContract, GuiMessages.get("Contract")/*  */);
			break;
		case SHOW_TRANSACTIONS:
				btnTransactions.setSelected(true);
				selectPanel(panelTransactions, GuiMessages.get("Transactions")/*  */);
			break;
		case SHOW_DELEGATES:
				btnDelegates.setSelected(true);
				selectPanel(panelDelegates, GuiMessages.get("Delegates")/*  */);
			break;
		case LOCK:
			lock();
			break;
		case REFRESH:
			refresh();
			break;
		default:
			throw new UnreachableException();
		}
	}

	/**
	 * Locks the wallet.
	 */
	protected void lock() {
		Wallet w = kernel.getWallet();
		w.lock();

		lockGlassPane.setVisible(true);
		model.fireLockEvent();
//		btnLock.setText(GuiMessages.get("Unlock"));
	}

	/**
	 * Tries to unlock the wallet with the given password.
	 */
	protected boolean unlock(String password) {
		Wallet w = kernel.getWallet();

		if (password != null && w.unlock(password)) {
			lockGlassPane.setVisible(false);
//			btnLock.setText(GuiMessages.get("Lock"));
			return true;
		}

		return false;
	}

	/**
	 * Event listener of ${@link Action#REFRESH}.
	 */
	protected void refresh() {
		if (statusBar != null) {
			// update status bar
			statusBar.setPeersNumber(model.getActivePeers().size());
			model.getSyncProgress().ifPresent(statusBar::setProgress);
		}

		if (statusBarNew != null) {
			model.getSyncProgress().ifPresent(statusBarNew::setProgress);
		}

		//
		updateUserList();
	}

	private static final Border BORDER_NORMAL = new CompoundBorder(new LineBorder(new Color(180, 180, 180)),
			new EmptyBorder(0, 5, 0, 10));
	private static final Border BORDER_FOCUS = new CompoundBorder(new LineBorder(new Color(51, 153, 255)),
			new EmptyBorder(0, 5, 0, 10));

	/**
	 * Selects an tabbed panel to display.
	 *
	 * @param panel
	 * @param button
	 */
	protected void select(JPanel panel, AbstractButton button) {
		if (activeButton != null) {
			activeButton.setBorder(BORDER_NORMAL);
		}
		activeButton = button;
		activeButton.setBorder(BORDER_FOCUS);

		activePanel.removeAll();
		activePanel.add(panel);

		activePanel.revalidate();
		activePanel.repaint();
	}

	protected void selectPanel(JPanel panel, String name) {
		activePanel.removeAll();
		activePanel.add(panel);

		if(titleLabel != null) {
			titleLabel.setText(name);
		}

		activePanel.revalidate();
		activePanel.repaint();
	}

	/**
	 * Creates a button in the tool bar.
	 *
	 * @param name
	 * @param icon
	 * @param action
	 * @return
	 */
	protected JButton createButton(String name, String icon, Action action) {
		JButton btn = new JButton(name);
		btn.setActionCommand(action.name());
		btn.addActionListener(this);
		btn.setIcon(SwingUtil.loadImage(icon, 36, 36));
		btn.setFocusPainted(false);
		btn.setBorder(BORDER_NORMAL);
		btn.setContentAreaFilled(false);
		btn.setFont(btn.getFont().deriveFont(btn.getFont().getStyle() | Font.BOLD));

		Dimension preferredSize = new Dimension(130, 60);//
		btn.setMaximumSize(preferredSize);
		btn.setPreferredSize(preferredSize);
		return btn;
	}

	protected AbstractButton createButton(AbstractButton button, String name, String icon, ActionListener action,
			Integer mnemonic) {
		button.addActionListener(action);
		if (mnemonic != null) {
			button.setMnemonic(mnemonic);
		}

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				button.setEnabled(true);
				buttonPanel.setComponentZOrder(button, 0);
				buttonPanel.validate();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (button.getModel().isSelected()) {
					button.setEnabled(false);
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

		Dimension dimension = new Dimension(202, 77);
		button.setSize(dimension);
		button.setPreferredSize(dimension);
		button.setMaximumSize(dimension);
		button.setMinimumSize(dimension);

		ImageIcon buttonIcon = SwingUtil.loadImage(icon, 24, 24);
		ImageIcon selectIcon = SwingUtil.loadImage("select", dimension.width, dimension.height);
		ImageIcon selectNoIcon = SwingUtil.loadImage("select_no", dimension.width, dimension.height);

		Font font = FontUtils.getFont().deriveFont(Font.BOLD, 18);
		font = button.getFont().deriveFont(Font.BOLD, 18F);

		ImageIcon noSelectIcon = LAFUtils.getButtonImageIcon(selectNoIcon, buttonIcon, null, name, "8E939D", font);
		ImageIcon rolloverIcon = LAFUtils.getButtonImageIcon(selectNoIcon, buttonIcon, "FFAD00", name, "FFAD00", font);
		ImageIcon pressedIcon = LAFUtils.getButtonImageIcon(selectNoIcon, buttonIcon, "4F74FE", name, "4F74FE", font);

		ImageIcon select_icon = LAFUtils.getButtonImageIcon(selectIcon, buttonIcon, "ffff00", name, "ffff00", font);

		button.setIcon(noSelectIcon);//

		button.setRolloverIcon(rolloverIcon);//

		button.setRolloverSelectedIcon(select_icon);//
		button.setSelectedIcon(select_icon);//

		button.setPressedIcon(pressedIcon);//

		button.setDisabledIcon(select_icon);//
		button.setDisabledSelectedIcon(select_icon);//

		return button;
	}
	
	protected AbstractButton createButton1(AbstractButton button, String name, String icon, ActionListener action,
			Integer mnemonic) {
		button.addActionListener(action);
		if (mnemonic != null) {
			button.setMnemonic(mnemonic);
		}

		button.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				button.setEnabled(true);
				buttonPanel.setComponentZOrder(button, 0);
				buttonPanel.validate();
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (button.getModel().isSelected()) {
					button.setEnabled(false);
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
		
		Font	font = button.getFont().deriveFont(Font.BOLD, 18F);
		
        FontMetrics fm = button.getFontMetrics(font);
        
        int width = SwingUtilities2.stringWidth(button, fm, name);
        int height = fm.getHeight();
        
        width=width+2;
        height=height+8;

		Dimension dimension = new Dimension(width, height);
		button.setSize(dimension);
		button.setPreferredSize(dimension);
		button.setMaximumSize(dimension);
		button.setMinimumSize(dimension);

		ImageIcon selectIcon = LAFUtils.createLineIconImage(dimension.width, dimension.height, 0x1E1E1F, 0xFFFF00, 2,4);
		ImageIcon selectNoIcon = LAFUtils.createEmptyIconImage(dimension.width, dimension.height, 0x1E1E1F);

		ImageIcon noSelectIcon = LAFUtils.getButtonImageTop(selectNoIcon, null, null, name, "c0c3c8", font);
		ImageIcon rolloverIcon = LAFUtils.getButtonImageTop(selectNoIcon, null, null, name, "ffad00", font);
		ImageIcon pressedIcon = LAFUtils.getButtonImageTop(selectNoIcon, null, null, name, "5ea0f4", font);
		ImageIcon select_icon = LAFUtils.getButtonImageTop(selectIcon, null, null, name, "ffff00", font);

		button.setIcon(noSelectIcon);//

		button.setRolloverIcon(rolloverIcon);//

		button.setRolloverSelectedIcon(select_icon);//
		button.setSelectedIcon(select_icon);//

		button.setPressedIcon(pressedIcon);//

		button.setDisabledIcon(select_icon);//
		button.setDisabledSelectedIcon(select_icon);//

		return button;
	}

	/**
	 * A gray overlay which shows on top of the GUI to prevent user actions.
	 */
	protected class LockGlassPane extends JPanel {

		private static final long serialVersionUID = 1L;

		public LockGlassPane() {
			this.addMouseListener(new MouseAdapter() {
				@Override
				public void mousePressed(MouseEvent e) {
					String pwd = new InputDialog(MainFrame.this, GuiMessages.get("EnterPassword") + ":", true)
							.showAndGet();

					if (pwd != null && !unlock(pwd)) {
						JOptionPane.showMessageDialog(MainFrame.this, GuiMessages.get("IncorrectPassword"));
					}
				}
			});
			this.addKeyListener(new KeyAdapter() {
				// eats all key events
			});
		}

		@Override
		public void paintComponent(Graphics g) {
			g.setColor(new Color(0, 0, 0, 96));
			g.fillRect(0, 0, this.getWidth(), this.getHeight());
		}
	}
}
